package com.modelado.simulacion.view.components;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

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

    public void actualizarNivelAgua(double nivelActual) {
        double alturaMaxima = 220;        // Altura útil del tanque
        double nivelY = 320 - (nivelActual * alturaMaxima); // Coordenada Y del nivel actual

        agua.getElements().setAll(
                new MoveTo(222, 320),                          // Inferior izquierda
                new QuadCurveTo(317.5, 340, 378, 320),         // Curva inferior
                new LineTo(378, nivelY),                       // Lado derecho hacia arriba
                new LineTo(222, nivelY),                       // Lado superior
                new ClosePath()                                // Cierra el contorno
        );
    }

    public Path getAgua() {
        return agua;
    }
}
