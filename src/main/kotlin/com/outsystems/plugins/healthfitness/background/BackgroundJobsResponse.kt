package com.outsystems.plugins.healthfitness.background

import com.google.gson.annotations.SerializedName

data class BackgroundJobsResponse(
   @SerializedName("results") var results : List<BackgroundJobsResponseBlock>
)