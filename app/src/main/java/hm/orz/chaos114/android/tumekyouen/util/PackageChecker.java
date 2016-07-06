package hm.orz.chaos114.android.tumekyouen.util;

import android.content.Context;
import android.content.pm.PackageManager;

public final class PackageChecker {
    private PackageChecker() {
        // prevent instantiate
    }

    public static boolean check(Context context, String packageName) {
        try {
            // 共円チェッカーの存在有無チェック
            final PackageManager pm = context.getPackageManager();
            pm.getApplicationInfo(packageName, 0);
            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            // 存在しない場合
            return false;
        }
    }
}
