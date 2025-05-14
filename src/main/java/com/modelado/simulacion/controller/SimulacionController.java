package com.modelado.simulacion.controller;

import com.modelado.simulacion.model.Tanque;

import com.modelado.simulacion.utils.Errores;
import com.modelado.simulacion.utils.Validacion;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

import static com.modelado.simulacion.utils.Validacion.restringirSoloNumeros;

public class SimulacionController {

    Tanque tanque = new Tanque(1.0); // Instancia del modelo Tanque
    private static final String CLASE_TUBERIA = "tuberia"; // Clase CSS para tuberías
    private static final String LINEA_VALVULA = "valvula-linea"; // Clase CSS para la línea de la válvula

    // --- Elementos de Simulación ---
    private Arc dibujoValvula; // Representación gráfica de la válvula
    private Path aguaTanque;  // Representación gráfica del agua en el tanque
    private Path aguaTuberia; // Representación gráfica del agua en la tubería
    private List<PathElement> caminoAgua;  // Camino completo que sigue el agua
    private int pasoActual = 0; // Paso actual de la animación de la tubería
    private double nivelActual = tanque.getNivel();  // Nivel actual del tanque (0-1)
    private boolean simulacionActiva = false;
    private AnimationTimer animacion; // Animación para el llenado del tanque
    private AnimationTimer animacionTuberia; // Animación para el agua en la tubería

    private AnimationTimer animacionVaciadoTuberia; // Animación para el vaciado de la tubería

    // --- Controles del FXML Existente ---
    @FXML
    private Pane simulacionPane; // Pane donde se dibuja la simulación
    @FXML
    private TextField setpointField; // Campo de texto para el setpoint
    @FXML
    public TextField levelMinField; // Campo de texto para el nivel mínimo
    @FXML
    private Button startButton; // Botón para iniciar la simulación
    @FXML
    private Button stopButton; // Botón para detener la simulación
    @FXML
    private Button emptyButton; // Botón para vaciar el tanque

    @FXML
    private void initialize() {
        restringirSoloNumeros(setpointField);
        restringirSoloNumeros(levelMinField);
        dibujarSimulacion();  // Dibuja el tanque, tuberías, etc.
        crearAnimacionLLenarTanque();     // Crea la animación para el llenado del tanque
        stopButton.setDisable(true); // Deshabilitar el botón de detener al inicio
        emptyButton.setDisable(true); // Deshabilitar el botón de vaciar al inicio
        startButton.setCursor(Cursor.HAND);
    }

    @FXML
    private void handleStart() {  // Debe coincidir exactamente
        if (obtenerSetpoint() != 0) {
            startButton.setDisable(true);
            startButton.setCursor(Cursor.DEFAULT);
            stopButton.setDisable(false);
            stopButton.setCursor(Cursor.HAND);
            emptyButton.setDisable(false);
            emptyButton.setCursor(Cursor.HAND);
            dibujoValvula.setFill(Color.GREEN); // Válvula abierta
            pasoActual = 0;
            dibujarAguaTuberia();
            crearAnimacionAguaTuberia();
            animacionTuberia.start(); // Solo inicia esto. Lo demás se activa automáticamente después.
        }
    }

    @FXML
    private void handleStop() {  // Debe coincidir exactamente
        simulacionActiva = false;
        startButton.setDisable(false);
        startButton.setCursor(Cursor.HAND);
        stopButton.setDisable(true);
        stopButton.setCursor(Cursor.DEFAULT);
        dibujoValvula.setFill(Color.RED); // Válvula cerrada
        pasoActual = caminoAgua.size(); // Por si quieres vaciar la tubería también
        crearAnimacionVaciadoTuberia();
        animacionVaciadoTuberia.start();
    }

    @FXML
    public void handleEmpty() {
        crearAnimacionVaciado();
        animacion.start();
    }
    private void dibujarSimulacion() {
        // Limpia el Pane antes de dibujar
        simulacionPane.getChildren().clear();

        // Controlador LC
        DoubleProperty levelMin = enlazarCampoConDouble(levelMinField, 0.5);
        simulacionPane.getChildren().add(dibujarControlador(levelMin));

        // Transmisor LT
        DoubleProperty setpoint = enlazarCampoConDouble(setpointField, 1.0);
        simulacionPane.getChildren().add(dibujarTransmisor(setpoint));

        dibujarTanque();
        dibujarTuberiaEntrada();
        dibujarValvula();
        dibujarTuberiaSalida();
        dibujarControlador(levelMin);
        dibujarTransmisor(setpoint);
    }
    private void dibujarAguaTuberia() {
        caminoAgua = new ArrayList<>();
        caminoAgua.add(new MoveTo(147, 47));

        // Tramo horizontal
        for (int x = 150; x <= 238; x += 1) {
            caminoAgua.add(new LineTo(x, 47));
        }

        // Curva: la simplificamos con dos segmentos
        caminoAgua.add(new QuadCurveTo(248, 47, 248, 57));

        // Tramo vertical largo: también lo dividimos
        for (int y = 70; y <= 315-nivelActual; y += 2) {
            caminoAgua.add(new LineTo(248, y));
        }
        // Path que se animará gradualmente
        aguaTuberia = new Path();
        aguaTuberia.getStyleClass().add("agua-tuberia");
        aguaTuberia.getElements().add(caminoAgua.get(0));

        simulacionPane.getChildren().addAll(aguaTuberia);
    }
    private void dibujarTanque() {
        Path contornoTanque = new Path();
        contornoTanque.getElements().addAll(
                new MoveTo(220, 80),   // Esquina superior izquierda (sin línea superior)
                new LineTo(220, 320),  // Lado izquierdo
                new QuadCurveTo(317.5, 340, 415, 320), // Curva inferior (centro en X, más abajo en Y)
                new LineTo(415, 80)    // Lado derecho
        );
        contornoTanque.getStyleClass().add("tanque"); // Aplicar estilo CSS
        // Agua dentro del tanque (inicialmente vacía)
        aguaTanque = new Path();
        aguaTanque.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.5)); // Azul semitransparente

        simulacionPane.getChildren().addAll(contornoTanque, aguaTanque);
    }
    private void dibujarTuberiaEntrada() {
        Path tuberiaValvulaSup = new Path(
                new MoveTo(15, 40), // Punto inicial (fuera del tanque)
                new LineTo(100, 40) // Línea vertical hacia la válvula
        );
        tuberiaValvulaSup.getStyleClass().add(CLASE_TUBERIA);

        Path tuberiaValvulaInf = new Path(
                new MoveTo(15, 55), // Punto inicial (fuera del tanque)
                new LineTo(100, 55) // Línea vertical hacia la válvula
        );
        tuberiaValvulaInf.getStyleClass().add(CLASE_TUBERIA);

        Rectangle aguaHastaValvula = new Rectangle(15, 41, 85, 13); // Posición inicial en la base
        aguaHastaValvula.setFill(Color.BLUE); // Azul semitransparente

        // Tubería de entrada superior con curva
        Path tuberiaEntradaSup = new Path();
        tuberiaEntradaSup.getElements().add(new MoveTo(140, 40));            // Punto inicial
        tuberiaEntradaSup.getElements().add(new LineTo(245, 40));            // Línea horizontal
        tuberiaEntradaSup.getElements().add(
                new QuadCurveTo(255, 40, 255, 50)                                // Curva hacia abajo
        );
        tuberiaEntradaSup.getElements().add(new LineTo(255, 70));            // Línea vertical final
        tuberiaEntradaSup.getStyleClass().add(CLASE_TUBERIA);

        // Tubería de entrada inferior con curva
        Path tuberiaEntradaInf = new Path();
        tuberiaEntradaInf.getElements().add(new MoveTo(140, 55));
        tuberiaEntradaInf.getElements().add(new LineTo(230, 55));
        tuberiaEntradaInf.getElements().add(
                new QuadCurveTo(240, 55, 240, 65)                                // Curva hacia abajo
        );
        tuberiaEntradaInf.getElements().add(new LineTo(240, 70));
        tuberiaEntradaInf.getStyleClass().add(CLASE_TUBERIA);



        simulacionPane.getChildren().addAll(
                tuberiaValvulaSup, tuberiaValvulaInf, tuberiaEntradaSup, tuberiaEntradaInf,
                aguaHastaValvula // Agua hasta la válvula
        );
    }
    private void dibujarTuberiaSalida() {
        // Tubería superior
        Path tuberiaSalidaSup = new Path();
        tuberiaSalidaSup.getElements().add(new MoveTo(418, 300));         // Punto inicial
        tuberiaSalidaSup.getElements().add(new LineTo(480, 300));         // Línea horizontal
        tuberiaSalidaSup.getElements().add(
                new QuadCurveTo(495, 300, 495, 315)                            // Curva de codo hacia abajo
        );
        tuberiaSalidaSup.getElements().add(new LineTo(495, 335));         // Línea vertical final
        tuberiaSalidaSup.getStyleClass().add(CLASE_TUBERIA);

        // Tubería inferior
        Path tuberiaSalidaInf = new Path();
        tuberiaSalidaInf.getElements().add(new MoveTo(418, 315));
        tuberiaSalidaInf.getElements().add(new LineTo(470, 315));
        tuberiaSalidaInf.getElements().add(
                new QuadCurveTo(480, 315, 480, 325)                            // Curva de codo hacia abajo
        );
        tuberiaSalidaInf.getElements().add(new LineTo(480, 335));
        tuberiaSalidaInf.getStyleClass().add(CLASE_TUBERIA);

        simulacionPane.getChildren().addAll(tuberiaSalidaSup, tuberiaSalidaInf);
    }
    private void dibujarValvula() {
        dibujoValvula = new Arc(
                120,       // centro X
                30,       // centro Y
                12,        // radio X
                12,        // radio Y
                0,         // ángulo inicial (grados)
                180        // extensión angular (180° = medio círculo)
        );
        dibujoValvula.getStyleClass().add("valvula"); // Aplicar estilo CSS
        dibujoValvula.setType(ArcType.OPEN); // Sin relleno entre los extremos

        Path lineaInfValvula = new Path(
                new MoveTo(108, 30), // Punto de la válvula
                new LineTo(132, 30) // Línea horizontal hacia la válvula
        );
        lineaInfValvula.getStyleClass().add(LINEA_VALVULA);

        Path valvulaContornoizq = new Path(
                new MoveTo(100, 40),
                new LineTo(100, 55),
                new LineTo(120, 49),
                new LineTo(100, 40)
        );
        valvulaContornoizq.getStyleClass().add(LINEA_VALVULA);

        Path valvulaContornoDer = new Path(
                new MoveTo(140, 40),
                new LineTo(140, 55),
                new LineTo(120, 49),
                new LineTo(140, 40)
        );
        valvulaContornoDer.getStyleClass().add(LINEA_VALVULA);

        Path lineaValvula = new Path(
                new MoveTo(120, 49),
                new LineTo(120, 30)
        );
        lineaValvula.getStyleClass().add(LINEA_VALVULA);

        simulacionPane.getChildren().addAll(
                valvulaContornoizq, valvulaContornoDer, lineaValvula, lineaInfValvula, dibujoValvula
        );
    }
    private Group dibujarControlador(DoubleProperty levelMin) {
        Path lineaControlador = new Path();
        Circle controlador = new Circle(570, 150, 25, Color.WHITE);
        Text textoLC = new Text(565, 155, "LC");

        controlador.getStyleClass().add("controlador");
        textoLC.getStyleClass().add("texto-controlador");
        lineaControlador.getStyleClass().add("linea-controlador");
        simulacionPane.getChildren().addAll(controlador, textoLC);

        levelMin.addListener((obs, oldVal, newVal) -> {
            // Calcular posición Y (0 = abajo, 1 = arriba)
            double yPos = calcularPosicionY(newVal.doubleValue());

            // Actualizar tubería
            lineaControlador.getElements().setAll(
                    new MoveTo(415, yPos),
                    new LineTo(570, yPos)
            );

            // Actualizar transmisor y texto
            controlador.setCenterY(yPos);
            textoLC.setY(yPos + 5);
        });

        // 4. Posición inicial (valor por defecto 1)
        double yInicial = calcularPosicionY(levelMin.get());
        lineaControlador.getElements().addAll(
                new MoveTo(415, yInicial),
                new LineTo(570, yInicial)
        );
        controlador.setCenterY(yInicial);
        textoLC.setY(yInicial + 5);

        return new Group(lineaControlador, controlador, textoLC);
    }
    private Group dibujarTransmisor(DoubleProperty setpointProperty) {
        // 1. Crear elementos gráficos
        Path tuberiaTransmisor = new Path();
        Circle transmisor = new Circle(480, 0, 25); // Posición Y temporal
        Text textoLT = new Text(475, 0, "LT"); // Posición Y temporal

        // Aplicar estilos CSS
        transmisor.getStyleClass().add("transmisor");
        tuberiaTransmisor.getStyleClass().add("tuberia-transmisor");
        textoLT.getStyleClass().add("texto-transmisor");

        // 3. Listener para actualizar posición cuando cambia el setpoint
        setpointProperty.addListener((obs, oldVal, newVal) -> {
            // Calcular posición Y (0 = abajo, 1 = arriba)
            double yPos = calcularPosicionY(newVal.doubleValue());

            // Actualizar tubería
            tuberiaTransmisor.getElements().setAll(
                    new MoveTo(415, yPos),
                    new LineTo(480, yPos)
            );

            // Actualizar transmisor y texto
            transmisor.setCenterY(yPos);
            textoLT.setY(yPos + 5);
        });

        // 4. Posición inicial (valor por defecto 1)
        double yInicial = calcularPosicionY(setpointProperty.get());
        tuberiaTransmisor.getElements().addAll(
                new MoveTo(415, yInicial),
                new LineTo(480, yInicial)
        );
        transmisor.setCenterY(yInicial);
        textoLT.setY(yInicial + 5);

        return new Group(tuberiaTransmisor, transmisor, textoLT);
    }
    private double calcularPosicionY(double setpoint) {
        // Asegurar que el setpoint esté entre 0 y 1
        double nivel = Math.max(0, Math.min(1, setpoint));
        // Coordenadas del tanque
        double ySuperior = 80;   // Parte superior del tanque
        double yInferior = 320;  // Parte inferior del tanque
        // Calcular posición Y (invertido porque en JavaFX Y aumenta hacia abajo)
        return yInferior - (nivel * (yInferior - ySuperior));
    }
    private DoubleProperty enlazarCampoConDouble(TextField campo, double valorPorDefecto) {
        DoubleProperty property = new SimpleDoubleProperty(valorPorDefecto);

        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                property.set(Double.parseDouble(newVal));
            } catch (NumberFormatException e) {
                property.set(valorPorDefecto);
            }
        });
        return property;
    }

    // --- Métodos de Animación ---
    private void crearAnimacionLLenarTanque() {
        animacion = new AnimationTimer() {
            private long ultimoTiempo = 0;

            @Override
            public void handle(long ahora) {
                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }

                double segundosTranscurridos = (ahora - ultimoTiempo) / 1_000_000_000.0;
                ultimoTiempo = ahora;

                if (simulacionActiva) {
                    // Aumentar el nivel del agua
                    // % por segundo
                    double velocidadLlenado = 0.1;
                    nivelActual += velocidadLlenado * segundosTranscurridos;

                    // Limitar al máximo (100%) y al setpoint
                    double setpoint = obtenerSetpoint();
                    nivelActual = Math.min(nivelActual, setpoint);

                    actualizarNivelAgua();
                }

                if (nivelActual >= 1.0 || nivelActual >= obtenerSetpoint()) {
                    // Detener la animación si el nivel alcanza el máximo o el setpoint
                    stop();
                    simulacionActiva = false;
                    startButton.setDisable(false);
                    startButton.setCursor(Cursor.HAND);
                    stopButton.setDisable(true);
                    stopButton.setCursor(Cursor.DEFAULT);
                    dibujoValvula.setFill(Color.RED); // Válvula cerrada
                    pasoActual = caminoAgua.size(); // Por si quieres vaciar la tubería también
                    crearAnimacionVaciadoTuberia();
                    animacionVaciadoTuberia.start();
                }
            }
        };
    }

    private void crearAnimacionVaciado() {
        animacion = new AnimationTimer() {
            private long ultimoTiempo = 0;

            @Override
            public void handle(long ahora) {
                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }

                double segundosTranscurridos = (ahora - ultimoTiempo) / 1_000_000_000.0;
                ultimoTiempo = ahora;

                if (!simulacionActiva) { // Asegura que no estamos en modo llenado
                    // Velocidad de vaciado (% por segundo)
                    double velocidadVaciado = 0.1;
                    nivelActual -= velocidadVaciado * segundosTranscurridos;

                    // Limitar al mínimo (0%)
                    nivelActual = Math.max(nivelActual, 0);

                    actualizarNivelAgua();
                }

                if (nivelActual <= 0) {
                    stop();
                    simulacionActiva = false;
                    startButton.setDisable(false);
                    startButton.setCursor(Cursor.HAND);
                    stopButton.setDisable(true);
                    stopButton.setCursor(Cursor.DEFAULT);
                    dibujoValvula.setFill(Color.RED); // Válvula cerrada
                }
            }
        };
    }

    private void crearAnimacionAguaTuberia() {
        animacionTuberia = new AnimationTimer() {
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
                double intervalo = 0.008;
                if (tiempoTranscurrido >= intervalo && pasoActual < caminoAgua.size()) {
                    aguaTuberia.getElements().add(caminoAgua.get(pasoActual));
                    pasoActual++;
                    ultimoTiempo = ahora;
                }

                if (pasoActual >= caminoAgua.size()) {
                    stop();

                    simulacionActiva = true;
                    animacion.start();
                }
            }
        };
    }

    private void crearAnimacionVaciadoTuberia() {
        animacionVaciadoTuberia = new AnimationTimer() {
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
                    ObservableList<PathElement> elementos = aguaTuberia.getElements();

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

    private double obtenerminimo() {
        try {
            if (Validacion.validarRango(Double.parseDouble(levelMinField.getText()), 0, 1)) {
                return Double.parseDouble(levelMinField.getText());
            } else {
                Errores.mostrarError("El valor ingresado excede la altura maxima");
                return 0;
            }
        } catch (NumberFormatException e) {
            Errores.mostrarError("Debe ingresar un valor");
            return 0;
        }
    }

    private void actualizarNivelAgua() {
        double alturaMaxima = 240;        // Altura útil del tanque
        double nivelY = 320 - (nivelActual * alturaMaxima); // Coordenada Y del nivel actual

        aguaTanque.getElements().setAll(
                new MoveTo(222, 320),                          // Inferior izquierda
                new QuadCurveTo(317.5, 340, 413, 320),         // Curva inferior
                new LineTo(413, nivelY),                       // Lado derecho hacia arriba
                new LineTo(222, nivelY),                       // Lado superior
                new ClosePath()                                // Cierra el contorno
        );
    }
}
