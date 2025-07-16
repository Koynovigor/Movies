package com.l3on1kl.movies

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.l3on1kl.movies.presentation.MainActivity
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUiTest {

    @Test
    fun displayMainScreen() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.progressBar)).check(matches(isDisplayed()))
    }

    @Test
    fun openMovieDetailsAndBack() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(isRoot()).perform(waitFor(10000))

        onView(withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(
                    0,
                    object : ViewAction {
                        override fun getDescription() =
                            "Click on first movie in nested RecyclerView"

                        override fun getConstraints(): Matcher<View> =
                            isAssignableFrom(View::class.java)

                        override fun perform(
                            uiController: UiController,
                            view: View
                        ) {
                            val nestedRecycler =
                                view.findViewById<RecyclerView>(R.id.moviesRecycler)

                            checkNotNull(nestedRecycler) {
                                "Nested RecyclerView not found"
                            }

                            val viewHolder =
                                nestedRecycler.findViewHolderForAdapterPosition(0)

                            checkNotNull(viewHolder) {
                                "Nested item not found at position 0"
                            }

                            viewHolder.itemView.performClick()
                            uiController.loopMainThreadUntilIdle()
                        }
                    }
                ))

        onView(isRoot()).perform(waitFor(1000))

        onView(withId(R.id.backChip)).check(matches(isDisplayed()))

        onView(withId(R.id.backChip)).perform(click())

        onView(isRoot()).perform(waitFor(1000))

        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }

    private fun waitFor(millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription(): String = "Wait for $millis ms."
            override fun perform(uiController: UiController, view: View?) {
                uiController.loopMainThreadForAtLeast(millis)
            }
        }
    }

    @Test
    fun displaySnackbarText() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            com.google.android.material.snackbar.Snackbar.make(
                activity.findViewById(android.R.id.content),
                activity.getString(R.string.retry),
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
        }
        onView(withText(R.string.retry)).check(matches(isDisplayed()))
    }
}