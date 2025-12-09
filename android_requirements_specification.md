# 주식 데이터 분석 및 시각화 안드로이드 앱 요구사항 명세서

## 프로젝트 개요

**프로젝트명**: 주식 데이터 분석 및 시각화 안드로이드 앱  
**개발자**: 강민지 (20211317)  
**목적**: 실시간 주식 데이터 수집, 머신러닝 기반 예측 분석, 사용자 친화적 시각화를 제공하는 안드로이드 네이티브 앱 개발

---

## 1. 시스템 개요

### 1.1 시스템 목표
- 주식 투자자를 위한 모바일 중심 데이터 분석 플랫폼
- 머신러닝 모델을 활용한 주가 예측 서비스
- 실시간 매크로 경제 지표 모니터링
- 터치 최적화된 직관적인 모바일 UI/UX

### 1.2 주요 사용자
- 개인 투자자 (모바일 환경)
- 이동 중 주식 정보를 확인하는 사용자
- 빠른 시장 동향 파악이 필요한 투자자

### 1.3 플랫폼
- **타겟 플랫폼**: Android
- **최소 지원 버전**: Android 8.0 (API 26)
- **권장 버전**: Android 13+ (API 33+)
- **화면 크기**: 스마트폰 및 태블릿 지원

---

## 2. 기능 요구사항

### 2.1 스플래시 화면 (Splash Screen)

#### 2.1.1 초기 로딩 화면
**우선순위**: 높음

**기능 설명**:
- 앱 로고 및 브랜딩 표시
- 초기 데이터 로딩
- API 연결 확인

**화면 요소**:
- 앱 로고
- 로딩 인디케이터
- 버전 정보

**로딩 시간**: 2-3초

---

### 2.2 메인 화면 (Dashboard Activity)

#### 2.2.1 실시간 매크로 지표 표시
**우선순위**: 높음

**기능 설명**:
- 실시간 환율 정보 카드뷰로 표시
- 달러 인덱스(DXY) 실시간 추적
- 스와이프로 새로고침 (Pull to Refresh)
- 자동 갱신 (30초 간격)

**UI 컴포넌트**:
- RecyclerView (가로 스크롤)
- CardView (각 지표)
- SwipeRefreshLayout

**출력 데이터**:
- 환율: USD/KRW, EUR/KRW, JPY/KRW
- 달러 인덱스: 현재 지수, 변동률
- 마지막 업데이트 시간

**API 연동**:
- ExchangeRate API
- FRED API

#### 2.2.2 검색 기능
**우선순위**: 높음

**기능 설명**:
- 상단 SearchView 배치
- 기업명 또는 티커 심볼 검색
- 실시간 자동완성
- 최근 검색 기록 (SharedPreferences)
- 인기 종목 바로가기

**UI 컴포넌트**:
- SearchView
- RecyclerView (검색 결과)
- ChipGroup (최근 검색)

**입력**:
- 검색어 (기업명 또는 티커)

**출력**:
- 검색 결과 리스트
- 각 항목: 티커, 기업명, 현재가, 변동률

#### 2.2.3 인기 종목 목록
**우선순위**: 중간

**기능 설명**:
- 거래량 상위 종목 표시
- 가격 변동률 상위 종목
- 항목 클릭 시 상세 화면 이동
- 즐겨찾기 기능

**UI 컴포넌트**:
- RecyclerView (세로 스크롤)
- CardView
- ToggleButton (즐겨찾기)

---

### 2.3 기업 상세 화면 (Stock Detail Activity)

#### 2.3.1 기업 정보 헤더
**우선순위**: 높음

**기능 설명**:
- 기업명, 티커, 업종
- 현재 주가 (큰 글씨)
- 전일 대비 변동 (색상 구분)
- 공유 버튼, 즐겨찾기 버튼

**UI 컴포넌트**:
- CollapsingToolbarLayout
- FloatingActionButton (공유, 즐겨찾기)

#### 2.3.2 탭 레이아웃
**우선순위**: 높음

**기능 설명**:
- ViewPager2 + TabLayout
- 5개 탭: 차트, 재무제표, 투자지표, 배당정보, 예측분석
- 스와이프로 탭 전환

**UI 컴포넌트**:
- TabLayout
- ViewPager2
- Fragment (각 탭)

---

### 2.4 차트 Fragment

#### 2.4.1 주가 차트
**우선순위**: 높음

**기능 설명**:
- 캔들스틱 차트
- 터치 줌/스크롤 지원
- 크로스헤어 (가격 확인)
- 거래량 차트 (하단)

**차트 라이브러리**:
- MPAndroidChart 또는 AnyChart

**UI 컴포넌트**:
- CandleStickChart
- BarChart (거래량)
- ScrollView

#### 2.4.2 기술적 지표
**우선순위**: 높음

**기능 설명**:
- 이동평균선 (MA 5, 20, 60, 120)
- RSI (14일)
- MACD
- 볼린저 밴드
- 지표 선택 토글 (BottomSheet)

**UI 컴포넌트**:
- LineChart (지표)
- BottomSheetDialogFragment (지표 선택)
- CheckBox (지표 on/off)

#### 2.4.3 기간 필터
**우선순위**: 중간

**기능 설명**:
- ChipGroup으로 기간 선택
- 1개월, 3개월, 6개월, 1년, 전체

**UI 컴포넌트**:
- ChipGroup
- Chip (각 기간)

---

### 2.5 재무제표 Fragment

#### 2.5.1 재무제표 표시
**우선순위**: 높음

**기능 설명**:
- 손익계산서, 재무상태표, 현금흐름표
- 탭 또는 스피너로 전환
- 가로 스크롤 가능한 테이블
- 분기/연간 선택

**UI 컴포넌트**:
- RecyclerView (테이블)
- Spinner (재무제표 선택)
- ChipGroup (분기/연간)

**데이터 표시**:
- 최근 4개 분기 또는 연간
- 주요 항목 강조 (매출액, 영업이익, 당기순이익)

#### 2.5.2 테이블 인터랙션
**우선순위**: 중간

**기능 설명**:
- 가로 스크롤
- 항목 클릭 시 상세 설명 (Dialog)
- 긴 숫자는 K, M, B 단위로 표시

---

### 2.6 투자지표 Fragment

#### 2.6.1 지표 카드
**우선순위**: 높음

**기능 설명**:
- GridLayout으로 지표 표시
- PER, PBR, PSR, ROE, ROA 등
- 업종 평균과 비교
- 색상으로 양호/불량 표시

**UI 컴포넌트**:
- RecyclerView (Grid)
- CardView (각 지표)
- ProgressBar (업종 평균 대비)

**지표 목록**:
- 밸류에이션: PER, PBR, PSR, EV/EBITDA
- 수익성: ROE, ROA, 영업이익률, 순이익률
- 재무 안정성: 부채비율, 유동비율, 당좌비율

#### 2.6.2 지표 설명
**우선순위**: 중간

**기능 설명**:
- 지표 카드 클릭 시 설명 표시
- BottomSheet 또는 Dialog
- 계산 방법 및 해석 가이드

---

### 2.7 배당정보 Fragment

#### 2.7.1 배당 개요
**우선순위**: 중간

**기능 설명**:
- 현재 배당 수익률
- 연간 배당금
- 배당 성향
- 배당 지급 이력 차트

**UI 컴포넌트**:
- CardView (배당 정보)
- LineChart (배당 이력)

---

### 2.8 예측분석 Fragment

#### 2.8.1 예측 차트
**우선순위**: 높음

**기능 설명**:
- 실제 주가 + 3개 모델 예측
- 7일, 14일, 30일 예측 선택
- 범례 표시
- 터치로 가격 확인

**모델**:
- Linear Regression
- Random Forest
- SVM

**UI 컴포넌트**:
- LineChart (예측)
- ChipGroup (예측 기간)
- RecyclerView (범례)

#### 2.8.2 모델 성능
**우선순위**: 중간

**기능 설명**:
- 각 모델의 RMSE, MAE, R² Score
- 카드뷰로 비교
- 가장 성능 좋은 모델 강조

**UI 컴포넌트**:
- RecyclerView (가로 스크롤)
- CardView (각 모델)

#### 2.8.3 주의사항
**우선순위**: 높음

**기능 설명**:
- 상단에 경고 배너
- 예측의 한계 명시

**UI 컴포넌트**:
- MaterialBanner 또는 CardView

---

### 2.9 설정 화면 (Settings Activity)

#### 2.9.1 앱 설정
**우선순위**: 중간

**기능 설명**:
- 테마 설정 (라이트/다크/시스템)
- 알림 설정
- 자동 갱신 간격 설정
- 데이터 캐시 관리

**UI 컴포넌트**:
- PreferenceScreen
- SwitchPreference
- ListPreference

#### 2.9.2 즐겨찾기 관리
**우선순위**: 중간

**기능 설명**:
- 즐겨찾기 목록 표시
- 순서 변경 (드래그)
- 삭제

**UI 컴포넌트**:
- RecyclerView (ItemTouchHelper)

---

### 2.10 알림 기능

#### 2.10.1 가격 알림
**우선순위**: 낮음

**기능 설명**:
- 특정 가격 도달 시 알림
- 변동률 알림 (예: 5% 이상 변동)
- WorkManager로 백그라운드 체크

**UI 컴포넌트**:
- Dialog (알림 설정)
- Notification

---

## 3. 비기능 요구사항

### 3.1 성능 요구사항
- 앱 초기 실행 시간: 3초 이내
- 화면 전환: 0.5초 이내
- API 응답 시간: 2초 이내
- 메모리 사용량: 150MB 이하
- 배터리 효율: 백그라운드 최소화

### 3.2 사용성 요구사항
- Material Design 3 가이드라인 준수
- 터치 타겟: 최소 48dp x 48dp
- 한 손 사용 가능한 레이아웃
- 명확한 피드백 (로딩, 에러, 성공)
- 다크 모드 지원

### 3.3 호환성 요구사항
- Android 8.0 (API 26) 이상
- 화면 크기: 4인치 ~ 12인치
- 화면 방향: 세로/가로 모두 지원
- 다양한 해상도 대응

### 3.4 보안 요구사항
- HTTPS 통신
- API 키 암호화 (ProGuard)
- Certificate Pinning (선택사항)
- 데이터 암호화 (SharedPreferences)

### 3.5 오프라인 지원
- 마지막 조회 데이터 캐싱 (Room Database)
- 오프라인 모드 안내
- 네트워크 상태 감지

### 3.6 접근성
- TalkBack 지원
- 최소 폰트 크기 지원
- 색맹 대응 (색상만으로 정보 전달 금지)

---

## 4. 기술 스택

### 4.1 개발 언어
- **주 언어**: Kotlin
- **최소 버전**: Kotlin 1.9+

### 4.2 아키텍처
- **패턴**: MVVM (Model-View-ViewModel)
- **아키텍처 컴포넌트**: 
  - ViewModel
  - LiveData / StateFlow
  - Room Database
  - Navigation Component
  - WorkManager

### 4.3 주요 라이브러리

#### 4.3.1 네트워킹
- **Retrofit2**: REST API 통신
- **OkHttp3**: HTTP 클라이언트
- **Gson** 또는 **Moshi**: JSON 파싱

#### 4.3.2 비동기 처리
- **Coroutines**: 비동기 작업
- **Flow**: 데이터 스트림

#### 4.3.3 의존성 주입
- **Hilt** (권장) 또는 **Koin**

#### 4.3.4 차트
- **MPAndroidChart**: 주가 차트
- **AAChartCore** (선택사항)

#### 4.3.5 이미지
- **Coil** 또는 **Glide**: 이미지 로딩

#### 4.3.6 로컬 데이터베이스
- **Room**: SQLite 래퍼
- **DataStore**: 설정 저장

#### 4.3.7 머신러닝
- **TensorFlow Lite** (선택사항): 온디바이스 ML
- 또는 백엔드 서버에서 처리

### 4.4 백엔드 API
- **주가 데이터**: Yahoo Finance API (yfinance)
- **환율 데이터**: ExchangeRate-API
- **매크로 지표**: FRED API

**백엔드 옵션**:
1. Python Flask 서버 (원본 계획)
2. Firebase Functions (서버리스)
3. AWS Lambda (서버리스)

### 4.5 UI 컴포넌트
- **Material Components**: Material Design 3
- **ConstraintLayout**: 복잡한 레이아웃
- **RecyclerView**: 리스트 표시
- **ViewPager2**: 탭 스와이프
- **Navigation Component**: 화면 전환

### 4.6 테스트
- **JUnit4**: 단위 테스트
- **Espresso**: UI 테스트
- **MockK**: Mocking
- **Truth**: Assertion

### 4.7 빌드 도구
- **Gradle**: 빌드 시스템
- **ProGuard/R8**: 코드 난독화

---

## 5. 프로젝트 구조

### 5.1 패키지 구조
```
com.stockanalyzer
├── data
│   ├── model (데이터 모델)
│   ├── repository (데이터 저장소)
│   ├── api (API 인터페이스)
│   └── local (Room Database)
├── ui
│   ├── main (메인 화면)
│   ├── detail (상세 화면)
│   │   ├── chart
│   │   ├── financial
│   │   ├── metrics
│   │   ├── dividend
│   │   └── prediction
│   ├── search (검색)
│   └── settings (설정)
├── viewmodel (ViewModel)
├── util (유틸리티)
└── di (의존성 주입)
```

### 5.2 화면 구조
```
SplashActivity
    ↓
MainActivity (메인 화면)
    ├── DashboardFragment (홈)
    ├── SearchFragment (검색)
    └── FavoritesFragment (즐겨찾기)
    
StockDetailActivity (기업 상세)
    ├── ChartFragment
    ├── FinancialFragment
    ├── MetricsFragment
    ├── DividendFragment
    └── PredictionFragment
    
SettingsActivity (설정)
```

---

## 6. API 명세

### 6.1 백엔드 엔드포인트

#### GET /api/stock/info
- **설명**: 기업 기본 정보 조회
- **파라미터**: `ticker` (string)
- **응답**:
```json
{
  "ticker": "AAPL",
  "name": "Apple Inc.",
  "sector": "Technology",
  "marketCap": 3000000000000,
  "currentPrice": 180.50,
  "changePercent": 1.25,
  "high52Week": 199.62,
  "low52Week": 124.17
}
```

#### GET /api/stock/historical
- **설명**: 과거 주가 데이터
- **파라미터**: 
  - `ticker` (string)
  - `period` (string): "1mo", "3mo", "6mo", "1y", "max"
- **응답**: OHLCV 데이터 배열

#### GET /api/stock/indicators
- **설명**: 기술적 지표 계산
- **파라미터**: 
  - `ticker` (string)
  - `indicators` (array): ["ma", "rsi", "macd", "bb"]
- **응답**: 지표 데이터

#### POST /api/stock/predict
- **설명**: 주가 예측
- **요청**:
```json
{
  "ticker": "AAPL",
  "days": 7,
  "models": ["linear", "rf", "svm"]
}
```
- **응답**: 예측 데이터 + 성능 지표

#### GET /api/macro/exchange
- **설명**: 실시간 환율
- **응답**: 환율 데이터

#### GET /api/macro/dollar-index
- **설명**: 달러 인덱스
- **응답**: DXY 데이터

---

## 7. 화면 설계 (상세)

### 7.1 메인 화면 (MainActivity)

**레이아웃**:
- AppBarLayout: 검색바
- BottomNavigationView: 홈, 검색, 즐겨찾기

**DashboardFragment**:
- SwipeRefreshLayout
  - RecyclerView (매크로 지표 - 가로 스크롤)
  - RecyclerView (인기 종목 - 세로 스크롤)

### 7.2 기업 상세 화면 (StockDetailActivity)

**레이아웃**:
- CoordinatorLayout
  - AppBarLayout
    - CollapsingToolbarLayout
      - 기업 정보 헤더
  - TabLayout (고정)
  - ViewPager2 (탭 컨텐츠)
  - FloatingActionButton (즐겨찾기)

### 7.3 차트 Fragment

**레이아웃**:
- ScrollView
  - ChipGroup (기간 필터)
  - CandleStickChart (주가)
  - BarChart (거래량)
  - Button (지표 선택)

### 7.4 예측분석 Fragment

**레이아웃**:
- ScrollView
  - MaterialBanner (주의사항)
  - ChipGroup (예측 기간)
  - LineChart (예측 차트)
  - RecyclerView (모델 성능 카드 - 가로 스크롤)
  - CardView (예측 요약)

---

## 8. 데이터 모델

### 8.1 주요 Data Class

```kotlin
data class Stock(
    val ticker: String,
    val name: String,
    val currentPrice: Double,
    val changePercent: Double,
    val marketCap: Long,
    val sector: String
)

data class StockHistory(
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

data class TechnicalIndicator(
    val date: String,
    val ma5: Double?,
    val ma20: Double?,
    val rsi: Double?,
    val macd: Double?
)

data class Prediction(
    val date: String,
    val linearRegression: Double,
    val randomForest: Double,
    val svm: Double
)

data class MacroIndicator(
    val type: String,
    val value: Double,
    val changePercent: Double,
    val timestamp: Long
)
```

### 8.2 Room Entity

```kotlin
@Entity(tableName = "favorites")
data class FavoriteStock(
    @PrimaryKey val ticker: String,
    val name: String,
    val addedAt: Long
)

@Entity(tableName = "cache")
data class StockCache(
    @PrimaryKey val ticker: String,
    val data: String, // JSON
    val cachedAt: Long
)
```

---

## 9. 개발 일정

### Phase 1: 프로젝트 설정 및 기본 구조 (1주)
- 프로젝트 생성 및 의존성 설정
- 패키지 구조 설계
- 백엔드 API 서버 구축
- 네트워킹 레이어 구현

### Phase 2: 메인 화면 개발 (2주)
- SplashActivity 구현
- MainActivity + BottomNavigation
- DashboardFragment (매크로 지표, 인기 종목)
- SearchFragment
- API 연동

### Phase 3: 기업 상세 화면 - 기본 (2주)
- StockDetailActivity 구조
- 차트 Fragment (캔들스틱, 거래량)
- 기술적 지표 구현
- 차트 라이브러리 통합

### Phase 4: 기업 상세 화면 - 추가 탭 (2주)
- 재무제표 Fragment
- 투자지표 Fragment
- 배당정보 Fragment

### Phase 5: 예측 모델 통합 (1.5주)
- 백엔드 ML 모델 API 연동
- 예측분석 Fragment
- 모델 성능 시각화

### Phase 6: 추가 기능 및 최적화 (1주)
- 즐겨찾기 기능
- 설정 화면
- 다크 모드
- 캐싱 및 오프라인 지원

### Phase 7: 테스트 및 디버깅 (0.5주)
- 단위 테스트
- UI 테스트
- 버그 수정
- 성능 최적화

### Phase 8: 배포 준비 (0.5주)
- ProGuard 설정
- 아이콘 및 스플래시 디자인
- Google Play Store 등록 준비
- 문서화

**총 개발 기간**: 약 10주

---

## 10. 테스트 계획

### 10.1 단위 테스트
- Repository 테스트
- ViewModel 테스트
- 데이터 변환 로직 테스트

### 10.2 UI 테스트
- Espresso로 주요 화면 테스트
- 네비게이션 테스트
- 사용자 시나리오 테스트

### 10.3 통합 테스트
- API 연동 테스트
- 데이터베이스 테스트

### 10.4 성능 테스트
- 메모리 누수 확인 (LeakCanary)
- 네트워크 성능
- UI 렌더링 성능

---

## 11. 배포 계획

### 11.1 Google Play Store
- **앱 이름**: Stock Analyzer
- **카테고리**: 금융
- **등급**: 전체 이용가
- **스크린샷**: 5개 (주요 화면)
- **설명**: 한글/영문

### 11.2 버전 관리
- **버전 1.0.0**: MVP (핵심 기능)
- **버전 1.1.0**: 알림 기능 추가
- **버전 1.2.0**: 뉴스 통합

### 11.3 APK 빌드
- Release 빌드 설정
- ProGuard 적용
- 서명 키 생성

---

## 12. 위험 관리

### 12.1 기술적 위험
- **외부 API 장애**: 캐싱 및 에러 처리
- **차트 성능 이슈**: 데이터 샘플링, 최적화
- **ML 모델 지연**: 로딩 인디케이터, 타임아웃 설정
- **메모리 부족**: 이미지 최적화, 데이터 페이징

### 12.2 일정 위험
- **개발 지연**: MVP 우선, 추가 기능 후순위
- **API 연동 문제**: Mock 데이터로 병렬 개발

---

## 13. 향후 개선 사항

### 13.1 v2 기능
- 포트폴리오 관리
- 실시간 알림 (가격, 뉴스)
- 위젯 (홈 화면)
- 뉴스 통합 (크롤링 및 감성 분석)
- 소셜 기능 (의견 공유)

### 13.2 기술 개선
- Jetpack Compose 전환 (UI)
- 온디바이스 ML (TensorFlow Lite)
- GraphQL API
- Wear OS 지원

### 13.3 수익화 (선택사항)
- 프리미엄 기능 (구독)
- 광고 (AdMob)

---

## 14. 개발 환경 설정

### 14.1 필수 도구
- **Android Studio**: Hedgehog (2023.1.1) 이상
- **JDK**: 17
- **Gradle**: 8.0+
- **에뮬레이터** 또는 **실제 디바이스**

### 14.2 Gradle 의존성 (예시)

```gradle
// Kotlin
implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.0"

// AndroidX
implementation "androidx.core:core-ktx:1.12.0"
implementation "androidx.appcompat:appcompat:1.6.1"
implementation "androidx.constraintlayout:constraintlayout:2.1.4"

// Material Design
implementation "com.google.android.material:material:1.11.0"

// Navigation
implementation "androidx.navigation:navigation-fragment-ktx:2.7.6"
implementation "androidx.navigation:navigation-ui-ktx:2.7.6"

// Lifecycle
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"

// Retrofit
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-gson:2.9.0"
implementation "com.squareup.okhttp3:logging-interceptor:4.12.0"

// Room
implementation "androidx.room:room-runtime:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"

// Hilt
implementation "com.google.dagger:hilt-android:2.48"
kapt "com.google.dagger:hilt-compiler:2.48"

// Chart
implementation "com.github.PhilJay:MPAndroidChart:v3.1.0"

// Image Loading
implementation "io.coil-kt:coil:2.5.0"

// DataStore
implementation "androidx.datastore:datastore-preferences:1.0.0"

// WorkManager
implementation "androidx.work:work-runtime-ktx:2.9.0"
```

---

## 15. 참고 자료

### 15.1 Android 개발
- Android Developers: https://developer.android.com/
- Material Design 3: https://m3.material.io/
- Kotlin 문서: https://kotlinlang.org/docs/

### 15.2 라이브러리
- Retrofit: https://square.github.io/retrofit/
- MPAndroidChart: https://github.com/PhilJay/MPAndroidChart
- Hilt: https://dagger.dev/hilt/

### 15.3 API
- Yahoo Finance API: https://github.com/ranaroussi/yfinance
- ExchangeRate-API: https://www.exchangerate-api.com/
- FRED API: https://fred.stlouisfed.org/docs/api/

### 15.4 api 키 값
- FRED: Authorization: Bearer abcdefghijklmnopqrstuvwxyz123456
- exchangeRate-api: b0291ce736032066c1f7351c
---

**문서 버전**: 1.0  
**최종 수정일**: 2024-12-05  
**작성자**: 강민지 (20211317)  
**플랫폼**: Android
