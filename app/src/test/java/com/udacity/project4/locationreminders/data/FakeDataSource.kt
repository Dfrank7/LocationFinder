package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    val reminderList : MutableList<ReminderDTO>? = mutableListOf()
    var returnError = false

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminderList?.let {
            return Result.Success(ArrayList(it))
        }
        return Result.Error("Empty Data")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminderList?.let {
            return Result.Success(it.first { reminder->
                reminder.id ==id
            })
        }

        return Result.Error("none found")
    }

    override suspend fun deleteAllReminders() {
        reminderList?.clear()
    }


}