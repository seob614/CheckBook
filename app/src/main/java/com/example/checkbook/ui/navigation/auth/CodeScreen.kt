package com.example.checkbook.ui.navigation.auth

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.checkbook.R
import com.example.checkbook.TopBarWithBackButton
import com.example.checkbook.auth.signInWithPhoneAuthCredential
import com.example.checkbook.mvi.MainViewModel
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch

const val CodeRoute = "code_route"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CodeScreen(mainViewModel: MainViewModel, navController: NavController, verificationId: String? = null, email: String? = null, password: String? = null) {
    var code by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    if (verificationId == null || email == null || password == null) {
        Toast.makeText(context, "입력 오류", Toast.LENGTH_SHORT).show()
        navController.popBackStack()
        return
    }
    BackHandler {
        navController.popBackStack()
    }
    
    Scaffold(
        topBar = {
            TopBarWithBackButton(
                title = "회원가입",
                onBackPressed = {
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
                // 인증 코드 입력
                BasicTextField(
                    value = code,
                    onValueChange = { newValue -> code = newValue.take(6) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, bottom = 15.dp)
                        .border(1.dp, Color.Gray),
                    singleLine = true,
                    maxLines = 1,
                    decorationBox = { innerTextField ->
                        Box(Modifier.padding(12.dp)) {
                            if (code.isEmpty()) {
                                Text(text = "코드", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    }
                )

                // 코드 확인 버튼
                Button(
                    onClick = {
                        if (code.isNotEmpty()) {
                            isLoading = true

                            coroutineScope.launch {
                                signInWithPhoneAuthCredential(
                                    context as Activity,
                                    verificationId,
                                    code,
                                    email,
                                    password,
                                    onError = { errorMessage ->
                                        isLoading = false
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    },
                                    onSuccess = {
                                        isLoading = false
                                        Toast.makeText(context, "회원가입 완료", Toast.LENGTH_SHORT).show()
                                        mainViewModel.hideDetail()
                                        navController.popBackStack(SignInRoute, true)
                                    }
                                )
                            }
                        } else {
                            errorMessage = "코드를 입력해주세요."
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
                    enabled = !isLoading,
                ) {
                    Text(text = if (isLoading) "확인 중..." else "코드확인")
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