package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.annotation.Config
import org.koin.test.KoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest : KoinTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource
    lateinit var appContext: Application

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as FakeDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as FakeDataSource
                )
            }
            single { FakeDataSource() }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }
        dataSource = get()
        remindersListViewModel = get()

    }


    @Test
    fun callInvalidatefunction_DataValueisNotNull() = mainCoroutineRule.runBlockingTest{

        remindersListViewModel.loadReminders()
        val value = remindersListViewModel.showNoData.getOrAwaitValue()

        assertThat(value, not(false))
    }

    //test with livedata value indicating an error.
    @Test
    fun loadReminders_withEmptyData_snackBarlivedata_showasError() {
        //the following line ensures that if list is empty it returns error.
        dataSource.returnError = true

        remindersListViewModel.loadReminders()
        val value = remindersListViewModel.showSnackBar.getOrAwaitValue()

        assertThat(value, not(nullValue()))
        assertThat(value, `is`("Empty Data"))
    }

    @Test
    fun reminders_liveData_checkLoading() = mainCoroutineRule.runBlockingTest {

        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        //checking if livedata is true.

        //extension function in livedatatestutil is used as discussed in the tutorial videos.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        //after executing, the livedata value should be false.
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @After
    fun cleanUp() {
        stopKoin()
    }

}