package com.example.bookingcourt.presentation.payment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.remote.api.BookingApi
import com.example.bookingcourt.data.remote.dto.CreateBookingResponseDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val bookingApi: BookingApi
) : ViewModel() {

    private val _uploadState = MutableStateFlow<Resource<CreateBookingResponseDto>?>(null)
    val uploadState: StateFlow<Resource<CreateBookingResponseDto>?> = _uploadState.asStateFlow()

    private val _confirmState = MutableStateFlow<Resource<CreateBookingResponseDto>?>(null)
    val confirmState: StateFlow<Resource<CreateBookingResponseDto>?> = _confirmState.asStateFlow()

    fun uploadPaymentProof(bookingId: Long, imageFile: File) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading()
            try {
                val requestFile: RequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

                val response = bookingApi.uploadPaymentProof(bookingId, body)
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        _uploadState.value = Resource.Success(data)
                    } else {
                        _uploadState.value = Resource.Error("Upload thành công nhưng không có dữ liệu trả về")
                    }
                } else {
                    _uploadState.value = Resource.Error("Lỗi upload: ${response.code()}")
                }
            } catch (e: Exception) {
                _uploadState.value = Resource.Error(e.message ?: "Đã xảy ra lỗi khi upload")
            }
        }
    }

    fun confirmPayment(bookingId: Long, paymentProofUrl: String) {
        viewModelScope.launch {
            _confirmState.value = Resource.Loading()
            try {
                val body = mapOf("paymentProofUrl" to paymentProofUrl)
                val response = bookingApi.confirmPayment(bookingId, body)
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        _confirmState.value = Resource.Success(data)
                    } else {
                        _confirmState.value = Resource.Error("Xác nhận thành công nhưng không có dữ liệu trả về")
                    }
                } else {
                    _confirmState.value = Resource.Error("Lỗi xác nhận: ${response.code()}")
                }
            } catch (e: Exception) {
                _confirmState.value = Resource.Error(e.message ?: "Đã xảy ra lỗi khi xác nhận")
            }
        }
    }

    fun reset() {
        _uploadState.value = null
        _confirmState.value = null
    }
}
