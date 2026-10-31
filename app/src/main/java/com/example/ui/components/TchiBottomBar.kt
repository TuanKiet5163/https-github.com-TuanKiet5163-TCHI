package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TchiBackground
import com.example.ui.theme.TchiBorder
import com.example.ui.theme.TchiCard
import com.example.ui.theme.TchiPrimary
import com.example.ui.theme.TchiSecondaryText
import com.example.viewmodel.TchiTab

@Composable
fun TchiBottomBar(
    currentTab: TchiTab,
    onTabSelected: (TchiTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, ambientColor = Color.Black, spotColor = TchiPrimary.copy(alpha = 0.2f)),
        color = TchiCard,
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = TchiBorder)
                .height(72.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Nhật ký
                BottomNavItem(
                    label = "Nhật ký",
                    icon = Icons.AutoMirrored.Filled.LibraryBooks,
                    isSelected = currentTab == TchiTab.JOURNAL,
                    testTag = "nav_journal",
                    onClick = { onTabSelected(TchiTab.JOURNAL) }
                )

                // Tab 2: Khám phá
                BottomNavItem(
                    label = "Khám phá",
                    icon = Icons.Default.Explore,
                    isSelected = currentTab == TchiTab.DISCOVER,
                    testTag = "nav_discover",
                    onClick = { onTabSelected(TchiTab.DISCOVER) }
                )

                // Center Spacer for protruding Camera Button
                Box(modifier = Modifier.size(56.dp))

                // Tab 4: Thống kê
                BottomNavItem(
                    label = "Thống kê",
                    icon = Icons.Default.InsertChart,
                    isSelected = currentTab == TchiTab.STATS,
                    testTag = "nav_stats",
                    onClick = { onTabSelected(TchiTab.STATS) }
                )

                // Tab 5: Cá nhân
                BottomNavItem(
                    label = "Cá nhân",
                    icon = Icons.Default.Person,
                    isSelected = currentTab == TchiTab.PROFILE,
                    testTag = "nav_profile",
                    onClick = { onTabSelected(TchiTab.PROFILE) }
                )
            }

            // Center Camera Button (larger, highlighted in blue)
            Box(
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF67E8F9), TchiPrimary)
                        )
                    )
                    .border(3.dp, TchiBackground, CircleShape)
                    .shadow(10.dp, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(TchiTab.CAMERA) }
                    .testTag("nav_camera_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Chụp ảnh chi tiêu",
                    tint = Color(0xFF071521),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val iconColor = animateColorAsState(
        targetValue = if (isSelected) TchiPrimary else TchiSecondaryText,
        label = "nav_icon_color"
    )

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor.value,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = iconColor.value,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
