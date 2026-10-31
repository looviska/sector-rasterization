package com.cgvsu.rasterization.core;

/**
 * Единственная операция вывода, которую разрешает задание:
 * закрасить один пиксель заданным цветом.
 *
 * Цвет передаётся в формате ARGB, упакованном в int (0xAARRGGBB).
 * Благодаря интерфейсу алгоритмы не зависят от JavaFX: в приложении
 * за ним стоит PixelWriter.setColor(x, y, color), в тестах - BufferedImage.
 */
@FunctionalInterface
public interface PixelSink {
    void setPixel(int x, int y, int argb);
}