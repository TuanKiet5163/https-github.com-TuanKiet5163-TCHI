package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TchiBorder
import com.example.ui.theme.TchiCard
import com.example.ui.theme.TchiPrimary
import com.example.ui.theme.TchiSecondaryText
import com.example.ui.theme.TchiWhite

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String? = null,
    totalHighlightText: String? = null,
    testTag: String = "empty_state",
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(TchiCard, RoundedCornerShape(20.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(20.dp))
            .padding(32.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(TchiPrimary.copy(alpha = 0.12f))
                    .border(1.dp, TchiPrimary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = TchiPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                color = TchiWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = TchiSecondaryText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (totalHighlightText != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF071521), RoundedCornerShape(12.dp))
                        .border(1.dp, TchiBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Tổng chi tiêu: $totalHighlightText",
                        color = TchiPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }

            if (buttonText != null && onButtonClick != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiPrimary,
                        contentColor = Color(0xFF071521)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("${testTag}_button")
                ) {
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
