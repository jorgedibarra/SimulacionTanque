package com.modelado.simulacion.model;

import javafx.scene.chart.XYChart;

public class Grafica {
    private final XYChart.Series<Number, Number> serieNivel;
    private double tiempoAcumulado = 0;
    private double ultimoNivel = 0;

    public Grafica(XYChart.Series<Number, Number> serieNivel) {
        this.serieNivel = serieNivel;
    }

    public void agregarPunto(double deltaTiempo, double nivel) {
        tiempoAcumulado += deltaTiempo;
        ultimoNivel = nivel;
        serieNivel.getData().add(new XYChart.Data<>(tiempoAcumulado, nivel));
    }

    public void reset() {
        tiempoAcumulado = 0;
        ultimoNivel = 0;
        serieNivel.getData().clear();
    }

    public double getUltimoNivel() {
        return ultimoNivel;
    }

    public double getTiempoAcumulado() {
        return tiempoAcumulado;
    }
}
