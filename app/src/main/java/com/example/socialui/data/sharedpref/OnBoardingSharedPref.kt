package com.example.socialui.data.sharedpref

import android.content.Context
import android.content.Context.MODE_PRIVATE


private const val TAG = "OnBoardingSharedPref"
private val SHARED_PREF = "ON_BOARDING_SHARED_PREF"
private val IS_FIRST_TIME = "IS_FIRST_TIME"

fun Context.setOnBoardingStatus(boolean: Boolean) {
    val sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putBoolean(IS_FIRST_TIME, boolean)
    editor.apply()
}

fun Context.getOnBoardingStatus(): Boolean {
    val sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
    return sharedPreferences.getBoolean(IS_FIRST_TIME, true)
}

