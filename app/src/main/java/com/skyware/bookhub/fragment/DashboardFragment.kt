package com.skyware.bookhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.skyware.bookhub.R
import com.skyware.bookhub.adapter.DashboardRecyclerAdapter
import com.skyware.bookhub.model.Book
import com.skyware.bookhub.util.ConnectionManager
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.util.Collections


class DashboardFragment : Fragment() {

    private lateinit var recyclerDashboard: RecyclerView

    lateinit var layoutManager: RecyclerView.LayoutManager

    //lateinit var btnCheckInternet: Button

    lateinit var recyclerAdapter: DashboardRecyclerAdapter

    lateinit var progressLayout: RelativeLayout

    lateinit var progreressbar: ProgressBar

    /*val bookList = arrayListOf<String>(
    "P.S. I love You",
    "The Great Gatsby",
    "Anna Karenina",
    "Madame Bovary",
    "War and Peace",
    "Lolita",
    "Middlemarch",
    "The Adventures of Huckleberry Finn",
    "Moby-Dick",
    "The Lord of the Rings"
)*/

    val bookInfoList = arrayListOf<Book>()

    var ratingComparator = Comparator<Book>{book1, book2 ->

        if (book1.bookRating.compareTo(book2.bookRating, true) == 0) {
            book1.bookName.compareTo(book2.bookName, true)
        } else {
            book1.bookRating.compareTo(book2.bookRating, true)
        }
    }

    /*(
        Book("P.S. I love You", "Cecelia Ahern", "Rs. 299", "4.5", R.drawable.ps_ily),
        Book("The Great Gatsby", "F. Scott Fitzgerald", "Rs. 399", "4.1", R.drawable.great_gatsby),
        Book("Anna Karenina", "Leo Tolstoy", "Rs. 199", "4.3", R.drawable.anna_kare),
        Book("Madame Bovary", "Gustave Flaubert", "Rs. 500", "4.0", R.drawable.madame),
        Book("War and Peace", "Leo Tolstoy", "Rs. 249", "4.8", R.drawable.war_and_peace),
        Book("Lolita", "Vladimir Nabokov", "Rs. 349", "3.9", R.drawable.lolita),
        Book("Middlemarch", "George Eliot", "Rs. 599", "4.2", R.drawable.middlemarch),
        Book("The Adventures of Huckleberry Finn", "Mark Twain", "Rs. 699", "4.5", R.drawable.adventures_finn),
        Book("Moby-Dick", "Herman Melville", "Rs. 499", "4.5", R.drawable.moby_dick),
        Book("The Lord of the Rings", "J.R.R Tolkien", "Rs. 749", "5.0", R.drawable.lord_of_rings)
    )*/



    val value: Int = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        setHasOptionsMenu(true)

        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)

        //btnCheckInternet = view.findViewById(R.id.btnCheckInternet)

        progressLayout = view.findViewById(R.id.progressLayout)

        progreressbar = view.findViewById(R.id.progressbar)

        progressLayout.visibility = View.VISIBLE

        /*
        btnCheckInternet.setOnClickListener {
            if (ConnectionManager().checkConnectivity(activity as Context)) {
                // Internet is available
                val dialog = AlertDialog.Builder(activity as Context)
                dialog.setTitle("Success")
                dialog.setMessage("Internet Connection Found")
                dialog.setPositiveButton("Ok"){text, listener ->
                    //Do Nothing
                }
                dialog.setNegativeButton("Cancal") {text, listner ->
                    //Do Nothing
                }
                dialog.create()
                dialog.show()
            }else{
                // Internet is Not Available
                val dialog = AlertDialog.Builder(activity as Context)
                dialog.setTitle("Error")
                dialog.setMessage("Internet Connection is Not Found")
                dialog.setPositiveButton("Ok"){text, listener ->
                    //Do Nothing
                }
                dialog.setNegativeButton("Cancal") {text, listner ->
                    //Do Nothing
                }
                dialog.create()
                dialog.show()

            }
        }*/

        layoutManager = LinearLayoutManager(activity)

        val queue = Volley.newRequestQueue(activity as Context)

        val url = "http://13.235.250.119/v1/book/fetch_books/"
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET,url,null,Response.Listener{

                try {
                    progressLayout.visibility = View.GONE
                    val success = it.getBoolean("success")

                    if (success) {
                        val data = it.getJSONArray("data")
                        for (i in 0 until data.length()){
                            val bookJSONObject = data.getJSONObject(i)
                            val bookObject = Book(
                                bookJSONObject.getString("book_id"),
                                bookJSONObject.getString("name"),
                                bookJSONObject.getString("author"),
                                bookJSONObject.getString("rating"),
                                bookJSONObject.getString("price"),
                                bookJSONObject.getString("image")
                            )
                            bookInfoList.add(bookObject)
                            recyclerAdapter = DashboardRecyclerAdapter(activity as Context, bookInfoList)

                            recyclerDashboard.adapter =recyclerAdapter

                            recyclerDashboard.layoutManager = layoutManager

                            /*
                            recyclerDashboard.addItemDecoration(
                                DividerItemDecoration(
                                    recyclerDashboard.context,
                                    (layoutManager as LinearLayoutManager).orientation
                                )
                            )*/
                        }
                    } else {
                        Toast.makeText(activity as Context, "Some Error Occurred !!!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(activity as Context, "Some Unexpected Error Occured !!!!", Toast.LENGTH_SHORT).show()
                }
            },
                Response.ErrorListener {
                    if (activity != null) {
                        Toast.makeText(activity as Context, "Volley Error Occured !!!!", Toast.LENGTH_SHORT).show()
                    }
                }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "dd7e00638c605c"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        } else {
            // Internet is Not Available
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is Not Found")
            dialog.setPositiveButton("Open Settings"){text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
                //Do Nothing
            }
            dialog.setNegativeButton("Exit") {text, listner ->
                ActivityCompat.finishAffinity(activity as Activity)
                //Do Nothing
            }
            dialog.create()
            dialog.show()
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item?.itemId
        if (id == R.id.action_sort) {
            Collections.sort(bookInfoList, ratingComparator)
            bookInfoList.reverse()
        }

        recyclerAdapter.notifyDataSetChanged()

        return super.onOptionsItemSelected(item)
    }

}