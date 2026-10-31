package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TchiBorder
import com.example.ui.theme.TchiCard
import com.example.ui.theme.TchiPrimary
import com.example.ui.theme.TchiSecondaryText
import com.example.ui.theme.TchiWhite
import com.example.util.VndFormatter

private val ChartColors = listOf(
    Color(0xFF38BDF8), // Primary Sky Blue
    Color(0xFF22C55E), // Green
    Color(0xFFF59E0B), // Amber
    Color(0xFFEC4899), // Pink
    Color(0xFF8B5CF6), // Purple
    Color(0xFF14B8A6), // Teal
    Color(0xFFEF4444), // Red
    Color(0xFF6366F1), // Indigo
    Color(0xFFEAB308), // Yellow
    Color(0xFF06B6D4)  // Cyan
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDoughnutChart(
    categoryTotals: Map<String, Long>,
    modifier: Modifier = Modifier
) {
    if (categoryTotals.isEmpty()) return

    val totalSpent = categoryTotals.values.sum().coerceAtLeast(1L)
    val entries = categoryTotals.entries.sortedByDescending { it.value }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TchiCard, RoundedCornerShape(16.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Phân bổ theo danh mục",
            color = TchiWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                var startAngle = -90f
                val strokeWidth = 24.dp.toPx()

                entries.forEachIndexed { index, entry ->
                    val sweepAngle = (entry.value.toFloat() / totalSpent.toFloat()) * 360f
                    val color = ChartColors[index % ChartColors.size]

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tổng",
                    color = TchiSecondaryText,
                    fontSize = 11.sp
                )
                Text(
                    text = VndFormatter.formatVnd(totalSpent),
                    color = TchiPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Legend with percentage and VNĐ
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            entries.forEachIndexed { index, entry ->
                val percent = ((entry.value.toFloat() / totalSpent.toFloat()) * 100).toInt()
                val color = ChartColors[index % ChartColors.size]

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF071521), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${entry.key} ($percent%)",
                        color = TchiWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = VndFormatter.formatVnd(entry.value),
                        color = TchiSecondaryText,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyBarChart(
    monthlyTrend: List<Pair<String, Long>>,
    modifier: Modifier = Modifier
) {
    if (monthlyTrend.isEmpty()) return

    val maxAmount = monthlyTrend.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TchiCard, RoundedCornerShape(16.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Xu hướng chi tiêu theo tháng",
            color = TchiWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            monthlyTrend.forEachIndexed { index, item ->
                val barFraction = if (maxAmount > 0) (item.second.toFloat() / maxAmount.toFloat()).coerceIn(0.04f, 1f) else 0.04f
                val isLatest = index == monthlyTrend.size - 1

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (item.second > 0) {
                        Text(
                            text = if (item.second >= 1_000_000) "${item.second / 1_000_000}tr" else "${item.second / 1_000}k",
                            color = if (isLatest) TchiPrimary else TchiSecondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height((100 * barFraction).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (isLatest) TchiPrimary else Color(0xFF164E70)
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.first,
                        color = if (isLatest) TchiPrimary else TchiSecondaryText,
                        fontSize = 11.sp,
                        fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
