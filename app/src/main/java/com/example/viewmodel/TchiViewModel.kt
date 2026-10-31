package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BudgetEntity
import com.example.data.local.PostEntity
import com.example.data.local.UserEntity
import com.example.data.repository.FeedPostItem
import com.example.data.repository.FriendItem
import com.example.data.repository.TchiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TchiTab {
    JOURNAL,
    DISCOVER,
    CAMERA,
    STATS,
    PROFILE
}

enum class JournalViewMode {
    GRID,
    FEED
}

enum class JournalTimeFilter(val label: String) {
    TODAY("Hôm nay"),
    THIS_WEEK("Tuần này"),
    THIS_MONTH("Tháng này"),
    ALL("Tất cả")
}

enum class JournalAuthorFilter(val label: String) {
    ALL("Tất cả"),
    MY_POSTS("Của tôi"),
    FRIENDS("Bạn bè")
}

data class FinancialStats(
    val todaySpentVnd: Long = 0L,
    val weekSpentVnd: Long = 0L,
    val monthSpentVnd: Long = 0L,
    val yearSpentVnd: Long = 0L,
    val totalItems: Int = 0,
    val dailyAverageVnd: Long = 0L,
    val maxExpenseVnd: Long = 0L,
    val minExpenseVnd: Long = 0L,
    val categoryTotals: Map<String, Long> = emptyMap(),
    val monthlyTrend: List<Pair<String, Long>> = emptyList()
)

data class SearchUserResult(
    val user: UserEntity,
    val friendshipStatus: String? // null, PENDING, ACCEPTED
)

class TchiViewModel(application: Application) : AndroidViewModel(application) {

    val repository = TchiRepository(application)

    // Auth & User State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished: StateFlow<Boolean> = _isSplashFinished.asStateFlow()

    // Navigation
    private val _currentTab = MutableStateFlow(TchiTab.JOURNAL)
    val currentTab: StateFlow<TchiTab> = _currentTab.asStateFlow()

    // Photo captured waiting to be posted
    private val _capturedPhotoUri = MutableStateFlow<String?>(null)
    val capturedPhotoUri: StateFlow<String?> = _capturedPhotoUri.asStateFlow()

    // Journal view options
    private val _viewMode = MutableStateFlow(JournalViewMode.GRID)
    val viewMode: StateFlow<JournalViewMode> = _viewMode.asStateFlow()

    private val _timeFilter = MutableStateFlow(JournalTimeFilter.ALL)
    val timeFilter: StateFlow<JournalTimeFilter> = _timeFilter.asStateFlow()

    private val _authorFilter = MutableStateFlow(JournalAuthorFilter.ALL)
    val authorFilter: StateFlow<JournalAuthorFilter> = _authorFilter.asStateFlow()

    // Real Posts & Friends
    private val _allMyPosts = MutableStateFlow<List<PostEntity>>(emptyList())
    val allMyPosts: StateFlow<List<PostEntity>> = _allMyPosts.asStateFlow()

    private val _feedItems = MutableStateFlow<List<FeedPostItem>>(emptyList())
    val feedItems: StateFlow<List<FeedPostItem>> = _feedItems.asStateFlow()

    private val _publicPosts = MutableStateFlow<List<FeedPostItem>>(emptyList())
    val publicPosts: StateFlow<List<FeedPostItem>> = _publicPosts.asStateFlow()

    // Friends
    private val _acceptedFriends = MutableStateFlow<List<UserEntity>>(emptyList())
    val acceptedFriends: StateFlow<List<UserEntity>> = _acceptedFriends.asStateFlow()

    private val _pendingReceived = MutableStateFlow<List<FriendItem>>(emptyList())
    val pendingReceived: StateFlow<List<FriendItem>> = _pendingReceived.asStateFlow()

    private val _pendingSent = MutableStateFlow<List<FriendItem>>(emptyList())
    val pendingSent: StateFlow<List<FriendItem>> = _pendingSent.asStateFlow()

    // Search Users
    private val _searchResults = MutableStateFlow<List<SearchUserResult>>(emptyList())
    val searchResults: StateFlow<List<SearchUserResult>> = _searchResults.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Budget
    private val _currentMonthlyBudget = MutableStateFlow<BudgetEntity?>(null)
    val currentMonthlyBudget: StateFlow<BudgetEntity?> = _currentMonthlyBudget.asStateFlow()

    // Stats
    private val _stats = MutableStateFlow(FinancialStats())
    val stats: StateFlow<FinancialStats> = _stats.asStateFlow()

    // Messages / UI Toast
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        checkSavedSession()
    }

    fun finishSplash() {
        _isSplashFinished.value = true
    }

    fun selectTab(tab: TchiTab) {
        _currentTab.value = tab
    }

    fun setViewMode(mode: JournalViewMode) {
        _viewMode.value = mode
    }

    fun setTimeFilter(filter: JournalTimeFilter) {
        _timeFilter.value = filter
    }

    fun setAuthorFilter(filter: JournalAuthorFilter) {
        _authorFilter.value = filter
    }

    fun setCapturedPhotoUri(uri: String?) {
        _capturedPhotoUri.value = uri
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    private fun checkSavedSession() {
        viewModelScope.launch {
            val user = repository.getCurrentUser()
            _currentUser.value = user
            if (user != null) {
                loadUserData(user.userId)
            }
        }
    }

    fun loadUserData(userId: String) {
        // Collect user posts
        viewModelScope.launch {
            repository.postDao.getPostsByUserId(userId).collect { posts ->
                _allMyPosts.value = posts
                computeStats(posts)
            }
        }

        // Collect accepted friendships
        viewModelScope.launch {
            repository.friendshipDao.getAcceptedFriendships(userId).collect { friendships ->
                val friendIds = friendships.map {
                    if (it.senderId == userId) it.receiverId else it.senderId
                }
                val friendUsers = repository.userDao.getUsersByIds(friendIds)
                _acceptedFriends.value = friendUsers

                // Refresh feed posts
                refreshFeed(userId, friendIds)
            }
        }

        // Collect pending received
        viewModelScope.launch {
            repository.friendshipDao.getReceivedPendingRequests(userId).collect { requests ->
                val senderIds = requests.map { it.senderId }
                val senders = repository.userDao.getUsersByIds(senderIds).associateBy { it.userId }
                _pendingReceived.value = requests.mapNotNull { req ->
                    senders[req.senderId]?.let { user ->
                        FriendItem(req.friendshipId, user, req.status, isSender = false)
                    }
                }
            }
        }

        // Collect pending sent
        viewModelScope.launch {
            repository.friendshipDao.getSentPendingRequests(userId).collect { requests ->
                val receiverIds = requests.map { it.receiverId }
                val receivers = repository.userDao.getUsersByIds(receiverIds).associateBy { it.userId }
                _pendingSent.value = requests.mapNotNull { req ->
                    receivers[req.receiverId]?.let { user ->
                        FriendItem(req.friendshipId, user, req.status, isSender = true)
                    }
                }
            }
        }

        // Collect budget for current month & year
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)
        viewModelScope.launch {
            repository.budgetDao.getMonthlyBudget(userId, currentMonth, currentYear).collect {
                _currentMonthlyBudget.value = it
            }
        }

        // Load public posts
        viewModelScope.launch {
            repository.postDao.getPublicPosts(userId).collect { posts ->
                val authorIds = posts.map { it.userId }.distinct()
                val authors = repository.userDao.getUsersByIds(authorIds).associateBy { it.userId }
                _publicPosts.value = posts.mapNotNull { p ->
                    authors[p.userId]?.let { author ->
                        FeedPostItem(p, author)
                    }
                }
            }
        }
    }

    private fun refreshFeed(currentUserId: String, friendIds: List<String>) {
        viewModelScope.launch {
            repository.postDao.getFeedPosts(currentUserId, friendIds).collect { posts ->
                val userIds = posts.map { it.userId }.distinct()
                val users = repository.userDao.getUsersByIds(userIds).associateBy { it.userId }
                _feedItems.value = posts.mapNotNull { p ->
                    users[p.userId]?.let { user ->
                        FeedPostItem(p, user)
                    }
                }
            }
        }
    }

    // AUTH METHODS
    fun register(
        username: String,
        displayName: String,
        passwordPlain: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.registerUser(username, displayName, passwordPlain)
            result.onSuccess { user ->
                _currentUser.value = user
                loadUserData(user.userId)
                showToast("Đăng ký thành công! Chào mừng bạn đến với TCHI.")
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Đăng ký thất bại")
            }
        }
    }

    fun login(
        usernameOrTchiId: String,
        passwordPlain: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.loginUser(usernameOrTchiId, passwordPlain)
            result.onSuccess { user ->
                _currentUser.value = user
                loadUserData(user.userId)
                showToast("Đăng nhập thành công!")
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Đăng nhập thất bại")
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _allMyPosts.value = emptyList()
        _feedItems.value = emptyList()
        _acceptedFriends.value = emptyList()
        _stats.value = FinancialStats()
        _currentMonthlyBudget.value = null
        showToast("Đã đăng xuất")
    }

    fun updateProfile(displayName: String, bio: String, avatarPath: String?) {
        viewModelScope.launch {
            val res = repository.updateUserProfile(displayName, bio, avatarPath)
            res.onSuccess {
                _currentUser.value = repository.getCurrentUser()
                showToast("Cập nhật thông tin thành công")
            }.onFailure {
                showToast(it.message ?: "Lỗi cập nhật")
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.changePassword(oldPass, newPass)
            res.onSuccess {
                showToast("Đổi mật khẩu thành công")
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Đổi mật khẩu thất bại")
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.deleteAccount()
            res.onSuccess {
                _currentUser.value = null
                _allMyPosts.value = emptyList()
                _feedItems.value = emptyList()
                showToast("Tài khoản và dữ liệu đã được xóa hoàn toàn")
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Lỗi xóa tài khoản")
            }
        }
    }

    // POST ACTIONS
    fun createPost(
        imageUri: String,
        productName: String,
        amountVnd: Long,
        category: String,
        paymentMethod: String,
        note: String,
        visibility: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createPost(
                imageUri = imageUri,
                productName = productName,
                amountVnd = amountVnd,
                category = category,
                paymentMethod = paymentMethod,
                note = note,
                visibility = visibility
            )
            result.onSuccess {
                setCapturedPhotoUri(null)
                selectTab(TchiTab.JOURNAL)
                showToast("Đã ghi nhận chi tiêu vào nhật ký!")
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Lỗi tạo bài đăng")
            }
        }
    }

    fun updatePost(post: PostEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.updatePost(post)
            res.onSuccess {
                showToast("Đã cập nhật bài đăng")
                onSuccess()
            }.onFailure {
                showToast(it.message ?: "Lỗi cập nhật")
            }
        }
    }

    fun deletePost(post: PostEntity) {
        viewModelScope.launch {
            val res = repository.deletePost(post)
            res.onSuccess {
                showToast("Đã xóa bài đăng khỏi nhật ký")
            }.onFailure {
                showToast(it.message ?: "Lỗi xóa bài đăng")
            }
        }
    }

    // SEARCH & FRIENDS
    fun onSearchQueryChanged(q: String) {
        _searchQuery.value = q
        val currentUserId = _currentUser.value?.userId ?: return
        if (q.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val users = repository.userDao.searchUsers(q, currentUserId)
            val results = users.map { target ->
                val friendship = repository.friendshipDao.getFriendshipBetween(currentUserId, target.userId)
                SearchUserResult(target, friendship?.status)
            }
            _searchResults.value = results
        }
    }

    fun sendFriendRequest(targetUserId: String) {
        viewModelScope.launch {
            val res = repository.sendFriendRequest(targetUserId)
            res.onSuccess {
                showToast("Đã gửi lời mời kết bạn")
                onSearchQueryChanged(_searchQuery.value)
            }.onFailure {
                showToast(it.message ?: "Không thể gửi lời mời")
            }
        }
    }

    fun acceptFriendRequest(friendshipId: String) {
        viewModelScope.launch {
            val res = repository.acceptFriendRequest(friendshipId)
            res.onSuccess {
                showToast("Đã chấp nhận kết bạn")
            }.onFailure {
                showToast(it.message ?: "Lỗi chấp nhận kết bạn")
            }
        }
    }

    fun declineOrCancelFriendRequest(friendshipId: String) {
        viewModelScope.launch {
            val res = repository.declineOrCancelFriendRequest(friendshipId)
            res.onSuccess {
                showToast("Đã hủy lời mời")
                onSearchQueryChanged(_searchQuery.value)
            }.onFailure {
                showToast(it.message ?: "Lỗi hủy lời mời")
            }
        }
    }

    fun removeFriend(targetUserId: String) {
        viewModelScope.launch {
            val res = repository.removeFriend(targetUserId)
            res.onSuccess {
                showToast("Đã hủy kết bạn")
                onSearchQueryChanged(_searchQuery.value)
            }.onFailure {
                showToast(it.message ?: "Lỗi hủy kết bạn")
            }
        }
    }

    // BUDGET
    fun setMonthlyBudget(amountVnd: Long) {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        viewModelScope.launch {
            val res = repository.setMonthlyBudget(amountVnd, month, year)
            res.onSuccess {
                showToast("Đã cập nhật ngân sách tháng")
            }.onFailure {
                showToast(it.message ?: "Lỗi cập nhật ngân sách")
            }
        }
    }

    // COMPUTATIONS
    private fun computeStats(posts: List<PostEntity>) {
        if (posts.isEmpty()) {
            _stats.value = FinancialStats()
            return
        }

        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        // Start of today
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        // Start of week
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val startOfWeek = cal.timeInMillis

        // Start of month
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = cal.timeInMillis

        // Start of year
        cal.set(Calendar.DAY_OF_YEAR, 1)
        val startOfYear = cal.timeInMillis

        var todaySpent = 0L
        var weekSpent = 0L
        var monthSpent = 0L
        var yearSpent = 0L
        var maxExpense = 0L
        var minExpense = if (posts.isNotEmpty()) posts[0].amountVnd else 0L
        val categoryMap = mutableMapOf<String, Long>()

        posts.forEach { post ->
            val amt = post.amountVnd
            if (post.createdAt >= startOfToday) todaySpent += amt
            if (post.createdAt >= startOfWeek) weekSpent += amt
            if (post.createdAt >= startOfMonth) monthSpent += amt
            if (post.createdAt >= startOfYear) yearSpent += amt

            if (amt > maxExpense) maxExpense = amt
            if (amt < minExpense) minExpense = amt

            categoryMap[post.category] = (categoryMap[post.category] ?: 0L) + amt
        }

        // Daily average in current month
        val daysPassedInMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val dailyAvg = monthSpent / daysPassedInMonth

        // Monthly trend (past 6 months)
        val trendList = mutableListOf<Pair<String, Long>>()
        val tempCal = Calendar.getInstance()
        for (i in 5 downTo 0) {
            tempCal.timeInMillis = now
            tempCal.add(Calendar.MONTH, -i)
            val m = tempCal.get(Calendar.MONTH) + 1
            val y = tempCal.get(Calendar.YEAR)
            val monthLabel = "T$m"

            val mStartCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m - 1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val mEndCal = (mStartCal.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
            }

            val mTotal = posts
                .filter { it.createdAt >= mStartCal.timeInMillis && it.createdAt < mEndCal.timeInMillis }
                .sumOf { it.amountVnd }

            trendList.add(Pair(monthLabel, mTotal))
        }

        _stats.value = FinancialStats(
            todaySpentVnd = todaySpent,
            weekSpentVnd = weekSpent,
            monthSpentVnd = monthSpent,
            yearSpentVnd = yearSpent,
            totalItems = posts.size,
            dailyAverageVnd = dailyAvg,
            maxExpenseVnd = maxExpense,
            minExpenseVnd = minExpense,
            categoryTotals = categoryMap,
            monthlyTrend = trendList
        )
    }
}
