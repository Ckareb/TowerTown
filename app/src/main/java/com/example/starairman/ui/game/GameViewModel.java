package com.example.starairman.ui.game;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.starairman.data.model.Enemy;
import com.example.starairman.data.model.TownPosition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameViewModel extends ViewModel {

    private static final float ENEMY_SPEED_DP_PER_SEC = 180f;
    private static final float ENEMY_SIZE_DP = 40f;
    private static final float SPAWN_INTERVAL_SEC = 0.5f;

    private static int POINT_SCORE = 0;

    private static final int DAMAGE = 100;

    private final Random random = new Random();

    private final MutableLiveData<TownPosition> townPosition = new MutableLiveData<>();

    private final MutableLiveData<Integer> townLives = new MutableLiveData<>(TownPosition.getHealthPoints());

    private final List<Enemy> enemies = new ArrayList<>();

    // Метрики поля — задаются Activity после первого layout
    private int fieldWidth;
    private int fieldHeight;
    private int diamondSizePx;
    private float density;

    private float spawnTimer;

    public static int getPointScore() {
        return POINT_SCORE;
    }

    public LiveData<Integer> getTownLives() {
        return townLives;
    }

    public LiveData<TownPosition> getTownPosition() {
        return townPosition;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void changeTownLives(int lives) {
        townLives.setValue(TownPosition.getHealthPoints() - lives);
    }

    /**
     * Генерирует новую случайную позицию в диапазоне [0..1].
     * Логика чистая: никаких пикселей, никакого Context, никакого View.
     */
    public void rollNewPosition() {
        float x = random.nextFloat();
        float y = random.nextFloat();
        townPosition.setValue(new TownPosition(x, y));
    }

    /** Вызывается Activity после того, как View измерились. */
    public void setFieldMetrics(int fieldWidth, int fieldHeight, int diamondSizePx, float density) {
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        this.diamondSizePx = diamondSizePx;
        this.density = density;
    }

    /** Один кадр. dt — секунды с прошлого кадра. */
    public void tick(float dt) {
        if (fieldWidth == 0 || fieldHeight == 0) return;

        // Игра окончена — не двигаем и не спавним
        Integer lives = townLives.getValue();
        if (lives == null || lives <= 0) return;

        moveEnemies(dt);
        checkCollisions();

        spawnTimer -= dt;
        if (spawnTimer <= 0f) {
            spawnEnemy();
            spawnTimer = SPAWN_INTERVAL_SEC;
        }
    }

    // ---------- Движение ----------

    private void moveEnemies(float dt) {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            e.setX(e.getX() + (e.getVx() * dt));
            e.setY(e.getY() + e.getVy() * dt);

            // Убираем тех, кто далеко ушёл за экран (например, ромб увернулся)
            float margin = e.getSize() * 3f;
            if (e.getX() > fieldWidth + margin || e.getX() < -margin
                    || e.getY() > fieldHeight + margin || e.getY() < -margin) {
                it.remove();
            }
        }
    }

    // ---------- Коллизии ----------

    private void checkCollisions() {
        float[] diamond = getDiamondBoundsPx();
        if (diamond == null) return;

        float dLeft = diamond[0], dTop = diamond[1];
        float dRight = diamond[2], dBottom = diamond[3];

        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            boolean overlaps = e.getX() + e.getSize() > dLeft && e.getX() < dRight
                    && e.getY() + e.getSize() > dTop  && e.getY() < dBottom;
            if (overlaps) {
                it.remove();
                decreaseLives(e.getDamage());
            }
        }
    }

    private void decreaseLives(int damage) {
        Integer lives = townLives.getValue();
        if (lives == null) return;
        int next = Math.max(0, lives - damage);
        townLives.setValue(next);
    }

    /** Возвращает [left, top, right, bottom] ромба в пикселях или null, если позиция ещё не готова. */
    private float[] getDiamondBoundsPx() {
        TownPosition pos = townPosition.getValue();
        if (pos == null) return null;

        float left = pos.getX() * (fieldWidth - diamondSizePx);
        float top  = pos.getY() * (fieldHeight - diamondSizePx);
        return new float[]{ left, top, left + diamondSizePx, top + diamondSizePx };
    }

    // ---------- Спавн ----------

    private void spawnEnemy() {
        float[] diamond = getDiamondBoundsPx();
        if (diamond == null) return;

        float size = ENEMY_SIZE_DP * density;
        float safeZone = diamondSizePx * 2f;

        // Какие границы "свободны" — рядом с которыми нет ромба
        List<Integer> freeEdges = new ArrayList<>(4);
        if (diamond[0] > safeZone)                                 freeEdges.add(0); // левая
        if (diamond[2] < fieldWidth - safeZone)                    freeEdges.add(1); // правая
        if (diamond[1] > safeZone)                                 freeEdges.add(2); // верхняя
        if (diamond[3] < fieldHeight - safeZone)                   freeEdges.add(3); // нижняя
        if (freeEdges.isEmpty()) freeEdges.add(0); // страховка

        int edge = freeEdges.get(random.nextInt(freeEdges.size()));

        float x, y;
        switch (edge) {
            case 0: // слева
                x = -size;
                y = random.nextFloat() * fieldHeight;
                break;
            case 1: // справа
                x = fieldWidth + size;
                y = random.nextFloat() * fieldHeight;
                break;
            case 2: // сверху
                x = random.nextFloat() * fieldWidth;
                y = -size;
                break;
            default: // снизу
                x = random.nextFloat() * fieldWidth;
                y = fieldHeight + size;
                break;
        }

        // Целимся в центр ромба на момент спавна
        float targetX = diamond[0] + diamondSizePx / 2f;
        float targetY = diamond[1] + diamondSizePx / 2f;
        float enemyCenterX = x + size / 2f;
        float enemyCenterY = y + size / 2f;

        float dx = targetX - enemyCenterX;
        float dy = targetY - enemyCenterY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) len = 1f;

        float speed = ENEMY_SPEED_DP_PER_SEC * density;
        float vx = dx / len * speed;
        float vy = dy / len * speed;

        enemies.add(new Enemy(x, y, size, vx, vy, DAMAGE));
    }

    /**
     * Пытается уничтожить врага в точке (px, py).
     * Возвращает true, если попал.
     */
    public boolean tryDestroyEnemyAt(float px, float py) {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            boolean inside = px >= e.getX() && px <= e.getX() + e.getSize()
                    && py >= e.getY() && py <= e.getY() + e.getSize();
            if (inside) {
                it.remove();
                POINT_SCORE++;
                return true;
            }
        }
        return false;
    }
}
