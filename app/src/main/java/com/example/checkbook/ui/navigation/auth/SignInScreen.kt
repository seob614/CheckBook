package com.example.checkbook.ui.navigation.auth

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
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
import com.example.checkbook.viewmodel.MyInfoViewModel
import com.example.checkbook.mvi.MainViewModel
import kotlinx.coroutines.launch

const val SignInRoute = "sign_in_route"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SignInScreen(mainViewModel: MainViewModel, myInfoViewModel: MyInfoViewModel, navController: NavController) {
    val context = LocalContext.current

    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
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
                title = "로그인",
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
                // 이메일 입력
                BasicTextField(
                    value = email,
                    onValueChange = {
                        if (it.text.length <= 13) {
                            email = it
                        }
                    },
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, bottom = 15.dp)
                        .border(1.dp, Color.Gray),
                    decorationBox = { innerTextField ->
                        Box(Modifier.padding(12.dp)) {
                            if (email.text.isEmpty()) {
                                Text(text = "아이디", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    }
                )

                // 비밀번호 입력
                BasicTextField(
                    value = password,
                    onValueChange = {
                        if (it.text.length <= 16) {
                            password = it
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, bottom = 15.dp)
                        .border(1.dp, Color.Gray),
                    singleLine = true,
                    maxLines = 1,
                    decorationBox = { innerTextField ->
                        Box(Modifier.padding(12.dp)) {
                            if (password.text.isEmpty()) {
                                Text(text = "비밀번호", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    }
                )

                // 로그인 버튼
                Button(
                    onClick = {
                        if (email.text.isNotEmpty() && password.text.isNotEmpty()) {
                            isLoading = true
                            coroutineScope.launch {
                                val user = myInfoViewModel.signIn(email.text.trim()+"@checkbook.com", password.text)
                                isLoading = false
                                if (user != null) {
                                    // 로그인 성공
                                    mainViewModel.hideDetail()
                                    navController.popBackStack()
                                    Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show()
                                } else {
                                    // 로그인 실패
                                    errorMessage = "로그인 실패: 아이디 또는 비밀번호를 확인해주세요."
                                }
                            }
                        } else {
                            errorMessage = "아이디과 비밀번호를 입력해주세요."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, bottom = 10.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.main_color), // 배경색
                        contentColor = Color.White    // 텍스트 색
                    ),
                    enabled = !isLoading
                ) {
                    Text(text = if (isLoading) "로그인 중..." else "로그인")
                }
                // 회원가입 버튼
                Button(
                    onClick = {
                        navController.navigate("$RegisterRoute")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, bottom = 10.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.main_color), // 배경색
                        contentColor = Color.White    // 텍스트 색
                    ),
                ) {
                    Text(text = "회원가입")
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
    SignInScreen(mainViewModel,myInfoViewModel,navController)
}