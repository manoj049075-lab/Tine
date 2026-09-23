package com.example.ui.milestones

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.DailyEntry
import com.example.data.JourneyStats
import com.example.data.Milestone
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.SoftBlue
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date
import java.util.Locale

@Composable
fun MilestonesScreen(
    milestones: List<Milestone>,
    entries: List<DailyEntry>,
    stats: JourneyStats,
    modifier: Modifier = Modifier
) {
    var inspectedEntry by remember { mutableStateOf<DailyEntry?>(null) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    // Map dateString -> DailyEntry for fast lookups
    val entriesByDate = remember(entries) {
        entries.associateBy { it.dateString }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("milestones_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Journey & Milestones",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track your daily consistency and unlocked achievements",
                    color = Slate400,
                    fontSize = 13.sp
                )
            }

            // Monthly Calendar Card
            item {
                MonthlyCalendarCard(
                    yearMonth = currentYearMonth,
                    entriesByDate = entriesByDate,
                    onPrevMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
                    onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) },
                    onDateClick = { dateStr ->
                        val entry = entriesByDate[dateStr]
                        if (entry != null) {
                            inspectedEntry = entry
                        }
                    }
                )
            }

            // Milestones Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACHIEVEMENTS",
                        color = Slate400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    val unlockedCount = milestones.count { it.isUnlocked }
                    Text(
                        text = "$unlockedCount / ${milestones.size} Unlocked",
                        color = ElectricGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Milestone Items
            items(milestones) { milestone ->
                MilestoneItemCard(milestone = milestone)
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Inspected Date Modal
        if (inspectedEntry != null) {
            DateInspectionModal(
                entry = inspectedEntry!!,
                onDismiss = { inspectedEntry = null }
            )
        }
    }
}

@Composable
fun MonthlyCalendarCard(
    yearMonth: YearMonth,
    entriesByDate: Map<String, DailyEntry>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateClick: (String) -> Unit
) {
    val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.US).format(
        SimpleDateFormat("yyyy-MM", Locale.US).parse("${yearMonth.year}-${String.format("%02d", yearMonth.monthValue)}") ?: Date()
    )

    val today = LocalDate.now()
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value // 1 (Mon) - 7 (Sun)
    val leadingEmptyDays = (firstDayOfWeek - 1).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Slate900)
            .border(1.dp, Slate850, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("monthly_calendar_card")
    ) {
        Column {
            // Header: Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthTitle,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = Slate400
                        )
                    }
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = Slate400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weekday Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(
                        text = day,
                        color = Slate400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day Grid (up to 6 weeks)
            val totalCells = leadingEmptyDays + daysInMonth
            val totalRows = (totalCells + 6) / 7

            for (row in 0 until totalRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - leadingEmptyDays + 1

                        if (dayNumber in 1..daysInMonth) {
                            val dateStr = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                yearMonth.year,
                                yearMonth.monthValue,
                                dayNumber
                            )
                            val hasEntry = entriesByDate.containsKey(dateStr)
                            val isToday = (yearMonth.year == today.year &&
                                    yearMonth.monthValue == today.monthValue &&
                                    dayNumber == today.dayOfMonth)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isToday) ElectricGreen.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        if (isToday) 1.dp else 0.dp,
                                        if (isToday) ElectricGreen else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = hasEntry) {
                                        onDateClick(dateStr)
                                    }
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    color = if (hasEntry) Color.White else Slate400,
                                    fontSize = 12.sp,
                                    fontWeight = if (hasEntry || isToday) FontWeight.Bold else FontWeight.Normal
                                )

                                // Green dot indicator for captured day
                                if (hasEntry) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(ElectricGreen)
                                    )
                                }
                            }
                        } else {
                            // Blank cell
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneItemCard(milestone: Milestone) {
    val progress = (milestone.currentCount.toFloat() / milestone.targetCount.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Slate900)
            .border(
                1.dp,
                if (milestone.isUnlocked) ElectricGreen.copy(alpha = 0.5f) else Slate850,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .testTag("milestone_item_${milestone.id}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (milestone.isUnlocked) ElectricGreen.copy(alpha = 0.15f)
                        else Slate850
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (milestone.isUnlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                    contentDescription = milestone.title,
                    tint = if (milestone.isUnlocked) ElectricGreen else Slate400,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = milestone.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (milestone.isUnlocked) "UNLOCKED" else "${milestone.currentCount}/${milestone.targetCount}",
                        color = if (milestone.isUnlocked) ElectricGreen else Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = milestone.description,
                    color = Slate400,
                    fontSize = 12.sp
                )

                if (!milestone.isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = ElectricGreen,
                        trackColor = Slate800
                    )
                }
            }
        }
    }
}

// Date Inspection Dialog
@Composable
fun DateInspectionModal(
    entry: DailyEntry,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Slate900)
                .border(1.dp, Slate800, RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("date_inspection_dialog")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.dateString,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Photo Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.5.dp, ElectricGreen, RoundedCornerShape(14.dp))
            ) {
                AsyncImage(
                    model = File(entry.imagePath),
                    contentDescription = "Daily Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metadata Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Alignment Score",
                    color = Slate400,
                    fontSize = 12.sp
                )
                Text(
                    text = "${entry.alignmentScore}%",
                    color = ElectricGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (entry.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DAILY NOTE",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.note,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close", color = Color.White)
            }
        }
    }
}
