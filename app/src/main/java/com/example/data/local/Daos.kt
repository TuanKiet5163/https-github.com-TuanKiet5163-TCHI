package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE tchiId = :tchiId LIMIT 1")
    suspend fun getUserByTchiId(tchiId: String): UserEntity?

    @Query("""
        SELECT * FROM users 
        WHERE userId != :currentUserId 
          AND (LOWER(username) LIKE '%' || LOWER(:query) || '%' 
               OR LOWER(tchiId) LIKE '%' || LOWER(:query) || '%'
               OR LOWER(displayName) LIKE '%' || LOWER(:query) || '%')
        LIMIT 20
    """)
    suspend fun searchUsers(query: String, currentUserId: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE userId IN (:userIds)")
    suspend fun getUsersByIds(userIds: List<String>): List<UserEntity>

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE postId = :postId")
    suspend fun deletePostById(postId: String)

    @Query("SELECT * FROM posts WHERE postId = :postId LIMIT 1")
    suspend fun getPostById(postId: String): PostEntity?

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUserId(userId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getPostsByUserIdSync(userId: String): List<PostEntity>

    @Query("""
        SELECT * FROM posts 
        WHERE userId = :userId 
           OR (userId IN (:friendIds) AND visibility IN ('FRIENDS', 'PUBLIC'))
        ORDER BY createdAt DESC
    """)
    fun getFeedPosts(userId: String, friendIds: List<String>): Flow<List<PostEntity>>

    @Query("""
        SELECT * FROM posts 
        WHERE userId != :currentUserId 
          AND visibility = 'PUBLIC'
        ORDER BY createdAt DESC 
        LIMIT 50
    """)
    fun getPublicPosts(currentUserId: String): Flow<List<PostEntity>>

    @Query("DELETE FROM posts WHERE userId = :userId")
    suspend fun deleteAllUserPosts(userId: String)
}

@Dao
interface FriendshipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendship(friendship: FriendshipEntity)

    @Update
    suspend fun updateFriendship(friendship: FriendshipEntity)

    @Query("DELETE FROM friendships WHERE friendshipId = :friendshipId")
    suspend fun deleteFriendship(friendshipId: String)

    @Query("""
        SELECT * FROM friendships 
        WHERE (senderId = :user1 AND receiverId = :user2) 
           OR (senderId = :user2 AND receiverId = :user1) 
        LIMIT 1
    """)
    suspend fun getFriendshipBetween(user1: String, user2: String): FriendshipEntity?

    @Query("""
        SELECT * FROM friendships 
        WHERE (senderId = :userId OR receiverId = :userId) 
          AND status = 'ACCEPTED'
    """)
    fun getAcceptedFriendships(userId: String): Flow<List<FriendshipEntity>>

    @Query("""
        SELECT * FROM friendships 
        WHERE (senderId = :userId OR receiverId = :userId) 
          AND status = 'ACCEPTED'
    """)
    suspend fun getAcceptedFriendshipsSync(userId: String): List<FriendshipEntity>

    @Query("SELECT * FROM friendships WHERE receiverId = :userId AND status = 'PENDING'")
    fun getReceivedPendingRequests(userId: String): Flow<List<FriendshipEntity>>

    @Query("SELECT * FROM friendships WHERE senderId = :userId AND status = 'PENDING'")
    fun getSentPendingRequests(userId: String): Flow<List<FriendshipEntity>>

    @Query("DELETE FROM friendships WHERE senderId = :userId OR receiverId = :userId")
    suspend fun deleteAllUserFriendships(userId: String)
}

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity)

    @Query("""
        SELECT * FROM budgets 
        WHERE userId = :userId AND month = :month AND year = :year AND category IS NULL 
        LIMIT 1
    """)
    fun getMonthlyBudget(userId: String, month: Int, year: Int): Flow<BudgetEntity?>

    @Query("""
        SELECT * FROM budgets 
        WHERE userId = :userId AND month = :month AND year = :year AND category IS NULL 
        LIMIT 1
    """)
    suspend fun getMonthlyBudgetSync(userId: String, month: Int, year: Int): BudgetEntity?

    @Query("""
        SELECT * FROM budgets 
        WHERE userId = :userId AND month = :month AND year = :year AND category IS NOT NULL
    """)
    fun getCategoryBudgets(userId: String, month: Int, year: Int): Flow<List<BudgetEntity>>

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllUserBudgets(userId: String)
}
