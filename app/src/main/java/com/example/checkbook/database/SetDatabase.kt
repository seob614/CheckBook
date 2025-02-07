package com.example.checkbook.database

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.checkbook.auth.auth
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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