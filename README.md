OPEN JDK 20

используемые модули JAVA SDK 21.0.13.1
favafx.fxml
favafx.controls

команда для создания инсталятора
jpackage --type msi --app-version "1.0" --input . --dest . --main-jar CherryRero.jar --main-class com.example.cherryrero.HelloApplication --module-path "C:\Program Files\Java\javafx-jmods-21.0.13.1" --add-modules javafx.controls,javafx.fxml --win-shortcut --win-menu --name "Mycherry"
