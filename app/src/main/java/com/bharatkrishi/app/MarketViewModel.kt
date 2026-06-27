package com.bharatkrishi.app

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bharatkrishi.app.network.AgriApi
import com.bharatkrishi.app.network.AgriApiRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


sealed class DataState {
    object Loading : DataState()
    data class Success(val data: List<MarketData>) : DataState()
    data class Error(val message: String) : DataState()
}

class MarketViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = AgriApi.retrofitService
    private val marketDao = AppDatabase.getDatabase(application).marketDao()

    private val _marketDataState = MutableLiveData<DataState>()
    val marketDataState: LiveData<DataState> = _marketDataState

    private val _selectedState = MutableLiveData<String?>()
    val selectedState: LiveData<String?> = _selectedState

    private val _selectedCommodity = MutableLiveData<String?>()
    val selectedCommodity: LiveData<String?> = _selectedCommodity

    private var allCachedData: List<MarketData> = emptyList()
    private var fetchJob: Job? = null
    private var lastApiFetchTime = 0L

    companion object {
        private const val MIN_API_INTERVAL_MS = 60_000L
        private const val DEBOUNCE_MS = 500L
    }

    init {
        loadCacheAndFetch()
    }

    fun onStateSelected(state: String?) {
        _selectedState.value = state
        applyFiltersLocally()
    }

    fun onCommoditySelected(commodity: String?) {
        _selectedCommodity.value = commodity
        applyFiltersLocally()
    }

    fun retry() {
        lastApiFetchTime = 0L
        loadCacheAndFetch()
    }

    private fun applyFiltersLocally() {
        if (allCachedData.isNotEmpty()) {
            postFilteredData()
        } else {
            loadCacheAndFetch()
        }
    }

    private fun postFilteredData() {
        val stateFilter = _selectedState.value
        val commodityFilter = _selectedCommodity.value

        val filtered = allCachedData.filter { item ->
            val stateMatch = stateFilter.isNullOrBlank() ||
                item.state.equals(stateFilter, ignoreCase = true)
            val commodityMatch = commodityFilter.isNullOrBlank() ||
                item.commodity.equals(commodityFilter, ignoreCase = true)
            stateMatch && commodityMatch
        }
        _marketDataState.postValue(DataState.Success(filtered))
    }

    private fun loadCacheAndFetch() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            _marketDataState.postValue(DataState.Loading)

            try {
                val cachedData = marketDao.getAll()
                if (cachedData.isNotEmpty()) {
                    allCachedData = cachedData
                    postFilteredData()
                }
            } catch (e: Exception) {
                Log.e("MarketVM", "Cache read error", e)
            }

            val now = System.currentTimeMillis()
            if (now - lastApiFetchTime < MIN_API_INTERVAL_MS && allCachedData.isNotEmpty()) {
                Log.d("MarketVM", "Skipping API call - within cooldown, using cache")
                return@launch
            }

            delay(DEBOUNCE_MS)
            loadFromApi()
        }
    }

    private suspend fun loadFromApi() {
        var lastError: Exception? = null

        for (attempt in 1..3) {
            try {
                val apiFilters = mutableMapOf<String, String>()
                val state = _selectedState.value
                val commodity = _selectedCommodity.value

                if (!state.isNullOrBlank()) {
                    apiFilters["filters[state]"] = state
                }
                if (!commodity.isNullOrBlank()) {
                    apiFilters["filters[commodity]"] = commodity
                }

                val apiResponse = apiService.getMarketData(
                    apiKey = BuildConfig.MARKET_API_KEY,
                    format = "json",
                    offset = "0",
                    limit = "50",
                    filters = apiFilters
                )

                val newData = mapApiResponseToMarketData(apiResponse.records)
                lastApiFetchTime = System.currentTimeMillis()

                if (newData.isNotEmpty()) {
                    if (state.isNullOrBlank() && commodity.isNullOrBlank()) {
                        try {
                            marketDao.deleteAll()
                            marketDao.insertAll(newData)
                        } catch (e: Exception) {
                            Log.e("MarketVM", "Cache write error", e)
                        }
                        allCachedData = newData
                    } else {
                        allCachedData = newData
                    }
                }

                postFilteredData()
                return

            } catch (e: Exception) {
                lastError = e
                Log.e("MarketVM", "API attempt $attempt failed", e)
                if (attempt < 3) {
                    delay(2000L * attempt)
                }
            }
        }

        if (allCachedData.isNotEmpty()) {
            Log.d("MarketVM", "API failed, showing cached data")
            postFilteredData()
        } else {
            _marketDataState.postValue(
                DataState.Error("Could not load prices. Tap to retry.")
            )
        }
    }

    private fun mapApiResponseToMarketData(apiRecords: List<AgriApiRecord>): List<MarketData> {
        return apiRecords.map { record ->
            MarketData(
                state = record.state,
                district = record.district,
                market = record.market,
                commodity = record.commodity,
                variety = record.variety,
                min_price = record.min_price,
                max_price = record.max_price,
                modal_price = record.modal_price
            )
        }
    }
}
