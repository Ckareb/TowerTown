package com.example.starairman.data.model;

public class TownPosition {
    private final float x;

    private final float y;

    private static final int HEALTH_POINTS = 100;

    public TownPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static int getHealthPoints() {
        return HEALTH_POINTS;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
