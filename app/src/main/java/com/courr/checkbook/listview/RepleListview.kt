package com.courr.checkbook.listview

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.courr.checkbook.dialog.MoreDialog
import com.courr.checkbook.dialog.RepleMoreDialog
import com.courr.checkbook.ui.navigation.search.DetailInfoRoute
import com.courr.checkbook.viewmodel.MyInfoViewModel
import com.courr.checkbook.viewmodel.RepleItem
import com.courr.checkbook.viewmodel.RepleViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RepleListView(repleViewModel: RepleViewModel, searchViewModel: SearchViewModel, myInfoViewModel: MyInfoViewModel, navController: NavController, id:String, searchItem: SearchItem?,screenType: String,isMyData: Boolean?) {

    val hashMap = searchItem?.reple

    LaunchedEffect(id) {  // id가 변경될 때마다 적절한 데이터 로드
        if (screenType.equals("myreple")) {
            //searchViewModel.loadData_my(id,list)
        } else {
            repleViewModel.loadData(hashMap?:HashMap<String,String>())
        }
    }

    val items by if (screenType.equals("myreple")) {
        repleViewModel.itemsMy.collectAsState(emptyList())
    } else {
        repleViewModel.items.collectAsState(emptyList())
    }

    LazyColumn(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(items) { item ->
            RepleListItem(
                navController = navController,  // navController 전달
                repleItem = item,
                searchViewModel = searchViewModel,
                myInfoViewModel = myInfoViewModel,
                repleViewModel = repleViewModel,
                screenType = screenType,
                isMyData = isMyData,
                id = id,
                searchItem = searchItem
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RepleListItem(
    navController: NavController,
    repleItem: RepleItem,
    searchViewModel: SearchViewModel,
    myInfoViewModel: MyInfoViewModel,
    repleViewModel: RepleViewModel,
    screenType: String,
    isMyData: Boolean?,
    id: String,
    searchItem: SearchItem?
) {
    val context = LocalContext.current
    val currentUser by myInfoViewModel.currentUser.observeAsState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var deleteItem by remember { mutableStateOf(false) }
    var declareItem by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    deleteItem = if (repleItem.delete?:false) true else false
    declareItem = if ((repleItem.declare?:ArrayList<String>()).size>10) true else false
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable {
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
                Icon(
                    painter = painterResource(
                        id = R.drawable.bottom_user_on
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(top = 5.dp)
                ){
                    Text(
                        text = repleItem.name ?: "익명",
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.Black,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = repleItem.date!!,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 8.sp,
                        color = Color.Gray,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(
                        id = R.drawable.more
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            showDialog = true
                        },
                    tint = Color.Unspecified
                )
            }
            Text(
                text = if(declareItem){
                    " 신고된 정보입니다."
                }else{
                    " "+repleItem.info
                },
                color = if (declareItem){
                    Color.Red
                }else{
                    Color.Black
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp) // 최대 높이 설정
                    .padding(vertical = 3.dp)
                    .padding(horizontal = 2.dp),
                overflow = TextOverflow.Ellipsis,

            )

        }
        /*
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
                                repleItem.push!!,
                                onError = { errorMessage ->
                                    Toast.makeText(context,errorMessage, Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                },
                                onSuccess = {
                                    Toast.makeText(context,"삭제되었습니다.", Toast.LENGTH_SHORT).show()
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
        }else if(declareItem&&isMyData){
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
                                        repleItem.push
                                    )
                                    checkSetDatabase(
                                        user,
                                        false,
                                        set_isFound,
                                        set_pushKey,
                                        repleItem.push,
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
                                                    repleItem.push.toString(),
                                                    opposite_num,
                                                    now_num,
                                                    opposite_check,
                                                    now_check
                                                )
                                            } else {
                                                searchViewModel.updateCheckNum(
                                                    repleItem.push.toString(),
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
                            id = if(repleItem.f_check ?: false)
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
                            text = if (repleItem.t_num == null || repleItem.f_num == null || repleItem.t_num == 0 || repleItem.f_num == 0) {
                                if (repleItem.f_num == 0){
                                    "0 (0%)"
                                }else{
                                    "${repleItem.f_num} (100%)"
                                }

                            } else {
                                "${repleItem.f_num} (${"%.0f%%".format((repleItem.f_num.toFloat() / (repleItem.t_num + repleItem.f_num).toFloat()) * 100)})"
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
                                        repleItem.push
                                    )
                                    checkSetDatabase(
                                        user,
                                        true,
                                        set_isFound,
                                        set_pushKey,
                                        repleItem.push,
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
                                                    repleItem.push.toString(),
                                                    now_num,
                                                    opposite_num,
                                                    now_check,
                                                    opposite_check
                                                )
                                            } else {
                                                searchViewModel.updateCheckNum(
                                                    repleItem.push.toString(),
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
                            id = if(repleItem.t_check ?: false)
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
                            text = if (repleItem.t_num == null || repleItem.f_num == null || repleItem.t_num == 0 || repleItem.f_num == 0) {
                                if (repleItem.t_num == 0){
                                    "0 (0%)"
                                }else{
                                    "${repleItem.t_num} (100%)"
                                }

                            } else {
                                "${repleItem.t_num} (${"%.0f%%".format((repleItem.t_num.toFloat() / (repleItem.t_num + repleItem.f_num).toFloat()) * 100)})"
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

         */
        if (showDialog) {
            val user = currentUser?.email?.substringBefore("@") ?: null
            if (user!=null){
                RepleMoreDialog(
                    searchViewModel = searchViewModel,
                    myInfoViewModel = myInfoViewModel,
                    repleViewModel = repleViewModel,
                    userId = repleItem.id!!,
                    myId = user,
                    push = repleItem.push!!,
                    info_push = repleItem.info_push!!,
                    onSuccesss = {
                        var my_data = if (repleItem.id.equals(user)) true else false
                        if (my_data){
                            val updatedList = HashMap(searchItem?.reple ?: emptyMap())
                            updatedList.remove(repleItem.push)

                            val updatedItem = searchItem?.copy(
                                reple = updatedList,
                            )
                            if (screenType.equals("check")){
                                searchViewModel.setCheckSearchItem(updatedItem)
                            }else if(screenType.equals("detail")){
                                searchViewModel.setDetailSearchItem(updatedItem,isMyData)
                            }

                            Toast.makeText(context,"삭제되었습니다.",Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(context,"신고가 접수되었습니다.",Toast.LENGTH_SHORT).show()
                        }

                        showDialog = false
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context,errorMessage,Toast.LENGTH_SHORT).show()
                        showDialog = false
                    },
                    onDismiss = { showDialog = false }
                )
            }else{
                Toast.makeText(context,"로그인이 필요합니다.",Toast.LENGTH_SHORT).show()
                showDialog = false
            }

        }
    }

}
