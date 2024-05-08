package com.example.passwordmanager

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val accountName = mutableStateOf("")
    val usernameOrEmail = mutableStateOf("")
    val password = mutableStateOf("")

    fun onAccountNameChange(value: String){
        accountName.value = value
    }

    fun onUserNameOrEmailChange(value: String){
        usernameOrEmail.value = value
    }

    fun onPasswordChange(value: String){
        password.value = value
    }

}