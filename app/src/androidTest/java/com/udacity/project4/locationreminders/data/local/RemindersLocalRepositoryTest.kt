package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    // Executes each task synchronously
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        )
                .allowMainThreadQueries()
                .build()
        remindersLocalRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun close() = database.close()

    @Test
    fun insertReminder_RetrieveFromRepoById_BothAreTheSame() = runBlocking {
        //GIVEN- REMINDER IS INSERTED IN TO DATABASE.
        val reminderDTO = ReminderDTO(
                "test title",
                "test description", "test location", 10.0, 20.0, "id"
        )
        remindersLocalRepository.saveReminder(reminderDTO)

        //WHEN- DATA IS RETRIEVED FROM DATABASE.
        val dataFromSource = remindersLocalRepository.getReminder(reminderDTO.id)

        //THEN- BOTH DATA ARE SAME.
        assertThat(
                dataFromSource,
                CoreMatchers.not(CoreMatchers.nullValue())
        )
        dataFromSource as Result.Success

        assertThat(dataFromSource.data.id, `is`(reminderDTO.id))
        assertThat(dataFromSource.data.title, `is`(reminderDTO.title))
        assertThat(dataFromSource.data.description, `is`(reminderDTO.description))
        assertThat(dataFromSource.data.location, `is`(reminderDTO.location))
        assertThat(dataFromSource.data.latitude, `is`(reminderDTO.latitude))
        assertThat(dataFromSource.data.longitude, `is`(reminderDTO.longitude))
    }


    @Test
    fun retrieveInvalidData_returnsResultError() = runBlocking {
        //GIVEN THE REPOSITORY.

        //WHEN- INVALID DATA IS RETRIEVED FROM REPOSITORY.
        val dataFromSource = remindersLocalRepository.getReminder("test id")

        //THEN- RETURNS RESULT.ERROR.
        assertThat(
                dataFromSource,
                CoreMatchers.not(CoreMatchers.nullValue())
        )
        dataFromSource as Result.Error
        assertThat(
                dataFromSource,
                `is`(Result.Error("Reminder not found!"))
        )
    }


}