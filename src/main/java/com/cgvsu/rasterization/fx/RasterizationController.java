package com.cgvsu.rasterization.fx;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;

/** Контроллер главного окна. Пока только хранит ссылку на canvas. */
public class RasterizationController {

    @FXML
    private Canvas canvas;

    @FXML
    private void initialize() {
        // Отрисовка сцены будет добавлена после реализации алгоритма.
    }
}