module com.example.cherryrero {
     requires jlayer;
    requires static  lombok;
     requires javafx.fxml;
    requires javafx.controls;
    requires java.logging;


    opens com.example.cherryrero to javafx.fxml;
    exports com.example.cherryrero;
}