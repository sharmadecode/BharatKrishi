package com.bharatkrishi.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_data_table") 
data class MarketData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, 

    val state: String?,
    val district: String?,
    val market: String?,
    val commodity: String?,
    val variety: String?,
    val min_price: String?,
    val max_price: String?,
    val modal_price: String?,

    
    val lastRefreshed: Long = System.currentTimeMillis()
)