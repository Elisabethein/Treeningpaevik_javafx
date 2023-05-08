module com.example.oop_v2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.oop_v2 to javafx.fxml;
    exports com.example.oop_v2;
}