package com.example.mygmap

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import com.example.mygmap.ui.MapsActivity
import java.lang.StringBuilder
import java.text.DateFormat
import java.util.*

class LocationResultHelper(
    private val mContext: Context,
    private val mLocationList: List<Location>
) {

    fun getLocationResultText(): String {
        return if (mLocationList.isEmpty()) {
            "LocationNot Received"
        } else {
            val sb = StringBuilder()
            for (location in mLocationList) {
                sb.apply {
                    append("(")
                    append(location.latitude)
                    append(", ")
                    append(location.longitude)
                    append(")")
                    append("\n")
                }
            }
            sb.toString()
        }
    }

    fun getLocationResultTitle(): CharSequence {
        val result = mContext.resources.getQuantityString(
            R.plurals.num_locations_reported,
            mLocationList.size,
            mLocationList.size
        )
        return result + " : " + DateFormat.getDateTimeInstance().format(Date())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showNotification() {
        val notificationIntent = Intent(mContext, MapsActivity::class.java)

        // Construct a task stack
        val stackBuilder = TaskStackBuilder.create(mContext)

        // Add mainActivity to the task stack as the parent
        stackBuilder.addParentStack(MapsActivity::class.java)

        //Push the content intent onto the stack
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack
        /*
        -> Targeting S+ (version 31 and above) requires that one of FLAG_IMMUTABLE or FLAG_MUTABLE be specified when creating a PendingIntent
        -> Strongly consider using FLAG_IMMUTABLE, only use FLAG_MUTABLE if some functionality depends on the PendingIntent being mutable,
        e.g. if it needs to be used with inline replies or bubbles
         */
        val notificationPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = Notification.Builder(mContext, CHANNEL_ID)
            .setContentTitle(getLocationResultTitle())
            .setContentText(getLocationResultText())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(notificationPendingIntent)

        val notificationManager =
            mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())

    }

    @SuppressLint("CommitPrefEdits")
    fun saveLocationResult() {
        PreferenceManager.getDefaultSharedPreferences(mContext)
            .edit()
            .putString(
                KEY_LOCATION_RESULTS,
                getLocationResultTitle().toString() + "\n" + getLocationResultText()
            )
            .apply()
    }

    companion object{
        fun getSavedLocationResult(context: Context): String? {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_RESULTS, "Default Value")
        }
        fun getLocationRequestStatus(context: Context) : Boolean{
           return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_REQUEST, false)
        }
        fun setLocationRequestStatus(context: Context, value: Boolean){
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_REQUEST, value)
                .apply()
        }
    }

}