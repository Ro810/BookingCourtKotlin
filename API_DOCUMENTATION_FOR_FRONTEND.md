# API DOCUMENTATION - PHẦN BỔ SUNG

> **Thêm các endpoint còn thiếu vào tài liệu chính**

---

## 🆕 **ENDPOINT MỚI THÊM**

### ⭐ GET /venues/{venueId}/courts - LẤY DANH SÁCH COURTS ĐơN GIẢN

**Vị trí thêm vào:** Đặt **TRƯỚC** section "17. Get Courts with Availability"

---

### 16.1. Get Courts by Venue ⭐ **ENDPOINT ĐƠN GIẢN - KHÔNG CẦN startTime/endTime**
**GET** `/venues/{venueId}/courts`

**Authentication Required:** ✅ Yes (Any authenticated user)

**🎯 Use Case:** Lấy danh sách courts của một venue cụ thể - KHÔNG CẦN truyền thời gian

**💡 Khi nào dùng API này:**
- ✅ Khi cần hiển thị danh sách sân của venue (VD: "Venue này có 5 sân")
- ✅ Khi build dropdown/picker chọn sân
- ✅ Khi chỉ cần thông tin cơ bản của courts, không cần biết slot nào đã đặt
- ✅ Khi user mới vào trang venue detail (chưa chọn ngày)

**💡 KHÔNG nên dùng khi:**
- ❌ Cần biết slot nào đã đặt → Dùng `/venues/{venueId}/courts/availability`
- ❌ Đang ở màn hình booking calendar → Dùng API availability

**Example:**
```
GET /venues/1/courts
```

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "description": "Sân số 1",
      "venues": {
        "id": 1,
        "name": "Sân bóng ABC"
      }
    },
    {
      "id": 2,
      "description": "Sân số 2",
      "venues": {
        "id": 1,
        "name": "Sân bóng ABC"
      }
    },
    {
      "id": 3,
      "description": "Sân số 3",
      "venues": {
        "id": 1,
        "name": "Sân bóng ABC"
      }
    }
  ],
  "message": "Courts list",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Response Error - Venue not found (404):**
```json
{
  "success": false,
  "message": "Venue not found with id: 999",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

## 📱 **FRONTEND IMPLEMENTATION**

### **JavaScript/TypeScript:**

```javascript
/**
 * Lấy danh sách courts đơn giản - không cần biết booked slots
 */
async function getCourtsByVenue(venueId) {
  const token = localStorage.getItem('token');
  
  const response = await fetch(`/api/venues/${venueId}/courts`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const data = await response.json();
  
  if (!data.success) {
    throw new Error(data.message);
  }
  
  return data.data; // Array of courts
}

// Example 1: Hiển thị dropdown chọn sân
async function populateCourtsDropdown(venueId) {
  try {
    const courts = await getCourtsByVenue(venueId);
    
    const selectElement = document.getElementById('court-select');
    selectElement.innerHTML = '<option value="">-- Chọn sân --</option>';
    
    courts.forEach(court => {
      const option = document.createElement('option');
      option.value = court.id;
      option.textContent = court.description;
      selectElement.appendChild(option);
    });
  } catch (error) {
    console.error('Error loading courts:', error);
    alert('Không thể tải danh sách sân');
  }
}

// Example 2: Hiển thị số lượng sân
async function displayCourtsInfo(venueId) {
  const courts = await getCourtsByVenue(venueId);
  
  document.getElementById('courts-count').textContent = 
    `Có ${courts.length} sân`;
  
  document.getElementById('courts-list').innerHTML = courts.map(court => 
    `<li>${court.description}</li>`
  ).join('');
}
```

### **React Example:**

```jsx
function VenueDetailScreen({ venueId }) {
  const [courts, setCourts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    async function loadCourts() {
      setLoading(true);
      try {
        const data = await getCourtsByVenue(venueId);
        setCourts(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }
    
    loadCourts();
  }, [venueId]);
  
  if (loading) return <div>Đang tải...</div>;
  if (error) return <div>Lỗi: {error}</div>;
  
  return (
    <div className="venue-detail">
      <h3>Danh sách sân ({courts.length} sân)</h3>
      <ul className="courts-list">
        {courts.map(court => (
          <li key={court.id} className="court-item">
            {court.description}
          </li>
        ))}
      </ul>
    </div>
  );
}
```

### **Kotlin/Android Example:**

```kotlin
// Data class
data class Court(
    val id: Long,
    val description: String,
    val venues: VenueBasic
)

data class VenueBasic(
    val id: Long,
    val name: String
)

// API Service
@GET("venues/{venueId}/courts")
suspend fun getCourtsByVenue(
    @Path("venueId") venueId: Long
): ApiResponse<List<Court>>

// ViewModel
class VenueDetailViewModel : ViewModel() {
    private val _courts = MutableLiveData<List<Court>>()
    val courts: LiveData<List<Court>> = _courts
    
    fun loadCourts(venueId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getCourtsByVenue(venueId)
                if (response.success) {
                    _courts.value = response.data
                } else {
                    // Handle error
                }
            } catch (e: Exception) {
                Log.e("VenueDetail", "Error loading courts", e)
            }
        }
    }
}

// Fragment/Activity
class VenueDetailFragment : Fragment() {
    private val viewModel: VenueDetailViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val venueId = args.venueId
        viewModel.loadCourts(venueId)
        
        viewModel.courts.observe(viewLifecycleOwner) { courts ->
            binding.courtsCount.text = "Có ${courts.size} sân"
            
            // Hiển thị danh sách
            val adapter = CourtsAdapter(courts)
            binding.courtsList.adapter = adapter
        }
    }
}
```

---

## ⚖️ **SO SÁNH 2 API**

| Tính năng | `/venues/{id}/courts` | `/venues/{id}/courts/availability` |
|-----------|----------------------|-------------------------------------|
| **Query params** | ❌ Không cần | ✅ Cần `startTime`, `endTime` |
| **Response** | Courts cơ bản | Courts + booked slots |
| **Tốc độ** | ⚡ Nhanh (chỉ query courts) | 🐢 Chậm hơn (phải tính toán slots) |
| **Use case chính** | Hiển thị danh sách sân | Booking calendar/grid |
| **Khi nào dùng** | Lúc xem thông tin venue | Lúc đặt sân cần biết slot trống |
| **Ví dụ màn hình** | Venue Detail Screen | Booking Screen |

---

## 💡 **BEST PRACTICES**

### ✅ **ĐÚNG - Dùng API phù hợp:**

```javascript
// Scenario 1: User vào trang venue detail
async function onVenuePageLoad(venueId) {
  // ✅ Dùng API đơn giản - chỉ cần biết có bao nhiêu sân
  const courts = await getCourtsByVenue(venueId);
  displayCourtsCount(courts.length); // "Có 5 sân"
  displayCourtsList(courts); // Hiển thị danh sách
}

// Scenario 2: User nhấn "Đặt sân" và chọn ngày
async function onBookingButtonClick(venueId, selectedDate) {
  // ✅ Bây giờ mới dùng API availability - cần biết slot nào trống
  const startTime = `${selectedDate}T00:00:00`;
  const endTime = `${selectedDate}T23:59:59`;
  
  const courtsWithAvailability = await getCourtsWithAvailability(
    venueId, startTime, endTime
  );
  
  displayBookingGrid(courtsWithAvailability); // Hiển thị lưới đặt sân
}
```

### ❌ **SAI - Lãng phí tài nguyên:**

```javascript
// ❌ SAI: Dùng API phức tạp khi chỉ cần thông tin đơn giản
async function onVenuePageLoad(venueId) {
  // Gọi API phức tạp với thời gian
  const courtsWithAvailability = await getCourtsWithAvailability(
    venueId, 
    '2025-11-05T00:00:00', 
    '2025-11-05T23:59:59'
  );
  
  // Nhưng chỉ dùng để hiển thị số lượng!
  displayCourtsCount(courtsWithAvailability.length); // Lãng phí!
}

// ✅ ĐÚNG: Dùng API đơn giản
async function onVenuePageLoad(venueId) {
  const courts = await getCourtsByVenue(venueId); // Nhanh hơn!
  displayCourtsCount(courts.length);
}
```

---

## 🔧 **WORKAROUND CŨ (TRƯỚC KHI CÓ API MỚI)**

**Trước đây frontend phải làm thế này (không tối ưu):**

```javascript
// ❌ Cách cũ: Lấy TẤT CẢ courts rồi filter
async function getCourtsByVenue_OLD(venueId) {
  // Lấy tất cả courts trong hệ thống (có thể 1000+ courts)
  const response = await fetch('/api/courts');
  const allCourts = await response.json();
  
  // Filter ở client
  const filteredCourts = allCourts.filter(court => 
    court.venues.id === venueId
  );
  
  return filteredCourts;
}

// ✅ Cách mới: Backend filter sẵn
async function getCourtsByVenue_NEW(venueId) {
  const response = await fetch(`/api/venues/${venueId}/courts`);
  const data = await response.json();
  return data.data; // Chỉ nhận courts của venue này
}
```

**So sánh hiệu năng:**
```
Giả sử hệ thống có:
- 100 venues
- Mỗi venue có 5 courts
- Tổng: 500 courts

Cách cũ (GET /courts):
- Response size: ~50KB (500 courts)
- Network time: 500ms
- Filter time: 10ms
- Total: 510ms

Cách mới (GET /venues/{id}/courts):
- Response size: ~0.5KB (5 courts)
- Network time: 50ms
- Filter time: 0ms (backend đã filter)
- Total: 50ms

→ Nhanh hơn 10 lần!
```

---

## 🔍 **KIỂM TRA JSON RESPONSE CÓ CHUẨN KHÔNG**

### ✅ **Response CHUẨN - Có ApiResponse wrapper:**

```json
{
  "success": true,
  "data": [...],
  "message": "Courts list",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Các field bắt buộc:**
- ✅ `success` (boolean)
- ✅ `data` (object hoặc array hoặc null)
- ✅ `message` (string)
- ✅ `timestamp` (ISO DateTime string)

### ⚠️ **LƯU Ý VỀ API `/courts` (Section 18)**

**API này KHÔNG CHUẨN - Không có wrapper:**

```json
// ❌ KHÔNG CHUẨN: Response trực tiếp là array
[
  {
    "id": 1,
    "description": "Sân số 1",
    "venues": {
      "id": 1,
      "name": "Sân bóng ABC"
    }
  }
]
```

**Nên sửa thành:**

```json
// ✅ CHUẨN: Có ApiResponse wrapper
{
  "success": true,
  "data": [
    {
      "id": 1,
      "description": "Sân số 1",
      "venues": {
        "id": 1,
        "name": "Sân bóng ABC"
      }
    }
  ],
  "message": "All courts",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

## 📝 **TÓM TẮT CẦN BỔ SUNG VÀO TÀI LIỆU CHÍNH**

### **1. Thêm Section 16.1:**
- Endpoint: `GET /venues/{venueId}/courts`
- Vị trí: Đặt NGAY SAU "16. Delete Venue Image"
- Đặt TRƯỚC "17. Get Courts with Availability"

### **2. Fix JSON Response Section 18:**
- API `GET /courts` hiện tại trả về array trực tiếp
- Cần ghi chú rõ: "Response này KHÔNG có ApiResponse wrapper"
- Hoặc đề xuất backend fix

### **3. Thêm bảng so sánh:**
- So sánh `/venues/{id}/courts` vs `/venues/{id}/courts/availability`
- Giúp frontend dev chọn đúng API

---

## 🚀 **HÀNH ĐỘNG TIẾP THEO**

### **Cho Frontend Developer:**
1. ✅ Sử dụng endpoint mới `GET /venues/{venueId}/courts`
2. ✅ Chỉ dùng `courts/availability` khi thực sự cần booked slots
3. ✅ Xóa code workaround cũ (filter ở client)

### **Cho Backend Developer:**
1. ⚠️ Cân nhắc fix `GET /courts` để có ApiResponse wrapper (tính nhất quán)
2. ⚠️ Hoặc ít nhất ghi chú rõ trong API doc

### **Cho Technical Writer:**
1. ✅ Merge nội dung file này vào `API_DOCUMENTATION_FOR_FRONTEND.md`
2. ✅ Thêm section 16.1 vào đúng vị trí
3. ✅ Cập nhật Table of Contents

---

**Ngày tạo:** 05/11/2025  
**Người tạo:** AI Assistant  
**Mục đích:** Bổ sung endpoint còn thiếu và kiểm tra JSON response chuẩn

