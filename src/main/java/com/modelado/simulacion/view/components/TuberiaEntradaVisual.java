package com.modelado.simulacion.view.components;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class TuberiaEntradaVisual {

    private Pane contenedor; // Contenedor donde se dibujará la tubería
    private static final String CLASE_TUBERIA = "tuberia"; // Clase CSS para tuberías

    public TuberiaEntradaVisual(Pane contenedor) {
        this.contenedor = contenedor;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        Path tuberiaValvulaSup = new Path(
                new MoveTo(15, 60), // Punto inicial (fuera del tanque)
                new LineTo(100, 60) // Línea vertical hacia la válvula
        );
        tuberiaValvulaSup.getStyleClass().add(CLASE_TUBERIA);

        Path tuberiaValvulaInf = new Path(
                new MoveTo(15, 75), // Punto inicial (fuera del tanque)
                new LineTo(100, 75) // Línea vertical hacia la válvula
        );
        tuberiaValvulaInf.getStyleClass().add(CLASE_TUBERIA);

        Rectangle aguaHastaValvula = new Rectangle(15, 61, 85, 13); // Posición inicial en la base
        aguaHastaValvula.setFill(Color.BLUE); // Azul semitransparente

        // Tubería de entrada superior con curva
        Path tuberiaEntradaSup = new Path();
        tuberiaEntradaSup.getElements().add(new MoveTo(140, 60));            // Punto inicial
        tuberiaEntradaSup.getElements().add(new LineTo(245, 60));            // Línea horizontal
        tuberiaEntradaSup.getElements().add(
                new QuadCurveTo(255, 60, 255, 70)                                // Curva hacia abajo
        );
        tuberiaEntradaSup.getElements().add(new LineTo(255, 90));            // Línea vertical final
        tuberiaEntradaSup.getStyleClass().add(CLASE_TUBERIA);

        // Tubería de entrada inferior con curva
        Path tuberiaEntradaInf = new Path();
        tuberiaEntradaInf.getElements().add(new MoveTo(140, 75));
        tuberiaEntradaInf.getElements().add(new LineTo(230, 75));
        tuberiaEntradaInf.getElements().add(
                new QuadCurveTo(240, 75, 240, 75)                                // Curva hacia abajo
        );
        tuberiaEntradaInf.getElements().add(new LineTo(240, 90));
        tuberiaEntradaInf.getStyleClass().add(CLASE_TUBERIA);



        contenedor.getChildren().addAll(
                tuberiaValvulaSup, tuberiaValvulaInf, tuberiaEntradaSup, tuberiaEntradaInf,
                aguaHastaValvula // Agua hasta la válvula
        );
    }
}
