package com.courr.checkbook.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(){
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // 이메일로 로그인
    suspend fun signInWithEmail(email: String, password: String): FirebaseUser? {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            authResult.user  // 로그인 성공 시 반환
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e("AuthError", "등록되지 않은 아이디입니다: ${e.message}")
            null
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("AuthError", "비밀번호가 잘못되었습니다: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("AuthError", "기타 인증 오류: ${e.message}")
            null
        }
    }

    // 현재 로그인된 사용자 가져오기
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    // 로그아웃
    fun signOut() {
        firebaseAuth.signOut()
    }
}