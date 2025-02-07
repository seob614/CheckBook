package com.example.checkbook.listview

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchViewModel @Inject constructor() : ViewModel() {
    // Firebase에서 아이템을 가져오는 로직
    var selectedSearchItem: SearchItem? = null
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

                    val updatedItem = item.copy(name = nameValue) // 기존 객체에 name 값 업데이트
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

                        val updatedItem = item.copy(name = nameValue) // 기존 객체에 name 값 업데이트
                        dataList.add(updatedItem) // 수정된 아이템을 dataList에 추가
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
    //val reple: ArrayList<RepleItem>
)

data class RepleItem(
    val id: String,
    val date: String,
    val info: String,
    val t_num: Int,
    val f_num: Int,
)

