package hm.orz.chaos114.android.tumekyouen.modules.kyouen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import hm.orz.chaos114.android.tumekyouen.model.KyouenData;
import hm.orz.chaos114.android.tumekyouen.model.Line;

/**
 * 共円描画用のビュー
 *
 * @author noboru
 */
public class OverlayView extends View {
    // スクリーンの幅
    private int maxScrnWidth;

    // 盤面のサイズ
    private int size;

    // 共円の情報
    private KyouenData data;

    // 描画用オブジェクト
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

        // ディスプレイサイズの取得
        final Display display = ((WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        maxScrnWidth = displaySize.x;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = (int) (widthSize);

        setMeasuredDimension(widthSize, heightSize);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    @SuppressWarnings("SuspiciousNameCombination")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null) {
            return;
        }

        double offset = maxScrnWidth / size;
        if (data.lineKyouen()) {
            // 直線の場合
            Line line = data.line();
            float startX;
            float startY;
            float stopX;
            float stopY;
            if (line.a() == 0) {
                // x軸と平行な場合
                startX = 0;
                startY = (float) (line.getY(0) * offset + offset / 2);
                stopX = maxScrnWidth;
                stopY = (float) (line.getY(0) * offset + offset / 2);
            } else if (line.b() == 0) {
                // y軸と平行な場合
                startX = (float) (line.getX(0) * offset + offset / 2);
                startY = 0;
                stopX = (float) (line.getX(0) * offset + offset / 2);
                stopY = maxScrnWidth;
            } else {
                // 上記以外の場合
                if (-1 * line.c() / line.b() > 0) {
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
            float cx = (float) (data.center().x() * offset + offset / 2);
            float cy = (float) (data.center().y() * offset + offset / 2);
            float radius = (float) (data.radius() * offset);
            canvas.drawCircle(cx, cy, radius, paint);
        }
    }

    /**
     * 表示するための情報を設定します。
     *
     * @param aSize 盤面のサイズ
     * @param aData 共円の情報
     */
    void setData(int aSize, KyouenData aData) {
        this.size = aSize;
        this.data = aData;
    }
}