package io.ionic.libs.ionhealthfitnesslib.helpers

import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthRecordInputMethod
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthRecordMetadata
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthRecordSourceDevice

internal fun Metadata.toHealthRecordMetadata(): HealthRecordMetadata = HealthRecordMetadata(
    inputMethod = this.recordingMethod.toInputMethod(),
    originAppPackageName = this.dataOrigin.packageName,
    device = this.device?.toHealthRecordSourceDevice()
)

private fun @receiver:Metadata.Companion.RecordingMethod Int.toInputMethod(): HealthRecordInputMethod =
    when (this) {
        Metadata.RECORDING_METHOD_MANUAL_ENTRY -> HealthRecordInputMethod.MANUAL
        Metadata.RECORDING_METHOD_ACTIVELY_RECORDED -> HealthRecordInputMethod.ACTIVELY_RECORDED
        Metadata.RECORDING_METHOD_AUTOMATICALLY_RECORDED -> HealthRecordInputMethod.AUTOMATIC
        Metadata.RECORDING_METHOD_UNKNOWN -> HealthRecordInputMethod.UNKNOWN
        else -> HealthRecordInputMethod.UNKNOWN
    }

private fun Device.toHealthRecordSourceDevice(): HealthRecordSourceDevice =
    HealthRecordSourceDevice(
        type = this.type.toSourceDeviceType(),
        manufacturer = this.manufacturer,
        model = this.model
    )

private fun @receiver:Device.DeviceType Int.toSourceDeviceType(): HealthRecordSourceDevice.Type =
    when (this) {
        Device.TYPE_PHONE -> HealthRecordSourceDevice.Type.PHONE
        Device.TYPE_WATCH -> HealthRecordSourceDevice.Type.WATCH
        Device.TYPE_SCALE -> HealthRecordSourceDevice.Type.SCALE
        Device.TYPE_RING -> HealthRecordSourceDevice.Type.RING
        Device.TYPE_HEAD_MOUNTED -> HealthRecordSourceDevice.Type.HEAD_MOUNTED
        Device.TYPE_FITNESS_BAND -> HealthRecordSourceDevice.Type.FITNESS_BAND
        Device.TYPE_CHEST_STRAP -> HealthRecordSourceDevice.Type.CHEST_STRAP
        Device.TYPE_SMART_DISPLAY -> HealthRecordSourceDevice.Type.SMART_DISPLAY
        Device.TYPE_UNKNOWN -> HealthRecordSourceDevice.Type.UNKNOWN
        else -> HealthRecordSourceDevice.Type.UNKNOWN
    }