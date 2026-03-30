package com.example.detectionimgmobile.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.detectionimgmobile.data.model.BaseResponse
import com.example.detectionimgmobile.data.model.LoginData
import com.example.detectionimgmobile.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val data: LoginData) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(nip: String, pass: String, role: String) {
        if (nip.isEmpty() || pass.isEmpty()) {
            _uiState.value = LoginUiState.Error("NIP dan Password tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = repository.login(nip, pass, role)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        _uiState.value = LoginUiState.Success(body.data)
                    } else {
                        _uiState.value = LoginUiState.Error(body?.message ?: "Login gagal")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val jsonObject = JSONObject(errorBody ?: "")
                        jsonObject.getString("message")
                    } catch (e: Exception) {
                        "Terjadi kesalahan: ${response.code()}"
                    }
                    _uiState.value = LoginUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Koneksi gagal: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
