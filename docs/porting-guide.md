# Chanmi iOS → Android 포팅 설계서

## 1. 프로젝트 개요

**Chanmi(찬미)**는 가톨릭 묵주기도 앱으로, 4개 탭으로 구성됩니다:
1. **묵주기도 (Rosary)** — 4가지 신비(환희/고통/영광/빛) 선택 후 스와이프로 기도 진행
2. **달력 (Calendar)** — 월별 달력에서 묵주기도/선행 기록 관리
3. **기도문 (Prayers)** — 카테고리별 기도문 열람 (JSON 번들)
4. **주변 성당 (NearbyChurch)** — 현재 위치 기반 10km 반경 성당 검색 + 지도

**iOS 아키텍처**: MVVM + Repository 패턴, 외부 의존성 없음
**총 Swift 소스 파일**: 33개 (Models 8, ViewModels 4, Views 12, Repositories 6, Utilities 1, App 2)

---

## 2. 기술 스택 매핑

| iOS | Android | 비고 |
|-----|---------|------|
| SwiftUI | Jetpack Compose | 선언형 UI |
| SwiftData (`@Model`) | Room Database (`@Entity`) | 로컬 영속 저장소 |
| `@Observable` | `ViewModel` + `StateFlow`/`MutableStateFlow` | 상태 관리 |
| `@State` | `remember`/`mutableStateOf` | 로컬 UI 상태 |
| `@Bindable` | State hoisting (파라미터 전달) | 양방향 바인딩 |
| `@Environment(\.modelContext)` | Hilt DI (`@Inject`) | 의존성 주입 |
| `@AppStorage` | DataStore Preferences | 경량 키-값 저장 |
| `NavigationStack` | NavHost + NavController | 화면 전환 |
| `TabView` | `BottomNavigation` + `NavHost` | 탭 네비게이션 |
| `NavigationSplitView` | `ListDetailPaneScaffold` | iPad/태블릿 적응형 |
| `CoreLocation` | `FusedLocationProviderClient` | 위치 서비스 |
| `MapKit` (`Map`) | Google Maps Compose (`GoogleMap`) | 지도 |
| `.sheet()` | `ModalBottomSheet` / Dialog | 시트/다이얼로그 |
| `.fullScreenCover()` | 전체화면 Composable 또는 Activity | 전체화면 오버레이 |
| `.searchable()` | `SearchBar` Composable | 검색 UI |
| `UIImpactFeedbackGenerator` | `HapticFeedback` (Compose) | 햅틱 피드백 |
| `Bundle.main.url(forResource:)` | `assets/` 폴더 + `AssetManager` | 번들 리소스 |
| `Task { }` (Swift Concurrency) | `viewModelScope.launch` (Coroutines) | 비동기 처리 |
| `async/await` | `suspend fun` / `Flow` | 비동기 패턴 |
| `#Predicate` (SwiftData) | Room `@Query` / DAO | 데이터 쿼리 |

---

## 3. 프로젝트 구조 매핑

### iOS 구조
```
Chanmi/
├── ChanmiApp.swift              # @main 앱 진입점
├── ContentView.swift            # TabView (4탭)
├── Models/                      # 데이터 모델 (8파일)
├── ViewModels/                  # @Observable 뷰모델 (4파일)
├── Views/                       # SwiftUI 뷰 (12파일)
│   ├── Calendar/
│   ├── Prayers/
│   ├── Rosary/
│   └── NearbyChurch/
├── Repositories/
│   ├── Protocols/               # 프로토콜 (3파일)
│   └── Implementations/         # 구현체 (3파일)
├── Utilities/                   # LocationManager
└── Resources/                   # prayers.json, korean_catholic_churches.json
```

### Android 대응 구조 (권장)
```
app/src/main/
├── java/com/chanmi/app/
│   ├── ChanmiApplication.kt          # Application 클래스
│   ├── MainActivity.kt               # 단일 Activity (Compose)
│   ├── navigation/
│   │   └── ChanmiNavigation.kt       # NavHost + BottomNav
│   ├── data/
│   │   ├── model/                     # Room Entity + 일반 모델
│   │   │   ├── DailyRecord.kt
│   │   │   ├── RosaryEntry.kt
│   │   │   ├── GoodDeed.kt
│   │   │   ├── MysteryType.kt
│   │   │   ├── RosaryPhase.kt
│   │   │   ├── Prayer.kt
│   │   │   ├── ChurchItem.kt
│   │   │   └── ChurchCluster.kt
│   │   ├── local/
│   │   │   ├── ChanmiDatabase.kt     # Room Database
│   │   │   ├── DailyRecordDao.kt     # Room DAO
│   │   │   ├── RosaryEntryDao.kt
│   │   │   └── GoodDeedDao.kt
│   │   └── repository/
│   │       ├── CalendarRepository.kt
│   │       ├── ChurchRepository.kt
│   │       └── PrayerRepository.kt
│   ├── ui/
│   │   ├── theme/
│   │   │   └── Theme.kt              # Material3 테마 + 커스텀 컬러
│   │   ├── rosary/
│   │   │   ├── RosaryViewModel.kt
│   │   │   ├── MysterySelectionScreen.kt
│   │   │   ├── RosaryCompletionScreen.kt
│   │   │   └── SwipeAdvanceModifier.kt
│   │   ├── calendar/
│   │   │   ├── CalendarViewModel.kt
│   │   │   ├── CalendarScreen.kt
│   │   │   ├── DayDetailScreen.kt
│   │   │   └── GoodDeedFormDialog.kt
│   │   ├── prayers/
│   │   │   ├── PrayersViewModel.kt
│   │   │   ├── PrayerCategoryListScreen.kt
│   │   │   ├── PrayerListScreen.kt
│   │   │   └── PrayerDetailScreen.kt
│   │   └── nearbychurch/
│   │       ├── NearbyChurchViewModel.kt
│   │       ├── NearbyChurchScreen.kt
│   │       └── ChurchListSheet.kt
│   ├── location/
│   │   └── LocationManager.kt
│   └── di/
│       ├── AppModule.kt               # Hilt 모듈
│       └── DatabaseModule.kt
├── assets/
│   ├── prayers.json
│   └── korean_catholic_churches.json
└── res/
    └── values/
        ├── colors.xml
        └── strings.xml
```

---

## 4. 모델 레이어 상세 매핑

### 4.1 DailyRecord (SwiftData → Room)

**iOS (SwiftData)**:
```swift
@Model
final class DailyRecord {
    var date: Date
    @Relationship(deleteRule: .cascade, inverse: \RosaryEntry.dailyRecord)
    var rosaryEntries: [RosaryEntry]
    @Relationship(deleteRule: .cascade, inverse: \GoodDeed.dailyRecord)
    var goodDeeds: [GoodDeed]
}
```

**Android (Room)**:
```kotlin
@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long  // epoch millis, startOfDay
)

// 1:N 관계는 Room에서 @Relation으로 표현
data class DailyRecordWithDetails(
    @Embedded val record: DailyRecord,
    @Relation(parentColumn = "id", entityColumn = "dailyRecordId")
    val rosaryEntries: List<RosaryEntry>,
    @Relation(parentColumn = "id", entityColumn = "dailyRecordId")
    val goodDeeds: List<GoodDeed>
)
```

### 4.2 RosaryEntry

**iOS**:
```swift
@Model
final class RosaryEntry {
    var mysteryType: String     // "joyful", "sorrowful", "glorious", "luminous"
    var completedAt: Date
    var dailyRecord: DailyRecord?
}
```

**Android**:
```kotlin
@Entity(
    tableName = "rosary_entries",
    foreignKeys = [ForeignKey(
        entity = DailyRecord::class,
        parentColumns = ["id"],
        childColumns = ["dailyRecordId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class RosaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mysteryType: String,
    val completedAt: Long,
    val dailyRecordId: Long
)
```

### 4.3 GoodDeed

**iOS**:
```swift
@Model
final class GoodDeed {
    var content: String
    var category: String        // "service", "donation", "prayer", "forgiveness", "other"
    var createdAt: Date
    var dailyRecord: DailyRecord?
}
```

**Android**:
```kotlin
@Entity(
    tableName = "good_deeds",
    foreignKeys = [ForeignKey(
        entity = DailyRecord::class,
        parentColumns = ["id"],
        childColumns = ["dailyRecordId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GoodDeed(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val category: String,
    val createdAt: Long,
    val dailyRecordId: Long
)
```

### 4.4 MysteryType (enum → sealed class 또는 enum class)

**iOS**: `enum MysteryType: String, CaseIterable` — 4가지 신비 + 묵상 텍스트, 요일별 추천 로직

**Android**:
```kotlin
enum class MysteryType(val key: String) {
    JOYFUL("joyful"),
    SORROWFUL("sorrowful"),
    GLORIOUS("glorious"),
    LUMINOUS("luminous");

    val displayName: String get() = when (this) {
        JOYFUL -> "환희의 신비"
        SORROWFUL -> "고통의 신비"
        GLORIOUS -> "영광의 신비"
        LUMINOUS -> "빛의 신비"
    }

    val recommendedDays: String get() = when (this) {
        JOYFUL -> "월요일, 토요일"
        SORROWFUL -> "화요일, 금요일"
        GLORIOUS -> "수요일, 일요일"
        LUMINOUS -> "목요일"
    }

    val meditations: List<String> get() = when (this) {
        JOYFUL -> listOf(
            "마리아께서 예수님을 잉태하심을 묵상합시다",
            "마리아께서 엘리사벳을 찾아보심을 묵상합시다",
            "마리아께서 예수님을 낳으심을 묵상합시다",
            "마리아께서 예수님을 성전에 바치심을 묵상합시다",
            "마리아께서 잃으셨던 예수님을 성전에서 찾으심을 묵상합시다"
        )
        SORROWFUL -> listOf(
            "예수님께서 우리를 위하여 피땀 흘리심을 묵상합시다",
            "예수님께서 우리를 위하여 매맞으심을 묵상합시다",
            "예수님께서 우리를 위하여 가시관 쓰심을 묵상합시다",
            "예수님께서 우리를 위하여 십자가 지심을 묵상합시다",
            "예수님께서 우리를 위하여 십자가에 못박혀 돌아가심을 묵상합시다"
        )
        GLORIOUS -> listOf(
            "예수님께서 부활하심을 묵상합시다",
            "예수님께서 승천하심을 묵상합시다",
            "예수님께서 성령을 보내심을 묵상합시다",
            "마리아께서 하늘에 올림을 받으심을 묵상합시다",
            "마리아께서 하늘과 땅의 모후로 뽑히심을 묵상합시다"
        )
        LUMINOUS -> listOf(
            "예수님께서 요르단 강에서 세례 받으심을 묵상합시다",
            "예수님께서 카나 혼인잔치에서 첫 기적을 행하심을 묵상합시다",
            "예수님께서 하느님 나라를 선포하심을 묵상합시다",
            "예수님께서 거룩하게 변모하심을 묵상합시다",
            "예수님께서 성체성사를 세우심을 묵상합시다"
        )
    }

    companion object {
        fun recommendedForToday(): MysteryType {
            return when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.SUNDAY -> GLORIOUS
                java.util.Calendar.MONDAY -> JOYFUL
                java.util.Calendar.TUESDAY -> SORROWFUL
                java.util.Calendar.WEDNESDAY -> GLORIOUS
                java.util.Calendar.THURSDAY -> LUMINOUS
                java.util.Calendar.FRIDAY -> SORROWFUL
                java.util.Calendar.SATURDAY -> JOYFUL
                else -> JOYFUL
            }
        }
    }
}
```

### 4.5 RosaryPhase (enum with associated values → sealed class)

**iOS**: `enum RosaryPhase` with cases like `.decade(Int, DecadeStep)`, `.openingHailMary(Int)`

**Android**: sealed class가 associated values를 자연스럽게 표현
```kotlin
sealed class DecadeStep {
    data object Meditation : DecadeStep()
    data object OurFather : DecadeStep()
    data class HailMary(val count: Int) : DecadeStep()  // 1~10
    data object Glory : DecadeStep()
    data object Fatima : DecadeStep()
}

sealed class RosaryPhase {
    data object MysterySelection : RosaryPhase()
    data object SignOfCross : RosaryPhase()
    data object ApostlesCreed : RosaryPhase()
    data object OpeningOurFather : RosaryPhase()
    data class OpeningHailMary(val count: Int) : RosaryPhase()  // 1~3
    data object OpeningGlory : RosaryPhase()
    data class Decade(val number: Int, val step: DecadeStep) : RosaryPhase()  // 1~5
    data object SalveRegina : RosaryPhase()
    data object ClosingPrayer : RosaryPhase()
    data object Completed : RosaryPhase()
}
```

### 4.6 Prayer / PrayerCategory (JSON 번들 모델)

**iOS**: `Codable` 구조체

**Android**: Gson/Kotlinx Serialization 데이터 클래스
```kotlin
@Serializable
data class PrayerData(val categories: List<PrayerCategory>)

@Serializable
data class PrayerCategory(
    val id: String,
    val name: String,
    val icon: String,  // SF Symbol → Material Icon 매핑 필요
    val prayers: List<Prayer>
)

@Serializable
data class Prayer(
    val id: String,
    val title: String,
    val content: String
)
```

### 4.7 ChurchItem / ChurchCluster

**iOS**: `Codable` struct + `CLLocationCoordinate2D`, `openInMaps()` 메서드
**Android**: data class + `LatLng` (Google Maps), Intent로 지도앱 실행

```kotlin
data class ChurchItem(
    val name: String,
    val diocese: String,
    val address: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    var distance: Double = 0.0
) {
    val id: String get() = "${name}_${latitude}_${longitude}"
    val latLng: LatLng get() = LatLng(latitude, longitude)

    val formattedDistance: String get() = if (distance < 1000) {
        "${distance.toInt()}m"
    } else {
        String.format("%.1fkm", distance / 1000)
    }

    fun openInMaps(context: Context) {
        val uri = Uri.parse("geo:${latitude},${longitude}?q=${Uri.encode(name)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}

data class ChurchCluster(
    val churches: List<ChurchItem>,
    val center: LatLng
) {
    val id: String get() = churches.map { it.id }.sorted().joinToString("_")
    val count: Int get() = churches.size
    val name: String get() = when {
        churches.isEmpty() -> ""
        churches.size == 1 -> churches[0].name
        else -> "${churches[0].name} 외 ${churches.size - 1}개"
    }
}
```

---

## 5. Repository 레이어 상세 매핑

### 5.1 CalendarRepository

**iOS 프로토콜**:
```swift
protocol CalendarRepositoryProtocol {
    func fetchRecord(for date: Date) async throws -> DailyRecord?
    func fetchRecords(from startDate: Date, to endDate: Date) async throws -> [DailyRecord]
    func addRosaryEntry(date: Date, mysteryType: String) async throws
    func addGoodDeed(date: Date, content: String, category: String) async throws
    func deleteGoodDeed(_ deed: GoodDeed) async throws
    func deleteRosaryEntry(_ entry: RosaryEntry) async throws
    func updateGoodDeed(_ deed: GoodDeed, content: String, category: String) async throws
}
```

**iOS 구현** (`SwiftDataCalendarRepository`):
- `ModelContext`로 SwiftData CRUD 수행
- `getOrCreateRecord(for:)` — 해당 날짜의 DailyRecord가 없으면 생성
- `#Predicate`로 날짜 범위 쿼리

**Android DAO**:
```kotlin
@Dao
interface DailyRecordDao {
    @Transaction
    @Query("SELECT * FROM daily_records WHERE date >= :start AND date <= :end")
    fun getRecordsInRange(start: Long, end: Long): Flow<List<DailyRecordWithDetails>>

    @Transaction
    @Query("SELECT * FROM daily_records WHERE date >= :start AND date < :end LIMIT 1")
    suspend fun getRecord(start: Long, end: Long): DailyRecordWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DailyRecord): Long

    @Insert
    suspend fun insertRosaryEntry(entry: RosaryEntry)

    @Insert
    suspend fun insertGoodDeed(deed: GoodDeed)

    @Delete
    suspend fun deleteRosaryEntry(entry: RosaryEntry)

    @Delete
    suspend fun deleteGoodDeed(deed: GoodDeed)

    @Update
    suspend fun updateGoodDeed(deed: GoodDeed)
}
```

**Android Repository**:
```kotlin
class CalendarRepository @Inject constructor(
    private val dao: DailyRecordDao
) {
    fun getRecordsForMonth(year: Int, month: Int): Flow<List<DailyRecordWithDetails>>
    suspend fun addRosaryEntry(date: LocalDate, mysteryType: String)
    suspend fun addGoodDeed(date: LocalDate, content: String, category: String)
    suspend fun deleteRosaryEntry(entry: RosaryEntry)
    suspend fun deleteGoodDeed(deed: GoodDeed)
    suspend fun updateGoodDeed(deed: GoodDeed, content: String, category: String)
}
```

### 5.2 ChurchRepository

**iOS**: `BundleChurchRepository` — `Bundle.main.url(forResource:)`로 JSON 로드, 메모리 캐싱

**Android**:
```kotlin
class ChurchRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cachedData: List<ChurchItem>? = null

    suspend fun loadChurches(): List<ChurchItem> = withContext(Dispatchers.IO) {
        cachedData?.let { return@withContext it }
        val json = context.assets.open("korean_catholic_churches.json")
            .bufferedReader().use { it.readText() }
        val data = Json.decodeFromString<ChurchData>(json)
        cachedData = data.churches
        data.churches
    }
}
```

### 5.3 PrayerRepository

**iOS**: `BundlePrayerRepository` — `prayers.json` 로드, 검색 기능

**Android**: 동일 패턴, `assets/` 폴더에서 로드

---

## 6. ViewModel 레이어 상세 매핑

### 6.1 RosaryViewModel (가장 복잡)

**핵심 로직**:
- **상태 머신**: `RosaryPhase`를 순차적으로 advance — 성호경 → 사도신경 → 주님의 기도 → 성모송×3 → 영광송 → [묵상→주님기도→성모송×10→영광송→파티마기도]×N단 → 성모찬송 → 마침기도 → 완료
- **진행률 계산**: `totalSteps = 9 + 14 * numberOfDecades`, `currentStepIndex`로 progress 계산
- **타이머**: Swift Task + Task.sleep → Coroutine + delay
- **디바운스**: 빠른 연속 스와이프 방지 (0.3초 간격)
- **AppStorage**: `preferredHand` (좌/우 스와이프 방향), `hasSeenSwipeGuide` (가이드 표시 여부)
- **기도문 텍스트**: 주님의 기도, 성모송, 영광송, 파티마의 기도, 성모찬송 등 한국어 전문

**Android 매핑**:
```kotlin
@HiltViewModel
class RosaryViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _currentPhase = MutableStateFlow<RosaryPhase>(RosaryPhase.MysterySelection)
    val currentPhase: StateFlow<RosaryPhase> = _currentPhase.asStateFlow()

    private val _selectedMystery = MutableStateFlow(MysteryType.recommendedForToday())
    val selectedMystery: StateFlow<MysteryType> = _selectedMystery.asStateFlow()

    private val _isPraying = MutableStateFlow(false)
    private val _elapsedSeconds = MutableStateFlow(0)
    private val _numberOfDecades = MutableStateFlow(5)

    private var timerJob: Job? = null
    private var lastAdvanceTime = 0L
    private val advanceDebounceMs = 300L

    // preferredHand, hasSeenSwipeGuide → DataStore에서 관리

    fun startPraying() { ... }
    fun advance() { ... }  // 상태 머신 전이 로직 동일
    fun debouncedAdvance() { ... }
    fun reset() { ... }
}
```

### 6.2 CalendarViewModel

**핵심 로직**:
- `currentMonth: Date` → 월 이동 (이전/다음)
- `daysInMonth: [Date?]` → 그리드용 날짜 배열 (빈칸 포함)
- `dailyRecords: [Date: DailyRecord]` → 해당 월의 모든 기록을 한 번에 로드
- `selectDate()` — 같은 날짜 다시 선택 시 선택 해제 (토글)
- CRUD: `addRosaryEntry`, `addGoodDeed`, `deleteRosaryEntry`, `deleteGoodDeed`, `updateGoodDeed`

**Android 매핑**: `YearMonth`, `LocalDate` 사용, `Flow<Map<LocalDate, DailyRecordWithDetails>>` 구독

### 6.3 PrayersViewModel

**핵심 로직**:
- `categories` 로드 (JSON 번들)
- `searchQuery` → `searchResults` 계산 (title/content 검색)

**Android**: 간단한 `StateFlow` + `Dispatchers.IO`

### 6.4 NearbyChurchViewModel

**핵심 로직**:
- `searchNearbyChurches(near:)` — 전체 교회 데이터에서 10km 반경 필터링 + 거리 정렬
- `clusterChurches(radius:)` — 카메라 줌 레벨에 따른 클러스터링 (haversine 거리 기반)
- `updateClusters(cameraDistance:)` — 카메라 거리에 따라 클러스터 반경 조절
- `checkIfMapMovedSignificantly()` — 1km 이상 이동 시 "이 지역에서 검색" 버튼 표시
- `haversineDistance()` — 두 좌표 간 거리 계산 (지구 반경 6,371km)
- `MapCameraPosition` → Google Maps `CameraPositionState`

---

## 7. View(UI) 레이어 상세 매핑

### 7.1 앱 진입점 & 네비게이션

**iOS** (`ContentView.swift`):
- `TabView(selection:)` with 4 tabs
- `@State selectedTab = 0`
- 각 탭: `.tag(0~3)`, `.tabItem { Label("name", systemImage: "icon") }`

**Android**:
```kotlin
@Composable
fun ChanmiApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                // 묵주기도, 달력, 기도문, 주변 성당
            }
        }
    ) { paddingValues ->
        NavHost(navController, startDestination = "rosary") {
            composable("rosary") { MysterySelectionScreen() }
            composable("calendar") { CalendarScreen() }
            composable("prayers") { PrayerCategoryListScreen() }
            composable("nearby") { NearbyChurchScreen() }
        }
    }
}
```

**아이콘 매핑** (SF Symbols → Material Icons):
| iOS SF Symbol | Android Material Icon | 탭 |
|---|---|---|
| `hands.and.sparkles` | `Icons.Default.AutoAwesome` 또는 커스텀 | 묵주기도 |
| `calendar` | `Icons.Default.CalendarMonth` | 달력 |
| `book` | `Icons.Default.MenuBook` | 기도문 |
| `mappin.and.ellipse` | `Icons.Default.LocationOn` | 주변 성당 |

### 7.2 묵주기도 탭 (Rosary)

#### MysterySelectionView → MysterySelectionScreen
**주요 컴포넌트**:
1. **신비 선택 세그먼트** — `HStack` of 4 buttons → `SegmentedButton` 또는 커스텀 `Row`
2. **묵주 구슬 원형** — 10개 구슬이 원형 배치, Canvas로 그리기
   - `Circle().offset(x: radius*cos, y: radius*sin)` → `Canvas` + `drawCircle` at polar coordinates
   - 3가지 상태: incomplete(GoldAccent), current(AccentColor 60%), completed(AccentColor)
   - 기도 중이 아닐 때 탭하면 `startPraying()`
3. **스와이프 영역** — `SwipeAdvanceModifier` (커스텀 DragGesture)
   - `DragGesture(minimumDistance: 10)` + 50px 이상 아래로 → advance
   - → `Modifier.pointerInput` + `detectVerticalDragGestures`
   - 좌/우 배치: `preferredHand` 설정에 따라 좌측 또는 우측
4. **기도문 카드** — 접기/펼치기, 현재 기도 단계별 텍스트
5. **단수 선택** — 1~5단 원형 버튼 (기도 시작 전)
6. **진행률/시간** — 두 개의 카드 (진행률 %, 경과 시간 MM:SS)

**기도 진행 모드** (`isPraying == true`):
- ScrollView 제거 (제스처 충돌 방지) → 고정 레이아웃
- 툴바에 "중단" / "완료" 버튼
- `.closingPrayer` 단계에서 "완료" 버튼 표시
- 완료 시 `fullScreenCover` → `RosaryCompletionView`

#### SwipeGuideOverlay → SwipeGuideOverlay Composable
- 반투명 오버레이 + 애니메이션 화살표
- 첫 기도 시 1회만 표시 (`hasSeenSwipeGuide`)

#### RosaryCompletionView → RosaryCompletionScreen
- 완료 메시지 + 기록 자동 저장 (SwiftDataCalendarRepository.addRosaryEntry)
- `.task` 블록에서 저장 → `LaunchedEffect`에서 저장

### 7.3 달력 탭 (Calendar)

#### CalendarTabView → CalendarScreen
**적응형 레이아웃**:
- `horizontalSizeClass == .regular` (iPad) → HStack(달력 | 상세)
- `horizontalSizeClass == .compact` (iPhone) → VStack(달력, 하단 상세)
- → Android: `WindowSizeClass`로 분기, 또는 `ListDetailPaneScaffold`

**CalendarGridView**:
- 월 이동: 좌우 화살표 + 스와이프 (`DragGesture`)
- 7열 `LazyVGrid` → `LazyVerticalGrid(columns = GridCells.Fixed(7))`
- 요일 헤더: 일(빨강), 토(파랑), 나머지(기본)
- DayCellView: 날짜 숫자 + 하단 도트 인디케이터 (묵주기도=보라, 선행=핑크)

**DayDetailInlineView / CompactDayDetailView**:
- 묵주기도 기록 Section: ForEach entries + 수동 추가 버튼 + 스와이프 삭제
- 선행 기록 Section: ForEach deeds + 추가/수정/삭제
- 시트: `GoodDeedFormView`, `AddRosarySheet`

#### GoodDeedFormView → GoodDeedFormDialog (BottomSheet)
- `TextField` + `Picker` (카테고리: 봉사/기부/기도/용서/기타)
- 수정 모드: 기존 값 프리필
- `presentationDetents([.medium])` → `ModalBottomSheet`

#### AddRosarySheet → AddRosaryDialog
- `Picker("신비 유형")` → Exposed Dropdown Menu
- 저장 시 `addRosaryEntry(date:, mysteryType:)` 호출

### 7.4 기도문 탭 (Prayers)

#### PrayerCategoryListView → PrayerCategoryListScreen
**적응형 레이아웃**:
- iPhone: `NavigationStack` → ScrollView → 카테고리 카드 리스트
- iPad: `NavigationSplitView` (3단: 카테고리 | 기도 목록 | 기도 상세)
- → Android compact: `NavHost`로 카테고리→목록→상세 순차 이동
- → Android expanded: `ListDetailPaneScaffold` 또는 커스텀 3-pane

**PrayerCategoryCard**: 아이콘 + 카테고리명 + 미리보기 (처음 3개 기도 제목) + 개수 + chevron

**검색**: `.searchable(text:prompt:)` → `SearchBar` Composable
- 검색 결과: flatMap → filter (title/content)

#### PrayerListView → PrayerListScreen
- 카테고리의 기도 목록 → 카드형 NavigationLink

#### PrayerDetailView → PrayerDetailScreen
- 기도문 전문 표시, 스크롤
- **글꼴 크기 조절**: `@AppStorage("prayerFontSize")` 14~32pt
  - 하단 툴바에 +/- 버튼
  - → DataStore + BottomAppBar 또는 TopAppBar actions

**SF Symbol → Material Icon 매핑 (기도문 카테고리)**:
prayers.json의 `icon` 필드에 SF Symbol 이름이 저장되어 있으므로, Android용 매핑 맵 필요:
```kotlin
fun sfSymbolToMaterialIcon(sfSymbol: String): ImageVector = when (sfSymbol) {
    "cross.fill" -> Icons.Default.Add  // 커스텀 십자가 아이콘 권장
    "book.fill" -> Icons.Default.MenuBook
    "heart.fill" -> Icons.Default.Favorite
    "hands.and.sparkles" -> Icons.Default.AutoAwesome
    // ... prayers.json 확인 후 매핑 추가
    else -> Icons.Default.Article
}
```

### 7.5 주변 성당 탭 (NearbyChurch)

#### NearbyChurchView → NearbyChurchScreen
**상태별 UI**:
1. `notDetermined` → 위치 권한 요청 화면
2. `authorizedWhenInUse/Always` → 지도 + 목록
3. `denied/restricted` → 설정 이동 안내

**적응형 레이아웃**:
- iPhone: 전체 지도 + 하단 "성당 목록" 버튼 → Sheet
- iPad: HStack(지도 | 사이드 리스트 320pt)
- → Android: `WindowSizeClass` 분기

**지도 기능**:
- `Map(position:)` → `GoogleMap(cameraPositionState:)`
- `UserAnnotation()` → 기본 내 위치 마커
- `Annotation` → `MarkerComposable` 또는 `Marker`
- 클러스터: 커스텀 Annotation (원형 + 카운트) → Google Maps ClusterManager 또는 커스텀 마커
- `.onMapCameraChange(frequency: .onEnd)` → `LaunchedEffect(cameraPositionState.isMoving)` 또는 `snapshotFlow`
- `MapUserLocationButton()`, `MapCompass()` → Google Maps UI Settings

**"이 지역에서 검색" 버튼**: 1km 이상 이동 시 상단에 표시

**성당 상세 시트** (`churchDetailSheet`):
- 성당 이름, 교구, 주소, 전화번호, 거리
- "길찾기" 버튼 → `MKMapItem.openInMaps` → `Intent(ACTION_VIEW, geo: URI)`
- 전화 걸기: `tel:` URL scheme → 동일

#### ChurchListView → ChurchListSheet
- BottomSheet에 교회 목록
- 각 항목 탭 → 지도앱에서 열기

---

## 8. 유틸리티 매핑

### LocationManager

**iOS** (`@Observable`, `CLLocationManagerDelegate`):
```swift
@Observable @MainActor
final class LocationManager: NSObject, CLLocationManagerDelegate {
    var userLocation: CLLocationCoordinate2D?
    var authorizationStatus: CLAuthorizationStatus
    func requestPermission()
    func startUpdating()
    // delegate callbacks
}
```

**Android**:
```kotlin
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000).build()
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                _userLocation.value = LatLng(it.latitude, it.longitude)
            }
        }
    }
}
```

**위치 권한**: Android에서는 `rememberLauncherForActivityResult(RequestPermission())` 사용

---

## 9. 테마 & 컬러 매핑

### iOS 커스텀 컬러 (Assets.xcassets)
| 컬러 이름 | 용도 |
|-----------|------|
| `AccentColor` | 앱 강조색 (탭, 버튼, 선택) |
| `AppBackground` | 전체 배경 |
| `CardBackground` | 카드/셀 배경 |
| `GoldAccent` | 성당 마커, 묵주 구슬, 기도문 아이콘 |

### Android Material3 테마 매핑
```kotlin
// Theme.kt
private val LightColorScheme = lightColorScheme(
    primary = AccentColor,           // iOS AccentColor
    background = AppBackground,       // iOS AppBackground
    surface = CardBackground,         // iOS CardBackground
    // GoldAccent → 커스텀 확장 컬러
)

// 커스텀 컬러 확장
val ColorScheme.goldAccent: Color
    get() = Color(0xFFD4A843)  // 실제 값은 iOS Assets에서 확인
```

---

## 10. JSON 리소스

### prayers.json (28KB)
- 위치: `assets/prayers.json`
- 구조: `{ "categories": [{ "id", "name", "icon", "prayers": [{ "id", "title", "content" }] }] }`
- **주의**: `icon` 필드가 SF Symbol 이름이므로 Android Material Icon으로 매핑 필요

### korean_catholic_churches.json (423KB)
- 위치: `assets/korean_catholic_churches.json`
- 구조: `{ "churches": [{ "name", "diocese", "address", "phone", "latitude", "longitude" }] }`
- 그대로 사용 가능

---

## 11. 주요 구현 시 주의사항

### 11.1 묵주기도 상태 머신
- `advance()` 함수의 상태 전이 로직이 핵심 — iOS와 정확히 동일하게 구현 필요
- `totalSteps = 9 + 14 * numberOfDecades` 공식 유지
- `currentStepIndex` 계산 로직 동일하게 유지 (진행률 계산의 정확성)

### 11.2 Haversine 거리 계산
- `NearbyChurchViewModel`의 `haversineDistance()` 함수 — 동일 알고리즘 사용
- 지구 반경: 6,371,000 미터

### 11.3 클러스터링 알고리즘
- 카메라 거리에 따른 클러스터 반경: 0/1/2/3/4/6/10 km
- 단순 그리디 클러스터링 (seed 기반)
- Google Maps의 ClusterManager 대신 커스텀 구현 권장 (iOS와 동일한 동작 보장)

### 11.4 달력 그리드
- 첫 번째 요일의 weekday 값으로 빈칸 수 결정
- iOS `Calendar.current.component(.weekday)`: 일=1, 월=2, ..., 토=7
- Java/Kotlin `Calendar.DAY_OF_WEEK`: 동일 (일=1, 월=2, ..., 토=7)

### 11.5 접근성
- iOS 코드에 `.accessibilityLabel`, `.accessibilityHint`, `.accessibilityAddTraits` 적극 사용
- Android에서도 `contentDescription`, `semantics { }` 블록으로 동등하게 구현

### 11.6 적응형 레이아웃 (iPad → Tablet)
- iOS에서 `horizontalSizeClass` 분기하는 모든 화면:
  - CalendarTabView (HStack vs VStack)
  - PrayerCategoryListView (NavigationSplitView vs NavigationStack)
  - NearbyChurchView (HStack(Map|List) vs Map+Sheet)
- → Android `WindowSizeClass`로 동일 분기

---

## 12. 의존성 목록 (Android)

```kotlin
// build.gradle.kts (app)
dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.XX.XX"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3.adaptive:adaptive")
    implementation("androidx.compose.material3.adaptive:adaptive-layout")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.X")

    // Room
    implementation("androidx.room:room-runtime:2.6.X")
    implementation("androidx.room:room-ktx:2.6.X")
    ksp("androidx.room:room-compiler:2.6.X")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.X")
    ksp("com.google.dagger:hilt-compiler:2.51.X")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Google Maps
    implementation("com.google.maps.android:maps-compose:6.X.X")
    implementation("com.google.android.gms:play-services-maps:19.X.X")
    implementation("com.google.android.gms:play-services-location:21.X.X")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.X")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.X")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.X")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.X")
}
```

---

## 13. 구현 우선순위 (권장)

1. **프로젝트 셋업** — Gradle, Hilt, Room, Navigation, Theme
2. **데이터 모델 + Room** — Entity, DAO, Database, Repository
3. **ViewModel 레이어** — 4개 ViewModel
4. **묵주기도 탭** — 가장 복잡한 UI + 상태 머신
5. **달력 탭** — Room CRUD + 달력 그리드
6. **기도문 탭** — JSON 로드 + 검색
7. **주변 성당 탭** — Google Maps + Location
8. **네비게이션 통합** — BottomNav + NavHost
9. **테마/디자인** — Material3 커스텀 테마
10. **테스트 & 폴리싱**

---

## 14. 파일별 iOS → Android 매핑 요약

| iOS 파일 | Android 대응 파일 | 비고 |
|----------|-------------------|------|
| `ChanmiApp.swift` | `ChanmiApplication.kt` + `MainActivity.kt` | Hilt @AndroidEntryPoint |
| `ContentView.swift` | `ChanmiNavigation.kt` | BottomNav + NavHost |
| `Models/DailyRecord.swift` | `data/model/DailyRecord.kt` | @Entity |
| `Models/RosaryEntry.swift` | `data/model/RosaryEntry.kt` | @Entity + ForeignKey |
| `Models/GoodDeed.swift` | `data/model/GoodDeed.kt` | @Entity + ForeignKey |
| `Models/MysteryType.swift` | `data/model/MysteryType.kt` | enum class |
| `Models/RosaryPhase.swift` | `data/model/RosaryPhase.kt` | sealed class |
| `Models/Prayer.swift` | `data/model/Prayer.kt` | @Serializable |
| `Models/ChurchItem.swift` | `data/model/ChurchItem.kt` | data class |
| `Models/ChurchCluster.swift` | `data/model/ChurchCluster.kt` | data class |
| `Repos/.../SwiftDataCalendarRepository.swift` | `data/local/DailyRecordDao.kt` + `data/repository/CalendarRepository.kt` | Room DAO + Repository |
| `Repos/.../BundleChurchRepository.swift` | `data/repository/ChurchRepository.kt` | AssetManager |
| `Repos/.../BundlePrayerRepository.swift` | `data/repository/PrayerRepository.kt` | AssetManager |
| `ViewModels/RosaryViewModel.swift` | `ui/rosary/RosaryViewModel.kt` | @HiltViewModel |
| `ViewModels/CalendarViewModel.swift` | `ui/calendar/CalendarViewModel.kt` | @HiltViewModel |
| `ViewModels/PrayersViewModel.swift` | `ui/prayers/PrayersViewModel.kt` | @HiltViewModel |
| `ViewModels/NearbyChurchViewModel.swift` | `ui/nearbychurch/NearbyChurchViewModel.kt` | @HiltViewModel |
| `Views/Rosary/MysterySelectionView.swift` | `ui/rosary/MysterySelectionScreen.kt` | @Composable |
| `Views/Rosary/RosaryCompletionView.swift` | `ui/rosary/RosaryCompletionScreen.kt` | @Composable |
| `Views/Rosary/SwipeAdvanceModifier.swift` | `ui/rosary/SwipeAdvanceModifier.kt` | Modifier.pointerInput |
| `Views/Rosary/SwipeGuideOverlay.swift` | `ui/rosary/SwipeGuideOverlay.kt` | @Composable |
| `Views/Calendar/CalendarTabView.swift` | `ui/calendar/CalendarScreen.kt` | @Composable |
| `Views/Calendar/DayDetailView.swift` | `ui/calendar/DayDetailScreen.kt` | @Composable |
| `Views/Calendar/GoodDeedFormView.swift` | `ui/calendar/GoodDeedFormDialog.kt` | ModalBottomSheet |
| `Views/Prayers/PrayerCategoryListView.swift` | `ui/prayers/PrayerCategoryListScreen.kt` | @Composable |
| `Views/Prayers/PrayerListView.swift` | `ui/prayers/PrayerListScreen.kt` | @Composable |
| `Views/Prayers/PrayerDetailView.swift` | `ui/prayers/PrayerDetailScreen.kt` | @Composable |
| `Views/NearbyChurch/NearbyChurchView.swift` | `ui/nearbychurch/NearbyChurchScreen.kt` | GoogleMap Composable |
| `Views/NearbyChurch/ChurchListView.swift` | `ui/nearbychurch/ChurchListSheet.kt` | ModalBottomSheet |
| `Utilities/LocationManager.swift` | `location/LocationManager.kt` | FusedLocationProvider |
| `Resources/prayers.json` | `assets/prayers.json` | 그대로 복사 |
| `Resources/korean_catholic_churches.json` | `assets/korean_catholic_churches.json` | 그대로 복사 |
