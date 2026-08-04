package io.ionic.libs.ionhealthfitnesslib.store

import io.ionic.libs.ionhealthfitnesslib.data.HealthFitnessError

class HealthStoreException(val error : HealthFitnessError) : Exception() {

}