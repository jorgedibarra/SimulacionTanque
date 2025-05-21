package com.modelado.simulacion.view;

import javafx.scene.layout.Pane;
import javafx.scene.shape.*;

public class ValvulaVisual {
    private Pane contenedor; // Contenedor donde se dibuja la válvula
    private static final String LINEA_VALVULA = "valvula-linea"; // Clase CSS para la línea de la válvula

    public ValvulaVisual(Pane contenedor) {
        this.contenedor = contenedor;
        inicializarComponentes();
    }
    private void inicializarComponentes() {
        Arc dibujoValvula;
        dibujoValvula = new Arc(
                120,       // centro X
                50,       // centro Y
                12,        // radio X
                12,        // radio Y
                0,         // ángulo inicial (grados)
                180        // extensión angular (180° = medio círculo)
        );
        dibujoValvula.getStyleClass().add("valvula"); // Aplicar estilo CSS
        dibujoValvula.setType(ArcType.OPEN); // Sin relleno entre los extremos

        Path lineaInfValvula = new Path(
                new MoveTo(108, 50), // Punto de la válvula
                new LineTo(132, 50) // Línea horizontal hacia la válvula
        );
        lineaInfValvula.getStyleClass().add(LINEA_VALVULA);

        Path valvulaContornoizq = new Path(
                new MoveTo(100, 60),
                new LineTo(100, 75),
                new LineTo(120, 69),
                new LineTo(100, 60)
        );
        valvulaContornoizq.getStyleClass().add(LINEA_VALVULA);

        Path valvulaContornoDer = new Path(
                new MoveTo(140, 60),
                new LineTo(140, 75),
                new LineTo(120, 69),
                new LineTo(140, 60)
        );
        valvulaContornoDer.getStyleClass().add(LINEA_VALVULA);

        Path lineaValvula = new Path(
                new MoveTo(120, 69),
                new LineTo(120, 50)
        );
        lineaValvula.getStyleClass().add(LINEA_VALVULA);

        contenedor.getChildren().addAll(
                valvulaContornoizq, valvulaContornoDer, lineaValvula, lineaInfValvula, dibujoValvula
        );
    }
}
