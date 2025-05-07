package com.modelado.simulacion.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

public class SimulacionController {
    // --- Elementos de Simulación ---
    @FXML
    private Pane simulacionPane;
    private Rectangle agua;
    private Circle valvula;

    // --- Controles del FXML Existente ---
    @FXML
    private TextField setpointField;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private LineChart<Number, Number> grafica;

    @FXML
    private void initialize() {
        dibujarSimulacion();  // Dibuja el tanque, tuberías, etc.
        configurarEventos();   // Configura botones/sliders
    }

    @FXML
    private void handleStart() {  // Debe coincidir exactamente
        // Lógica del botón
    }

    @FXML
    private void handleStop() {  // Debe coincidir exactamente
        // Lógica del botón
    }

    private void dibujarSimulacion() {
        // Limpia el Pane antes de dibujar
        simulacionPane.getChildren().clear();

        // 1. Tanque
        Rectangle tanque = new Rectangle(200, 50, 200, 300);
        tanque.setFill(Color.LIGHTGRAY);
        tanque.setStroke(Color.BLACK);

        // 2. Agua
        agua = new Rectangle(200, 350, 200, 0);
        agua.setFill(Color.BLUE);

        // 3. Tubería de entrada + válvula
        Path tuberiaEntrada = new Path(
                new MoveTo(50, 150), new LineTo(150, 150),
                new LineTo(150, 200), new LineTo(200, 200)
        );
        tuberiaEntrada.setStroke(Color.BLACK);
        tuberiaEntrada.setStrokeWidth(8);

        valvula = new Circle(150, 175, 12, Color.RED);
        valvula.setStroke(Color.BLACK);

        // 4. Tubería de salida
        Path tuberiaSalida = new Path(
                new MoveTo(400, 250), new LineTo(450, 250),
                new LineTo(450, 300), new LineTo(500, 300)
        );
        tuberiaSalida.setStroke(Color.BLACK);
        tuberiaSalida.setStrokeWidth(8);

        // 5. Transmisor LT
        Circle transmisor = new Circle(350, 150, 25, Color.WHITE);
        transmisor.setStroke(Color.BLACK);
        Text textoLT = new Text(340, 155, "LT");

        simulacionPane.getChildren().addAll(
                tanque, agua, tuberiaEntrada, valvula,
                tuberiaSalida, transmisor, textoLT
        );
    }

    private void configurarEventos() {
        // Botón Iniciar/DETENER
        startButton.setOnAction(e -> iniciarSimulacion());
        stopButton.setOnAction(e -> detenerSimulacion());

    }

    private void iniciarSimulacion() {
        try {
            double setpoint = Double.parseDouble(setpointField.getText());
            // Aquí iría la lógica del control PID (si es necesario)
            actualizarNivelAgua(setpoint * 0.5);  // Ejemplo simplificado
        } catch (NumberFormatException e) {
            setpointField.setStyle("-fx-border-color: red;");
        }


    }
    // Método para actualizar la gráfica
    private void actualizarGrafica(double tiempo, double nivel) {
        grafica.getData().get(0).getData().add(new XYChart.Data<>(tiempo, nivel));
    }
    private void detenerSimulacion() {
        // Detener cualquier animación en curso
    }

    // Métodos para actualizar la vista
    public void actualizarNivelAgua(double nivel) {
        agua.setHeight(nivel * 3);
        agua.setY(350 - nivel * 3);
    }

    public void setValvulaAbierta(boolean abierta) {
        valvula.setFill(abierta ? Color.GREEN : Color.RED);
    }
}
