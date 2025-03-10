package com.courr.checkbook.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courr.checkbook.auth.AuthRepository
import com.courr.checkbook.database.checkDatabase
import com.courr.checkbook.database.reple_checkDatabase
import com.courr.checkbook.listview.SearchItem
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class RepleViewModel @Inject constructor() : ViewModel() {
    val isBottomSheetVisible = mutableStateOf(false)

    // 바텀 시트 열기
    fun showBottomSheetDialog() {
        isBottomSheetVisible.value = true
    }

    // 바텀 시트 닫기
    fun hideBottomSheetDialog() {
        isBottomSheetVisible.value = false
    }

    private val repleItems = MutableStateFlow<List<RepleItem>>(emptyList())
    val items: StateFlow<List<RepleItem>> = repleItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    private var _isDatabase = false
    val isDatabase: Boolean = _isDatabase

    fun yetDatabase() {
        _isDatabase = false
    }

    fun loadData(reple:HashMap<String,String>) {
        if (_isDatabase) return
        setLoading(true)
        viewModelScope.launch {
            val data = fetchDataFromFirebase(reple)
            repleItems.value = data
            _isDatabase = true
            setLoading(false)
        }
    }

    private suspend fun fetchDataFromFirebase(reple:HashMap<String,String>): List<RepleItem> {
        // 실제 Firebase 데이터를 가져오는 코드

        return withContext(Dispatchers.IO) {
            Log.d("firebase", "firebase")
            val dataList = mutableListOf<RepleItem>()

            for(reple_push in reple){
                val databaseReference = Firebase.database.getReference("reple").child(reple_push.value) // Firebase 경로

                // Firebase 데이터를 한 번만 가져옴
                val snapshot = databaseReference.get().await() // KTX 확장을 사용해 비동기 작업을 동기적으로 처리
                if (snapshot.exists()){
                    val item = snapshot.getValue(RepleItem::class.java) // Firebase 데이터를 SearchItem으로 매핑

                    if (item != null) {
                        if ((item.declare?:ArrayList<String>()).size>10){
                            continue
                        }
                        val databaseReference2 = Firebase.database.getReference("id")
                            .child(item.id!!).child("name") // name 경로

                        val nameValue = databaseReference2.get().await().getValue(String::class.java) ?: "익명" // 값이 없으면 "익명" 기본값 사용

                        var tCheck = false
                        var fCheck = false
                        if (AuthRepository().getCurrentUser() != null) {
                            val user = AuthRepository().getCurrentUser()?.email?.substringBefore("@")
                            val (isFound, pushKey) = reple_checkDatabase(user, reple_push.value)

                            if (isFound) {
                                val (tCheckStatus, fCheckStatus) = getCheckStatus(
                                    FirebaseDatabase.getInstance().reference, user, pushKey!!)
                                tCheck = tCheckStatus ?: false
                                fCheck = fCheckStatus ?: false
                            }
                        }

                        val updatedItem = item.copy(
                            name = nameValue,
                            t_check = tCheck,
                            f_check = fCheck
                        )
                        dataList.add(updatedItem)
                    }
                }
            }
            dataList.reverse()
            dataList // 가져온 데이터를 반환
        }
    }

    suspend fun getCheckStatus(db: DatabaseReference, id: String?, pushKey: String): Pair<Boolean?, Boolean?> {
        return suspendCancellableCoroutine { continuation ->
            db.child("id").child(id.toString()).child("check").child(pushKey).child("check")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val value = snapshot.value?.toString()

                        if (value == null) {
                            // 값이 없으면 null 반환
                            continuation.resume(Pair(null, null))
                        } else {
                            // 값이 있는 경우 true는 tCheck, false는 fCheck로 반환
                            val isTrue = value.toBoolean()
                            val tCheck = if (isTrue) true else false
                            val fCheck = if (isTrue) false else true
                            continuation.resume(Pair(tCheck, fCheck))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Firebase 요청 실패 시 null 반환
                        Log.e("FirebaseError", "Failed to get check value: ${error.message}")
                        continuation.resume(Pair(null, null))
                    }
                })
        }
    }

    private val repleItemsMy = MutableStateFlow<List<RepleItem>>(emptyList())
    val itemsMy: StateFlow<List<RepleItem>> = repleItemsMy.asStateFlow()

    private var _isDatabase_my = false
    val isDatabase_my: Boolean = _isDatabase_my

    fun yetDatabase_my() {
        _isDatabase_my = false
    }

    suspend fun deleteReple(userId: String, push: String, info_push:String, onError: (String) -> Unit, onSuccess: () -> Unit) {
        val db = FirebaseDatabase.getInstance().reference
        val idRef = db.child("id").child(userId).child("reple")
        val infoRef = db.child("info").child(info_push).child("reple")

        try {
            db.child("reple").child(push).removeValue().await()

            val snapshot = idRef.get().await()
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    if (child.value == push) { // push 값이 일치하는 경우
                        idRef.child(child.key!!).removeValue().await() // 삭제 후 대기
                        break
                    }
                }
            }

            val snapshot2 = infoRef.get().await()
            if (snapshot2.exists()) {
                for (child in snapshot2.children) {
                    if (child.value == push) { // push 값이 일치하는 경우
                        infoRef.child(child.key!!).removeValue().await() // 삭제 후 대기
                        break
                    }
                }
            }

            repleItems.value = repleItems.value.filterNot { it.push == push }
            onSuccess()
        } catch (e: Exception) {
            val errorMessage = "데이터 삭제 실패: ${e.message}"
            onError(errorMessage)  // 실패하면 onError 호출
            FirebaseCrashlytics.getInstance().log("데이터 삭제 실패: ${e.message}")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun declareReple(userId: String, push: String, onError: (String) -> Unit, onSuccess: () -> Unit) {
        val db = FirebaseDatabase.getInstance().reference
        val repleRef = db.child("reple").child(push).child("declare")

        try {
            if (declareDatabase(userId, push)){
                onError("이미 신고한 정보입니다.")
                return
            }
            declare_runTransactionAsTask(userId, repleRef).addOnCompleteListener { overallTask ->
                if (overallTask.isSuccessful) {
                    val newDeclareList = overallTask.result ?: ArrayList()

                    val updatedList = repleItems.value?.map { repleItem ->
                        if (repleItem.push == push) {
                            // 아이템이 찾았으면 그 값만 업데이트
                            repleItem.copy(declare = newDeclareList)
                        } else {
                            // 그렇지 않으면 그대로 유지
                            repleItem
                        }
                    }
                    repleItems.value = updatedList!!
                    onSuccess()
                } else {
                    val errorMessage = "신고 저장 실패: ${overallTask.exception?.message}"
                    onError(errorMessage)
                    val exception = overallTask.exception ?: Exception(errorMessage)
                    FirebaseCrashlytics.getInstance().log("신고 저장 실패: ${exception.message}")
                    FirebaseCrashlytics.getInstance().recordException(exception)
                }
            }
        } catch (e: Exception) {
            val errorMessage = "신고 실패: ${e.message}"
            onError(errorMessage)  // 실패하면 onError 호출
            FirebaseCrashlytics.getInstance().log("신고 실패: ${e.message}")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    suspend fun declareDatabase(id: String, push: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val db = FirebaseDatabase.getInstance().reference
            db.child("reple").child(push).child("declare")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var isFound = false
                        for (child in snapshot.children) {
                            if (child.value == id) {
                                isFound = true
                                break
                            }
                        }
                        if (isFound){
                            continuation.resume(true)
                        }else{
                            continuation.resume(false)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(Exception("reple_push 검색 오류"))
                    }
                })
        }
    }
    fun declare_runTransactionAsTask(idKey:String, reference: DatabaseReference): Task<ArrayList<String>> {
        val taskCompletionSource = TaskCompletionSource<ArrayList<String>>()

        reference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val existingList = mutableData.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                val updatedList = existingList + idKey // 기존 데이터에 새 값 추가
                mutableData.value = updatedList
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    taskCompletionSource.setException(error.toException()) // 오류 발생 시 Task 실패
                    Log.e("firebase", "Error runTransaction: ${error.message}")
                    val errorMessage = "Error runTransaction"
                    // Crashlytics에 오류 보고
                    FirebaseCrashlytics.getInstance().log("Error runTransaction: ${error.message}")
                    FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                } else {
                    val updatedDeclareList = currentData?.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: java.util.ArrayList()
                    taskCompletionSource.setResult(updatedDeclareList)
                }
            }
        })

        return taskCompletionSource.task
    }

}

data class RepleItem(
    val push: String? = null,
    val info_push: String? = null,
    val name: String? = "익명",
    val id: String? = null,
    val date: String? = null,
    val info: String? = "",
    val t_num: Int? = 0,
    val f_num: Int? = 0,
    val t_check: Boolean? = false,
    val f_check: Boolean? = false,
    val delete: Boolean? = false,
    val declare: ArrayList<String>? = ArrayList(),
)