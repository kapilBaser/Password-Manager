package com.example.passwordmanager

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passwordmanager.ui.theme.PasswordManagerTheme

class MainActivity : AppCompatActivity() {
    val prompt by lazy {
        BiometricLogin(this)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasswordManagerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val biometricResult by prompt.promptResult.collectAsState(null)
                    val context = LocalContext.current
                    val sheetState = rememberModalBottomSheetState()
                    var isSheetOpen by remember {
                        mutableStateOf(false)
                    }
                    val enrollLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) {
                        println("Activity result $it")
                    }
                    LaunchedEffect(Unit) {
                        prompt.showBiometricPrompt()
                    }
                    LaunchedEffect(biometricResult) {
                        if(biometricResult == 9){
                            if(Build.VERSION.SDK_INT >= 30){
                                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                                }
                                enrollLauncher.launch(enrollIntent)
                            }else{
                                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                                startActivity(intent)
                            }
                        }
                    }
                    Scaffold(
                        floatingActionButton = {
                            FloatingActionButton(onClick = { isSheetOpen = true }, shape = RoundedCornerShape(16.dp)) {
                                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                            }
                        }
                    ) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(it)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {

                                }
                            }
                            if(isSheetOpen){

                                ModalBottomSheet(onDismissRequest = { isSheetOpen = false},
                                    sheetState = sheetState) {
                                    Text(text = "Hello")
                                    TextField(value = "", onValueChange = {}, placeholder = { Text(
                                        text = "UserName"
                                    )})

                                }
                            }
                        }

                    }
                }
            }
        }
    }
}











