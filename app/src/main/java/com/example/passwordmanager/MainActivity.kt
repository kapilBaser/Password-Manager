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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.passwordmanager.room.RoomDatabaseObject
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

                    var checkIfISEmpty by remember {
                        mutableStateOf(false)
                    }
                    val roomDB = RoomDatabaseObject.getDatabase(this)
                    val entityDao = roomDB.entityDao()
                    val viewModel = ViewModelProvider(this, MainViewModelFactory(entityDao)).get(MainViewModel::class.java)
                    val accountName = viewModel.accountName.value
                    val userName = viewModel.usernameOrEmail.value
                    val password = viewModel.password.value
                    val passwordString = viewModel.passwordCheckString.value
                    var showPassword by remember {
                        mutableStateOf(false)
                    }
                    var isInEdit = viewModel.isInEdit.value
                    var isDetailSheetOpen by remember {
                        mutableStateOf(false)
                    }
                    val allEntries = viewModel.allEntries.value
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        if(biometricResult != 1){
                            Column(modifier = Modifier
                                .fillMaxSize()
                                .zIndex(2f)
                                .background(Color.White),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(text = "Tap to unlock",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = primaryColor,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.clickable {
                                        prompt.showBiometricPrompt()
                                    })
                            }
                        }
                        Scaffold(
                            floatingActionButton = {
                                FloatingActionButton(onClick = { isSheetOpen = true }, shape = RoundedCornerShape(16.dp), containerColor = primaryColor) {
                                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.White)
                                }
                            }
                        ) {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .background(surfaceVarient)
                                .padding(it)) {

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    Column(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)) {
                                        Text(text = "Password Manager",
                                            style = MaterialTheme.typography.headlineSmall)
                                    }
                                    Divider(color = surfaceVarient40, modifier = Modifier.padding(bottom = 12.dp))

                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(allEntries){
                                            ListRow(it.accountName, it.password, onClick = {
                                                viewModel.getDetailOfItem(it.id)
                                                isDetailSheetOpen = true
                                            })
                                        }
                                    }
                                }

                                if(isDetailSheetOpen){
                                    ModalBottomSheet(onDismissRequest = { isDetailSheetOpen = false
                                        viewModel.refreshValues()
                                    },
                                        containerColor = surfaceVarient) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        ) {
                                            Text(
                                                "Account Details",
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    color = primaryColor,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                )
                                            )
                                            NameValuePair(name = "Account Type", value = accountName)
                                            NameValuePair(name = "Username/ Email", value = userName)
                                            Row(modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically) {
                                                NameValuePair(name = "Password", value = if(showPassword) password else{
                                                    var str = ""
                                                    repeat(password.length){
                                                        str += "*"
                                                    }
                                                    str
                                                })
                                                IconButton(onClick = {
                                                    showPassword = !showPassword
                                                }) {
                                                    Icon(imageVector = if(showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = null, tint = onSurfaceVarient)

                                                }
                                            }

                                            Row(modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)) {
                                                Button(
                                                    onClick = {
                                                        isDetailSheetOpen = false
                                                        isSheetOpen = true
                                                        viewModel.isInEdit.value = true
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = onSurfaceVarient)
                                                ) {
                                                    Text(text = "Edit")
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Button(
                                                    onClick = { viewModel.deleteRowByID(viewModel.rowId.value)
                                                        isDetailSheetOpen = false
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(240, 70, 70))
                                                ) {
                                                    Text(text = "Delete")
                                                }
                                            }
                                        }
                                    }
                                }


                                if(isSheetOpen){

                                    ModalBottomSheet(onDismissRequest = { isSheetOpen = false
                                        viewModel.isInEdit.value = false
                                        viewModel.refreshValues()
                                        showPassword = false
                                        viewModel.passwordCheckString.value = ""
                                        checkIfISEmpty = false
                                    },
                                        sheetState = sheetState,
                                        containerColor = surfaceVarient) {
                                        Column(modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)) {
                                            TextFieldDesign(
                                                value = accountName,
                                                onValueChange = {
                                                    viewModel.onAccountNameChange(it)
                                                },
                                                placeholder = "Account Name"
                                            )
                                            TextFieldDesign(
                                                value = userName,
                                                onValueChange = {
                                                    viewModel.onUserNameOrEmailChange(it)
                                                },
                                                placeholder = "Username/ Email"
                                            )
                                            TextFieldDesign(
                                                value = password,
                                                onValueChange = {
                                                    viewModel.onPasswordChange(it)
                                                },
                                                placeholder = "Password",
                                                visualTransformation = if(showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                                trailingIcon = {
                                                    Icon(imageVector = if(showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                        contentDescription = null,
                                                        modifier = Modifier.clickable {
                                                            showPassword = !showPassword
                                                        })
                                                },
                                            )
                                            Text(text = passwordString, style = MaterialTheme.typography.bodySmall )


                                            Spacer(modifier = Modifier.height(8.dp))
                                            if(checkIfISEmpty){
                                                Text(
                                                    text = if (accountName.isEmpty()) "Account name is required" else {
                                                        if (userName.isEmpty()) "Username is required" else{
                                                            if(password.isEmpty()){
                                                                "Password is required"
                                                            }else{
                                                                checkIfISEmpty = false
                                                                ""
                                                            }
                                                        }
                                                    },
                                                    color = Color(240, 70, 70),
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Button(onClick = {
                                                checkIfISEmpty = true
                                                if(userName.isNotEmpty() && accountName.isNotEmpty() && password.isNotEmpty()){
                                                    viewModel.AddNewAccount()
                                                    isSheetOpen = false
                                                    showPassword = false
                                                    viewModel.isInEdit.value = false
                                                    viewModel.passwordCheckString.value = ""
                                                    checkIfISEmpty = false
                                                }

                                            },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = onSurfaceVarient)) {
                                                Text(text = if(isInEdit)"Save" else "Add New Account")
                                            }
                                        }


                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        prompt.showBiometricPrompt()
    }
}

@Composable
fun NameValuePair(name: String,
                  value: String){
    Column(modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = surfaceVarient40
            ),
            )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),)
    }
}


@Composable
fun ListRow(accountName: String,
            password: String,
            onClick: ()->Unit){
    var str = ""
    repeat(password.length){
        str += "*"
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .background(Color.White, RoundedCornerShape(12.dp))
        .clip(RoundedCornerShape(12.dp))
        .clickable { onClick() }
        .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${ accountName }", style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = str.trim(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.CenterVertically))
        }
        Icon(imageVector = Icons.Rounded.ArrowForwardIos, contentDescription = null)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldDesign(value: String,
                    onValueChange: (String)->Unit,
                    placeholder: String,
                    visualTransformation: VisualTransformation = VisualTransformation.None,
                    trailingIcon: @Composable()(()->Unit)? = null){

    TextField(value = value,
        onValueChange = onValueChange
        , placeholder = { Text(
            text = placeholder
        )},
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .border(1.dp, surfaceVarient40, RoundedCornerShape(16.dp)),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            focusedPlaceholderColor = surfaceVarient40,
            unfocusedPlaceholderColor = surfaceVarient40,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        )
    )
}












