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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.UserEntity
import com.example.data.repository.FeedPostItem
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.TchiBackground
import com.example.ui.theme.TchiBorder
import com.example.ui.theme.TchiCard
import com.example.ui.theme.TchiInputBackground
import com.example.ui.theme.TchiPrimary
import com.example.ui.theme.TchiSecondaryBlue
import com.example.ui.theme.TchiSecondaryText
import com.example.ui.theme.TchiWhite
import com.example.util.VndFormatter
import com.example.viewmodel.SearchUserResult
import com.example.viewmodel.TchiViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiscoverScreen(viewModel: TchiViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val publicPosts by viewModel.publicPosts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tìm bạn bè, 1: Bài đăng công khai

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TchiBackground)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = "Khám phá",
                color = TchiWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tìm kiếm bạn bè và xem chi tiêu công khai",
                color = TchiSecondaryText,
                fontSize = 12.sp
            )
        }

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = TchiCard,
            contentColor = TchiPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = TchiPrimary,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TchiBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Tìm kiếm bạn bè",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("tab_discover_users")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Bài đăng công khai",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("tab_discover_posts")
            )
        }

        if (selectedTab == 0) {
            // SEARCH USER VIEW
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Nhập username hoặc TCHI ID...", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TchiPrimary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("discover_search_input"),
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

                if (searchQuery.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(TchiSecondaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = TchiPrimary, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tìm kiếm người dùng",
                                color = TchiWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Nhập tên tài khoản hoặc mã TCHI ID của bạn bè để kết nối mạng xã hội chi tiêu thật.",
                                color = TchiSecondaryText,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không tìm thấy người dùng \"$searchQuery\"",
                            color = TchiSecondaryText,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_results_list"),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(searchResults, key = { it.user.userId }) { result ->
                            UserSearchResultRow(
                                item = result,
                                onSendRequest = { viewModel.sendFriendRequest(result.user.userId) },
                                onCancelRequest = { /* cancel from profile */ }
                            )
                        }
                    }
                }
            }
        } else {
            // PUBLIC POSTS VIEW
            if (publicPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Default.Explore,
                        title = "Chưa có bài đăng công khai",
                        description = "Khi bạn bè hoặc cộng đồng chia sẻ món đồ với quyền riêng tư PUBLIC, chúng sẽ xuất hiện tại đây.",
                        testTag = "empty_public_posts"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(publicPosts, key = { it.post.postId }) { item ->
                        PublicPostCard(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserSearchResultRow(
    item: SearchUserResult,
    onSendRequest: () -> Unit,
    onCancelRequest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = TchiCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TchiSecondaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.user.displayName.take(1).uppercase(),
                        color = TchiWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.user.displayName,
                        color = TchiWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "@${item.user.username} • ${item.user.tchiId}",
                        color = TchiPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (item.user.bio.isNotBlank()) {
                        Text(
                            text = item.user.bio,
                            color = TchiSecondaryText,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Friendship action button
            when (item.friendshipStatus) {
                "ACCEPTED" -> {
                    Box(
                        modifier = Modifier
                            .background(TchiPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, TchiPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = TchiPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bạn bè", color = TchiPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                "PENDING" -> {
                    Box(
                        modifier = Modifier
                            .background(TchiSecondaryBlue, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = TchiSecondaryText, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Đã gửi lời mời", color = TchiSecondaryText, fontSize = 12.sp)
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onSendRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TchiPrimary,
                            contentColor = Color(0xFF071521)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kết bạn", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicPostCard(item: FeedPostItem) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.post.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = TchiCard)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TchiSecondaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.author.displayName.take(1).uppercase(),
                        color = TchiWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
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
                        text = "@${item.author.username} • $dateStr",
                        color = TchiSecondaryText,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
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

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.post.productName,
                        color = TchiWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = VndFormatter.formatVnd(item.post.amountVnd),
                        color = TchiPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                if (item.post.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.post.note,
                        color = TchiSecondaryText,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
