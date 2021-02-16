package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.lang.Exception

class FakeReminderDataSource : ReminderDataSource {
    val reminderList = mutableListOf<ReminderDTO>()
    var returnError: Boolean = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return reminderList?.let {
            Result.Success(it)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return try {
            Result.Success(reminderList.first {
                it.id == id
            })
        } catch (exception: Exception) {
            Result.Error("No data found.")
        }
    }

    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }

}