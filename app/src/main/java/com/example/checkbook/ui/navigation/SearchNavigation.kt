package com.example.checkbook.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.serialization.Serializable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.rememberNavController
import com.example.checkbook.R
import com.example.checkbook.listview.SearchViewModel
import com.example.checkbook.location.getCurrentLocationAsString
import com.example.checkbook.mvi.MainViewModel
import com.example.checkbook.mvi.ScreenIntent
import com.example.checkbook.ui.navigation.search.SearchInfoRoute
import com.example.checkbook.ui.theme.CheckBookTheme
import com.example.checkbook.viewmodel.MyInfoViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@Serializable
object SearchRoute

@Composable
fun SearchScreen(mainViewModel: MainViewModel, searchViewModel:SearchViewModel, myInfoViewModel: MyInfoViewModel, navController: NavController) {
    var userInput by remember {
        mutableStateOf(TextFieldValue())
    }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()

    // 권한 요청 런처 설정
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 승인되면 위치 가져오기
            coroutineScope.launch {
                Toast.makeText(context, getCurrentLocationAsString(context, fusedLocationClient), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            Modifier.fillMaxWidth().height(85.dp).padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxSize(),
                value = userInput,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.search_on),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )},
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.gps),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable(onClick = {
                                    when {
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED -> {
                                            coroutineScope.launch {
                                                val location = getCurrentLocationAsString(context, fusedLocationClient)
                                                if (location.isNotEmpty()) {
                                                    mainViewModel.onIntent(ScreenIntent.NavigateToSearchInfo(location))
                                                    mainViewModel.showDetail() // 디테일 화면 표시 상태 업데이트
                                                    searchViewModel.yetDatabase()
                                                } else {
                                                    val message = "위치 검색을 실패했습니다."
                                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }

                                        else -> {
                                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    }
                                })
                        )

                        Spacer(modifier = Modifier.width(8.dp)) // 아이콘 간 간격 조정

                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable(onClick = {
                                    if (userInput.text.isNotEmpty()) {
                                        mainViewModel.onIntent(ScreenIntent.NavigateToSearchInfo(userInput.text))
                                        mainViewModel.showDetail() // 디테일 화면 표시 상태 업데이트
                                        searchViewModel.yetDatabase()
                                    } else {
                                        val message = "검색어를 입력하세요."
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                })
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }},
                shape = RoundedCornerShape(20.dp),
                onValueChange = { userInput = it },
                label = { Text("검색어 입력") },
            )
        }

    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    val navController = rememberNavController()
    val mainViewModel = MainViewModel()
    val searchViewModel = SearchViewModel()
    val myInfoViewModel = MyInfoViewModel()
    CheckBookTheme{
        SearchScreen(mainViewModel, searchViewModel, myInfoViewModel, navController)
    }
}