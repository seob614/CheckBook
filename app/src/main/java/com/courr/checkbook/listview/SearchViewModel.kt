package com.courr.checkbook.listview

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courr.checkbook.auth.AuthRepository
import com.courr.checkbook.database.checkDatabase
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

class SearchViewModel @Inject constructor() : ViewModel() {
    // Firebase에서 아이템을 가져오는 로직
    private val searchItems = MutableStateFlow<List<SearchItem>>(emptyList())
    val items: StateFlow<List<SearchItem>> = searchItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
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

    fun loadData(search_data:String) {
        if (_isDatabase) return
        setLoading(true)
        viewModelScope.launch {
            val data = fetchDataFromFirebase(search_data)
            searchItems.value = data
            _isDatabase = true
            setLoading(false)
        }
    }

    private suspend fun fetchDataFromFirebase(search_data:String): List<SearchItem> {
        // 실제 Firebase 데이터를 가져오는 코드

        return withContext(Dispatchers.IO) {
            Log.d("firebase", "firebase")
            val dataList = mutableListOf<SearchItem>()
            val databaseReference = Firebase.database.getReference("info") // Firebase 경로

            // Firebase 데이터를 한 번만 가져옴
            val snapshot = databaseReference.get().await() // KTX 확장을 사용해 비동기 작업을 동기적으로 처리
            for (child in snapshot.children) {
                val item = child.getValue(SearchItem::class.java) // Firebase 데이터를 SearchItem으로 매핑

                if (item != null) {
                    if ((item.declare?:ArrayList<String>()).size>10){
                        continue
                    }
                    val searchWords = search_data.split("\\s+".toRegex())

                    val infoContains = item.info?.let { info ->
                        searchWords.any { word -> info.contains(word, ignoreCase = true) }
                    } == true

                    val titleContains = item.title?.let { title ->
                        searchWords.any { word -> title.contains(word, ignoreCase = true) }
                    } == true

                    if (infoContains || titleContains) { // 하나라도 포함되면 추가
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
                }



            }
            dataList.reverse()
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

    //private val checkItems = MutableLiveData<List<SearchItem>>()
    //val check_items: LiveData<List<SearchItem>> = checkItems

    private val checkItems = MutableStateFlow<List<SearchItem>>(emptyList())
    val check_items: StateFlow<List<SearchItem>> = checkItems.asStateFlow()

    private var _isDatabase_check = false
    val isDatabase_check: Boolean = _isDatabase_check

    fun yetDatabase_check() {
        _isDatabase_check = false
    }

    private val _Change_check = MutableLiveData(false)
    val change_check: LiveData<Boolean> = _Change_check

    fun setChange_check(value: Boolean) {
        _Change_check.value = value
    }

    fun loadData_check() {
        if (_isDatabase_check) return
        setLoading(true)
        viewModelScope.launch {
            val data = fetchDataFromFirebase_check()
            checkItems.value = data
            //checkItems.postValue(data)
            _isDatabase_check = true
            setLoading(false)
        }
    }

    private val check_searchItem = MutableStateFlow<SearchItem?>(null)
    val checkSearchItem: StateFlow<SearchItem?> = check_searchItem.asStateFlow()

    fun setCheckSearchItem(set_searchItem: SearchItem?){
        if (set_searchItem!=null){
            check_searchItem.value = set_searchItem
        }else{
            check_searchItem.value = null
        }

    }

    fun setDetailSearchItem(set_searchItem: SearchItem?,isMyData: Boolean?){
        if (set_searchItem!=null){
            if (isMyData?:false){
                searchItemsMy.value = searchItemsMy.value.map { item ->
                    if (item.push == set_searchItem.push) set_searchItem else item
                }
            }else{
                searchItems.value = searchItems.value.map { item ->
                    if (item.push == set_searchItem.push) set_searchItem else item
                }
            }
        }

    }


    private suspend fun fetchDataFromFirebase_check(): List<SearchItem> {
        // 실제 Firebase 데이터를 가져오는 코드

        return withContext(Dispatchers.IO) {
            Log.d("firebase", "firebase")
            val dataList = mutableListOf<SearchItem>()
            val databaseReference = Firebase.database.getReference("info") // Firebase 경로

            // Firebase 데이터를 한 번만 가져옴
            val snapshot = databaseReference.get().await() // KTX 확장을 사용해 비동기 작업을 동기적으로 처리
            for (child in snapshot.children) {
                if (dataList.size >= 10) break
                val item = child.getValue(SearchItem::class.java) // Firebase 데이터를 SearchItem으로 매핑

                if (item?.id != null) {
                    val databaseReference2 = Firebase.database.getReference("id")
                        .child(item.id).child("name") // name 경로

                    val nameValue = databaseReference2.get().await().getValue(String::class.java) ?: "익명" // 값이 없으면 "익명" 기본값 사용

                    var excludeItem = false

                    if (AuthRepository().getCurrentUser() != null) {
                        val user = AuthRepository().getCurrentUser()?.email?.substringBefore("@")
                        val (isFound, pushKey) = checkDatabase(user, child.key.toString())

                        if (isFound) {
                            excludeItem = true
                        }
                    }

                    if (!excludeItem) {
                        val updatedItem = item.copy(
                            name = nameValue,
                        )
                        dataList.add(updatedItem)
                    }
                }

            }
            dataList.reverse()
            dataList // 가져온 데이터를 반환
        }
    }

    private val searchItemsMy = MutableStateFlow<List<SearchItem>>(emptyList())
    val itemsMy: StateFlow<List<SearchItem>> = searchItemsMy.asStateFlow()

    private var _isDatabase_my = false
    val isDatabase_my: Boolean = _isDatabase_my

    fun yetDatabase_my() {
        _isDatabase_my = false
    }

    fun loadData_my(id: String,hashMap:HashMap<String,String>) {
        if (_isDatabase_my) return

        setLoading(true)

        if (hashMap.isEmpty()) {
            searchItemsMy.value = emptyList()

            _isDatabase_my = true

            setLoading(false)
            return
        }

        viewModelScope.launch {
            val data = fetchDataFromFirebase_my(id,hashMap)
            //searchItemsMy.postValue(data)
            searchItemsMy.value = data
            _isDatabase_my = true

            setLoading(false)
        }
    }

    private suspend fun fetchDataFromFirebase_my(id:String,hashMap: HashMap<String,String>): List<SearchItem> {
        // 실제 Firebase 데이터를 가져오는 코드

        return withContext(Dispatchers.IO) {
            Log.d("firebase", "firebase")
            val dataList = mutableListOf<SearchItem>()

            for(info_push in hashMap){
                val databaseReference2 = Firebase.database.getReference("info").child(info_push.value)

                val snapshot = databaseReference2.get().await()
                if (snapshot.exists()) {
                    val item = snapshot.getValue(SearchItem::class.java) // Firebase 데이터를 SearchItem으로 매핑

                    if (item != null) {
                        val databaseReference3 = Firebase.database.getReference("id")
                            .child(item.id!!).child("name") // name 경로

                        val nameValue = databaseReference3.get().await().getValue(String::class.java) ?: "익명" // 값이 없으면 "익명" 기본값 사용

                        var tCheck = false
                        var fCheck = false

                        val (isFound, pushKey) = checkDatabase(id, info_push.value)

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
                }else{
                    val deletedItem = SearchItem(
                        push = info_push.value,
                        id = "",
                        name = "",
                        profile = "",
                        date = "",
                        title = "삭제된 정보입니다.",
                        info = "",
                        t_num = 0,
                        f_num = 0,
                        t_check = false,
                        f_check = false,
                        delete = true
                    )
                    dataList.add(deletedItem)
                }
            }

            dataList.reverse()
            dataList // 가져온 데이터를 반환
        }
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

    suspend fun deleteMyCheck(userId: String, push: String, onError: (String) -> Unit, onSuccess: () -> Unit){
        val db = FirebaseDatabase.getInstance().reference

        val infoRef = db.child("id").child(userId).child("check")

        try {
            val snapshot = infoRef.get().await()
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val getpush = child.key
                    val infoPushValue = child.child("info_push").getValue(String::class.java)

                    if (infoPushValue == push) { // push 값이 일치하는 경우
                        val checkRef = db.child("id").child(userId).child("check").child(getpush!!)
                        checkRef.removeValue().await() // 해당 데이터만 삭제
                        break // 첫 번째 발견된 항목만 삭제 후 종료
                    }
                }
            }

            searchItemsMy.value = searchItemsMy.value.filterNot { it.push == push }
            onSuccess()
        } catch (e: Exception) {
            val errorMessage = "데이터 삭제 실패: ${e.message}"
            onError(errorMessage)  // 실패하면 onError 호출
            FirebaseCrashlytics.getInstance().log("데이터 삭제 실패: ${e.message}")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    suspend fun deleteMyInfo(userId: String, push: String, onError: (String) -> Unit, onSuccess: () -> Unit) {
        val db = FirebaseDatabase.getInstance().reference
        val infoRef = db.child("id").child(userId).child("info")

        try {
            db.child("info").child(push).removeValue().await()

            val snapshot = infoRef.get().await()
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    if (child.value == push) { // push 값이 일치하는 경우
                        infoRef.child(child.key!!).removeValue().await() // 삭제 후 대기
                        break
                    }
                }
            }

            searchItemsMy.value = searchItemsMy.value.filterNot { it.push == push }
            onSuccess()
        } catch (e: Exception) {
            val errorMessage = "데이터 삭제 실패: ${e.message}"
            onError(errorMessage)  // 실패하면 onError 호출
            FirebaseCrashlytics.getInstance().log("데이터 삭제 실패: ${e.message}")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun declareInfo(userId: String, push: String, onError: (String) -> Unit, onSuccess: () -> Unit) {
        val db = FirebaseDatabase.getInstance().reference
        val infoRef = db.child("info").child(push).child("declare")

        try {
            if (declareDatabase(userId, push)){
                onError("이미 신고한 정보입니다.")
                return
            }
            declare_runTransactionAsTask(userId, infoRef).addOnCompleteListener { overallTask ->
                if (overallTask.isSuccessful) {
                    val newDeclareList = overallTask.result ?: ArrayList()

                    val updatedList = searchItems.value?.map { searchItem ->
                        if (searchItem.push == push) {
                            // 아이템이 찾았으면 그 값만 업데이트
                            searchItem.copy(declare = newDeclareList)
                        } else {
                            // 그렇지 않으면 그대로 유지
                            searchItem
                        }
                    }
                    // 업데이트된 리스트를 searchItems에 반영
                    searchItems.value = updatedList!!
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

    suspend fun declareDatabase(id: String, info_push: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val db = FirebaseDatabase.getInstance().reference
            db.child("info").child(info_push).child("declare")
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
                        continuation.resumeWithException(Exception("info_push 검색 오류"))
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
    // SearchItem을 업데이트하는 함수
    fun updateMyItem(push: String) {
        searchItemsMy.value = searchItemsMy.value.filterNot { it.push == push }
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
    val delete: Boolean? = false,
    val declare: ArrayList<String>? = ArrayList(),
    val reple: HashMap<String, String>? = HashMap()
)
