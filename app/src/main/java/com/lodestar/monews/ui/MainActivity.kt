package com.lodestar.monews.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.lodestar.monews.R
import com.lodestar.monews.adapters.NewsHomeAdapter
import com.lodestar.monews.model.Article
import com.lodestar.monews.model.NewsData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = "FCM_MainActivity: "
    private var data: String = ""
    lateinit var newsHomeAdapter: NewsHomeAdapter
    lateinit var newsDataObject: NewsData
    var sorted: Boolean = false
    var oldToNew: Boolean = true
    var fcmToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sortBtn.isEnabled = false
        val linLayoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        newsRcv.layoutManager = linLayoutManager
        initFirebase()
        getData()
        initClickListeners()
        checkForNotifications()
    }

    private fun checkForNotifications() {
        try {
            val title = intent.getStringExtra("title")
            val body = intent.getStringExtra("body")
            val url = intent.getStringExtra("url")
            Log.d(TAG, title!!)
            Log.d(TAG, body!!)
            Log.d(TAG, url!!)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showProgressBar(status: Boolean) {
        if (status) {
            newsProgressBar.visibility = View.VISIBLE
        } else {
            newsProgressBar.visibility = View.GONE
        }
    }


    private fun initFirebase() {
        FirebaseApp.initializeApp(this)
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { instanceIdResult ->
                val token = instanceIdResult.token
                fcmToken = token
                Log.e("FCM_TOKEN:------", "Refreshed token: $token")
            }
    }

    private fun getData() {
        CoroutineScope(IO).launch {
            val job = launch {
                val newsData: NewsData = getNewsDataFromApi()
                Log.d("1112__", newsData.status)
                updateMainUI(newsData)
            }
        }
    }

    private suspend fun updateMainUI(newsData: NewsData) {
        withContext(Main) {
            if (newsDataObject.articles.isNullOrEmpty()) {
                showProgressBar(false)
                Toast.makeText(
                    this@MainActivity,
                    "Some error occurred. Try again later.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                showProgressBar(false)
                newsHomeAdapter = NewsHomeAdapter(this@MainActivity, newsDataObject.articles)
                newsRcv.adapter = newsHomeAdapter
                sortBtn.isEnabled = true
            }
        }
    }

    //  get timestamp in milli-seconds
    private fun getTimestamp(publishedAt: String?): Long {
//  val dateString = "2013-09-19T03:27:23+01:00"
        var testDate: Date? = null
        if (publishedAt != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            try {
                testDate = sdf.parse(publishedAt)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return testDate!!.time
    }

    // fetch news data
    private suspend fun getNewsDataFromApi(): NewsData {
        try {
            Log.d("1112__", "running...")
            val url = URL(
                "https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json"
            )
            val httpURLConnection = url.openConnection() as HttpURLConnection
            val inputStream = httpURLConnection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = ""
            while (line != null) {
                line = bufferedReader.readLine()
                data += line
            }
            val jsonObject = JSONObject(data)
            val gson = Gson()
            newsDataObject = gson.fromJson(jsonObject.toString(), NewsData::class.java)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            Log.d("1112__1", e.message!!)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("1112__2", e.message!!)
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.d("1112__3", e.message!!)
        }
        if (!this::newsDataObject.isInitialized) {
            newsDataObject = NewsData()
        }
        return newsDataObject
    }

    private fun initClickListeners() {
        sortBtn.setOnClickListener {
            if (!sorted) {
                Toast.makeText(this, "Sorting Old to New", Toast.LENGTH_SHORT).show()
                for (n in newsDataObject.articles.indices) {
                    newsDataObject.articles[n].timestamp =
                        getTimestamp(newsDataObject.articles[n].publishedAt)
                }
                newsDataObject.articles.sort()
                if (this::newsHomeAdapter.isInitialized) {
                    showProgressBar(false)
                    newsHomeAdapter.notifyDataSetChanged()
                } else {
                    showProgressBar(false)
                    newsHomeAdapter = NewsHomeAdapter(
                        this@MainActivity,
                        newsDataObject.articles as ArrayList<Article>
                    )
                    newsRcv.adapter = newsHomeAdapter
                }
                sorted = true
            } else {
                if (oldToNew) {
                    Toast.makeText(this, "Sorting New to Old", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Sorting Old to New", Toast.LENGTH_SHORT).show()
                }
                newsDataObject.articles.reverse()
                oldToNew = !oldToNew
                if (this::newsHomeAdapter.isInitialized) {
                    newsHomeAdapter.notifyDataSetChanged()
                } else {
                    newsHomeAdapter = NewsHomeAdapter(
                        this@MainActivity,
                        newsDataObject.articles as ArrayList<Article>
                    )
                    newsRcv.adapter = newsHomeAdapter
                }
            }
        }

        appName.setOnClickListener {
            Toast.makeText(this, "Copied fcmToken to clipboard", Toast.LENGTH_SHORT).show()
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("fcmToken", fcmToken)
            clipboard.setPrimaryClip(clip)
        }
    }
}