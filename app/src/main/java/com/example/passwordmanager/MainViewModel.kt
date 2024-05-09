package com.example.passwordmanager

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passwordmanager.room.AccountPassword
import com.example.passwordmanager.room.DaoInterface
import com.example.passwordmanager.room.Keys
//import com.example.passwordmanager.room.SpecKey
import kotlinx.coroutines.launch
import java.lang.Exception
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class MainViewModel(private val entityDao: DaoInterface, private val secretIv: String = "67876vbjhgvbjhgf"): ViewModel() {
    val rowId = mutableStateOf(-1L)

    val accountName = mutableStateOf("")
    val usernameOrEmail = mutableStateOf("")
    val password = mutableStateOf("")
    val allEntries = mutableStateOf<List<AccountPassword>>(emptyList())
    var isInEdit = mutableStateOf(false)
    val secretKey: String = ""
    var ivValue: ByteArray? = null
    var passwordCheckString = mutableStateOf("")

    fun onAccountNameChange(value: String){
        accountName.value = value
    }

    fun onUserNameOrEmailChange(value: String){
        usernameOrEmail.value = value
    }

    fun onPasswordChange(value: String){
        password.value = value
        passwordCheckString.value = passwordCheck()
    }
    init {
        getAllRows()
    }

    fun AddNewAccount(){
        Log.d("837dfsb", "${accountName.value} ${usernameOrEmail.value} ${password.value}")
        viewModelScope.launch {
            entityDao.upsertRow(AccountPassword(
                id = if(rowId.value != -1L) rowId.value else 0L,
                accountName = accountName.value,
                username = usernameOrEmail.value,
                password = encryptString(password.value)))
            getAllRows()
            refreshValues()
        }
    }
    fun getAllRows(){
        viewModelScope.launch {
            var rows = entityDao.getAllRows()
            rows = rows?.onEach {
                val key = entityDao.getKeyByID(it.id)
                it.password = decryptString(it.password, key)
            }
            allEntries.value = rows ?: emptyList()
        }
    }
    fun getDetailOfItem(id: Long){
        viewModelScope.launch {
            val row = entityDao.getRowByID(id)
            val key = entityDao.getKeyByID(id)
            if(row!=null){
                rowId.value = id
                accountName.value = row.accountName
                usernameOrEmail.value = row.username
                password.value = decryptString(row.password, key)
            }
        }
    }
    fun deleteRowByID(id: Long){
        viewModelScope.launch {
            entityDao.deleteRowByID(id)
            getAllRows()
        }
    }
    fun refreshValues(){
        rowId.value = -1L
        accountName.value = ""
        usernameOrEmail.value = ""
        password.value = ""

    }

    fun passwordCheck(): String{
        val capitalLetterRegex = Regex("[A-Z]")
        val smallLetterRegex = Regex("[a-z]")
        val digitRegex = Regex("[0-9]")

        val hasCapitalLetter = capitalLetterRegex.containsMatchIn(password.value)
        val hasSmallLetter = smallLetterRegex.containsMatchIn(password.value)
        val hasDigit = digitRegex.containsMatchIn(password.value)

        val isLengthValid = password.value.length >= 8

        val str = when{
            hasDigit && hasSmallLetter && hasCapitalLetter && isLengthValid -> "very strong"
            hasDigit && hasSmallLetter && hasCapitalLetter ||
            hasSmallLetter && hasCapitalLetter && isLengthValid ||
            hasDigit && hasSmallLetter && isLengthValid ||
            hasDigit && hasCapitalLetter && isLengthValid -> "strong"
            hasDigit && hasSmallLetter || hasCapitalLetter && isLengthValid || hasSmallLetter && hasCapitalLetter || hasDigit && isLengthValid -> "weak"
            else -> "very weak"

        }

        return  str
    }



    fun decryptPassword(value: String): String{
//        return decryptString(value)
        return ""
    }

    fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }
    fun generateIVKey(): ByteArray{
        var iv = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)
        return iv
    }


    private fun encryptString(value: String): String{

        var cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val secretKey = generateAESKey().encoded
        val iv = generateIVKey()
        val secretKeySpec = SecretKeySpec(secretKey, "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val plainText = value.toByteArray(Charsets.UTF_8)
        var encryptBytes = cipher.doFinal(plainText)
        viewModelScope.launch {

            entityDao.insertKey(Keys(id = if(rowId.value != -1L) rowId.value else 0, specKey = Base64.encodeToString(secretKey, Base64.DEFAULT), iv = Base64.encodeToString(iv, Base64.DEFAULT)))
        }
        ivValue = cipher.iv
        return Base64.encodeToString(encryptBytes, Base64.DEFAULT)
    }

    private fun decryptString(value: String, key: Keys): String{
        var cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val secretKey = Base64.decode(key.specKey, Base64.DEFAULT)
        val iv = Base64.decode(key.iv, Base64.DEFAULT)
        val secretKeySpec = SecretKeySpec(secretKey, "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val plainText = Base64.decode(value, Base64.DEFAULT)
        var encryptBytes = cipher.doFinal(plainText)
        return String(encryptBytes, Charsets.UTF_8)

    }


}

class MainViewModelFactory(private val entityDao: DaoInterface): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(entityDao) as T
    }
}