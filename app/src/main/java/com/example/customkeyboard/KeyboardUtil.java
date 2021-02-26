package com.example.customkeyboard;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/2/25
 * DES:
 */
public class KeyboardUtil {

    private final Activity mActivity;
    private final KeyboardView mKeyboardView;
    private final EditText mEdit;

    private final Keyboard provinceKeyboard;

    private final Keyboard numberKeyboard;

    public KeyboardUtil(Activity mActivity, EditText mEdit) {
        this.mActivity = mActivity;
        this.mEdit = mEdit;

        provinceKeyboard = new Keyboard(mActivity, R.xml.province_abbreviation);
        numberKeyboard = new Keyboard(mActivity, R.xml.number_or_letters);
        mKeyboardView = mActivity.findViewById(R.id.keyboard_view);
        mKeyboardView.setKeyboard(provinceKeyboard);
        mKeyboardView.setEnabled(true);
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setOnKeyboardActionListener(listener);

    }

    private final KeyboardView.OnKeyboardActionListener listener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onPress(int primaryCode) {

        }

        @Override
        public void onRelease(int primaryCode) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {

            Editable editable = mEdit.getText();
            int start = mEdit.getSelectionStart();
            //判定是否是中文的正则表达式 [\\u4e00-\\u9fa5]判断一个中文 [\\u4e00-\\u9fa5]+多个中文
            String reg = "[\\u4e00-\\u9fa5]";
            if (primaryCode == -1) {// 省份简称与数字键盘切换
                if (mEdit.getText().toString().matches(reg)) {
                    changeKeyboard(true);
                }
            } else if (primaryCode == -3) {
                if (editable != null && editable.length() > 0) {
                    //没有输入内容时软键盘重置为省份简称软键盘
                    if (editable.length() == 1) {
                        changeKeyboard(false);
                    }
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == -4) {
                hideKeyboard();
            } else {
                editable.insert(start, Character.toString((char) primaryCode));
                // 判断第一个字符是否是中文,是，则自动切换到数字软键盘
                if (mEdit.getText().toString().matches(reg)) {
                    changeKeyboard(true);
                }
            }

        }

        @Override
        public void onText(CharSequence text) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    };

    /**
     * 指定切换软键盘 isNumber false表示要切换为省份简称软键盘 true表示要切换为数字软键盘
     */
    private void changeKeyboard(boolean isNumber) {
        if (isNumber) {
            mKeyboardView.setKeyboard(numberKeyboard);
        } else {
            mKeyboardView.setKeyboard(provinceKeyboard);
        }
    }

    /**
     * 软键盘展示状态
     */
    public boolean isShow() {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    /**
     * 软键盘展示
     */
    public void showKeyboard() {
        int visibility = mKeyboardView.getVisibility();
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mKeyboardView.getWidth(), 0);
        translateAnimation.setDuration(500);
        mKeyboardView.setAnimation(translateAnimation);
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            mKeyboardView.setVisibility(View.VISIBLE);
            mKeyboardView.setAlpha(1);
        }
    }

    /**
     * 软键盘隐藏
     */
    public void hideKeyboard() {
        int visibility = mKeyboardView.getVisibility();
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mKeyboardView.getWidth());
        translateAnimation.setDuration(500);
        mKeyboardView.setAnimation(translateAnimation);
        if (visibility == View.VISIBLE) {
            mKeyboardView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 禁掉系统软键盘
     */
    public void hideSoftInputMethod() {
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        String methodName = null;
        if (currentVersion >= 16) {
            // 4.2
            methodName = "setShowSoftInputOnFocus";
        } else if (currentVersion >= 14) {
            // 4.0
            methodName = "setSoftInputShownOnFocus";
        }
        if (methodName == null) {
            mEdit.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            try {
                setShowSoftInputOnFocus = cls.getMethod(methodName, boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(mEdit, false);
            } catch (NoSuchMethodException e) {
                mEdit.setInputType(InputType.TYPE_NULL);
                e.printStackTrace();
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

}
