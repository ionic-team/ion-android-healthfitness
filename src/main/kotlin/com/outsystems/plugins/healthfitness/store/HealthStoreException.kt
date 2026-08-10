package com.outsystems.plugins.healthfitness.store

import com.outsystems.plugins.healthfitness.data.HealthFitnessError

class HealthStoreException(val error : HealthFitnessError) : Exception() {

}