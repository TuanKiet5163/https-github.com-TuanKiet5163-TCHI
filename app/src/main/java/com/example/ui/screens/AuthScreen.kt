package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.TchiBackground
import com.example.ui.theme.TchiBorder
import com.example.ui.theme.TchiCard
import com.example.ui.theme.TchiInputBackground
import com.example.ui.theme.TchiPrimary
import com.example.ui.theme.TchiSecondaryBlue
import com.example.ui.theme.TchiSecondaryText
import com.example.ui.theme.TchiWhite
import com.example.viewmodel.TchiViewModel

@Composable
fun AuthScreen(
    viewModel: TchiViewModel,
    onAuthSuccess: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Đăng nhập, 1 = Đăng ký

    // Login state
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Register state
    var regUsername by remember { mutableStateOf("") }
    var regDisplayName by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }

    var showForgotDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo & Header
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(2.dp, TchiPrimary.copy(alpha = 0.4f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tchi_logo),
                    contentDescription = "TCHI Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Tab selector (Login vs Register)
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
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TchiBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Đăng nhập",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_login")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Đăng ký",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_register")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TchiCard, RoundedCornerShape(16.dp))
                    .border(1.dp, TchiBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                if (selectedTab == 0) {
                    // LOGIN FORM
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Tên đăng nhập hoặc TCHI ID",
                            color = TchiSecondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = { loginUsername = it },
                            placeholder = { Text("vd: kiet_nguyen hoặc TCHI-12345", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TchiPrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_input"),
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

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Mật khẩu",
                            color = TchiSecondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            placeholder = { Text("Nhập mật khẩu", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TchiPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                    Icon(
                                        imageVector = if (showLoginPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password",
                                        tint = TchiSecondaryText
                                    )
                                }
                            },
                            visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
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

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Quên mật khẩu?",
                            color = TchiPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { showForgotDialog = true }
                                .padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (loginUsername.isBlank() || loginPassword.isBlank()) {
                                    viewModel.showToast("Vui lòng nhập đầy đủ thông tin")
                                } else {
                                    viewModel.login(loginUsername, loginPassword, onAuthSuccess)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TchiPrimary,
                                contentColor = Color(0xFF071521)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button")
                        ) {
                            Text("ĐĂNG NHẬP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                } else {
                    // REGISTER FORM
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Tên đăng nhập (username viết liền, không dấu)",
                            color = TchiSecondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it.filter { c -> c.isLetterOrDigit() || c == '_' } },
                            placeholder = { Text("vd: kiet_nguyen", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TchiPrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_username_input"),
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Tên hiển thị",
                            color = TchiSecondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regDisplayName,
                            onValueChange = { regDisplayName = it },
                            placeholder = { Text("vd: Tuấn Kiệt", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_display_name_input"),
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Mật khẩu (tối thiểu 6 ký tự)",
                            color = TchiSecondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            placeholder = { Text("Nhập mật khẩu", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TchiPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                    Icon(
                                        imageVector = if (showRegPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password",
                                        tint = TchiSecondaryText
                                    )
                                }
                            },
                            visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_password_input"),
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Xác nhận mật khẩu",
                            color = TchiSecondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it },
                            placeholder = { Text("Nhập lại mật khẩu", color = TchiSecondaryText.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TchiPrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_confirm_password_input"),
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

                        Button(
                            onClick = {
                                when {
                                    regUsername.length < 3 -> viewModel.showToast("Tên đăng nhập phải từ 3 ký tự")
                                    regPassword.length < 6 -> viewModel.showToast("Mật khẩu phải từ 6 ký tự")
                                    regPassword != regConfirmPassword -> viewModel.showToast("Mật khẩu xác nhận không khớp")
                                    else -> {
                                        viewModel.register(
                                            username = regUsername,
                                            displayName = regDisplayName,
                                            passwordPlain = regPassword,
                                            onSuccess = onAuthSuccess
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TchiPrimary,
                                contentColor = Color(0xFF071521)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_submit_button")
                        ) {
                            Text("TẠO TÀI KHOẢN TCHI", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Forgot password dialog
        if (showForgotDialog) {
            AlertDialog(
                onDismissRequest = { showForgotDialog = false },
                containerColor = TchiCard,
                title = { Text("Khôi phục mật khẩu", color = TchiWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Dữ liệu tài khoản TCHI được bảo mật mã hóa an toàn trên hệ thống. Bạn có thể sử dụng TCHI ID hoặc liên hệ quản trị viên để xác thực khôi phục.",
                        color = TchiSecondaryText,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showForgotDialog = false }) {
                        Text("ĐÃ HIỂU", color = TchiPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
