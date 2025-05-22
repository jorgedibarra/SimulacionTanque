package com.modelado.simulacion.view.components;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

public class ControladorVisual {
    private Pane contenedor;
    private Text textoNivel;

    public ControladorVisual(Pane contenedor, double nivelActual) {
        this.contenedor = contenedor;
        inicializarComponentes(nivelActual);
    }

    private void inicializarComponentes(double nivelActual) {
        Path lineaControlador = new Path(
                new MoveTo(485, 200), // Punto inicial
                new LineTo(550, 200) // Línea vertical hacia el controlador
        );
        Circle controlador = new Circle(550, 200, 25, Color.WHITE);
        Text textoLC = new Text(545, 205, "LC");

        Path lineaValvula = new Path(
                new MoveTo(550, 200), // Punto de la válvula
                new LineTo(550, 20),
                new LineTo(120, 20), // Línea horizontal hacia la válvula
                new LineTo(120, 37) // Línea vertical hacia la válvula
        );

        Path lineaVisor = new Path(
                new MoveTo(550, 200), // Punto de la válvula
                new LineTo(550, 235) // Línea horizontal hacia la válvula
        );
        Rectangle visorNivel = new Rectangle(530, 235, 40, 30);

        textoNivel = new Text(535, 255, String.format("%.2f", nivelActual));


        controlador.getStyleClass().add("controlador");
        textoLC.getStyleClass().add("texto-controlador");
        lineaControlador.getStyleClass().add("linea-controlador");
        lineaValvula.getStyleClass().add("linea-controlador");
        visorNivel.getStyleClass().add("visor-nivel");
        lineaVisor.getStyleClass().add("valvula-linea");
        textoNivel.getStyleClass().add("texto-nivel");

        contenedor.getChildren().addAll(lineaVisor, lineaValvula, lineaControlador, controlador, textoLC, visorNivel, textoNivel);
    }

    public Text getTextoNivel() {
        return textoNivel;
    }

    public void setTextoNivel(Text textoNivel) {
        this.textoNivel = textoNivel;
    }
}
