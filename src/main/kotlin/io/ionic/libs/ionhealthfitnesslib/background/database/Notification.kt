package io.ionic.libs.ionhealthfitnesslib.background.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.SecureRandom

@Entity
class Notification{

    // Should we use UUID for this?
    @PrimaryKey(autoGenerate = true) var id: Long
    @ColumnInfo(name = "notification_id") var notificationID: Int
    @ColumnInfo(name = "title") var title: String = ""
    @ColumnInfo(name = "body") var body: String = ""

    init {
        id = 0
        notificationID = computeAutoIncrementId()
    }

    fun update(title: String, body: String) {
        this.title = title
        this.body = body
    }

    private fun computeAutoIncrementId() : Int {
        val random = SecureRandom()
        return random.nextInt()
    }

    companion object {
        const val TABLE_NAME: String= "notification"
    }
}