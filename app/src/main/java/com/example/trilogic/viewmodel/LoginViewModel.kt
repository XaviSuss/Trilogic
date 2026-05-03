package com.example.trilogic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trilogic.model.User
import com.example.trilogic.model.UserRepository
import com.example.trilogic.model.db.UserDao
import com.example.trilogic.navigation.AppScreens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val message: String = "",
    val errorMsg: String = "",
    val isLoading: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationChannel = Channel<String>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    fun onUsernameChange(input: String) {
        _uiState.value = _uiState.value.copy(username = input, message = "", errorMsg = "")
    }

    fun onPasswordChange(input: String) {
        _uiState.value = _uiState.value.copy(password = input, message = "", errorMsg = "")
    }

    fun onRegisterClick(dao: UserDao) {
        val current = _uiState.value
        if (current.username.isNotBlank() && current.password.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                val isSuccess = UserRepository.addUser(User(current.username, current.password), dao)
                withContext(Dispatchers.Main) {
                    if (isSuccess) {
                        _uiState.value = current.copy(message = "Usuari registrat correctament !!", username = "", password = "", errorMsg = "")
                    } else {
                        _uiState.value = current.copy(errorMsg = "ERROR: L'usuari ja existeix !!", message = "")
                    }
                }
            }
        }
    }

    fun onLoginClick(dao: UserDao) {
        val current = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            val storedUser = UserRepository.getUser(current.username, dao)
            withContext(Dispatchers.Main) {
                if (storedUser == null) {
                    _uiState.value = current.copy(errorMsg = "ERROR: L'usuari no existeix !!", message = "")
                } else {
                    if (storedUser.password == current.password) {
                        _navigationChannel.send(AppScreens.Welcome.createRoute(current.username))
                        _uiState.value = LoginUiState()
                    } else {
                        _uiState.value = current.copy(message = "", errorMsg = "ERROR: Credencials invàlides !!")
                    }
                }
            }
        }
    }
}
