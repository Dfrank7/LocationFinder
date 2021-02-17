package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setup() {
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource())
    }

    @Test
    fun validateData_ReturnsFalse(){
        val reminderDataItem = ReminderDataItem(
                "title",
                "description", null, 10.0, 10.0
        )
        val value = saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(value, `is`(false))
    }

    @Test
    fun validateData_ReturnsTrue() {
        val reminderDataItem = ReminderDataItem(
                "title",
                "description", "location", 100.0, 100.0
        )
        val value = saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(value, `is`(true))
    }

    @Test
    fun saveReminder_liveData_checkLoading() = mainCoroutineRule.runBlockingTest {
        val reminderDataItem = ReminderDataItem(
                "title",
                "description", "location", 10.0, 10.0
        )
        //currently "showloading" livedata is not assingned anything.

        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)


        //now livedata value should not be null. so checking that is the case.

        //extension function in livedatatestutil is used as discussed in the lecture videos.

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @After
    fun cleanUp() {
        stopKoin()
    }



}