package com.chanmi.app.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.chanmi.app.data.model.GoodDeed
import com.chanmi.app.data.model.MysteryType

// ===== 선행 추가/수정 BottomSheet =====

private val goodDeedCategories = listOf(
    "service" to "봉사",
    "donation" to "기부",
    "prayer" to "기도",
    "forgiveness" to "용서",
    "other" to "기타"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodDeedFormDialog(
    editingDeed: GoodDeed? = null,
    onDismiss: () -> Unit,
    onSave: (content: String, category: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var content by remember { mutableStateOf(editingDeed?.content ?: "") }
    var selectedCategory by remember { mutableStateOf(editingDeed?.category ?: "other") }
    var categoryExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val isEditing = editingDeed != null
    val isSaveEnabled = content.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (isEditing) "선행 수정" else "선행 추가",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { onSave(content.trim(), selectedCategory) },
                    enabled = isSaveEnabled
                ) {
                    Text("저장")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 선행 내용 입력
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("선행 내용을 입력하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 카테고리 선택
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = goodDeedCategories.first { it.first == selectedCategory }.second,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("카테고리") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    goodDeedCategories.forEach { (key, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedCategory = key
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

// ===== 묵주기도 수동 추가 BottomSheet =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRosaryDialog(
    onDismiss: () -> Unit,
    onSave: (mysteryType: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedMystery by remember { mutableStateOf(MysteryType.JOYFUL) }
    var mysteryExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "묵주기도 추가",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { onSave(selectedMystery.key) }) {
                    Text("저장")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 신비 유형 선택
            ExposedDropdownMenuBox(
                expanded = mysteryExpanded,
                onExpandedChange = { mysteryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedMystery.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("신비 유형") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mysteryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = mysteryExpanded,
                    onDismissRequest = { mysteryExpanded = false }
                ) {
                    MysteryType.entries.forEach { mystery ->
                        DropdownMenuItem(
                            text = { Text(mystery.displayName) },
                            onClick = {
                                selectedMystery = mystery
                                mysteryExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
