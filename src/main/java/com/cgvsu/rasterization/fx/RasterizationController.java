package com.cgvsu.rasterization.fx;

import com.cgvsu.rasterization.demo.SectorScene;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;

/** Контроллер главного окна: при запуске рисует демонстрационную сцену с секторами. */
public class RasterizationController {

    @FXML
    private Canvas canvas;

    @FXML
    private void initialize() {
        FxPixelSink sink = new FxPixelSink(
                canvas.getGraphicsContext2D().getPixelWriter(),
                (int) canvas.getWidth(),
                (int) canvas.getHeight());
        SectorScene.draw(sink);
    }
}