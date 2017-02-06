package hm.orz.chaos114.android.tumekyouen.util;

import android.content.Context;
import android.widget.Toast;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.network.NewKyouenService;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * サーバよりデータを取得し、DBに登録するクラス。
 *
 * @author noboru
 */
public class InsertDataTask {

    private static final int FETCH_SIZE = 10;

    // タスク実行中フラグ
    private static boolean running = false;

    // コンテキスト
    private Context mContext;

    // DBアクセスオブジェクト
    private KyouenDb mKyouenDb;

    // 取得する回数
    private int mCount;

    // 処理終了時の処理
    private Runnable mRun;

    private NewKyouenService kyouenService;

    /**
     * コンストラクタ。
     *
     * @param context コンテキスト
     * @param run     処理終了時の処理
     */
    public InsertDataTask(Context context, Runnable run, NewKyouenService kyouenService) {
        this(context, 1, run, kyouenService);
    }

    /**
     * コンストラクタ。
     *
     * @param context コンテキスト
     * @param count   取得する回数
     * @param run     処理終了時の処理
     */
    public InsertDataTask(Context context, int count, Runnable run, NewKyouenService kyouenService) {
        this.mContext = context;
        this.mCount = count;
        this.mRun = run;
        this.kyouenService = kyouenService;

        mKyouenDb = new KyouenDb(context);
    }

    /**
     * タスク実行中フラグを返却します。
     *
     * @return タスク実行中フラグ
     */
    public static synchronized boolean isRunning() {
        return InsertDataTask.running;
    }

    /**
     * タスク実行中フラグを設定します。
     *
     * @param running タスク実行中フラグ
     */
    private static synchronized void setRunning(boolean running) {
        InsertDataTask.running = running;
    }

    public void execute(String params) {
        if (isRunning()) {
            // 実施中の場合は排他エラー
            onPostExecute(-2);
            return;
        }
        setRunning(true);

        int offset = Integer.parseInt(params);
        fetch(offset, 0);
    }

    private void fetch(int offset, int count) {
        kyouenService.getStage(offset, FETCH_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                            if (list.isEmpty()) {
                                onPostExecute(offset);
                                return;
                            }
                            mKyouenDb.insert(list);
                            if (count + 1 >= mCount) {
                                onPostExecute(offset + list.size());
                                return;
                            }
                            fetch(offset + list.size(), count + 1);
                        },
                        throwable -> {
                            Timber.e(throwable, "cannot get stage.");
                            onPostExecute(-1);
                        });
    }

    private void onPostExecute(Integer result) {
        Timber.d("in onPostExecute result is %d", result);
        if (result != -2) {
            // 排他エラー以外の場合はfalseを設定
            setRunning(false);
        }
        if (mRun != null) {
            mRun.run();
        }

        if (result > 0) {
            // 取得できた場合
            Toast.makeText(mContext,
                    mContext.getString(R.string.toast_get_stage, result),
                    Toast.LENGTH_SHORT).show();
        } else if (result == 0) {
            // 取得できなかった場合
            Toast.makeText(mContext,
                    R.string.toast_no_stage,
                    Toast.LENGTH_SHORT).show();
        } else {
            // エラーが発生した場合
            Toast.makeText(mContext,
                    R.string.toast_no_stage,
                    Toast.LENGTH_SHORT).show();
        }
    }
}