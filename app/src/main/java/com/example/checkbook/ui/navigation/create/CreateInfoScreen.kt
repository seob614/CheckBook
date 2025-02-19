package com.example.checkbook.ui.navigation.create

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.checkbook.R
import com.example.checkbook.TopBarWithBackButton
import com.example.checkbook.auth.AuthRepository
import com.example.checkbook.auth.signInWithPhoneAuthCredential
import com.example.checkbook.database.infoSetDatabase
import com.example.checkbook.mvi.MainViewModel
import com.example.checkbook.ui.navigation.auth.RegisterRoute
import com.example.checkbook.ui.navigation.auth.SignInRoute
import com.example.checkbook.viewmodel.MyInfoViewModel
import kotlinx.coroutines.launch

const val CreateInfoRoute = "create_info_route"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CreateInfoScreen(mainViewModel: MainViewModel, myInfoViewModel: MyInfoViewModel, navController: NavController) {
    val context = LocalContext.current
    val currentUser by myInfoViewModel.currentUser.observeAsState()
    var title by remember { mutableStateOf(TextFieldValue()) }
    var info by remember { mutableStateOf(TextFieldValue()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        mainViewModel.hideDetail()
        navController.popBackStack()
    }
    Scaffold(
        topBar = {
            TopBarWithBackButton(
                title = "지식추가",
                onBackPressed = {
                    mainViewModel.hideDetail()
                    navController.popBackStack()
                },
            )

        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "제목",
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 70.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )

                BasicTextField(
                    value = title,
                    onValueChange = {
                        if (it.text.length <= 50) {
                            title = it
                        }
                    },
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .border(1.dp, Color.Gray),
                    decorationBox = { innerTextField ->
                        Box(Modifier.padding(12.dp)) {
                            if (title.text.isEmpty()) {
                                Text(text = "제목: 50자 이내", color = Color.Gray) // "제목"이 기본 텍스트로 표시
                            }
                            innerTextField()
                        }
                    }
                )

                Text(
                    text = "내용",
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(10.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )

                BasicTextField(
                    value = info,
                    onValueChange = {
                        if (it.text.length <= 300) {
                            info = it
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                        .border(1.dp, Color.Gray),
                    decorationBox = { innerTextField ->
                        Box(Modifier.padding(12.dp)) {
                            if (info.text.isEmpty()) {
                                Text(text = "내용: 300자 이내", color = Color.Gray) // "제목"이 기본 텍스트로 표시
                            }
                            innerTextField()
                        }
                    }
                )

                // 로그인 버튼
                Button(
                    onClick = {
                        if (title.text.isNotEmpty() && info.text.isNotEmpty()) {
                            isLoading = true
                            coroutineScope.launch {
                                val user = currentUser?.email?.substringBefore("@") ?: null
                                isLoading = false
                                if (user != null) {
                                    coroutineScope.launch {
                                        infoSetDatabase(
                                            user,
                                            title.text,
                                            info.text,
                                            onError = { errorMessage ->
                                                isLoading = false
                                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                            },
                                            onSuccess = {
                                                isLoading = false
                                                mainViewModel.hideDetail()
                                                navController.popBackStack()
                                                Toast.makeText(context, "업로드 성공", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }

                                } else {
                                    errorMessage = "업로드 실패: 로그인를 상태를 확인해주세요."
                                }
                            }
                        } else {
                            errorMessage = "정보를 모두 입력해주세요."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, start = 10.dp, end = 10.dp, bottom = 5.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.main_color), // 배경색
                        contentColor = Color.White    // 텍스트 색
                    ),
                    enabled = !isLoading
                ) {
                    Text(text = if (isLoading) "업로드 중..." else "업로드")
                }

                // 오류 메시지
                if (errorMessage.isNotEmpty()) {
                    Text(modifier = Modifier
                        .padding(start = 18.dp, end = 18.dp),
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    )

}

@Composable
@Preview
fun PreviewSignInScreen() {
    val mainViewModel = MainViewModel()
    val authRepository = AuthRepository()
    val myInfoViewModel = MyInfoViewModel()
    val navController = rememberNavController()
    CreateInfoScreen(mainViewModel,myInfoViewModel,navController)
}