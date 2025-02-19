package com.example.checkbook.ui.navigation.mylist

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.checkbook.TopBarWithBackButton
import com.example.checkbook.listview.SearchListView
import com.example.checkbook.listview.SearchViewModel
import com.example.checkbook.mvi.MainViewModel
import com.example.checkbook.ui.theme.CheckBookTheme
import com.example.checkbook.viewmodel.MyInfoItem
import com.example.checkbook.viewmodel.MyInfoViewModel

const val MyInfoRoute = "my_info_route"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyInfoScreen(mainViewModel: MainViewModel, myInfoViewModel: MyInfoViewModel,searchViewModel: SearchViewModel, navController: NavController) {
    val myInfoItem by myInfoViewModel.myInfoItem.observeAsState(MyInfoItem())
    val currentUser by myInfoViewModel.currentUser.observeAsState()
    BackHandler {
        mainViewModel.hideDetail()
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                title = "My지식",
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
                if (currentUser != null)
                    SearchListView(searchViewModel, myInfoViewModel, navController, "", myInfoItem.id.toString())
                else
                    "로그인이 필요합니다"
            }

        }
    )


}
@Preview
@Composable
fun SimpleComposablePreview() {
    val navController = rememberNavController()
    val mainViewModel = MainViewModel()
    val myInfoViewModel = MyInfoViewModel()
    val searchViewModel = SearchViewModel()
    CheckBookTheme{
        MyInfoScreen(mainViewModel, myInfoViewModel, searchViewModel, navController)
    }
}