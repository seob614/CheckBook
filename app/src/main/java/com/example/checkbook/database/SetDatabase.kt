package com.example.checkbook.database

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.checkbook.auth.auth
import com.example.checkbook.viewmodel.MyInfoItem
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SuppressLint("NewApi")
fun infoSetDatabase(id: String, title: String, info: String, onError: (String) -> Unit, onSuccess: () -> Unit) {
    // Realtime Database에 사용자 정보 저장
    val db = FirebaseDatabase.getInstance().reference
    val newRef = db.child("info").push()
    val pushKey = newRef.key

    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy년MM월dd일HH시mm분ss초")
    val formattedDate = currentDateTime.format(formatter)
    // 저장할 사용자 정보
    val infoMap = mapOf(
        "push" to pushKey,
        "id" to id,
        "date" to formattedDate,
        "title" to title,
        "info" to info,
        "t_num" to 0,
        "f_num" to 0,
    )

    //runTransaction 여러 사용자가 동시에 데이터를 추가해도 기존 데이터가 사라지지 않고, 원자적(atomic)으로 안전하게 업데이트
    val task1 = runTransactionAsTask(pushKey.toString(),db.child("id").child(id).child("info"))

    val task2 = newRef.setValue(infoMap)

    // 두 작업이 모두 완료될 때까지 기다림
    Tasks.whenAll(task1, task2).addOnCompleteListener { overallTask ->
        if (overallTask.isSuccessful) {
            onSuccess() // 두 작업 모두 성공
        } else {
            val errorMessage = "지식 저장 실패: ${overallTask.exception?.message}"
            onError(errorMessage)
            val exception = overallTask.exception ?: Exception(errorMessage)
            FirebaseCrashlytics.getInstance().log("지식 저장 실패: ${exception.message}")
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }
}

suspend fun checkDatabase(id: String?, info_push: String?): Pair<Boolean, String?> {
    return suspendCancellableCoroutine { continuation ->
        val db = FirebaseDatabase.getInstance().reference
        db.child("id").child(id.toString()).child("check")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var isFound = false
                    var pushKey: String? = null
                    for (pushSnapshot in snapshot.children) {
                        val get_info_push = pushSnapshot.child("info_push").value
                        if (get_info_push != null && get_info_push == info_push) {
                            isFound = true
                            pushKey = pushSnapshot.key
                            break
                        }
                    }
                    continuation.resume(Pair(isFound, pushKey))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(Exception("info_push 검색 오류"))
                }
            })
    }
}
suspend fun getCheckValue(db: DatabaseReference, id: String?, pushKey: String): Boolean {
    return suspendCancellableCoroutine { continuation ->
        db.child("id").child(id.toString()).child("check").child(pushKey).child("check")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val getCheck = snapshot.value.toString().toBoolean()
                    continuation.resume(getCheck)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(Exception("Failed to get check value"))
                }
            })
    }
}

suspend fun checkSetDatabase(id: String?, check: Boolean, isFound: Boolean, pushKey: String?, info_push: String?, onError: (String) -> Unit, onSuccess: (now_num:Int,opposite_num:Int,now_check:Boolean, opposite_check:Boolean) -> Unit) {
    val db = FirebaseDatabase.getInstance().reference

    try {
        if (isFound) {
            // get_check 값을 동기적으로 가져옴
            val getCheck = getCheckValue(db, id.toString(), pushKey.toString())
            val updates = mapOf("check" to check)
            db.child("id").child(id.toString()).child("check").child(pushKey.toString()).updateChildren(updates).await()

            val checkRef = if (check) "t_num" else "f_num"
            // 현재 checkRef의 값을 가져옴
            val snapshot = db.child("info").child(info_push.toString()).child(checkRef).get().await()
            val checkNum = snapshot.value.toString().toInt()

            // getCheck와 check가 같으면 현재 checkRef의 값을 -1
            // getCheck와 check가 다르면 반대 checkRef의 값을 -1
            if (getCheck == check) {
                val updates2 = mapOf(checkRef to checkNum - 1)
                db.child("info").child(info_push.toString()).updateChildren(updates2).await()
                db.child("id").child(id.toString()).child("check").child(pushKey.toString()).removeValue()
                onSuccess(-1,0,false,false)
            } else {
                // 반대 checkRef의 값을 가져옴
                val oppositeCheckRef = if (checkRef == "t_num") "f_num" else "t_num"
                val oppositeSnapshot = db.child("info").child(info_push.toString()).child(oppositeCheckRef).get().await()
                val oppositeCheckNum = oppositeSnapshot.value.toString().toInt()

                // 반대 checkRef의 값을 -1 하고, 현재 checkRef의 값을 +1
                val updates2 = mapOf(
                    checkRef to checkNum + 1,
                    oppositeCheckRef to oppositeCheckNum - 1
                )
                db.child("info").child(info_push.toString()).updateChildren(updates2).await()
                onSuccess(1,-1,true,false)
            }

        } else {
            val newRef = db.child("id").child(id.toString()).child("check").push()
            val newPushKey = newRef.key

            val checkMap = mapOf(
                "push" to newPushKey,
                "check" to check,
                "info_push" to info_push,
            )
            newRef.setValue(checkMap).await()

            var checkRef = if (check) "t_num" else "f_num"

            val snapshot = db.child("info").child(info_push.toString()).child(checkRef).get().await()
            val checkNum = snapshot.value.toString().toInt()
            val updates = mapOf(checkRef to checkNum + 1)
            db.child("info").child(info_push.toString()).updateChildren(updates).await()

            onSuccess(1,0,true,false) // 성공적으로 완료되면 onSuccess 호출
        }
    } catch (e: Exception) {
        val errorMessage = "check 저장 실패: ${e.message}"
        onError(errorMessage)  // 실패하면 onError 호출
        FirebaseCrashlytics.getInstance().log("check 저장 실패: ${e.message}")
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

fun runTransactionAsTask(pushKey:String, reference: DatabaseReference): Task<Void> {
    val taskCompletionSource = TaskCompletionSource<Void>()

    reference.runTransaction(object : Transaction.Handler {
        override fun doTransaction(mutableData: MutableData): Transaction.Result {
            val existingList = mutableData.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
            val updatedList = existingList + pushKey // 기존 데이터에 새 값 추가
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
                taskCompletionSource.setResult(null) // 성공 시 Task 완료
            }
        }
    })

    return taskCompletionSource.task
}