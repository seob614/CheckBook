package com.courr.checkbook.ui.navigation.auth

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.courr.checkbook.R
import com.courr.checkbook.TopBarWithBackButton
import com.courr.checkbook.auth.checkEmailBeforeRegister
import com.courr.checkbook.auth.startPhoneNumberVerification
import kotlinx.coroutines.launch

const val RegisterRoute = "register_route"
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen (navController: NavController) {
    val context = LocalContext.current

    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

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
                // 이메일 입력
                BasicTextField(
                    value = email,
                    onValueChange = {
                        if (it.text.length <= 20) {
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
                        if (it.text.length <= 20) {
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

                // 전화번호 입력
                BasicTextField(
                    value = phoneNumber,
                    onValueChange = { newValue ->
                        // 숫자만 허용
                        if (newValue.all { it.isDigit() }) {
                            phoneNumber = newValue.take(11) // 최대 11자리 제한
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, bottom = 15.dp)
                        .border(1.dp, Color.Gray),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    maxLines = 1,
                    decorationBox = { innerTextField ->
                        Box(Modifier.padding(12.dp)) {
                            if (phoneNumber.isEmpty()) {
                                Text(text = "전화번호(010-xxxx-xxxx)", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    }
                )

                // 코드 전송 버튼
                Button(
                    onClick = {
                        if (email.text.isNotEmpty() && password.text.isNotEmpty()&& phoneNumber.isNotEmpty()) {
                            isLoading = true
                            coroutineScope.launch {
                                // 아이디 조건: 5~13자, 영문 소문자, 숫자, 특수기호(_),(-)만 허용
                                val emailRegex = "^[a-z0-9_-]{5,13}$".toRegex()
                                if (!email.text.trim().matches(emailRegex)) {
                                    errorMessage = "아이디: 5~13자의 영문 소문자, 숫자와 특수기호(_),(-)만 사용 가능합니다."
                                    isLoading = false
                                    return@launch // 이메일 조건이 맞지 않으면 종료
                                }
                                // 동기적 함수로 수정
                                try {
                                    checkEmailBeforeRegister(email.text.trim()).getOrThrow()
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "회원가입 오류"
                                    isLoading = false
                                    return@launch // 이메일 중복인 경우 종료
                                }

                                // 비밀번호 조건: 8~16자, 영문 대/소문자, 숫자, 특수문자
                                val passwordRegex = "^[a-zA-Z0-9!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,16}$".toRegex()
                                if (!password.text.trim().matches(passwordRegex)) {
                                    errorMessage = "비밀번호: 8~16자의 영문 대/소문자, 숫자, 특수문자만 사용 가능합니다."
                                    isLoading = false
                                    return@launch // 비밀번호 조건이 맞지 않으면 종료
                                }
                                val formatemail = email.text.trim()+"@checkbook.com"
                                val formattedPhoneNumber = "+82" + phoneNumber.substring(1) // 전화번호 형식 변경
                                val formatpassword = password.text.trim()

                                // 인증 요청 성공 시 처리
                                startPhoneNumberVerification(formattedPhoneNumber, context as Activity,onCodeSent = { verificationId ->
                                    // verificationId를 포함해 CodeRoute로 이동
                                    navController.navigate("$CodeRoute/$verificationId/$formatemail/$formatpassword")
                                    Toast.makeText(context, "인증요청 완료", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                },
                                    onVerificationFailed = { errorMessage2 ->
                                        // 인증 실패 시 처리
                                        errorMessage = errorMessage2
                                        Toast.makeText(context, "인증요청 실패: ${errorMessage}", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                    }
                                )

                            }
                        } else {
                            errorMessage = "정보를 전부 입력해주세요."
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
                    Text(text = if (isLoading) "요청 중..." else "인증요청")
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
fun PreviewRegisterScreen() {
    val navController = rememberNavController()
    RegisterScreen(navController)
}