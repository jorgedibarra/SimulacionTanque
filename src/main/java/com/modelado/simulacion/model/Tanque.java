package com.modelado.simulacion.model;

public class Tanque {
    private double nivel; // Nivel de agua en el tanque
    private double nivelMax;
    private double nivelMin;
    private double capacidad; // Capacidad máxima del tanque


    public Tanque(double capacidad) {
        this.capacidad = capacidad;
        this.nivel = 0;
    }

    public double getNivel() {
        return nivel;
    }


}
