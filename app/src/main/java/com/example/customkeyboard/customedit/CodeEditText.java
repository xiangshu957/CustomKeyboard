package com.example.customkeyboard.customedit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.customkeyboard.R;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/2/22
 * DES:
 */
public class CodeEditText extends AppCompatEditText {

    private int mTextColor;
    private int mMaxLength = 7;
    private int mStrokeWidth;
    private int mStrokeHeight;
    private int mStrokePadding = 20;
    private final Rect mRect = new Rect();
    private Drawable mStrokeDrawable;

    public static int count = 0;

    /**
     * 输入结束监听
     */
    private OnTextFinishListener mOnInputFinishListener;
    private OnTextChangeListener onTextChangeListener;

    public CodeEditText(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public CodeEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public CodeEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CodeEditText);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.CodeEditText_strokeHeight) {
                this.mStrokeHeight = (int) typedArray.getDimension(index, 60);
            } else if (index == R.styleable.CodeEditText_strokeWidth) {
                this.mStrokeWidth = (int) typedArray.getDimension(index, 60);
            } else if (index == R.styleable.CodeEditText_strokePadding) {
                this.mStrokePadding = (int) typedArray.getDimension(index, 20);
            } else if (index == R.styleable.CodeEditText_strokeBackground) {
                this.mStrokeDrawable = typedArray.getDrawable(index);
            } else if (index == R.styleable.CodeEditText_strokeLength) {
                this.mMaxLength = typedArray.getInteger(index, 4);
            }
        }
        typedArray.recycle();

        if (mStrokeDrawable == null) {
            throw new NullPointerException("stroke drawable not allowed to be null!");
        }

        setMaxLength(mMaxLength);
        setLongClickable(false);
        // 去掉背景颜色
        setBackgroundColor(Color.TRANSPARENT);
        // 不显示光标
        setCursorVisible(false);

    }

    /**
     * 设置最大长度
     */
    private void setMaxLength(int maxLength) {
        if (maxLength >= 0) {
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        } else {
            setFilters(new InputFilter[0]);
        }
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 当前输入框的宽高信息
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 判断高度是否小于推荐高度
        if (height < mStrokeHeight) {
            height = mStrokeHeight;
        }

        int recommendWidth = mStrokeWidth * mMaxLength + mStrokePadding * (mMaxLength - 1);
        if (width < recommendWidth) {
            width = recommendWidth;
        }

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode);

        // 设置测量布局
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 在画支持设置文本颜色，把系统化的文本透明掉，相当于覆盖
        mTextColor = getCurrentTextColor();
        setTextColor(Color.TRANSPARENT);
        super.onDraw(canvas);
        // 重新设置文本颜色
        setTextColor(mTextColor);
        // 重绘背景颜色
        drawStrokeBackground(canvas);
        // 重绘文本
        drawText(canvas);

    }

    private void drawStrokeBackground(Canvas canvas) {
        //绘制方框背景颜色
        //确定反馈位置
        mRect.left = 0;
        mRect.top = 0;
        mRect.right = mStrokeWidth;
        mRect.bottom = mStrokeHeight;
        //  当前画布保存的状态
        int count = canvas.getSaveCount();
        // 保存画布
        canvas.save();
        for (int i = 0; i < mMaxLength; i++) {
            // 设置位置
            mStrokeDrawable.setBounds(mRect);
            // 设置图像状态
            mStrokeDrawable.setState(new int[]{android.R.attr.state_enabled});
            //  画到画布上
            mStrokeDrawable.draw(canvas);
            //  确定下一个方框的位置
            // X坐标位置
            float dx = mRect.right + mStrokePadding;
            // 保存画布
            canvas.save();
            // [注意细节] 移动画布到下一个位置
            canvas.translate(dx, 0);

        }
        // [注意细节] 把画布还原到画反馈之前的状态，这样就还原到最初位置了
        canvas.restoreToCount(count);
        // 画布归位
        canvas.translate(0, 0);

        // 下面绘制高亮状态的边框
        // 当前高亮的索引
        int activatedIndex = Math.max(0, getEditableText().length());
        mRect.left = mStrokeWidth * activatedIndex + mStrokePadding * activatedIndex;
        mRect.right = mRect.left + mStrokeWidth;
        mStrokeDrawable.setState(new int[]{android.R.attr.state_focused});
        mStrokeDrawable.setBounds(mRect);
        mStrokeDrawable.draw(canvas);
    }

    private void drawText(Canvas canvas) {

        int count = canvas.getSaveCount();
        canvas.translate(0, 0);
        int length = getEditableText().length();
        for (int i = 0; i < length; i++) {
            String text = String.valueOf(getEditableText().charAt(i));
            TextPaint textPaint = getPaint();
            textPaint.setColor(mTextColor);
            //获取文本大小
            textPaint.getTextBounds(text, 0, 1, mRect);
            //计算(x,y)坐标
            int x = mStrokeWidth / 2 + (mStrokeWidth + mStrokePadding) * i - (mRect.centerX());
            int y = canvas.getHeight() / 2 + mRect.height() / 2;
            canvas.drawText(text, x, y, textPaint);
        }
        canvas.restoreToCount(count);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        int textLength = getEditableText().length();
        String key;
        if (textLength < 1) {
            key = "省";
        } else {
            key = "num";
        }
        if (textLength == mMaxLength) {
            hideSoftInput();
            if (mOnInputFinishListener != null) {
                mOnInputFinishListener.onTextFinish(getEditableText().toString(), mMaxLength);
            }
        }
        if (onTextChangeListener != null) {
            onTextChangeListener.onTextChange(key, textLength);
        }
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 设置输入完成监听
     */
    public void setOnTextFinishListener(OnTextFinishListener onInputFinishListener) {
        this.mOnInputFinishListener = onInputFinishListener;
    }

    public interface OnTextFinishListener {

        void onTextFinish(CharSequence text, int length);
    }


    public void setOnTextChangeListener(OnTextChangeListener onTextChangeListener) {
        this.onTextChangeListener = onTextChangeListener;
    }

    public interface OnTextChangeListener {
        void onTextChange(String key, int length);
    }

}
