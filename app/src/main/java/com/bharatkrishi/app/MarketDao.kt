package com.bharatkrishi.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MarketDao {

    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(marketData: List<MarketData>)

    
    @Query("SELECT * FROM market_data_table")
    suspend fun getAll(): List<MarketData>

    
    @Query("DELETE FROM market_data_table")
    suspend fun deleteAll()
}
