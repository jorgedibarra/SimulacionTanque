package com.modelado.simulacion.view.animations;

import com.modelado.simulacion.model.Grafica;
import com.modelado.simulacion.view.components.TanqueVisual;
import javafx.animation.AnimationTimer;
import javafx.scene.text.Text;

public class TanqueLLenadoAnimacion extends AnimationTimer {
    private final TanqueVisual tanqueVisual;
    private final Runnable onLlenadoCompleto;
    private final Grafica grafica;
    private final Text textoNivel;

    private double nivelActual;
    private double setpoint;
    private long ultimoTiempo = 0;

    public TanqueLLenadoAnimacion(TanqueVisual tanqueVisual,
                                  Runnable onLlenadoCompleto,
                                  Grafica grafica,
                                  Text textoNivel) {
        this.tanqueVisual = tanqueVisual;
        this.onLlenadoCompleto = onLlenadoCompleto;
        this.grafica = grafica;
        this.textoNivel = textoNivel;
    }

    public void configurar( double setpoint) {
        this.nivelActual = grafica.getUltimoNivel();
        this.setpoint = setpoint;
        this.ultimoTiempo = 0;
    }

    @Override
    public void handle(long ahora) {
        if (ultimoTiempo == 0) {
            ultimoTiempo = ahora;
            return;
        }

        double deltaTiempo = (ahora - ultimoTiempo) / 1_000_000_000.0;
        ultimoTiempo = ahora;

        double velocidad = 0.095;
        nivelActual += velocidad * deltaTiempo;
        nivelActual = Math.min(nivelActual, setpoint + 0.1);

        actualizarVisualizacion(deltaTiempo);

        if (nivelActual >= setpoint + 0.1) {
            stop();
            onLlenadoCompleto.run();
        }
    }

    private void actualizarVisualizacion(double deltaTiempo) {
        tanqueVisual.actualizarNivelAgua(nivelActual);
        grafica.agregarPunto(deltaTiempo, nivelActual);
        textoNivel.setText(String.format("%.2f", nivelActual));
    }

    public void reset() {
        this.nivelActual = 0;
        this.ultimoTiempo = 0;
    }
}
