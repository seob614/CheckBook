package com.courr.checkbook.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.courr.checkbook.auth.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class MyInfoViewModel @Inject constructor() : ViewModel() {
    val authRepository = AuthRepository()
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    init {
        _currentUser.value = authRepository.getCurrentUser()

        currentUser.observeForever { user ->
            user?.let { fetchDataFromFirebase(it) }
        }
    }

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        return try {
            val user = authRepository.signInWithEmail(email, password)
            _currentUser.value = user
            user // 로그인된 사용자 반환
        } catch (e: Exception) {
            // Crashlytics에 오류 보고
            FirebaseCrashlytics.getInstance().log("로그인 오류")
            FirebaseCrashlytics.getInstance().recordException(e)
            null // 예외 처리
        }
    }

    private val _myInfoItem = MutableLiveData<MyInfoItem>()
    val myInfoItem: LiveData<MyInfoItem> = _myInfoItem

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _myInfoItem.value  = MyInfoItem()
    }

    private fun fetchDataFromFirebase(user: FirebaseUser) {
        val id = user.email?.substringBefore("@") ?:return
        val databaseReference = Firebase.database.getReference("id").child(id)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(MyInfoItem::class.java) // 단일 객체 매핑
                _myInfoItem.value = item ?: MyInfoItem() // 값이 없으면 기본값 설정
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "Error fetching data: ${error.message}")
                val errorMessage = "Error fetching data"
                // Crashlytics에 오류 보고
                FirebaseCrashlytics.getInstance().log("Error fetching data: ${error.message}")
                FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
            }
        })
    }

    fun updateUserName(userId: String, newName: String, onComplete: (Boolean) -> Unit) {
        val databaseReference = Firebase.database.getReference("id").child(userId)

        val updates = mapOf("name" to newName) // 업데이트할 데이터
        databaseReference.updateChildren(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { exception ->
                onComplete(false)
                // FirebaseCrashlytics에 오류 보고
                FirebaseCrashlytics.getInstance().log("Failed to update database: ${exception.message}")
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
    }
}
data class MyInfoItem(
    val id: String? = null,
    val reg_date: String? = null,
    val name: String? = "익명",
    val opt: Boolean? = true,
    val uid: String? = null,
    val info: ArrayList<String>? = ArrayList(),
    val reple: ArrayList<String>? = ArrayList(),
    val check: Map<String, Map<String, Any>>? = null
    //val reple: ArrayList<RepleItem>
)

