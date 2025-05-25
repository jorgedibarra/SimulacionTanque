package com.modelado.simulacion.view.components;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;

public class TransmisorVisual {
    private Pane contenedor;

    public TransmisorVisual(Pane contenedor) {
        this.contenedor = contenedor;
        dibujarTransmisor();
    }

    private void dibujarTransmisor() {
        // 1. Crear elementos gráficos
        Path tuberiaTransmisor = new Path(
                new MoveTo(383, 200), // Punto inicial
                new LineTo(460, 200)  // Línea horizontal hacia el transmisor
        );
        Circle transmisor = new Circle(460, 200, 25);
        Text textoLT = new Text(455, 205, "LT");

        // Aplicar estilos CSS
        transmisor.getStyleClass().add("transmisor");
        tuberiaTransmisor.getStyleClass().add("tuberia-transmisor");
        textoLT.getStyleClass().add("texto-transmisor");

        contenedor.getChildren().addAll(tuberiaTransmisor, transmisor, textoLT);
    }
}
