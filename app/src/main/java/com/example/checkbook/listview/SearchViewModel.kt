package com.example.checkbook.listview

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.checkbook.auth.AuthRepository
import com.example.checkbook.database.checkDatabase
import com.example.checkbook.database.getCheckValue
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class SearchViewModel @Inject constructor() : ViewModel() {
    // Firebase에서 아이템을 가져오는 로직
    private val searchItems = MutableLiveData<List<SearchItem>>()
    val items: LiveData<List<SearchItem>> = searchItems
    /*
    데이터 변경시 자동 호출
    val items: LiveData<List<SearchItem>> = liveData {
        val data = fetchDataFromFirebase()
        emit(data)
    }

     */
    // 데이터를 수동으로 로드하는 함수

    private var _isDatabase = false
    val isDatabase: Boolean = _isDatabase

    fun yetDatabase() {
        _isDatabase = false
    }

    fun loadData() {
        if (_isDatabase) return
        //searchItems.postValue(emptyList()) //기존데이터 초기화

        viewModelScope.launch {
            val data = fetchDataFromFirebase()
            searchItems.postValue(data)
            _isDatabase = true
        }
    }

    private suspend fun fetchDataFromFirebase(): List<SearchItem> {
        // 실제 Firebase 데이터를 가져오는 코드

        return withContext(Dispatchers.IO) {
            Log.d("firebase", "firebase")
            val dataList = mutableListOf<SearchItem>()
            val databaseReference = Firebase.database.getReference("info") // Firebase 경로

            // Firebase 데이터를 한 번만 가져옴
            val snapshot = databaseReference.get().await() // KTX 확장을 사용해 비동기 작업을 동기적으로 처리
            for (child in snapshot.children) {
                val item = child.getValue(SearchItem::class.java) // Firebase 데이터를 SearchItem으로 매핑

                if (item?.id != null) {
                    val databaseReference2 = Firebase.database.getReference("id")
                        .child(item.id).child("name") // name 경로

                    val nameValue = databaseReference2.get().await().getValue(String::class.java) ?: "익명" // 값이 없으면 "익명" 기본값 사용

                    var tCheck = false
                    var fCheck = false
                    if (AuthRepository().getCurrentUser() != null) {
                        val user = AuthRepository().getCurrentUser()?.email?.substringBefore("@")
                        val (isFound, pushKey) = checkDatabase(user, child.key.toString())

                        if (isFound) {
                            val (tCheckStatus, fCheckStatus) = getCheckStatus(FirebaseDatabase.getInstance().reference, user, pushKey!!)
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

            dataList // 가져온 데이터를 반환
        }
        /*
        return listOf(
            SearchItem("seob614", "전문가","","2025년01월03일19시04분10초","jetpack compose란?",
                "Jetpack Compose는 UI 개발을 간소화하기 위해 설계된 최신 툴킷",
                10,2),
            SearchItem("courr", "코알","","2025년01월03일07시12분18초","안드로이드는 어려워",
                "안드로이드(영어: Android)는 스마트폰, 태블릿 PC 같은 터치스크린 모바일 장치 용으로 디자인된 운영 체제이자 수정된 리눅스 커널 버전을 비롯한 오픈 소스 소프트웨어에 기반을 둔 모바일 운영 체제다. 또한, 운영 체제와 미들웨어, 사용자 인터페이스 그리고 표준 응용 프로그램(웹 브라우저, 이메일 클라이언트, 단문 메시지 서비스(SMS), 멀티미디어 메시지 서비스(MMS) 등을 포함하고 있는 소프트웨어 스택이자 모바일 운영 체제이다. 안드로이드는 개발자들이 자바와 코틀린 언어로 응용 프로그램을 작성할 수 있게 하였으며, 컴파일된 바이트코드를 구동할 수 있는 런타임 라이브러리를 제공한다. 또한 안드로이드 소프트웨어 개발 키트(SDK)를 통해 응용 프로그램을 개발하는 데 필요한 각종 도구와 응용 프로그램 인터페이스(API)를 제공한다.",
                20,1),
        )

         */
    }

    // SearchItem을 업데이트하는 함수
    fun updateCheckNum(push: String, tNum: Int, fNum: Int,tCheck:Boolean,fCheck:Boolean) {
        // 해당 push 값을 가진 아이템만 업데이트
        val updatedList = searchItems.value?.map { searchItem ->
            if (searchItem.push == push) {
                // 아이템이 찾았으면 그 값만 업데이트
                searchItem.copy(t_num = searchItem.t_num!! + tNum, f_num = searchItem.f_num!! + fNum, t_check = tCheck, f_check = fCheck)
            } else {
                // 그렇지 않으면 그대로 유지
                searchItem
            }
        }
        // 업데이트된 리스트를 searchItems에 반영
        searchItems.value = updatedList!!
    }

    private val searchItemsMy = MutableLiveData<List<SearchItem>>()  // 마이 데이터
    val itemsMy: LiveData<List<SearchItem>> = searchItemsMy

    private var _isDatabase_my = false
    val isDatabase_my: Boolean = _isDatabase_my

    fun yetDatabase_my() {
        _isDatabase_my = false
    }

    fun loadData_my(id: String) {
        if (_isDatabase_my) return

        viewModelScope.launch {
            val data = fetchDataFromFirebase_my(id)
            searchItemsMy.postValue(data)
            _isDatabase_my = true
        }
    }

    private suspend fun fetchDataFromFirebase_my(id:String): List<SearchItem> {
        // 실제 Firebase 데이터를 가져오는 코드

        return withContext(Dispatchers.IO) {
            Log.d("firebase", "firebase")
            val dataList = mutableListOf<SearchItem>()
            val databaseReference = Firebase.database.getReference("id").child(id).child("info") // Firebase 경로

            // Firebase 데이터를 한 번만 가져옴
            val snapshot = databaseReference.get().await()
            val infoList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

            infoList?.forEach { value ->
                val databaseReference2 = Firebase.database.getReference("info").child(value.toString())

                val snapshot = databaseReference2.get().await()
                if (snapshot.exists()) {
                    val item = snapshot.getValue(SearchItem::class.java) // Firebase 데이터를 SearchItem으로 매핑

                    if (item != null) {
                        val databaseReference3 = Firebase.database.getReference("id")
                            .child(item.id!!).child("name") // name 경로

                        val nameValue = databaseReference3.get().await().getValue(String::class.java) ?: "익명" // 값이 없으면 "익명" 기본값 사용

                        var tCheck = false
                        var fCheck = false

                        val (isFound, pushKey) = checkDatabase(id, value.toString())

                        if (isFound) {
                            val (tCheckStatus, fCheckStatus) = getCheckStatus(FirebaseDatabase.getInstance().reference, id, pushKey!!)
                            tCheck = tCheckStatus ?: false
                            fCheck = fCheckStatus ?: false
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
            dataList // 가져온 데이터를 반환
        }
        /*
        return listOf(
            SearchItem("seob614", "전문가","","2025년01월03일19시04분10초","jetpack compose란?",
                "Jetpack Compose는 UI 개발을 간소화하기 위해 설계된 최신 툴킷",
                10,2),
            SearchItem("courr", "코알","","2025년01월03일07시12분18초","안드로이드는 어려워",
                "안드로이드(영어: Android)는 스마트폰, 태블릿 PC 같은 터치스크린 모바일 장치 용으로 디자인된 운영 체제이자 수정된 리눅스 커널 버전을 비롯한 오픈 소스 소프트웨어에 기반을 둔 모바일 운영 체제다. 또한, 운영 체제와 미들웨어, 사용자 인터페이스 그리고 표준 응용 프로그램(웹 브라우저, 이메일 클라이언트, 단문 메시지 서비스(SMS), 멀티미디어 메시지 서비스(MMS) 등을 포함하고 있는 소프트웨어 스택이자 모바일 운영 체제이다. 안드로이드는 개발자들이 자바와 코틀린 언어로 응용 프로그램을 작성할 수 있게 하였으며, 컴파일된 바이트코드를 구동할 수 있는 런타임 라이브러리를 제공한다. 또한 안드로이드 소프트웨어 개발 키트(SDK)를 통해 응용 프로그램을 개발하는 데 필요한 각종 도구와 응용 프로그램 인터페이스(API)를 제공한다.",
                20,1),
        )

         */
    }

    // SearchItem을 업데이트하는 함수
    fun updateMyCheckNum(push: String, tNum: Int, fNum: Int,tCheck:Boolean,fCheck:Boolean) {
        // 해당 push 값을 가진 아이템만 업데이트
        val updatedList = searchItemsMy.value?.map { searchItem ->
            if (searchItem.push == push) {
                // 아이템이 찾았으면 그 값만 업데이트
                searchItem.copy(t_num = searchItem.t_num!! + tNum, f_num = searchItem.f_num!! + fNum, t_check = tCheck, f_check = fCheck)
            } else {
                // 그렇지 않으면 그대로 유지
                searchItem
            }
        }
        // 업데이트된 리스트를 searchItems에 반영
        searchItemsMy.value = updatedList!!
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
}

data class SearchItem(
    val push: String? = null,
    val id: String? = null,
    val name: String? = "익명",
    val profile: String? = null,
    val date: String? = null,
    val title: String? = "",
    val info: String? = "",
    val t_num: Int? = 0,
    val f_num: Int? = 0,
    val t_check: Boolean? = false,
    val f_check: Boolean? = false,
    //val reple: ArrayList<RepleItem>
)

data class RepleItem(
    val id: String,
    val date: String,
    val info: String,
    val t_num: Int,
    val f_num: Int,
)

