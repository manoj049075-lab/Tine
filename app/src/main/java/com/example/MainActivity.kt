package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TimeMorphViewModel
import com.example.ui.capsule.CapsuleScreen
import com.example.ui.components.AppInfoDialog
import com.example.ui.components.NavigationTab
import com.example.ui.components.TimeMorphBottomNav
import com.example.ui.components.TimeMorphTopBar
import com.example.ui.milestones.MilestonesScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.Slate950
import com.example.ui.theme.TimeMorphTheme
import com.example.ui.today.TodayScreen

class MainActivity : ComponentActivity() {
    private val viewModel: TimeMorphViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeMorphTheme {
                TimeMorphApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TimeMorphApp(viewModel: TimeMorphViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val latestEntry by viewModel.latestEntry.collectAsStateWithLifecycle()
    val capsules by viewModel.capsules.collectAsStateWithLifecycle()
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    if (!uiState.isOnboardingComplete) {
        OnboardingScreen(
            onFinishOnboarding = { viewModel.completeOnboarding() }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950),
            topBar = {
                Box(modifier = Modifier.statusBarsPadding()) {
                    TimeMorphTopBar(
                        currentStreak = stats.currentStreak,
                        syncState = "LOCAL",
                        onInfoClick = { viewModel.showInfoDialog(true) }
                    )
                }
            },
            bottomBar = {
                TimeMorphBottomNav(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            },
            containerColor = Slate950
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Slate950)
            ) {
                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_navigation"
                ) { tab ->
                    when (tab) {
                        NavigationTab.TODAY -> {
                            TodayScreen(
                                latestEntry = latestEntry,
                                onSaveCapture = { bitmap, note, score ->
                                    viewModel.saveCapture(bitmap, note, score)
                                }
                            )
                        }
                        NavigationTab.CAPSULE -> {
                            CapsuleScreen(
                                capsules = capsules,
                                entries = entries,
                                onCreateCapsule = { title, duration, isStrict, note ->
                                    viewModel.createCapsule(title, duration, isStrict, note)
                                }
                            )
                        }
                        NavigationTab.MILESTONES -> {
                            MilestonesScreen(
                                milestones = milestones,
                                entries = entries,
                                stats = stats
                            )
                        }
                        NavigationTab.PROFILE -> {
                            ProfileScreen(
                                stats = stats,
                                isHindiLanguage = uiState.isHindiLanguage,
                                onLanguageToggle = { viewModel.toggleLanguage(it) }
                            )
                        }
                    }
                }

                if (uiState.infoDialogVisible) {
                    AppInfoDialog(onDismiss = { viewModel.showInfoDialog(false) })
                }
            }
        }
    }
}
