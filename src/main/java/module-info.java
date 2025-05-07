module com.modelado.simulacion {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.modelado.simulacion to javafx.fxml;
    exports com.modelado.simulacion;

    // Exporta el paquete donde está el controlador
    exports com.modelado.simulacion.controller to javafx.fxml;
    // Si usas FXML, también abre el paquete
    opens com.modelado.simulacion.controller to javafx.fxml;
}