# Tích hợp API Court Availability

## Tổng quan
Đã tích hợp API `/venues/{venueId}/courts/availability` vào màn hình chi tiết venue của chủ sân. API này trả về thông tin chi tiết về tình trạng sân và các time slots đã được đặt.

## API Endpoint
```
GET /venues/{venueId}/courts/availability?startTime={startTime}&endTime={endTime}
```

### Request Parameters
- `venueId`: ID của venue (Long)
- `startTime`: Thời gian bắt đầu (ISO format: "2025-11-07T14:00:00")
- `endTime`: Thời gian kết thúc (ISO format: "2025-11-07T15:00:00")

### Response Format
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "description": "Sân số 1",
      "available": false,
      "bookedSlots": [
        {
          "startTime": [2025, 11, 7, 14, 0],
          "endTime": [2025, 11, 7, 15, 0],
          "bookingId": 38
        }
      ]
    }
  ]
}
```

## Các thay đổi đã thực hiện

### 1. Data Layer

#### File: `CourtAvailabilityDto.kt`
- **Cập nhật**: `BookedSlotInfoDto` để nhận `startTime` và `endTime` dưới dạng `List<Int>` thay vì String
- **Thêm**: Helper methods `getStartTimeString()` và `getEndTimeString()` để convert từ array sang string format "HH:mm:ss"

```kotlin
data class BookedSlotInfoDto(
    @SerializedName("startTime")
    val startTime: List<Int>, // [2025, 11, 7, 14, 0]

    @SerializedName("endTime")
    val endTime: List<Int>,

    @SerializedName("bookingId")
    val bookingId: Long
)
```

### 2. Domain Layer

#### File: `CourtAvailability.kt` (MỚI)
- **Tạo mới**: Domain models cho court availability
  - `CourtAvailability`: Thông tin sân và tình trạng
  - `BookedSlotInfo`: Thông tin time slot đã đặt

```kotlin
data class CourtAvailability(
    val courtId: Long,
    val courtName: String,
    val available: Boolean,
    val bookedSlots: List<BookedSlotInfo>
)

data class BookedSlotInfo(
    val startTime: String,  // "HH:mm:ss"
    val endTime: String,
    val bookingId: Long
)
```

#### File: `VenueRepository.kt`
- **Thêm**: Method interface `getCourtsAvailability()`

```kotlin
suspend fun getCourtsAvailability(
    venueId: Long,
    startTime: String,
    endTime: String
): Flow<Resource<List<CourtAvailability>>>
```

### 3. Repository Implementation

#### File: `VenueRepositoryImpl.kt`
- **Implement**: Method `getCourtsAvailability()`
- **Logic**:
  - Gọi API `venueApi.getCourtsAvailability()`
  - Convert DTO sang domain model
  - Convert time từ array format sang string format
  - Xử lý error và logging

### 4. Presentation Layer

#### File: `CourtDetailViewModel.kt`
- **Thêm**: Field `courtsAvailability` vào `CourtDetailState`
- **Thêm**: Method `getCourtsAvailabilityForWholeDay()` - Lấy availability cho cả ngày
- **Thêm**: Method `getCourtsAvailabilityForTimeRange()` - Lấy availability cho khoảng thời gian cụ thể

```kotlin
fun getCourtsAvailabilityForWholeDay(venueId: Long, date: String) {
    // Gọi API với startTime = "00:00:00", endTime = "23:59:59"
}
```

#### File: `CourtDetailScreen.kt`
- **Cập nhật**: `LaunchedEffect` để gọi `getCourtsAvailabilityForWholeDay()` khi ngày thay đổi
- **Cập nhật**: Bảng tình trạng sân:
  - Hiển thị tên sân từ API (`court.courtName`) thay vì hardcode "Sân 1", "Sân 2"...
  - Tăng độ rộng cột tên sân từ 70dp lên 90dp để hiển thị tên dài hơn
  - Sử dụng data từ `courtsAvailability` thay vì dùng `actualCourtCount`
  - Kiểm tra booked slots từ `court.bookedSlots` của mỗi court
  - Hiển thị "Đang tải thông tin sân..." khi chưa có data

## Luồng hoạt động

1. **User chọn ngày**: User chọn ngày trong DatePicker
2. **Trigger fetch**: `LaunchedEffect` detect sự thay đổi của `selectedDate`
3. **Convert format**: Convert từ "dd/MM/yyyy" sang "yyyy-MM-dd"
4. **Call API**: `viewModel.getCourtsAvailabilityForWholeDay(venueId, date)`
5. **Process response**:
   - ViewModel gọi repository
   - Repository gọi API với startTime = "{date}T00:00:00", endTime = "{date}T23:59:59"
   - Convert DTO sang domain model
   - Update state với `courtsAvailability`
6. **UI update**:
   - Screen hiển thị danh sách sân với tên từ API
   - Hiển thị các time slots đã đặt với màu đỏ

## Ví dụ sử dụng

### API Response
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "description": "Sân số 1",
      "available": false,
      "bookedSlots": [
        {
          "startTime": [2025, 11, 7, 14, 0],
          "endTime": [2025, 11, 7, 15, 0],
          "bookingId": 38
        }
      ]
    },
    {
      "id": 11,
      "description": "Sân số 2",
      "available": true,
      "bookedSlots": []
    }
  ]
}
```

### Kết quả hiển thị
```
┌──────────┬───────┬───────┬───────┬───────┐
│ Sân      │ 14:00 │ 14:30 │ 15:00 │ 15:30 │
├──────────┼───────┼───────┼───────┼───────┤
│ Sân số 1 │ Đã đặt│       │       │       │
├──────────┼───────┼───────┼───────┼───────┤
│ Sân số 2 │       │       │       │       │
└──────────┴───────┴───────┴───────┴───────┘
```

## Testing

Để test chức năng:

1. Mở màn hình chi tiết venue (CourtDetailScreen)
2. Chọn một ngày bất kỳ
3. Kiểm tra logs để xem API call:
   ```
   CourtDetailScreen: 🔍 Fetching courts availability for venue X on yyyy-MM-dd
   VenueRepository: ========== FETCHING COURTS AVAILABILITY ==========
   ```
4. Kiểm tra bảng tình trạng sân:
   - Tên sân hiển thị đúng theo API (VD: "Sân số 1", "Sân số 2")
   - Các slot đã đặt hiển thị màu đỏ với text "Đã đặt"
   - Các slot trống hiển thị màu trắng

## Lưu ý

- API trả về `startTime`/`endTime` dưới dạng array `[year, month, day, hour, minute]`
- Backend cần đảm bảo format này không thay đổi
- Nếu không có courts availability data, UI sẽ hiển thị "Đang tải thông tin sân..."
- Time slots được tạo dựa trên `openingTime` và `closingTime` của venue (mỗi slot 30 phút)

## Files được thay đổi

1. ✅ `data/remote/dto/CourtAvailabilityDto.kt` - Cập nhật DTO
2. ✅ `domain/model/CourtAvailability.kt` - Tạo mới domain model
3. ✅ `domain/repository/VenueRepository.kt` - Thêm interface method
4. ✅ `data/repository/VenueRepositoryImpl.kt` - Implement method
5. ✅ `presentation/court/viewmodel/CourtDetailViewModel.kt` - Thêm state và methods
6. ✅ `presentation/court/screen/CourtDetailScreen.kt` - Cập nhật UI

## Kết luận

Tích hợp API court availability đã hoàn thành thành công. Màn hình chi tiết venue của chủ sân giờ đây hiển thị:
- ✅ Tên sân chính xác từ backend
- ✅ Tình trạng sân theo thời gian thực
- ✅ Các time slots đã được đặt
- ✅ Tự động cập nhật khi thay đổi ngày
