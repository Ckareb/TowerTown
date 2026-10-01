package com.example.starairman.ui.game;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.starairman.data.model.Enemy;

import java.util.Collections;
import java.util.List;

public class GameFieldView extends View {
    private Bitmap trailBitmap;
    private Canvas trailCanvas;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public interface OnFieldTouchListener {
        void onFieldTouch(float x, float y);
    }

    private OnFieldTouchListener touchListener;

    public void setOnFieldTouchListener(OnFieldTouchListener touchListener) {
        this.touchListener = touchListener;
    }

    private List<Enemy> enemies = Collections.emptyList();

    public GameFieldView(Context context) {
        super(context);
    }

    public GameFieldView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GameFieldView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies != null ? enemies : Collections.emptyList();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            trailBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            trailCanvas = new Canvas(trailBitmap);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (trailBitmap == null) return;

        paint.setColor(isNightMode() ? Color.WHITE : Color.BLACK);

        for (Enemy e : enemies) {
            trailCanvas.drawRect(e.getX(), e.getY(), e.getX() + e.getSize(), e.getY() + e.getSize(), paint);
        }

        canvas.drawBitmap(trailBitmap, 0, 0, null);

        paint.setColor(Color.RED);
        for (Enemy e : enemies) {
            canvas.drawRect(e.getX(), e.getY(), e.getX() + e.getSize(), e.getY() + e.getSize(), paint);
        }
    }

    private boolean isNightMode() {
        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (touchListener != null) {
                touchListener.onFieldTouch(event.getX(), event.getY());
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
