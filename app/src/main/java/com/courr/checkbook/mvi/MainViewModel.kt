package com.courr.checkbook.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    // ScreenState를 저장하는 MutableStateFlow
    private val _state = MutableStateFlow(ScreenState(isLoading = false))
    val state: StateFlow<ScreenState> = _state

    private val _navigationEvent = MutableSharedFlow<ScreenIntent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        // 초기 상태 설정
        _state.value = ScreenState(isLoading = false)
    }

    fun onIntent(intent: ScreenIntent) {
        when (intent) {
            is ScreenIntent.LoadData -> {
                _state.value = ScreenState(isLoading = true)
                _state.value = ScreenState(isLoading = false, data = "Loaded Data")
            }
            is ScreenIntent.NavigateToNextScreen -> {
                viewModelScope.launch {
                    _navigationEvent.emit(intent) // NavigateToNextScreen 이벤트 발생
                }
            }
            is ScreenIntent.NavigateToSearchInfo -> { // SearchInfoRoute 처리
                viewModelScope.launch {
                    _navigationEvent.emit(intent)
                }
            }
            is ScreenIntent.NavigateToMyInfo -> { // SearchInfoRoute 처리
                viewModelScope.launch {
                    _navigationEvent.emit(intent)
                }
            }
            is ScreenIntent.NavigateToDetail -> {
                viewModelScope.launch {
                    _navigationEvent.emit(intent) // NavigateToDetail 이벤트 발생
                }
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