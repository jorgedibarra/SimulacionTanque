package com.modelado.simulacion.view.components;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;

public class TanqueVisual {
    private Path contorno;
    private Path agua; // Agua dentro del tanque
    private Pane contenedor;

    public TanqueVisual(Pane contenedor) {
        this.contenedor = contenedor;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        contorno = new Path();
        contorno.getElements().addAll(
                new MoveTo(220, 100),   // Esquina superior izquierda (sin línea superior)
                new LineTo(220, 320),  // Lado izquierdo
                new QuadCurveTo(317.5, 340, 380, 320), // Curva inferior (centro en X, más abajo en Y)
                new LineTo(380, 100)    // Lado derecho
        );
        contorno.getStyleClass().add("tanque"); // Aplicar estilo CSS
        // Agua dentro del tanque (inicialmente vacía)
        agua = new Path();
        agua.setFill(Color.BLUE); // Azul semitransparente

        contenedor.getChildren().addAll(contorno, agua);
    }

    public Path getAgua() {
        return agua;
    }
}
