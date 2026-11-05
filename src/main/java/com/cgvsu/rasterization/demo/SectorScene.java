package com.cgvsu.rasterization.demo;

import com.cgvsu.rasterization.core.ColorUtils;
import com.cgvsu.rasterization.core.PixelSink;
import com.cgvsu.rasterization.core.SectorRasterizer;

/**
 * Тестовая сцена: сетка 4 x 3 ячейки, в каждой - сектор с другими параметрами.
 * Покрывает случаи, которые требует методичка: разные углы, растяжение
 * (разные радиусы), секторы в разных четвертях, больше и меньше 180 градусов.
 */
public final class SectorScene {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int COLUMNS = 4;
    public static final int ROWS = 3;
    public static final int CELL = 200;

    /** Описание одного сектора сцены. */
    public record Case(String title, int radius, double startDeg, double sweepDeg,
                       int centerColor, int edgeColor) {
    }

    private static final int RED = ColorUtils.rgb(230, 30, 30);
    private static final int BLUE = ColorUtils.rgb(30, 60, 230);
    private static final int GREEN = ColorUtils.rgb(40, 200, 70);
    private static final int YELLOW = ColorUtils.rgb(250, 220, 40);
    private static final int WHITE = ColorUtils.rgb(255, 255, 255);
    private static final int BLACK = ColorUtils.rgb(20, 20, 20);
    private static final int ORANGE = ColorUtils.rgb(250, 140, 20);
    private static final int PURPLE = ColorUtils.rgb(130, 40, 190);
    private static final int CYAN = ColorUtils.rgb(30, 200, 220);

    private static final Case[] CASES = {
            new Case("0..90, красный->синий", 85, 0, 90, RED, BLUE),
            new Case("30..60, жёлтый->зелёный", 85, 30, 30, YELLOW, GREEN),
            new Case("45..225 (180), белый->чёрный", 85, 45, 180, WHITE, BLACK),
            new Case("200..470 (270), оранжевый->фиолетовый", 85, 200, 270, ORANGE, PURPLE),
            new Case("10..360 (350), голубой->красный", 85, 10, 350, CYAN, RED),
            new Case("90..210 (120), зелёный->синий", 85, 90, 120, GREEN, BLUE),
            new Case("-60..60 (через 0), чёрный->жёлтый", 85, -60, 120, BLACK, YELLOW),
            new Case("181, фиолетовый->оранжевый", 85, 0, 181, PURPLE, ORANGE),
            new Case("179, красный->зелёный", 85, 100, 179, RED, GREEN),
            new Case("полный круг, один цвет", 60, 0, 360, ORANGE, ORANGE),
            new Case("малый радиус, 90", 20, 270, 90, RED, YELLOW),
            new Case("тонкий сектор 5", 85, 30, 5, BLUE, CYAN),
    };

    private SectorScene() {
    }

    public static Case[] cases() {
        return CASES.clone();
    }

    /** Центр ячейки с номером i. */
    public static int centerX(int i) {
        return (i % COLUMNS) * CELL + CELL / 2;
    }

    public static int centerY(int i) {
        return (i / COLUMNS) * CELL + CELL / 2;
    }

    /** Рисует всю сцену через единственную разрешённую операцию - setPixel. */
    public static void draw(PixelSink sink) {
        for (int i = 0; i < CASES.length; i++) {
            Case c = CASES[i];
            SectorRasterizer.fillSector(sink, centerX(i), centerY(i), c.radius(),
                    c.startDeg(), c.sweepDeg(), c.centerColor(), c.edgeColor());
        }
    }
}