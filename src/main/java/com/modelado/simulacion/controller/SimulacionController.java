package com.modelado.simulacion.controller;

import com.modelado.simulacion.model.Tanque;

import com.modelado.simulacion.model.Valvula;
import com.modelado.simulacion.utils.Errores;
import com.modelado.simulacion.utils.Validacion;
import javafx.animation.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.modelado.simulacion.utils.Validacion.restringirSoloNumeros;

public class SimulacionController {

    Tanque tanque = new Tanque(1.0); // Instancia del modelo Tanque
    Valvula valvula = new Valvula(); // Instancia del modelo Válvula
    private static final String CLASE_TUBERIA = "tuberia"; // Clase CSS para tuberías
    private static final String LINEA_VALVULA = "valvula-linea"; // Clase CSS para la línea de la válvula

    // --- Elementos de Simulación ---
    private Arc dibujoValvula; // Representación gráfica de la válvula
    private Path aguaTanque;  // Representación gráfica del agua en el tanque
    private Path aguaTuberiaEntrada; // Representación gráfica del agua en la tubería de entrada
    private Path aguaTuberiaSalida; // Representación gráfica del agua en la tubería de salida
    private List<PathElement> caminoAgua;  // Camino completo que sigue el agua
    private int pasoActual = 0; // Paso actual de la animación de la tubería
    private int pasoActualSalida = 0; // Paso actual de la animación de la tubería
    private double nivelActual = tanque.getNivel();  // Nivel actual del tanque (0-1)
    private Text textoNivel; // Texto que muestra el nivel del agua
    private boolean simulacionActiva = false;
    private boolean modoLlenadoActivo = false;
    private boolean modoVaciadoActivo = false;
    private boolean consumoActivado = false;
    private boolean primeraVez = true;
    private boolean tuberiaEnCurso = false; // Para evitar que el agua se dibuje varias veces

    private AnimationTimer animacionTanque; // Animación para el llenado del tanque
    private AnimationTimer animacionTuberiaEntrada; // Animación para el agua en la tubería
    private AnimationTimer animacionVaciadoTuberiaEntrada; // Animación para el vaciado de la tubería
    private AnimationTimer animacionTuberiaSalida; // Animación para el agua en la tubería de salida
    private AnimationTimer animacionVaciadoTuberiaSalida; // Animación para el vaciado de la tubería de salida
    private XYChart.Series<Number, Number> serieNivel;
    private double tiempoTotal = 0; // tiempo simulado en segundos
    double tiempoTranscurrido = 0;

    // --- Controles del FXML Existente ---
    @FXML
    private LineChart<Number, Number> grafica;
    @FXML
    private Pane simulacionPane; // Pane donde se dibuja la simulación
    @FXML
    private TextField setpointField; // Campo de texto para el setpoint
    @FXML
    private Button startButton; // Botón para iniciar la simulación
    @FXML
    private Button stopButton; // Botón para detener la simulación
    @FXML
    private Button emptyButton; // Botón para vaciar el tanque

    @FXML
    private void initialize() {
        restringirSoloNumeros(setpointField);
        dibujarSimulacion();  // Dibuja el tanque, tuberías, etc.
        stopButton.setDisable(true); // Deshabilitar el botón de detener al inicio
        emptyButton.setDisable(true); // Deshabilitar el botón de vaciar al inicio
        startButton.setCursor(Cursor.HAND);
        serieNivel = new XYChart.Series<>();
        grafica.setLegendVisible(false);
        grafica.getData().add(serieNivel);
    }

    @FXML
    private void handleStart() {
        if (obtenerSetpoint() != 0) {
            simulacionActiva = true;
            modoLlenadoActivo = true;
            modoVaciadoActivo = false;
            startButton.setDisable(true);
            startButton.setCursor(Cursor.DEFAULT);
            stopButton.setDisable(false);
            stopButton.setCursor(Cursor.HAND);
            emptyButton.setDisable(false);
            emptyButton.setCursor(Cursor.HAND);
            valvula.abrir(dibujoValvula);
            pasoActual = 0;
            dibujarAguaTuberiaEntrada();
            crearAnimacionAguaTuberiaEntrada();
            animacionTuberiaEntrada.start(); // Solo inicia esto. Lo demás se activa automáticamente después.
        }
    }

    @FXML
    private void handleStop() {
        simulacionActiva = false;
        modoLlenadoActivo = false;
        modoVaciadoActivo = false;
        animacionTanque.stop();
        startButton.setDisable(false);
        startButton.setCursor(Cursor.HAND);
        stopButton.setDisable(true);
        stopButton.setCursor(Cursor.DEFAULT);
        valvula.cerrar(dibujoValvula);
        pasoActual = caminoAgua.size(); // Por si quieres vaciar la tubería también
        crearAnimacionVaciadoTuberiaEntrada();
        animacionVaciadoTuberiaEntrada.start();
        crearAnimacionVaciadoTuberiaSalida();
        animacionVaciadoTuberiaSalida.start();
    }

    @FXML
    public void handleEmpty() {
        consumoActivado = true;
        modoVaciadoActivo = true;
        dibujarAguaTuberiaSalida();
        crearAnimacionAguaTuberiaSalida();
        animacionTuberiaSalida.start(); // Inicia la animación de vaciado del tanque
    }
    private void dibujarSimulacion() {
        // Limpia el Pane antes de dibujar
        simulacionPane.getChildren().clear();

        dibujarTanque();
        dibujarTuberiaEntrada();
        dibujarValvula();
        dibujarTuberiaSalida();
        dibujarTransmisor();
        dibujarControlador();

        URL recurso = getClass().getResource("/com/modelado/simulacion/images/casa_tanque.png");
        assert recurso != null;
        Image imagen = new Image(recurso.toExternalForm());
        ImageView imageView = new ImageView(imagen);
        imageView.setLayoutX(413);
        imageView.setLayoutY(360);

        simulacionPane.getChildren().add(imageView);

    }
    private void dibujarAguaTuberiaEntrada() {
        caminoAgua = new ArrayList<>();
        caminoAgua.add(new MoveTo(147, 67));

        // Tramo horizontal
        for (int x = 150; x <= 238; x += 1) {
            caminoAgua.add(new LineTo(x, 67));
        }

        // Curva: la simplificamos con dos segmentos
        caminoAgua.add(new QuadCurveTo(248, 67, 248, 77));

        // Tramo vertical largo: también lo dividimos
        for (int y = 70; y <= 315-nivelActual; y += 2) {
            caminoAgua.add(new LineTo(248, y));
        }
        // Path que se animará gradualmente
        aguaTuberiaEntrada = new Path();
        aguaTuberiaEntrada.getStyleClass().add("agua-tuberia");
        aguaTuberiaEntrada.getElements().add(caminoAgua.get(0));

        simulacionPane.getChildren().addAll(aguaTuberiaEntrada);
    }
    private void dibujarAguaTuberiaSalida() {
        List<PathElement> caminoAguaSalida = new ArrayList<>();
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
        simulacionPane.getChildren().add(aguaTuberiaSalida);
    }
    private void dibujarTanque() {
        Path contornoTanque = new Path();
        contornoTanque.getElements().addAll(
                new MoveTo(220, 100),   // Esquina superior izquierda (sin línea superior)
                new LineTo(220, 320),  // Lado izquierdo
                new QuadCurveTo(317.5, 340, 380, 320), // Curva inferior (centro en X, más abajo en Y)
                new LineTo(380, 100)    // Lado derecho
        );
        contornoTanque.getStyleClass().add("tanque"); // Aplicar estilo CSS
        // Agua dentro del tanque (inicialmente vacía)
        aguaTanque = new Path();
        aguaTanque.setFill(Color.BLUE); // Azul semitransparente

        simulacionPane.getChildren().addAll(contornoTanque, aguaTanque);
    }
    private void dibujarTuberiaEntrada() {
        Path tuberiaValvulaSup = new Path(
                new MoveTo(15, 60), // Punto inicial (fuera del tanque)
                new LineTo(100, 60) // Línea vertical hacia la válvula
        );
        tuberiaValvulaSup.getStyleClass().add(CLASE_TUBERIA);

        Path tuberiaValvulaInf = new Path(
                new MoveTo(15, 75), // Punto inicial (fuera del tanque)
                new LineTo(100, 75) // Línea vertical hacia la válvula
        );
        tuberiaValvulaInf.getStyleClass().add(CLASE_TUBERIA);

        Rectangle aguaHastaValvula = new Rectangle(15, 61, 85, 13); // Posición inicial en la base
        aguaHastaValvula.setFill(Color.BLUE); // Azul semitransparente

        // Tubería de entrada superior con curva
        Path tuberiaEntradaSup = new Path();
        tuberiaEntradaSup.getElements().add(new MoveTo(140, 60));            // Punto inicial
        tuberiaEntradaSup.getElements().add(new LineTo(245, 60));            // Línea horizontal
        tuberiaEntradaSup.getElements().add(
                new QuadCurveTo(255, 60, 255, 70)                                // Curva hacia abajo
        );
        tuberiaEntradaSup.getElements().add(new LineTo(255, 90));            // Línea vertical final
        tuberiaEntradaSup.getStyleClass().add(CLASE_TUBERIA);

        // Tubería de entrada inferior con curva
        Path tuberiaEntradaInf = new Path();
        tuberiaEntradaInf.getElements().add(new MoveTo(140, 75));
        tuberiaEntradaInf.getElements().add(new LineTo(230, 75));
        tuberiaEntradaInf.getElements().add(
                new QuadCurveTo(240, 75, 240, 75)                                // Curva hacia abajo
        );
        tuberiaEntradaInf.getElements().add(new LineTo(240, 90));
        tuberiaEntradaInf.getStyleClass().add(CLASE_TUBERIA);



        simulacionPane.getChildren().addAll(
                tuberiaValvulaSup, tuberiaValvulaInf, tuberiaEntradaSup, tuberiaEntradaInf,
                aguaHastaValvula // Agua hasta la válvula
        );
    }
    private void dibujarTuberiaSalida() {
        // Tubería superior
        Path tuberiaSalidaSup = new Path();
        tuberiaSalidaSup.getElements().add(new MoveTo(383, 300));         // Punto inicial
        tuberiaSalidaSup.getElements().add(new LineTo(450, 300));         // Línea horizontal
        tuberiaSalidaSup.getElements().add(
                new QuadCurveTo(465, 300, 465, 315)                            // Curva de codo hacia abajo
        );
        tuberiaSalidaSup.getElements().add(new LineTo(465, 335));         // Línea vertical final
        tuberiaSalidaSup.getStyleClass().add(CLASE_TUBERIA);

        // Tubería inferior
        Path tuberiaSalidaInf = new Path();
        tuberiaSalidaInf.getElements().add(new MoveTo(383, 315));
        tuberiaSalidaInf.getElements().add(new LineTo(440, 315));
        tuberiaSalidaInf.getElements().add(
                new QuadCurveTo(450, 315, 450, 315)                            // Curva de codo hacia abajo
        );
        tuberiaSalidaInf.getElements().add(new LineTo(450, 335));
        tuberiaSalidaInf.getStyleClass().add(CLASE_TUBERIA);

        simulacionPane.getChildren().addAll(tuberiaSalidaSup, tuberiaSalidaInf);
    }
    private void dibujarValvula() {
        dibujoValvula = new Arc(
                120,       // centro X
                50,       // centro Y
                12,        // radio X
                12,        // radio Y
                0,         // ángulo inicial (grados)
                180        // extensión angular (180° = medio círculo)
        );
        dibujoValvula.getStyleClass().add("valvula"); // Aplicar estilo CSS
        dibujoValvula.setType(ArcType.OPEN); // Sin relleno entre los extremos

        Path lineaInfValvula = new Path(
                new MoveTo(108, 50), // Punto de la válvula
                new LineTo(132, 50) // Línea horizontal hacia la válvula
        );
        lineaInfValvula.getStyleClass().add(LINEA_VALVULA);

        Path valvulaContornoizq = new Path(
                new MoveTo(100, 60),
                new LineTo(100, 75),
                new LineTo(120, 69),
                new LineTo(100, 60)
        );
        valvulaContornoizq.getStyleClass().add(LINEA_VALVULA);

        Path valvulaContornoDer = new Path(
                new MoveTo(140, 60),
                new LineTo(140, 75),
                new LineTo(120, 69),
                new LineTo(140, 60)
        );
        valvulaContornoDer.getStyleClass().add(LINEA_VALVULA);

        Path lineaValvula = new Path(
                new MoveTo(120, 69),
                new LineTo(120, 50)
        );
        lineaValvula.getStyleClass().add(LINEA_VALVULA);

        simulacionPane.getChildren().addAll(
                valvulaContornoizq, valvulaContornoDer, lineaValvula, lineaInfValvula, dibujoValvula
        );
    }
    private void dibujarControlador() {
        Path lineaControlador = new Path(
                new MoveTo(485, 200), // Punto inicial
                new LineTo(550, 200) // Línea vertical hacia el controlador
        );
        Circle controlador = new Circle(550, 200, 25, Color.WHITE);
        Text textoLC = new Text(545, 205, "LC");

        Path lineaValvula = new Path(
                new MoveTo(550, 200), // Punto de la válvula
                new LineTo(550, 20),
                new LineTo(120, 20), // Línea horizontal hacia la válvula
                new LineTo(120, 37) // Línea vertical hacia la válvula
        );

        Path lineaVisor = new Path(
                new MoveTo(550, 200), // Punto de la válvula
                new LineTo(550, 235) // Línea horizontal hacia la válvula
        );
        Rectangle visorNivel = new Rectangle(530, 235, 40, 30);

        textoNivel = new Text(535, 255, String.format("%.2f", nivelActual));


        controlador.getStyleClass().add("controlador");
        textoLC.getStyleClass().add("texto-controlador");
        lineaControlador.getStyleClass().add("linea-controlador");
        lineaValvula.getStyleClass().add("linea-controlador");
        visorNivel.getStyleClass().add("visor-nivel");
        lineaVisor.getStyleClass().add(LINEA_VALVULA);
        textoNivel.getStyleClass().add("texto-nivel");

        simulacionPane.getChildren().addAll(lineaVisor, lineaValvula, lineaControlador, controlador, textoLC, visorNivel, textoNivel);
    }
    private void dibujarTransmisor() {
        // 1. Crear elementos gráficos
        Path tuberiaTransmisor = new Path(
                new MoveTo(383, 200), // Punto inicial
                new LineTo(460, 200)  // Línea horizontal hacia el transmisor
        );
        Circle transmisor = new Circle(460, 200, 25);
        Text textoLT = new Text(455, 205, "LT");

        // Aplicar estilos CSS
        transmisor.getStyleClass().add("transmisor");
        tuberiaTransmisor.getStyleClass().add("tuberia-transmisor");
        textoLT.getStyleClass().add("texto-transmisor");

        simulacionPane.getChildren().addAll(tuberiaTransmisor, transmisor, textoLT);
    }

    // --- Métodos de Animación ---
    private void crearAnimacionTanque() {

        animacionTanque = new AnimationTimer() {
            private long ultimoTiempo = 0;

            @Override
            public void handle(long ahora) {
                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }

                double segundosTranscurridos = (ahora - ultimoTiempo) / 1_000_000_000.0;

                ultimoTiempo = ahora;

                if (!simulacionActiva) {
                    stop(); // Detiene la animación si se detuvo toda la simulación
                    return;
                }

                double velocidad = 0.08;
                boolean actualizo = false;

                if ( modoLlenadoActivo && nivelActual < obtenerSetpoint()) {
                    nivelActual += velocidad * segundosTranscurridos;
                    nivelActual = Math.min(nivelActual, obtenerSetpoint());
                    actualizo = true;

                    if (nivelActual >= obtenerSetpoint()) {
                        modoLlenadoActivo = false;
                        modoVaciadoActivo = true;
                        valvula.cerrar(dibujoValvula); // Válvula cerrada si quieres
                    }
                } else {
                    if (nivelActual >= obtenerSetpoint()) {
                        modoLlenadoActivo = false;
                        valvula.cerrar(dibujoValvula);
                        pasoActual = 0; // Reiniciar el paso de la tubería
                        crearAnimacionVaciadoTuberiaEntrada();
                        animacionVaciadoTuberiaEntrada.start(); // Inicia la animación de llenado del tanque
                    }
                }

                if (consumoActivado && !tuberiaEnCurso) {
                    tuberiaEnCurso = true; // ✅ bloquea nuevas llamadas

                    dibujarAguaTuberiaEntrada();
                    crearAnimacionAguaTuberiaEntrada();
                    animacionTuberiaEntrada.start();
                }

                if (consumoActivado && modoVaciadoActivo && nivelActual > 0) {
                    nivelActual -= velocidad * segundosTranscurridos;
                    nivelActual = Math.max(nivelActual, 0);
                    actualizo = true;
                    tiempoTranscurrido += segundosTranscurridos;
                    System.out.println("tiempoTranscurrido: " + tiempoTranscurrido);

                    // aquí entra el control automático:
                    if (nivelActual <= obtenerSetpoint() - 0.1 && tuberiaEnCurso && tiempoTranscurrido > 3.8) {
                        System.out.println("Modo Llenado");
                        modoLlenadoActivo = true;
                        modoVaciadoActivo = false;
                        valvula.abrir(dibujoValvula); // Válvula de entrada abierta
                    }
                }

                if (actualizo) {
                    actualizarNivelAgua();
                    tiempoTotal += segundosTranscurridos;
                    serieNivel.getData().add(new XYChart.Data<>(tiempoTotal, nivelActual));
                }
            }
        };
    }
    private void crearAnimacionAguaTuberiaEntrada() {
        animacionTuberiaEntrada = new AnimationTimer() {
            private long ultimoTiempo = 0;

            @Override
            public void handle(long ahora) {
                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }
                // Calcular el tiempo transcurrido
                double tiempoTranscurrido = (ahora - ultimoTiempo) / 1_000_000_000.0;

                // intervalo en segundos
                double intervalo = 0.006;
                if (tiempoTranscurrido >= intervalo && pasoActual < caminoAgua.size()) {
                    aguaTuberiaEntrada.getElements().add(caminoAgua.get(pasoActual));
                    pasoActual++;
                    ultimoTiempo = ahora;
                }

                if (pasoActual >= caminoAgua.size()) {
                    if (primeraVez) {
                        stop();
                        primeraVez = false;
                        crearAnimacionTanque(); // Inicia la animación de llenado del tanque
                        animacionTanque.start(); // Inicia la animación de llenado del tanque
                    } else {
                        stop();
                        modoLlenadoActivo = true;
                        crearAnimacionVaciadoTuberiaEntrada();
                        animacionVaciadoTuberiaEntrada.start(); // Inicia la animación de vaciado de la tubería
                    }
                }
            }
        };
    }
    private void crearAnimacionVaciadoTuberiaEntrada() {
        animacionVaciadoTuberiaEntrada = new AnimationTimer() {
            private long ultimoTiempo = 0;

            @Override
            public void handle(long ahora) {
                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }

                double tiempoTranscurrido = (ahora - ultimoTiempo) / 1_000_000_000.0;
                double intervalo = 0.009;

                if (tiempoTranscurrido >= intervalo) {
                    ObservableList<PathElement> elementos = aguaTuberiaEntrada.getElements();

                    if (elementos.size() > 2) {
                        // Antes de eliminar el segundo (índice 1), convierte el siguiente en MoveTo
                        PathElement siguiente = elementos.get(2);
                        if (siguiente instanceof LineTo lineTo) {
                            elementos.set(2, new MoveTo(lineTo.getX(), lineTo.getY()));
                        } else if (siguiente instanceof CubicCurveTo curve) {
                            elementos.set(2, new MoveTo(curve.getX(), curve.getY()));
                        }
                        elementos.remove(1); // ahora sí elimina el actual
                    } else if (elementos.size() == 2) {
                        // Solo quedan MoveTo + otro → eliminar el otro
                        elementos.remove(1);
                    } else if (elementos.size() == 1 && elementos.get(0) instanceof MoveTo) {
                        elementos.clear(); // eliminar el último
                        stop();
                    }

                    ultimoTiempo = ahora;
                }
            }
        };
    }
    private void crearAnimacionAguaTuberiaSalida() {
        animacionTuberiaSalida = new AnimationTimer() {
            private long ultimoTiempo = 0;

            @Override
            public void handle(long ahora) {
                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }
                // Calcular el tiempo transcurrido
                double tiempoTranscurrido = (ahora - ultimoTiempo) / 1_000_000_000.0;

                // intervalo en segundos
                double intervalo = 0.009;
                if (tiempoTranscurrido >= intervalo && pasoActualSalida < caminoAgua.size()) {
                    aguaTuberiaSalida.getElements().add(caminoAgua.get(pasoActualSalida));
                    pasoActualSalida++;
                    ultimoTiempo = ahora;
                }

                if (pasoActualSalida >= caminoAgua.size()) {
                    stop();
                }
            }
        };
    }
    private void crearAnimacionVaciadoTuberiaSalida() {
        animacionVaciadoTuberiaSalida = new AnimationTimer() {
            private long ultimoTiempo = 0;

            @Override
            public void handle(long ahora) {
                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }

                double tiempoTranscurrido = (ahora - ultimoTiempo) / 1_000_000_000.0;
                double intervalo = 0.008;

                if (tiempoTranscurrido >= intervalo) {
                    ObservableList<PathElement> elementos = aguaTuberiaSalida.getElements();

                    if (elementos.size() > 2) {
                        // Antes de eliminar el segundo (índice 1), convierte el siguiente en MoveTo
                        PathElement siguiente = elementos.get(2);
                        if (siguiente instanceof LineTo lineTo) {
                            elementos.set(2, new MoveTo(lineTo.getX(), lineTo.getY()));
                        } else if (siguiente instanceof CubicCurveTo curve) {
                            elementos.set(2, new MoveTo(curve.getX(), curve.getY()));
                        }
                        elementos.remove(1); // ahora sí elimina el actual
                    } else if (elementos.size() == 2) {
                        // Solo quedan MoveTo + otro → eliminar el otro
                        elementos.remove(1);
                    } else if (elementos.size() == 1 && elementos.get(0) instanceof MoveTo) {
                        elementos.clear(); // eliminar el último
                        stop();
                    }

                    ultimoTiempo = ahora;
                }
            }
        };
    }

    private double obtenerSetpoint() {
        try {
            if (Validacion.validarRango(Double.parseDouble(setpointField.getText()), 0, 1)) {
                return Double.parseDouble(setpointField.getText());
            } else {
                Errores.mostrarError("El valor del setpoint debe estar entre 0 y 1.");
                setpointField.setText("1.0"); // Restablecer a valor por defecto
                return 1.0; // Valor por defecto si no es válido
            }
        } catch (NumberFormatException e) {
            Errores.mostrarError("Debe ingresar un numero");
            return 0; // Valor por defecto si no es válido
        }


    }
    private void actualizarNivelAgua() {
        double alturaMaxima = 220;        // Altura útil del tanque
        double nivelY = 320 - (nivelActual * alturaMaxima); // Coordenada Y del nivel actual

        aguaTanque.getElements().setAll(
                new MoveTo(222, 320),                          // Inferior izquierda
                new QuadCurveTo(317.5, 340, 378, 320),         // Curva inferior
                new LineTo(378, nivelY),                       // Lado derecho hacia arriba
                new LineTo(222, nivelY),                       // Lado superior
                new ClosePath()                                // Cierra el contorno
        );

        textoNivel.setText(String.format("%.2f", nivelActual)); // Actualiza el texto del nivel
    }
}
