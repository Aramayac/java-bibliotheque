package com.example.livrehome.Util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

// Import explicite des entités
import com.example.livrehome.model.Utilisateur;
import com.example.livrehome.model.Livre;
import com.example.livrehome.model.Categorie;
import com.example.livrehome.model.Adherent;
import com.example.livrehome.model.Emprunt;

public class HibernateUtil {

    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;

    static {
        try {
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("Initialisation de Hibernate...");
            System.out.println("═══════════════════════════════════════════════════");

            // Charger la configuration XML
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

            // Ajout explicite des classes annotées
            configuration.addAnnotatedClass(Utilisateur.class);
            configuration.addAnnotatedClass(Livre.class);
            configuration.addAnnotatedClass(Categorie.class);
            configuration.addAnnotatedClass(Adherent.class);
            configuration.addAnnotatedClass(Emprunt.class);

            // Créer le registre de services
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            // Créer la SessionFactory
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);

            System.out.println("✓ SessionFactory créée avec succès");
            System.out.println("✓ Connexion à la base de données établie");
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("Hibernate initialisé avec succès!");
            System.out.println("═══════════════════════════════════════════════════\n");

        } catch (Throwable ex) {
            System.err.println("❌ ERREUR CRITIQUE: Échec de l'initialisation de Hibernate");
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException("SessionFactory non initialisée.");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        if (serviceRegistry != null) {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
        }
    }

    // Méthode main pour tester HibernateUtil
    public static void main(String[] args) {
        System.out.println("\n🧪 TEST DE HIBERNATEUTIL\n");

        try {
            if (isInitialized()) {
                System.out.println("✅ SUCCÈS - Hibernate est initialisé\n");
            } else {
                System.out.println("❌ ÉCHEC - Hibernate n'est pas initialisé\n");
                return;
            }

            // Afficher les entités mappées
            System.out.println("Entités mappées :");
            sessionFactory.getMetamodel().getEntities().forEach(entity -> {
                System.out.println("  - " + entity.getName());
            });

        } catch (Exception e) {
            System.err.println("\n❌ ERREUR LORS DES TESTS:");
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    public static boolean isInitialized() {
        return sessionFactory != null && !sessionFactory.isClosed();
    }
}