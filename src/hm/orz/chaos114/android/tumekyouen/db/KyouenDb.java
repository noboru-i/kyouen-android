package hm.orz.chaos114.android.tumekyouen.db;

import java.util.Date;

import hm.orz.chaos114.android.tumekyouen.model.StageCountModel;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * 詰め共円情報テーブルを管理するクラス
 * 
 * @author noboru
 */
public class KyouenDb {

	/** テーブル名 */
	private static final String TABLE_NAME = "tume_kyouen";

	/** カラム名 */
	public interface TumeKyouenDataColumns extends BaseColumns {
		/** ステージ番号 */
		String STAGE_NO = "stage_no";

		/** サイズ */
		String SIZE = "size";

		/** ステージ上の石の配置 */
		String STAGE = "stage";

		/** 作者 */
		String CREATOR = "creator";

		/** クリアフラグ */
		String CLEAR_FLAG = "clear_flag";
		
		/** クリア日付 */
		String CLEAR_DATE = "clear_date";
	}

	/** ヘルパークラス */
	private SQLiteOpenHelper mHelper;

	/**
	 * パズル情報DBのヘルパークラス
	 * 
	 * @author noboru
	 */
	class IroKaeDbOpenHelper extends SQLiteOpenHelper {

		/** DBのバージョン */
		private static final int DB_VERSION = 2;

		/** DB名 */
		private static final String DB_NAME = "irokae.db";

		private IroKaeDbOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			StringBuilder sql = new StringBuilder();
			sql.append("CREATE TABLE IF NOT EXISTS ");
			sql.append(TABLE_NAME);
			sql.append("(");
			sql.append(BaseColumns._ID);
			sql.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
			sql.append(TumeKyouenDataColumns.STAGE_NO);
			sql.append(" INTEGER NOT NULL UNIQUE, ");
			sql.append(TumeKyouenDataColumns.SIZE);
			sql.append(" INTEGER NOT NULL, ");
			sql.append(TumeKyouenDataColumns.STAGE);
			sql.append(" TEXT, ");
			sql.append(TumeKyouenDataColumns.CREATOR);
			sql.append(" TEXT, ");
			sql.append(TumeKyouenDataColumns.CLEAR_FLAG);
			sql.append(" INTEGER DEFAULT 0, ");
			sql.append(TumeKyouenDataColumns.CLEAR_DATE);
			sql.append(" INTEGER DEFAULT 0 ");
			sql.append(");");
			db.execSQL(sql.toString());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion == 1 && newVersion == 2) {
				StringBuilder sql = new StringBuilder();
				sql.append("ALTER TABLE ");
				sql.append(TABLE_NAME);
				sql.append(" ADD COLUMN ");
				sql.append(TumeKyouenDataColumns.CLEAR_DATE);
				sql.append(";");
				db.execSQL(sql.toString());
			}
		}
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param context コンテキスト
	 */
	public KyouenDb(Context context) {
		mHelper = new IroKaeDbOpenHelper(context);
	}

	/**
	 * データベースのオープンを試みます。
	 */
	public void check() {
		mHelper.getWritableDatabase();
		mHelper.close();
	}

	/**
	 * レコードを登録します。
	 * 
	 * @param csvString CSV文字列
	 * @return ID
	 */
	public long insert(String csvString) {
		SQLiteDatabase database = null;
		try {
			database = mHelper.getWritableDatabase();
			// レコード登録
			long id = insert(database, csvString);

			return id;
		} finally {
			if (database != null) {
				database.close();
			}
		}
	}

	/**
	 * レコードを登録します。
	 * 
	 * @param csvString CSV文字列
	 * @return ID
	 */
	public long insert(SQLiteDatabase db, String csvString) {
		String[] splitString = csvString.split(",", -1);
		if (splitString.length != 4) {
			return -1;
		}
		int i = 0;
		ContentValues values = new ContentValues();
		values.put(TumeKyouenDataColumns.STAGE_NO, splitString[i++]);
		values.put(TumeKyouenDataColumns.SIZE, splitString[i++]);
		values.put(TumeKyouenDataColumns.STAGE, splitString[i++]);
		values.put(TumeKyouenDataColumns.CREATOR, splitString[i++]);

		// レコード登録
		long id = db.insert(TABLE_NAME, "", values);

		return id;
	}

	/**
	 * クリアフラグを更新する。
	 * 
	 * @param item 更新オブジェクト
	 * @return 更新件数
	 */
	public int updateClearFlag(TumeKyouenModel item) {

		ContentValues values = new ContentValues();
		values.put(TumeKyouenDataColumns.CLEAR_FLAG, item.getClearFlag());
		if (item.getClearFlag() == TumeKyouenModel.CLEAR) {
			values.put(TumeKyouenDataColumns.CLEAR_DATE, new Date().getTime());
		}

		SQLiteDatabase database = null;
		try {
			database = mHelper.getWritableDatabase();

			// レコード更新
			int count = database.update(TABLE_NAME, values,
					TumeKyouenDataColumns.STAGE_NO + " = ?",
					new String[] { Integer.toString(item.getStageNo()) });

			return count;
		} finally {
			if (database != null) {
				database.close();
			}
		}
	}

	/**
	 * 次のステージ情報を検索する。
	 * 
	 * @param stageNo 現在のステージ番号
	 * @return ステージ情報
	 */
	public TumeKyouenModel selectNextStage(int stageNo) {
		return selectCurrentStage(stageNo + 1);
	}

	/**
	 * 現在のステージ情報を検索する。
	 * 
	 * @param stageNo 現在のステージ番号
	 * @return ステージ情報
	 */
	public TumeKyouenModel selectCurrentStage(int stageNo) {
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = mHelper.getReadableDatabase();

			// レコード取得
			cursor = database.query(TABLE_NAME, null,
					TumeKyouenDataColumns.STAGE_NO + " = ?",
					new String[] { String.valueOf(stageNo) }, null, null, null,
					null);

			TumeKyouenModel model = null;
			if (cursor.moveToNext()) {
				model = cursorToTumeKyouenModel(cursor);
			}

			return model;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (database != null) {
				database.close();
			}
		}
	}

	/**
	 * 前のステージ情報を検索する。
	 * 
	 * @param stageNo 現在のステージ番号
	 * @return ステージ情報
	 */
	public TumeKyouenModel selectPrevStage(int stageNo) {
		return selectCurrentStage(stageNo - 1);
	}

	/**
	 * CursorよりTumeKyouenModelを取得します。
	 * 
	 * @param cursor Cursorオブジェクト
	 * @return TumeKyouenModelオブジェクト
	 */
	private TumeKyouenModel cursorToTumeKyouenModel(Cursor cursor) {
		TumeKyouenModel item = new TumeKyouenModel();
		int stageNo = cursor.getInt(cursor
				.getColumnIndex(KyouenDb.TumeKyouenDataColumns.STAGE_NO));
		item.setStageNo(stageNo);
		int size = cursor.getInt(cursor
				.getColumnIndex(KyouenDb.TumeKyouenDataColumns.SIZE));
		item.setSize(size);
		String stage = cursor.getString(cursor
				.getColumnIndex(KyouenDb.TumeKyouenDataColumns.STAGE));
		item.setStage(stage);
		String creator = cursor.getString(cursor
				.getColumnIndex(KyouenDb.TumeKyouenDataColumns.CREATOR));
		item.setCreator(creator);
		int clearFlag = cursor.getInt(cursor
				.getColumnIndex(KyouenDb.TumeKyouenDataColumns.CLEAR_FLAG));
		item.setClearFlag(clearFlag);

		return item;
	}

	/** 最大ステージ番号を取得するSQL */
	private static final String SQL_SELECT_MAX_STAGE_NO = "SELECT MAX("
			+ TumeKyouenDataColumns.STAGE_NO + ") " + " FROM " + TABLE_NAME;

	/**
	 * 最大のステージ番号を取得する。
	 * 
	 * @return ステージ番号
	 */
	public long selectMaxStageNo() {
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = mHelper.getReadableDatabase();

			// レコード取得
			cursor = database.rawQuery(SQL_SELECT_MAX_STAGE_NO, null);

			long maxStageNo = 0;
			if (cursor.moveToNext()) {
				maxStageNo = cursor.getLong(0);
			}

			return maxStageNo;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (database != null) {
				database.close();
			}
		}
	}

	/** ステージ数を取得するSQL */
	private static final String SQL_SELECT_STAGE_COUNT = "SELECT "
			+ " COUNT(*) AS COUNT, " + " SUM("
			+ TumeKyouenDataColumns.CLEAR_FLAG + ") AS CLEAR_COUNT " + " FROM "
			+ TABLE_NAME;

	/**
	 * ステージ数を取得する。
	 * 
	 * @return ステージ数
	 */
	public StageCountModel selectStageCount() {
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = mHelper.getReadableDatabase();

			// レコード取得
			cursor = database.rawQuery(SQL_SELECT_STAGE_COUNT, null);

			StageCountModel model = new StageCountModel();
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				model.setStageCount(count);
				int clearCount = cursor.getInt(1);
				model.setClearStageCount(clearCount);
			}

			return model;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (database != null) {
				database.close();
			}
		}
	}
}