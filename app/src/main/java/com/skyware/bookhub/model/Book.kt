package com.skyware.bookhub.model

data class Book(
    val bookId: String,
    val bookName: String,
    val bookAuthor: String,
    //val bookCost: String,
    val bookPrice: String,
    val bookRating: String,
    val bookImage: String
)