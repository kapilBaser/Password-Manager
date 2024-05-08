package com.example.passwordmanager

import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricLogin(
    val activity: AppCompatActivity
) {
    val promptResultChannel = Channel<Int>()
    val promptResult = promptResultChannel.receiveAsFlow()

    fun showBiometricPrompt() {

        val biometricManager = BiometricManager.from(activity)

        val authenticators = if (Build.VERSION.SDK_INT >= 30) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else BiometricManager.Authenticators.BIOMETRIC_WEAK

        val promptBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setDescription("Login to see passwords")
            .setAllowedAuthenticators(authenticators)

        if(Build.VERSION.SDK_INT < 30){
            promptBuilder.setNegativeButtonText("Cancel")
        }

        when(biometricManager.canAuthenticate(authenticators)){
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                promptResultChannel.trySend(404)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                promptResultChannel.trySend(9)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                promptResultChannel.trySend(405)
                return
            }
            else -> Unit
        }
        val prompt = BiometricPrompt(
            activity,
            object: BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    promptResultChannel.trySend(0)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    promptResultChannel.trySend(1)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    promptResultChannel.trySend(-1)
                }
            }
        )
        prompt.authenticate(promptBuilder.build())
    }
}


