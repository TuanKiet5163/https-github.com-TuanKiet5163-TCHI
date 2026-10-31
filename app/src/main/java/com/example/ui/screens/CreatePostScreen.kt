package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TchiBackground
import com.example.ui.theme.TchiBorder
import com.example.ui.theme.TchiCard
import com.example.ui.theme.TchiInputBackground
import com.example.ui.theme.TchiPrimary
import com.example.ui.theme.TchiSecondaryBlue
import com.example.ui.theme.TchiSecondaryText
import com.example.ui.theme.TchiWhite
import com.example.util.VndFormatter
import com.example.viewmodel.TchiViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DefaultCategories = listOf(
    "Ăn uống", "Mua sắm", "Quần áo", "Đồ điện tử",
    "Di chuyển", "Học tập", "Giải trí", "Sức khỏe",
    "Làm đẹp", "Hóa đơn", "Khác"
)

private val DefaultPaymentMethods = listOf(
    "Tiền mặt", "Chuyển khoản", "Thẻ ngân hàng", "Khác"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    photoPath: String,
    viewModel: TchiViewModel,
    onPostCreated: () -> Unit,
    onBack: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var rawAmountInput by remember { mutableStateOf("") } // digits only
    var selectedCategory by remember { mutableStateOf(DefaultCategories[0]) }
    var selectedPaymentMethod by remember { mutableStateOf(DefaultPaymentMethods[0]) }
    var note by remember { mutableStateOf("") }
    var selectedVisibility by remember { mutableStateOf("PRIVATE") } // Default PRIVATE as requested

    val scrollState = rememberScrollState()
    val formattedDate = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    val bitmap = remember(photoPath) {
        BitmapFactory.decodeFile(photoPath)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TchiBackground)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = TchiWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ghi nhận chi tiêu",
                    color = TchiWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Photo Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, TchiBorder, RoundedCornerShape(16.dp))
                    .background(TchiCard),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Ảnh sản phẩm",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Ảnh chụp món đồ", color = TchiSecondaryText)
                }

                // Overlay pill badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = formattedDate,
                        color = TchiWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Product Name Field
            Text(
                text = "Tên món đồ *",
                color = TchiWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                placeholder = { Text("vd: Cà phê muối, Tai nghe bluetooth...", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = TchiPrimary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_product_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TchiInputBackground,
                    unfocusedContainerColor = TchiInputBackground,
                    focusedBorderColor = TchiPrimary,
                    unfocusedBorderColor = TchiBorder,
                    focusedTextColor = TchiWhite,
                    unfocusedTextColor = TchiWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount VNĐ Field (MANDATORY VNĐ ONLY)
            Text(
                text = "Số tiền đã chi (VNĐ) *",
                color = TchiWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = VndFormatter.formatInput(rawAmountInput),
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    if (digits.length <= 12) { // up to 999 billion VNĐ
                        rawAmountInput = digits
                    }
                },
                placeholder = { Text("0 VNĐ", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = TchiPrimary) },
                trailingIcon = {
                    Text(
                        text = "VNĐ",
                        color = TchiPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_amount_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TchiInputBackground,
                    unfocusedContainerColor = TchiInputBackground,
                    focusedBorderColor = TchiPrimary,
                    unfocusedBorderColor = TchiBorder,
                    focusedTextColor = TchiWhite,
                    unfocusedTextColor = TchiWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Category Chips
            Text(
                text = "Danh mục",
                color = TchiWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DefaultCategories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) TchiPrimary else TchiCard)
                            .border(1.dp, if (isSelected) TchiPrimary else TchiBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("category_chip_$category")
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color(0xFF071521) else TchiWhite,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Payment Method
            Text(
                text = "Phương thức thanh toán",
                color = TchiWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DefaultPaymentMethods.forEach { method ->
                    val isSelected = selectedPaymentMethod == method
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) TchiSecondaryBlue else TchiCard)
                            .border(1.dp, if (isSelected) TchiPrimary else TchiBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedPaymentMethod = method }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = method,
                            color = if (isSelected) TchiWhite else TchiSecondaryText,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Note Field
            Text(
                text = "Ghi chú (tùy chọn)",
                color = TchiWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Thêm cảm nhận, lý do mua món đồ này...", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = TchiSecondaryText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_note_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TchiInputBackground,
                    unfocusedContainerColor = TchiInputBackground,
                    focusedBorderColor = TchiPrimary,
                    unfocusedBorderColor = TchiBorder,
                    focusedTextColor = TchiWhite,
                    unfocusedTextColor = TchiWhite
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy Selection
            Text(
                text = "Quyền riêng tư bài đăng",
                color = TchiWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TchiCard, RoundedCornerShape(12.dp))
                    .border(1.dp, TchiBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                // PRIVATE
                PrivacyOptionRow(
                    title = "PRIVATE - Riêng tư (Khuyên dùng)",
                    description = "Chỉ một mình bạn xem được bài đăng này",
                    icon = Icons.Default.Lock,
                    isSelected = selectedVisibility == "PRIVATE",
                    onClick = { selectedVisibility = "PRIVATE" }
                )

                // FRIENDS
                PrivacyOptionRow(
                    title = "FRIENDS - Bạn bè",
                    description = "Chỉ những người đã kết bạn trên TCHI mới xem được",
                    icon = Icons.Default.People,
                    isSelected = selectedVisibility == "FRIENDS",
                    onClick = { selectedVisibility = "FRIENDS" }
                )

                // PUBLIC
                PrivacyOptionRow(
                    title = "PUBLIC - Công khai",
                    description = "Mọi người trên TCHI đều có thể thấy món đồ này trong Khám phá",
                    icon = Icons.Default.Public,
                    isSelected = selectedVisibility == "PUBLIC",
                    onClick = { selectedVisibility = "PUBLIC" }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            Button(
                onClick = {
                    val amountLong = rawAmountInput.toLongOrNull() ?: 0L
                    if (productName.isBlank()) {
                        viewModel.showToast("Vui lòng nhập tên món đồ")
                    } else if (amountLong <= 0L) {
                        viewModel.showToast("Vui lòng nhập số tiền chi tiêu hợp lệ")
                    } else {
                        viewModel.createPost(
                            imageUri = photoPath,
                            productName = productName,
                            amountVnd = amountLong,
                            category = selectedCategory,
                            paymentMethod = selectedPaymentMethod,
                            note = note,
                            visibility = selectedVisibility,
                            onSuccess = onPostCreated
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TchiPrimary,
                    contentColor = Color(0xFF071521)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("publish_post_button")
            ) {
                Text(
                    text = "ĐĂNG VÀO NHẬT KÝ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrivacyOptionRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = TchiPrimary,
                unselectedColor = TchiSecondaryText
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) TchiPrimary else TchiSecondaryText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = if (isSelected) TchiWhite else TchiSecondaryText,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = description,
                color = TchiSecondaryText.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}
