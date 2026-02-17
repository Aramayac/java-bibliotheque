package com.example.livrehome;

import com.example.livrehome.Util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale de l'application Bibliothèque
 * Point d'entrée JavaFX
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger l'écran de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root, 600, 400);

            // Configurer la fenêtre
            primaryStage.setTitle("Bibliothèque Municipale - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();

            System.out.println("✓ Application démarrée avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur de démarrage de l'application:");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Fermer proprement Hibernate à l'arrêt de l'application
        System.out.println("\n🛑 Arrêt de l'application...");
        HibernateUtil.shutdown();
        System.out.println("✓ Application arrêtée proprement");
    }

    public static void main(String[] args) {
        // Vérifier Hibernate avant de lancer l'interface
        try {
            System.out.println("🚀 Démarrage de l'application Bibliothèque...\n");

            // Tester la connexion Hibernate
            if (HibernateUtil.isInitialized()) {
                System.out.println("✓ Connexion à la base de données OK\n");
            } else {
                System.err.println("❌ Échec de la connexion à la base de données");
                System.err.println("Veuillez vérifier:");
                System.err.println("1. MySQL est démarré");
                System.err.println("2. La base 'bibliotheque_db' existe");
                System.err.println("3. Les identifiants dans hibernate.cfg.xml sont corrects\n");
                System.exit(1);
            }

            // Lancer l'application JavaFX
            launch(args);

        } catch (Exception e) {
            System.err.println("❌ Erreur critique au démarrage:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
