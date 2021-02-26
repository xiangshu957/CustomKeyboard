package com.example.customkeyboard;

import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customkeyboard.customedit.CodeEditText;

public class MainActivity extends AppCompatActivity {

    private CodeEditText etId;
    private KeyboardView keyboardView;


    private KeyboardUtil keyboardUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        etId = (CodeEditText) findViewById(R.id.et_id);
        keyboardView = (KeyboardView) findViewById(R.id.keyboard_view);

        etId.setOnTouchListener(onTouchListener);
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (keyboardUtil == null) {
                    keyboardUtil = new KeyboardUtil(MainActivity.this, etId);
                    keyboardUtil.hideSoftInputMethod();
                }
                keyboardUtil.showKeyboard();
            }
            return false;
        }
    };
}