package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.PostEntity
import com.example.data.local.UserEntity
import com.example.data.repository.FeedPostItem
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.TchiBackground
import com.example.ui.theme.TchiBorder
import com.example.ui.theme.TchiCard
import com.example.ui.theme.TchiExpense
import com.example.ui.theme.TchiInputBackground
import com.example.ui.theme.TchiPrimary
import com.example.ui.theme.TchiSecondaryBlue
import com.example.ui.theme.TchiSecondaryText
import com.example.ui.theme.TchiWhite
import com.example.util.VndFormatter
import com.example.viewmodel.JournalAuthorFilter
import com.example.viewmodel.JournalTimeFilter
import com.example.viewmodel.JournalViewMode
import com.example.viewmodel.TchiTab
import com.example.viewmodel.TchiViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: TchiViewModel,
    onNavigateToCamera: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val timeFilter by viewModel.timeFilter.collectAsState()
    val authorFilter by viewModel.authorFilter.collectAsState()

    val myPosts by viewModel.allMyPosts.collectAsState()
    val feedItems by viewModel.feedItems.collectAsState()

    // Post detail sheet state
    var selectedPostForDetail by remember { mutableStateOf<FeedPostItem?>(null) }
    var editingPost by remember { mutableStateOf<PostEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<PostEntity?>(null) }

    // Combine posts and filter them
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis

    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    val startOfWeek = cal.timeInMillis

    cal.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = cal.timeInMillis

    // Build raw list based on author filter
    val rawList: List<FeedPostItem> = when (authorFilter) {
        JournalAuthorFilter.MY_POSTS -> {
            currentUser?.let { user ->
                myPosts.map { FeedPostItem(it, user) }
            } ?: emptyList()
        }
        JournalAuthorFilter.FRIENDS -> {
            feedItems.filter { it.post.userId != currentUser?.userId }
        }
        JournalAuthorFilter.ALL -> {
            // Include user's own posts + friends posts
            val userPosts = currentUser?.let { user ->
                myPosts.map { FeedPostItem(it, user) }
            } ?: emptyList()
            (userPosts + feedItems.filter { it.post.userId != currentUser?.userId })
                .distinctBy { it.post.postId }
                .sortedByDescending { it.post.createdAt }
        }
    }

    // Apply time filter
    val filteredPosts = rawList.filter { item ->
        when (timeFilter) {
            JournalTimeFilter.TODAY -> item.post.createdAt >= startOfToday
            JournalTimeFilter.THIS_WEEK -> item.post.createdAt >= startOfWeek
            JournalTimeFilter.THIS_MONTH -> item.post.createdAt >= startOfMonth
            JournalTimeFilter.ALL -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TchiBackground)
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, TchiPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.tchi_logo),
                        contentDescription = "TCHI Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "TCHI",
                        color = TchiWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Nhật ký chi tiêu",
                        color = TchiPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // View Mode Toggle (Grid vs Feed)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TchiCard)
                    .border(1.dp, TchiBorder, RoundedCornerShape(10.dp))
                    .padding(2.dp)
            ) {
                IconButton(
                    onClick = { viewModel.setViewMode(JournalViewMode.GRID) },
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            if (viewMode == JournalViewMode.GRID) TchiPrimary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .testTag("mode_grid_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Chế độ lưới",
                        tint = if (viewMode == JournalViewMode.GRID) Color(0xFF071521) else TchiSecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.setViewMode(JournalViewMode.FEED) },
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            if (viewMode == JournalViewMode.FEED) TchiPrimary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .testTag("mode_feed_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewStream,
                        contentDescription = "Chế độ bảng tin",
                        tint = if (viewMode == JournalViewMode.FEED) Color(0xFF071521) else TchiSecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // FILTER CHIPS ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Time filters
            JournalTimeFilter.values().forEach { filter ->
                val isSelected = timeFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) TchiSecondaryBlue else TchiCard)
                        .border(1.dp, if (isSelected) TchiPrimary else TchiBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setTimeFilter(filter) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter.label,
                        color = if (isSelected) TchiWhite else TchiSecondaryText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // AUTHOR FILTER CHIPS ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            JournalAuthorFilter.values().forEach { filter ->
                val isSelected = authorFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) TchiPrimary.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (isSelected) TchiPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setAuthorFilter(filter) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = filter.label,
                        color = if (isSelected) TchiPrimary else TchiSecondaryText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // EMPTY STATE (Part 14 compliant)
        if (filteredPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.CameraAlt,
                    title = "Nhật ký của bạn đang trống",
                    description = "Hãy chụp món đồ đầu tiên và lưu lại khoản chi tiêu của bạn.",
                    buttonText = "CHỤP ẢNH NGAY",
                    testTag = "empty_journal_state",
                    onButtonClick = onNavigateToCamera
                )
            }
        } else {
            // CONTENT: GRID vs FEED
            if (viewMode == JournalViewMode.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .testTag("journal_grid"),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPosts, key = { it.post.postId }) { item ->
                        JournalGridItem(
                            item = item,
                            onClick = { selectedPostForDetail = item }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("journal_feed"),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredPosts, key = { it.post.postId }) { item ->
                        JournalFeedCard(
                            item = item,
                            currentUserId = currentUser?.userId,
                            onClick = { selectedPostForDetail = item },
                            onEdit = { editingPost = item.post },
                            onDelete = { showDeleteConfirmDialog = item.post }
                        )
                    }
                }
            }
        }
    }

    // POST DETAIL MODAL
    if (selectedPostForDetail != null) {
        val item = selectedPostForDetail!!
        val isOwner = item.post.userId == currentUser?.userId
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.post.createdAt))

        ModalBottomSheet(
            onDismissRequest = { selectedPostForDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = TchiCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Author row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TchiSecondaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.author.displayName.take(1).uppercase(),
                                color = TchiWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = item.author.displayName,
                                color = TchiWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "@${item.author.username} • ${item.author.tchiId}",
                                color = TchiSecondaryText,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Visibility badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF071521), RoundedCornerShape(8.dp))
                            .border(1.dp, TchiBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.post.visibility,
                            color = TchiPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, TchiBorder, RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(item.post.imageUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = item.post.productName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name & Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.post.productName,
                        color = TchiWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = VndFormatter.formatVnd(item.post.amountVnd),
                        color = TchiPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata tags: Category, Payment method, Date
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF071521), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(item.post.category, color = TchiSecondaryText, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF071521), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(item.post.paymentMethod, color = TchiSecondaryText, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF071521), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(dateStr, color = TchiSecondaryText, fontSize = 12.sp)
                    }
                }

                if (item.post.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.post.note,
                        color = TchiWhite,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                // If owner: Edit & Delete buttons
                if (isOwner) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val p = item.post
                                selectedPostForDetail = null
                                editingPost = p
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TchiSecondaryBlue,
                                contentColor = TchiWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chỉnh sửa")
                        }

                        Button(
                            onClick = {
                                val p = item.post
                                selectedPostForDetail = null
                                showDeleteConfirmDialog = p
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TchiExpense.copy(alpha = 0.15f),
                                contentColor = TchiExpense
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Xóa bài")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // EDIT POST DIALOG
    if (editingPost != null) {
        var editName by remember { mutableStateOf(editingPost!!.productName) }
        var editAmount by remember { mutableStateOf(editingPost!!.amountVnd.toString()) }
        var editCategory by remember { mutableStateOf(editingPost!!.category) }
        var editNote by remember { mutableStateOf(editingPost!!.note) }
        var editVisibility by remember { mutableStateOf(editingPost!!.visibility) }

        AlertDialog(
            onDismissRequest = { editingPost = null },
            containerColor = TchiCard,
            title = { Text("Chỉnh sửa món đồ", color = TchiWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Ghi chú", color = TchiSecondaryText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
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
                                amountVnd = amt,
                                note = editNote.trim()
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
                    Text("LƯU THAY ĐỔI")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPost = null }) {
                    Text("HỦY", color = TchiSecondaryText)
                }
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    if (showDeleteConfirmDialog != null) {
        val postToDelete = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            containerColor = TchiCard,
            title = { Text("Xóa món đồ này?", color = TchiWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Món đồ \"${postToDelete.productName}\" (${VndFormatter.formatVnd(postToDelete.amountVnd)}) sẽ bị xóa khỏi nhật ký và số tiền chi tiêu sẽ được tự động trừ đi khỏi thống kê.",
                    color = TchiSecondaryText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePost(postToDelete)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiExpense,
                        contentColor = TchiWhite
                    )
                ) {
                    Text("XÓA NGAY")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("HỦY", color = TchiSecondaryText)
                }
            }
        )
    }
}

@Composable
private fun JournalGridItem(
    item: FeedPostItem,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(item.post.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("journal_grid_item_${item.post.postId}"),
        colors = CardDefaults.cardColors(containerColor = TchiCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(item.post.imageUri))
                    .crossfade(true)
                    .build(),
                contentDescription = item.post.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
            )

            // Top Date and Visibility Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    color = TchiWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                if (item.post.visibility != "PRIVATE") {
                    Icon(
                        imageVector = if (item.post.visibility == "FRIENDS") Icons.Default.People else Icons.Default.Public,
                        contentDescription = item.post.visibility,
                        tint = TchiPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Bottom gradient overlay with Name & Price Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = item.post.productName,
                        color = TchiWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(TchiPrimary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = VndFormatter.formatVnd(item.post.amountVnd),
                            color = Color(0xFF071521),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalFeedCard(
    item: FeedPostItem,
    currentUserId: String?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isOwner = item.post.userId == currentUserId
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.post.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("journal_feed_item_${item.post.postId}"),
        colors = CardDefaults.cardColors(containerColor = TchiCard)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Author header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TchiSecondaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.author.displayName.take(1).uppercase(),
                            color = TchiWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.author.displayName,
                            color = TchiWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$dateStr • ${item.post.category}",
                            color = TchiSecondaryText,
                            fontSize = 11.sp
                        )
                    }
                }

                // Visibility pill
                Box(
                    modifier = Modifier
                        .background(Color(0xFF071521), RoundedCornerShape(6.dp))
                        .border(1.dp, TchiBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.post.visibility,
                        color = TchiPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(item.post.imageUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = item.post.productName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Info bar
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.post.productName,
                        color = TchiWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .background(TchiPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, TchiPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = VndFormatter.formatVnd(item.post.amountVnd),
                            color = TchiPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }

                if (item.post.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.post.note,
                        color = TchiSecondaryText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thanh toán: ${item.post.paymentMethod}",
                        color = TchiSecondaryText.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    if (isOwner) {
                        Row {
                            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = TchiSecondaryText, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = TchiExpense, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
