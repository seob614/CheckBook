package com.example.checkbook.ui.navigation.search

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.checkbook.NameChangeDialog
import com.example.checkbook.R
import com.example.checkbook.TopBarWithBackButton
import com.example.checkbook.database.checkDatabase
import com.example.checkbook.database.checkSetDatabase
import com.example.checkbook.dialog.MoreDialog
import com.example.checkbook.listview.SearchListView
import com.example.checkbook.listview.SearchViewModel
import com.example.checkbook.mvi.MainViewModel
import com.example.checkbook.ui.theme.CheckBookTheme
import com.example.checkbook.viewmodel.MyInfoViewModel
import kotlinx.coroutines.launch

const val DetailInfoRoute = "detail_info_route"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DetailInfoScreen(searchViewModel: SearchViewModel, myInfoViewModel: MyInfoViewModel, navController: NavController, navBackStackEntry: NavBackStackEntry?) {
    val push = navBackStackEntry?.arguments?.getString("push")
    val data = navBackStackEntry?.arguments?.getString("data_key")
    val isMyData = navBackStackEntry?.arguments?.getBoolean("isMyData")
    val info_type = navBackStackEntry?.arguments?.getString("info_type")

    var showDialog by remember { mutableStateOf(false) }

    val items by if (isMyData?:false) {
        searchViewModel.itemsMy.collectAsState(emptyList())
    } else {
        searchViewModel.items.observeAsState(emptyList())
    }

    val searchItem = items.find { it.push == push }

    BackHandler {
        navController.popBackStack()
    }
    val context = LocalContext.current
    Scaffold(
        topBar = {
            if (info_type.equals("check")){
                TopBarWithBackButton(
                    title = data ?: "검색어 없음",
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            } else{
                TopBarWithBackButton(
                    data ?: "검색어 없음",
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onMoreClick = {
                        showDialog = true
                    },
                    isMyData = isMyData ?: false,
                )
            }

        },
        content = {
            val context = LocalContext.current
            val currentUser by myInfoViewModel.currentUser.observeAsState()
            val coroutineScope = rememberCoroutineScope()
            var isLoading by remember { mutableStateOf(false) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 63.dp)
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .height(57.dp)
                        .padding(horizontal = 5.dp)
                ) {
                    Column (
                        modifier = Modifier
                            .width(45.dp)
                            .fillMaxHeight()

                    ){
                        Icon(
                            painter = painterResource(
                                id = R.drawable.bottom_user_on
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.CenterHorizontally),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = searchItem?.name ?: "익명",
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .height(18.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = searchItem?.title ?:"",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = TextAlign.Start
                    )
                }
                Text(
                    text = searchItem?.date ?: "",
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 11.dp),
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = " "+searchItem?.info+"\n",
                    color = Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 11.dp)
                        .verticalScroll(rememberScrollState()) // 스크롤 가능하도록 설정
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ){
                    Card(
                        modifier = Modifier
                            .clickable {
                                // 클릭 이벤트 처리
                                println("Card clicked!")
                            }
                            .padding(5.dp),

                    ){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(13.dp)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.add
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(23.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = " 지식댓글 ",
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                            )
                            Text(
                                text = "10",
                                color = Color.Gray,
                                fontSize = 16.sp,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.next
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(23.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .clickable {
                                    if (isLoading) return@clickable
                                    if (currentUser != null) {
                                        isLoading = true
                                        coroutineScope.launch {
                                            val user = currentUser?.email?.substringBefore("@")
                                            val (set_isFound, set_pushKey) = checkDatabase(
                                                user,
                                                searchItem?.push
                                            )
                                            checkSetDatabase(
                                                user,
                                                false,
                                                set_isFound,
                                                set_pushKey,
                                                searchItem?.push,
                                                searchViewModel,
                                                isMyData ?: false,
                                                onError = { errorMessage ->
                                                    isLoading = false
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "데이터베이스 오류2",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                },
                                                onSuccess = { now_num, opposite_num, now_check, opposite_check ->
                                                    if (isMyData ?: false) {
                                                        searchViewModel.updateMyCheckNum(
                                                            searchItem?.push.toString(),
                                                            opposite_num,
                                                            now_num,
                                                            opposite_check,
                                                            now_check
                                                        )
                                                    } else {
                                                        searchViewModel.updateCheckNum(
                                                            searchItem?.push.toString(),
                                                            opposite_num,
                                                            now_num,
                                                            opposite_check,
                                                            now_check
                                                        )
                                                    }

                                                    isLoading = false
                                                }
                                            )
                                        }
                                    } else {
                                        Toast
                                            .makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(
                                    id = if(searchItem?.f_check ?: false)
                                        R.color.f_color
                                    else
                                        R.color.f_color2)
                            ),
                            shape = RoundedCornerShape(
                                topStart = 0.dp,  // 왼쪽 상단
                                topEnd = 0.dp,    // 오른쪽 상단
                                bottomEnd = 0.dp,  // 오른쪽 하단은 둥글지 않게
                                bottomStart = 13.dp // 왼쪽 하단
                            )

                        ){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                            ){
                                Text(
                                    text = "거짓",
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(1.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                )
                                Text(
                                    text = if (searchItem?.t_num == null || searchItem.f_num == null || searchItem.t_num == 0 || searchItem.f_num == 0) {
                                        if (searchItem?.f_num == 0){
                                            "0 (0%)"
                                        }else{
                                            "${searchItem?.f_num} (100%)"
                                        }

                                    } else {
                                        "${searchItem.f_num} (${"%.0f%%".format((searchItem.f_num.toFloat() / (searchItem.t_num + searchItem.f_num).toFloat()) * 100)})"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                        }
                        Card(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .clickable {
                                    if (isLoading) return@clickable
                                    if (currentUser != null) {
                                        isLoading = true
                                        coroutineScope.launch {
                                            val user = currentUser?.email?.substringBefore("@")
                                            val (set_isFound, set_pushKey) = checkDatabase(
                                                user,
                                                searchItem?.push
                                            )
                                            checkSetDatabase(
                                                user,
                                                true,
                                                set_isFound,
                                                set_pushKey,
                                                searchItem?.push,
                                                searchViewModel,
                                                isMyData ?: false,
                                                onError = { errorMessage ->
                                                    isLoading = false
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "데이터베이스 오류",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                },
                                                onSuccess = { now_num, opposite_num, now_check, opposite_check ->
                                                    if (isMyData ?: false) {
                                                        searchViewModel.updateMyCheckNum(
                                                            searchItem?.push.toString(),
                                                            now_num,
                                                            opposite_num,
                                                            now_check,
                                                            opposite_check
                                                        )
                                                    } else {
                                                        searchViewModel.updateCheckNum(
                                                            searchItem?.push.toString(),
                                                            now_num,
                                                            opposite_num,
                                                            now_check,
                                                            opposite_check
                                                        )
                                                    }

                                                    isLoading = false
                                                }
                                            )
                                        }
                                    } else {
                                        Toast
                                            .makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(
                                    id = if(searchItem?.t_check ?: false)
                                        R.color.t_color
                                    else
                                        R.color.t_color2)
                            ),
                            shape = RoundedCornerShape(
                                topStart = 0.dp,  // 왼쪽 상단
                                topEnd = 0.dp,    // 오른쪽 상단
                                bottomEnd = 13.dp,  // 오른쪽 하단은 둥글지 않게
                                bottomStart = 0.dp // 왼쪽 하단
                            )
                        ){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                            ){
                                Text(
                                    text = "진실",
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(1.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (searchItem?.t_num == null || searchItem?.f_num == null || searchItem.t_num == 0 || searchItem.f_num == 0) {
                                        if (searchItem?.t_num == 0){
                                            "0 (0%)"
                                        }else{
                                            "${searchItem?.t_num} (100%)"
                                        }

                                    } else {
                                        "${searchItem.t_num} (${"%.0f%%".format((searchItem.t_num.toFloat() / (searchItem.t_num + searchItem.f_num).toFloat()) * 100)})"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                        }

                    }
                }
            }
            if (showDialog) {
                MoreDialog(
                    searchViewModel = searchViewModel,
                    userId = searchItem?.id!!,
                    push = push!!,
                    info_type = info_type!!,
                    onSuccesss = {
                        navController.popBackStack()
                        Toast.makeText(context,"삭제되었습니다.",Toast.LENGTH_SHORT).show()
                        showDialog = false
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context,errorMessage,Toast.LENGTH_SHORT).show()
                        showDialog = false
                    },
                    onDismiss = { showDialog = false }
                )
            }

        }
    )
}
@Preview
@Composable
fun DetailInfoComposablePreview() {
    val navController = rememberNavController()
    val searchViewModel = SearchViewModel()
    val myInfoViewModel = MyInfoViewModel()
    val navBackStackEntry: NavBackStackEntry? = null
    CheckBookTheme{
        DetailInfoScreen(searchViewModel,myInfoViewModel, navController,navBackStackEntry)
    }
}