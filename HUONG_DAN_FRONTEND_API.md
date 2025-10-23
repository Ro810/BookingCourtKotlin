# 🚀 HƯỚNG DẪN FRONTEND SỬ DỤNG API - HỆ THỐNG QUẢN LÝ ĐẶT SÂN

## 📋 MỤC LỤC
1. [Cấu hình DevTunnel](#cấu-hình-devtunnel)
2. [Cấu trúc API Response](#cấu-trúc-api-response)
3. [Authentication & Authorization](#authentication--authorization)
4. [API Endpoints](#api-endpoints)
   - [Auth APIs](#1-auth-apis)
   - [User APIs](#2-user-apis)
   - [Venues APIs](#3-venues-apis)
   - [Court APIs](#4-court-apis)
   - [Price Rule APIs](#5-price-rule-apis)
   - [Booking APIs](#6-booking-apis)
   - [Notification APIs](#7-notification-apis)
5. [Luồng nghiệp vụ chính](#luồng-nghiệp-vụ-chính)
6. [Mã ví dụ Frontend](#mã-ví-dụ-frontend)

---

## 🔧 CÁC BƯỚC CẤU HÌNH DEVTUNNEL

### Bước 1: Cài đặt DevTunnel
```bash
# Tải và cài đặt DevTunnel CLI
# Trên macOS (Homebrew)
brew install --cask devtunnel

# Hoặc tải trực tiếp từ: https://aka.ms/devtunnels/download
```

### Bước 2: Đăng nhập DevTunnel
```bash
# Đăng nhập bằng tài khoản Microsoft/GitHub
devtunnel user login
```

### Bước 3: Tạo tunnel cho Backend
```bash
# Tạo tunnel persistent (giữ nguyên URL)
devtunnel create --allow-anonymous

# Ghi nhớ tunnel-id được trả về (ví dụ: abc123xyz)
```

### Bước 4: Khởi động Backend Spring Boot
```bash
# Di chuyển đến thư mục project
cd /Users/phammanh/Documents/JavaProject/QuanLyDatSan

# Chạy backend (port 8080)
./mvnw spring-boot:run
```

### Bước 5: Expose Backend qua DevTunnel
```bash
# Expose port 8080 (port của Spring Boot)
devtunnel port create 8080 --protocol https

# Bật tunnel và lấy URL
devtunnel host
```

### Bước 6: Lấy Public URL
Sau khi chạy `devtunnel host`, bạn sẽ thấy output như:
```
Hosting port: 8080
Connect via browser: https://abc123xyz-8080.devtunnels.ms
```

**🔗 URL này chính là BASE_URL cho Frontend sử dụng**

---

## 📦 CẤU TRÚC API RESPONSE

Tất cả API đều trả về cấu trúc chuẩn:

### ✅ Success Response
```json
{
  "success": true,
  "data": { /* Dữ liệu trả về */ },
  "message": "Success message",
  "timestamp": "2025-10-23T08:53:22.125Z"
}
```

### ❌ Error Response
```json
{
  "success": false,
  "data": null,
  "message": "Error message",
  "timestamp": "2025-10-23T08:53:22.125Z"
}
```

---

## 🔐 AUTHENTICATION & AUTHORIZATION

### Header yêu cầu cho các API cần xác thực:
```javascript
headers: {
  'Authorization': 'Bearer YOUR_JWT_TOKEN',
  'Content-Type': 'application/json'
}
```

### Phân quyền Role:
- **ROLE_USER**: Người dùng đặt sân
- **ROLE_OWNER**: Chủ sân (cần nâng cấp từ USER)

---

## 📍 API ENDPOINTS

### Base URL
```
https://YOUR-TUNNEL-ID-8080.devtunnels.ms/api
```

---

## 1. AUTH APIs

### 1.1. Đăng ký tài khoản
**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "fullname": "Nguyễn Văn A",
  "email": "user@example.com",
  "phone": "0987654321",
  "password": "password123",
  "confirmPassword": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "data": "User registered successfully",
  "message": "Registered"
}
```

**Lưu ý:**
- Tài khoản mới đăng ký mặc định là **ROLE_USER**
- Phone phải là số từ 8-15 chữ số
- Password tối thiểu 6 ký tự
- Email và phone phải unique

---

### 1.2. Đăng nhập
**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "phone": "0987654321",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": 1,
    "phone": "0987654321",
    "roles": ["ROLE_USER"]
  },
  "message": "Login success"
}
```

**Lưu trữ token:**
```javascript
// Lưu vào localStorage
localStorage.setItem('token', response.data.token);
localStorage.setItem('userId', response.data.id);
localStorage.setItem('roles', JSON.stringify(response.data.roles));
```

---

### 1.3. Quên mật khẩu
**Endpoint:** `POST /auth/forgot-password`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "data": "Nếu email hợp lệ, mã đặt lại đã được gửi",
  "message": "Sent"
}
```

---

### 1.4. Đặt lại mật khẩu
**Endpoint:** `POST /auth/reset-password`

**Request Body:**
```json
{
  "token": "RESET_TOKEN_FROM_EMAIL",
  "newPassword": "newpassword123"
}
```

**Response:**
```json
{
  "success": true,
  "data": "Đổi mật khẩu thành công",
  "message": "Password changed"
}
```

---

## 2. USER APIs

### 2.1. Lấy thông tin user hiện tại
**Endpoint:** `GET /users/me`

**Headers:** Yêu cầu token

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "fullname": "Nguyễn Văn A",
    "phone": "0987654321",
    "email": "user@example.com",
    "roles": ["ROLE_USER"],
    "bankName": null,
    "bankAccountNumber": null,
    "bankAccountName": null
  },
  "message": "Success"
}
```

---

### 2.2. Cập nhật thông tin user (bao gồm thông tin ngân hàng)
**Endpoint:** `PUT /users/me`

**Headers:** Yêu cầu token

**Request Body:**
```json
{
  "fullname": "Nguyễn Văn A Updated",
  "email": "newemail@example.com",
  "bankName": "Vietcombank",
  "bankAccountNumber": "1234567890",
  "bankAccountName": "NGUYEN VAN A"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "fullname": "Nguyễn Văn A Updated",
    "email": "newemail@example.com",
    "bankName": "Vietcombank",
    "bankAccountNumber": "1234567890",
    "bankAccountName": "NGUYEN VAN A"
  },
  "message": "Cập nhật thông tin thành công"
}
```

**Lưu ý:** 
- Thông tin ngân hàng BẮT BUỘC trước khi tạo venues (nếu là OWNER)

---

### 2.3. Nâng cấp lên chủ sân (USER → OWNER)
**Endpoint:** `POST /users/me/request-owner-role`

**Headers:** Yêu cầu token, role USER

**Response:**
```json
{
  "success": true,
  "data": "Đã nâng cấp thành chủ sân thành công! Vui lòng đăng nhập lại để cập nhật quyền.",
  "message": "Success"
}
```

**Sau khi nâng cấp:**
- User cần **đăng nhập lại** để JWT token được cập nhật role mới
- Sau đó có thể truy cập các API dành cho OWNER

---

## 3. VENUES APIs

### 3.1. Lấy danh sách tất cả venues
**Endpoint:** `GET /venues`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Sân bóng ABC",
      "description": "Sân bóng đá mini chất lượng cao",
      "phoneNumber": "0901234567",
      "email": "contact@sanabac.com",
      "numberOfCourt": 5,
      "address": {
        "id": 1,
        "provinceOrCity": "TP. Hồ Chí Minh",
        "district": "Quận 1",
        "detailAddress": "123 Đường Lê Lợi"
      },
      "owner": {
        "id": 2,
        "fullname": "Nguyễn Văn B",
        "phone": "0987654321"
      }
    }
  ],
  "message": "List venues"
}
```

---

### 3.2. Tìm kiếm venues
**Endpoint:** `GET /venues/search`

**Query Parameters:**
- `name` (optional): Tên sân
- `province` (optional): Tỉnh/Thành phố
- `district` (optional): Quận/Huyện
- `detail` (optional): Địa chỉ chi tiết

**Ví dụ:**
```
GET /venues/search?name=ABC&province=TP.%20Hồ%20Chí%20Minh
```

**Response:** Giống GET /venues

---

### 3.3. Lấy thông tin chi tiết venues
**Endpoint:** `GET /venues/{id}`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Sân bóng ABC",
    "description": "Sân bóng đá mini chất lượng cao",
    "phoneNumber": "0901234567",
    "email": "contact@sanabac.com",
    "numberOfCourt": 5,
    "address": {
      "id": 1,
      "provinceOrCity": "TP. Hồ Chí Minh",
      "district": "Quận 1",
      "detailAddress": "123 Đường Lê Lợi"
    },
    "owner": {
      "id": 2,
      "fullname": "Nguyễn Văn B",
      "phone": "0987654321",
      "email": "owner@example.com",
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN B"
    }
  }
}
```

---

### 3.4. Tạo venues mới (OWNER only)
**Endpoint:** `POST /venues`

**Headers:** Yêu cầu token, role OWNER

**Request Body:**
```json
{
  "name": "Sân bóng XYZ",
  "description": "Sân bóng đá 5 người có mái che",
  "phoneNumber": "0909123456",
  "email": "xyz@example.com",
  "address": {
    "provinceOrCity": "Hà Nội",
    "district": "Quận Ba Đình",
    "detailAddress": "456 Đường Láng"
  }
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "name": "Sân bóng XYZ",
    "numberOfCourt": 0,
    "address": {
      "id": 2,
      "provinceOrCity": "Hà Nội",
      "district": "Quận Ba Đình",
      "detailAddress": "456 Đường Láng"
    }
  },
  "message": "Created"
}
```

**Lưu ý:**
- OWNER phải cập nhật thông tin ngân hàng trước khi tạo venues
- `numberOfCourt` ban đầu = 0, tăng khi tạo court

---

### 3.5. Cập nhật venues (OWNER only)
**Endpoint:** `PUT /venues/{id}`

**Headers:** Yêu cầu token, role OWNER

**Request Body:** Giống POST /venues

**Response:** Giống POST /venues

---

### 3.6. Xóa venues (OWNER only)
**Endpoint:** `DELETE /venues/{id}`

**Headers:** Yêu cầu token, role OWNER

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Deleted"
}
```

---

## 4. COURT APIs

### 4.1. Lấy danh sách tất cả courts
**Endpoint:** `GET /courts`

**Response:**
```json
[
  {
    "id": 1,
    "description": "Sân số 1 - Cỏ nhân tạo cao cấp",
    "booked": false,
    "venues": {
      "id": 1,
      "name": "Sân bóng ABC"
    }
  }
]
```

---

### 4.2. Lấy thông tin court
**Endpoint:** `GET /courts/{id}`

**Response:**
```json
{
  "id": 1,
  "description": "Sân số 1 - Cỏ nhân tạo cao cấp",
  "booked": false,
  "venues": {
    "id": 1,
    "name": "Sân bóng ABC"
  }
}
```

---

### 4.3. Tạo court mới (OWNER only)
**Endpoint:** `POST /courts`

**Headers:** Yêu cầu token

**Request Body:**
```json
{
  "venueId": 1,
  "description": "Sân số 2 - Có mái che"
}
```

**Response:**
```json
{
  "id": 2,
  "description": "Sân số 2 - Có mái che",
  "booked": false,
  "venues": {
    "id": 1,
    "name": "Sân bóng ABC"
  }
}
```

**Lưu ý:**
- `numberOfCourt` của venues tự động tăng lên 1

---

### 4.4. Cập nhật court
**Endpoint:** `PUT /courts/{id}`

**Request Body:**
```json
{
  "description": "Sân số 2 - Đã sửa chữa",
  "booked": false
}
```

---

### 4.5. Xóa court
**Endpoint:** `DELETE /courts/{id}`

**Response:** 204 No Content

**Lưu ý:**
- `numberOfCourt` của venues tự động giảm 1

---

## 5. PRICE RULE APIs

### 5.1. Lấy quy tắc giá của venues
**Endpoint:** `GET /pricerules/venue/{venueId}`

**Response:**
```json
[
  {
    "id": 1,
    "name": "Giờ vàng",
    "startTime": "18:00:00",
    "endTime": "21:00:00",
    "pricePerHour": 200000,
    "active": true,
    "venues": {
      "id": 1,
      "name": "Sân bóng ABC"
    }
  }
]
```

---

### 5.2. Tạo quy tắc giá (OWNER only)
**Endpoint:** `POST /pricerules`

**Headers:** Yêu cầu token, role OWNER

**Request Body:**
```json
{
  "venueId": 1,
  "name": "Giờ bình thường",
  "startTime": "06:00:00",
  "endTime": "18:00:00",
  "pricePerHour": 150000
}
```

**Response:**
```json
{
  "id": 2,
  "name": "Giờ bình thường",
  "startTime": "06:00:00",
  "endTime": "18:00:00",
  "pricePerHour": 150000,
  "active": true
}
```

---

### 5.3. Cập nhật quy tắc giá (OWNER only)
**Endpoint:** `PUT /pricerules/{id}`

**Headers:** Yêu cầu token, role OWNER

**Request Body:**
```json
{
  "name": "Giờ bình thường (updated)",
  "pricePerHour": 160000
}
```

---

### 5.4. Bật/tắt quy tắc giá (OWNER only)
**Endpoint:** `PATCH /pricerules/{id}/toggle`

**Headers:** Yêu cầu token, role OWNER

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "active": false
  },
  "message": "Price rule deactivated"
}
```

---

### 5.5. Xóa quy tắc giá (OWNER only)
**Endpoint:** `DELETE /pricerules/{id}`

**Headers:** Yêu cầu token, role OWNER

**Response:**
```json
{
  "success": true,
  "message": "Price rule deleted successfully"
}
```

---

## 6. BOOKING APIs

### 6.1. Tạo booking (USER only)
**Endpoint:** `POST /bookings`

**Headers:** Yêu cầu token, role USER

**Request Body:**
```json
{
  "venueId": 1,
  "courtId": 2,
  "startTime": "2025-10-25T14:00:00",
  "endTime": "2025-10-25T16:00:00"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "startTime": "2025-10-25T14:00:00",
    "endTime": "2025-10-25T16:00:00",
    "totalPrice": 400000,
    "status": "PENDING_PAYMENT",
    "expireTime": "2025-10-25T14:15:00",
    "user": {
      "id": 1,
      "fullname": "Nguyễn Văn A",
      "phone": "0987654321"
    },
    "court": {
      "id": 2,
      "description": "Sân số 2"
    },
    "venue": {
      "id": 1,
      "name": "Sân bóng ABC"
    },
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN B"
    }
  },
  "message": "Đặt sân thành công. Vui lòng chuyển khoản trong 15 phút."
}
```

**Lưu ý:**
- `expireTime`: Thời gian hết hạn thanh toán (15 phút sau khi tạo)
- `ownerBankInfo`: Thông tin TK ngân hàng chủ sân để chuyển khoản
- Frontend hiển thị countdown từ expireTime

---

### 6.2. Xác nhận đã chuyển khoản (USER only)
**Endpoint:** `PUT /bookings/{id}/confirm-payment`

**Headers:** Yêu cầu token, role USER

**Request Body:**
```json
{
  "paymentProofUrl": "https://example.com/payment-proof.jpg"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "PAYMENT_UPLOADED",
    "paymentProofUrl": "https://example.com/payment-proof.jpg",
    "paymentProofUploadedAt": "2025-10-25T14:10:00"
  },
  "message": "Đã gửi chứng minh chuyển khoản. Chờ chủ sân xác nhận."
}
```

---

### 6.3. Xác nhận booking (OWNER only)
**Endpoint:** `PUT /bookings/{id}/accept`

**Headers:** Yêu cầu token, role OWNER

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "CONFIRMED"
  },
  "message": "Đã xác nhận đặt sân thành công."
}
```

---

### 6.4. Từ chối booking (OWNER only)
**Endpoint:** `PUT /bookings/{id}/reject`

**Headers:** Yêu cầu token, role OWNER

**Request Body:**
```json
{
  "rejectionReason": "Hình ảnh chuyển khoản không rõ ràng"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "REJECTED",
    "rejectionReason": "Hình ảnh chuyển khoản không rõ ràng"
  },
  "message": "Đã từ chối đặt sân."
}
```

---

### 6.5. Hủy booking (USER only)
**Endpoint:** `PUT /bookings/{id}/cancel`

**Headers:** Yêu cầu token, role USER

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "CANCELLED"
  },
  "message": "Booking cancelled successfully"
}
```

---

### 6.6. Lấy danh sách booking của tôi (USER)
**Endpoint:** `GET /bookings/my-bookings`

**Headers:** Yêu cầu token, role USER

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "startTime": "2025-10-25T14:00:00",
      "endTime": "2025-10-25T16:00:00",
      "status": "CONFIRMED",
      "totalPrice": 400000,
      "venue": {
        "id": 1,
        "name": "Sân bóng ABC"
      },
      "court": {
        "id": 2,
        "description": "Sân số 2"
      }
    }
  ],
  "message": "My bookings retrieved successfully"
}
```

---

### 6.7. Lấy booking theo venues (OWNER only)
**Endpoint:** `GET /bookings/venue/{venueId}`

**Headers:** Yêu cầu token, role OWNER

**Response:** Giống GET /bookings/my-bookings

---

### 6.8. Lấy danh sách booking chờ xác nhận (OWNER only)
**Endpoint:** `GET /bookings/pending`

**Headers:** Yêu cầu token, role OWNER

**Response:** Danh sách booking có `status = PAYMENT_UPLOADED`

---

### 6.9. Lấy chi tiết booking
**Endpoint:** `GET /bookings/{id}`

**Headers:** Yêu cầu token

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "startTime": "2025-10-25T14:00:00",
    "endTime": "2025-10-25T16:00:00",
    "totalPrice": 400000,
    "status": "CONFIRMED",
    "paymentProofUrl": "https://example.com/payment-proof.jpg",
    "paymentProofUploadedAt": "2025-10-25T14:10:00",
    "user": {
      "id": 1,
      "fullname": "Nguyễn Văn A",
      "phone": "0987654321"
    },
    "venue": {
      "id": 1,
      "name": "Sân bóng ABC"
    },
    "court": {
      "id": 2,
      "description": "Sân số 2"
    }
  },
  "message": "Booking retrieved successfully"
}
```

---

## 7. NOTIFICATION APIs

### 7.1. Lấy danh sách thông báo
**Endpoint:** `GET /notifications`

**Headers:** Yêu cầu token

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Đặt sân thành công",
      "message": "Bạn đã đặt sân ABC thành công. Vui lòng chuyển khoản trong 15 phút.",
      "type": "BOOKING_CREATED",
      "read": false,
      "createdAt": "2025-10-25T14:00:00"
    }
  ],
  "message": "Lấy danh sách thông báo thành công."
}
```

---

### 7.2. Lấy số thông báo chưa đọc
**Endpoint:** `GET /notifications/unread-count`

**Headers:** Yêu cầu token

**Response:**
```json
{
  "success": true,
  "data": 5,
  "message": "Số thông báo chưa đọc."
}
```

---

### 7.3. Đánh dấu đã đọc
**Endpoint:** `PUT /notifications/{id}/read`

**Headers:** Yêu cầu token

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Đã đánh dấu thông báo là đã đọc."
}
```

---

### 7.4. Đánh dấu tất cả đã đọc
**Endpoint:** `PUT /notifications/read-all`

**Headers:** Yêu cầu token

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Đã đánh dấu tất cả thông báo là đã đọc."
}
```

---

### 7.5. Xóa thông báo
**Endpoint:** `DELETE /notifications/{id}`

**Headers:** Yêu cầu token

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Đã xóa thông báo."
}
```

---

## 🔄 LUỒNG NGHIỆP VỤ CHÍNH

### LUỒNG 1: Đăng ký và đăng nhập
```
1. User đăng ký: POST /auth/register
2. Hệ thống tạo tài khoản với ROLE_USER
3. User đăng nhập: POST /auth/login
4. Lưu token vào localStorage
5. Gọi GET /users/me để lấy thông tin user
```

### LUỒNG 2: Nâng cấp lên chủ sân
```
1. User (ROLE_USER) gọi: POST /users/me/request-owner-role
2. Hệ thống thêm ROLE_OWNER
3. User đăng nhập lại: POST /auth/login
4. Token mới có ROLE_OWNER
5. Cập nhật thông tin ngân hàng: PUT /users/me (BẮT BUỘC)
6. Tạo venues: POST /venues
```

### LUỒNG 3: Chủ sân tạo và quản lý sân
```
1. OWNER đã có thông tin ngân hàng
2. Tạo venues: POST /venues
3. Tạo các court cho venues: POST /courts
4. Tạo quy tắc giá: POST /pricerules
5. Quản lý: GET /bookings/venue/{venueId}
```

### LUỒNG 4: User đặt sân (QUAN TRỌNG)
```
1. USER tìm sân: GET /venues/search
2. Xem chi tiết: GET /venues/{id}
3. Xem giá: GET /pricerules/venue/{venueId}
4. Đặt sân: POST /bookings
   ↓
5. Nhận response với:
   - expireTime (15 phút)
   - ownerBankInfo (thông tin TK chủ sân)
   - totalPrice
   ↓
6. Frontend hiển thị:
   - Countdown 15 phút
   - Thông tin TK để chuyển khoản
   - Form upload hình ảnh chuyển khoản
   ↓
7. User chuyển khoản và upload ảnh:
   PUT /bookings/{id}/confirm-payment
   ↓
8. Status chuyển: PENDING_PAYMENT → PAYMENT_UPLOADED
   ↓
9. OWNER nhận thông báo: GET /notifications
10. OWNER xem booking: GET /bookings/pending
11. OWNER xác nhận/từ chối:
    - Accept: PUT /bookings/{id}/accept
    - Reject: PUT /bookings/{id}/reject
    ↓
12. USER nhận thông báo kết quả
```

### LUỒNG 5: Theo dõi trạng thái booking
```
BookingStatus flow:
PENDING_PAYMENT → (sau 15 phút không upload) → EXPIRED
PENDING_PAYMENT → (upload ảnh) → PAYMENT_UPLOADED
PAYMENT_UPLOADED → (owner accept) → CONFIRMED
PAYMENT_UPLOADED → (owner reject) → REJECTED
PENDING_PAYMENT/PAYMENT_UPLOADED → (user cancel) → CANCELLED
CONFIRMED → (sau thời gian đặt) → COMPLETED
```

---

## 💻 MÃ VÍ DỤ FRONTEND

### 1. Cấu hình Axios
```javascript
// api/config.js
import axios from 'axios';

const BASE_URL = 'https://YOUR-TUNNEL-ID-8080.devtunnels.ms/api';

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor tự động thêm token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor xử lý response
api.interceptors.response.use(
  (response) => response.data, // Trả về data trực tiếp
  (error) => {
    if (error.response?.status === 401) {
      // Token hết hạn, redirect về login
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error.response?.data || error.message);
  }
);

export default api;
```

### 2. Service Auth
```javascript
// services/authService.js
import api from './config';

export const authService = {
  async register(data) {
    return await api.post('/auth/register', data);
  },

  async login(phone, password) {
    const response = await api.post('/auth/login', { phone, password });
    if (response.success) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('userId', response.data.id);
      localStorage.setItem('roles', JSON.stringify(response.data.roles));
    }
    return response;
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('roles');
  },

  isAuthenticated() {
    return !!localStorage.getItem('token');
  },

  hasRole(role) {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]');
    return roles.includes(role);
  }
};
```

### 3. Service User
```javascript
// services/userService.js
import api from './config';

export const userService = {
  async getCurrentUser() {
    return await api.get('/users/me');
  },

  async updateUser(data) {
    return await api.put('/users/me', data);
  },

  async requestOwnerRole() {
    return await api.post('/users/me/request-owner-role');
  }
};
```

### 4. Service Booking
```javascript
// services/bookingService.js
import api from './config';

export const bookingService = {
  async createBooking(data) {
    return await api.post('/bookings', data);
  },

  async confirmPayment(bookingId, paymentProofUrl) {
    return await api.put(`/bookings/${bookingId}/confirm-payment`, {
      paymentProofUrl
    });
  },

  async getMyBookings() {
    return await api.get('/bookings/my-bookings');
  },

  async getBookingById(id) {
    return await api.get(`/bookings/${id}`);
  },

  async cancelBooking(id) {
    return await api.put(`/bookings/${id}/cancel`);
  },

  // OWNER APIs
  async getPendingBookings() {
    return await api.get('/bookings/pending');
  },

  async acceptBooking(id) {
    return await api.put(`/bookings/${id}/accept`);
  },

  async rejectBooking(id, reason) {
    return await api.put(`/bookings/${id}/reject`, {
      rejectionReason: reason
    });
  }
};
```

### 5. Component: Countdown Timer
```javascript
// components/BookingCountdown.jsx
import React, { useState, useEffect } from 'react';

export const BookingCountdown = ({ expireTime, onExpire }) => {
  const [timeLeft, setTimeLeft] = useState(null);

  useEffect(() => {
    const interval = setInterval(() => {
      const now = new Date();
      const expire = new Date(expireTime);
      const diff = expire - now;

      if (diff <= 0) {
        clearInterval(interval);
        setTimeLeft(null);
        onExpire && onExpire();
      } else {
        const minutes = Math.floor(diff / 60000);
        const seconds = Math.floor((diff % 60000) / 1000);
        setTimeLeft(`${minutes}:${seconds.toString().padStart(2, '0')}`);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [expireTime, onExpire]);

  if (!timeLeft) {
    return <div className="text-red-500">Đã hết thời gian thanh toán</div>;
  }

  return (
    <div className="text-orange-500 font-bold text-lg">
      ⏱️ Thời gian còn lại: {timeLeft}
    </div>
  );
};
```

### 6. Component: Booking Form
```javascript
// components/BookingForm.jsx
import React, { useState } from 'react';
import { bookingService } from '../services/bookingService';
import { BookingCountdown } from './BookingCountdown';

export const BookingForm = ({ venueId, courtId, onSuccess }) => {
  const [formData, setFormData] = useState({
    startTime: '',
    endTime: ''
  });
  const [booking, setBooking] = useState(null);
  const [paymentProofUrl, setPaymentProofUrl] = useState('');
  const [loading, setLoading] = useState(false);

  const handleCreateBooking = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await bookingService.createBooking({
        venueId,
        courtId,
        startTime: formData.startTime,
        endTime: formData.endTime
      });

      if (response.success) {
        setBooking(response.data);
        alert(response.message);
      }
    } catch (error) {
      alert(error.message || 'Đặt sân thất bại');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmPayment = async () => {
    if (!paymentProofUrl) {
      alert('Vui lòng nhập link hình ảnh chuyển khoản');
      return;
    }

    setLoading(true);
    try {
      const response = await bookingService.confirmPayment(
        booking.id,
        paymentProofUrl
      );

      if (response.success) {
        alert(response.message);
        onSuccess && onSuccess();
      }
    } catch (error) {
      alert(error.message || 'Xác nhận thanh toán thất bại');
    } finally {
      setLoading(false);
    }
  };

  if (booking) {
    return (
      <div className="bg-white p-6 rounded-lg shadow-lg">
        <h2 className="text-2xl font-bold mb-4">Thông tin đặt sân</h2>

        <BookingCountdown
          expireTime={booking.expireTime}
          onExpire={() => alert('Hết thời gian thanh toán!')}
        />

        <div className="mt-4 p-4 bg-blue-50 rounded">
          <h3 className="font-bold mb-2">Thông tin chuyển khoản:</h3>
          <p>Ngân hàng: {booking.ownerBankInfo.bankName}</p>
          <p>Số TK: {booking.ownerBankInfo.bankAccountNumber}</p>
          <p>Chủ TK: {booking.ownerBankInfo.bankAccountName}</p>
          <p className="font-bold text-red-600 mt-2">
            Số tiền: {booking.totalPrice.toLocaleString('vi-VN')} VNĐ
          </p>
          <p className="text-sm text-gray-600 mt-2">
            Nội dung: DAT SAN {booking.id}
          </p>
        </div>

        <div className="mt-4">
          <label className="block mb-2 font-bold">
            Link hình ảnh chuyển khoản:
          </label>
          <input
            type="url"
            className="w-full border p-2 rounded"
            placeholder="https://example.com/payment-proof.jpg"
            value={paymentProofUrl}
            onChange={(e) => setPaymentProofUrl(e.target.value)}
          />
        </div>

        <button
          onClick={handleConfirmPayment}
          disabled={loading}
          className="mt-4 w-full bg-green-500 text-white p-3 rounded font-bold hover:bg-green-600"
        >
          {loading ? 'Đang xử lý...' : 'Xác nhận đã chuyển khoản'}
        </button>
      </div>
    );
  }

  return (
    <form onSubmit={handleCreateBooking} className="bg-white p-6 rounded-lg shadow-lg">
      <h2 className="text-2xl font-bold mb-4">Đặt sân</h2>

      <div className="mb-4">
        <label className="block mb-2 font-bold">Thời gian bắt đầu:</label>
        <input
          type="datetime-local"
          className="w-full border p-2 rounded"
          value={formData.startTime}
          onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
          required
        />
      </div>

      <div className="mb-4">
        <label className="block mb-2 font-bold">Thời gian kết thúc:</label>
        <input
          type="datetime-local"
          className="w-full border p-2 rounded"
          value={formData.endTime}
          onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
          required
        />
      </div>

      <button
        type="submit"
        disabled={loading}
        className="w-full bg-blue-500 text-white p-3 rounded font-bold hover:bg-blue-600"
      >
        {loading ? 'Đang xử lý...' : 'Đặt sân'}
      </button>
    </form>
  );
};
```

---

## 🔍 LƯU Ý QUAN TRỌNG

### 1. Xử lý lỗi
```javascript
try {
  const response = await api.post('/endpoint', data);
  // Xử lý response.data
} catch (error) {
  if (error.success === false) {
    // Lỗi từ API
    alert(error.message);
  } else {
    // Lỗi network hoặc khác
    alert('Có lỗi xảy ra. Vui lòng thử lại.');
  }
}
```

### 2. Refresh token khi hết hạn
- Token hết hạn sau 24 giờ (86400000ms)
- Khi API trả 401, redirect về login
- User cần đăng nhập lại

### 3. Role-based rendering
```javascript
// ProtectedRoute.jsx
const ProtectedRoute = ({ children, requiredRole }) => {
  const hasRole = authService.hasRole(requiredRole);
  
  if (!hasRole) {
    return <Navigate to="/unauthorized" />;
  }
  
  return children;
};

// Usage
<Route path="/owner/venues" element={
  <ProtectedRoute requiredRole="ROLE_OWNER">
    <VenuesManagement />
  </ProtectedRoute>
} />
```

### 4. Upload file (hình ảnh chuyển khoản)
- Backend chỉ nhận **URL** của hình ảnh
- Frontend cần:
  1. Upload hình lên service như Cloudinary, ImgBB, Firebase Storage
  2. Lấy public URL
  3. Gửi URL đó cho backend

```javascript
// Example với ImgBB
const uploadImage = async (file) => {
  const formData = new FormData();
  formData.append('image', file);
  
  const response = await fetch('https://api.imgbb.com/1/upload?key=YOUR_API_KEY', {
    method: 'POST',
    body: formData
  });
  
  const data = await response.json();
  return data.data.url; // URL của hình ảnh
};
```

### 5. Polling notifications
```javascript
// Kiểm tra thông báo mới mỗi 30 giây
useEffect(() => {
  const interval = setInterval(async () => {
    const response = await api.get('/notifications/unread-count');
    if (response.success) {
      setUnreadCount(response.data);
    }
  }, 30000);

  return () => clearInterval(interval);
}, []);
```

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
1. Kiểm tra DevTunnel có đang chạy không
2. Kiểm tra Backend có running trên port 8080 không
3. Kiểm tra token trong localStorage
4. Kiểm tra role trong localStorage
5. Xem console log và network tab

---

## 🎯 CHECKLIST HOÀN THÀNH

### User (Người đặt sân)
- [ ] Đăng ký tài khoản
- [ ] Đăng nhập
- [ ] Xem danh sách sân
- [ ] Tìm kiếm sân
- [ ] Xem chi tiết sân và giá
- [ ] Đặt sân
- [ ] Upload hình chuyển khoản
- [ ] Xem danh sách booking của tôi
- [ ] Hủy booking
- [ ] Xem thông báo
- [ ] Đánh dấu đã đọc thông báo

### Owner (Chủ sân)
- [ ] Đăng ký từ USER
- [ ] Nâng cấp lên OWNER
- [ ] Cập nhật thông tin ngân hàng
- [ ] Tạo venues
- [ ] Tạo court
- [ ] Tạo quy tắc giá
- [ ] Xem danh sách booking chờ xác nhận
- [ ] Xác nhận/Từ chối booking
- [ ] Xem thông báo
- [ ] Quản lý venues, court, price rules

---

**🎉 Chúc bạn phát triển thành công!**

