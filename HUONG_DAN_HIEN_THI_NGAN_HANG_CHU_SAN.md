# Hướng Dẫn Hiển Thị Thông Tin Ngân Hàng Của Chủ Sân

## Tổng Quan

Tính năng này cho phép hiển thị thông tin ngân hàng của chủ sân khi người dùng đặt sân, giúp người dùng biết thông tin để chuyển khoản thanh toán.

## Các Thay Đổi Đã Thực Hiện

### 1. **Model Layer (Domain)**

#### BankInfo.kt
- Đã cập nhật model `BankInfo` với các trường phù hợp với API:
  - `bankName`: Tên ngân hàng
  - `bankAccountNumber`: Số tài khoản (đã đổi từ `accountNumber`)
  - `bankAccountName`: Tên chủ tài khoản (đã đổi từ `accountHolderName`)

#### BookingData.kt
- Đã thêm 2 trường mới:
  - `ownerBankInfo: BankInfo?`: Thông tin ngân hàng của chủ sân
  - `expireTime: String?`: Thời gian hết hạn thanh toán (15 phút)

#### Booking.kt
- Đã thêm model `BookingWithBankInfo`: Response model khi tạo booking mới, bao gồm thông tin ngân hàng của chủ sân

### 2. **Data Layer (DTO & Repository)**

#### BookingDto.kt
- Đã thêm các DTO mới:
  - `BankInfoDto`: DTO cho thông tin ngân hàng
  - `CreateBookingResponseDto`: Response DTO khi tạo booking mới (khác với `BookingDto` thông thường)
  - `UserInfoDto`, `CourtInfoDto`, `VenueInfoDto`: DTO cho thông tin liên quan

#### BookingApi.kt
- Đã cập nhật `createBooking()` để trả về `CreateBookingResponseDto` thay vì `BookingDto`
- Response này bao gồm `ownerBankInfo` và `expireTime`

#### BookingRepositoryImpl.kt (MỚI)
- Repository mới để xử lý logic booking
- Bao gồm mapper functions để chuyển đổi từ DTO sang domain model
- Hỗ trợ các chức năng:
  - `createBooking()`: Tạo booking mới và lấy thông tin ngân hàng
  - `getUserBookings()`: Lấy danh sách booking
  - `getBookingById()`: Lấy chi tiết booking
  - `cancelBooking()`: Hủy booking
  - `confirmBooking()`: Xác nhận booking (cho owner)

#### RepositoryModule.kt
- Đã thêm binding cho `BookingRepository`

### 3. **Presentation Layer (UI & ViewModel)**

#### BookingViewModel.kt (MỚI)
- ViewModel mới để xử lý logic booking
- Quản lý các state:
  - `createBookingState`: State khi tạo booking (bao gồm thông tin ngân hàng)
  - `bookingsState`: State danh sách booking
  - `bookingDetailState`: State chi tiết booking

#### BookingScreen.kt
- Đã thêm `BookingViewModel` vào constructor
- Sẵn sàng để tích hợp gọi API tạo booking

#### PaymentScreen.kt
- Đã cập nhật để hiển thị thông tin ngân hàng của chủ sân
- Hiển thị:
  - Tên ngân hàng
  - Số tài khoản
  - Tên chủ tài khoản
  - Số tiền cần thanh toán
  - Thời gian hết hạn thanh toán (countdown)

## Phân Biệt VenueId và CourtId

**Quan trọng:** Trong project này:

- **VenueId**: ID của địa điểm/cơ sở thể thao (VD: "Sân bóng ABC")
- **CourtId**: ID của sân cụ thể trong venue (VD: "Sân số 1", "Sân số 2")

Khi tạo booking, **BẮT BUỘC sử dụng CourtId**, không phải VenueId!

### Ví dụ cấu trúc:
```
Venue (ID: venue_001)
└── Court 1 (ID: court_001) ← Dùng ID này để booking
└── Court 2 (ID: court_002) ← Dùng ID này để booking
└── Court 3 (ID: court_003) ← Dùng ID này để booking
```

## Cách Sử Dụng

### 1. Tạo Booking và Lấy Thông Tin Ngân Hàng

```kotlin
// Trong BookingScreen.kt hoặc nơi cần tạo booking
val bookingViewModel: BookingViewModel = hiltViewModel()

// Observe state
val createBookingState by bookingViewModel.createBookingState.collectAsState()

// Tạo booking
bookingViewModel.createBooking(
    courtId = "court_001",  // CHÚ Ý: Dùng courtId, không phải venueId
    startTime = "2025-10-28T10:00:00",
    endTime = "2025-10-28T11:00:00",
    notes = "Đặt sân cho 4 người",
    paymentMethod = "BANK_TRANSFER"
)

// Handle state
when (val state = createBookingState) {
    is Resource.Loading -> {
        // Hiển thị loading
    }
    is Resource.Success -> {
        val bookingWithBankInfo = state.data
        // Lấy thông tin ngân hàng
        val bankInfo = bookingWithBankInfo?.ownerBankInfo
        val expireTime = bookingWithBankInfo?.expireTime
        
        // Truyền sang PaymentScreen
        val bookingData = BookingData(
            // ... các thông tin khác
            ownerBankInfo = bankInfo,
            expireTime = expireTime.toString()
        )
    }
    is Resource.Error -> {
        // Hiển thị lỗi
        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
    }
}
```

### 2. Hiển Thị Thông Tin Ngân Hàng trong PaymentScreen

PaymentScreen đã được cập nhật để tự động hiển thị thông tin ngân hàng nếu có:

```kotlin
// PaymentScreen tự động hiển thị nếu bookingData.ownerBankInfo != null
bookingData.ownerBankInfo?.let { bankInfo ->
    // Hiển thị thông tin ngân hàng
    InfoSection(title = "Thông tin chuyển khoản") {
        InfoRow(label = "Ngân hàng:", value = bankInfo.bankName)
        InfoRow(label = "Số tài khoản:", value = bankInfo.bankAccountNumber)
        InfoRow(label = "Chủ tài khoản:", value = bankInfo.bankAccountName)
        InfoRow(label = "Số tiền:", value = "${totalPrice / 1000}.000 VNĐ")
    }
}
```

## API Response Example

### POST /api/bookings - Tạo Booking Mới

**Request:**
```json
{
  "court_id": "court_001",
  "start_time": "2025-10-28T10:00:00",
  "end_time": "2025-10-28T11:00:00",
  "notes": "Đặt sân cho 4 người",
  "payment_method": "BANK_TRANSFER"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "booking_001",
    "user": {
      "id": "user_001",
      "fullname": "Nguyen Van A",
      "phone": "0123456789"
    },
    "court": {
      "id": "court_001",
      "description": "Sân số 1"
    },
    "venue": {
      "id": "venue_001",
      "name": "Sân bóng ABC"
    },
    "startTime": "2025-10-28T10:00:00",
    "endTime": "2025-10-28T11:00:00",
    "totalPrice": 150000,
    "status": "PENDING",
    "expireTime": "2025-10-28T10:15:00",
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN B"
    },
    "notes": "Đặt sân cho 4 người"
  },
  "message": "Đặt sân thành công. Vui lòng chuyển khoản trong 15 phút."
}
```

## Lưu Ý Quan Trọng

### 1. Sự Khác Biệt Giữa VenueId và CourtId
- **VenueId**: ID của địa điểm (cơ sở thể thao)
- **CourtId**: ID của sân cụ thể trong venue
- **Khi tạo booking**: Luôn sử dụng **CourtId**, không phải VenueId

### 2. Thời Gian Hết Hạn (Expire Time)
- Server trả về `expireTime` (thời gian hết hạn thanh toán)
- Thường là 15 phút sau khi tạo booking
- Frontend nên hiển thị countdown để nhắc nhở user

### 3. Trạng Thái Booking
- `PENDING`: Chờ thanh toán
- `CONFIRMED`: Đã xác nhận
- `CANCELLED`: Đã hủy
- `COMPLETED`: Đã hoàn thành
- `NO_SHOW`: Không đến

### 4. Phương Thức Thanh Toán
- `BANK_TRANSFER`: Chuyển khoản ngân hàng (mặc định)
- `CASH`: Tiền mặt
- `E_WALLET`: Ví điện tử
- `CREDIT_CARD`: Thẻ tín dụng

## Các Bước Tiếp Theo

Để hoàn thiện tính năng, bạn cần:

1. **Tích hợp BookingViewModel vào BookingScreen**
   - Gọi `createBooking()` khi user nhấn "Xác nhận đặt sân"
   - Observe `createBookingState` để lấy thông tin ngân hàng
   - Truyền `ownerBankInfo` và `expireTime` sang PaymentScreen

2. **Thêm Countdown Timer**
   - Hiển thị thời gian còn lại cho đến `expireTime`
   - Tự động chuyển về trang chủ khi hết hạn

3. **Xác Nhận Đã Chuyển Khoản**
   - Thêm API call `PUT /bookings/{id}/confirm-payment`
   - Cho phép user xác nhận đã chuyển khoản

4. **Hiển thị QR Code (Optional)**
   - Server có thể trả về QR code cho việc chuyển khoản
   - Hiển thị QR code trong PaymentScreen

## Testing

Để test tính năng:

1. **Build project**: Đảm bảo không có lỗi compile
2. **Test API**: Sử dụng Postman để test endpoint `POST /api/bookings`
3. **Test UI**: Tạo booking và kiểm tra thông tin ngân hàng hiển thị đúng

## Troubleshooting

### Lỗi "Unresolved reference: BookingRepository"
- Đảm bảo đã build lại project sau khi thêm `BookingRepository`
- Kiểm tra `RepositoryModule.kt` đã có binding cho `BookingRepository`

### Không hiển thị thông tin ngân hàng
- Kiểm tra `BookingData.ownerBankInfo` có giá trị không null
- Kiểm tra API response có trả về `ownerBankInfo` đúng format

### Lỗi "courtId not found"
- Kiểm tra đang sử dụng `courtId` chứ không phải `venueId`
- Verify courtId tồn tại trong database

---

**Tác giả**: GitHub Copilot  
**Ngày cập nhật**: 03/11/2025

