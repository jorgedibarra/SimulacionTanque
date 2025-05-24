package com.modelado.simulacion.view.animations;

import com.modelado.simulacion.view.components.AguaTuberiaSalidaVisual;
import javafx.animation.AnimationTimer;

public class TuberiaSalidaAnimacion extends AnimationTimer {
    private final AguaTuberiaSalidaVisual aguaTuberiaSalidaVisual;
    private final Runnable animacionCompletada;
    private long ultimoTiempo = 0;
    private int pasoActual = 0;
    private static final double INTERVALO = 0.0025; // Intervalo de tiempo entre pasos

    public TuberiaSalidaAnimacion(AguaTuberiaSalidaVisual aguaTuberiaSalidaVisual, Runnable animacionCompletada) {
        this.aguaTuberiaSalidaVisual = aguaTuberiaSalidaVisual;
        this.animacionCompletada = animacionCompletada;
    }

    @Override
    public void handle(long ahora) {
        if (ultimoTiempo == 0) {
            ultimoTiempo = ahora;
            return;
        }

        double tiempoTranscurrido = (ahora - ultimoTiempo) / 1_000_000_000.0; // Convertir a segundos

        if (tiempoTranscurrido >= INTERVALO && pasoActual < aguaTuberiaSalidaVisual.getCaminoAguaSalida().size()) {
            aguaTuberiaSalidaVisual.getAguaTuberiaSalida().getElements().add(aguaTuberiaSalidaVisual.getCaminoAguaSalida().get(pasoActual));
            pasoActual++;
            ultimoTiempo = ahora;
        }

        if (pasoActual >= aguaTuberiaSalidaVisual.getCaminoAguaSalida().size()) {
            stop(); // Detener la animación
            if (animacionCompletada != null) animacionCompletada.run(); // Ejecutar la acción de finalización

        }
    }

    public void reiniciar() {
        aguaTuberiaSalidaVisual.getAguaTuberiaSalida().getElements().clear();
        pasoActual = 0;
        ultimoTiempo = 0;
    }
}
