package com.bharatkrishi.app.network

data class Forecast(
    val alerts: Alerts,
    val forecastday: List<Forecastday>
)

data class Alerts(
    val alert: List<Any>
)


data class Forecastday(
    val astro: Astro,
    val date: String,
    val date_epoch: String,
    val day: Day,
    val hour: List<Hour>
)

data class Astro(
    val is_moon_up: String,
    val is_sun_up: String,
    val moon_illumination: String,
    val moon_phase: String,
    val moonrise: String,
    val moonset: String,
    val sunrise: String,
    val sunset: String
)
data class Day(
    val avghumidity: String,
    val avgtemp_c: String,
    val avgtemp_f: String,
    val avgvis_km: String,
    val avgvis_miles: String,
    val condition: ConditionX,
    val daily_chance_of_rain: String,
    val daily_chance_of_snow: String,
    val daily_will_it_rain: String,
    val daily_will_it_snow: String,
    val maxtemp_c: String,
    val maxtemp_f: String,
    val maxwind_kph: String,
    val maxwind_mph: String,
    val mintemp_c: String,
    val mintemp_f: String,
    val totalprecip_in: String,
    val totalprecip_mm: String,
    val totalsnow_cm: String,
    val uv: String
)

data class Hour(
    val chance_of_rain: String,
    val chance_of_snow: String,
    val cloud: String,
    val condition: ConditionX,
    val dewpoint_c: String,
    val dewpoint_f: String,
    val diff_rad: String,
    val dni: String,
    val feelslike_c: String,
    val feelslike_f: String,
    val gti: String,
    val gust_kph: String,
    val gust_mph: String,
    val heatindex_c: String,
    val heatindex_f: String,
    val humidity: String,
    val is_day: String,
    val precip_in: String,
    val precip_mm: String,
    val pressure_in: String,
    val pressure_mb: String,
    val short_rad: String,
    val snow_cm: String,
    val temp_c: String,
    val temp_f: String,
    val time: String,
    val time_epoch: String,
    val uv: String,
    val vis_km: String,
    val vis_miles: String,
    val will_it_rain: String,
    val will_it_snow: String,
    val wind_degree: String,
    val wind_dir: String,
    val wind_kph: String,
    val wind_mph: String,
    val windchill_c: String,
    val windchill_f: String
)
data class ConditionX(
    val code: String,
    val icon: String,
    val text: String
)