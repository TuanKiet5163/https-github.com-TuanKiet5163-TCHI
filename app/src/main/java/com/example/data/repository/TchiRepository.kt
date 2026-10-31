package com.example.data.repository

import android.content.Context
import com.example.data.local.BudgetDao
import com.example.data.local.BudgetEntity
import com.example.data.local.FriendshipDao
import com.example.data.local.FriendshipEntity
import com.example.data.local.PostDao
import com.example.data.local.PostEntity
import com.example.data.local.TchiDatabase
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest
import java.util.Calendar
import java.util.UUID
import kotlin.random.Random

data class FriendItem(
    val friendshipId: String,
    val friendUser: UserEntity,
    val status: String,
    val isSender: Boolean
)

data class FeedPostItem(
    val post: PostEntity,
    val author: UserEntity
)

class TchiRepository(private val context: Context) {

    private val db = TchiDatabase.getDatabase(context)
    val userDao: UserDao = db.userDao()
    val postDao: PostDao = db.postDao()
    val friendshipDao: FriendshipDao = db.friendshipDao()
    val budgetDao: BudgetDao = db.budgetDao()

    private val prefs = context.getSharedPreferences("tchi_session", Context.MODE_PRIVATE)

    fun getCurrentUserId(): String? {
        return prefs.getString("active_user_id", null)
    }

    fun setCurrentUserId(userId: String?) {
        if (userId == null) {
            prefs.edit().remove("active_user_id").apply()
        } else {
            prefs.edit().putString("active_user_id", userId).apply()
        }
    }

    // Password Hashing
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Generate unique TCHI ID like "TCHI-74921"
    private suspend fun generateUniqueTchiId(): String {
        while (true) {
            val randomNum = Random.nextInt(10000, 99999)
            val candidateId = "TCHI-$randomNum"
            val existing = userDao.getUserByTchiId(candidateId)
            if (existing == null) {
                return candidateId
            }
        }
    }

    suspend fun registerUser(
        username: String,
        displayName: String,
        passwordPlain: String,
        bio: String = ""
    ): Result<UserEntity> {
        val cleanUsername = username.trim().lowercase()
        if (cleanUsername.length < 3) {
            return Result.failure(Exception("Tên đăng nhập phải có ít nhất 3 ký tự"))
        }
        if (passwordPlain.length < 6) {
            return Result.failure(Exception("Mật khẩu phải có ít nhất 6 ký tự"))
        }
        val existing = userDao.getUserByUsername(cleanUsername)
        if (existing != null) {
            return Result.failure(Exception("Tên đăng nhập đã tồn tại"))
        }

        val tchiId = generateUniqueTchiId()
        val newUser = UserEntity(
            userId = UUID.randomUUID().toString(),
            username = cleanUsername,
            displayName = displayName.trim().ifEmpty { cleanUsername },
            avatarPath = null,
            tchiId = tchiId,
            bio = bio.trim(),
            passwordHash = hashPassword(passwordPlain),
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)
        setCurrentUserId(newUser.userId)
        return Result.success(newUser)
    }

    suspend fun loginUser(usernameOrTchiId: String, passwordPlain: String): Result<UserEntity> {
        val query = usernameOrTchiId.trim()
        val user = userDao.getUserByUsername(query.lowercase())
            ?: userDao.getUserByTchiId(query.uppercase())
            ?: return Result.failure(Exception("Không tìm thấy tài khoản"))

        val hashedInput = hashPassword(passwordPlain)
        if (user.passwordHash != hashedInput) {
            return Result.failure(Exception("Mật khẩu không chính xác"))
        }

        setCurrentUserId(user.userId)
        return Result.success(user)
    }

    fun logout() {
        setCurrentUserId(null)
    }

    suspend fun getCurrentUser(): UserEntity? {
        val id = getCurrentUserId() ?: return null
        return userDao.getUserById(id)
    }

    fun getCurrentUserFlow(): Flow<UserEntity?> = flow {
        val id = getCurrentUserId()
        if (id == null) {
            emit(null)
        } else {
            userDao.getUserFlow(id).collect { emit(it) }
        }
    }

    suspend fun updateUserProfile(
        displayName: String,
        bio: String,
        avatarPath: String?
    ): Result<Unit> {
        val user = getCurrentUser() ?: return Result.failure(Exception("Chưa đăng nhập"))
        val updated = user.copy(
            displayName = displayName.trim(),
            bio = bio.trim(),
            avatarPath = avatarPath ?: user.avatarPath
        )
        userDao.updateUser(updated)
        return Result.success(Unit)
    }

    suspend fun changePassword(oldPasswordPlain: String, newPasswordPlain: String): Result<Unit> {
        val user = getCurrentUser() ?: return Result.failure(Exception("Chưa đăng nhập"))
        if (hashPassword(oldPasswordPlain) != user.passwordHash) {
            return Result.failure(Exception("Mật khẩu hiện tại không đúng"))
        }
        if (newPasswordPlain.length < 6) {
            return Result.failure(Exception("Mật khẩu mới phải có ít nhất 6 ký tự"))
        }
        val updated = user.copy(passwordHash = hashPassword(newPasswordPlain))
        userDao.updateUser(updated)
        return Result.success(Unit)
    }

    suspend fun deleteAccount(): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
        postDao.deleteAllUserPosts(userId)
        friendshipDao.deleteAllUserFriendships(userId)
        budgetDao.deleteAllUserBudgets(userId)
        userDao.deleteUser(userId)
        setCurrentUserId(null)
        return Result.success(Unit)
    }

    // POSTS
    suspend fun createPost(
        imageUri: String,
        productName: String,
        amountVnd: Long,
        category: String,
        paymentMethod: String,
        note: String,
        visibility: String
    ): Result<PostEntity> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
        if (productName.isBlank()) {
            return Result.failure(Exception("Vui lòng nhập tên món đồ"))
        }
        if (amountVnd <= 0L) {
            return Result.failure(Exception("Số tiền phải lớn hơn 0 VNĐ"))
        }

        val newPost = PostEntity(
            postId = UUID.randomUUID().toString(),
            userId = userId,
            imageUri = imageUri,
            productName = productName.trim(),
            amountVnd = amountVnd,
            category = category,
            paymentMethod = paymentMethod,
            note = note.trim(),
            visibility = visibility,
            createdAt = System.currentTimeMillis()
        )
        postDao.insertPost(newPost)
        return Result.success(newPost)
    }

    suspend fun updatePost(post: PostEntity): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
        if (post.userId != currentUserId) {
            return Result.failure(Exception("Bạn không có quyền chỉnh sửa bài đăng này"))
        }
        postDao.updatePost(post)
        return Result.success(Unit)
    }

    suspend fun deletePost(post: PostEntity): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
        if (post.userId != currentUserId) {
            return Result.failure(Exception("Bạn không có quyền xóa bài đăng này"))
        }
        postDao.deletePost(post)
        return Result.success(Unit)
    }

    // FRIENDSHIPS
    suspend fun sendFriendRequest(targetUserId: String): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
        if (currentUserId == targetUserId) {
            return Result.failure(Exception("Không thể kết bạn với chính mình"))
        }
        val existing = friendshipDao.getFriendshipBetween(currentUserId, targetUserId)
        if (existing != null) {
            if (existing.status == "ACCEPTED") {
                return Result.failure(Exception("Đã là bạn bè"))
            } else if (existing.status == "PENDING") {
                return Result.failure(Exception("Đang chờ phản hồi lời mời"))
            }
        }
        val friendship = FriendshipEntity(
            friendshipId = UUID.randomUUID().toString(),
            senderId = currentUserId,
            receiverId = targetUserId,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        friendshipDao.insertFriendship(friendship)
        return Result.success(Unit)
    }

    suspend fun acceptFriendRequest(friendshipId: String): Result<Unit> {
        val friendship = friendshipDao.getReceivedPendingRequests(getCurrentUserId() ?: "")
            .firstOrNull()?.find { it.friendshipId == friendshipId }
            ?: return Result.failure(Exception("Không tìm thấy lời mời"))
        val updated = friendship.copy(status = "ACCEPTED")
        friendshipDao.updateFriendship(updated)
        return Result.success(Unit)
    }

    suspend fun declineOrCancelFriendRequest(friendshipId: String): Result<Unit> {
        friendshipDao.deleteFriendship(friendshipId)
        return Result.success(Unit)
    }

    suspend fun removeFriend(targetUserId: String): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
        val existing = friendshipDao.getFriendshipBetween(currentUserId, targetUserId)
        if (existing != null) {
            friendshipDao.deleteFriendship(existing.friendshipId)
        }
        return Result.success(Unit)
    }

    // BUDGET
    suspend fun setMonthlyBudget(amountVnd: Long, month: Int, year: Int): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
        val existing = budgetDao.getMonthlyBudgetSync(currentUserId, month, year)
        val budget = if (existing != null) {
            existing.copy(amountVnd = amountVnd)
        } else {
            BudgetEntity(
                budgetId = UUID.randomUUID().toString(),
                userId = currentUserId,
                category = null,
                amountVnd = amountVnd,
                month = month,
                year = year
            )
        }
        budgetDao.insertOrUpdateBudget(budget)
        return Result.success(Unit)
    }
}
