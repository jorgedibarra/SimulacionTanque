package com.modelado.simulacion.controller;

import com.modelado.simulacion.model.Grafica;
import com.modelado.simulacion.model.Tanque;

import com.modelado.simulacion.model.Valvula;
import com.modelado.simulacion.utils.Errores;
import com.modelado.simulacion.utils.Validacion;
import com.modelado.simulacion.view.animations.*;
import com.modelado.simulacion.view.components.*;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

import java.net.URL;

import static com.modelado.simulacion.utils.Validacion.restringirSoloNumeros;

public class SimulacionController {

    Tanque tanque = new Tanque(1.0); // Instancia del modelo Tanque
    Valvula valvula = new Valvula(); // Instancia del modelo Válvula
    Grafica grafica; // Instancia del modelo Gráfica

    private TanqueVisual tanqueVisual; // Instancia de la vista del tanque
    private ValvulaVisual valvulaVisual; // Instancia de la vista de la válvula
    private TuberiaEntradaVisual tuberiaEntradaVisual; // Instancia de la vista de la tubería de entrada
    private TuberiaSalidaVisual tuberiaSalidaVisual; // Instancia de la vista de la tubería de salida
    private ControladorVisual controladorVisual; // Instancia de la vista del controlador
    private TransmisorVisual transmisorVisual; // Instancia de la vista del transmisor
    private AguaTuberiaEntradaVisual aguaTuberiaEntradaVisual; // Instancia de la vista del agua en la tubería de entrada
    private AguaTuberiaSalidaVisual aguaTuberiaSalidaVisual; // Instancia de la vista del agua en la tubería de salida

    // --- Elementos de Simulación ---
    private TanqueLLenadoAnimacion tanqueLLenadoAnimacion;
    private TanqueVaciadoAnimacion tanqueVaciadoAnimacion; // Animación de llenado y vaciado del tanque
    private TuberiaSalidaAnimacion tuberiaSalidaAnimacion; // Animación de la tubería de salida
    private TuberiaEntradaAnimacion tuberiaEntradaAnimacion; // Animación de la tubería de entrada
    private TuberiaEntradaVaciadoAnimacion tuberiaEntradaVaciadoAnimacion; // Animación de la tubería de entrada al vaciar
    private double nivelActual = tanque.getNivel();  // Nivel actual del tanque (0-1)

    private final PauseTransition pausa = new PauseTransition(Duration.seconds(0.7)); // Pausa para la animación

    private boolean consumoActivo = false; // Flag para el consumo activo
    private boolean primeraVez = true; // Flag para el primer llenado
    private XYChart.Series<Number, Number> serieNivel;

    // --- Controles del FXML Existente ---
    @FXML
    private LineChart<Number, Number> graficaPunto; // Gráfica para mostrar el nivel de agua
    @FXML
    NumberAxis ejeY = new NumberAxis();
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
    private Button resetButton; // Botón para reiniciar la simulación

    @FXML
    private void initialize() {
        restringirSoloNumeros(setpointField); // Restringir el campo a solo números
        dibujarSimulacion();  // Dibuja el tanque, tuberías, etc.
        configurarGrafica(); // Configurar la gráfica
        configurarBotones(-1); // Configurar los botones al inicio
    }

    @FXML
    private void handleStart() {
        if (obtenerSetpoint() != 0) {
            configurarBotones(0); // Cambiar el estado de los botones
            valvula.abrir(valvulaVisual.getDibujoValvula()); // Abrir la válvula
            tuberiaEntradaAnimacion = new TuberiaEntradaAnimacion(aguaTuberiaEntradaVisual, () -> {

                if (primeraVez) {
                    primeraVez = false; // Cambiar el flag para el primer llenado
                    tanqueLLenadoAnimacion = new TanqueLLenadoAnimacion(tanqueVisual, () -> {
                        valvula.cerrar(valvulaVisual.getDibujoValvula());
                        tuberiaEntradaVaciadoAnimacion = new TuberiaEntradaVaciadoAnimacion(aguaTuberiaEntradaVisual, () -> {

                        });
                        tuberiaEntradaVaciadoAnimacion.start();
                    }, grafica, controladorVisual.getTextoNivel());
                    tanqueLLenadoAnimacion.configurar(obtenerSetpoint() - 0.1); // Configurar el llenado del tanque
                    tanqueLLenadoAnimacion.start();
                }
            });
            tuberiaEntradaAnimacion.start();
        }
    }

    @FXML
    private void handleStop() {
        configurarBotones(1);
        valvula.cerrar(valvulaVisual.getDibujoValvula());
        tanqueLLenadoAnimacion.stop(); // Detener la animación de llenado
        tanqueVaciadoAnimacion.stop(); // Detener la animación de vaciado
        tuberiaEntradaAnimacion.stop(); // Detener la animación de la tubería de entrada
        tuberiaSalidaAnimacion.stop(); // Detener la animación de la tubería de salida
        tuberiaEntradaVaciadoAnimacion.stop(); // Detener la animación de la tubería de entrada al vaciar
    }

    @FXML
    public void handleEmpty() {
        valvula.abrir(valvulaVisual.getDibujoValvula());
        configurarBotones(2);
        if (!consumoActivo) {
            consumoActivo = true; // Activar el consumo
            iniciarConsumo(); // Iniciar el consumo de agua
        }
    }

    @FXML
    public void handleReset() {
        // 1. Detener todas las animaciones en curso
        detenerTodasLasAnimaciones();
        // 2. Reiniciar variables de estado
        reiniciarVariables();
        // 3. Restablecer componentes visuales
        reiniciarComponentesVisuales();
        // 4. Reiniciar la gráfica
        reiniciarGrafica();
        // 5. Restablecer controles
        configurarBotones(-1); // Estado inicial
    }

    private void iniciarConsumo() {
        tanqueVaciadoAnimacion = new TanqueVaciadoAnimacion(tanqueVisual, this::vaciadoCompleto, grafica, controladorVisual.getTextoNivel());
        tanqueVaciadoAnimacion.configurar(obtenerSetpoint());
        tanqueVaciadoAnimacion.start();
        tanqueLLenadoAnimacion = new TanqueLLenadoAnimacion(tanqueVisual, this::llenadoCompleto, grafica, controladorVisual.getTextoNivel());
        tuberiaEntradaAnimacion = new TuberiaEntradaAnimacion(aguaTuberiaEntradaVisual, () -> valvula.cerrar(valvulaVisual.getDibujoValvula()));
        tuberiaEntradaAnimacion.start();

        tuberiaSalidaAnimacion = new TuberiaSalidaAnimacion(aguaTuberiaSalidaVisual, () -> tuberiaSalidaAnimacion.stop());
        tuberiaSalidaAnimacion.start();
    }

    private void llenadoCompleto() {
        pausa.setOnFinished(event -> {
            valvula.abrir(valvulaVisual.getDibujoValvula());
            tuberiaEntradaAnimacion.reiniciar();
            tuberiaEntradaAnimacion.start();
        });
        pausa.play();
        tanqueVaciadoAnimacion.configurar(obtenerSetpoint());
        tanqueVaciadoAnimacion.start();
        valvula.cerrar(valvulaVisual.getDibujoValvula());

    }

    private void vaciadoCompleto() {
        valvula.abrir(valvulaVisual.getDibujoValvula());
        tanqueLLenadoAnimacion.configurar(obtenerSetpoint());
        tanqueLLenadoAnimacion.start();
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
        aguaTuberiaEntradaVisual = new AguaTuberiaEntradaVisual(simulacionPane,nivelActual);
        aguaTuberiaSalidaVisual = new AguaTuberiaSalidaVisual(simulacionPane);

        URL recurso = getClass().getResource("/com/modelado/simulacion/images/casa_tanque.png");
        assert recurso != null;
        Image imagen = new Image(recurso.toExternalForm());
        ImageView imageView = new ImageView(imagen);
        imageView.setLayoutX(413);
        imageView.setLayoutY(360);
        simulacionPane.getChildren().add(imageView);

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

    private void configurarGrafica() {
        serieNivel = new XYChart.Series<>(); // Serie de datos para la gráfica
        graficaPunto.getData().add(serieNivel);
        grafica = new Grafica(serieNivel); // Instancia de la gráfica
        graficaPunto.setLegendVisible(false); // Ocultar la leyenda
        ejeY.setTickUnit(0.1); // Establecer la unidad de tick del eje Y
    }

    private void configurarBotones(int estado) {
        switch (estado) {
            case 0 -> {
                startButton.setDisable(true);
                startButton.setCursor(Cursor.DEFAULT);
                stopButton.setDisable(false);
                stopButton.setCursor(Cursor.HAND);
                emptyButton.setDisable(false);
                emptyButton.setCursor(Cursor.HAND);
            }
            case 1 -> {
                startButton.setDisable(false);
                startButton.setCursor(Cursor.HAND);
                stopButton.setDisable(true);
                stopButton.setCursor(Cursor.DEFAULT);
                emptyButton.setDisable(true);
                emptyButton.setCursor(Cursor.DEFAULT);
                resetButton.setDisable(false);
                resetButton.setCursor(Cursor.HAND);
            }
            case 2 -> {
                startButton.setDisable(true);
                startButton.setCursor(Cursor.DEFAULT);
                stopButton.setDisable(false);
                stopButton.setCursor(Cursor.HAND);
                emptyButton.setDisable(true);
                emptyButton.setCursor(Cursor.DEFAULT);
            }
            default -> {
                stopButton.setDisable(true); // Deshabilitar el botón de detener al inicio
                emptyButton.setDisable(true); // Deshabilitar el botón de vaciar al inicio
                resetButton.setDisable(true);
                startButton.setCursor(Cursor.HAND); // Cambiar el cursor al iniciar
            }
        }
    }

    private void detenerTodasLasAnimaciones() {
        if (tanqueLLenadoAnimacion != null) tanqueLLenadoAnimacion.stop();
        if (tanqueVaciadoAnimacion != null) tanqueVaciadoAnimacion.stop();
        if (tuberiaEntradaAnimacion != null) tuberiaEntradaAnimacion.stop();
        if (tuberiaSalidaAnimacion != null) tuberiaSalidaAnimacion.stop();
        if (tuberiaEntradaVaciadoAnimacion != null) tuberiaEntradaVaciadoAnimacion.stop();
    }

    private void reiniciarVariables() {
        nivelActual = 0;
        consumoActivo = false;
        primeraVez = true;
    }

    private void reiniciarComponentesVisuales() {
        // Limpiar y volver a dibujar todos los componentes
        simulacionPane.getChildren().clear();
        dibujarSimulacion();

        // Reiniciar textos y estados visuales
        controladorVisual.actualizarNivelTexto(0);
        setpointField.setText("0"); // Valor por defecto
    }

    private void reiniciarGrafica() {
        serieNivel.getData().clear();
        // 2. Remover y volver a agregar la serie al gráfico
        graficaPunto.getData().remove(serieNivel);
        serieNivel = new XYChart.Series<>(); // Crear nueva instancia
        graficaPunto.getData().add(serieNivel);
        grafica = new Grafica(serieNivel); // Reiniciar la instancia de gráfica
    }
}
