package hm.orz.chaos114.android.tumekyouen.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivity;

public final class NotificationUtil {
    private static final String CHANNEL_ID_DEFAULT = "default_channel";

    private NotificationUtil() {
    }

    public static void setup(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL_ID_DEFAULT,
                context.getString(R.string.channel_name_default),
                NotificationManager.IMPORTANCE_DEFAULT
        ));
    }

    public static void notify(final Context context, final CharSequence title, final CharSequence message) {

        final Intent newIntent = new Intent(context, TitleActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newIntent, 0);

        final Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_DEFAULT)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.icon_notification)
                .setColor(ContextCompat.getColor(context, android.R.color.white))
                .setContentIntent(contentIntent)
                .build();

        NotificationManagerCompat.from(context).notify(
                0,
                notification
        );
    }
}
