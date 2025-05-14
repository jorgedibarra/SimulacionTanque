package com.modelado.simulacion.utils;

import javafx.scene.control.Alert;

public class Errores {
    public static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Valor inválido");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }
}
