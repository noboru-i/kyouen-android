package hm.orz.chaos114.android.tumekyouen.util;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * サーバよりデータを取得し、DBに登録するクラス。
 *
 * @author noboru
 */
public class InsertDataTask {

    // TODO application scopeでsingletonにするなど、staticを消す
    // タスク実行中フラグ
    private static boolean running = false;

    private Context mContext;

    private KyouenDb mKyouenDb;

    // 取得する回数
    private int mCount;

    // 処理終了時の処理
    private Runnable mRun;

    private TumeKyouenService mTumeKyouenService;

    /**
     * コンストラクタ。
     *
     * @param context コンテキスト
     * @param run     処理終了時の処理
     */
    public InsertDataTask(Context context, Runnable run, TumeKyouenService tumeKyouenService) {
        this(context, 1, run, tumeKyouenService);
    }

    /**
     * コンストラクタ。
     *
     * @param context コンテキスト
     * @param count   取得する回数
     * @param run     処理終了時の処理
     */
    public InsertDataTask(Context context, int count, Runnable run, TumeKyouenService tumeKyouenService) {
        this.mContext = context;
        this.mCount = count;
        this.mRun = run;
        this.mTumeKyouenService = tumeKyouenService;

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

        int stageNo = Integer.parseInt(params);
        fetch(stageNo, 0);
    }

    private void fetch(int stageNo, int count) {
        mTumeKyouenService.getStage(stageNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                            String s = null;
                            try {
                                s = response.body().string();
                            } catch (IOException e) {
                                s = "";
                            }
                            if ("no_data".equals(s)) {
                                onPostExecute(stageNo);
                                return;
                            }
                            int addedCount = insertData(s.split("\n"));
                            if (count + 1 >= mCount) {
                                onPostExecute(stageNo + addedCount);
                                return;
                            }
                            fetch(stageNo + addedCount, count + 1);
                        },
                        throwable -> {
                            Timber.e(throwable, "cannot get stage.");
                            onPostExecute(-1);
                        });
    }

    /**
     * DBにデータを登録します。
     *
     * @param insertData 登録データ（CSV文字列の配列）
     * @return 登録件数
     */
    private int insertData(String[] insertData) {
        Timber.d("insertData is %s", Arrays.asList(insertData));
        int count = 0;
        for (String csvString : insertData) {
            long id = mKyouenDb.insert(csvString);
            Timber.d("id is %d", id);
            if (id != -1) {
                count++;
            }
        }

        Timber.d("count is %d", count);
        return count;
    }

    protected void onPostExecute(Integer result) {
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