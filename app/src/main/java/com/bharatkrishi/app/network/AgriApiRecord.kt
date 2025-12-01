package com.bharatkrishi.app.network
/**
 * This data class represents a single record received from the AGMARKET API.
 * The variable names here must exactly match the field names in the JSON response.
 */
data class AgriApiRecord(
    val state: String?,
    val district: String?,
    val market: String?,
    val commodity: String?,
    val variety: String?,
    val arrival_date: String?,
    val min_price: String?,
    val max_price: String?,
    val modal_price: String?
)