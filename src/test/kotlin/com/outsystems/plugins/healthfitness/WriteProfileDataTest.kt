package com.outsystems.plugins.healthfitness

import androidx.health.connect.client.HealthConnectClient
import com.outsystems.plugins.healthfitness.data.HealthFitnessError
import com.outsystems.plugins.healthfitness.data.HealthRecord
import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthConnectHelperMock
import com.outsystems.plugins.healthfitness.repository.HealthConnectRepository
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectDataManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class WriteProfileDataTest {

    private val packageName = "my.package"
    private val databaseMock = DatabaseManagerMock()
    private val dataManager = HealthConnectDataManager(databaseMock)
    private val repository = HealthConnectRepository(dataManager)
    private val healthConnectHelperMock = HealthConnectHelperMock()
    private val healthConnectClientMock: HealthConnectClient = Mockito.mock(HealthConnectClient::class.java)

    // new tests
    @Test
    fun givenInvalidVariableWhenWriteProfileDataThenVariableNotAvailableError() {
        runBlocking {
            repository.writeData(
                HealthRecord.HEART_RATE,
                100.0,
                healthConnectClientMock,
                healthConnectHelperMock,
                packageName,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.WRITE_DATA_NOT_PROFILE_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.WRITE_DATA_NOT_PROFILE_ERROR.message, it.message)
                }
            )
        }

    }

    @Test
    fun givenVariableWithoutPermissionsWhenWriteProfileDataThenVariableNotAuthorizedError() {
        healthConnectHelperMock.variableHasPermissions = false
        runBlocking {
            repository.writeData(
                HealthRecord.WEIGHT,
                100.0,
                healthConnectClientMock,
                healthConnectHelperMock,
                packageName,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.WRITE_DATA_NO_PERMISSIONS.code, it.code)
                    Assert.assertEquals(HealthFitnessError.WRITE_DATA_NO_PERMISSIONS.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenValueOutOfRangeWhenWriteWeightDataThenWriteValueOutOfRangeError() {
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldThrowIllegalArgument = true
        runBlocking {
            repository.writeData(
                HealthRecord.WEIGHT,
                100000.0,
                healthConnectClientMock,
                healthConnectHelperMock,
                packageName,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenValueOutOfRangeWhenWriteHeightDataThenWriteValueOutOfRangeError() {
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldThrowIllegalArgument = true
        runBlocking {
            repository.writeData(
                HealthRecord.HEIGHT,
                -10.0,
                healthConnectClientMock,
                healthConnectHelperMock,
                packageName,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenValueOutOfRangeWhenWriteBodyFatDataThenWriteValueOutOfRangeError() {
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldThrowIllegalArgument = true
        runBlocking {
            repository.writeData(
                HealthRecord.BODY_FAT_PERCENTAGE,
                -10.0,
                healthConnectClientMock,
                healthConnectHelperMock,
                packageName,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenValueOutOfRangeWhenWriteBasalDataThenWriteValueOutOfRangeError() {
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldThrowIllegalArgument = true
        runBlocking {
            repository.writeData(
                HealthRecord.BASAL_METABOLIC_RATE,
                100000.0,
                healthConnectClientMock,
                healthConnectHelperMock,
                packageName,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenValidVariableValidValueWhenWriteProfileDataThenSuccess() {
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldThrowIllegalArgument = false
        runBlocking {
            repository.writeData(
                HealthRecord.WEIGHT,
                78.0,
                healthConnectClientMock,
                healthConnectHelperMock,
                packageName,
                {
                    // do nothing, success
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenSomeExceptionWhenWriteProfileDataThenWriteDataError() {
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldThrowIllegalArgument = false
        healthConnectHelperMock.shouldThrowException = true
        runBlocking {
            repository.writeData(
                HealthRecord.BASAL_METABOLIC_RATE,
                100.0,
                healthConnectClientMock,
                healthConnectHelperMock,
                packageName,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.WRITE_DATA_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.WRITE_DATA_ERROR.message, it.message)
                }
            )
        }
    }

}