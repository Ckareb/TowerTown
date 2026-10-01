package com.example.starairman;

import android.content.Intent;
import android.widget.Button;
import com.example.starairman.ui.game.GameActivity;
import com.example.starairman.ui.base.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getRootViewId() {
        return R.id.main;
    }
    @Override
    protected void onViewReady() {
        Button buttonStartGame = findViewById(R.id.buttonStartGame);
        buttonStartGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });
    }
}
