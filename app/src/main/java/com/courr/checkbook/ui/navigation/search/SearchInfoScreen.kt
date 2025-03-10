package com.courr.checkbook.ui.navigation.search

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.courr.checkbook.TopBarWithBackButton
import com.courr.checkbook.listview.SearchListView
import com.courr.checkbook.listview.SearchViewModel
import com.courr.checkbook.mvi.MainViewModel
import com.courr.checkbook.ui.theme.CheckBookTheme
import com.courr.checkbook.viewmodel.MyInfoViewModel
import java.util.ArrayList
import java.util.HashMap

const val SearchInfoRoute = "search_info_route"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchInfoScreen(mainViewModel: MainViewModel, searchViewModel: SearchViewModel, myInfoViewModel: MyInfoViewModel, navController: NavController, navBackStackEntry: NavBackStackEntry?) {
    val data = navBackStackEntry?.arguments?.getString("data_key")
    val isLoading by searchViewModel.isLoading.collectAsState()
    val items by searchViewModel.items.collectAsState()

    var emptyItem by remember { mutableStateOf(false) }

    LaunchedEffect(items) {
        if (items.isEmpty()){
            emptyItem = true
        }else{
            emptyItem = false
        }
    }

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
                if (isLoading) {
                    // 로딩 중일 때 로딩 화면 표시
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                } else{
                    if (emptyItem) {
                        Text(
                            text = "검색된 정보가 없습니다.",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxSize().wrapContentHeight(Alignment.CenterVertically)
                        )
                    } else {
                        SearchListView(searchViewModel, myInfoViewModel, navController, data?:"검색어 없음",
                            "", HashMap(),"검색")
                    }
                }

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