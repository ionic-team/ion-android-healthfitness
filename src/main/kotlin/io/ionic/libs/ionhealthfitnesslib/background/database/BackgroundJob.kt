package io.ionic.libs.ionhealthfitnesslib.background.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.ionic.libs.ionhealthfitnesslib.background.UpdateBackgroundJobParameters
import java.util.UUID

@Entity(
    tableName = BackgroundJob.TABLE_NAME,
    foreignKeys = [ForeignKey(
        entity = Notification::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("notification_id"),
        onDelete = ForeignKey.CASCADE)],
    indices = [Index(
        "variable", "comparison", "value",
        name="unique_bg_job",
        unique=true)]
)
open class BackgroundJob {

    enum class ComparisonOperationEnum(val id : String) {
        GREATER("HIGHER"),
        GREATER_OR_EQUALS("HIGHER_EQUAL"),
        LESS("LOWER"),
        LESS_OR_EQUALS("LOWER_EQUAL"),
        EQUALS("EQUAL"),
    }

    @PrimaryKey
    @ColumnInfo(name = "id") var id: String = UUID.randomUUID().toString()
    @ColumnInfo(name = "variable") var variable: String = ""
    @ColumnInfo(name = "comparison") var comparison: String = ""
    @ColumnInfo(name = "value") var value: Float = 0.0f
    @ColumnInfo(name = "time_unit") var timeUnit: String? = null
    @ColumnInfo(name = "time_unit_grouping") var timeUnitGrouping: Int? = null
    @ColumnInfo(name = "notification_id") var notificationId: Long? = null
    @ColumnInfo(name = "notification_frequency") var notificationFrequency: String = "ALWAYS"
    @ColumnInfo(name = "notification_frequency_grouping") var notificationFrequencyGrouping: Int = 1
    @ColumnInfo(name = "next_notification_timestamp") var nextNotificationTimestamp: Long = 0
    @ColumnInfo(name = "isActive") var isActive: Boolean = true

    fun update(parameters: UpdateBackgroundJobParameters) {
        if(parameters.value != null){
            this.value = parameters.value
        }
        if(parameters.condition != null){
            this.comparison = parameters.condition
        }
        if(parameters.isActive != null){
            this.isActive = parameters.isActive
        }
        if(parameters.notificationFrequency != null){
            this.notificationFrequency = parameters.notificationFrequency
            this.nextNotificationTimestamp = 0
        }
        if(parameters.notificationFrequencyGrouping != null){
            this.notificationFrequencyGrouping = parameters.notificationFrequencyGrouping
            this.nextNotificationTimestamp = 0
        }
    }

    companion object {
        const val TABLE_NAME: String= "backgroundJob_2"
    }

}