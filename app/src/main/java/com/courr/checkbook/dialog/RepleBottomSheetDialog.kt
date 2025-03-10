package com.courr.checkbook.dialog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.courr.checkbook.R
import com.courr.checkbook.database.infoSetDatabase
import com.courr.checkbook.database.repleSetDatabase
import com.courr.checkbook.listview.RepleListView
import com.courr.checkbook.listview.SearchItem
import com.courr.checkbook.listview.SearchListView
import com.courr.checkbook.listview.SearchViewModel
import com.courr.checkbook.location.getCurrentLocationAsString
import com.courr.checkbook.mvi.ScreenIntent
import com.courr.checkbook.viewmodel.MyInfoViewModel
import com.courr.checkbook.viewmodel.RepleViewModel
import kotlinx.coroutines.launch
import java.util.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepleBottomSheetDialog(
    isVisible: Boolean,
    searchViewModel: SearchViewModel,
    myInfoViewModel: MyInfoViewModel,
    repleViewModel: RepleViewModel,
    searchItem: SearchItem?,
    screenType: String,
    isMyData: Boolean?,
    navController: NavController,
    onClickCancel: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val context = LocalContext.current
    val currentUser by myInfoViewModel.currentUser.observeAsState()
    val focusRequester = remember { FocusRequester() }
    var inputText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val isLoading by repleViewModel.isLoading.collectAsState()
    val items by repleViewModel.items.collectAsState()

    // 바텀 시트 상태 업데이트
    LaunchedEffect(isVisible) {
        if (isVisible) {
            modalBottomSheetState.show()
        } else {
            modalBottomSheetState.hide()
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = { onClickCancel() },
            sheetState = modalBottomSheetState,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color.White)
                ) {
                    Divider(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.LightGray)
                            .width(35.dp)
                            .height(3.dp)
                    )
                }
            }
        ) {
            val isHalfExpanded = modalBottomSheetState.targetValue == SheetValue.PartiallyExpanded
            val isFullyExpanded = modalBottomSheetState.targetValue == SheetValue.Expanded

            val sheetHeight = if (isFullyExpanded) 2 / 3f else 0.5f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(sheetHeight)
                    .background(Color.White)
            ) {
                if (isLoading) {
                    // 로딩 중일 때 로딩 화면 표시
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                } else{
                    Column(
                        modifier = Modifier.fillMaxSize().padding(bottom = 85.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (searchItem?.reple?.isEmpty() == true) {
                            Text(
                                text = "댓글이 없습니다.",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        } else {
                            RepleListView(repleViewModel, searchViewModel, myInfoViewModel, navController, "",
                                searchItem,screenType,isMyData)
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.White)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .heightIn(max = 120.dp),
                        placeholder = { Text("댓글을 입력하세요...") },
                        readOnly = isLoading,
                    )
                    Icon(
                        painter = painterResource(
                            id = R.drawable.upload
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(38.dp).padding(bottom = 18.dp, start = 8.dp).align(Alignment.Bottom)
                            .clickable(!isLoading) {
                                if (inputText.isNotEmpty()) {
                                    repleViewModel.setLoading(true)
                                    coroutineScope.launch {
                                        val user = currentUser?.email?.substringBefore("@") ?: null
                                        if (user != null) {
                                            coroutineScope.launch {
                                                repleSetDatabase(
                                                    user,
                                                    inputText,
                                                    searchItem?.push,
                                                    onError = { errorMessage ->
                                                        repleViewModel.setLoading(false)
                                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                    },
                                                    onSuccess = { push ->
                                                        val updatedList = HashMap(searchItem?.reple ?: emptyMap())
                                                        updatedList[push] = push

                                                        val updatedItem = searchItem?.copy(
                                                            reple = updatedList,
                                                        )
                                                        if (screenType.equals("check")){
                                                            searchViewModel.setCheckSearchItem(updatedItem)
                                                        }else if(screenType.equals("detail")){
                                                            searchViewModel.setDetailSearchItem(updatedItem,isMyData)
                                                        }

                                                        repleViewModel.yetDatabase()
                                                        repleViewModel.loadData(updatedItem?.reple!!)
                                                        repleViewModel.setLoading(false)
                                                        inputText = ""
                                                        Toast.makeText(context, "업로드 성공", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }

                                        } else {
                                            repleViewModel.setLoading(false)
                                            inputText = ""
                                            Toast.makeText(context, "업로드 실패: 로그인를 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            },
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}
