package com.example.trilogic.navigation

sealed class AppScreens(val route: String) {
    data object Login : AppScreens("login_screen")
    data object Welcome : AppScreens("welcome_screen/{username}") {
        fun createRoute(username: String) = "welcome_screen/$username"
    }
    data object Choice : AppScreens("choice_screen") // Nueva: Elegir X o O
    data object Game : AppScreens("game_screen/{playerSide}") { // Nueva: Juego
        fun createRoute(side: String) = "game_screen/$side"
    }
}