package hm.orz.chaos114.android.tumekyouen;

import hm.orz.chaos114.android.tumekyouen.model.KyouenData;
import hm.orz.chaos114.android.tumekyouen.model.Line;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 共円描画用のビュー
 * 
 * @author noboru
 */
public class OverlayView extends View {
	/** スクリーンの幅 */
	private int maxScrnWidth;

	/** 盤面のサイズ */
	private int size;

	/** 共円の情報 */
	private KyouenData data;

	/** 描画用オブジェクト */
	private Paint paint;

	public OverlayView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OverlayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		paint = new Paint();
		paint.setColor(Color.rgb(128, 128, 128));
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		this.maxScrnWidth = getWidth();
		if (getWidth() != getHeight()) {
			getLayoutParams().height = getWidth();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (data == null) {
			return;
		}

		double offset = maxScrnWidth / size;
		if (data.isLine()) {
			// 直線の場合
			Line line = data.getLine();
			float startX = 0;
			float startY = 0;
			float stopX = 0;
			float stopY = 0;
			if (line.getA() == 0) {
				// x軸と平行な場合
				startX = 0;
				startY = (float) (line.getY(0) * offset + offset / 2);
				stopX = maxScrnWidth;
				stopY = (float) (line.getY(0) * offset + offset / 2);
			} else if (line.getB() == 0) {
				// y軸と平行な場合
				startX = (float) (line.getX(0) * offset + offset / 2);
				startY = 0;
				stopX = (float) (line.getX(0) * offset + offset / 2);
				stopY = maxScrnWidth;
			} else {
				// 上記以外の場合
				if (-1 * line.getC() / line.getB() > 0) {
					startX = 0;
					startY = (float) (line.getY(-0.5) * offset + offset * 2 / 4);
					stopX = maxScrnWidth;
					stopY = (float) (line.getY(size - 0.5) * offset + offset * 2 / 4);
				} else {
					startX = (float) (line.getX(-0.5) * offset + offset * 2 / 4);
					startY = 0;
					stopX = (float) (line.getX(size - 0.5) * offset + offset * 2 / 4);
					stopY = maxScrnWidth;
				}
			}
			canvas.drawLine(startX, startY, stopX, stopY, paint);
		} else {
			// 円の場合
			float cx = (float) (data.getCenter().getX() * offset + offset / 2);
			float cy = (float) (data.getCenter().getY() * offset + offset / 2);
			float radius = (float) (data.getRadius() * offset);
			canvas.drawCircle(cx, cy, radius, paint);
		}
	}

	/**
	 * 表示するための情報を設定します。
	 * 
	 * @param size 盤面のサイズ
	 * @param data 共円の情報
	 */
	public void setData(int size, KyouenData data) {
		this.size = size;
		this.data = data;
	}
}