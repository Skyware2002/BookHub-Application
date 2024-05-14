package com.skyware.bookhub.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.skyware.bookhub.R
import com.skyware.bookhub.database.BookDatabase
import com.skyware.bookhub.database.BookEntity
import com.skyware.bookhub.model.Book
import com.skyware.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.lang.Exception



class DescriptionActivity : AppCompatActivity() {

    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtBookPrice:TextView
    lateinit var txtBookRating: TextView
    lateinit var imgBookImage: ImageView
    lateinit var txtBookDesc: TextView
    lateinit var btnAddToFav: Button
    lateinit var progressBar: ProgressBar
    lateinit var progressLayout: RelativeLayout

    lateinit var toolbar: Toolbar

    var bookId: String? = "100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        imgBookImage = findViewById(R.id.imgBookImage)
        txtBookDesc = findViewById(R.id.txtBookDesc)
        btnAddToFav = findViewById(R.id.btnAddToFav)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"

        if (intent !=null) {
            bookId= intent.getStringExtra("book_id")
        } else {
            Toast.makeText(this@DescriptionActivity,"Some Unexpected Error Occured !!",Toast.LENGTH_SHORT).show()
        }

        if(bookId=="100") {
            finish()
            Toast.makeText(this@DescriptionActivity,"Some Unexpected Error Occured !!",Toast.LENGTH_SHORT).show()
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id",bookId)

        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)){
            val jsonRequest = object: JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {

                try {
                    progressLayout.visibility = View.GONE
                    val success: Boolean = it.getBoolean("success")
                    if (success) {
                        val bookJsonObject = it.getJSONObject("book_data")

                        val bookImageUrl = bookJsonObject.getString("image")

                        Picasso.get().load(bookJsonObject.getString("image")).error(R.drawable.default_book_cover).into(imgBookImage)
                        txtBookName.text = bookJsonObject.getString("name")
                        txtBookAuthor.text = bookJsonObject.getString("author")
                        txtBookPrice.text = bookJsonObject.getString("price")
                        txtBookRating.text = bookJsonObject.getString("rating")
                        txtBookDesc.text = bookJsonObject.getString("description")

                        val bookEntity = BookEntity(
                            bookId?.toInt() as Int,
                            txtBookName.text.toString(),
                            txtBookAuthor.text.toString(),
                            txtBookRating.text.toString(),
                            txtBookDesc.text.toString(),
                            txtBookPrice.text.toString(),
                            bookImageUrl
                        )

                        val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                        val isFav = checkFav.get()

                        if(isFav) {
                            btnAddToFav.text = "Remove from Favourites"
                            val favColor = ContextCompat.getColor(applicationContext,R.color.colorFavourite)
                            btnAddToFav.setBackgroundColor(favColor)
                        } else {
                            btnAddToFav.text = "Add from Favourites"
                            val nofavColor = ContextCompat.getColor(applicationContext,R.color.colorPrimary)
                            btnAddToFav.setBackgroundColor(nofavColor)
                        }

                        btnAddToFav.setOnClickListener{

                            if (!DBAsyncTask(applicationContext, bookEntity, 1).execute().get()){
                                val async = DBAsyncTask(applicationContext, bookEntity, 2).execute()
                                val result = async.get()
                                if (result) {
                                    Toast.makeText(this@DescriptionActivity, "Book Added To Favourites", Toast.LENGTH_SHORT).show()

                                    btnAddToFav.text = "Remove from Favourites"
                                    val favColor = ContextCompat.getColor(applicationContext,R.color.colorFavourite)
                                    btnAddToFav.setBackgroundColor(favColor)
                                } else {
                                    Toast.makeText(this@DescriptionActivity, "Some Error Occurred",Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val async = DBAsyncTask(applicationContext, bookEntity, 3).execute()
                                val result = async.get()

                                if(result) {
                                    Toast.makeText(this@DescriptionActivity, "Book Removed From Favourites",Toast.LENGTH_SHORT).show()

                                    btnAddToFav.text = "Add from Favourites"
                                    val nofavColor = ContextCompat.getColor(applicationContext,R.color.colorPrimary)
                                    btnAddToFav.setBackgroundColor(nofavColor)

                                } else {
                                    Toast.makeText(this@DescriptionActivity, "Some Error Occurred !!!", Toast.LENGTH_SHORT).show()
                                }
                            }

                        }

                    } else {
                        Toast.makeText(this@DescriptionActivity, "Some Error Occurred !!!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@DescriptionActivity, "Some Exception Error Occurred !!!", Toast.LENGTH_SHORT).show()
                }

            },Response.ErrorListener {
                Toast.makeText(this@DescriptionActivity, "Volley Error $it", Toast.LENGTH_SHORT).show()
            }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "dd7e00638c605c"
                    return headers

                }
            }

            queue.add(jsonRequest)
        } else {
            // Internet is Not Available
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is Not Found")
            dialog.setPositiveButton("Open Settings"){text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
                //Do Nothing
            }
            dialog.setNegativeButton("Exit") {text, listener ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
                //Do Nothing
            }
            dialog.create()
            dialog.show()
        }

    }

    class DBAsyncTask (val context: Context, val bookEntity: BookEntity, val mode: Int): AsyncTask<Void, Void, Boolean>() {

        /*
        * Mode 1 -> Check DB if the book is Fav or Not
        * Mode 2 -> Save the book into DB as favourite
        * Mode 3 -> Remove the favourite book
        * */

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            when(mode) {

                1 -> {
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }

                2 -> {
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }

                3 -> {
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }

            return false
        }

    }
}