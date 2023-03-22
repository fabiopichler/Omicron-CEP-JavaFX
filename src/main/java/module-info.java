module net.fabiopichler.omicroncepjavafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires org.json;
    requires java.sql;

    opens net.fabiopichler.omicroncepjavafx to javafx.fxml;
    exports net.fabiopichler.omicroncepjavafx;
    exports net.fabiopichler.omicroncepjavafx.gui;
    opens net.fabiopichler.omicroncepjavafx.gui to javafx.fxml;
    exports net.fabiopichler.omicroncepjavafx.core;
    opens net.fabiopichler.omicroncepjavafx.core to javafx.fxml;
}
