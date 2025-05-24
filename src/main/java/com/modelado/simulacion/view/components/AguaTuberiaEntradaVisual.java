package com.modelado.simulacion.view.components;

import javafx.scene.layout.Pane;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public class AguaTuberiaEntradaVisual {
    private Path aguaTuberia; // Tubería de entrada de agua
    private List<PathElement> caminoAgua;  // Camino completo que sigue el agua
    private final Pane contenedor; // Contenedor donde se dibuja la tubería

    public AguaTuberiaEntradaVisual(Pane contenedor, double nivelActual) {
        this.contenedor = contenedor;
        inicializarComponentes(nivelActual);
    }

    private void inicializarComponentes(double nivelActual) {
        caminoAgua = new ArrayList<>();
        caminoAgua.add(new MoveTo(147, 67));

        // Tramo horizontal
        for (int x = 150; x <= 238; x += 3) {
            caminoAgua.add(new LineTo(x, 67));
        }

        // Curva: la simplificamos con dos segmentos
        caminoAgua.add(new QuadCurveTo(248, 67, 248, 77));

        // Tramo vertical largo: también lo dividimos
        for (int y = 70; y <= 315-nivelActual; y += 4) {
            caminoAgua.add(new LineTo(248, y));
        }
        // Path que se animará gradualmente
        aguaTuberia = new Path();
        aguaTuberia.getStyleClass().add("agua-tuberia");
        aguaTuberia.getElements().add(caminoAgua.get(0));

        contenedor.getChildren().addAll(aguaTuberia);
    }

    public Path getAguaTuberia() {
        return aguaTuberia;
    }

    public List<PathElement> getCaminoAgua() {
        return caminoAgua;
    }
}
