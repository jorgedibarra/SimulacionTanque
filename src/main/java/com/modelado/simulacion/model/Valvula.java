package com.modelado.simulacion.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;

public class Valvula {
    private boolean abierta; // Estado de la válvula (abierta o cerrada)
    private double flujo; // Flujo a través de la válvula

    public Valvula() {
        this.abierta = false;
        this.flujo = 0;

    }

    public void abrir(Arc color) {
        this.abierta = true;
        this.flujo = 1; // Flujo máximo al abrir
        color.setFill(Color.GREEN); // Cambia el color a verde al abrir
    }

    public void cerrar(Arc color) {
        this.abierta = false;
        this.flujo = 0; // Sin flujo al cerrar
        color.setFill(Color.RED); // Cambia el color a rojo al cerrar
    }

    public boolean isAbierta() {
        return abierta;
    }

    public double getFlujo() {
        return flujo;
    }
}
