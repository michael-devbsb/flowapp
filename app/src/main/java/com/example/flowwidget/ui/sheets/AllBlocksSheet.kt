package com.example.flowwidget.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flowwidget.data.local.RoutineBlock
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllBlocksSheet(
    routines: List<RoutineBlock>,
    onEdit: (RoutineBlock) -> Unit,
    onDelete: (RoutineBlock) -> Unit,
    onClick: (RoutineBlock) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Todos os Blocos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (routines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum bloco cadastrado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                val (fixed, punctual) = remember(routines) {
                    routines.partition { it.isFixed }
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (punctual.isNotEmpty()) {
                        item {
                            Text(
                                text = "Pontuais",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }

                    items(punctual, key = { it.id }) { block ->
                        AllBlockItem(
                            block = block,
                            onClick = { onClick(block) }
                        )
                    }

                    if (punctual.isNotEmpty() && fixed.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }

                    if (fixed.isNotEmpty()) {
                        item {
                            Text(
                                text = "Fixas",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }

                    items(fixed, key = { it.id }) { block ->
                        AllBlockItem(
                            block = block,
                            onClick = { onClick(block) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AllBlockItem(
    block: RoutineBlock,
    onClick: () -> Unit
) {
    val blockColor = try {
        Color(android.graphics.Color.parseColor(block.colorHex))
    } catch (e: Exception) {
        Color.Gray
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(blockColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (block.isCompleted && !block.isFixed) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                    textDecoration = if (block.isCompleted && !block.isFixed) {
                        TextDecoration.LineThrough
                    } else {
                        null
                    }
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${block.startTime} - ${block.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (block.isFixed) {
                        WeekdayMinimalist(block.selectedDays ?: "")
                    } else {
                        val formattedDate = remember(block.date) {
                            block.date?.let {
                                try {
                                    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    val date = parser.parse(it)
                                    if (date != null) formatter.format(date) else it
                                } catch (e: Exception) {
                                    it
                                }
                            } ?: "Pontual"
                        }
                        Text(
                            text = "• $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayMinimalist(selectedDays: String) {
    val days = listOf("D", "S", "T", "Q", "Q", "S", "S")
    val selectedList = selectedDays.split(",").mapNotNull { it.toIntOrNull() }
    
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        days.forEachIndexed { index, label ->
            val dayInt = index + 1
            val isSelected = selectedList.contains(dayInt)
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
