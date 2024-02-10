package com.example.biometricauthentication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.biometricauthentication.ui.theme.BiometricAuthenticationTheme
import com.example.biometricauthentication.ui.theme.checkExistence
import com.example.biometricauthentication.ui.theme.cipher
import com.example.biometricauthentication.ui.theme.generateSecretKey
import com.example.biometricauthentication.ui.theme.sdkInt
import com.example.biometricauthentication.ui.theme.showToast

val LocalActivity = compositionLocalOf<FragmentActivity> { error("error") }

class MainActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricAuthenticationTheme {
                CompositionLocalProvider(LocalActivity provides this) {
                    BiometricAuth()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun BiometricAuth() {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val resultCode = remember { mutableStateOf(Int.MIN_VALUE) }
    val executors = ContextCompat.getMainExecutor(context)
    val launcherIntent =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { result ->
                when (result.resultCode) {
                    1 -> {
                        context.showToast("Enrollment Done !!")
                        resultCode.value = 1
                    }

                    2 -> {
                        context.showToast("Rejected")
                    }

                    else -> {
                        context.showToast("User Cancel the Biometric")
                    }
                }

            })
    val biometricManager = BiometricManager.from(context)

    LaunchedEffect(key1 = resultCode.value, block = {
        biometricManager.checkExistence(onSuccess = {
            val biometricPromptInfo = BiometricPrompt.PromptInfo
                .Builder()
                .setTitle("Authenticate")
                .setSubtitle("Authenticate Subtitle")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(it)
                .build()

            val biometricPrompt = BiometricPrompt(
                activity,
                executors,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        context.showToast("Failed")
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        context.showToast("success")
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        context.showToast("error")
                    }

                })
            val secretKey = generateSecretKey()
            val cipher = cipher(secretKey)
            val cryptObject = BiometricPrompt.CryptoObject(cipher)
            biometricPrompt.authenticate(biometricPromptInfo, cryptObject)

        },
            openSettings = {
                sdkInt(aboveVersion9 = {
                    val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BiometricManager.Authenticators.BIOMETRIC_STRONG
                        )
                    }
                    launcherIntent.launch(intent)
                }, belowVersion10 = {
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    activity.startActivity(intent)
                })
            },
            onError = {
                context.showToast(it)
            })
    })

}