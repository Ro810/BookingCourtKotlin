package com.example.bookingcourt.domain.model

/**
 * Thông tin ngân hàng của chủ sân
 * Dùng để khách hàng chuyển khoản thanh toán
 */
data class BankInfo(
    val bankName: String,           // Tên ngân hàng (VD: Vietcombank, Techcombank, ...)
    val accountNumber: String,      // Số tài khoản
    val accountHolderName: String,  // Tên chủ tài khoản
)

/**
 * Request để đăng ký trở thành chủ sân
 */
data class BecomeOwnerRequest(
    val bankName: String,
    val accountNumber: String,
    val accountHolderName: String,
)
