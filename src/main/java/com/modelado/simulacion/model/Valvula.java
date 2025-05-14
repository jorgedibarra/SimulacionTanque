package com.modelado.simulacion.model;

public class Valvula {
    private boolean abierta; // Estado de la válvula (abierta o cerrada)
    private double flujo; // Flujo a través de la válvula

    public Valvula() {
        this.abierta = false;
        this.flujo = 0;
    }

    public void abrir() {
        this.abierta = true;
        this.flujo = 1; // Flujo máximo al abrir
    }

    public void cerrar() {
        this.abierta = false;
        this.flujo = 0; // Sin flujo al cerrar
    }

    public boolean isAbierta() {
        return abierta;
    }

    public double getFlujo() {
        return flujo;
    }
}
