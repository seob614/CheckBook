package com.courr.checkbook.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.FirebaseException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// 인증 상태 관리
val verificationId = mutableStateOf<String?>(null)
val auth = FirebaseAuth.getInstance()

// 전화번호로 인증 시작
fun startPhoneNumberVerification(
    phoneNumber: String,
    activity: Activity,
    onCodeSent: (verificationId: String) -> Unit,
    onVerificationFailed: (errorMessage: String) -> Unit // 실패 시 처리할 콜백 추가
) {
    auth.setLanguageCode("kr")
    val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
        .setPhoneNumber(phoneNumber)       // 인증할 전화번호
        .setTimeout(60L, TimeUnit.SECONDS) // 인증 타임아웃 설정
        .setActivity(activity)             // 인증 UI가 표시될 Activity
        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // 인증 완료된 경우, 자동으로 사용자 정보 처리
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // 인증 실패 처리 시 토스트 메시지 표시
                Log.e("Auth", "Phone verification failed", e)

                // Crashlytics에 오류 보고
                FirebaseCrashlytics.getInstance().log("Phone verification failed")
                FirebaseCrashlytics.getInstance().recordException(e)
                //Toast.makeText(activity, "인증 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                onVerificationFailed("${e.localizedMessage}")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // 코드 전송 완료 후, Verification ID를 저장
                Log.d("Auth", "Code sent: $verificationId")
                onCodeSent(verificationId) // 콜백 호출
            }
        })
        .build()

    PhoneAuthProvider.verifyPhoneNumber(options)
}

//아이디 중복 확인
suspend fun checkEmailBeforeRegister(email: String): Result<Unit> {
    return suspendCancellableCoroutine { continuation ->
        val db = FirebaseDatabase.getInstance().reference

        // 이메일을 "users" 경로 아래에서 찾기
        db.child("id")
            .orderByChild("id")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // 이메일이 이미 존재함
                        val errorMessage = "이미 사용 중인 아이디입니다."
                        Log.e("Auth", "Email is already in use")
                        val exception = Exception(errorMessage)
                        FirebaseCrashlytics.getInstance().log("이미 사용 중인 아이디입니다. ${exception.message}")
                        FirebaseCrashlytics.getInstance().recordException(exception)
                        continuation.resume(Result.failure(Exception(errorMessage)))
                    } else {
                        // 이메일이 사용되지 않음
                        continuation.resume(Result.success(Unit))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    val errorMessage = "회원가입 오류(e:5)"
                    Log.e("Auth", "${error.message}")
                    // Crashlytics에 오류 보고
                    FirebaseCrashlytics.getInstance().log("회원가입 오류(e:5): ${error.message}")
                    FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                    continuation.resumeWithException(Exception(errorMessage))
                }
            })
    }
}

fun signInWithPhoneAuthCredential(activity: Activity, verificationId: String, code: String, email: String?=null, password: String?=null,onError: (String) -> Unit, onSuccess: () -> Unit) {
    val credential = PhoneAuthProvider.getCredential(verificationId, code)
    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val phoneUser = task.result?.user
                // 전화번호 인증 완료 후 이메일 추가로 진행
                if (phoneUser != null && email != null && password != null) {
                    // 사용자에게 이메일과 비밀번호를 입력받고 이메일로 계정을 생성
                    linkEmailToPhoneAccount(activity, email, password, phoneUser, onError, onSuccess)
                }
            } else {
                // 실패 처리
                val errorMessage = "코드를 확인하세요. 회원가입 오류(e:2)"
                Log.e("Auth", "Phone sign-in failed", task.exception)
                val exception = task.exception ?: Exception(errorMessage)
                FirebaseCrashlytics.getInstance().log("코드를 확인하세요. 회원가입 오류(e:2): ${exception.message}")
                FirebaseCrashlytics.getInstance().recordException(exception)
                onError(errorMessage)
            }
        }
}

@SuppressLint("NewApi")
fun linkEmailToPhoneAccount(activity: Activity, email: String, password: String, phoneUser: FirebaseUser, onError: (String) -> Unit, onSuccess: () -> Unit) {
    phoneUser.linkWithCredential(EmailAuthProvider.getCredential(email!!, password))
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 이메일과 전화번호 계정이 성공적으로 연결됨
                Log.d("Auth", "Email and phone linked")
                val user = auth.currentUser
                user?.let {
                    // Realtime Database에 사용자 정보 저장
                    val db = FirebaseDatabase.getInstance().reference

                    // 사용자 고유 ID
                    val id = email.substringBefore("@")

                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy년MM월dd일HH시mm분ss초")
                    val formattedDate = currentDateTime.format(formatter)
                    // 저장할 사용자 정보
                    val userMap = mapOf(
                        "id" to id,
                        "reg_date" to formattedDate,
                        "name" to "익명",
                        "opt" to true,
                        "profile" to false,
                        "uid" to user.uid,
                    )

                    // Realtime Database의 'users' 경로에 정보 저장
                    db.child("id").child(id).setValue(userMap)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                onSuccess() // 저장 성공
                            } else {
                                val errorMessage = "사용자 정보 저장 실패: ${dbTask.exception?.message}"
                                onError(errorMessage) // 저장 실패
                                val exception = Exception(errorMessage)
                                FirebaseCrashlytics.getInstance().log("사용자 정보 저장 실패: ${exception.message}")
                                FirebaseCrashlytics.getInstance().recordException(exception)
                            }
                        }
                }?: run {
                    // user가 null일 경우 여기 실행
                    val errorMessage = "회원가입 오류(e:4)"
                    onError(errorMessage)
                    Log.e("Auth", "로그인된 사용자가 없습니다.")
                    // Crashlytics에 오류 보고
                    val exception = Exception(errorMessage)
                    FirebaseCrashlytics.getInstance().log("회원가입 오류(e:4): ${exception.message}")
                    FirebaseCrashlytics.getInstance().recordException(exception)
                }

                onSuccess()
            } else {
                val errorMessage = "회원가입 오류(e:3)"
                Log.e("Auth", "Failed to link email with phone", task.exception)
                // Crashlytics에 오류 보고
                val exception = task.exception ?: Exception(errorMessage)
                FirebaseCrashlytics.getInstance().log("회원가입 오류(e:3): ${exception.message}")
                FirebaseCrashlytics.getInstance().recordException(exception)
                onError(errorMessage)
            }
        }
}

/*
fun createAccountWithEmail(activity: Activity, email: String, password: String, phoneUser: FirebaseUser,onError: (String) -> Unit) {
    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val emailUser = task.result?.user
                // 이메일로 회원가입 완료 후, 전화번호 계정과 연결
                if (emailUser != null) {
                    //linkEmailToPhoneAccount(activity, emailUser, password, phoneUser, onError)
                }
            } else {
                val errorMessage = "회원가입 오류(e:2)"
                Log.e("Auth", "Email registration failed", task.exception)
                onError(errorMessage)
            }
        }
}

 */