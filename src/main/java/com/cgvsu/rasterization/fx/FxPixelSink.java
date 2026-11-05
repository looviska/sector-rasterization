package com.cgvsu.rasterization.fx;

import com.cgvsu.rasterization.core.ColorUtils;
import com.cgvsu.rasterization.core.PixelSink;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

/**
 * Адаптер: PixelSink поверх JavaFX PixelWriter.
 * Единственная операция вывода - pixelWriter.setColor(x, y, color).
 */
public final class FxPixelSink implements PixelSink {

    private final PixelWriter pixelWriter;
    private final int width;
    private final int height;

    public FxPixelSink(PixelWriter pixelWriter, int width, int height) {
        this.pixelWriter = pixelWriter;
        this.width = width;
        this.height = height;
    }

    @Override
    public void setPixel(int x, int y, int argb) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return; // всё, что вышло за границы холста, отбрасываем
        }
        Color color = Color.rgb(
                ColorUtils.red(argb),
                ColorUtils.green(argb),
                ColorUtils.blue(argb),
                ColorUtils.alpha(argb) / 255.0);
        pixelWriter.setColor(x, y, color);
    }
}