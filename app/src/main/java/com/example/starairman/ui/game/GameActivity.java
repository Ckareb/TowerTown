package com.example.starairman.ui.game;


import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.example.starairman.R;
import com.example.starairman.data.model.TownPosition;
import com.example.starairman.ui.base.BaseActivity;

public class GameActivity extends BaseActivity {

    private GameViewModel viewModel;

    private FrameLayout gameRoot;

    private FrameLayout container;

    private TextView textTownLives;

    private GameFieldView gameField;

    private boolean gameLoopRunning;
    private long lastFrameNanos;

    private boolean navigatedToGameOver;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_game;
    }

    @Override
    protected int getRootViewId() {
        return R.id.gameRoot;
    }

    @Override
    protected void onViewReady() {

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        gameRoot = findViewById(R.id.gameRoot);
        container = findViewById(R.id.townContainer);
        textTownLives = findViewById(R.id.textTownLives);
        gameField = findViewById(R.id.gameField);

        observeTownPosition();
        observeTownLives();
        triggerFirstRollIfNeeded();
        scheduleMetricsUpdate();

        gameField.setOnFieldTouchListener((x, y) -> {
            if (viewModel.tryDestroyEnemyAt(x, y)) {
                gameField.invalidate();
            }
        });
    }

    private void observeTownPosition() {
        viewModel.getTownPosition().observe(this, position -> {
            if (position == null) return;
            gameRoot.post(() -> moveTownTo(position));
        });
    }

    private void triggerFirstRollIfNeeded() {
        if (viewModel.getTownPosition().getValue() == null) {
            gameRoot.post(viewModel::rollNewPosition);
        }
    }

    private void moveTownTo(TownPosition position) {
        int usableWidth  = gameRoot.getWidth()  - gameRoot.getPaddingLeft() - gameRoot.getPaddingRight();
        int usableHeight = gameRoot.getHeight() - gameRoot.getPaddingTop()  - gameRoot.getPaddingBottom();

        int maxX = usableWidth  - container.getWidth();
        int maxY = usableHeight - container.getHeight();

        if (maxX < 0) maxX = 0;
        if (maxY < 0) maxY = 0;

        float dx = position.getX() * maxX;
        float dy = position.getY() * maxY;

        container.setTranslationX(dx);
        container.setTranslationY(dy);
    }

    private void observeTownLives() {
        viewModel.getTownLives().observe(this, lives -> {
            if (lives == null) return;
            textTownLives.setText(String.valueOf(lives));
            if (lives <= 0 && !navigatedToGameOver) {
                navigatedToGameOver = true;
                gameRoot.post(this::goToGameOver);
            }
        });
    }

    private void goToGameOver() {
        startActivity(new Intent(this, GameOverActivity.class));
        finish();
    }

    private void scheduleMetricsUpdate() {
        gameField.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (right - left <= 0 || bottom - top <= 0) return;
                viewModel.setFieldMetrics(
                        right - left,
                        bottom - top,
                        container.getWidth(),
                        getResources().getDisplayMetrics().density
                );
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        gameLoopRunning = true;
        lastFrameNanos = 0L;
        gameField.postOnAnimation(frameRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameLoopRunning = false;
    }

    private final Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            if (!gameLoopRunning) return;

            long now = System.nanoTime();
            if (lastFrameNanos != 0L) {
                float dt = (now - lastFrameNanos) / 1_000_000_000f;
                if (dt > 0.1f) dt = 0.1f;

                viewModel.tick(dt);
                gameField.setEnemies(viewModel.getEnemies());
            }
            lastFrameNanos = now;

            gameField.postOnAnimation(this);
        }
    };
}
