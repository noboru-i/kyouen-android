package hm.orz.chaos114.android.tumekyouen.util;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * サーバよりデータを取得し、DBに登録するクラス。
 * 
 * @author noboru
 */
public class InsertDataTask extends AsyncTask<String, Integer, Integer> {

	/** タスク実行中フラグ */
	private static boolean running = false;

	/** コンテキスト */
	private Context mContext;

	/** DBアクセスオブジェクト */
	private KyouenDb mKyouenDb;

	/** 取得する回数 */
	private int mCount;

	/** 処理終了時の処理 */
	private Runnable mRun;

	/**
	 * コンストラクタ。
	 * 
	 * @param context コンテキスト
	 * @param run 処理終了時の処理
	 */
	public InsertDataTask(Context context, Runnable run) {
		this(context, 1, run);
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param context コンテキスト
	 * @param count 取得する回数
	 * @param run 処理終了時の処理
	 */
	public InsertDataTask(Context context, int count, Runnable run) {
		this.mContext = context;
		this.mCount = count;
		this.mRun = run;

		mKyouenDb = new KyouenDb(context);
	}

	@Override
	protected Integer doInBackground(String... params) {
		if (isRunning()) {
			// 実施中の場合は排他エラー
			return -2;
		}
		setRunning(true);

		// データの取得
		int addedCount = 0;
		int stageNo = Integer.parseInt(params[0]);
		for (int i = 0; i < mCount; i++) {
			int c = process(stageNo + addedCount);
			switch (c) {
			case 0:
				// 取得件数が0の場合は処理中断
				return addedCount;
			case -1:
				// エラーが返却された場合
				return -1;
			default:
				// 取得件数を追加して処理続行
				addedCount += c;
			}
		}

		// 最終的な処理件数を返却
		return addedCount;
	}

	/**
	 * 追加ステージを取得します。
	 * 
	 * @param currentMaxStageNo 現在の最大ステージ番号
	 * @return 取得件数（エラーの場合は"-1"）
	 */
	private int process(int currentMaxStageNo) {
		String data = getStageData(currentMaxStageNo);
		if (data == null) {
			// 例外発生時
			return -1;
		} else if ("no_data".equals(data)) {
			// データ無しの場合
			return 0;
		}

		// データの登録
		int count = insertData(data.split("\n"));
		return count;
	}

	/**
	 * サーバよりステージを取得します。
	 * 
	 * @param currentMaxStageNo 現在の最大ステージ番号
	 * @return 取得データ（改行区切り。取得出来なかった場合は"no_data"。例外発生時はnull）
	 */
	private String getStageData(int currentMaxStageNo) {
		// URLの作成
		String url = mContext.getString(R.string.server_url) + "/kyouen/get?stageNo="
				+ currentMaxStageNo;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			String ret = EntityUtils.toString(response.getEntity());

			return ret;
		} catch (IOException e) {
			return null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * DBにデータを登録します。
	 * 
	 * @param insertData 登録データ（CSV文字列の配列）
	 * @return 登録件数
	 */
	private int insertData(String[] insertData) {
		int count = 0;
		for (String csvString : insertData) {
			long id = mKyouenDb.insert(csvString);
			if (id != -1) {
				count++;
			}
		}

		return count;
	}

	@Override
	protected void onPostExecute(Integer result) {
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
					mContext.getString(R.string.toast_no_stage, result),
					Toast.LENGTH_SHORT).show();
		} else {
			// エラーが発生した場合
			Toast.makeText(mContext,
					mContext.getString(R.string.toast_no_stage, result),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * タスク実行中フラグを設定します。
	 * 
	 * @param running タスク実行中フラグ
	 */
	private static synchronized void setRunning(boolean running) {
		InsertDataTask.running = running;
	}

	/**
	 * タスク実行中フラグを返却します。
	 * 
	 * @return タスク実行中フラグ
	 */
	public static synchronized boolean isRunning() {
		return InsertDataTask.running;
	}
}