package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DailyEntry
import com.example.data.JourneyStats
import com.example.data.Milestone
import com.example.data.TimeCapsule
import com.example.data.TimeMorphDatabase
import com.example.data.TimeMorphRepository
import com.example.ui.components.NavigationTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TimeMorphUiState(
    val selectedTab: NavigationTab = NavigationTab.TODAY,
    val isOnboardingComplete: Boolean = true,
    val isHindiLanguage: Boolean = false,
    val infoDialogVisible: Boolean = false
)

class TimeMorphViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TimeMorphDatabase.getDatabase(application)
    private val repository = TimeMorphRepository(application, database)

    val entries: StateFlow<List<DailyEntry>> = repository.allEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val latestEntry: StateFlow<DailyEntry?> = repository.latestEntry.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val capsules: StateFlow<List<TimeCapsule>> = repository.allCapsules.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val milestones: StateFlow<List<Milestone>> = repository.allMilestones.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stats: StateFlow<JourneyStats> = repository.journeyStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JourneyStats()
    )

    private val _uiState = MutableStateFlow(TimeMorphUiState())
    val uiState: StateFlow<TimeMorphUiState> = _uiState.asStateFlow()

    fun selectTab(tab: NavigationTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun completeOnboarding() {
        _uiState.value = _uiState.value.copy(isOnboardingComplete = true)
    }

    fun toggleLanguage(isHindi: Boolean) {
        _uiState.value = _uiState.value.copy(isHindiLanguage = isHindi)
    }

    fun showInfoDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(infoDialogVisible = show)
    }

    fun saveCapture(bitmap: Bitmap, note: String, alignmentScore: Int) {
        viewModelScope.launch {
            repository.saveDailyEntry(bitmap, note, alignmentScore)
        }
    }

    fun createCapsule(title: String, durationMonths: Int, isStrict: Boolean, note: String) {
        viewModelScope.launch {
            repository.createCapsule(title, durationMonths, isStrict, note)
        }
    }

    fun updateNote(id: String, note: String) {
        viewModelScope.launch {
            repository.updateNote(id, note)
        }
    }
}
