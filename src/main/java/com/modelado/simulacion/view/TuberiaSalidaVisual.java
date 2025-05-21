package com.modelado.simulacion.view;

import javafx.scene.layout.Pane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;

public class TuberiaSalidaVisual {
    private Pane contenedor;
    private static final String CLASE_TUBERIA = "tuberia"; // Clase CSS para tuberías

    public TuberiaSalidaVisual(Pane contenedor) {
        this.contenedor = contenedor;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // Tubería superior
        Path tuberiaSalidaSup = new Path();
        tuberiaSalidaSup.getElements().add(new MoveTo(383, 300));         // Punto inicial
        tuberiaSalidaSup.getElements().add(new LineTo(450, 300));         // Línea horizontal
        tuberiaSalidaSup.getElements().add(
                new QuadCurveTo(465, 300, 465, 315)                            // Curva de codo hacia abajo
        );
        tuberiaSalidaSup.getElements().add(new LineTo(465, 335));         // Línea vertical final
        tuberiaSalidaSup.getStyleClass().add(CLASE_TUBERIA);

        // Tubería inferior
        Path tuberiaSalidaInf = new Path();
        tuberiaSalidaInf.getElements().add(new MoveTo(383, 315));
        tuberiaSalidaInf.getElements().add(new LineTo(440, 315));
        tuberiaSalidaInf.getElements().add(
                new QuadCurveTo(450, 315, 450, 315)                            // Curva de codo hacia abajo
        );
        tuberiaSalidaInf.getElements().add(new LineTo(450, 335));
        tuberiaSalidaInf.getStyleClass().add(CLASE_TUBERIA);

        contenedor.getChildren().addAll(tuberiaSalidaSup, tuberiaSalidaInf);
    }
}
