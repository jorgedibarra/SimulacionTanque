package com.modelado.simulacion.utils;

import javafx.scene.control.TextField;

public class Validacion {
    public static boolean validarRango(double valor, double min, double max) {
        return valor >= min && valor <= max;
    }

    public static void restringirSoloNumeros(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("0(\\.\\d*)?|1(\\.0*)?")) { // Solo dígitos
                textField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

}
