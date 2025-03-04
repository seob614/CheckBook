package com.courr.checkbook.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.serialization.Serializable
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.courr.checkbook.NameChangeDialog
import com.courr.checkbook.R
import com.courr.checkbook.auth.AuthRepository
import com.courr.checkbook.listview.SearchViewModel
import com.courr.checkbook.viewmodel.MyInfoViewModel
import com.courr.checkbook.mvi.MainViewModel
import com.courr.checkbook.mvi.ScreenIntent
import com.courr.checkbook.ui.navigation.auth.SignInRoute
import com.courr.checkbook.ui.navigation.create.CreateInfoRoute
import com.courr.checkbook.ui.theme.CheckBookTheme
import com.courr.checkbook.viewmodel.MyInfoItem

@Serializable
object UserRoute

@Composable
fun UserScreen(mainViewModel: MainViewModel, searchViewModel:SearchViewModel, myInfoViewModel: MyInfoViewModel, navController: NavController) {
    val context = LocalContext.current
    val currentUser by myInfoViewModel.currentUser.observeAsState()

    val myInfoItem by myInfoViewModel.myInfoItem.observeAsState(MyInfoItem())
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.light_gray)),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Icon(
                painter = painterResource(
                    id = if(currentUser != null)
                        R.drawable.logout
                    else
                        R.drawable.login
                ),
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        // 클릭 시 동작할 코드
                        if (currentUser != null) {
                            myInfoViewModel.signOut()
                            Toast.makeText(context, "로그아웃", Toast.LENGTH_SHORT).show()
                        } else {
                            navController.navigate("$SignInRoute")
                            mainViewModel.showDetail()
                        }

                    },
                contentDescription = null,
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.padding(end = 15.dp))
            Icon(
                painter = painterResource(
                    id = R.drawable.setting
                ),
                modifier = Modifier.size(30.dp),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
        Card(
            modifier = Modifier
                .padding(top = 10.dp, start = 18.dp, end = 18.dp)
                .height(85.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.white)
            ),
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize()
                    .fillMaxWidth()
            ){
                Icon(
                    painter = painterResource(
                        id = R.drawable.bottom_user_on
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified
                )
                Text(
                    text =
                        if (currentUser != null)
                            "${myInfoItem.name ?: "익명"}"
                        else
                            "로그인이 필요합니다"
                    ,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .fillMaxWidth(0.8f),
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(
                        id = R.drawable.modify
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            if (currentUser != null)
                                showDialog = true

                        },
                    tint = Color.Unspecified
                )
            }
        }
        Text(
            text = "MY 책",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier
                .padding(start = 15.dp, top = 15.dp),
            textAlign = TextAlign.Start
        )
        Card(
            modifier = Modifier
                .padding(top = 15.dp, start = 13.dp, end = 13.dp)
                .clickable {
                    // 클릭 시 동작할 코드
                    if (currentUser != null) {
                        navController.navigate("$CreateInfoRoute")
                        mainViewModel.showDetail()
                    } else {
                        Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }

                }
                .height(60.dp),
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(colorResource(id = R.color.main_color3))
                    .padding(13.dp)
                    .fillMaxSize()

            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.add2
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    tint = Color.Unspecified
                )
                Text(
                    text = " 지식추가 ",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }
        }
        Card(
            modifier = Modifier
                .padding(top = 15.dp, start = 13.dp, end = 13.dp)
                .clickable {
                    mainViewModel.onIntent(ScreenIntent.NavigateToMyInfo("info"))
                    //navController.navigate("$MyInfoRoute/info")
                    mainViewModel.showDetail() // 디테일 화면 표시 상태 업데이트
                    searchViewModel.yetDatabase_my()
                }
                .height(60.dp),
            ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(13.dp)
                    .fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.info
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    tint = Color.Unspecified
                )
                Text(
                    text = " MY지식 ",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                Text(
                    text ="${myInfoItem.info?.size ?: 0}",
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
        Card(
            modifier = Modifier
                .padding(top = 13.dp, start = 15.dp, end = 15.dp)
                .clickable {

                }
                .height(60.dp),
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(13.dp)
                    .fillMaxSize()
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
                    text = " MY댓글 ",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                Text(
                    text ="${myInfoItem.reple?.size ?: 0}",
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
        Card(
            modifier = Modifier
                .padding(top = 13.dp, start = 15.dp, end = 15.dp)
                .clickable {
                    mainViewModel.onIntent(ScreenIntent.NavigateToMyInfo("check"))
                    //navController.navigate("$MyInfoRoute/check")
                    mainViewModel.showDetail() // 디테일 화면 표시 상태 업데이트
                    searchViewModel.yetDatabase_my()
                }
                .height(60.dp),
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(13.dp)
                    .fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.check_on
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    tint = Color.Unspecified
                )
                Text(
                    text = " MY체크 ",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                Text(
                    text ="${myInfoItem.check?.size ?: 0}",
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


    }
    if (showDialog) {
        NameChangeDialog(
            myInfoViewModel = myInfoViewModel,
            userId = myInfoItem.id ?: "",
            currentName = myInfoItem.name ?: "익명",
            onDismiss = { showDialog = false }
        )
    }
}

@Preview
@Composable
fun UserScreenPreview() {
    val navController = rememberNavController()
    val mainViewModel = MainViewModel()
    val searchViewModel = SearchViewModel()
    val authRepository = AuthRepository()
    val myInfoViewModel = MyInfoViewModel()
    CheckBookTheme{
        UserScreen(mainViewModel, searchViewModel, myInfoViewModel, navController)
    }
}