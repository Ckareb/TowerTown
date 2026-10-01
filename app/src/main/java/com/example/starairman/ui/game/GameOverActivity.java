package com.example.starairman.ui.game;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.example.starairman.R;
import com.example.starairman.ui.base.BaseActivity;
import com.example.starairman.MainActivity;

public class GameOverActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_game_over;
    }

    @Override
    protected int getRootViewId() {
        return R.id.gameOverRoot;
    }

    @Override
    protected void onViewReady() {
        Button backToMenu = findViewById(R.id.buttonBackToMenu);

        TextView textResultPoint = findViewById(R.id.textResultPoint);

        textResultPoint.setText(String.valueOf(GameViewModel.getPointScore()));

        backToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            // Очищаем стек: возвращаемся на главный и не оставляем GameOver/Game в стеке
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}