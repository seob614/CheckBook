package com.courr.checkbook.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RepleViewModel @Inject constructor() : ViewModel() {
    val isBottomSheetVisible = mutableStateOf(false)

    // 바텀 시트 열기
    fun showBottomSheetDialog() {
        isBottomSheetVisible.value = true
    }

    // 바텀 시트 닫기
    fun hideBottomSheetDialog() {
        isBottomSheetVisible.value = false
    }
}