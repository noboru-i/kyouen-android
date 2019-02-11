package hm.orz.chaos114.android.tumekyouen.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivity

object NotificationUtil {
    private val CHANNEL_ID_DEFAULT = "default_channel"

    fun setup(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(
                CHANNEL_ID_DEFAULT,
                context.getString(R.string.channel_name_default),
                NotificationManager.IMPORTANCE_DEFAULT
        ))
    }

    fun notify(context: Context, title: CharSequence, message: CharSequence) {

        val newIntent = Intent(context, TitleActivity::class.java)
        newIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val contentIntent = PendingIntent.getActivity(context, 0, newIntent, 0)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DEFAULT)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.icon_notification)
                .setColor(ContextCompat.getColor(context, android.R.color.white))
                .setContentIntent(contentIntent)
                .build()

        NotificationManagerCompat.from(context).notify(
                0,
                notification
        )
    }
}
