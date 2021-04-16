package com.example.newstoday

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NewsItemClicked {

    private lateinit var mAdapter: NewsListAdapter

    val BASE_URL = "https://saurav.tech/NewsAPI"

    var categoryText: String = "general"
    var countryText: String = "in"

    var top_headlines_url = "$BASE_URL/top-headlines/category/$categoryText/$countryText.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val categories = arrayOf("General", "Entertainment", "Business", "Health", "Science", "Sports", "Technology")
        val countries = arrayOf("India", "USA", "Australia", "Russia", "France", "UK")

        val spinner1: Spinner = findViewById(R.id.spinner1)
        val arrayAdapter1 = ArrayAdapter(this, R.layout.my_spinner_list, categories)
        arrayAdapter1.setDropDownViewResource(R.layout.my_spinner_dropdown_list)
        spinner1.adapter = arrayAdapter1

        val spinner2: Spinner = findViewById(R.id.spinner2)
        val arrayAdapter2 = ArrayAdapter(this, R.layout.my_spinner_list, countries)
        arrayAdapter2.setDropDownViewResource(R.layout.my_spinner_dropdown_list)
        spinner2.adapter = arrayAdapter2

        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchData()
        mAdapter = NewsListAdapter(this)
        recyclerView.adapter = mAdapter

        spinner1.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                categoryText = categories[p2].decapitalize(Locale.ROOT);
                top_headlines_url = "$BASE_URL/top-headlines/category/$categoryText/$countryText.json"
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spinner2.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(countries[p2]) {
                    "India" -> countryText = "in"
                    "USA" -> countryText = "us"
                    "Australia" -> countryText = "au"
                    "Russia" -> countryText = "ru"
                    "France" -> countryText = "fr"
                    "UK" -> countryText = "gb"
                }
                top_headlines_url = "$BASE_URL/top-headlines/category/$categoryText/$countryText.json"
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun fetchData() {

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, top_headlines_url, null,
            {
                val newsJsonArray = it.getJSONArray("articles")
                val newsArray = ArrayList<News>()
                for (i in 0 until newsJsonArray.length()) {
                    val newsJsonObject = newsJsonArray.getJSONObject(i)
                    val news = News(
                        newsJsonObject.getString("title"),
                        newsJsonObject.getString("author"),
                        newsJsonObject.getString("url"),
                        newsJsonObject.getString("urlToImage")
                    )
                    newsArray.add(news)
                }

                mAdapter.updateNews(newsArray)
            },
            {
                Log.d("error listener", it.localizedMessage)
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            })

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    override fun onItemClicked(item: News) {
        //Toast.makeText(this, "Author of the news: ${item.author}", Toast.LENGTH_LONG).show()
        /*val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(item.url)
        val chooser = Intent.createChooser(openURL, "Open URL using...")
        startActivity(chooser)*/

        //Using chrome custom tabs:-
        val builder = CustomTabsIntent.Builder();
        builder.setToolbarColor(Color.parseColor("#6200EE"))
        val customTabsIntent = builder.build();

        val packageName = "com.android.chrome"
        customTabsIntent.intent.setPackage(packageName)
        customTabsIntent.launchUrl(this, Uri.parse(item.url));

    }

    fun search(view: View) {
        fetchData()
        mAdapter = NewsListAdapter(this)
        recyclerView.adapter = mAdapter
    }
}