package com.example.checkbook.ui.navigation.search

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.checkbook.TopBarWithBackButton
import com.example.checkbook.listview.SearchListView
import com.example.checkbook.listview.SearchViewModel
import com.example.checkbook.mvi.MainViewModel
import com.example.checkbook.ui.navigation.SearchScreen
import com.example.checkbook.ui.theme.CheckBookTheme
import com.example.checkbook.viewmodel.MyInfoViewModel

const val SearchInfoRoute = "search_info_route"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchInfoScreen(mainViewModel: MainViewModel, searchViewModel: SearchViewModel, myInfoViewModel: MyInfoViewModel, navController: NavController, navBackStackEntry: NavBackStackEntry?) {
    val data = navBackStackEntry?.arguments?.getString("data_key")

    BackHandler {
        mainViewModel.hideDetail()
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                title = data ?: "검색어 없음",
                onBackPressed = {
                    mainViewModel.hideDetail()
                    navController.popBackStack()
                },

            )

        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize() // 전체 크기 차지
                    .background(Color.White) // 배경색 설정
            ) {
                SearchListView(searchViewModel, myInfoViewModel, navController, data?:"검색어 없음", "") // SearchListView는 Box 안에 배치됨
            }

        }
    )


}
@Preview
@Composable
fun SimpleComposablePreview() {
    val navController = rememberNavController()
    val mainViewModel = MainViewModel()
    val searchViewModel = SearchViewModel()
    val myInfoViewModel = MyInfoViewModel()
    val navBackStackEntry: NavBackStackEntry? = null
    CheckBookTheme{
        SearchInfoScreen(mainViewModel, searchViewModel, myInfoViewModel, navController, navBackStackEntry)
    }
}