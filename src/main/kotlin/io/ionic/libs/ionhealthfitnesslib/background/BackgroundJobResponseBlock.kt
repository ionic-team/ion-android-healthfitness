package io.ionic.libs.ionhealthfitnesslib.background

import com.google.gson.annotations.SerializedName

data class BackgroundJobsResponseBlock (
    @SerializedName("variable") val variable: String?,
    @SerializedName("condition") val condition: String?,
    @SerializedName("value") val value: Float?,
    @SerializedName("notificationHeader") val notificationHeader: String?,
    @SerializedName("notificationBody") val notificationBody: String?,
    @SerializedName("notificationFrequency") val notificationFrequency: String?,
    @SerializedName("notificationFrequencyGrouping") val notificationFrequencyGrouping: Int?,
    @SerializedName("active") val active: Boolean?,
    @SerializedName("id") val id: String?
)