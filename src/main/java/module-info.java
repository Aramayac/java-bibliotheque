module com.example.livrehome {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires jbcrypt;
    requires java.naming;

    opens com.example.livrehome.model to javafx.base, org.hibernate.orm.core;
    opens com.example.livrehome to javafx.fxml;
    opens com.example.livrehome.controller to javafx.fxml;
    exports com.example.livrehome;
    exports com.example.livrehome.controller;
}