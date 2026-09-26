package com.bharatupadhyay.espnest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bharatupadhyay.espnest.data.repository.DeviceRepository
import com.bharatupadhyay.espnest.di.AppContainer
import com.bharatupadhyay.espnest.domain.ConnectionTarget
import com.bharatupadhyay.espnest.domain.Device
import com.bharatupadhyay.espnest.domain.IpValidator
import com.bharatupadhyay.espnest.domain.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeFormState(
    val ip: String = "192.168.4.1",
    val port: String = "80",
    val ipError: String? = null,
    val portError: String? = null
)

data class HomeUiState(
    val form: HomeFormState = HomeFormState(),
    val devices: List<Device> = emptyList(),
    val network: NetworkMonitor.NetworkState = NetworkMonitor.NetworkState(),
    val selectedDevice: Device? = null,
    val editingDevice: Device? = null,
    val renamingDevice: Device? = null,
    val deletingDevice: Device? = null,
    val editName: String = "",
    val editIp: String = "",
    val editPort: String = "",
    val editNameError: String? = null,
    val editIpError: String? = null,
    val editPortError: String? = null
)

class HomeViewModel(
    private val deviceRepository: DeviceRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val form = MutableStateFlow(HomeFormState())
    private val selectedDevice = MutableStateFlow<Device?>(null)
    private val editingDevice = MutableStateFlow<Device?>(null)
    private val renamingDevice = MutableStateFlow<Device?>(null)
    private val deletingDevice = MutableStateFlow<Device?>(null)
    private val editName = MutableStateFlow("")
    private val editIp = MutableStateFlow("")
    private val editPort = MutableStateFlow("")
    private val editNameError = MutableStateFlow<String?>(null)
    private val editIpError = MutableStateFlow<String?>(null)
    private val editPortError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        combine(form, deviceRepository.observeDevices(), networkMonitor.state) { formState, devices, network ->
            Triple(formState, devices, network)
        },
        combine(selectedDevice, editingDevice, renamingDevice, deletingDevice) { selected, editing, renaming, deleting ->
            arrayOf(selected, editing, renaming, deleting)
        },
        combine(editName, editIp, editPort) { name, ip, port -> Triple(name, ip, port) },
        combine(editNameError, editIpError, editPortError) { n, i, p -> Triple(n, i, p) }
    ) { triple, sheets, edits, errors ->
        val (formState, devices, network) = triple
        HomeUiState(
            form = formState,
            devices = devices,
            network = network,
            selectedDevice = sheets[0],
            editingDevice = sheets[1],
            renamingDevice = sheets[2],
            deletingDevice = sheets[3],
            editName = edits.first,
            editIp = edits.second,
            editPort = edits.third,
            editNameError = errors.first,
            editIpError = errors.second,
            editPortError = errors.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    private val _connectEvent = MutableStateFlow<ConnectionTarget?>(null)
    val connectEvent: StateFlow<ConnectionTarget?> = _connectEvent.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.seedIfEmpty()
        }
    }

    fun onIpChange(value: String) {
        form.update { it.copy(ip = value, ipError = null) }
    }

    fun onPortChange(value: String) {
        form.update { it.copy(port = value.filter { ch -> ch.isDigit() }.take(5), portError = null) }
    }

    fun connectFromForm(): ConnectionTarget? {
        val current = form.value
        val ipError = IpValidator.validateIp(current.ip)
        val portError = IpValidator.validatePort(current.port)
        form.update { it.copy(ipError = ipError, portError = portError) }
        if (ipError != null || portError != null) return null
        val port = IpValidator.parsePort(current.port) ?: return null
        val target = ConnectionTarget(
            name = null,
            ip = current.ip.trim(),
            port = port
        )
        _connectEvent.value = target
        return target
    }

    fun connectDevice(device: Device): ConnectionTarget {
        dismissSheets()
        val target = device.toTarget()
        _connectEvent.value = target
        return target
    }

    fun consumeConnectEvent() {
        _connectEvent.value = null
    }

    fun openDeviceSheet(device: Device) {
        selectedDevice.value = device
    }

    fun dismissSheets() {
        selectedDevice.value = null
        editingDevice.value = null
        renamingDevice.value = null
        deletingDevice.value = null
        editNameError.value = null
        editIpError.value = null
        editPortError.value = null
    }

    fun toggleFavorite(device: Device) {
        viewModelScope.launch { deviceRepository.toggleFavorite(device) }
    }

    fun startRename(device: Device) {
        selectedDevice.value = null
        renamingDevice.value = device
        editName.value = device.name
        editNameError.value = null
    }

    fun startEdit(device: Device) {
        selectedDevice.value = null
        editingDevice.value = device
        editName.value = device.name
        editIp.value = device.ip
        editPort.value = device.port.toString()
        editNameError.value = null
        editIpError.value = null
        editPortError.value = null
    }

    fun startDelete(device: Device) {
        selectedDevice.value = null
        deletingDevice.value = device
    }

    fun onEditName(value: String) {
        editName.value = value
        editNameError.value = null
    }

    fun onEditIp(value: String) {
        editIp.value = value
        editIpError.value = null
    }

    fun onEditPort(value: String) {
        editPort.value = value.filter { it.isDigit() }.take(5)
        editPortError.value = null
    }

    fun confirmRename() {
        val device = renamingDevice.value ?: return
        val error = IpValidator.validateName(editName.value)
        if (error != null) {
            editNameError.value = error
            return
        }
        viewModelScope.launch {
            deviceRepository.update(device.copy(name = editName.value.trim()))
            dismissSheets()
        }
    }

    fun confirmEdit() {
        val device = editingDevice.value ?: return
        val nameError = IpValidator.validateName(editName.value)
        val ipError = IpValidator.validateIp(editIp.value)
        val portError = IpValidator.validatePort(editPort.value)
        editNameError.value = nameError
        editIpError.value = ipError
        editPortError.value = portError
        if (nameError != null || ipError != null || portError != null) return
        val port = IpValidator.parsePort(editPort.value) ?: return
        viewModelScope.launch {
            val existing = deviceRepository.getByAddress(editIp.value.trim(), port)
            if (existing != null && existing.id != device.id) {
                editIpError.value = "A device with this address is already saved"
                return@launch
            }
            deviceRepository.update(
                device.copy(
                    name = editName.value.trim(),
                    ip = editIp.value.trim(),
                    port = port
                )
            )
            dismissSheets()
        }
    }

    fun confirmDelete() {
        val device = deletingDevice.value ?: return
        viewModelScope.launch {
            deviceRepository.delete(device)
            dismissSheets()
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(
                        deviceRepository = container.deviceRepository,
                        networkMonitor = container.networkMonitor
                    ) as T
                }
            }
    }
}
