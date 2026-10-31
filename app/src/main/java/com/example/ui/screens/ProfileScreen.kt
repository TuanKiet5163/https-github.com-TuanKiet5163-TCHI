package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwitchAccount
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.UserEntity
import com.example.data.repository.FriendItem
import com.example.ui.components.EmptyStateView
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
import com.example.util.PhotoStorageUtil
import com.example.util.VndFormatter
import com.example.viewmodel.TchiTab
import com.example.viewmodel.TchiViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: TchiViewModel,
    onNavigateToDiscover: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val posts by viewModel.allMyPosts.collectAsState()
    val acceptedFriends by viewModel.acceptedFriends.collectAsState()
    val pendingReceived by viewModel.pendingReceived.collectAsState()
    val pendingSent by viewModel.pendingSent.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var showFriendsSheet by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Avatar picker launcher
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val savedPath = PhotoStorageUtil.saveFromUri(context, uri)
                currentUser?.let { user ->
                    viewModel.updateProfile(user.displayName, user.bio, savedPath)
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TchiBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title with Official TCHI Logo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hồ sơ cá nhân",
                color = TchiWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, TchiPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tchi_logo),
                    contentDescription = "TCHI Official Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, TchiBorder, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = TchiCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with Camera Edit Badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .border(2.dp, TchiPrimary, CircleShape)
                            .background(TchiSecondaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser?.avatarPath != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(currentUser!!.avatarPath!!))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = (currentUser?.displayName ?: "U").take(1).uppercase(),
                                color = TchiWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp
                            )
                        }
                    }

                    // Edit avatar button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(TchiPrimary)
                            .border(2.dp, TchiCard, CircleShape)
                            .clickable {
                                avatarPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Đổi avatar",
                            tint = Color(0xFF071521),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display Name
                Text(
                    text = currentUser?.displayName ?: "Người dùng TCHI",
                    color = TchiWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )

                // Username
                Text(
                    text = "@${currentUser?.username ?: ""}",
                    color = TchiSecondaryText,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // TCHI ID with Copy Button (Part 7 & 13)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF071521), RoundedCornerShape(10.dp))
                        .border(1.dp, TchiBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            currentUser?.tchiId?.let { id ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("TCHI ID", id))
                                viewModel.showToast("Đã sao chép TCHI ID: $id")
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("copy_tchi_id_button")
                ) {
                    Text(
                        text = "TCHI ID: ${currentUser?.tchiId ?: "TCHI-00000"}",
                        color = TchiPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Sao chép ID",
                        tint = TchiPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (currentUser?.bio?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentUser!!.bio,
                        color = TchiWhite.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // COUNTERS (Posts, Friends, Total Spent)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProfileCounter(label = "Bài đăng", count = "${posts.size}")
                    ProfileCounter(
                        label = "Bạn bè",
                        count = "${acceptedFriends.size}",
                        onClick = { showFriendsSheet = true },
                        highlight = true
                    )
                    ProfileCounter(
                        label = "Tổng chi",
                        count = VndFormatter.formatVnd(stats.yearSpentVnd)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SETTINGS & ACTIONS LIST
        Text(
            text = "Cài đặt & Tài khoản",
            color = TchiSecondaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, TchiBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = TchiCard)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Manage Friends
                SettingsRow(
                    icon = Icons.Default.People,
                    title = "Quản lý bạn bè & lời mời",
                    subtitle = "${acceptedFriends.size} bạn bè • ${pendingReceived.size} lời mời chờ",
                    badge = if (pendingReceived.isNotEmpty()) "${pendingReceived.size}" else null,
                    onClick = { showFriendsSheet = true }
                )

                // Edit Profile
                SettingsRow(
                    icon = Icons.Default.Edit,
                    title = "Chỉnh sửa thông tin hồ sơ",
                    subtitle = "Đổi tên hiển thị và tiểu sử",
                    onClick = { showEditProfileDialog = true }
                )

                // Change Password
                SettingsRow(
                    icon = Icons.Default.LockReset,
                    title = "Đổi mật khẩu",
                    subtitle = "Cập nhật mật khẩu bảo mật tài khoản",
                    onClick = { showChangePasswordDialog = true }
                )

                // Export CSV (Part 18)
                SettingsRow(
                    icon = Icons.Default.FileDownload,
                    title = "Xuất dữ liệu chi tiêu (CSV)",
                    subtitle = "Xuất toàn bộ giao dịch UTF-8 ra Excel",
                    onClick = {
                        val csv = CsvExporter.exportTransactionsToCsv(context, posts)
                        CsvExporter.shareCsvFile(context, csv)
                    }
                )

                // Switch Account (Allows testing two users as friends on device!)
                SettingsRow(
                    icon = Icons.Default.SwitchAccount,
                    title = "Chuyển / Đăng nhập tài khoản khác",
                    subtitle = "Đăng nhập tài khoản bạn bè để kiểm thử kết bạn",
                    iconTint = TchiPrimary,
                    onClick = { viewModel.logout() }
                )

                // Logout
                SettingsRow(
                    icon = Icons.Default.Logout,
                    title = "Đăng xuất",
                    subtitle = "Đăng xuất khỏi tài khoản trên thiết bị",
                    iconTint = TchiExpense,
                    onClick = { showLogoutDialog = true }
                )

                // Delete Account
                SettingsRow(
                    icon = Icons.Default.Delete,
                    title = "Xóa tài khoản",
                    subtitle = "Xóa vĩnh viễn tài khoản và toàn bộ bài đăng",
                    iconTint = TchiExpense,
                    showDivider = false,
                    onClick = { showDeleteAccountDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & Currency notice
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF071521), RoundedCornerShape(12.dp))
                .border(1.dp, TchiBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = TchiPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "TCHI cam kết bảo mật quyền riêng tư. Toàn bộ hệ thống giao dịch hoạt động bằng VNĐ và chỉ hiển thị bài đăng theo đúng quyền bạn cho phép.",
                    color = TchiSecondaryText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Official TCHI Brand Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TchiCard, RoundedCornerShape(14.dp))
                .border(1.dp, TchiBorder, RoundedCornerShape(14.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
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
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "TCHI • Capture Your Spending",
                        color = TchiWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Ứng dụng chi tiêu & nhật ký ảnh chính thức",
                        color = TchiPrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }

    // FRIENDS MANAGEMENT SHEET (Part 7 & Part 14)
    if (showFriendsSheet) {
        var friendsTab by remember { mutableIntStateOf(0) } // 0: Bạn bè, 1: Lời mời nhận, 2: Lời mời gửi

        ModalBottomSheet(
            onDismissRequest = { showFriendsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = TchiCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Mạng lưới bạn bè TCHI",
                    color = TchiWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                TabRow(
                    selectedTabIndex = friendsTab,
                    containerColor = Color(0xFF0B1A26),
                    contentColor = TchiPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[friendsTab]),
                            color = TchiPrimary,
                            height = 2.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = friendsTab == 0,
                        onClick = { friendsTab = 0 },
                        text = { Text("Bạn bè (${acceptedFriends.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = friendsTab == 1,
                        onClick = { friendsTab = 1 },
                        text = { Text("Lời mời (${pendingReceived.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = friendsTab == 2,
                        onClick = { friendsTab = 2 },
                        text = { Text("Đã gửi (${pendingSent.size})", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (friendsTab) {
                    0 -> {
                        // ACCEPTED FRIENDS
                        if (acceptedFriends.isEmpty()) {
                            // Part 14 Empty state
                            EmptyStateView(
                                icon = Icons.Default.Group,
                                title = "Bạn chưa có người bạn nào",
                                description = "Tìm kiếm và kết nối với bạn bè trên TCHI.",
                                buttonText = "TÌM BẠN BÈ",
                                testTag = "empty_friends_state",
                                onButtonClick = {
                                    showFriendsSheet = false
                                    onNavigateToDiscover()
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 20.dp)
                            ) {
                                items(acceptedFriends, key = { it.userId }) { friend ->
                                    FriendRowItem(
                                        friend = friend,
                                        onRemove = { viewModel.removeFriend(friend.userId) }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // PENDING RECEIVED
                        if (pendingReceived.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Không có lời mời kết bạn nào", color = TchiSecondaryText)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 20.dp)
                            ) {
                                items(pendingReceived, key = { it.friendshipId }) { item ->
                                    PendingRequestRow(
                                        item = item,
                                        onAccept = { viewModel.acceptFriendRequest(item.friendshipId) },
                                        onDecline = { viewModel.declineOrCancelFriendRequest(item.friendshipId) }
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // PENDING SENT
                        if (pendingSent.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Chưa gửi lời mời nào", color = TchiSecondaryText)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 20.dp)
                            ) {
                                items(pendingSent, key = { it.friendshipId }) { item ->
                                    SentRequestRow(
                                        item = item,
                                        onCancel = { viewModel.declineOrCancelFriendRequest(item.friendshipId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // EDIT PROFILE DIALOG
    if (showEditProfileDialog && currentUser != null) {
        var editDisplayName by remember { mutableStateOf(currentUser!!.displayName) }
        var editBio by remember { mutableStateOf(currentUser!!.bio) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = TchiCard,
            title = { Text("Chỉnh sửa thông tin", color = TchiWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Tên hiển thị", color = TchiSecondaryText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = editDisplayName,
                        onValueChange = { editDisplayName = it },
                        singleLine = true,
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Tiểu sử (Bio)", color = TchiSecondaryText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
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
                        if (editDisplayName.isNotBlank()) {
                            viewModel.updateProfile(editDisplayName, editBio, null)
                            showEditProfileDialog = false
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
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("HỦY", color = TchiSecondaryText)
                }
            }
        )
    }

    // CHANGE PASSWORD DIALOG
    if (showChangePasswordDialog) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            containerColor = TchiCard,
            title = { Text("Đổi mật khẩu", color = TchiWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Mật khẩu hiện tại", color = TchiSecondaryText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Mật khẩu mới (ít nhất 6 ký tự)", color = TchiSecondaryText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
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
                        viewModel.changePassword(oldPassword, newPassword) {
                            showChangePasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiPrimary,
                        contentColor = Color(0xFF071521)
                    )
                ) {
                    Text("CẬP NHẬT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("HỦY", color = TchiSecondaryText)
                }
            }
        )
    }

    // LOGOUT CONFIRMATION
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = TchiCard,
            title = { Text("Đăng xuất khỏi TCHI?", color = TchiWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text("Dữ liệu của bạn được lưu an toàn. Bạn có thể đăng nhập lại bất cứ lúc nào bằng tài khoản và mật khẩu.", color = TchiSecondaryText)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiExpense,
                        contentColor = TchiWhite
                    )
                ) {
                    Text("ĐĂNG XUẤT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("HỦY", color = TchiSecondaryText)
                }
            }
        )
    }

    // DELETE ACCOUNT CONFIRMATION
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = TchiCard,
            title = { Text("Xác nhận xóa tài khoản?", color = TchiExpense, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Hành động này sẽ xóa vĩnh viễn toàn bộ bài đăng, giao dịch chi tiêu, và danh sách bạn bè của bạn khỏi thiết bị. Không thể hoàn tác.",
                    color = TchiSecondaryText,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteAccount {}
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiExpense,
                        contentColor = TchiWhite
                    )
                ) {
                    Text("XÓA TÀI KHOẢN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("HỦY", color = TchiSecondaryText)
                }
            }
        )
    }
}

@Composable
private fun ProfileCounter(
    label: String,
    count: String,
    highlight: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )
    ) {
        Text(
            text = count,
            color = if (highlight) TchiPrimary else TchiWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            color = TchiSecondaryText,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = TchiPrimary,
    badge: String? = null,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF071521)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TchiWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(text = subtitle, color = TchiSecondaryText, fontSize = 11.sp)
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(TchiPrimary)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(text = badge, color = Color(0xFF071521), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (showDivider) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TchiBorder))
        }
    }
}

@Composable
private fun FriendRowItem(
    friend: UserEntity,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B1B28), RoundedCornerShape(12.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(TchiSecondaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(friend.displayName.take(1).uppercase(), color = TchiWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(friend.displayName, color = TchiWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("@${friend.username} • ${friend.tchiId}", color = TchiSecondaryText, fontSize = 11.sp)
            }
        }

        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "Hủy kết bạn", tint = TchiExpense)
        }
    }
}

@Composable
private fun PendingRequestRow(
    item: FriendItem,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B1B28), RoundedCornerShape(12.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(TchiSecondaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(item.friendUser.displayName.take(1).uppercase(), color = TchiWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(item.friendUser.displayName, color = TchiWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("@${item.friendUser.username}", color = TchiSecondaryText, fontSize = 11.sp)
            }
        }

        Row {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = TchiPrimary, contentColor = Color(0xFF071521)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Chấp nhận", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onDecline, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Từ chối", tint = TchiSecondaryText)
            }
        }
    }
}

@Composable
private fun SentRequestRow(
    item: FriendItem,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B1B28), RoundedCornerShape(12.dp))
            .border(1.dp, TchiBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(TchiSecondaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(item.friendUser.displayName.take(1).uppercase(), color = TchiWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(item.friendUser.displayName, color = TchiWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Đang chờ phản hồi...", color = TchiSecondaryText, fontSize = 11.sp)
            }
        }

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF071521), contentColor = TchiSecondaryText),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text("Hủy lời mời", fontSize = 11.sp)
        }
    }
}
