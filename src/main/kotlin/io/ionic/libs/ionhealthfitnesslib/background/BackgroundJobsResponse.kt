package io.ionic.libs.ionhealthfitnesslib.background

import com.google.gson.annotations.SerializedName

data class BackgroundJobsResponse(
   @SerializedName("results") var results : List<BackgroundJobsResponseBlock>
)