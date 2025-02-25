package com.example.checkbook.mvi

// 사용자가 발생시킨 이벤트나 명령을 정의하는 sealed class
sealed class ScreenIntent {
    object LoadData : ScreenIntent()
    object NavigateToNextScreen : ScreenIntent()
    data class NavigateToSearchInfo(val data_key: String) : ScreenIntent()
    data class NavigateToMyInfo(val info: String) : ScreenIntent()
    data class NavigateToDetail(val data: String, val isMyData: Boolean, val push: String?) : ScreenIntent()
}