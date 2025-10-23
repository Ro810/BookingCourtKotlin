# BookingCourt - Ứng dụng đặt sân thể thao

Ứng dụng mobile Android cho phép người dùng đặt sân thể thao (cầu lông, bóng bàn, tennis, bóng đá, bóng rổ, bóng chuyền).

## Công nghệ sử dụng

-   **Language**: Kotlin 2.1.0
-   **UI Framework**: Jetpack Compose (BOM 2024.10.01)
-   **Architecture**: Clean Architecture (Data / Domain / Presentation)
-   **State Management**: MVI Pattern (Model-View-Intent)
-   **Dependency Injection**: Hilt 2.51.1
-   **Database**: Room 2.6.1
-   **Network**: Retrofit 2.11.0 + OkHttp 4.12.0
-   **Navigation**: Navigation Compose 2.8.3
-   **Image Loading**: Coil 2.7.0
-   **Code Formatting**: Spotless + ktlint 1.0.1

## Yêu cầu

-   Android Studio Ladybug | 2024.2.1 hoặc mới hơn
-   JDK 17
-   Android SDK 35
-   Minimum SDK 26 (Android 8.0)

## Cài đặt

### 1. Clone dự án

```bash
git clone <repository-url>
cd BookingCourtKotlin
```

### 2. Cài đặt Git Hooks (quan trọng!)

```bash
./install-hooks.sh
```

Script này sẽ cài đặt pre-commit hook để tự động format code trước mỗi lần commit.

### 3. Build dự án

```bash
./gradlew build
```

### 4. Chạy ứng dụng

Mở dự án trong Android Studio và chạy trên emulator hoặc thiết bị thật.

## Cấu trúc dự án

```
app/src/main/java/com/example/bookingcourt/
├── core/                          # Core functionality
│   ├── common/                    # Base classes, Resources
│   ├── di/                        # Dependency Injection (Hilt modules)
│   ├── navigation/                # Navigation setup
│   ├── network/                   # Network interceptors, monitors
│   └── utils/                     # Constants, Extensions
├── data/                          # Data layer
│   ├── local/                     # Room database, DataStore
│   │   ├── dao/                   # Data Access Objects
│   │   ├── entity/                # Room entities
│   │   ├── database/              # Database setup
│   │   └── datastore/             # DataStore preferences
│   ├── remote/                    # API layer
│   │   ├── api/                   # Retrofit API interfaces
│   │   └── dto/                   # Data Transfer Objects
│   └── repository/                # Repository implementations
├── domain/                        # Domain layer
│   ├── model/                     # Domain models
│   └── repository/                # Repository interfaces
└── presentation/                  # Presentation layer (UI)
    ├── auth/                      # Authentication screens
    ├── booking/                   # Booking screens
    ├── court/                     # Court screens
    ├── home/                      # Home screens
    ├── payment/                   # Payment screens
    ├── profile/                   # Profile screens
    ├── settings/                  # Settings screens
    └── theme/                     # Theme, colors, typography
```

## Tài liệu

-   **[ARCHITECTURE.md](ARCHITECTURE.md)** - Hướng dẫn chi tiết về kiến trúc, pattern, và best practices
-   **[CODE_FORMATTING.md](CODE_FORMATTING.md)** - Hướng dẫn về code formatting và Git hooks

## Code Formatting

Dự án sử dụng Spotless + ktlint để đảm bảo code consistency.

### Format toàn bộ code:

```bash
./gradlew spotlessApply
```

### Kiểm tra format:

```bash
./gradlew spotlessCheck
```

**Lưu ý**: Pre-commit hook sẽ tự động format code trước mỗi lần commit nếu bạn đã chạy `./install-hooks.sh`.

## Git Workflow

1. Tạo branch mới từ `main`:

```bash
git checkout -b feature/ten-tinh-nang
```

2. Code và commit (code sẽ tự động được format):

```bash
git add .
git commit -m "feat: thêm tính năng xyz"
```

3. Push và tạo Pull Request:

```bash
git push origin feature/ten-tinh-nang
```

## Quy tắc Commit Message

Sử dụng conventional commits:

-   `feat:` - Tính năng mới
-   `fix:` - Sửa bug
-   `refactor:` - Refactor code
-   `style:` - Format code, styling
-   `docs:` - Cập nhật documentation
-   `test:` - Thêm hoặc sửa tests
-   `chore:` - Cập nhật dependencies, config

Ví dụ:

```
feat: thêm màn hình đặt sân
fix: sửa lỗi hiển thị giá sân
refactor: cải thiện cấu trúc BookingViewModel
```

## Chạy Tests

```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

## Build Release

```bash
./gradlew assembleRelease
```

APK sẽ được tạo tại: `app/build/outputs/apk/release/app-release.apk`

## API Configuration

Cập nhật base URL trong `core/utils/Constants.kt`:

```kotlin
object Constants {
    const val BASE_URL = "https://your-api.com/api/v1/"
}
```
