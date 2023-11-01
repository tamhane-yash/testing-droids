package com.example.daggerexample

import android.util.Log

class EmailService {

    fun send(to: String, from: String, body: String) {
        Log.d("EmailService", "Email sent")
    }
}