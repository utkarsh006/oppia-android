package org.oppia.android.app.profile

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProfileChooserFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileChooserFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileChooserFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  private val activityTestRule: ActivityTestRule<ProfileChooserActivity> = ActivityTestRule(
    ProfileChooserActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var profileManagementController: ProfileManagementController

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testProfileChooserActivity_hasCorrectLabel() {
    activityTestRule.launchActivity(/* startIntent= */ null)
    val title = activityTestRule.activity.title
    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.profile_chooser_activity_label))
  }

  @Test
  fun testProfileChooserFragment_initializeProfiles_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin"
      )
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_is_admin_text,
        stringToMatch = context.getString(R.string.profile_chooser_admin)
      )
      scrollToPosition(position = 1)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 1,
        targetView = R.id.profile_name_text,
        stringToMatch = "Ben"
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 1,
          targetViewId = R.id.profile_is_admin_text
        )
      ).check(matches(not(isDisplayed())))
      scrollToPosition(position = 3)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 4,
        targetView = R.id.add_profile_text,
        stringToMatch = context.getString(R.string.profile_chooser_add)
      )
    }
  }

  @Test
  fun testProfileChooserFragment_afterVisitingHomeActivity_showsJustNowText() {
    // Note that the auto-log in here is simulating HomeActivity having been visited before (i.e.
    // that a profile was previously logged in).
    profileTestHelper.initializeProfiles(autoLogIn = true)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 0,
          targetViewId = R.id.profile_last_visited
        )
      ).check(matches(isDisplayed()))
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_last_visited,
        stringToMatch = "${context.getString(R.string.profile_last_used)} just now"
      )
    }
  }

  @Test
  fun testProfileChooserFragment_afterVisitingHomeActivity_changeConfiguration_showsJustNowText() {
    // Note that the auto-log in here is simulating HomeActivity having been visited before (i.e.
    // that a profile was previously logged in).
    profileTestHelper.initializeProfiles(autoLogIn = true)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 0,
          targetViewId = R.id.profile_last_visited
        )
      ).check(matches(isDisplayed()))
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_last_visited,
        stringToMatch = "${context.getString(R.string.profile_last_used)} just now"
      )
    }
  }

  @Test
  fun testProfileChooserFragment_addManyProfiles_checkProfilesSortedAndNoAddProfile() {
    profileTestHelper.initializeProfiles(autoLogIn = false)
    profileTestHelper.addMoreProfiles(8)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 0,
        targetView = R.id.profile_name_text,
        stringToMatch = "Admin"
      )
      scrollToPosition(position = 1)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 1,
        targetView = R.id.profile_name_text,
        stringToMatch = "A"
      )
      scrollToPosition(position = 2)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 2,
        targetView = R.id.profile_name_text,
        stringToMatch = "B"
      )
      scrollToPosition(position = 3)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 3,
        targetView = R.id.profile_name_text,
        stringToMatch = "Ben"
      )
      scrollToPosition(position = 4)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 4,
        targetView = R.id.profile_name_text,
        stringToMatch = "C"
      )
      scrollToPosition(position = 5)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 5,
        targetView = R.id.profile_name_text,
        stringToMatch = "D"
      )
      scrollToPosition(position = 6)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 6,
        targetView = R.id.profile_name_text,
        stringToMatch = "E"
      )
      scrollToPosition(position = 7)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 7,
        targetView = R.id.profile_name_text,
        stringToMatch = "F"
      )
      scrollToPosition(position = 8)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 8,
        targetView = R.id.profile_name_text,
        stringToMatch = "G"
      )
      scrollToPosition(position = 9)
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 9,
        targetView = R.id.profile_name_text,
        stringToMatch = "H"
      )
    }
  }

  @Test
  fun testProfileChooserFragment_clickProfile_checkOpensPinPasswordActivity() {
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch(ProfileChooserActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPosition(
          recyclerViewId = R.id.profile_recycler_view,
          position = 0
        )
      ).perform(click())
      intended(hasComponent(PinPasswordActivity::class.java.name))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminProfileWithNoPin_checkOpensAdminPinActivity() {
    profileManagementController.addProfile(
      name = "Admin",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    )
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 1,
          targetViewId = R.id.add_profile_item
        )
      ).perform(click())
      intended(hasComponent(AdminPinActivity::class.java.name))
      intended(hasExtra(ADMIN_PIN_ENUM_EXTRA_KEY, AdminAuthEnum.PROFILE_ADD_PROFILE.value))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminControlsWithNoPin_checkOpensAdminControlsActivity() {
    profileManagementController.addProfile(
      name = "Admin",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    )
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_linear_layout)).perform(click())
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
      intended(hasExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, /* internalProfileId= */ 0))
    }
  }

  @Test
  fun testProfileChooserFragment_checkLayoutManager_isLinearLayoutManager() {
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      it.onActivity { activity ->
        val profileRecyclerView = activity.findViewById<RecyclerView>(
          R.id.profile_recycler_view
        )
        val layoutManager = profileRecyclerView
          .layoutManager as LinearLayoutManager
        assertThat(layoutManager.orientation).isEqualTo(LinearLayoutManager.VERTICAL)
      }
    }
  }

  @Test
  fun testProfileChooserFragment_onlyAdminProfile_checkText_setUpMultipleProfilesIsVisible() {
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 1,
        targetView = R.id.add_profile_text,
        stringToMatch = context.getString(R.string.set_up_multiple_profiles)
      )
    }
  }

  @Test
  fun testProfileChooserFragment_onlyAdminProfile_checkDescriptionText_isDisplayed() {
    profileTestHelper.addOnlyAdminProfile()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 1,
          targetViewId = R.id.add_profile_description_text
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testProfileChooserFragment_multipleProfiles_checkText_addProfileIsVisible() {
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      verifyTextOnProfileListItemAtPosition(
        itemPosition = 4,
        targetView = R.id.add_profile_text,
        stringToMatch = context.getString(R.string.profile_chooser_add)
      )
    }
  }

  @Test
  fun testProfileChooserFragment_multipleProfiles_checkDescriptionText_isDisplayed() {
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_recycler_view,
          position = 4,
          targetViewId = R.id.add_profile_description_text
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAdminControls_opensAdminAuthActivity() {
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.administrator_controls_linear_layout)).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
      intended(hasExtra(ADMIN_AUTH_ENUM_EXTRA_KEY, AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value))
    }
  }

  @Test
  fun testProfileChooserFragment_clickAddProfile_opensAdminAuthActivity() {
    profileTestHelper.initializeProfiles(autoLogIn = false)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPosition(
          recyclerViewId = R.id.profile_recycler_view,
          position = 4
        )
      ).perform(click())
      intended(hasComponent(AdminAuthActivity::class.java.name))
      intended(hasExtra(ADMIN_AUTH_ENUM_EXTRA_KEY, AdminAuthEnum.PROFILE_ADD_PROFILE.value))
    }
  }

  private fun createProfileChooserActivityIntent(): Intent {
    return ProfileChooserActivity
      .createProfileChooserActivity(ApplicationProvider.getApplicationContext())
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.profile_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
  }

  private fun verifyTextOnProfileListItemAtPosition(
    itemPosition: Int,
    targetView: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.profile_recycler_view,
        position = itemPosition,
        targetViewId = targetView
      )
    ).check(matches(withText(stringToMatch)))
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(profileChooserFragmentTest: ProfileChooserFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileChooserFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileChooserFragmentTest: ProfileChooserFragmentTest) {
      component.inject(profileChooserFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
