package com.jainhardik120.passbud.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.jainhardik120.passbud.data.local.entities.Credential
import com.jainhardik120.passbud.data.local.entities.CredentialAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialsDao{

    @Query("SELECT * FROM credentials_account_table")
    fun getAllAccounts() : Flow<List<CredentialAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createAccount(account: CredentialAccount)

    @Upsert
    suspend fun upsertCredential(credential: Credential)

    @Query("SELECT * FROM credentials_account_table WHERE accountId=:accountId")
    fun getAccountDetails(accountId : String) : Flow<CredentialAccount>

    @Query("SELECT * FROM credential_table WHERE accountId=:accountId")
    fun getAccountCredentials(accountId : String) : Flow<List<Credential>>


}