package com.example.checkbook.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.serialization.Serializable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.rememberNavController
import com.example.checkbook.R
import com.example.checkbook.listview.SearchViewModel
import com.example.checkbook.mvi.MainViewModel
import com.example.checkbook.ui.navigation.search.SearchInfoRoute
import com.example.checkbook.ui.theme.CheckBookTheme
import com.example.checkbook.viewmodel.MyInfoViewModel

@Serializable
object SearchRoute

@Composable
fun SearchScreen(mainViewModel: MainViewModel, searchViewModel:SearchViewModel, myInfoViewModel: MyInfoViewModel, navController: NavController) {
    var userInput by remember {
        mutableStateOf(TextFieldValue())
    }
    val context = LocalContext.current
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
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp).clickable(onClick = {
                            if (userInput.text.isNotEmpty()) {
                                navController.navigate("$SearchInfoRoute/${userInput.text}")
                                mainViewModel.showDetail() // 디테일 화면 표시 상태 업데이트
                                searchViewModel.yetDatabase()
                            }else{
                                val message = "검색어를 입력하세요."
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                            }
                        })
                        
                    )},
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