package io.github.thanosfisherman.blueflow.sample.usecase

import android.bluetooth.BluetoothDevice
import android.util.Log
import io.github.thanosfisherman.blueflow.BlueFlow
import io.github.thanosfisherman.blueflow.BluetoothDeviceWrapper
import io.github.thanosfisherman.blueflow.sample.BtDiscoveryState
import io.github.thanosfisherman.blueflow.sample.activity.TAG
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

@ExperimentalCoroutinesApi
class DiscoverBtDevicesUseCase(private val blueFlow: BlueFlow) {
    private val bondedDevicesList = mutableListOf<BluetoothDeviceWrapper>()
    private val devicesList = mutableListOf<BluetoothDeviceWrapper>()

    fun getBondedDevices(): List<BluetoothDevice>? {
        return blueFlow.bondedDevices()?.toList()
    }

    fun startDiscovery(): Boolean {
        if (!blueFlow.isDiscovering())
            return blueFlow.startDiscovery()
        return true
    }

    fun cancelDiscovery(): Boolean {
        if (blueFlow.isDiscovering())
            return blueFlow.cancelDiscovery()
        return true
    }

    fun discoverBondedDevices() = flow<BtDiscoveryState> {
        bondedDevicesList.clear()
        with(bondedDevicesList) {
            getBondedDevices()?.forEach {
                add(BluetoothDeviceWrapper(it, 0))
                val deviceWrapperList = this.distinctBy { devList -> devList.bluetoothDevice.address }
                emit(BtDiscoveryState.BtDiscoverySuccess(deviceWrapperList))
            }
        }
    }

    fun discoverDevices() = flow {
        devicesList.clear()
        cancelDiscovery()
        Log.i(TAG, "IS DISCOVERY ACTUALLY STARTED? " + startDiscovery())

        try {
            blueFlow.discoverDevices().collect { device ->
                Log.i(TAG, "FOUND DEVICE $device")
                val deliveredDevices = with(devicesList) {
                    add(device)
                    distinctBy { it.bluetoothDevice.address }.map { it }
                }
                emit(BtDiscoveryState.BtDiscoverySuccess(deliveredDevices))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(BtDiscoveryState.BtDiscoveryError)
        }
    }
}