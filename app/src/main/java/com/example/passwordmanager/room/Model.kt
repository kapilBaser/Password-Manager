package com.example.passwordmanager.room

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class AccountPassword(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var accountName: String,
    var username: String,
    var password: String
)

@Entity
data class Keys(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val specKey: String,
    val iv: String
)