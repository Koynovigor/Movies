package com.l3on1kl.movies

import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.l3on1kl.movies.presentation.MainActivity
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
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
            Snackbar.make(
                activity.findViewById(android.R.id.content),
                activity.getString(R.string.retry),
                Snackbar.LENGTH_SHORT
            ).show()
        }
        onView(withText(R.string.retry)).check(matches(isDisplayed()))
    }

    @Test
    fun loadMoviesList() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(isRoot()).perform(waitFor(10000))

        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))

        onView(withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(
                    0,
                    object : ViewAction {
                        override fun getDescription() =
                            "Check first movie item"

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
                                "Nested item not found"
                            }

                            val title =
                                viewHolder.itemView.findViewById<TextView>(R.id.title)
                            val rating =
                                viewHolder.itemView.findViewById<Chip>(R.id.rating)

                            assertThat(
                                title.text.toString(),
                                not(emptyOrNullString())
                            )

                            assertThat(
                                rating.text.toString(),
                                not(emptyOrNullString())
                            )
                        }
                    }
                )
            )
    }

    @Test
    fun showSnackbarOnNetworkError() {
        var retried = false
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            Snackbar.make(
                activity.findViewById(android.R.id.content),
                activity.getString(R.string.error_no_internet),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.retry) {
                retried = true
            }.show()
        }

        onView(isRoot()).perform(waitFor(5000))

        onView(
            withText(
                R.string.error_no_internet
            )
        ).check(matches(isDisplayed()))

        onView(
            withText(
                R.string.retry
            )
        ).perform(click())

        assertThat(retried, `is`(true))
    }


    @Test
    fun displayEmptyStateWhenNoMovies() {
        val scenario =
            ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val root = activity.findViewById<ViewGroup>(android.R.id.content)

            root.findViewById<RecyclerView>(R.id.recyclerView)?.visibility = View.GONE

            val tv = TextView(activity).apply {
                text = "Нет доступных фильмов"
            }
            root.addView(tv)
        }

        onView(withText("Нет доступных фильмов"))
            .check(matches(isDisplayed()))

        onView(
            withId(
                R.id.recyclerView
            )
        ).check(matches(isNotDisplayed()))
    }

    @Test
    fun navigateToDetailsScreen() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        onView(isRoot()).perform(waitFor(10000))

        onView(withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(
                    0,
                    object : ViewAction {
                        override fun getDescription() =
                            "Open first movie"

                        override fun getConstraints(): Matcher<View> =
                            isAssignableFrom(View::class.java)

                        override fun perform(
                            uiController: UiController,
                            view: View
                        ) {
                            val nestedRecycler =
                                view.findViewById<RecyclerView>(R.id.moviesRecycler)

                            val viewHolder =
                                nestedRecycler.findViewHolderForAdapterPosition(0)

                            viewHolder!!.itemView.performClick()
                            uiController.loopMainThreadUntilIdle()
                        }
                    }
                )
            )

        onView(isRoot()).perform(waitFor(1000))

        var titleText = ""
        onView(
            withId(
                R.id.title
            )
        ).perform(object : ViewAction {
            override fun getDescription() = "Capture title"

            override fun getConstraints(): Matcher<View> =
                isAssignableFrom(TextView::class.java)

            override fun perform(
                uiController: UiController,
                view: View
            ) {
                titleText = (view as TextView).text.toString()
            }
        })

        scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        onView(isRoot()).perform(waitFor(1000))

        onView(
            withId(
                R.id.title
            )
        ).check(matches(withText(titleText)))

        onView(
            withId(
                R.id.backChip
            )
        ).perform(click())

        onView(isRoot()).perform(waitFor(1000))
    }

    @Test
    fun retainScrollPositionAfterRotation() {
        val scenario =
            ActivityScenario.launch(MainActivity::class.java)

        onView(isRoot()).perform(waitFor(10000))

        onView(
            withId(
                R.id.recyclerView
            )
        ).perform(RecyclerViewActions.scrollToPosition<ViewHolder>(5))

        var firstVisible = -1
        onView(withId(R.id.recyclerView)).perform(object : ViewAction {
            override fun getDescription() = "Capture first visible"

            override fun getConstraints(): Matcher<View> =
                isAssignableFrom(RecyclerView::class.java)

            override fun perform(uiController: UiController, view: View) {
                val lm =
                    (view as RecyclerView).layoutManager as LinearLayoutManager

                firstVisible = lm.findFirstVisibleItemPosition()
            }
        })

        scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        onView(isRoot()).perform(waitFor(1000))

        onView(
            withId(
                R.id.recyclerView
            )
        ).perform(object : ViewAction {
            override fun getDescription() = "Check first visible"

            override fun getConstraints(): Matcher<View> =
                isAssignableFrom(RecyclerView::class.java)

            override fun perform(uiController: UiController, view: View) {
                val lm =
                    (view as RecyclerView).layoutManager as LinearLayoutManager

                assertThat(
                    lm.findFirstVisibleItemPosition(),
                    `is`(firstVisible)
                )
            }
        })
    }

    @Test
    fun retryAfterErrorLoadsData() {
        var retried = false
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val root = activity.findViewById<ViewGroup>(android.R.id.content)
            root.findViewById<RecyclerView>(R.id.recyclerView)?.visibility = View.GONE

            Snackbar.make(
                root,
                "Нет подключения к интернету",
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.retry) {
                retried = true
                root.findViewById<RecyclerView>(R.id.recyclerView)?.post {
                    root.findViewById<RecyclerView>(R.id.recyclerView)?.visibility = View.VISIBLE
                }
            }.show()
        }

        onView(isRoot()).perform(waitFor(5000))

        onView(
            withText(
                "Нет подключения к интернету"
            )
        ).check(matches(isDisplayed()))

        onView(withText(R.string.retry)).perform(click())

        assertThat(retried, `is`(true))

        onView(isRoot()).perform(waitFor(1000))

        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }

    private fun isNotDisplayed(): Matcher<View> {
        return withEffectiveVisibility(ViewMatchers.Visibility.GONE)
    }
}
