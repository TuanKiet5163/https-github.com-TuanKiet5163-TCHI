package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["tchiId"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarPath: String? = null,
    val tchiId: String,
    val bio: String = "",
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "posts",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"]),
        Index(value = ["visibility"])
    ]
)
data class PostEntity(
    @PrimaryKey
    val postId: String,
    val userId: String,
    val imageUri: String,
    val productName: String,
    val amountVnd: Long,
    val category: String,
    val paymentMethod: String,
    val note: String = "",
    val visibility: String = "PRIVATE", // PRIVATE, FRIENDS, PUBLIC
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "friendships",
    indices = [
        Index(value = ["senderId", "receiverId"], unique = true),
        Index(value = ["status"])
    ]
)
data class FriendshipEntity(
    @PrimaryKey
    val friendshipId: String,
    val senderId: String,
    val receiverId: String,
    val status: String, // PENDING, ACCEPTED, DECLINED, BLOCKED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["userId", "month", "year", "category"])
    ]
)
data class BudgetEntity(
    @PrimaryKey
    val budgetId: String,
    val userId: String,
    val category: String? = null, // null = overall monthly budget
    val amountVnd: Long,
    val month: Int,
    val year: Int
)
