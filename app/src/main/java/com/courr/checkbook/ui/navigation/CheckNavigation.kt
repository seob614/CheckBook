package com.courr.checkbook.ui.navigation

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.courr.checkbook.R
import com.courr.checkbook.database.checkDatabase
import com.courr.checkbook.database.checkSetDatabase
import com.courr.checkbook.dialog.RepleBottomSheetDialog
import com.courr.checkbook.listview.SearchItem
import com.courr.checkbook.listview.SearchViewModel
import com.courr.checkbook.mvi.MainViewModel
import com.courr.checkbook.viewmodel.MyInfoViewModel
import com.courr.checkbook.viewmodel.RepleViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch

@Serializable
object CheckRoute

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CheckScreen(mainViewModel: MainViewModel, searchViewModel: SearchViewModel, myInfoViewModel: MyInfoViewModel, repleViewModel: RepleViewModel, navController: NavController) {
    val items by searchViewModel.check_items.collectAsState(emptyList())

    val isDatabaseCheck by searchViewModel.change_check.observeAsState(false)

    var searchItem by remember { mutableStateOf<SearchItem?>(null) }
    var nextItemLoad by remember { mutableStateOf(false) }
    var shuffledItems by remember { mutableStateOf<List<SearchItem>>(emptyList()) } // 섞인 데이터를 보관할 리스트
    var currentIndex by remember { mutableStateOf(0) } // 현재 아이템 인덱스

    val isBottomSheetVisible by repleViewModel.isBottomSheetVisible

    searchViewModel.loadData_check()
    LaunchedEffect(items) {
        searchItem = null
        nextItemLoad = false
        if (items.isNotEmpty()) {
            shuffledItems = items.shuffled() // 리스트 섞기
            currentIndex = 0 // 인덱스 초기화
            if (shuffledItems.isNotEmpty()){
                searchItem = shuffledItems[currentIndex]
            }
        }
    }

    LaunchedEffect(isDatabaseCheck) {
        searchViewModel.yetDatabase_check()
        searchViewModel.loadData_check()
    }

    LaunchedEffect(nextItemLoad) {
        if (shuffledItems.isNotEmpty() && nextItemLoad && currentIndex < shuffledItems.size) {
            searchItem = shuffledItems[currentIndex]
            nextItemLoad = false
        } else if (currentIndex >= shuffledItems.size) {
            shuffledItems = emptyList()
            searchViewModel.yetDatabase_check()
            searchViewModel.loadData_check()
        }
    }
    Scaffold (
        content = {
            val context = LocalContext.current
            val currentUser by myInfoViewModel.currentUser.observeAsState()
            val coroutineScope = rememberCoroutineScope()
            var isLoading by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize() // 전체 크기 차지
                    .background(Color.White) // 배경색 설정
            ) {
                if (nextItemLoad) {
                    // 로딩 중일 때 로딩 화면 표시
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }else{
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
                        if (searchItem == null) {
                            Text(
                                text = "더 이상 정보가 없습니다.",
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .wrapContentHeight(Alignment.CenterVertically)  // 가운데 정렬
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(top = 10.dp, start = 5.dp, end = 5.dp)
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
                                            .height(22.dp)
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
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = " " + searchItem?.info + "\n",
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
                                    modifier = Modifier.clickable {
                                        repleViewModel.showBottomSheetDialog()
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
                                            .clickable(enabled = !isLoading) {
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
                                                            false,
                                                            onError = { errorMessage ->
                                                                isLoading = false
                                                                FirebaseCrashlytics.getInstance().log("Failed to update check: ${errorMessage}")
                                                                FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                                                                Toast
                                                                    .makeText(
                                                                        context,
                                                                        "데이터베이스 오류2",
                                                                        Toast.LENGTH_SHORT
                                                                    )
                                                                    .show()
                                                            },
                                                            onSuccess = { now_num, opposite_num, now_check, opposite_check ->
                                                                currentIndex++
                                                                nextItemLoad = true
                                                                isLoading = false
                                                            }
                                                        )
                                                    }
                                                } else {
                                                    Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
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
                                                text = if (searchItem?.t_num == null || searchItem?.f_num == null || searchItem!!.t_num == 0 || searchItem!!.f_num == 0) {
                                                    if (searchItem?.f_num == 0){
                                                        "0 (0%)"
                                                    }else{
                                                        "${searchItem?.f_num} (100%)"
                                                    }
                                                } else {
                                                    val fNum = searchItem!!.f_num ?: 0
                                                    val tNum = searchItem!!.t_num ?: 0
                                                    "${fNum} (${"%.0f%%".format((fNum.toFloat() / (tNum + fNum).toFloat()) * 100)})"
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
                                            .clickable(enabled = !isLoading) {
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
                                                            false,
                                                            onError = { errorMessage ->
                                                                isLoading = false
                                                                FirebaseCrashlytics.getInstance().log("Failed to update check: ${errorMessage}")
                                                                FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                                                                Toast
                                                                    .makeText(
                                                                        context,
                                                                        "데이터베이스 오류",
                                                                        Toast.LENGTH_SHORT
                                                                    )
                                                                    .show()
                                                            },
                                                            onSuccess = { now_num, opposite_num, now_check, opposite_check ->
                                                                currentIndex++
                                                                nextItemLoad = true
                                                                isLoading = false
                                                            }
                                                        )
                                                    }
                                                } else {
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "로그인이 필요합니다.",
                                                            Toast.LENGTH_SHORT
                                                        )
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
                                                text = if (searchItem?.t_num == null || searchItem?.f_num == null || searchItem!!.t_num == 0 || searchItem!!.f_num == 0) {
                                                    if (searchItem?.t_num == 0){
                                                        "0 (0%)"
                                                    }else{
                                                        "${searchItem?.t_num} (100%)"
                                                    }

                                                } else {
                                                    val fNum = searchItem!!.f_num ?: 0
                                                    val tNum = searchItem!!.t_num ?: 0
                                                    "${tNum} (${"%.0f%%".format((tNum.toFloat() / (tNum + fNum).toFloat()) * 100)})"
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

                    }
                }

            }
            RepleBottomSheetDialog(
                isVisible = isBottomSheetVisible,
                onClickCancel = {
                    repleViewModel.hideBottomSheetDialog() // 바텀 시트 닫기
                }
            )

            /*
            if (customBottomSheetDialogState.list.isEmpty()) {

            }

             */
        }
    )
}
