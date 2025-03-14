package com.courr.checkbook.ui.navigation.mylist

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.courr.checkbook.viewmodel.MyInfoItem
import com.courr.checkbook.viewmodel.MyInfoViewModel
import kotlin.collections.ArrayList

const val MyInfoRoute = "my_info_route"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyInfoScreen(mainViewModel: MainViewModel, myInfoViewModel: MyInfoViewModel,searchViewModel: SearchViewModel, navController: NavController, navBackStackEntry: NavBackStackEntry?) {
    val data = navBackStackEntry?.arguments?.getString("info")
    val myInfoItem by myInfoViewModel.myInfoItem.observeAsState(MyInfoItem())
    val currentUser by myInfoViewModel.currentUser.observeAsState()

    val isLoading by searchViewModel.isLoading.collectAsState()

    BackHandler {
        mainViewModel.hideDetail()
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                title = when (data) {
                    "info" -> "My지식"
                    "reple" -> "MY댓글"
                    "check" -> "MY체크"
                    else -> "데이터 오류"
                },
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
                    if (isLoading) {
                        // 로딩 중일 때 로딩 화면 표시
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    } else {
                        if (data.equals("info")) {
                            if (myInfoItem.info ?.isEmpty() == true) {
                                Text(
                                    text = "추가한 지식이 없습니다.",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxSize().wrapContentHeight(Alignment.CenterVertically)
                                )
                            } else {
                                SearchListView(
                                    searchViewModel, myInfoViewModel, navController,
                                    "", myInfoItem.id.toString(), myInfoItem.info ?: HashMap<String, String>(),data!!
                                )
                            }

                        } else if (data.equals("reple")) {
                            val dataList = HashMap<String,String>()
                            myInfoItem.check?.forEach { (key, value) ->
                                val pushValue = (value as? Map<String, Any>)?.get("info_push")
                                if (pushValue != null) {
                                    // push 값이 있다면 출력
                                    dataList.put(pushValue.toString(),pushValue.toString())
                                }
                            }
                            if (dataList.isEmpty()) {
                                Text(
                                    text = "체크한 정보가 없습니다.",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxSize().wrapContentHeight(Alignment.CenterVertically)
                                )
                            } else {
                                SearchListView(
                                    searchViewModel, myInfoViewModel, navController,
                                    "", myInfoItem.id.toString(), dataList,data!!
                                )
                            }

                        }else if (data.equals("check")) {
                            val dataList = HashMap<String,String>()
                            myInfoItem.check?.forEach { (key, value) ->
                                val pushValue = (value as? Map<String, Any>)?.get("info_push")
                                if (pushValue != null) {
                                    // push 값이 있다면 출력
                                    dataList.put(pushValue.toString(),pushValue.toString())
                                }
                            }
                            if (dataList.isEmpty()) {
                                Text(
                                    text = "체크한 정보가 없습니다.",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxSize().wrapContentHeight(Alignment.CenterVertically)
                                )
                            } else {
                                SearchListView(
                                    searchViewModel, myInfoViewModel, navController,
                                    "", myInfoItem.id.toString(), dataList,data!!
                                )
                            }

                        }
                    }

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
    val navBackStackEntry: NavBackStackEntry? = null
    CheckBookTheme{
        MyInfoScreen(mainViewModel, myInfoViewModel, searchViewModel, navController,navBackStackEntry)
    }
}