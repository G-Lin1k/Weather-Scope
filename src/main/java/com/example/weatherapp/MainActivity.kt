package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var homeRL: RelativeLayout
    private lateinit var loadingPB: ProgressBar
    private lateinit var cityNameTV: TextView
    private lateinit var temperatureTV: TextView
    private lateinit var conditionTV: TextView
    private lateinit var weatherRV: RecyclerView
    private lateinit var cityEdt: com.google.android.material.textfield.TextInputEditText
    private lateinit var backIV: ImageView
    private lateinit var iconIV: ImageView
    private lateinit var searchIV: ImageView
    private lateinit var weatherRVModalArrayList: ArrayList<WeatherRVModel>
    private lateinit var weatherRVAdapter: WeatherRVAdapter

    private lateinit var locationManager: LocationManager
    private val PERMISSION_CODE = 1
    private var cityName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContentView(R.layout.activity_main)

        homeRL = findViewById(R.id.idRLHome)
        loadingPB = findViewById(R.id.idPBLoading)
        cityNameTV = findViewById(R.id.idTVCityName)
        temperatureTV = findViewById(R.id.idTVTemperature)
        conditionTV = findViewById(R.id.idTVCondition)
        weatherRV = findViewById(R.id.idRvWeather)
        cityEdt = findViewById(R.id.idEdtCity)
        backIV = findViewById(R.id.idIVBack)
        iconIV = findViewById(R.id.idIVIcon)
        searchIV = findViewById(R.id.idTVSearch)

        weatherRVModalArrayList = ArrayList()
        weatherRVAdapter = WeatherRVAdapter(this, weatherRVModalArrayList)
        weatherRV.adapter = weatherRVAdapter

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_CODE
            )
        } else {
            val location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            cityName = getCityName(location?.longitude ?: 0.0, location?.latitude ?: 0.0)
            getWeatherInfo(cityName)
        }

        searchIV.setOnClickListener {
            val city = cityEdt.text.toString()
            if (city.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please enter city", Toast.LENGTH_SHORT).show()
            } else {
                cityNameTV.text = city
                getWeatherInfo(city)
            }
        }
    }

    private fun getCityName(longitude: Double, latitude: Double): String {
        var cityName = "Not Found"
        val gcd = Geocoder(baseContext, Locale.getDefault())
        try {
            val addresses: List<Address>? = gcd.getFromLocation(latitude, longitude, 10)
            for (adr: Address in addresses ?: ArrayList<Address>()) {
                if (adr != null) {
                    val city: String? = adr.locality
                    if (city != null && city != "") {
                        cityName = city
                    } else {
                        Log.d("TAG", "CITY NOT FOUND")
                        Toast.makeText(this@MainActivity, "User City Not Found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return cityName
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please Provide The Permission", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun getWeatherInfo(cityName: String?) {
        val url =
            "http://api.weatherapi.com/v1/forecast.json?key=ff88217e7dd845198f664859232711&q=$cityName&days=1&aqi=yes&alerts=yes"

        cityNameTV.text = cityName
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener<JSONObject> { response ->
                loadingPB.visibility = View.GONE
                homeRL.visibility = View.VISIBLE
                weatherRVModalArrayList.clear()

                try {
                    val temperature: String = response.getJSONObject("current").getString("temp_c")
                    temperatureTV.text = "$temperature°C"
                    val isDay: Int = response.getJSONObject("current").getInt("is_day")
                    val condition: String =
                        response.getJSONObject("current").getJSONObject("condition").getString("text")
                    val conditionIcon: String =
                        response.getJSONObject("current").getJSONObject("condition").getString("icon")
                    Picasso.get().load("http:$conditionIcon").into(iconIV)
                    conditionTV.text = condition
                    if (isDay == 1) {
                        Picasso.get().load("https://unsplashmorning").into(backIV)
                    } else {
                        Picasso.get().load("https://unsplashmidnight").into(backIV)
                    }

                    val forecastObj: JSONObject = response.getJSONObject("forecast")
                    val forecast0: JSONObject = forecastObj.getJSONArray("forecastday").getJSONObject(0)
                    val hourArray: JSONArray = forecast0.getJSONArray("hour")

                    for (i in 0 until hourArray.length()) {
                        val hourObj: JSONObject = hourArray.getJSONObject(i)
                        val time: String = hourObj.getString("time")
                        val temper: String = hourObj.getString("temp_c")
                        val img: String = hourObj.getJSONObject("condition").getString("icon")
                        val wind: String = hourObj.getString("wind_kph")
                        weatherRVModalArrayList.add(WeatherRVModel(time, temper, img, wind))
                    }

                    weatherRVAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this@MainActivity, "Please enter a valid city name...", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }
}
