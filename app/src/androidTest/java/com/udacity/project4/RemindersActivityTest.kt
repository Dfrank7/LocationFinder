package com.udacity.project4

import android.app.Application
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
        AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var decorView: View

    //rule is used to get decorview required to test whether toast is shown.
    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(RemindersActivity::class.java)

    @Before
    fun setUp() {
        activityScenarioRule.scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                        appContext,
                        get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                        appContext,
                        get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            //androidLogger(Level.DEBUG)
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()
        viewModel = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    //seems like the idling resource is not required for this test. But added it for safety.
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Test
    fun navigateTo_selectLocation_clickSave_showToast() = runBlocking {
        repository.saveReminder(
                ReminderDTO(
                        "test title",
                        "test description", "test location", 10.0, 10.0, "id1"
                )
        )

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        //seems like the idling resource is not required for this test. But added it for safety.
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //inside reminderlistfragment.
        Espresso.onView(ViewMatchers.withText("test title")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        //inside savereminderfragment.
        Espresso.onView(withId(R.id.reminderTitle)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("new title"))
                .perform(ViewActions.closeSoftKeyboard())
        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("new description"))
                .perform(ViewActions.closeSoftKeyboard())

        viewModel.latitude.postValue(20.0)
        viewModel.longitude.postValue(20.0)
        viewModel.reminderSelectedLocationStr.postValue("location")

        //navigate to reminderlist fragment after saving data.
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText("new title")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("new description")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        //checks if savereminderfragment views and snackbars shown
        Espresso.onView(withId(R.id.reminderTitle)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        //checks if snackbar shown.
        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text)).check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_enter_title)))

        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("new title"))
                .perform(ViewActions.closeSoftKeyboard())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("new description"))
                .perform(ViewActions.closeSoftKeyboard())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.btnConfirm)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(withId(R.id.btnConfirm)).perform(ViewActions.click())

        //testing for toast.
        Espresso.onView(ViewMatchers.withText(R.string.select_poi)).inRoot(RootMatchers.withDecorView(CoreMatchers.not(decorView))).check(
                ViewAssertions.matches(
                        ViewMatchers.isDisplayed()
                )
        )

        activityScenario.close()
    }

}
