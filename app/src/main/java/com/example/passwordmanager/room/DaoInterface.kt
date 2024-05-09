package com.example.passwordmanager.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert


@Dao
interface DaoInterface {
    @Upsert
    suspend fun upsertRow(accountPassword: AccountPassword)

    @Query("DELETE FROM accountpassword WHERE id = :id")
    suspend fun deleteRowByID(id: Long)

    @Query("SELECT * FROM accountpassword WHERE id = :id")
    suspend fun getRowByID(id: Long): AccountPassword?

    @Query("SELECT * FROM accountpassword ORDER BY accountName")
    suspend fun getAllRows(): List<AccountPassword>?

    @Query("SELECT * FROM keys WHERE id = :id")
    suspend fun getKeyByID(id: Long): Keys

    @Upsert
    suspend fun insertKey(keys: Keys)
}