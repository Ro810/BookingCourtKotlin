package com.example.bookingcourt.domain.model

/**
 * Thông tin ngân hàng của chủ sân
 * Dùng để khách hàng chuyển khoản thanh toán
 */
data class BankInfo(
    val bankName: String,           // Tên ngân hàng (VD: Vietcombank, Techcombank, ...)
    val bankAccountNumber: String,  // Số tài khoản (đổi từ accountNumber để match API)
    val bankAccountName: String,    // Tên chủ tài khoản (đổi từ accountHolderName để match API)
)

/**
 * Request để đăng ký trở thành chủ sân
 */
data class BecomeOwnerRequest(
    val bankName: String,
    val accountNumber: String,
    val accountHolderName: String,
)
