module com.example.cherryrero {
    requires javafx.controls;
    requires javafx.fxml;
    requires jlayer;


    opens com.example.cherryrero to javafx.fxml;
    exports com.example.cherryrero;
}