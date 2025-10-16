# BookingCourt - Clean Architecture Development Guide

## Table of Contents
1. [Tổng Quan Dự Án](#1-tổng-quan-dự-án)
2. [Cấu Trúc Dự Án Chi Tiết](#2-cấu-trúc-dự-án-chi-tiết)
3. [Hướng Dẫn Navigation](#3-hướng-dẫn-navigation)
4. [Hướng Dẫn Call API](#4-hướng-dẫn-call-api)
5. [Quản Lý State với MVI Pattern](#5-quản-lý-state-với-mvi-pattern)
6. [Dependency Injection với Hilt](#6-dependency-injection-với-hilt)
7. [ViewModel Best Practices](#7-viewmodel-best-practices)
8. [Compose UI Guidelines](#8-compose-ui-guidelines)
9. [Database với Room](#9-database-với-room)
10. [DataStore cho User Preferences](#10-datastore-cho-user-preferences)
11. [Conventions và Best Practices](#11-conventions-và-best-practices)
12. [Ví Dụ Thực Tế: Tạo Feature Hoàn Chỉnh](#12-ví-dụ-thực-tế-tạo-feature-hoàn-chỉnh)

---

## 1. Tổng Quan Dự Án

### 1.1. Kiến Trúc Clean Architecture

Dự án **BookingCourt** được xây dựng theo kiến trúc **Clean Architecture**, kết hợp với **MVVM** và **MVI pattern** cho Presentation Layer. Kiến trúc này chia ứng dụng thành 3 layers chính:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  (UI, Screens, ViewModels, Compose Components)             │
│  • Jetpack Compose UI                                       │
│  • ViewModels với MVI Pattern                              │
│  • State Management với StateFlow/SharedFlow               │
└─────────────────────────────────────────────────────────────┘
                          ↕
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                            │
│  (Business Logic, Use Cases, Models, Repositories)         │
│  • Pure Kotlin Models                                       │
│  • Repository Interfaces                                    │
│  • Use Cases (Business Logic)                              │
└─────────────────────────────────────────────────────────────┘
                          ↕
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
│  (APIs, Database, DataStore, Repository Implementations)   │
│  • Retrofit API Services                                    │
│  • Room Database                                            │
│  • DataStore Preferences                                    │
│  • DTOs và Mappers                                          │
└─────────────────────────────────────────────────────────────┘
```

### 1.2. Dependency Flow

Nguyên tắc **Dependency Rule**: Dependencies chỉ đi từ ngoài vào trong. Các layer bên trong không biết gì về layer bên ngoài.

```
Presentation → Domain ← Data
```

- **Presentation** phụ thuộc vào **Domain**
- **Data** phụ thuộc vào **Domain**
- **Domain** không phụ thuộc vào bất kỳ layer nào (Pure Kotlin)

### 1.3. Data Flow

Flow của data trong ứng dụng:

```
UI (Composable)
    ↓ (User Event)
ViewModel
    ↓ (Intent)
Repository (Domain Interface)
    ↓
Repository Implementation (Data Layer)
    ↓
API / Database / DataStore
    ↓ (Flow/Resource)
ViewModel (State Update)
    ↓ (StateFlow)
UI (Recomposition)
```

### 1.4. Technology Stack

| Category | Technology |
|----------|-----------|
| UI | Jetpack Compose + Material Design 3 |
| Architecture | Clean Architecture + MVVM + MVI |
| DI | Hilt (Dagger) |
| Networking | Retrofit + OkHttp + Gson |
| Database | Room |
| Preferences | DataStore |
| Async | Coroutines + Flow |
| Navigation | Navigation Compose |
| Image Loading | Coil |
| Date/Time | kotlinx-datetime |

### 1.5. Package Structure Overview

```
com.example.bookingcourt/
├── core/                          # Core utilities và shared components
│   ├── common/                    # Common classes (Resource, BaseViewModel, UiEvent)
│   ├── di/                        # Dependency Injection modules
│   ├── navigation/                # Navigation setup
│   ├── network/                   # Network interceptors
│   └── utils/                     # Utility functions và constants
├── data/                          # Data Layer
│   ├── local/                     # Local data sources
│   │   ├── dao/                   # Room DAOs
│   │   ├── database/              # Room Database setup
│   │   ├── datastore/             # DataStore implementation
│   │   └── entity/                # Room Entities
│   ├── remote/                    # Remote data sources
│   │   ├── api/                   # Retrofit API interfaces
│   │   ├── dto/                   # Data Transfer Objects
│   │   └── mapper/                # DTO to Domain mappers
│   └── repository/                # Repository implementations
├── domain/                        # Domain Layer (Pure Kotlin)
│   ├── model/                     # Domain models
│   ├── repository/                # Repository interfaces
│   └── usecase/                   # Use cases (business logic)
└── presentation/                  # Presentation Layer
    ├── auth/                      # Authentication feature
    │   ├── component/             # Reusable UI components
    │   ├── screen/                # Composable screens
    │   └── viewmodel/             # ViewModels
    ├── court/                     # Court feature
    ├── booking/                   # Booking feature
    ├── payment/                   # Payment feature
    ├── profile/                   # Profile feature
    ├── home/                      # Home feature
    ├── components/                # Shared UI components
    └── theme/                     # Theme configuration
```

---

## 2. Cấu Trúc Dự Án Chi Tiết

### 2.1. Core Layer

#### 2.1.1. core/common/

Chứa các class cơ bản được sử dụng xuyên suốt dự án.

**Resource.kt** - Wrapper cho API responses:
```kotlin
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
```

**Mục đích:**
- Wrap API responses với các states: Success, Error, Loading
- Cung cấp consistent way để handle network responses
- Dễ dàng observe và react trong UI

**BaseViewModel.kt** - Base class cho tất cả ViewModels với MVI pattern:
```kotlin
abstract class BaseViewModel<State, Event, Effect> : ViewModel() {

    private val initialState: State by lazy { createInitialState() }

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _effect: MutableSharedFlow<Effect> = MutableSharedFlow()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    val currentState: State
        get() = _uiState.value

    abstract fun createInitialState(): State
    abstract fun handleEvent(event: Event)

    protected fun setState(reduce: State.() -> State) {
        _uiState.value = currentState.reduce()
    }

    protected fun setEffect(builder: () -> Effect) {
        viewModelScope.launch {
            _effect.emit(builder())
        }
    }

    protected fun launchCatching(
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit
    ) {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            onError(throwable)
        }
        viewModelScope.launch(errorHandler, block = block)
    }
}
```

**Giải thích:**
- **State**: Represents UI state (immutable data class)
- **Event**: User actions/intents
- **Effect**: One-time events (navigation, show toast, etc.)
- **setState()**: Update state immutably
- **setEffect()**: Emit one-time effects
- **launchCatching()**: Safe coroutine launch with error handling

**UiEvent.kt** - Sealed interface cho UI events:
```kotlin
sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class ShowToast(val message: String) : UiEvent
    data object NavigateUp : UiEvent
    data class NavigateTo(val route: String) : UiEvent
}
```

#### 2.1.2. core/di/

**NetworkModule.kt** - Cấu hình networking:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        userPreferencesDataStore: UserPreferencesDataStore
    ): AuthInterceptor = AuthInterceptor(userPreferencesDataStore)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    // Tương tự cho các API khác...
}
```

**DatabaseModule.kt** - Cấu hình Room Database:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBookingCourtDatabase(
        @ApplicationContext context: Context
    ): BookingCourtDatabase = Room.databaseBuilder(
        context,
        BookingCourtDatabase::class.java,
        Constants.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideCourtDao(database: BookingCourtDatabase): CourtDao =
        database.courtDao()

    // Tương tự cho các DAO khác...
}
```

**AppModule.kt** - Cấu hình app-level dependencies:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
```

**RepositoryModule.kt** - Bind repository implementations:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    // Bind các repositories khác...
}
```

**WHY sử dụng @Binds thay vì @Provides?**
- `@Binds` hiệu quả hơn về performance (tạo less code)
- Chỉ dùng cho interface → implementation mapping
- `@Provides` dùng khi cần custom logic trong provider

#### 2.1.3. core/network/

**AuthInterceptor.kt** - Tự động thêm token vào requests:
```kotlin
class AuthInterceptor @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking {
            userPreferencesDataStore.accessToken.first()
        }

        val request = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
```

**Lưu ý:**
- Sử dụng `runBlocking` là acceptable trong interceptor context
- Token được lấy từ DataStore
- Tự động attach vào mọi request

### 2.2. Domain Layer

Domain layer là **pure Kotlin**, không có dependencies vào Android framework. Đây là business logic core của app.

#### 2.2.1. domain/model/

Domain models là **immutable data classes** đại diện cho business entities.

**User.kt**:
```kotlin
data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val avatar: String?,
    val role: UserRole,
    val isVerified: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val favoriteCourtIds: List<String> = emptyList(),
    val playingLevel: PlayingLevel? = null,
    val preferredSports: List<SportType> = emptyList()
)

enum class UserRole {
    CUSTOMER,
    COURT_OWNER,
    ADMIN
}

enum class PlayingLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    PROFESSIONAL
}
```

**Court.kt**:
```kotlin
data class Court(
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val images: List<String>,
    val sportType: SportType,
    val courtType: CourtType,
    val pricePerHour: Long,
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val amenities: List<Amenity>,
    val rules: String?,
    val ownerId: String,
    val rating: Float,
    val totalReviews: Int,
    val isActive: Boolean,
    val maxPlayers: Int
)

enum class SportType {
    BADMINTON,
    TABLE_TENNIS,
    TENNIS,
    FOOTBALL,
    BASKETBALL,
    VOLLEYBALL
}

enum class CourtType {
    INDOOR,
    OUTDOOR,
    COVERED
}
```

**Best Practices cho Domain Models:**
- Luôn immutable (val, not var)
- Không có business logic (chỉ data)
- Không có Android dependencies
- Sử dụng meaningful types (không dùng primitive obsession)
- Sử dụng sealed classes cho states, enums cho fixed values

#### 2.2.2. domain/repository/

Repository interfaces define contract cho data operations. Implementation ở Data layer.

**AuthRepository.kt**:
```kotlin
interface AuthRepository {
    suspend fun login(username: String, password: String): Flow<Resource<User>>
    suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String,
        phone: String
    ): Flow<Resource<User>>
    suspend fun logout(): Flow<Resource<Unit>>
    suspend fun forgotPassword(email: String): Flow<Resource<Unit>>
    suspend fun getCurrentUser(): Flow<Resource<User?>>
    suspend fun updateProfile(user: User): Flow<Resource<User>>
    suspend fun saveUserSession(user: User)
    suspend fun clearUserSession()
    fun isLoggedIn(): Flow<Boolean>
}
```

**WHY return Flow<Resource<T>>?**
- Flow cho phép observe changes
- Resource wrap cả Success/Error/Loading states
- Dễ handle trong ViewModel

#### 2.2.3. domain/usecase/ (Optional nhưng Recommended)

Use Cases encapsulate business logic. Mỗi use case làm **một việc duy nhất**.

**Ví dụ Structure:**
```kotlin
// domain/usecase/auth/LoginUseCase.kt
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String
    ): Flow<Resource<User>> {
        // Validation logic
        if (username.isBlank()) {
            return flow { emit(Resource.Error("Username không được để trống")) }
        }
        if (password.length < Constants.MIN_PASSWORD_LENGTH) {
            return flow {
                emit(Resource.Error("Password phải có ít nhất ${Constants.MIN_PASSWORD_LENGTH} ký tự"))
            }
        }

        // Call repository
        return authRepository.login(username, password)
    }
}
```

**WHEN to use Use Cases?**
- ✅ Khi có business logic phức tạp
- ✅ Khi cần combine multiple repository calls
- ✅ Khi cần validation logic
- ❌ Không cần nếu chỉ là simple pass-through

### 2.3. Data Layer

Data layer chịu trách nhiệm cung cấp data cho Domain layer.

#### 2.3.1. data/remote/

**API Interfaces:**
```kotlin
// data/remote/api/AuthApi.kt
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequestDto): RefreshTokenResponseDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<BaseResponseDto>

    @POST("auth/logout")
    suspend fun logout(): BaseResponseDto
}
```

**DTOs (Data Transfer Objects):**
```kotlin
// data/remote/dto/LoginRequest.kt
data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

// data/remote/dto/LoginResponse.kt
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: LoginData? = null
) {
    fun toUser(): User? {
        return data?.user?.let { userResponse ->
            User(
                id = userResponse.id,
                email = userResponse.email,
                fullName = userResponse.fullName,
                phoneNumber = userResponse.phone,
                avatar = userResponse.avatar,
                role = when (userResponse.role.uppercase()) {
                    "ADMIN" -> UserRole.ADMIN
                    "COURT_OWNER" -> UserRole.COURT_OWNER
                    else -> UserRole.CUSTOMER
                },
                isVerified = userResponse.isActive,
                createdAt = Instant.fromEpochMilliseconds(userResponse.createdAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                updatedAt = Instant.fromEpochMilliseconds(userResponse.createdAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            )
        }
    }
}

data class LoginData(
    @SerializedName("user")
    val user: UserResponse,
    @SerializedName("token")
    val token: String
)
```

**Best Practices cho DTOs:**
- Chỉ dùng cho network layer
- Có extension function `toXXX()` để map sang Domain model
- Sử dụng `@SerializedName` annotation
- Default values cho nullable fields

#### 2.3.2. data/local/

**Room Entities:**
```kotlin
// data/local/entity/UserEntity.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "full_name")
    val fullName: String,
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    @ColumnInfo(name = "avatar")
    val avatar: String?,
    @ColumnInfo(name = "role")
    val role: String,
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "favorite_court_ids")
    val favoriteCourtIds: List<String>,
    @ColumnInfo(name = "playing_level")
    val playingLevel: String?,
    @ColumnInfo(name = "preferred_sports")
    val preferredSports: List<String>
)
```

**DAOs:**
```kotlin
// data/local/dao/CourtDao.kt
@Dao
interface CourtDao {
    @Query("SELECT * FROM courts")
    fun getAllCourts(): Flow<List<CourtEntity>>

    @Query("SELECT * FROM courts WHERE sport_type = :sportType")
    fun getCourtsBySportType(sportType: String): Flow<List<CourtEntity>>

    @Query("SELECT * FROM courts WHERE id = :courtId")
    suspend fun getCourtById(courtId: String): CourtEntity?

    @Query("SELECT * FROM courts WHERE id = :courtId")
    fun getCourtByIdFlow(courtId: String): Flow<CourtEntity?>

    @Query("SELECT * FROM courts WHERE is_favorite = 1")
    fun getFavoriteCourts(): Flow<List<CourtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourt(court: CourtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourts(courts: List<CourtEntity>)

    @Update
    suspend fun updateCourt(court: CourtEntity)

    @Query("UPDATE courts SET is_favorite = :isFavorite WHERE id = :courtId")
    suspend fun updateFavoriteStatus(courtId: String, isFavorite: Boolean)

    @Query("DELETE FROM courts WHERE id = :courtId")
    suspend fun deleteCourtById(courtId: String)

    @Query("DELETE FROM courts")
    suspend fun deleteAllCourts()

    @Query("DELETE FROM courts WHERE cached_at < :timestamp")
    suspend fun deleteOldCachedCourts(timestamp: Long)

    @Query("SELECT * FROM courts WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    fun searchCourts(query: String): Flow<List<CourtEntity>>
}
```

**Database Setup:**
```kotlin
// data/local/database/BookingCourtDatabase.kt
@Database(
    entities = [
        UserEntity::class,
        CourtEntity::class,
        BookingEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BookingCourtDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courtDao(): CourtDao
    abstract fun bookingDao(): BookingDao
}
```

#### 2.3.3. data/repository/

Repository Implementation làm cầu nối giữa API và Database.

**AuthRepositoryImpl.kt**:
```kotlin
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : AuthRepository {

    override suspend fun login(username: String, password: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())
            val response = authApi.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null && loginResponse.success) {
                    val user = loginResponse.toUser()
                    if (user != null) {
                        saveUserSession(user)
                        emit(Resource.Success(user))
                    } else {
                        emit(Resource.Error("Failed to parse user data"))
                    }
                } else {
                    emit(Resource.Error(loginResponse?.message ?: "Đăng nhập thất bại"))
                }
            } else {
                emit(Resource.Error("Lỗi kết nối: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun saveUserSession(user: User) {
        userPreferencesDataStore.saveUser(user)
    }

    override suspend fun clearUserSession() {
        userPreferencesDataStore.clearUser()
    }

    override fun isLoggedIn(): Flow<Boolean> = userPreferencesDataStore.isLoggedIn
}
```

**Pattern: Single Source of Truth với Cache**
```kotlin
// Ví dụ Repository với caching strategy
class CourtRepositoryImpl @Inject constructor(
    private val courtApi: CourtApi,
    private val courtDao: CourtDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CourtRepository {

    override fun getCourts(
        sportType: SportType?,
        forceRefresh: Boolean
    ): Flow<Resource<List<Court>>> = flow {
        // Emit loading với cached data
        emit(Resource.Loading())

        // Load from database first
        val cachedCourts = courtDao.getAllCourts().first()
        if (cachedCourts.isNotEmpty() && !forceRefresh) {
            emit(Resource.Success(cachedCourts.map { it.toDomain() }))
        }

        // Fetch from network
        try {
            val response = courtApi.getCourts(
                page = 0,
                size = 20,
                sportType = sportType?.name
            )

            // Save to database
            val courtEntities = response.data.map { it.toEntity() }
            courtDao.insertCourts(courtEntities)

            // Emit success
            emit(Resource.Success(courtEntities.map { it.toDomain() }))
        } catch (e: Exception) {
            // If network fails but we have cache, use it
            if (cachedCourts.isNotEmpty()) {
                emit(Resource.Success(cachedCourts.map { it.toDomain() }))
            } else {
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }.flowOn(ioDispatcher)
}
```

### 2.4. Presentation Layer

#### 2.4.1. Feature Structure

Mỗi feature được tổ chức theo structure:
```
feature_name/
├── component/      # Reusable UI components cho feature này
├── screen/         # Composable screens
└── viewmodel/      # ViewModels
```

---

## 3. Hướng Dẫn Navigation

### 3.1. Định nghĩa Routes trong Screen.kt

**Screen.kt** sử dụng sealed class để type-safe routes:

```kotlin
// core/navigation/Screen.kt
sealed class Screen(val route: String) {
    // Simple routes (no parameters)
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Home : Screen("home")

    // Routes with optional parameters
    data object CourtList : Screen("court_list?sportType={sportType}") {
        fun createRoute(sportType: String? = null) = if (sportType != null) {
            "court_list?sportType=$sportType"
        } else {
            "court_list"
        }
    }

    // Routes with required parameters
    data object CourtDetail : Screen("court_detail/{courtId}") {
        fun createRoute(courtId: String) = "court_detail/$courtId"
    }

    data object Booking : Screen("booking/{courtId}") {
        fun createRoute(courtId: String) = "booking/$courtId"
    }
}

// Route groups
object Route {
    const val AUTH = "auth"
    const val MAIN = "main"
}
```

**Best Practices:**
- ✅ Sử dụng `data object` cho routes
- ✅ Tạo helper function `createRoute()` cho routes có parameters
- ✅ Required parameters dùng `{param}`, optional dùng `?param={param}`
- ✅ Group related routes với nested navigation

### 3.2. Setup Navigation Graph

**NavigationGraph.kt**:
```kotlin
@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Simple screen without parameters
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Nested navigation for auth flow
        navigation(
            route = Route.AUTH,
            startDestination = Screen.Login.route
        ) {
            composable(route = Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Route.MAIN) {
                            popUpTo(Route.AUTH) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = Screen.Register.route) {
                RegisterScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }

        // Screen with required parameter
        composable(
            route = Screen.CourtDetail.route,
            arguments = listOf(
                navArgument("courtId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val courtId = backStackEntry.arguments?.getString("courtId") ?: ""
            CourtDetailScreen(
                courtId = courtId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToBooking = {
                    navController.navigate(
                        Screen.Booking.createRoute(courtId)
                    )
                }
            )
        }

        // Screen with optional parameter
        composable(
            route = Screen.CourtList.route,
            arguments = listOf(
                navArgument("sportType") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val sportType = backStackEntry.arguments?.getString("sportType")
            CourtListScreen(
                sportType = sportType,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToCourtDetail = { courtId ->
                    navController.navigate(
                        Screen.CourtDetail.createRoute(courtId)
                    )
                }
            )
        }
    }
}
```

### 3.3. Navigate giữa các Screens

**Trong Composable:**
```kotlin
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Navigate to detail
    Button(
        onClick = {
            navController.navigate(Screen.CourtDetail.createRoute("court_123"))
        }
    ) {
        Text("View Court Detail")
    }

    // Navigate with options
    Button(
        onClick = {
            navController.navigate(Screen.Profile.route) {
                // Remove screens from back stack
                popUpTo(Screen.Home.route) {
                    inclusive = false
                }
                // Avoid multiple copies
                launchSingleTop = true
            }
        }
    ) {
        Text("Go to Profile")
    }

    // Navigate back
    Button(onClick = { navController.navigateUp() }) {
        Text("Back")
    }
}
```

**Trong ViewModel (với UiEvent):**
```kotlin
// ViewModel
sealed interface CourtDetailEffect {
    data class NavigateToBooking(val courtId: String) : CourtDetailEffect
    object NavigateBack : CourtDetailEffect
}

class CourtDetailViewModel @Inject constructor() : ViewModel() {
    private val _effect = MutableSharedFlow<CourtDetailEffect>()
    val effect = _effect.asSharedFlow()

    fun onBookNowClick(courtId: String) {
        viewModelScope.launch {
            _effect.emit(CourtDetailEffect.NavigateToBooking(courtId))
        }
    }
}

// Composable
@Composable
fun CourtDetailScreen(
    courtId: String,
    navController: NavHostController,
    viewModel: CourtDetailViewModel = hiltViewModel()
) {
    // Observe effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CourtDetailEffect.NavigateToBooking -> {
                    navController.navigate(
                        Screen.Booking.createRoute(effect.courtId)
                    )
                }
                CourtDetailEffect.NavigateBack -> {
                    navController.navigateUp()
                }
            }
        }
    }

    // UI...
}
```

### 3.4. Truyền Complex Objects

**⚠️ KHÔNG nên pass complex objects qua navigation arguments!**

**❌ BAD:**
```kotlin
// DON'T DO THIS
navController.navigate("detail/${Gson().toJson(court)}")
```

**✅ GOOD: Chỉ pass ID, fetch data trong destination screen**
```kotlin
// Pass only ID
navController.navigate(Screen.CourtDetail.createRoute(courtId))

// Fetch in destination
@Composable
fun CourtDetailScreen(
    courtId: String,
    viewModel: CourtDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(courtId) {
        viewModel.loadCourt(courtId)
    }
    // ...
}
```

**Alternative: SharedViewModel**
```kotlin
// For temporary data sharing within same navigation graph
@Composable
fun ParentNavigation(
    navController: NavHostController
) {
    // Shared ViewModel scoped to navigation graph
    val sharedViewModel: SharedDataViewModel = hiltViewModel(
        viewModelStoreOwner = rememberNavController()
    )

    NavigationGraph(navController, sharedViewModel)
}
```

### 3.5. Handle Back Stack

**Clear back stack khi logout:**
```kotlin
Button(
    onClick = {
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true } // Clear everything
        }
    }
) {
    Text("Logout")
}
```

**Navigate và clear intermediate screens:**
```kotlin
// From: Home -> List -> Detail -> Booking
// Navigate to History và clear back stack về Home
navController.navigate(Screen.BookingHistory.route) {
    popUpTo(Screen.Home.route) // Keep Home, clear List -> Detail -> Booking
}
```

---

## 4. Hướng Dẫn Call API

### 4.1. Định nghĩa API Service với Retrofit

**Step 1: Tạo API Interface**
```kotlin
// data/remote/api/CourtApi.kt
interface CourtApi {
    @GET("courts")
    suspend fun getCourts(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sport_type") sportType: String? = null,
        @Query("search") search: String? = null,
        @Query("min_price") minPrice: Long? = null,
        @Query("max_price") maxPrice: Long? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radius") radius: Double? = null
    ): CourtListResponseDto

    @GET("courts/{id}")
    suspend fun getCourtById(@Path("id") courtId: String): CourtDto

    @POST("courts")
    suspend fun createCourt(@Body request: CreateCourtRequest): Response<CourtDto>

    @PUT("courts/{id}")
    suspend fun updateCourt(
        @Path("id") courtId: String,
        @Body request: UpdateCourtRequest
    ): Response<CourtDto>

    @DELETE("courts/{id}")
    suspend fun deleteCourt(@Path("id") courtId: String): Response<BaseResponseDto>

    @GET("courts/{id}/time-slots")
    suspend fun getTimeSlots(
        @Path("id") courtId: String,
        @Query("date") date: String
    ): List<TimeSlotDto>

    @Multipart
    @POST("courts/{id}/images")
    suspend fun uploadCourtImage(
        @Path("id") courtId: String,
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponseDto>
}
```

**Annotations Guide:**
- `@GET`, `@POST`, `@PUT`, `@DELETE` - HTTP methods
- `@Path` - Path parameters (required)
- `@Query` - Query parameters (?key=value)
- `@Body` - Request body
- `@Header` - Custom headers
- `@Multipart` + `@Part` - File uploads

### 4.2. Tạo DTOs

**Request DTO:**
```kotlin
// data/remote/dto/CreateCourtRequest.kt
data class CreateCourtRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("sport_type")
    val sportType: String,
    @SerializedName("price_per_hour")
    val pricePerHour: Long,
    @SerializedName("open_time")
    val openTime: String, // "HH:mm"
    @SerializedName("close_time")
    val closeTime: String,
    @SerializedName("amenities")
    val amenities: List<String>
)
```

**Response DTO:**
```kotlin
// data/remote/dto/CourtDto.kt
data class CourtDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("images")
    val images: List<String>?,
    @SerializedName("sport_type")
    val sportType: String,
    @SerializedName("court_type")
    val courtType: String,
    @SerializedName("price_per_hour")
    val pricePerHour: Long,
    @SerializedName("open_time")
    val openTime: String,
    @SerializedName("close_time")
    val closeTime: String,
    @SerializedName("owner_id")
    val ownerId: String,
    @SerializedName("rating")
    val rating: Float,
    @SerializedName("total_reviews")
    val totalReviews: Int,
    @SerializedName("is_active")
    val isActive: Boolean
)
```

**List Response Wrapper:**
```kotlin
// data/remote/dto/CourtListResponseDto.kt
data class CourtListResponseDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<CourtDto>,
    @SerializedName("pagination")
    val pagination: PaginationDto?
)

data class PaginationDto(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("page_size")
    val pageSize: Int,
    @SerializedName("total_items")
    val totalItems: Int
)
```

### 4.3. Map DTO sang Domain Model

**Extension Function Approach:**
```kotlin
// data/remote/dto/CourtDto.kt (continued)
fun CourtDto.toDomain(): Court {
    return Court(
        id = id,
        name = name,
        description = description,
        address = address,
        latitude = latitude,
        longitude = longitude,
        images = images ?: emptyList(),
        sportType = SportType.valueOf(sportType.uppercase()),
        courtType = CourtType.valueOf(courtType.uppercase()),
        pricePerHour = pricePerHour,
        openTime = LocalTime.parse(openTime), // "HH:mm"
        closeTime = LocalTime.parse(closeTime),
        amenities = emptyList(), // TODO: Map from API
        rules = null,
        ownerId = ownerId,
        rating = rating,
        totalReviews = totalReviews,
        isActive = isActive,
        maxPlayers = 4 // Default or from API
    )
}

// Reverse mapping (Domain -> DTO for POST/PUT)
fun Court.toCreateRequest(): CreateCourtRequest {
    return CreateCourtRequest(
        name = name,
        description = description,
        address = address,
        latitude = latitude,
        longitude = longitude,
        sportType = sportType.name.lowercase(),
        pricePerHour = pricePerHour,
        openTime = openTime.toString(),
        closeTime = closeTime.toString(),
        amenities = amenities.map { it.id }
    )
}
```

**Mapper Class Approach (for complex mappings):**
```kotlin
// data/remote/mapper/CourtMapper.kt
class CourtMapper @Inject constructor() {
    fun toDomain(dto: CourtDto): Court {
        return Court(
            // ... mapping logic
        )
    }

    fun toDomainList(dtos: List<CourtDto>): List<Court> {
        return dtos.map { toDomain(it) }
    }

    fun toDto(domain: Court): CourtDto {
        return CourtDto(
            // ... mapping logic
        )
    }
}
```

### 4.4. Implement Repository

**Full Repository Implementation:**
```kotlin
// data/repository/CourtRepositoryImpl.kt
@Singleton
class CourtRepositoryImpl @Inject constructor(
    private val courtApi: CourtApi,
    private val courtDao: CourtDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CourtRepository {

    override fun getCourts(
        page: Int,
        size: Int,
        sportType: SportType?,
        forceRefresh: Boolean
    ): Flow<Resource<List<Court>>> = flow {
        emit(Resource.Loading())

        // Try to load from cache first
        if (!forceRefresh) {
            val cachedCourts = courtDao.getAllCourts().first()
            if (cachedCourts.isNotEmpty()) {
                emit(Resource.Success(cachedCourts.map { it.toDomain() }))
            }
        }

        // Fetch from API
        try {
            val response = courtApi.getCourts(
                page = page,
                size = size,
                sportType = sportType?.name?.lowercase()
            )

            if (response.success) {
                val courts = response.data.map { it.toDomain() }

                // Cache to database
                courtDao.insertCourts(courts.map { it.toEntity() })

                emit(Resource.Success(courts))
            } else {
                emit(Resource.Error("Failed to load courts"))
            }
        } catch (e: Exception) {
            // Network error, try to use cache
            val cachedCourts = courtDao.getAllCourts().first()
            if (cachedCourts.isNotEmpty()) {
                emit(Resource.Success(cachedCourts.map { it.toDomain() }))
            } else {
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }.flowOn(ioDispatcher)

    override fun getCourtById(courtId: String): Flow<Resource<Court>> = flow {
        emit(Resource.Loading())

        try {
            // Try cache first
            val cachedCourt = courtDao.getCourtById(courtId)
            if (cachedCourt != null) {
                emit(Resource.Success(cachedCourt.toDomain()))
            }

            // Fetch from API
            val courtDto = courtApi.getCourtById(courtId)
            val court = courtDto.toDomain()

            // Update cache
            courtDao.insertCourt(court.toEntity())

            emit(Resource.Success(court))
        } catch (e: Exception) {
            // If network fails, try to use cached data
            val cachedCourt = courtDao.getCourtById(courtId)
            if (cachedCourt != null) {
                emit(Resource.Success(cachedCourt.toDomain()))
            } else {
                emit(Resource.Error(e.message ?: "Court not found"))
            }
        }
    }.flowOn(ioDispatcher)

    override suspend fun createCourt(court: Court): Flow<Resource<Court>> = flow {
        emit(Resource.Loading())

        try {
            val response = courtApi.createCourt(court.toCreateRequest())
            if (response.isSuccessful && response.body() != null) {
                val createdCourt = response.body()!!.toDomain()
                courtDao.insertCourt(createdCourt.toEntity())
                emit(Resource.Success(createdCourt))
            } else {
                emit(Resource.Error("Failed to create court"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(ioDispatcher)
}
```

### 4.5. Handle Errors

**Global Error Handling:**
```kotlin
// core/network/ErrorHandler.kt
sealed class ApiError {
    data class NetworkError(val message: String) : ApiError()
    data class ServerError(val code: Int, val message: String) : ApiError()
    data class UnknownError(val throwable: Throwable) : ApiError()
    object Unauthorized : ApiError()
    object NotFound : ApiError()
}

fun Throwable.toApiError(): ApiError {
    return when (this) {
        is UnknownHostException, is SocketTimeoutException -> {
            ApiError.NetworkError("Không có kết nối mạng")
        }
        is HttpException -> {
            when (code()) {
                401 -> ApiError.Unauthorized
                404 -> ApiError.NotFound
                in 500..599 -> ApiError.ServerError(code(), "Lỗi server")
                else -> ApiError.ServerError(code(), message())
            }
        }
        else -> ApiError.UnknownError(this)
    }
}
```

**Use in Repository:**
```kotlin
override fun getCourts(): Flow<Resource<List<Court>>> = flow {
    emit(Resource.Loading())

    try {
        val response = courtApi.getCourts(0, 20)
        emit(Resource.Success(response.data.map { it.toDomain() }))
    } catch (e: Exception) {
        val error = e.toApiError()
        val errorMessage = when (error) {
            is ApiError.NetworkError -> error.message
            is ApiError.ServerError -> "Lỗi server: ${error.message}"
            ApiError.Unauthorized -> "Phiên đăng nhập hết hạn"
            ApiError.NotFound -> "Không tìm thấy dữ liệu"
            is ApiError.UnknownError -> "Đã xảy ra lỗi"
        }
        emit(Resource.Error(errorMessage))
    }
}.flowOn(ioDispatcher)
```

### 4.6. File Upload

**Upload Image:**
```kotlin
suspend fun uploadCourtImage(courtId: String, imageUri: Uri): Flow<Resource<String>> = flow {
    emit(Resource.Loading())

    try {
        val file = uriToFile(imageUri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val response = courtApi.uploadCourtImage(courtId, body)
        if (response.isSuccessful && response.body() != null) {
            val imageUrl = response.body()!!.imageUrl
            emit(Resource.Success(imageUrl))
        } else {
            emit(Resource.Error("Failed to upload image"))
        }
    } catch (e: Exception) {
        emit(Resource.Error(e.message ?: "Upload failed"))
    }
}.flowOn(ioDispatcher)

private fun uriToFile(uri: Uri): File {
    // Convert URI to File
    // Implementation depends on your app
}
```

---

## 5. Quản Lý State với MVI Pattern

### 5.1. MVI Pattern Overview

**MVI (Model-View-Intent) Pattern:**
```
User → Intent → ViewModel → State → View
                    ↓
                  Effect (one-time events)
```

- **State**: Immutable UI state
- **Intent/Event**: User actions
- **Effect**: One-time events (navigation, show toast)

### 5.2. Define State, Intent, Effect

**Ví dụ: CourtListViewModel**
```kotlin
// State - Represents UI state
data class CourtListState(
    val isLoading: Boolean = false,
    val courts: List<Court> = emptyList(),
    val filteredCourts: List<Court> = emptyList(),
    val searchQuery: String = "",
    val selectedSportType: SportType? = null,
    val error: String? = null
)

// Intent - User actions
sealed interface CourtListIntent {
    data class LoadCourts(val sportType: SportType? = null) : CourtListIntent
    data class SearchCourts(val query: String) : CourtListIntent
    data class FilterBySportType(val sportType: SportType?) : CourtListIntent
    data class NavigateToDetail(val courtId: String) : CourtListIntent
    object Refresh : CourtListIntent
}

// Effect - One-time events
sealed interface CourtListEffect {
    data class ShowError(val message: String) : CourtListEffect
    data class NavigateToDetail(val courtId: String) : CourtListEffect
}
```

### 5.3. Implement ViewModel với MVI

**Using BaseViewModel:**
```kotlin
@HiltViewModel
class CourtListViewModel @Inject constructor(
    private val courtRepository: CourtRepository
) : BaseViewModel<CourtListState, CourtListIntent, CourtListEffect>() {

    override fun createInitialState(): CourtListState {
        return CourtListState()
    }

    init {
        handleEvent(CourtListIntent.LoadCourts())
    }

    override fun handleEvent(event: CourtListIntent) {
        when (event) {
            is CourtListIntent.LoadCourts -> loadCourts(event.sportType)
            is CourtListIntent.SearchCourts -> searchCourts(event.query)
            is CourtListIntent.FilterBySportType -> filterBySportType(event.sportType)
            is CourtListIntent.NavigateToDetail -> navigateToDetail(event.courtId)
            CourtListIntent.Refresh -> refresh()
        }
    }

    private fun loadCourts(sportType: SportType?) {
        setState { copy(isLoading = true, selectedSportType = sportType) }

        launchCatching(
            onError = { throwable ->
                setState { copy(isLoading = false, error = throwable.message) }
                setEffect { CourtListEffect.ShowError(throwable.message ?: "Unknown error") }
            }
        ) {
            courtRepository.getCourts(
                page = 0,
                size = 20,
                sportType = sportType
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        setState {
                            copy(
                                isLoading = false,
                                courts = resource.data ?: emptyList(),
                                filteredCourts = filterCourts(
                                    resource.data ?: emptyList(),
                                    currentState.searchQuery,
                                    sportType
                                ),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        setState { copy(isLoading = false, error = resource.message) }
                        setEffect { CourtListEffect.ShowError(resource.message ?: "Error") }
                    }
                    is Resource.Loading -> {
                        setState { copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun searchCourts(query: String) {
        setState {
            copy(
                searchQuery = query,
                filteredCourts = filterCourts(
                    courts,
                    query,
                    selectedSportType
                )
            )
        }
    }

    private fun filterBySportType(sportType: SportType?) {
        setState {
            copy(
                selectedSportType = sportType,
                filteredCourts = filterCourts(
                    courts,
                    searchQuery,
                    sportType
                )
            )
        }
    }

    private fun filterCourts(
        courts: List<Court>,
        query: String,
        sportType: SportType?
    ): List<Court> {
        return courts.filter { court ->
            (query.isEmpty() || court.name.contains(query, ignoreCase = true) ||
                    court.address.contains(query, ignoreCase = true)) &&
                    (sportType == null || court.sportType == sportType)
        }
    }

    private fun navigateToDetail(courtId: String) {
        setEffect { CourtListEffect.NavigateToDetail(courtId) }
    }

    private fun refresh() {
        handleEvent(CourtListIntent.LoadCourts(currentState.selectedSportType))
    }
}
```

**Simpler ViewModel (không dùng BaseViewModel):**
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            authRepository.login(username, password).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _loginState.value = LoginState.Success(result.data!!)
                        _uiEvent.emit(UiEvent.NavigateTo("home"))
                    }
                    is Resource.Error -> {
                        _loginState.value = LoginState.Error(
                            result.message ?: "Đăng nhập thất bại"
                        )
                    }
                    is Resource.Loading -> {
                        _loginState.value = LoginState.Loading
                    }
                }
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
```

### 5.4. Observe State trong Composable

**Collect StateFlow:**
```kotlin
@Composable
fun CourtListScreen(
    viewModel: CourtListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Observe one-time effects
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CourtListEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CourtListEffect.NavigateToDetail -> {
                    // Navigate handled by parent
                }
            }
        }
    }

    CourtListContent(
        state = state,
        onIntent = viewModel::handleEvent
    )
}

@Composable
fun CourtListContent(
    state: CourtListState,
    onIntent: (CourtListIntent) -> Unit
) {
    Column {
        // Search bar
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { query ->
                onIntent(CourtListIntent.SearchCourts(query))
            }
        )

        // Loading indicator
        if (state.isLoading) {
            CircularProgressIndicator()
        }

        // Error message
        state.error?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        // Court list
        LazyColumn {
            items(state.filteredCourts) { court ->
                CourtItem(
                    court = court,
                    onClick = {
                        onIntent(CourtListIntent.NavigateToDetail(court.id))
                    }
                )
            }
        }
    }
}
```

### 5.5. Best Practices cho State Management

**DO:**
- ✅ State phải immutable (data class với val)
- ✅ Sử dụng `copy()` để update state
- ✅ StateFlow cho state, SharedFlow cho effects
- ✅ Collect state với `collectAsStateWithLifecycle()`
- ✅ Handle effects trong LaunchedEffect
- ✅ Separate state update logic vào private functions

**DON'T:**
- ❌ Không mutate state trực tiếp
- ❌ Không expose MutableStateFlow
- ❌ Không dùng LiveData (prefer StateFlow)
- ❌ Không collect Flow trong Composable body (dùng LaunchedEffect)

---

## 6. Dependency Injection với Hilt

### 6.1. Setup Hilt

**Application Class:**
```kotlin
@HiltAndroidApp
class BookingCourtApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
```

**AndroidManifest.xml:**
```xml
<application
    android:name=".BookingCourtApplication"
    ...>
</application>
```

### 6.2. Tạo Modules

**Network Module:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)
}
```

**Repository Module (Abstract):**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCourtRepository(
        courtRepositoryImpl: CourtRepositoryImpl
    ): CourtRepository
}
```

### 6.3. Inject Dependencies

**Constructor Injection (RECOMMENDED):**
```kotlin
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : AuthRepository {
    // Implementation
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    // Implementation
}
```

**Field Injection (Avoid if possible):**
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var someService: SomeService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // someService is now available
    }
}
```

### 6.4. Scoping

**Singleton:**
```kotlin
@Provides
@Singleton
fun provideDatabase(
    @ApplicationContext context: Context
): BookingCourtDatabase {
    return Room.databaseBuilder(
        context,
        BookingCourtDatabase::class.java,
        "database"
    ).build()
}
```

**ActivityScoped:**
```kotlin
@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    @ActivityScoped
    fun provideActivityScopedDependency(): SomeDependency {
        return SomeDependency()
    }
}
```

**ViewModelScoped:**
```kotlin
// ViewModels are automatically scoped
@HiltViewModel
class MyViewModel @Inject constructor(
    // Dependencies here are scoped to ViewModel lifecycle
) : ViewModel()
```

### 6.5. Qualifiers

**Define Qualifiers:**
```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
```

**Provide với Qualifiers:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
```

**Inject với Qualifiers:**
```kotlin
class MyRepository @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) {
    // Use specific dispatchers
}
```

### 6.6. Inject trong Composables

**ViewModels:**
```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) {
    // viewModel is automatically injected
}
```

**Other Dependencies (using EntryPoint):**
```kotlin
@EntryPoint
@InstallIn(SingletonComponent::class)
interface MyComposableEntryPoint {
    fun someService(): SomeService
}

@Composable
fun MyComposable() {
    val entryPoint = EntryPointAccessors.fromApplication(
        LocalContext.current,
        MyComposableEntryPoint::class.java
    )
    val service = entryPoint.someService()
}
```

---

## 7. ViewModel Best Practices

### 7.1. ViewModel Structure

**Good ViewModel Structure:**
```kotlin
@HiltViewModel
class CourtDetailViewModel @Inject constructor(
    private val courtRepository: CourtRepository,
    private val bookingRepository: BookingRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    // State
    private val _uiState = MutableStateFlow(CourtDetailState())
    val uiState: StateFlow<CourtDetailState> = _uiState.asStateFlow()

    // Effects
    private val _uiEffect = MutableSharedFlow<CourtDetailEffect>()
    val uiEffect: SharedFlow<CourtDetailEffect> = _uiEffect.asSharedFlow()

    // Public API
    fun loadCourt(courtId: String) { /* ... */ }
    fun bookCourt() { /* ... */ }
    fun toggleFavorite() { /* ... */ }

    // Private helpers
    private fun handleError(throwable: Throwable) { /* ... */ }
}
```

### 7.2. Handle Lifecycle

**viewModelScope:**
```kotlin
fun loadData() {
    viewModelScope.launch {
        // Automatically cancelled when ViewModel is cleared
        repository.getData().collect { data ->
            _state.value = data
        }
    }
}
```

**Custom CoroutineScope:**
```kotlin
class MyViewModel @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    fun startBackgroundWork() {
        // This continues even after ViewModel is cleared
        applicationScope.launch {
            // Long-running work
        }
    }
}
```

### 7.3. SavedStateHandle

**Save and Restore State:**
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _searchQuery = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")
    val searchQuery: StateFlow<String> = _searchQuery

    fun updateSearchQuery(query: String) {
        savedStateHandle[KEY_SEARCH_QUERY] = query
    }

    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
    }
}
```

### 7.4. Testing ViewModels

**Unit Test Example:**
```kotlin
@ExperimentalCoroutinesTest
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: LoginViewModel
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        authRepository = mockk()
        viewModel = LoginViewModel(authRepository)
    }

    @Test
    fun `login success should update state to Success`() = runTest {
        // Given
        val mockUser = User(/* ... */)
        coEvery { authRepository.login(any(), any()) } returns flow {
            emit(Resource.Success(mockUser))
        }

        // When
        viewModel.login("username", "password")

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is LoginViewModel.LoginState.Success)
        assertEquals(mockUser, (state as LoginViewModel.LoginState.Success).user)
    }

    @Test
    fun `login error should update state to Error`() = runTest {
        // Given
        coEvery { authRepository.login(any(), any()) } returns flow {
            emit(Resource.Error("Invalid credentials"))
        }

        // When
        viewModel.login("username", "wrong_password")

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is LoginViewModel.LoginState.Error)
        assertEquals("Invalid credentials", (state as LoginViewModel.LoginState.Error).message)
    }
}
```

---

## 8. Compose UI Guidelines

### 8.1. Composable Structure

**Screen Composable:**
```kotlin
@Composable
fun CourtDetailScreen(
    courtId: String,
    onNavigateBack: () -> Unit,
    viewModel: CourtDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is CourtDetailEffect.ShowError -> {
                    // Show error
                }
            }
        }
    }

    // Load data
    LaunchedEffect(courtId) {
        viewModel.loadCourt(courtId)
    }

    CourtDetailContent(
        state = state,
        onBackClick = onNavigateBack,
        onBookClick = viewModel::bookCourt
    )
}

@Composable
private fun CourtDetailContent(
    state: CourtDetailState,
    onBackClick: () -> Unit,
    onBookClick: () -> Unit
) {
    // Stateless UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.court?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Content
    }
}
```

### 8.2. State Hoisting

**Hoisted State:**
```kotlin
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search...") }
    )
}

// Usage
@Composable
fun ParentScreen() {
    var searchQuery by remember { mutableStateOf("") }

    SearchBar(
        query = searchQuery,
        onQueryChange = { searchQuery = it }
    )
}
```

**WHY State Hoisting?**
- ✅ Reusable components
- ✅ Testable components
- ✅ Single source of truth
- ✅ Easier to compose

### 8.3. Side Effects

**LaunchedEffect - Run effect when keys change:**
```kotlin
@Composable
fun MyScreen(userId: String) {
    LaunchedEffect(userId) {
        // Runs when userId changes
        loadUserData(userId)
    }
}
```

**DisposableEffect - Clean up resources:**
```kotlin
@Composable
fun ObserveConnectivity() {
    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }

    DisposableEffect(Unit) {
        networkMonitor.start()
        onDispose {
            networkMonitor.stop()
        }
    }
}
```

**rememberUpdatedState - Capture latest value:**
```kotlin
@Composable
fun Timer(onTimeout: () -> Unit) {
    val currentOnTimeout by rememberUpdatedState(onTimeout)

    LaunchedEffect(Unit) {
        delay(5000)
        currentOnTimeout()
    }
}
```

### 8.4. Performance Optimization

**Remember expensive computations:**
```kotlin
@Composable
fun ExpensiveList(items: List<Item>) {
    val sortedItems = remember(items) {
        items.sortedBy { it.name }
    }

    LazyColumn {
        items(sortedItems) { item ->
            ItemRow(item)
        }
    }
}
```

**derivedStateOf - Minimize recompositions:**
```kotlin
@Composable
fun FilteredList(
    items: List<Item>,
    searchQuery: String
) {
    val filteredItems by remember {
        derivedStateOf {
            if (searchQuery.isEmpty()) {
                items
            } else {
                items.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    LazyColumn {
        items(filteredItems) { item ->
            ItemRow(item)
        }
    }
}
```

**key() - Preserve state in lists:**
```kotlin
LazyColumn {
    items(
        items = courts,
        key = { court -> court.id }
    ) { court ->
        CourtItem(court)
    }
}
```

**Immutable/Stable annotations:**
```kotlin
@Immutable
data class Court(
    val id: String,
    val name: String,
    // All properties are immutable
)

@Stable
interface CourtRepository {
    // Interface methods
}
```

### 8.5. Material Design 3 Theming

**Theme.kt:**
```kotlin
@Composable
fun BookingCourtTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

**Use Theme Colors:**
```kotlin
@Composable
fun MyButton() {
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = "Click Me",
            style = MaterialTheme.typography.labelLarge
        )
    }
}
```

---

## 9. Database với Room

### 9.1. Define Entities

**Entity với Relations:**
```kotlin
@Entity(tableName = "courts")
data class CourtEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "sport_type")
    val sportType: String,
    @ColumnInfo(name = "price_per_hour")
    val pricePerHour: Long,
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    val rating: Float,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = CourtEntity::class,
            parentColumns = ["id"],
            childColumns = ["court_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("court_id"), Index("user_id")]
)
data class BookingEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "court_id")
    val courtId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "booking_date")
    val bookingDate: Long,
    @ColumnInfo(name = "start_time")
    val startTime: String,
    @ColumnInfo(name = "end_time")
    val endTime: String,
    val status: String,
    @ColumnInfo(name = "total_price")
    val totalPrice: Long
)
```

### 9.2. Type Converters

**Converters.kt:**
```kotlin
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

    @TypeConverter
    fun toTimestamp(date: LocalDateTime?): Long? {
        return date?.toInstant(TimeZone.currentSystemDefault())?.toEpochMilliseconds()
    }
}
```

### 9.3. DAOs

**Comprehensive DAO:**
```kotlin
@Dao
interface CourtDao {
    // Query
    @Query("SELECT * FROM courts WHERE id = :courtId")
    suspend fun getCourtById(courtId: String): CourtEntity?

    @Query("SELECT * FROM courts WHERE id = :courtId")
    fun getCourtByIdFlow(courtId: String): Flow<CourtEntity?>

    @Query("SELECT * FROM courts ORDER BY name ASC")
    fun getAllCourts(): Flow<List<CourtEntity>>

    @Query("""
        SELECT * FROM courts
        WHERE sport_type = :sportType
        AND is_favorite = :isFavorite
        ORDER BY rating DESC
    """)
    fun getCourtsByTypeAndFavorite(
        sportType: String,
        isFavorite: Boolean
    ): Flow<List<CourtEntity>>

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourt(court: CourtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourts(courts: List<CourtEntity>)

    // Update
    @Update
    suspend fun updateCourt(court: CourtEntity)

    @Query("UPDATE courts SET is_favorite = :isFavorite WHERE id = :courtId")
    suspend fun updateFavoriteStatus(courtId: String, isFavorite: Boolean)

    // Delete
    @Query("DELETE FROM courts WHERE id = :courtId")
    suspend fun deleteCourtById(courtId: String)

    @Query("DELETE FROM courts WHERE cached_at < :timestamp")
    suspend fun deleteOldCachedCourts(timestamp: Long)

    // Transaction
    @Transaction
    @Query("SELECT * FROM courts WHERE id = :courtId")
    suspend fun getCourtWithBookings(courtId: String): CourtWithBookings?
}

// Relation data class
data class CourtWithBookings(
    @Embedded val court: CourtEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "court_id"
    )
    val bookings: List<BookingEntity>
)
```

### 9.4. Database Setup

**Database Class:**
```kotlin
@Database(
    entities = [
        UserEntity::class,
        CourtEntity::class,
        BookingEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BookingCourtDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courtDao(): CourtDao
    abstract fun bookingDao(): BookingDao
}
```

**Migration:**
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE courts ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0"
        )
    }
}

// In DatabaseModule
@Provides
@Singleton
fun provideDatabase(
    @ApplicationContext context: Context
): BookingCourtDatabase = Room.databaseBuilder(
    context,
    BookingCourtDatabase::class.java,
    Constants.DATABASE_NAME
)
    .addMigrations(MIGRATION_1_2)
    .fallbackToDestructiveMigration()
    .build()
```

### 9.5. Repository Pattern với Room

**Combine Remote + Local:**
```kotlin
class CourtRepositoryImpl @Inject constructor(
    private val courtApi: CourtApi,
    private val courtDao: CourtDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CourtRepository {

    override fun getCourts(): Flow<Resource<List<Court>>> = flow {
        // Emit cached data first
        courtDao.getAllCourts().first().let { cached ->
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached.map { it.toDomain() }))
            }
        }

        emit(Resource.Loading())

        try {
            // Fetch from API
            val response = courtApi.getCourts(0, 20)
            val courts = response.data.map { it.toDomain() }

            // Update cache
            courtDao.insertCourts(courts.map { it.toEntity() })

            emit(Resource.Success(courts))
        } catch (e: Exception) {
            // On error, try to use cached data
            val cached = courtDao.getAllCourts().first()
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached.map { it.toDomain() }))
            } else {
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }.flowOn(ioDispatcher)
}
```

---

## 10. DataStore cho User Preferences

### 10.1. Setup DataStore

**In AppModule:**
```kotlin
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore
}
```

### 10.2. UserPreferencesDataStore

**Full Implementation:**
```kotlin
@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {
    // Keys
    private val accessTokenKey = stringPreferencesKey(Constants.PrefsKeys.ACCESS_TOKEN)
    private val refreshTokenKey = stringPreferencesKey(Constants.PrefsKeys.REFRESH_TOKEN)
    private val userIdKey = stringPreferencesKey(Constants.PrefsKeys.USER_ID)
    private val userJsonKey = stringPreferencesKey("user_json")
    private val isLoggedInKey = booleanPreferencesKey(Constants.PrefsKeys.IS_LOGGED_IN)
    private val themeModeKey = stringPreferencesKey(Constants.PrefsKeys.THEME_MODE)

    // Flows
    val accessToken: Flow<String?> = dataStore.data.map { it[accessTokenKey] }
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[isLoggedInKey] ?: false }
    val themeMode: Flow<String> = dataStore.data.map { it[themeModeKey] ?: "system" }

    // Save methods
    suspend fun saveAuthTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            preferences[refreshTokenKey] = refreshToken
            preferences[isLoggedInKey] = true
        }
    }

    suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[userIdKey] = user.id
            preferences[userJsonKey] = gson.toJson(user)
            preferences[isLoggedInKey] = true
        }
    }

    suspend fun getUser(): User? {
        return try {
            val userJson = dataStore.data.first()[userJsonKey]
            userJson?.let { gson.fromJson(it, User::class.java) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = mode
        }
    }

    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.remove(userJsonKey)
            preferences.remove(userIdKey)
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
            preferences[isLoggedInKey] = false
        }
    }

    suspend fun clearAllData() {
        dataStore.edit { it.clear() }
    }
}
```

### 10.3. Use trong Repository

```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : AuthRepository {

    override suspend fun login(username: String, password: String): Flow<Resource<User>> = flow {
        // ... API call ...

        if (response.isSuccessful) {
            val user = response.body()!!.toUser()
            val token = response.body()!!.token

            // Save to DataStore
            userPreferencesDataStore.saveAuthTokens(token, "")
            userPreferencesDataStore.saveUser(user)

            emit(Resource.Success(user))
        }
    }

    override fun isLoggedIn(): Flow<Boolean> =
        userPreferencesDataStore.isLoggedIn
}
```

---

## 11. Conventions và Best Practices

### 11.1. Naming Conventions

**Files:**
- Screen: `LoginScreen.kt`, `CourtDetailScreen.kt`
- ViewModel: `LoginViewModel.kt`, `CourtListViewModel.kt`
- Repository: `AuthRepository.kt`, `AuthRepositoryImpl.kt`
- DTO: `LoginRequest.kt`, `LoginResponse.kt`, `UserDto.kt`
- Entity: `UserEntity.kt`, `CourtEntity.kt`
- Model: `User.kt`, `Court.kt`

**Classes:**
- Interfaces: `AuthRepository`, `CourtRepository`
- Implementations: `AuthRepositoryImpl`, `CourtRepositoryImpl`
- DTOs: `LoginRequest`, `LoginResponse`
- Entities: `UserEntity`, `CourtEntity`
- Models: `User`, `Court`

**Functions:**
- Public: `loadCourts()`, `getCourts()`, `updateProfile()`
- Private: `handleError()`, `filterCourts()`, `mapToEntity()`
- Suspend: `suspend fun login()`, `suspend fun saveCourt()`
- Composable: `@Composable fun LoginScreen()`

**Variables:**
- Mutable State: `_uiState`, `_loginState`
- Public State: `uiState`, `loginState`
- Constants: `CONNECT_TIMEOUT`, `BASE_URL`

### 11.2. Package Organization

**By Feature (RECOMMENDED):**
```
com.example.bookingcourt/
├── core/
├── data/
├── domain/
└── presentation/
    ├── auth/
    │   ├── component/
    │   ├── screen/
    │   └── viewmodel/
    ├── court/
    └── booking/
```

### 11.3. Code Style

**Kotlin Style Guide:**
```kotlin
// Use expression body for single expression functions
fun calculateTotal(price: Long, hours: Int): Long = price * hours

// Use trailing comma in lists
val sports = listOf(
    SportType.BADMINTON,
    SportType.TENNIS,
    SportType.FOOTBALL,
)

// Named arguments for clarity
createCourt(
    name = "Tennis Court 1",
    sportType = SportType.TENNIS,
    pricePerHour = 100000
)

// Use when instead of if-else chains
val message = when (status) {
    BookingStatus.PENDING -> "Pending"
    BookingStatus.CONFIRMED -> "Confirmed"
    BookingStatus.CANCELLED -> "Cancelled"
    else -> "Unknown"
}

// Extension functions for utilities
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

// Scope functions
user?.let {
    saveUser(it)
}

courts.filter { it.isActive }
      .sortedBy { it.pricePerHour }
      .take(10)
```

### 11.4. Error Handling Patterns

**Repository Level:**
```kotlin
override suspend fun getCourt(id: String): Flow<Resource<Court>> = flow {
    emit(Resource.Loading())

    try {
        val court = courtApi.getCourtById(id).toDomain()
        emit(Resource.Success(court))
    } catch (e: Exception) {
        emit(Resource.Error(e.toErrorMessage()))
    }
}.flowOn(ioDispatcher)
```

**ViewModel Level:**
```kotlin
fun loadCourt(courtId: String) {
    viewModelScope.launch {
        courtRepository.getCourt(courtId).collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    setState { copy(court = resource.data, isLoading = false) }
                }
                is Resource.Error -> {
                    setState { copy(error = resource.message, isLoading = false) }
                    setEffect { CourtDetailEffect.ShowError(resource.message ?: "Error") }
                }
                is Resource.Loading -> {
                    setState { copy(isLoading = true) }
                }
            }
        }
    }
}
```

**UI Level:**
```kotlin
@Composable
fun CourtDetailScreen(
    viewModel: CourtDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is CourtDetailEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    when {
        state.isLoading -> LoadingIndicator()
        state.error != null -> ErrorView(state.error!!)
        state.court != null -> CourtContent(state.court!!)
    }
}
```

### 11.5. Testing Strategies

**Unit Tests - ViewModels:**
```kotlin
@ExperimentalCoroutinesTest
class CourtListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CourtListViewModel
    private val courtRepository: CourtRepository = mockk()

    @Test
    fun `loadCourts success updates state with courts`() = runTest {
        // Given
        val mockCourts = listOf(/* mock courts */)
        coEvery { courtRepository.getCourts(any(), any(), any()) } returns flow {
            emit(Resource.Success(mockCourts))
        }

        // When
        viewModel.handleEvent(CourtListIntent.LoadCourts())

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(mockCourts, state.courts)
    }
}
```

**Integration Tests - Repository:**
```kotlin
@RunWith(AndroidJUnit4::class)
class CourtRepositoryImplTest {
    private lateinit var database: BookingCourtDatabase
    private lateinit var courtDao: CourtDao
    private lateinit var repository: CourtRepositoryImpl

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            BookingCourtDatabase::class.java
        ).build()
        courtDao = database.courtDao()
        repository = CourtRepositoryImpl(mockk(), courtDao, Dispatchers.IO)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveCourt_shouldPersistToDatabase() = runTest {
        // Given
        val court = Court(/* mock court */)

        // When
        repository.saveCourt(court)

        // Then
        val savedCourt = courtDao.getCourtById(court.id)
        assertNotNull(savedCourt)
        assertEquals(court.name, savedCourt?.name)
    }
}
```

**UI Tests - Compose:**
```kotlin
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginButton_isDisabled_whenFieldsAreEmpty() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {}
            )
        }

        composeTestRule
            .onNodeWithText("Login")
            .assertIsNotEnabled()
    }

    @Test
    fun loginButton_isEnabled_whenFieldsAreFilled() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {}
            )
        }

        composeTestRule
            .onNodeWithTag("username_field")
            .performTextInput("testuser")

        composeTestRule
            .onNodeWithTag("password_field")
            .performTextInput("password123")

        composeTestRule
            .onNodeWithText("Login")
            .assertIsEnabled()
    }
}
```

---

## 12. Ví Dụ Thực Tế: Tạo Feature Hoàn Chỉnh

Hãy tạo feature "Favorite Courts" từ đầu đến cuối.

### Step 1: Domain Models

```kotlin
// domain/model/FavoriteCourt.kt
data class FavoriteCourt(
    val courtId: String,
    val userId: String,
    val addedAt: LocalDateTime
)
```

### Step 2: Domain Repository Interface

```kotlin
// domain/repository/FavoriteRepository.kt
interface FavoriteRepository {
    suspend fun addToFavorites(courtId: String): Flow<Resource<Unit>>
    suspend fun removeFromFavorites(courtId: String): Flow<Resource<Unit>>
    fun getFavoriteCourts(): Flow<Resource<List<Court>>>
    fun isFavorite(courtId: String): Flow<Boolean>
}
```

### Step 3: Data Layer - DTO

```kotlin
// data/remote/dto/FavoriteDto.kt
data class AddToFavoritesRequest(
    @SerializedName("court_id")
    val courtId: String
)

data class FavoriteResponseDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?
)
```

### Step 4: Data Layer - API

```kotlin
// data/remote/api/FavoriteApi.kt
interface FavoriteApi {
    @POST("favorites")
    suspend fun addToFavorites(
        @Body request: AddToFavoritesRequest
    ): Response<FavoriteResponseDto>

    @DELETE("favorites/{courtId}")
    suspend fun removeFromFavorites(
        @Path("courtId") courtId: String
    ): Response<FavoriteResponseDto>

    @GET("favorites")
    suspend fun getFavoriteCourts(): CourtListResponseDto
}
```

### Step 5: Data Layer - Repository Implementation

```kotlin
// data/repository/FavoriteRepositoryImpl.kt
@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteApi: FavoriteApi,
    private val courtDao: CourtDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FavoriteRepository {

    override suspend fun addToFavorites(courtId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val response = favoriteApi.addToFavorites(
                AddToFavoritesRequest(courtId)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                // Update local database
                courtDao.updateFavoriteStatus(courtId, true)
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to add to favorites"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(ioDispatcher)

    override suspend fun removeFromFavorites(courtId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val response = favoriteApi.removeFromFavorites(courtId)

            if (response.isSuccessful && response.body()?.success == true) {
                courtDao.updateFavoriteStatus(courtId, false)
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to remove from favorites"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(ioDispatcher)

    override fun getFavoriteCourts(): Flow<Resource<List<Court>>> = flow {
        emit(Resource.Loading())

        // Load from cache first
        courtDao.getFavoriteCourts().first().let { cached ->
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached.map { it.toDomain() }))
            }
        }

        try {
            val response = favoriteApi.getFavoriteCourts()
            if (response.success) {
                val courts = response.data.map { it.toDomain() }
                courtDao.insertCourts(courts.map { it.toEntity(isFavorite = true) })
                emit(Resource.Success(courts))
            } else {
                emit(Resource.Error("Failed to load favorites"))
            }
        } catch (e: Exception) {
            val cached = courtDao.getFavoriteCourts().first()
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached.map { it.toDomain() }))
            } else {
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }.flowOn(ioDispatcher)

    override fun isFavorite(courtId: String): Flow<Boolean> = flow {
        courtDao.getCourtByIdFlow(courtId).collect { court ->
            emit(court?.isFavorite ?: false)
        }
    }.flowOn(ioDispatcher)
}
```

### Step 6: DI - Provide API and Repository

```kotlin
// core/di/NetworkModule.kt
@Provides
@Singleton
fun provideFavoriteApi(retrofit: Retrofit): FavoriteApi =
    retrofit.create(FavoriteApi::class.java)

// core/di/RepositoryModule.kt
@Binds
@Singleton
abstract fun bindFavoriteRepository(
    favoriteRepositoryImpl: FavoriteRepositoryImpl
): FavoriteRepository
```

### Step 7: Presentation - State, Intent, Effect

```kotlin
// presentation/favorite/viewmodel/FavoriteViewModel.kt
data class FavoriteListState(
    val isLoading: Boolean = false,
    val favoriteCourts: List<Court> = emptyList(),
    val error: String? = null
)

sealed interface FavoriteListIntent {
    object LoadFavorites : FavoriteListIntent
    data class RemoveFromFavorites(val courtId: String) : FavoriteListIntent
    data class NavigateToDetail(val courtId: String) : FavoriteListIntent
    object Refresh : FavoriteListIntent
}

sealed interface FavoriteListEffect {
    data class ShowSuccess(val message: String) : FavoriteListEffect
    data class ShowError(val message: String) : FavoriteListEffect
    data class NavigateToDetail(val courtId: String) : FavoriteListEffect
}
```

### Step 8: Presentation - ViewModel

```kotlin
// presentation/favorite/viewmodel/FavoriteViewModel.kt
@HiltViewModel
class FavoriteListViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : BaseViewModel<FavoriteListState, FavoriteListIntent, FavoriteListEffect>() {

    override fun createInitialState(): FavoriteListState {
        return FavoriteListState()
    }

    init {
        handleEvent(FavoriteListIntent.LoadFavorites)
    }

    override fun handleEvent(event: FavoriteListIntent) {
        when (event) {
            FavoriteListIntent.LoadFavorites -> loadFavorites()
            is FavoriteListIntent.RemoveFromFavorites -> removeFromFavorites(event.courtId)
            is FavoriteListIntent.NavigateToDetail -> navigateToDetail(event.courtId)
            FavoriteListIntent.Refresh -> refresh()
        }
    }

    private fun loadFavorites() {
        setState { copy(isLoading = true) }

        launchCatching(
            onError = { throwable ->
                setState { copy(isLoading = false, error = throwable.message) }
                setEffect { FavoriteListEffect.ShowError(throwable.message ?: "Error") }
            }
        ) {
            favoriteRepository.getFavoriteCourts().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        setState {
                            copy(
                                isLoading = false,
                                favoriteCourts = resource.data ?: emptyList(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        setState { copy(isLoading = false, error = resource.message) }
                        setEffect { FavoriteListEffect.ShowError(resource.message ?: "Error") }
                    }
                    is Resource.Loading -> {
                        setState { copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun removeFromFavorites(courtId: String) {
        launchCatching(
            onError = { throwable ->
                setEffect { FavoriteListEffect.ShowError(throwable.message ?: "Error") }
            }
        ) {
            favoriteRepository.removeFromFavorites(courtId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        setEffect { FavoriteListEffect.ShowSuccess("Removed from favorites") }
                        // Reload favorites
                        handleEvent(FavoriteListIntent.LoadFavorites)
                    }
                    is Resource.Error -> {
                        setEffect { FavoriteListEffect.ShowError(resource.message ?: "Error") }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    private fun navigateToDetail(courtId: String) {
        setEffect { FavoriteListEffect.NavigateToDetail(courtId) }
    }

    private fun refresh() {
        handleEvent(FavoriteListIntent.LoadFavorites)
    }
}
```

### Step 9: Presentation - Screen

```kotlin
// presentation/favorite/screen/FavoriteListScreen.kt
@Composable
fun FavoriteListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: FavoriteListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle effects
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FavoriteListEffect.ShowSuccess -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is FavoriteListEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is FavoriteListEffect.NavigateToDetail -> {
                    onNavigateToDetail(effect.courtId)
                }
            }
        }
    }

    FavoriteListContent(
        state = state,
        onBackClick = onNavigateBack,
        onIntent = viewModel::handleEvent
    )
}

@Composable
private fun FavoriteListContent(
    state: FavoriteListState,
    onBackClick: () -> Unit,
    onIntent: (FavoriteListIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Courts") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.favoriteCourts.isEmpty() -> {
                    EmptyFavoritesView(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.favoriteCourts,
                            key = { it.id }
                        ) { court ->
                            FavoriteCourtItem(
                                court = court,
                                onClick = {
                                    onIntent(FavoriteListIntent.NavigateToDetail(court.id))
                                },
                                onRemoveClick = {
                                    onIntent(FavoriteListIntent.RemoveFromFavorites(court.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteCourtItem(
    court: Court,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = court.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = court.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${court.pricePerHour.formatCurrency()}/hour",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No favorite courts yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start adding courts to your favorites!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Extension function for formatting
fun Long.formatCurrency(): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN"))
    return formatter.format(this)
}
```

### Step 10: Navigation - Add Route

```kotlin
// core/navigation/Screen.kt
data object FavoriteList : Screen("favorite_list")

// core/navigation/NavigationGraph.kt
composable(route = Screen.FavoriteList.route) {
    FavoriteListScreen(
        onNavigateBack = { navController.navigateUp() },
        onNavigateToDetail = { courtId ->
            navController.navigate(
                Screen.CourtDetail.createRoute(courtId)
            )
        }
    )
}
```

---

## Tổng Kết

Document này cung cấp:

1. ✅ **Tổng quan** về Clean Architecture trong dự án
2. ✅ **Cấu trúc** chi tiết từng layer và package
3. ✅ **Navigation** setup và best practices
4. ✅ **API Integration** từ A-Z
5. ✅ **State Management** với MVI pattern
6. ✅ **Dependency Injection** với Hilt
7. ✅ **ViewModel** patterns và lifecycle
8. ✅ **Compose UI** guidelines và optimization
9. ✅ **Room Database** setup và patterns
10. ✅ **DataStore** cho preferences
11. ✅ **Conventions** và coding standards
12. ✅ **Ví dụ thực tế** tạo feature hoàn chỉnh

### Key Takeaways

**Clean Architecture Benefits:**
- 🎯 Separation of concerns
- ✅ Testable code
- 🔄 Easy to maintain and extend
- 📦 Reusable components
- 🚀 Scalable architecture

**Best Practices Recap:**
- Luôn immutable state
- Single source of truth
- Repository pattern cho data access
- MVI pattern cho predictable state management
- Proper error handling ở mọi layer
- Comprehensive testing strategy

**Common Pitfalls:**
- ❌ Tight coupling giữa layers
- ❌ Logic trong Composables
- ❌ Mutable state
- ❌ Synchronous operations on main thread
- ❌ Missing error handling
- ❌ Memory leaks với improper Flow collection

---

**Happy Coding! 🚀**

*Document này là living document và sẽ được update khi project evolve.*
