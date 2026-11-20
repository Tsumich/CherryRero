module com.example.cherryrero {
    requires javafx.controls;
    requires javafx.fxml;
    requires jlayer;
    requires static lombok;
    requires java.desktop;
    requires java.logging;

    opens com.example.cherryrero to javafx.fxml;
    exports com.example.cherryrero;
}
