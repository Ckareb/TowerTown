package com.example.starairman.ui.base;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class BaseActivity extends AppCompatActivity {

    @LayoutRes
    protected abstract int getLayoutId();

    protected abstract int getRootViewId();

    protected boolean applyTopInset()    { return true; }
    protected boolean applyBottomInset() { return true; }
    protected boolean applyLeftInset()   { return true; }
    protected boolean applyRightInset()  { return true; }

    @Nullable protected Integer getCustomTopInset()    { return null; }
    @Nullable protected Integer getCustomBottomInset() { return null; }
    @Nullable protected Integer getCustomLeftInset()   { return null; }
    @Nullable protected Integer getCustomRightInset()  { return null; }

    protected void onViewReady() {}

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(getLayoutId());
        applySystemBarInsets();

        onViewReady();
    }

    private void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(getRootViewId()), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int left   = resolve(applyLeftInset(),   getCustomLeftInset(),   systemBars.left);
            int top    = resolve(applyTopInset(),    getCustomTopInset(),    systemBars.top);
            int right  = resolve(applyRightInset(),  getCustomRightInset(),  systemBars.right);
            int bottom = resolve(applyBottomInset(), getCustomBottomInset(), systemBars.bottom);

            v.setPadding(left, top, right, bottom);
            return insets;
        });
    }

    private int resolve(boolean enabled, @Nullable Integer custom, int systemValue) {
        if (!enabled) return 0;
        return custom != null ? custom : systemValue;
    }

}
