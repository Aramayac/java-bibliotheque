package com.example.livreshome.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.security.SecureRandom;
import java.util.logging.Logger;

public class SecurityUtil {

    private static final Logger LOGGER = Logger.getLogger(SecurityUtil.class.getName());
    private static final int BCRYPT_COST = 12;
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(BCRYPT_COST);

    // Hashage du mot de passe
    public static String hashPassword(String motDePasseClair) {
        if (motDePasseClair == null || motDePasseClair.isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }
        return encoder.encode(motDePasseClair);
    }

    // Validation du mot de passe
    public static boolean validatePassword(String motDePasseClair, String motDePasseHashe) {
        if (motDePasseClair == null || motDePasseHashe == null) return false;
        return encoder.matches(motDePasseClair, motDePasseHashe);
    }

    // Génération d’un mot de passe temporaire
    public static String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    // Vérification de la robustesse du mot de passe
    public static PasswordStrength checkPasswordStrength(String motDePasse) {
        if (motDePasse == null || motDePasse.isEmpty()) {
            return new PasswordStrength(PasswordStrength.Niveau.TRES_FAIBLE, "Mot de passe vide");
        }

        int score = 0;
        StringBuilder details = new StringBuilder();

        if (motDePasse.length() >= 8) score++; else details.append("- Minimum 8 caractères requis\n");
        if (motDePasse.length() >= 12) score++;
        if (motDePasse.matches(".*[A-Z].*")) score++; else details.append("- Doit contenir au moins une majuscule\n");
        if (motDePasse.matches(".*[a-z].*")) score++; else details.append("- Doit contenir au moins une minuscule\n");
        if (motDePasse.matches(".*[0-9].*")) score++; else details.append("- Doit contenir au moins un chiffre\n");

        // ✅ Regex simplifiée pour les caractères spéciaux
        if (motDePasse.matches(".*[^a-zA-Z0-9].*")) {
            score++;
        } else {
            details.append("- Doit contenir au moins un caractère spécial\n");
        }

        PasswordStrength.Niveau niveau;
        switch (score) {
            case 0: case 1: niveau = PasswordStrength.Niveau.TRES_FAIBLE; break;
            case 2: case 3: niveau = PasswordStrength.Niveau.FAIBLE; break;
            case 4: niveau = PasswordStrength.Niveau.MOYEN; break;
            case 5: niveau = PasswordStrength.Niveau.BON; break;
            default: niveau = PasswordStrength.Niveau.TRES_BON;
        }

        return new PasswordStrength(niveau, details.toString());
    }

    // Classe interne pour représenter la robustesse du mot de passe
    public static class PasswordStrength {
        public enum Niveau {
            TRES_FAIBLE("#D32F2F"),
            FAIBLE("#F57C00"),
            MOYEN("#FBC02D"),
            BON("#7CB342"),
            TRES_BON("#388E3C");

            private final String color;
            Niveau(String color) { this.color = color; }
            public String getColor() { return color; }
        }

        private final Niveau niveau;
        private final String details;

        public PasswordStrength(Niveau niveau, String details) {
            this.niveau = niveau;
            this.details = details;
        }

        public Niveau getNiveau() { return niveau; }
        public String getDetails() { return details; }
    }
}
