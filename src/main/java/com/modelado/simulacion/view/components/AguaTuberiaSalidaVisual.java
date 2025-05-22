package com.modelado.simulacion.view.components;

import javafx.scene.layout.Pane;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public class AguaTuberiaSalidaVisual {
    private Path aguaTuberiaSalida;
    private Pane contenedor;
    private List<PathElement> caminoAguaSalida; // Camino completo que sigue el agua

    public AguaTuberiaSalidaVisual(Pane contenedor) {
        this.contenedor = contenedor;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        caminoAguaSalida = new ArrayList<>();
        // Punto inicial (coincide con la entrada superior de la tubería)
        caminoAguaSalida.add(new MoveTo(389, 308)); // ligeramente centrado entre 300 y 315

        // Tramo horizontal
        for (int x = 391; x <= 450; x += 1) {
            caminoAguaSalida.add(new LineTo(x, 308)); // y = 307 para coincidir con el centro
        }

        // Curva: hacia abajo, simulando el codo de la tubería
        caminoAguaSalida.add(new QuadCurveTo(457, 308, 457, 315)); // punto de control centrado

        // Tramo vertical final (hasta el fondo de la tubería)
        for (int y = 325; y <= 335; y += 1) {
            caminoAguaSalida.add(new LineTo(457, y));
        }
        // Crear y aplicar el path que se animará
        aguaTuberiaSalida = new Path();
        aguaTuberiaSalida.getStyleClass().add("agua-tuberia");

        // Agregamos todos los elementos al Path
        aguaTuberiaSalida.getElements().addAll(caminoAguaSalida);
        contenedor.getChildren().add(aguaTuberiaSalida);
    }

    public List<PathElement> getCaminoAguaSalida() {
        return caminoAguaSalida;
    }

    public void setCaminoAguaSalida(List<PathElement> caminoAguaSalida) {
        this.caminoAguaSalida = caminoAguaSalida;
    }

    public Path getAguaTuberiaSalida() {
        return aguaTuberiaSalida;
    }
}
