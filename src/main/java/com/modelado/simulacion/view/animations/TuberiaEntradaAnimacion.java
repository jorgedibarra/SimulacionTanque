package com.modelado.simulacion.view.animations;

import com.modelado.simulacion.view.components.AguaTuberiaEntradaVisual;
import javafx.animation.AnimationTimer;

public class TuberiaEntradaAnimacion extends AnimationTimer {
    private final AguaTuberiaEntradaVisual aguaTuberiaEntradaVisual;
    private final Runnable animacionCompletada;
    private long ultimoTiempo = 0;
    private int pasoActual = 0;
    private static final double INTERVALO = 0.0025; // Intervalo de tiempo entre pasos

    public TuberiaEntradaAnimacion(AguaTuberiaEntradaVisual aguaTuberiaEntradaVisual, Runnable animacionCompletada) {
        this.aguaTuberiaEntradaVisual = aguaTuberiaEntradaVisual;
        this.animacionCompletada = animacionCompletada;
    }

    @Override
    public void handle(long ahora) {
        if (ultimoTiempo == 0) {
            ultimoTiempo = ahora;
            return;
        }

        double tiempoTranscurrido = (ahora - ultimoTiempo) / 1_000_000_000.0; // Convertir a segundos

        if (tiempoTranscurrido >= INTERVALO && pasoActual < aguaTuberiaEntradaVisual.getCaminoAgua().size()) {
            aguaTuberiaEntradaVisual.getAguaTuberia().getElements().add(aguaTuberiaEntradaVisual.getCaminoAgua().get(pasoActual));
            pasoActual++;
            ultimoTiempo = ahora;
        }

        if (pasoActual >= aguaTuberiaEntradaVisual.getCaminoAgua().size()) {
            stop(); // Detener la animación
            if (animacionCompletada != null) animacionCompletada.run(); // Ejecutar la acción de finalización

        }
    }

    public void reiniciar() {
        aguaTuberiaEntradaVisual.getAguaTuberia().getElements().clear();
        pasoActual = 0;
        ultimoTiempo = 0;
    }
}
