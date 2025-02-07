package com.example.checkbook.mvi

// 화면 상태를 정의하는 데이터 클래스
data class ScreenState(
    val isLoading: Boolean,
    val data: String? = null,
    val error: String? = null // 에러 메시지
)