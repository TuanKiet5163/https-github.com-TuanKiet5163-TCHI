package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.PostEntity
import com.example.ui.components.CategoryDoughnutChart
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MonthlyBarChart
import com.example.ui.theme.TchiBackground
import com.example.ui.theme.TchiBorder
import com.example.ui.theme.TchiCard
import com.example.ui.theme.TchiExpense
import com.example.ui.theme.TchiIncome
import com.example.ui.theme.TchiInputBackground
import com.example.ui.theme.TchiPrimary
import com.example.ui.theme.TchiSecondaryBlue
import com.example.ui.theme.TchiSecondaryText
import com.example.ui.theme.TchiWhite
import com.example.util.CsvExporter
import com.example.util.VndFormatter
import com.example.viewmodel.TchiViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: TchiViewModel,
    onNavigateToCamera: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.stats.collectAsState()
    val posts by viewModel.allMyPosts.collectAsState()
    val monthlyBudget by viewModel.currentMonthlyBudget.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showTransactionsSheet by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TchiBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Thống kê tài chính",
                    color = TchiWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Quản lý chi tiêu VNĐ thực tế",
                    color = TchiSecondaryText,
                    fontSize = 12.sp
                )
            }

            // Quick all transactions button
            IconButton(
                onClick = { showTransactionsSheet = true },
                modifier = Modifier
                    .background(TchiCard, CircleShape)
                    .border(1.dp, TchiBorder, CircleShape)
                    .testTag("open_transactions_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = "Danh sách giao dịch",
                    tint = TchiPrimary
                )
            }
        }

        // Empty state check (Part 14)
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.InsertChart,
                    title = "Chưa có dữ liệu chi tiêu",
                    description = "Thêm giao dịch đầu tiên để bắt đầu quản lý tài chính.",
                    totalHighlightText = "0 VNĐ",
                    buttonText = "CHỤP ẢNH MÓN ĐỒ",
                    testTag = "empty_stats_state",
                    onButtonClick = onNavigateToCamera
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // OVERVIEW SPENDING CARDS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Hôm nay",
                        amountVnd = stats.todaySpentVnd,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Tuần này",
                        amountVnd = stats.weekSpentVnd,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Tháng này",
                        amountVnd = stats.monthSpentVnd,
                        modifier = Modifier.weight(1f),
                        highlight = true
                    )
                    MetricCard(
                        title = "Năm nay",
                        amountVnd = stats.yearSpentVnd,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BUDGET SECTION (Part 11)
                BudgetTrackerCard(
                    budgetAmount = monthlyBudget?.amountVnd ?: 0L,
                    spentAmount = stats.monthSpentVnd,
                    onSetBudget = { showBudgetDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // QUICK STATS SUMMARY
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, TchiBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = TchiCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Chỉ số chi tiêu",
                            color = TchiWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Tổng món đã mua", "${stats.totalItems} món")
                            StatItem("Trung bình / ngày", VndFormatter.formatVnd(stats.dailyAverageVnd))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Chi lớn nhất", VndFormatter.formatVnd(stats.maxExpenseVnd), TchiExpense)
                            StatItem("Chi nhỏ nhất", VndFormatter.formatVnd(stats.minExpenseVnd), TchiIncome)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CATEGORY PIE / DOUGHNUT CHART
                CategoryDoughnutChart(categoryTotals = stats.categoryTotals)

                Spacer(modifier = Modifier.height(16.dp))

                // MONTHLY BAR CHART
                MonthlyBarChart(monthlyTrend = stats.monthlyTrend)

                Spacer(modifier = Modifier.height(16.dp))

                // Open Transactions Manager button
                Button(
                    onClick = { showTransactionsSheet = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiSecondaryBlue,
                        contentColor = TchiWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("QUẢN LÝ TẤT CẢ GIAO DỊCH (${posts.size})", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }

    // SET BUDGET DIALOG
    if (showBudgetDialog) {
        var rawBudgetInput by remember {
            mutableStateOf(monthlyBudget?.amountVnd?.toString() ?: "")
        }

        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            containerColor = TchiCard,
            title = {
                Text(
                    text = "Thiết lập ngân sách tháng",
                    color = TchiWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Nhập số tiền ngân sách tối đa trong tháng (VNĐ):",
                        color = TchiSecondaryText,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = VndFormatter.formatInput(rawBudgetInput),
                        onValueChange = { rawBudgetInput = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("vd: 5.000.000 VNĐ", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                        trailingIcon = { Text("VNĐ", color = TchiPrimary, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("set_budget_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TchiInputBackground,
                            unfocusedContainerColor = TchiInputBackground,
                            focusedBorderColor = TchiPrimary,
                            unfocusedBorderColor = TchiBorder,
                            focusedTextColor = TchiWhite,
                            unfocusedTextColor = TchiWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = rawBudgetInput.toLongOrNull() ?: 0L
                        if (amount > 0L) {
                            viewModel.setMonthlyBudget(amount)
                            showBudgetDialog = false
                        } else {
                            viewModel.showToast("Vui lòng nhập số tiền lớn hơn 0")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiPrimary,
                        contentColor = Color(0xFF071521)
                    )
                ) {
                    Text("LƯU NGÂN SÁCH")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("HỦY", color = TchiSecondaryText)
                }
            }
        )
    }

    // ALL TRANSACTIONS SHEET
    if (showTransactionsSheet) {
        TransactionsManagerSheet(
            posts = posts,
            viewModel = viewModel,
            onDismiss = { showTransactionsSheet = false }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    amountVnd: Long,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (highlight) TchiPrimary.copy(alpha = 0.6f) else TchiBorder,
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) Color(0xFF132F44) else TchiCard
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = if (highlight) TchiPrimary else TchiSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = VndFormatter.formatVnd(amountVnd),
                color = TchiWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color = TchiWhite
) {
    Column {
        Text(text = label, color = TchiSecondaryText, fontSize = 11.sp)
        Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun BudgetTrackerCard(
    budgetAmount: Long,
    spentAmount: Long,
    onSetBudget: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = TchiCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = TchiPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ngân sách tháng",
                        color = TchiWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = if (budgetAmount > 0) "Thay đổi" else "Thiết lập",
                    color = TchiPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSetBudget() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (budgetAmount <= 0) {
                Text(
                    text = "Chưa thiết lập ngân sách tháng này. Đặt hạn mức chi tiêu để kiểm soát tài chính tốt hơn.",
                    color = TchiSecondaryText,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onSetBudget,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiSecondaryBlue,
                        contentColor = TchiWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("+ Đặt ngân sách", fontSize = 12.sp)
                }
            } else {
                val percentage = ((spentAmount.toFloat() / budgetAmount.toFloat()) * 100).toInt()
                val remaining = budgetAmount - spentAmount
                val progressFraction = (spentAmount.toFloat() / budgetAmount.toFloat()).coerceIn(0f, 1f)

                val progressColor = when {
                    percentage >= 100 -> TchiExpense
                    percentage >= 80 -> Color(0xFFF59E0B) // Amber warning
                    else -> TchiPrimary
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Đã chi: ${VndFormatter.formatVnd(spentAmount)} ($percentage%)",
                        color = TchiWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (remaining >= 0) "Còn lại: ${VndFormatter.formatVnd(remaining)}" else "Vượt: ${VndFormatter.formatVnd(-remaining)}",
                        color = if (remaining >= 0) TchiIncome else TchiExpense,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = Color(0xFF071521)
                )

                // Warning banners (Part 11)
                if (percentage >= 100) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TchiExpense.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, TchiExpense.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = TchiExpense, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cảnh báo: Bạn đã chi tiêu vượt quá 100% ngân sách tháng!",
                            color = TchiExpense,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (percentage >= 80) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lưu ý: Bạn đã sử dụng hơn 80% hạn mức ngân sách tháng.",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsManagerSheet(
    posts: List<PostEntity>,
    viewModel: TchiViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Tất cả") }
    var sortOrder by remember { mutableStateOf(0) } // 0: Mới nhất, 1: Giá cao -> thấp, 2: Giá thấp -> cao

    var editingPost by remember { mutableStateOf<PostEntity?>(null) }
    var postToDelete by remember { mutableStateOf<PostEntity?>(null) }

    val filteredList = posts.filter { post ->
        val matchesQuery = post.productName.contains(searchQuery, ignoreCase = true) ||
                post.note.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "Tất cả" || post.category == selectedCategoryFilter
        matchesQuery && matchesCategory
    }.let { list ->
        when (sortOrder) {
            1 -> list.sortedByDescending { it.amountVnd }
            2 -> list.sortedBy { it.amountVnd }
            else -> list.sortedByDescending { it.createdAt }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TchiCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header with CSV Export button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quản lý giao dịch (${posts.size})",
                    color = TchiWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                // CSV Export button (Part 18)
                Button(
                    onClick = {
                        val csvFile = CsvExporter.exportTransactionsToCsv(context, posts)
                        CsvExporter.shareCsvFile(context, csvFile)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiPrimary,
                        contentColor = Color(0xFF071521)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xuất CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm theo tên món đồ...", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TchiPrimary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TchiInputBackground,
                    unfocusedContainerColor = TchiInputBackground,
                    focusedBorderColor = TchiPrimary,
                    unfocusedBorderColor = TchiBorder,
                    focusedTextColor = TchiWhite,
                    unfocusedTextColor = TchiWhite
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Sorting buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sortLabels = listOf("Mới nhất", "Giá cao nhất", "Giá thấp nhất")
                sortLabels.forEachIndexed { index, label ->
                    val isSelected = sortOrder == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TchiSecondaryBlue else Color(0xFF071521))
                            .border(1.dp, if (isSelected) TchiPrimary else TchiBorder, RoundedCornerShape(8.dp))
                            .clickable { sortOrder = index }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) TchiWhite else TchiSecondaryText,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transactions list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.postId }) { post ->
                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(post.createdAt))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TchiBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1B28))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(File(post.imageUri))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = post.productName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = post.productName,
                                    color = TchiWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$dateStr • ${post.category} • ${post.paymentMethod}",
                                    color = TchiSecondaryText,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = VndFormatter.formatVnd(post.amountVnd),
                                    color = TchiPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Row {
                                IconButton(onClick = { editingPost = post }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = TchiSecondaryText, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { postToDelete = post }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = TchiExpense, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // EDIT TRANSACTION DIALOG
    if (editingPost != null) {
        var editName by remember { mutableStateOf(editingPost!!.productName) }
        var editAmount by remember { mutableStateOf(editingPost!!.amountVnd.toString()) }
        var editNote by remember { mutableStateOf(editingPost!!.note) }

        AlertDialog(
            onDismissRequest = { editingPost = null },
            containerColor = TchiCard,
            title = { Text("Chỉnh sửa giao dịch", color = TchiWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Tên món đồ", color = TchiSecondaryText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TchiInputBackground,
                            unfocusedContainerColor = TchiInputBackground,
                            focusedBorderColor = TchiPrimary,
                            unfocusedBorderColor = TchiBorder,
                            focusedTextColor = TchiWhite,
                            unfocusedTextColor = TchiWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Số tiền VNĐ", color = TchiSecondaryText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = VndFormatter.formatInput(editAmount),
                        onValueChange = { editAmount = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TchiInputBackground,
                            unfocusedContainerColor = TchiInputBackground,
                            focusedBorderColor = TchiPrimary,
                            unfocusedBorderColor = TchiBorder,
                            focusedTextColor = TchiWhite,
                            unfocusedTextColor = TchiWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = editAmount.toLongOrNull() ?: 0L
                        if (editName.isNotBlank() && amt > 0L) {
                            val updated = editingPost!!.copy(
                                productName = editName.trim(),
                                amountVnd = amt
                            )
                            viewModel.updatePost(updated) {
                                editingPost = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiPrimary,
                        contentColor = Color(0xFF071521)
                    )
                ) {
                    Text("LƯU")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPost = null }) {
                    Text("HỦY", color = TchiSecondaryText)
                }
            }
        )
    }

    // DELETE TRANSACTION DIALOG
    if (postToDelete != null) {
        val target = postToDelete!!
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            containerColor = TchiCard,
            title = { Text("Xác nhận xóa giao dịch?", color = TchiWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Giao dịch \"${target.productName}\" (${VndFormatter.formatVnd(target.amountVnd)}) sẽ bị xóa vĩnh viễn và số tiền sẽ được tự động tính lại.",
                    color = TchiSecondaryText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePost(target)
                        postToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiExpense,
                        contentColor = TchiWhite
                    )
                ) {
                    Text("XÓA")
                }
            },
            dismissButton = {
                TextButton(onClick = { postToDelete = null }) {
                    Text("HỦY", color = TchiSecondaryText)
                }
            }
        )
    }
}
