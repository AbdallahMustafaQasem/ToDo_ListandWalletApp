package com.abdallah.taskvault.ui.pomodoro

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class PomodoroPhase(val totalSeconds: Int) {
    WORK(25 * 60),
    SHORT_BREAK(5 * 60),
    LONG_BREAK(15 * 60)
}

data class PomodoroUiState(
    val phase: PomodoroPhase = PomodoroPhase.WORK,
    val remainingSeconds: Int = PomodoroPhase.WORK.totalSeconds,
    val totalSeconds: Int = PomodoroPhase.WORK.totalSeconds,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0
) {
    val formattedTime: String
        get() {
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            return "%02d:%02d".format(m, s)
        }
}

@HiltViewModel
class PomodoroViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timer: CountDownTimer? = null

    fun start() {
        timer?.cancel()
        val remaining = _uiState.value.remainingSeconds.toLong() * 1000L
        timer = object : CountDownTimer(remaining, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.update { it.copy(remainingSeconds = (millisUntilFinished / 1000L).toInt(), isRunning = true) }
            }
            override fun onFinish() {
                onSessionFinished()
            }
        }.start()
        _uiState.update { it.copy(isRunning = true) }
    }

    fun pause() {
        timer?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun reset() {
        timer?.cancel()
        val phase = _uiState.value.phase
        _uiState.update { it.copy(remainingSeconds = phase.totalSeconds, totalSeconds = phase.totalSeconds, isRunning = false) }
    }

    fun skip() {
        timer?.cancel()
        onSessionFinished()
    }

    fun switchPhase(phase: PomodoroPhase) {
        timer?.cancel()
        _uiState.update { it.copy(phase = phase, remainingSeconds = phase.totalSeconds, totalSeconds = phase.totalSeconds, isRunning = false) }
    }

    private fun onSessionFinished() {
        timer?.cancel()
        val current = _uiState.value
        val sessions = if (current.phase == PomodoroPhase.WORK) current.completedSessions + 1 else current.completedSessions
        val nextPhase = when {
            current.phase == PomodoroPhase.WORK && sessions % 4 == 0 -> PomodoroPhase.LONG_BREAK
            current.phase == PomodoroPhase.WORK -> PomodoroPhase.SHORT_BREAK
            else -> PomodoroPhase.WORK
        }
        _uiState.update {
            it.copy(
                phase = nextPhase,
                remainingSeconds = nextPhase.totalSeconds,
                totalSeconds = nextPhase.totalSeconds,
                isRunning = false,
                completedSessions = sessions
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
