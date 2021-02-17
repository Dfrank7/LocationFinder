package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantexecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminder_RetrieveFromDb_BothareSame() = runBlockingTest {
        val reminderDTO = ReminderDTO(
                "test title",
                "test description", "test location", 1.0, 2.0, "id1"
        )
        database.reminderDao().saveReminder(reminderDTO)

        //Retrieving data from database
        val dataFromSource = database.reminderDao().getReminderById(reminderDTO.id)


        assertThat<ReminderDTO>(dataFromSource as ReminderDTO, CoreMatchers.not(CoreMatchers.nullValue()))
        assertThat(dataFromSource.id, `is`(reminderDTO.id))
        assertThat(dataFromSource.title, `is`(reminderDTO.title))
        assertThat(dataFromSource.description, `is`(reminderDTO.description))
        assertThat(dataFromSource.location, `is`(reminderDTO.location))
        assertThat(dataFromSource.latitude, `is`(reminderDTO.latitude))
        assertThat(dataFromSource.longitude, `is`(reminderDTO.longitude))

    }


    @Test
    fun gettingDataFromDb_returnsNull() = runBlockingTest {

        //WHEN- INVALID DATA IS RETRIEVED FROM DATABASE.
        val dataFromSource = database.reminderDao().getReminderById("id")

        //THEN- NULL VALUE IS RETURNED.
        assertThat(dataFromSource, `is`(CoreMatchers.nullValue()))

    }

}