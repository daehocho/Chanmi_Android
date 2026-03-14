package com.chanmi.app.ui.prayers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chanmi.app.data.model.Prayer
import com.chanmi.app.data.model.PrayerCategory
import com.chanmi.app.ui.theme.ChanmiIcons
import com.chanmi.app.ui.theme.chanmiColors

private const val MIN_FONT_SIZE = 14f
private const val MAX_FONT_SIZE = 32f
private const val FONT_STEP = 2f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerCategoryListScreen(
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    onNavigateToList: (categoryId: String, categoryName: String) -> Unit,
    onNavigateToDetail: (prayerId: String) -> Unit,
    viewModel: PrayersViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    var searchActive by rememberSaveable { mutableStateOf(false) }

    val isWide = widthSizeClass == WindowWidthSizeClass.Expanded || widthSizeClass == WindowWidthSizeClass.Medium

    if (isWide) {
        // 태블릿/폴드 펼침: 카테고리 | 기도 목록 | 기도 상세 (3패인)
        PrayerAdaptiveLayout(
            categories = categories,
            searchQuery = searchQuery,
            searchResults = searchResults,
            searchActive = searchActive,
            onSearchActiveChange = { searchActive = it },
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            viewModel = viewModel,
        )
    } else {
        // 폰: 기존 단일 컬럼 네비게이션
        PrayerCompactLayout(
            categories = categories,
            searchQuery = searchQuery,
            searchResults = searchResults,
            searchActive = searchActive,
            onSearchActiveChange = { searchActive = it },
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onNavigateToList = onNavigateToList,
            onNavigateToDetail = onNavigateToDetail,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerAdaptiveLayout(
    categories: List<PrayerCategory>,
    searchQuery: String,
    searchResults: List<Prayer>,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    viewModel: PrayersViewModel,
) {
    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPrayerId by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedCategory = remember(categories, selectedCategoryId) {
        categories.find { it.id == selectedCategoryId }
    }
    val selectedPrayer = remember(categories, selectedPrayerId) {
        categories.flatMap { it.prayers }.find { it.id == selectedPrayerId }
    }
    val fontSize by viewModel.prayerFontSize.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("기도문") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1패인: 카테고리 목록 + 검색
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
            ) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChange,
                            onSearch = { onSearchActiveChange(false) },
                            expanded = searchActive,
                            onExpandedChange = onSearchActiveChange,
                            placeholder = { Text("기도문 검색") },
                            leadingIcon = {
                                if (searchActive) {
                                    IconButton(onClick = {
                                        onSearchActiveChange(false)
                                        onSearchQueryChange("")
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "검색 닫기")
                                    }
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "검색어 지우기")
                                    }
                                }
                            }
                        )
                    },
                    expanded = searchActive,
                    onExpandedChange = onSearchActiveChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (searchActive) 0.dp else 8.dp),
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults, key = { it.id }) { prayer ->
                            PrayerSearchResultCard(
                                prayer = prayer,
                                onClick = {
                                    onSearchActiveChange(false)
                                    selectedPrayerId = prayer.id
                                }
                            )
                        }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        PrayerCategoryCard(
                            category = category,
                            isSelected = category.id == selectedCategoryId,
                            onClick = {
                                selectedCategoryId = category.id
                                selectedPrayerId = null
                            }
                        )
                    }
                }
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            // 2패인: 선택된 카테고리의 기도 목록
            if (selectedCategory != null) {
                Column(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = selectedCategory!!.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedCategory!!.prayers, key = { it.id }) { prayer ->
                            PrayerListCard(
                                prayer = prayer,
                                isSelected = prayer.id == selectedPrayerId,
                                onClick = { selectedPrayerId = prayer.id }
                            )
                        }
                    }
                }

                VerticalDivider(modifier = Modifier.fillMaxHeight())
            }

            // 3패인: 기도문 상세
            Box(
                modifier = Modifier
                    .weight(if (selectedCategory != null) 0.4f else 0.7f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                if (selectedPrayer != null) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 기도문 제목 + 폰트 크기 조절
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPrayer!!.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { if (fontSize > MIN_FONT_SIZE) viewModel.updatePrayerFontSize(fontSize - FONT_STEP) },
                                enabled = fontSize > MIN_FONT_SIZE
                            ) {
                                Text("가", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                "${fontSize.toInt()}pt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(
                                onClick = { if (fontSize < MAX_FONT_SIZE) viewModel.updatePrayerFontSize(fontSize + FONT_STEP) },
                                enabled = fontSize < MAX_FONT_SIZE
                            ) {
                                Text("가", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        // 기도문 본문
                        Text(
                            text = selectedPrayer!!.content,
                            style = TextStyle(
                                fontSize = fontSize.sp,
                                lineHeight = (fontSize + 6).sp
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        )
                    }
                } else {
                    // 빈 상태
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(
                            if (selectedCategory == null) "카테고리를 선택하세요" else "기도문을 선택하세요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerCompactLayout(
    categories: List<PrayerCategory>,
    searchQuery: String,
    searchResults: List<Prayer>,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToList: (categoryId: String, categoryName: String) -> Unit,
    onNavigateToDetail: (prayerId: String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("기도문") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearch = { onSearchActiveChange(false) },
                        expanded = searchActive,
                        onExpandedChange = onSearchActiveChange,
                        placeholder = { Text("기도문 검색") },
                        leadingIcon = {
                            if (searchActive) {
                                IconButton(onClick = {
                                    onSearchActiveChange(false)
                                    onSearchQueryChange("")
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "검색 닫기")
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "검색어 지우기")
                                }
                            }
                        }
                    )
                },
                expanded = searchActive,
                onExpandedChange = onSearchActiveChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (searchActive) 0.dp else 16.dp),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults, key = { it.id }) { prayer ->
                        PrayerSearchResultCard(
                            prayer = prayer,
                            onClick = {
                                onSearchActiveChange(false)
                                onNavigateToDetail(prayer.id)
                            }
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories, key = { it.id }) { category ->
                    PrayerCategoryCard(
                        category = category,
                        onClick = { onNavigateToList(category.id, category.name) }
                    )
                }
            }
        }
    }
}

// ===== 공통 컴포넌트 =====

@Composable
private fun PrayerCategoryCard(
    category: PrayerCategory,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val accessibilityDesc = "${category.name}, 기도문 ${category.prayers.size}개"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = accessibilityDesc }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.chanmiColors.goldAccent.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = ChanmiIcons.fromSfSymbol(category.icon),
                    contentDescription = null,
                    tint = MaterialTheme.chanmiColors.goldAccent,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Category info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = category.prayers.take(3).joinToString(", ") { it.title },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Prayer count
            Text(
                text = "${category.prayers.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PrayerListCard(
    prayer: Prayer,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prayer.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PrayerSearchResultCard(
    prayer: Prayer,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prayer.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
