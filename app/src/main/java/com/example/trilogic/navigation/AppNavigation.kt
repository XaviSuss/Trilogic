package com.example.trilogic.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import com.example.trilogic.model.Difficulty
import com.example.trilogic.model.db.AppDatabase
import com.example.trilogic.view.*
import com.example.trilogic.view.tictactoe.ScreenChoice
import com.example.trilogic.view.tictactoe.ScreenGame
import com.example.trilogic.viewmodel.*

@Composable
fun AppNavigation(
    onCloseApp: () -> Unit
) {
    val navController = rememberNavController()

    // Funciones de navegación (Helper functions)
    fun anarAChoice() = navController.navigate(AppScreens.Choice.route)
    fun anarAJoc(costat: String) = navController.navigate(AppScreens.Game.createRoute(costat))
    fun tornarAlInici() = navController.popBackStack(AppScreens.Login.route, false)

    NavHost(
        navController = navController,
        startDestination = AppScreens.Login.route
    ) {
        // 1. PANTALLA LOGIN
        composable(route = AppScreens.Login.route) {
            val vm: LoginViewModel = viewModel()
            val state by vm.uiState.collectAsState()
            val context = LocalContext.current
            val dao = AppDatabase.getDatabase(context).userDao()

            LaunchedEffect(Unit) {
                vm.navigationChannel.collect { ruta ->
                    navController.navigate(ruta)
                }
            }

            ScreenLogin(
                state = state,
                onUsernameChange = vm::onUsernameChange,
                onPasswordChange = vm::onPasswordChange,
                onRegisterClick = { vm.onRegisterClick(dao) },
                onLoginClick = { vm.onLoginClick(dao) },
                onCloseClick = onCloseApp
            )
        }

        // 2. PANTALLA WELCOME
        composable(
            route = AppScreens.Welcome.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val user = backStackEntry.arguments?.getString("username") ?: "User"
            ScreenWelcome(
                msgWelcome = "Hola, $user",
                onLogoutClick = { navController.popBackStack() },
                onCloseApp = onCloseApp,
                onStartGame = ::anarAChoice // Ahora va a elegir bando
            )
        }

        // 3. PANTALLA ELEGIR BANDO (X o O)
        composable(route = AppScreens.Choice.route) {
            val vm: TicTacToeViewModel = viewModel()
            ScreenChoice(
                onSideSelected = { lado -> anarAJoc(lado) },
                onDifficultySelected = { difficulty -> vm.setDifficulty(difficulty) }
            )
        }

        // 4. PANTALLA JUEGO (Ultimate Tic-Tac-Toe)
        composable(
            route = AppScreens.Game.route,
            arguments = listOf(navArgument("playerSide") { type = NavType.StringType })
        ) { _ ->
            ScreenGame(
                onBackClick = ::tornarAlInici
            )
        }
    }
}