package com.example.pantrybuddy.ui.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.pantrybuddy.R;

// Custom viewfinder overlay rendering a darkened scrim, rounded cutout window, and sleek corner brackets
public class BarcodeReticleOverlayView extends View {

    private final Paint scrimPaint = new Paint();
    private final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bracketPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF boxRect = new RectF();
    private final float cornerRadius;
    private final float bracketLength;

    public BarcodeReticleOverlayView(Context context) {
        this(context, null);
    }

    public BarcodeReticleOverlayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarcodeReticleOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = getResources().getDisplayMetrics().density;
        cornerRadius = 16f * density;
        bracketLength = 26f * density;
        float bracketStroke = 4f * density;

        scrimPaint.setColor(0x99000000); // 60% semi-transparent black

        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        bracketPaint.setStyle(Paint.Style.STROKE);
        bracketPaint.setStrokeWidth(bracketStroke);
        bracketPaint.setStrokeCap(Paint.Cap.ROUND);
        bracketPaint.setColor(ContextCompat.getColor(context, R.color.colorAccentClay));

        // Offscreen layer required for PorterDuff.Mode.CLEAR
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Center cutout box with responsive dimensions
        float boxSize = Math.min(w * 0.76f, 300f * getResources().getDisplayMetrics().density);
        float left = (w - boxSize) / 2f;
        float top = (h - boxSize) / 2f - (20f * getResources().getDisplayMetrics().density);
        float right = left + boxSize;
        float bottom = top + boxSize;

        boxRect.set(left, top, right, bottom);
    }

    @NonNull
    public RectF getFramingRect() {
        return boxRect;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // 1. Draw darkened scrim covering whole camera preview
        canvas.drawRect(0, 0, getWidth(), getHeight(), scrimPaint);

        // 2. Punch out transparent window in center for viewfinder
        canvas.drawRoundRect(boxRect, cornerRadius, cornerRadius, clearPaint);

        // 3. Draw stylish corner brackets around viewfinder window
        float left = boxRect.left;
        float top = boxRect.top;
        float right = boxRect.right;
        float bottom = boxRect.bottom;

        // Top-left corner
        canvas.drawLine(left, top + bracketLength, left, top + cornerRadius, bracketPaint);
        canvas.drawArc(left, top, left + cornerRadius * 2, top + cornerRadius * 2, 180, 90, false, bracketPaint);
        canvas.drawLine(left + cornerRadius, top, left + bracketLength, top, bracketPaint);

        // Top-right corner
        canvas.drawLine(right - bracketLength, top, right - cornerRadius, top, bracketPaint);
        canvas.drawArc(right - cornerRadius * 2, top, right, top + cornerRadius * 2, 270, 90, false, bracketPaint);
        canvas.drawLine(right, top + cornerRadius, right, top + bracketLength, bracketPaint);

        // Bottom-left corner
        canvas.drawLine(left, bottom - bracketLength, left, bottom - cornerRadius, bracketPaint);
        canvas.drawArc(left, bottom - cornerRadius * 2, left + cornerRadius * 2, bottom, 90, 90, false, bracketPaint);
        canvas.drawLine(left + cornerRadius, bottom, left + bracketLength, bottom, bracketPaint);

        // Bottom-right corner
        canvas.drawLine(right - bracketLength, bottom, right - cornerRadius, bottom, bracketPaint);
        canvas.drawArc(right - cornerRadius * 2, bottom - cornerRadius * 2, right, bottom, 0, 90, false, bracketPaint);
        canvas.drawLine(right, bottom - cornerRadius, right, bottom - bracketLength, bracketPaint);
    }
}
