package com.modelado.simulacion.view.animations;

import com.modelado.simulacion.view.components.AguaTuberiaEntradaVisual;
import com.modelado.simulacion.view.components.TuberiaEntradaVisual;
import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;

public class TuberiaEntradaVaciadoAnimacion extends AnimationTimer {

    private final AguaTuberiaEntradaVisual aguaTuberiaEntradaVisual;
    private final Runnable animacionCompletada;
    private long ultimoTiempo = 0;
    private static final double INTERVALO = 0.009; // Intervalo de tiempo entre pasos

    public TuberiaEntradaVaciadoAnimacion(AguaTuberiaEntradaVisual aguaTuberiaEntradaVisual, Runnable animacionCompletada) {
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

        if (tiempoTranscurrido >= INTERVALO) {
            ObservableList<PathElement> elementos = aguaTuberiaEntradaVisual.getAguaTuberia().getElements();

            if (!procesarElementos(elementos)) {
                stop(); // Detener la animación
                if (animacionCompletada != null) animacionCompletada.run(); // Ejecutar la acción de finalización
            }

            ultimoTiempo = ahora;
        }
    }

    private boolean procesarElementos(ObservableList<PathElement> elementos) {
        if (elementos.size() > 2) {
            // Convertir el siguiente elemento en MoveTo antes de eliminar el actual
            PathElement siguiente = elementos.get(2);
            if (siguiente instanceof LineTo) {
                LineTo lineTo = (LineTo) siguiente;
                elementos.set(2, new MoveTo(lineTo.getX(), lineTo.getY()));
            } else if (siguiente instanceof CubicCurveTo) {
                CubicCurveTo curve = (CubicCurveTo) siguiente;
                elementos.set(2, new MoveTo(curve.getX(), curve.getY()));
            }
            elementos.remove(1);
            return true;
        } else if (elementos.size() == 2) {
            elementos.remove(1);
            return true;
        } else if (elementos.size() == 1 && elementos.get(0) instanceof MoveTo) {
            elementos.clear();
            return false;
        }
        return false;
    }
}

