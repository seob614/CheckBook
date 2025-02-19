package com.example.checkbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.checkbook.data.TOP_LEVEL_DESTINATIONS
import com.example.checkbook.data.TopLevelRoute
import com.example.checkbook.ui.navigation.CheckRoute
import com.example.checkbook.ui.navigation.CheckScreen
import com.example.checkbook.ui.navigation.ExchangeRoute
import com.example.checkbook.ui.navigation.ExchangeScreen
import com.example.checkbook.ui.navigation.SearchRoute
import com.example.checkbook.ui.navigation.SearchScreen
import com.example.checkbook.ui.navigation.UserRoute
import com.example.checkbook.ui.navigation.UserScreen
import com.example.checkbook.ui.navigation.componets.BottomNavigationActions
import com.example.checkbook.ui.theme.CheckBookTheme
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.checkbook.viewmodel.MyInfoViewModel
import com.example.checkbook.listview.SearchViewModel
import com.example.checkbook.ui.navigation.search.SearchInfoRoute
import com.example.checkbook.ui.navigation.search.SearchInfoScreen
import com.example.checkbook.mvi.MainViewModel
import com.example.checkbook.ui.navigation.auth.CodeRoute
import com.example.checkbook.ui.navigation.auth.CodeScreen
import com.example.checkbook.ui.navigation.auth.RegisterRoute
import com.example.checkbook.ui.navigation.auth.RegisterScreen
import com.example.checkbook.ui.navigation.auth.SignInRoute
import com.example.checkbook.ui.navigation.auth.SignInScreen
import com.example.checkbook.ui.navigation.create.CreateInfoRoute
import com.example.checkbook.ui.navigation.create.CreateInfoScreen
import com.example.checkbook.ui.navigation.mylist.MyInfoRoute
import com.example.checkbook.ui.navigation.mylist.MyInfoScreen
import com.example.checkbook.ui.navigation.search.DetailInfoRoute
import com.example.checkbook.ui.navigation.search.DetailInfoScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            CheckBookTheme {
                val mainNavController = rememberNavController() // 바텀탭용
                val mainViewModel: MainViewModel = hiltViewModel()
                val searchViewModel: SearchViewModel = hiltViewModel()
                val myInfoViewModel: MyInfoViewModel = hiltViewModel()
                val showDetailNavHost by mainViewModel.showDetailNavHost.collectAsState()



                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    Scaffold(
                        modifier = Modifier
                            .statusBarsPadding()
                            .fillMaxSize(),

                        bottomBar = {
                            if (!showDetailNavHost) {
                                val navigationActions =
                                    remember(mainNavController) {
                                        BottomNavigationActions(
                                            mainNavController
                                        )
                                    }
                                val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination
                                BottomNavigationBar(
                                    currentDestination = currentDestination,
                                    navigateToTopLevelDestination = navigationActions::navigateTo
                                )
                            }
                        })
                    { innerPadding ->
                        // MainNavHost는 바텀탭을 포함하는 기본 네비게이션
                        MainBottomNavHost(
                            mainViewModel = mainViewModel,
                            searchViewModel = searchViewModel,
                            myInfoViewModel = myInfoViewModel,
                            navController = mainNavController,
                            modifier = Modifier.padding(innerPadding),
                        )
                        /*화면 겹칠때 이미지위 버튼
                        Box(Modifier.padding(innerPadding)) {

                        }
                         */
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBackButton(
    title: String,
    onBackPressed: () -> Unit
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.main_color), // 배경색
            titleContentColor = MaterialTheme.colorScheme.onPrimary, // 타이틀 텍스트 색상
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary // 네비게이션 아이콘 색상
        )
    )
}

@Composable
fun BottomNavigationBar(
    currentDestination: NavDestination?,
    navigateToTopLevelDestination: (TopLevelRoute<*>) -> Unit,
    ){
    // 내비게이션 바를 피하는 패딩값 계산
    val paddingValues = WindowInsets.navigationBars.asPaddingValues()
    BottomNavigation(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues),
        backgroundColor = Color.White,
    )
    {
        TOP_LEVEL_DESTINATIONS.forEach { topLevelRoute ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        modifier=Modifier.padding(6.dp),
                        painter = painterResource(
                            id = if (currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true)
                                topLevelRoute.selectedIcon
                            else
                                topLevelRoute.unselectedIcon,
                        ),
                        contentDescription = stringResource(topLevelRoute.titleTextId),
                        tint = Color.Unspecified // 아이콘에 틴트를 적용하지 않음
                    )
                },
                label = {
                    Text(
                        text=stringResource(topLevelRoute.titleTextId),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true)
                            colorResource(id = R.color.main_color)
                        else
                            colorResource(id = R.color.gray),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                },
                selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
                onClick ={navigateToTopLevelDestination(topLevelRoute)},
                modifier =Modifier.background(color = Color.White),
                alwaysShowLabel = true

            )

        }
    }

}
@Composable
private fun MainBottomNavHost(
    mainViewModel: MainViewModel,
    searchViewModel : SearchViewModel,
    myInfoViewModel: MyInfoViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = SearchRoute,
    ) {
        composable<SearchRoute> {
            SearchScreen(mainViewModel, searchViewModel, myInfoViewModel, navController)
        }
        composable<CheckRoute> {
            CheckScreen()
        }
        composable<ExchangeRoute> {
            ExchangeScreen()
        }
        composable<UserRoute> {
            UserScreen(mainViewModel, searchViewModel, myInfoViewModel, navController)
        }
        //타 Sceen
        composable("$SearchInfoRoute/{data_key}",
            arguments = listOf(navArgument("data_key") { type = NavType.StringType })) { backStackEntry ->
            SearchInfoScreen(
                mainViewModel,
                searchViewModel,
                myInfoViewModel,
                navController,
                backStackEntry
            )
        }
        composable("$DetailInfoRoute/{data_key}/{isMyData}/{push}",
            arguments = listOf(
                navArgument("data_key") { type = NavType.StringType },
                navArgument("isMyData") { type = NavType.BoolType },
                navArgument("push") { type = NavType.StringType })) { backStackEntry ->
            DetailInfoScreen(
                searchViewModel,
                myInfoViewModel,
                navController,
                backStackEntry
            )
        }
        composable("$SignInRoute") {
            SignInScreen(mainViewModel, myInfoViewModel , navController)
        }
        composable("$RegisterRoute") {
            RegisterScreen(navController)
        }
        composable("$CodeRoute/{verificationId}/{email}/{password}") { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId")
            val email = backStackEntry.arguments?.getString("email")
            val password = backStackEntry.arguments?.getString("password")

            // 필요한 UI를 띄우거나, CodeScreen 등을 호출
            CodeScreen(mainViewModel, navController, verificationId, email, password)
        }
        composable("$CreateInfoRoute") {
            CreateInfoScreen(mainViewModel, myInfoViewModel , navController)
        }
        composable("$MyInfoRoute") {
            MyInfoScreen(mainViewModel, myInfoViewModel, searchViewModel , navController)
        }
    }
}
@Composable
fun SimpleText(name: String){
    Text(text = "$name", color = Color.Black)
}

@Composable
fun NameChangeDialog(
    myInfoViewModel: MyInfoViewModel,
    userId: String,
    currentName: String,
    onDismiss: () -> Unit,
) {
    var newName by remember { mutableStateOf(currentName) }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        onDismissRequest = onDismiss,
        title = { Text(text = "이름 변경") },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("새로운 이름") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank()) {
                        isLoading = true
                        myInfoViewModel.updateUserName(userId, newName) { success ->
                            isLoading = false
                            if (success) {
                                onDismiss() // 다이얼로그 닫기
                            }
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text("변경")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
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