package hm.orz.chaos114.android.tumekyouen.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivity;

/**
 * ステータスバーに通知するためのユーティリティクラス。
 *
 * @author noboru
 */
public final class NotificationUtil {

    private NotificationUtil() {
    }

    /**
     * 通知を表示する。
     *
     * @param context コンテキスト
     * @param title   タイトル（ex.アプリ名）
     * @param message メッセージ
     */
    public static void notify(final Context context, final CharSequence title, final CharSequence message) {

        // 通知をタップされたときのintentを作成
        final Intent newIntent = new Intent(context, TitleActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, newIntent, 0);

        // Notificationオブジェクトの作成
        final Notification notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.icon_notification)
                .setColor(ContextCompat.getColor(context, android.R.color.white))
                .setContentIntent(contentIntent)
                .build();

        // Managerオブジェクトの取得
        final NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // 通知
        notificationManager.notify(0, notification);
    }
}
