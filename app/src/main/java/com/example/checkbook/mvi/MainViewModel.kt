package com.example.checkbook.mvi

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    // ScreenState를 저장하는 MutableStateFlow
    private val _state = MutableStateFlow(ScreenState(isLoading = false))
    val state: StateFlow<ScreenState> = _state

    init {
        // 초기 상태 설정
        _state.value = ScreenState(isLoading = false)
    }

    // 화면 전환 상태를 처리하는 함수
    fun onIntent(intent: ScreenIntent) {
        when (intent) {
            is ScreenIntent.LoadData -> {
                // 데이터 로딩 처리
                _state.value = ScreenState(isLoading = true)
                // 데이터 로딩 후 상태 업데이트
                _state.value = ScreenState(isLoading = false, data = "Loaded Data")
            }
            is ScreenIntent.NavigateToNextScreen -> {
                // 화면 전환 처리
                // 예: navController.navigate("next_screen")
            }
        }
    }

    private val _showDetailNavHost = MutableStateFlow(false)
    val showDetailNavHost: StateFlow<Boolean> = _showDetailNavHost

    fun showDetail() {
        _showDetailNavHost.value = true
    }

    fun hideDetail() {
        _showDetailNavHost.value = false
    }

    private val _searchDataKey = MutableStateFlow<String?>(null)
    val searchDataKey: StateFlow<String?> = _searchDataKey

    // 화면 전환 상태를 처리하는 함수
    fun updateDataKey(key: String) {
        _searchDataKey.value = key
    }

    fun clearDataKey() {
        _searchDataKey.value = null
    }

}