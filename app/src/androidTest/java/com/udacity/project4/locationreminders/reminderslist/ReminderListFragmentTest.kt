package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private lateinit var fakeRepo: FakeReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                        appContext,
                        get() as FakeReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                        appContext,
                        get() as FakeReminderDataSource
                )
            }
            single { FakeReminderDataSource() }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }
        //Get our real repository
        fakeRepo = get()

//        clear the data to start fresh
        runBlocking {
            fakeRepo.deleteAllReminders()
        }
    }

    @Test
    fun onStartFragment_fabVisible() = runBlocking<Unit> {
        //val dataSource:ReminderDataSource by inject()
        val reminderDTO = ReminderDTO(
                "test title",
                "test desc", "test location", 10.0, 10.0, "id1"
        )
        fakeRepo.saveReminder(reminderDTO)

          launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.addReminderFAB)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }

    //    Test the displayed data on the UI.
    @Test
    fun onStartFragment_itemDetailsVisible() = runBlocking<Unit> {
        //val dataSource:ReminderDataSource by inject()
        val reminderDTO = ReminderDTO(
                "test title",
                "test description", "test location", 10.0, 10.0, "id1"
        )
        fakeRepo.saveReminder(reminderDTO)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(ViewMatchers.withText("test title")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText("test description")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText("test location")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //    Test the navigation of the fragments.
    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment() {
        val scenario =
                launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
                ReminderListFragmentDirections.toSaveReminder()
        )
    }

    //    Test the displayed data on the UI.
    @Test
    fun onStartFragment_withNoData_errorIndicatedByNoDataTextView() = runBlocking<Unit> {

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        fakeRepo.returnError = true

        onView(withId(R.id.addReminderFAB)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }


}