package com.modelado.simulacion.controller;

import com.modelado.simulacion.model.Tanque;

import com.modelado.simulacion.model.Valvula;
import com.modelado.simulacion.utils.Errores;
import com.modelado.simulacion.utils.Validacion;
import com.modelado.simulacion.view.animations.TuberiaEntradaAnimacion;
import com.modelado.simulacion.view.animations.TuberiaEntradaVaciadoAnimacion;
import com.modelado.simulacion.view.components.*;
import javafx.animation.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;

import static com.modelado.simulacion.utils.Validacion.restringirSoloNumeros;

public class SimulacionController {
    boolean esValidoCambiarColor = true; // Variable de control de color de la valvula en consumo
    Tanque tanque = new Tanque(1.0); // Instancia del modelo Tanque
    Valvula valvula = new Valvula(); // Instancia del modelo Válvula

    private TanqueVisual tanqueVisual; // Instancia de la vista del tanque
    private ValvulaVisual valvulaVisual; // Instancia de la vista de la válvula
    private TuberiaEntradaVisual tuberiaEntradaVisual; // Instancia de la vista de la tubería de entrada
    private TuberiaSalidaVisual tuberiaSalidaVisual; // Instancia de la vista de la tubería de salida
    private ControladorVisual controladorVisual; // Instancia de la vista del controlador
    private TransmisorVisual transmisorVisual; // Instancia de la vista del transmisor
    private AguaTuberiaEntradaVisual aguaTuberiaEntradaVisual; // Instancia de la vista del agua en la tubería de entrada
    private AguaTuberiaSalidaVisual aguaTuberiaSalidaVisual; // Instancia de la vista del agua en la tubería de salida

    // --- Elementos de Simulación ---
    private Path aguaTanque;  // Representación gráfica del agua en el tanque
    private Path aguaTuberiaSalida; // Representación gráfica del agua en la tubería de salida
    private int pasoActual = 0; // Paso actual de la animación de la tubería
    private int pasoActualSalida = 0; // Paso actual de la animación de la tubería
    private double nivelActual = tanque.getNivel();  // Nivel actual del tanque (0-1)
    private boolean simulacionActiva = false;
    private boolean modoLlenadoActivo = false;
    private boolean modoVaciadoActivo = false;
    private boolean consumoActivado = false;
    private boolean primeraVez = true;
    private boolean primeraVezTanque = true; // Para evitar que el agua se dibuje varias veces
    private boolean tuberiaEnCurso = false; // Para evitar que el agua se dibuje varias veces

    private TuberiaEntradaAnimacion animacionTuberiaEntrada; // Animación para el agua en la tubería
    private TuberiaEntradaVaciadoAnimacion animacionVaciadoTuberiaEntrada; // Animación para el vaciado de la tubería
    private AnimationTimer animacionTanque; // Animación para el llenado del tanque
    private AnimationTimer animacionTuberiaSalida; // Animación para el agua en la tubería de salida
    private AnimationTimer animacionVaciadoTuberiaSalida; // Animación para el vaciado de la tubería de salida
    private XYChart.Series<Number, Number> serieNivel;
    private double tiempoTotal = 0; // tiempo simulado en segundos
    double tiempoTranscurrido = 0;

    @FXML
    NumberAxis ejeY = new NumberAxis();

    // --- Controles del FXML Existente ---
    @FXML
    private LineChart<Number, Number> grafica; // Gráfica para mostrar el nivel de agua
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
        ejeY.setTickUnit(0.1);
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
            valvula.abrir(valvulaVisual.getDibujoValvula());
            pasoActual = 0;
            aguaTuberiaEntradaVisual = new AguaTuberiaEntradaVisual(simulacionPane, nivelActual);
            animacionTuberiaEntrada = new TuberiaEntradaAnimacion(aguaTuberiaEntradaVisual, () -> {
                tuberiaEnCurso = false; // Permite nuevas llamadas
                modoLlenadoActivo = true;
                crearAnimacionTanque();
                animacionTanque.start(); // Inicia la animación del tanque
                if (consumoActivado) {
                    crearAnimacionVaciadoTuberiaSalida();
                    animacionVaciadoTuberiaSalida.start(); // Inicia la animación de vaciado del tanque
                }
            });
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
        valvula.cerrar(valvulaVisual.getDibujoValvula());
        animacionVaciadoTuberiaEntrada = new TuberiaEntradaVaciadoAnimacion(aguaTuberiaEntradaVisual, () -> {
            tuberiaEnCurso = false; // Permite nuevas llamadas
            modoLlenadoActivo = true;
            crearAnimacionTanque();
            animacionTanque.start(); // Inicia la animación del tanque
        });
        animacionVaciadoTuberiaEntrada.stop();
        crearAnimacionVaciadoTuberiaSalida();
        animacionVaciadoTuberiaSalida.stop();
        animacionVaciadoTuberiaEntrada.start();
    }
    @FXML
    public void handleEmpty() {
        consumoActivado = true;
        modoVaciadoActivo = true;
        valvula.abrir(valvulaVisual.getDibujoValvula());
        aguaTuberiaSalidaVisual = new AguaTuberiaSalidaVisual(simulacionPane);
        crearAnimacionAguaTuberiaSalida();
        animacionTuberiaSalida.start(); // Inicia la animación de vaciado del tanque
        emptyButton.setDisable(true); // Deshabilitar el botón de consumo
        emptyButton.setCursor(Cursor.DEFAULT);
    }

    private void dibujarSimulacion() {
        // Limpia el Pane antes de dibujar
        simulacionPane.getChildren().clear();

        tanqueVisual = new TanqueVisual(simulacionPane);
        valvulaVisual = new ValvulaVisual(simulacionPane);
        tuberiaEntradaVisual = new TuberiaEntradaVisual(simulacionPane);
        tuberiaSalidaVisual = new TuberiaSalidaVisual(simulacionPane);
        transmisorVisual = new TransmisorVisual(simulacionPane);
        controladorVisual = new ControladorVisual(simulacionPane, nivelActual);

        URL recurso = getClass().getResource("/com/modelado/simulacion/images/casa_tanque.png");
        assert recurso != null;
        Image imagen = new Image(recurso.toExternalForm());
        ImageView imageView = new ImageView(imagen);
        imageView.setLayoutX(413);
        imageView.setLayoutY(360);

        simulacionPane.getChildren().add(imageView);

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

                double velocidad = 0.07;
                boolean actualizo = false;
                double setpoint = obtenerSetpoint() + (primeraVezTanque ? 0 : 0.1); // Evita que el agua se dibuje varias veces

                if ( modoLlenadoActivo && nivelActual < setpoint) { // Si está activo el modo de llenado y el nivel del agua no ha llegado al nivel deseado (setpoint), entra a llenar.
                    nivelActual += velocidad * segundosTranscurridos; // Aumenta el nivel del agua segun la velocidad
                    nivelActual = Math.min(nivelActual, setpoint); // Asegura de que no exceda el setpoint
                    actualizo = true;

                    if (nivelActual >= setpoint) {
                        modoLlenadoActivo = false;
                        modoVaciadoActivo = true;
                        valvula.cerrar(valvulaVisual.getDibujoValvula()); // Válvula cerrada
                        primeraVezTanque = false;

                        animacionVaciadoTuberiaEntrada = new TuberiaEntradaVaciadoAnimacion(aguaTuberiaEntradaVisual, () -> {
                            valvula.cerrar(valvulaVisual.getDibujoValvula()); // Válvula cerrada
                        });
                        animacionVaciadoTuberiaEntrada.start(); // Inicia la animación de vaciado del tanque
                    }
                } else {
                    modoLlenadoActivo = false;
                    animacionVaciadoTuberiaEntrada = new TuberiaEntradaVaciadoAnimacion(aguaTuberiaEntradaVisual, () -> {
                        valvula.cerrar(valvulaVisual.getDibujoValvula()); // Válvula cerrada
                    });
                    animacionVaciadoTuberiaEntrada.start(); // Inicia la animación de llenado del tanque
                }

                if (consumoActivado && !tuberiaEnCurso) {
                    tuberiaEnCurso = true; // bloquea nuevas llamadas
                    aguaTuberiaEntradaVisual = new AguaTuberiaEntradaVisual(simulacionPane, nivelActual);
                    animacionTuberiaEntrada = new TuberiaEntradaAnimacion(aguaTuberiaEntradaVisual, () -> {
                        tuberiaEnCurso = false; // Permite nuevas llamadas
                        modoLlenadoActivo = true;
                        animacionTanque.start(); // Inicia la animación del tanque
                        if (consumoActivado) {
                            crearAnimacionVaciadoTuberiaSalida();
                            animacionVaciadoTuberiaSalida.start(); // Inicia la animación de vaciado del tanque
                        }
                    });
                    animacionTuberiaEntrada.start();
                }
                if (consumoActivado && modoVaciadoActivo) {
                    nivelActual -= velocidad * segundosTranscurridos;
                    nivelActual = Math.max(nivelActual, 0);
                    actualizo = true;
                    tiempoTranscurrido += segundosTranscurridos;
                    if (esValidoCambiarColor) {
                        valvula.abrir(valvulaVisual.getDibujoValvula()); // Válvula de entrada abierta
                        esValidoCambiarColor= false; // Cambia el color de la válvula
                    }
                    // aquí entra el control automático:
                    if (nivelActual <= obtenerSetpoint() - 0.1 && tuberiaEnCurso && tiempoTranscurrido > 4) {
                        modoLlenadoActivo = true;
                        modoVaciadoActivo = false;
                        valvula.abrir(valvulaVisual.getDibujoValvula()); // Válvula de entrada abierta
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
                if (tiempoTranscurrido >= intervalo && pasoActualSalida < aguaTuberiaSalidaVisual.getCaminoAguaSalida().size()) {
                    aguaTuberiaSalidaVisual.getAguaTuberiaSalida().getElements().add(aguaTuberiaSalidaVisual.getCaminoAguaSalida().get(pasoActualSalida));
                    pasoActualSalida++;
                    ultimoTiempo = ahora;
                }

                if (pasoActualSalida >= aguaTuberiaSalidaVisual.getCaminoAguaSalida().size()) {
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

        tanqueVisual.getAgua().getElements().setAll(
                new MoveTo(222, 320),                          // Inferior izquierda
                new QuadCurveTo(317.5, 340, 378, 320),         // Curva inferior
                new LineTo(378, nivelY),                       // Lado derecho hacia arriba
                new LineTo(222, nivelY),                       // Lado superior
                new ClosePath()                                // Cierra el contorno
        );

        controladorVisual.getTextoNivel().setText(String.format("%.2f", nivelActual)); // Actualiza el texto del nivel
    }
}
