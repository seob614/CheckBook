package com.courr.checkbook.listview

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.courr.checkbook.R
import com.courr.checkbook.database.checkDatabase
import com.courr.checkbook.database.checkSetDatabase
import com.courr.checkbook.ui.navigation.search.DetailInfoRoute
import com.courr.checkbook.viewmodel.MyInfoViewModel
import kotlinx.coroutines.launch

@Composable
fun SearchListView(searchViewModel: SearchViewModel, myInfoViewModel: MyInfoViewModel, navController: NavController, data: String, id:String, list:ArrayList<String>,info_type:String) {

    val isMyData = id.isNotEmpty()

    LaunchedEffect(id) {  // id가 변경될 때마다 적절한 데이터 로드
        if (isMyData) {
            if (list.isNotEmpty()){
                searchViewModel.loadData_my(id,list)
            } else {
                searchViewModel.loadData_my(id,list)  // 리스트 초기화 함수
            }
        } else {
            searchViewModel.loadData(data)
        }
    }

    val items by if (isMyData) {
        searchViewModel.itemsMy.collectAsState(emptyList())
    } else {
        searchViewModel.items.collectAsState(emptyList())
    }

    LazyColumn(
        modifier = Modifier
            .padding(top = 56.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            SearchListItem(
                navController = navController,  // navController 전달
                searchItem = item,  // SearchItem 객체 전달
                searchViewModel = searchViewModel,
                myInfoViewModel = myInfoViewModel,
                data = data,
                isMyData = isMyData,
                info_type = info_type,
                id = id
            )
            /*
            SearchListItem(navController = navController, push = item.push, id = item.id, name = item.name, profile = item.profile, date = item.date,title = item.title,
                info = item.info, t_num = item.t_num,f_num = item.f_num)
             */
        }
    }
}

@Composable
fun SearchListItem(
    navController: NavController,
    searchItem: SearchItem,
    searchViewModel: SearchViewModel,
    myInfoViewModel: MyInfoViewModel,
    data: String,
    isMyData: Boolean,
    info_type:String,
    id: String
    /*
    push: String? = null,
    id: String? = null,
    name: String? = "익명",
    profile: String? = null,
    date: String? = null,
    title: String? = "",
    info: String? = "",
    t_num: Int? = 0,
    f_num: Int? = 0,
     */
) {
    val context = LocalContext.current
    val currentUser by myInfoViewModel.currentUser.observeAsState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var deleteItem by remember { mutableStateOf(false) }
    var declareItem by remember { mutableStateOf(false) }

    deleteItem = if (searchItem.delete?:false) true else false
    declareItem = if ((searchItem.declare?:ArrayList<String>()).size>10) true else false
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable {
                // 클릭 이벤트 처리
                navController.navigate("$DetailInfoRoute/${data}/${isMyData}/${searchItem.push}/${info_type}")
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(Color.White)
        ) {
            //Divider(color = Color.Gray, thickness = 2.dp)
            Row(
                modifier = Modifier
                    .padding(6.dp)
                    .fillMaxWidth()
                    .height(57.dp)
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
                        text = searchItem.name ?: "익명",
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
                    text = searchItem.title ?:"",
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
                text = " "+searchItem.info,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp) // 최대 높이 설정
                    .padding(vertical = 3.dp)
                    .padding(horizontal = 2.dp),
                overflow = TextOverflow.Ellipsis,
                /*
                    .heightIn(max = 220.dp) // 최대 높이 설정
                    .verticalScroll(rememberScrollState()) // 스크롤 가능하도록 설정

                 */

            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.add
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
                Text(
                    text = "10",
                    color = Color.Black,
                    fontSize = 14.sp,
                )
            }

        }
        if (deleteItem){
            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) {
                        coroutineScope.launch {
                            isLoading = true
                            searchViewModel.deleteMyCheck(
                                id,
                                searchItem.push!!,
                                onError = { errorMessage ->
                                    Toast.makeText(context,errorMessage,Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                },
                                onSuccess = {
                                    Toast.makeText(context,"삭제되었습니다.",Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                })
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.gray)
                ),
                shape = RoundedCornerShape(
                    topStart = 0.dp,  // 왼쪽 상단
                    topEnd = 0.dp,    // 오른쪽 상단
                    bottomEnd = 13.dp,  // 오른쪽 하단은 둥글지 않게
                    bottomStart = 13.dp // 왼쪽 하단
                )

            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                ){
                    Text(
                        text = "삭제하기",
                        color = Color.White,
                        modifier = Modifier
                            .padding(1.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }

            }
        }else if(declareItem&&info_type.equals("info")){
            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .clickable() {

                    },
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.gray)
                ),
                shape = RoundedCornerShape(
                    topStart = 0.dp,  // 왼쪽 상단
                    topEnd = 0.dp,    // 오른쪽 상단
                    bottomEnd = 13.dp,  // 오른쪽 하단은 둥글지 않게
                    bottomStart = 13.dp // 왼쪽 하단
                )

            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                ){
                    Text(
                        text = "신고된 정보입니다.",
                        color = Color.White,
                        modifier = Modifier
                            .padding(1.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }

            }
        } else{
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
                                        searchItem.push
                                    )
                                    checkSetDatabase(
                                        user,
                                        false,
                                        set_isFound,
                                        set_pushKey,
                                        searchItem.push,
                                        searchViewModel,
                                        isMyData,
                                        onError = { errorMessage ->
                                            isLoading = false
                                            Toast
                                                .makeText(context, "데이터베이스 오류", Toast.LENGTH_SHORT)
                                                .show()
                                        },
                                        onSuccess = { now_num, opposite_num, now_check, opposite_check ->
                                            if (isMyData) {
                                                searchViewModel.updateMyCheckNum(
                                                    searchItem.push.toString(),
                                                    opposite_num,
                                                    now_num,
                                                    opposite_check,
                                                    now_check
                                                )
                                            } else {
                                                searchViewModel.updateCheckNum(
                                                    searchItem.push.toString(),
                                                    opposite_num,
                                                    now_num,
                                                    opposite_check,
                                                    now_check
                                                )
                                            }
                                            searchViewModel.setChange_check(true)

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
                            id = if(searchItem.f_check ?: false)
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
                            text = if (searchItem.t_num == null || searchItem.f_num == null || searchItem.t_num == 0 || searchItem.f_num == 0) {
                                if (searchItem.f_num == 0){
                                    "0 (0%)"
                                }else{
                                    "${searchItem.f_num} (100%)"
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
                        .clickable(enabled = !isLoading) {
                            if (currentUser != null) {
                                isLoading = true
                                coroutineScope.launch {
                                    val user = currentUser?.email?.substringBefore("@")
                                    val (set_isFound, set_pushKey) = checkDatabase(
                                        user,
                                        searchItem.push
                                    )
                                    checkSetDatabase(
                                        user,
                                        true,
                                        set_isFound,
                                        set_pushKey,
                                        searchItem.push,
                                        searchViewModel,
                                        isMyData,
                                        onError = { errorMessage ->
                                            isLoading = false
                                            Toast
                                                .makeText(context, "데이터베이스 오류", Toast.LENGTH_SHORT)
                                                .show()
                                        },
                                        onSuccess = { now_num, opposite_num, now_check, opposite_check ->
                                            if (isMyData) {
                                                searchViewModel.updateMyCheckNum(
                                                    searchItem.push.toString(),
                                                    now_num,
                                                    opposite_num,
                                                    now_check,
                                                    opposite_check
                                                )
                                            } else {
                                                searchViewModel.updateCheckNum(
                                                    searchItem.push.toString(),
                                                    now_num,
                                                    opposite_num,
                                                    now_check,
                                                    opposite_check
                                                )
                                            }
                                            searchViewModel.setChange_check(true)
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
                            id = if(searchItem.t_check ?: false)
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
                            text = if (searchItem.t_num == null || searchItem.f_num == null || searchItem.t_num == 0 || searchItem.f_num == 0) {
                                if (searchItem.t_num == 0){
                                    "0 (0%)"
                                }else{
                                    "${searchItem.t_num} (100%)"
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

}
