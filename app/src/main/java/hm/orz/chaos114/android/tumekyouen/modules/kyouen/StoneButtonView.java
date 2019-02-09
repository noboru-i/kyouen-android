package hm.orz.chaos114.android.tumekyouen.modules.kyouen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageButton;
import hm.orz.chaos114.android.tumekyouen.R;

public class StoneButtonView extends AppCompatImageButton {
    /**
     * ボタン色を表すenum
     */
    public enum ButtonState {
        NONE, BLACK, WHITE,
    }

    private final Paint paint = new Paint();

    public StoneButtonView(Context context) {
        this(context, null);
    }

    public StoneButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StoneButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int bitmapSize = canvas.getWidth();

        paint.setColor(Color.rgb(128, 128, 128));
        paint.setStrokeWidth(4);

        canvas.drawLine(bitmapSize / 2, 0, bitmapSize / 2, bitmapSize, paint);
        canvas.drawLine(0, bitmapSize / 2, bitmapSize, bitmapSize / 2, paint);

        paint.setColor(Color.rgb(32, 32, 32));
        paint.setStrokeWidth(2);

        canvas.drawLine(bitmapSize / 2, 0, bitmapSize / 2, bitmapSize, paint);
        canvas.drawLine(0, bitmapSize / 2, bitmapSize, bitmapSize / 2, paint);

        super.onDraw(canvas);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(widthSize, widthSize);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, heightMode);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setState(ButtonState state) {
        switch (state) {
            case WHITE:
                setImageResource(R.drawable.circle_white);
                break;
            case BLACK:
                setImageResource(R.drawable.circle_black);
                break;
            case NONE:
                setImageResource(0);
                break;
        }
    }
}
