package com.modelado.simulacion.model;

public class Tanque {
    private double nivel; // Nivel de agua en el tanque
    private double capacidad; // Capacidad máxima del tanque
    private double flujoEntrada; // Flujo de entrada al tanque
    private double flujoSalida; // Flujo de salida del tanque

    public Tanque(double capacidad) {
        this.capacidad = capacidad;
        this.nivel = 0;
        this.flujoEntrada = 0;
        this.flujoSalida = 0;
    }

    public void setFlujoEntrada(double flujo) {
        this.flujoEntrada = flujo;
    }

    public void setFlujoSalida(double flujo) {
        this.flujoSalida = flujo;
    }

    public void actualizarNivel(double tiempo) {
        double cambioNivel = (flujoEntrada - flujoSalida) * tiempo;
        nivel += cambioNivel;

        if (nivel > capacidad) {
            nivel = capacidad; // Evitar desbordamiento
        } else if (nivel < 0) {
            nivel = 0; // Evitar niveles negativos
        }
    }

    public double getNivel() {
        return nivel;
    }

    public double getCapacidad() {
        return capacidad;
    }
}
