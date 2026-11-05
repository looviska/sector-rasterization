package com.cgvsu.rasterization.core;

import com.cgvsu.rasterization.demo.SectorScene;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Рисует сцену в PNG без графического интерфейса (для проверки глазами и для отчёта).
 * Сам алгоритм пишет в изображение только через PixelSink.setPixel; линии сетки
 * на увеличенных фрагментах рисуются отдельно и к алгоритму отношения не имеют.
 *
 * Запуск: scripts/render.sh [каталог для PNG]
 */
public final class ScenePngRenderer {

    private static BufferedImage whiteImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }

    private static PixelSink sinkFor(BufferedImage img) {
        return (x, y, argb) -> {
            if (x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight()) {
                img.setRGB(x, y, argb);
            }
        };
    }

    /** Полная сцена 800 x 600. */
    static void renderScene(File dir) throws IOException {
        BufferedImage img = whiteImage(SectorScene.WIDTH, SectorScene.HEIGHT);
        SectorScene.draw(sinkFor(img));
        ImageIO.write(img, "png", new File(dir, "scene.png"));
    }

    /** Увеличенный фрагмент: один сектор малого радиуса, каждый пиксель - квадрат scale x scale с сеткой. */
    static void renderZoom(File dir, String name, int r, double start, double sweep,
                           int cCenter, int cEdge, int scale) throws IOException {
        int size = 2 * r + 3;
        BufferedImage small = whiteImage(size, size);
        int c = r + 1;
        SectorRasterizer.fillSector(sinkFor(small), c, c, r, start, sweep, cCenter, cEdge);

        BufferedImage big = new BufferedImage(size * scale, size * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = big.createGraphics();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                g.setColor(new Color(small.getRGB(x, y), true));
                g.fillRect(x * scale, y * scale, scale, scale);
            }
        }
        g.setColor(new Color(160, 160, 160));
        for (int i = 0; i <= size; i++) {
            g.drawLine(i * scale, 0, i * scale, size * scale);
            g.drawLine(0, i * scale, size * scale, i * scale);
        }
        // отмечаем центр круга
        g.setColor(Color.BLACK);
        int cx = c * scale + scale / 2;
        g.fillOval(cx - 3, cx - 3, 7, 7);
        g.dispose();
        ImageIO.write(big, "png", new File(dir, name));
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");
        File dir = new File(args.length > 0 ? args[0] : "build-images");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Не удалось создать каталог " + dir);
        }
        renderScene(dir);
        int red = ColorUtils.rgb(230, 30, 30);
        int blue = ColorUtils.rgb(30, 60, 230);
        int yellow = ColorUtils.rgb(250, 220, 40);
        int purple = ColorUtils.rgb(130, 40, 190);
        renderZoom(dir, "zoom_small.png", 14, 20, 100, red, blue, 18);
        renderZoom(dir, "zoom_large.png", 14, 200, 250, yellow, purple, 18);
        System.out.println("PNG записаны в " + dir.getAbsolutePath());
    }
}