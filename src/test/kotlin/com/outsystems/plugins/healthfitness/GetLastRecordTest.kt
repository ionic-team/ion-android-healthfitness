package com.outsystems.plugins.healthfitness

import androidx.health.connect.client.HealthConnectClient
import com.outsystems.plugins.healthfitness.data.HealthFitnessError
import com.outsystems.plugins.healthfitness.data.HealthRecord
import com.outsystems.plugins.healthfitness.data.types.HealthRecordInputMethod
import com.outsystems.plugins.healthfitness.data.types.HealthRecordMetadata
import com.outsystems.plugins.healthfitness.data.types.HealthRecordSourceDevice
import com.outsystems.plugins.healthfitness.data.types.HealthRecordSourceDevice.Type
import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthConnectHelperMock
import com.outsystems.plugins.healthfitness.repository.HealthConnectRepository
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectDataManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class GetLastRecordTest {

    private val databaseMock = DatabaseManagerMock()
    private val dataManager = HealthConnectDataManager(databaseMock)
    private val repository = HealthConnectRepository(dataManager)
    private val healthConnectHelperMock = HealthConnectHelperMock()
    private val healthConnectClientMock: HealthConnectClient = Mockito.mock(HealthConnectClient::class.java)

    @Test
    fun givenVariableWithoutPermissionsWhenGetLastRecordThenVariableNotAuthorizedError() {
        healthConnectHelperMock.variableHasPermissions = false
        runBlocking {
            repository.getLastRecord(
                HealthRecord.STEPS,
                healthConnectClientMock,
                healthConnectHelperMock,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.READ_DATA_NO_PERMISSIONS.code, it.code)
                    Assert.assertEquals(HealthFitnessError.READ_DATA_NO_PERMISSIONS.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenValidVariableAndNoRecordsWhenGetLastRecordThenShouldReturnEmpty(){
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldReturnEmptyList = true
        runBlocking {

            repository.getLastRecord(HealthRecord.STEPS,
                    healthConnectClientMock,
                    healthConnectHelperMock,
                    {
                        Assert.assertEquals(it.result.size, 0)
                    },
                    {
                        Assert.fail()
                    }
                )
        }
    }

    @Test
    fun givenValidVariableAndOneResultWhenGetLastRecordThenSuccess(){
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldReturnEmptyList = false
        healthConnectHelperMock.shouldThrowException = false
        healthConnectHelperMock.shouldReturnBloodPressure = false

        runBlocking {
            repository.getLastRecord( HealthRecord.STEPS,
                healthConnectClientMock,
                healthConnectHelperMock,
                {
                    Assert.assertEquals(it.result.size, 1)
                    val firstValue = it.result[0]
                    Assert.assertEquals(firstValue,2f)
                    val metadata = it.metadata!!
                    Assert.assertEquals(
                        metadata,
                        HealthRecordMetadata(
                            HealthRecordInputMethod.AUTOMATIC,
                            "",
                            HealthRecordSourceDevice(
                                Type.WATCH,
                                manufacturer = "Samsung",
                                model = "Galaxy Watch"
                            )
                        )
                    )
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenBloodPressureVariableWhenGetLastRecordThenSuccess(){
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldReturnEmptyList = false
        healthConnectHelperMock.shouldThrowException = false
        healthConnectHelperMock.shouldReturnBloodPressure = true

        runBlocking {
            repository.getLastRecord( HealthRecord.BLOOD_PRESSURE,
                healthConnectClientMock,
                healthConnectHelperMock,
                {
                    Assert.assertEquals(it.result.size, 2)
                    Assert.assertEquals(it.result[0],30f)
                    Assert.assertEquals(it.result[1],30f)
                    Assert.assertEquals(
                        it.metadata?.inputMethod,
                        HealthRecordInputMethod.UNKNOWN
                    )
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenOxygenSaturationVariableWhenGetLastRecordThenSuccess(){
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldReturnEmptyList = false
        healthConnectHelperMock.shouldThrowException = false
        healthConnectHelperMock.shouldReturnBloodPressure = false
        healthConnectHelperMock.shouldReturnOxygenSaturation = true

        runBlocking {
            repository.getLastRecord( HealthRecord.OXYGEN_SATURATION,
                healthConnectClientMock,
                healthConnectHelperMock,
                {
                    Assert.assertEquals(1, it.result.size)
                    Assert.assertEquals(99.7f, it.result[0])
                    Assert.assertEquals(
                        it.metadata?.inputMethod,
                        HealthRecordInputMethod.UNKNOWN
                    )
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenValidVariableButAnExceptionIsThrownWhenGetLastRecordThenExpectedError(){
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldThrowException = true
        healthConnectHelperMock.shouldReturnEmptyList = false

        runBlocking {
            repository.getLastRecord( HealthRecord.STEPS,
                healthConnectClientMock,
                healthConnectHelperMock,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.READ_DATA_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.READ_DATA_ERROR.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenBodyTemperatureVariableWhenGetLastRecordThenSuccess(){
        healthConnectHelperMock.variableHasPermissions = true
        healthConnectHelperMock.shouldReturnEmptyList = false
        healthConnectHelperMock.shouldThrowException = false
        healthConnectHelperMock.shouldReturnBloodPressure = false
        healthConnectHelperMock.shouldReturnBodyTemperature = true

        runBlocking {
            repository.getLastRecord( HealthRecord.BODY_TEMPERATURE,
                healthConnectClientMock,
                healthConnectHelperMock,
                {
                    Assert.assertEquals(1, it.result.size)
                    Assert.assertEquals(39.0f, it.result[0])
                    Assert.assertEquals(
                        it.metadata?.inputMethod,
                        HealthRecordInputMethod.UNKNOWN
                    )
                },
                {
                    Assert.fail()
                }
            )
        }
    }
}