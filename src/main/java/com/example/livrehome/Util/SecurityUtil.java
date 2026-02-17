package com.example.livrehome.Util;

import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour la gestion de la sécurité
 * - Hashage des mots de passe avec BCrypt
 * - Validation des mots de passe
 * - Génération de mots de passe aléatoires
 * 
 * @author Équipe Bibliothèque
 * @version 1.0
 */
public class SecurityUtil {
    
    /**
     * Niveau de complexité du hashage BCrypt (4-31)
     * Plus le nombre est élevé, plus le hashage est sécurisé mais lent
     * 10-12 est un bon compromis pour la plupart des applications
     */
    private static final int BCRYPT_ROUNDS = 10;
    
    /**
     * Pattern pour valider la force d'un mot de passe
     * Au moins 8 caractères, une majuscule, une minuscule, un chiffre
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$"
    );
    
    /**
     * Générateur de nombres aléatoires sécurisé
     */
    private static final SecureRandom RANDOM = new SecureRandom();
    
    // ═══════════════════════════════════════════════════════════════
    // MÉTHODES BCRYPT (RECOMMANDÉES)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Hasher un mot de passe avec BCrypt
     * 
     * BCrypt est l'algorithme recommandé car:
     * - Il inclut automatiquement un "salt" (sel) aléatoire
     * - Il est conçu pour être lent (protection contre le brute force)
     * - Il est évolutif (on peut augmenter la complexité avec le temps)
     * 
     * @param plainPassword Mot de passe en clair
     * @return String Le hash BCrypt (60 caractères)
     * @throws IllegalArgumentException si le mot de passe est null ou vide
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }
        
        // BCrypt génère automatiquement un salt et hash le mot de passe
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    /**
     * Vérifier un mot de passe contre un hash BCrypt
     * 
     * @param plainPassword Mot de passe en clair (saisi par l'utilisateur)
     * @param hashedPassword Hash stocké en base de données
     * @return true si le mot de passe correspond, false sinon
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            // BCrypt compare automatiquement avec le salt inclus dans le hash
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Hash invalide ou corrompu
            System.err.println("Erreur de vérification du mot de passe: " + e.getMessage());
            return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // MÉTHODES SHA-256 (ALTERNATIVE - MOINS SÉCURISÉE)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Hasher un mot de passe avec SHA-256 + Salt
     * 
     * ⚠️ ATTENTION: SHA-256 seul n'est PAS recommandé pour les mots de passe
     * Utilisez plutôt BCrypt (méthode hashPassword ci-dessus)
     * 
     * Cette méthode est fournie pour compatibilité ou cas spécifiques
     * 
     * @param plainPassword Mot de passe en clair
     * @param salt Sel aléatoire (généré avec generateSalt())
     * @return String Le hash SHA-256 en hexadécimal
     */
    public static String hashPasswordSHA256(String plainPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Combiner mot de passe + salt
            String saltedPassword = plainPassword + salt;
            
            // Hasher
            byte[] hash = digest.digest(saltedPassword.getBytes());
            
            // Convertir en hexadécimal
            return bytesToHex(hash);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur SHA-256: " + e.getMessage(), e);
        }
    }
    
    /**
     * Générer un salt aléatoire pour SHA-256
     * 
     * @return String Salt en Base64
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Convertir un tableau de bytes en chaîne hexadécimale
     * 
     * @param bytes Tableau de bytes
     * @return String Représentation hexadécimale
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // VALIDATION DES MOTS DE PASSE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Valider la force d'un mot de passe
     * 
     * Critères:
     * - Au moins 8 caractères
     * - Au moins une majuscule
     * - Au moins une minuscule
     * - Au moins un chiffre
     * 
     * @param password Mot de passe à valider
     * @return true si le mot de passe est fort, false sinon
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Obtenir un message d'erreur détaillé pour un mot de passe faible
     * 
     * @param password Mot de passe à analyser
     * @return String Message décrivant les problèmes
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Le mot de passe est vide";
        }
        
        StringBuilder message = new StringBuilder();
        
        if (password.length() < 8) {
            message.append("- Au moins 8 caractères requis\n");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            message.append("- Au moins une lettre majuscule requise\n");
        }
        
        if (!password.matches(".*[a-z].*")) {
            message.append("- Au moins une lettre minuscule requise\n");
        }
        
        if (!password.matches(".*[0-9].*")) {
            message.append("- Au moins un chiffre requis\n");
        }
        
        if (message.length() == 0) {
            return "Mot de passe fort ✓";
        }
        
        return "Mot de passe faible:\n" + message.toString();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // GÉNÉRATION DE MOTS DE PASSE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Générer un mot de passe aléatoire fort
     * 
     * @param length Longueur souhaitée (minimum 8)
     * @return String Mot de passe aléatoire
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("La longueur minimale est 8 caractères");
        }
        
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        String allChars = upperCase + lowerCase + digits + special;
        
        StringBuilder password = new StringBuilder();
        
        // Garantir au moins un caractère de chaque type
        password.append(upperCase.charAt(RANDOM.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(RANDOM.nextInt(lowerCase.length())));
        password.append(digits.charAt(RANDOM.nextInt(digits.length())));
        password.append(special.charAt(RANDOM.nextInt(special.length())));
        
        // Remplir le reste aléatoirement
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(RANDOM.nextInt(allChars.length())));
        }
        
        // Mélanger les caractères
        return shuffleString(password.toString());
    }
    
    /**
     * Mélanger les caractères d'une chaîne
     * 
     * @param input Chaîne à mélanger
     * @return String Chaîne mélangée
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // MÉTHODE DE TEST
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Méthode main pour tester SecurityUtil
     */
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("🧪 TEST DE SECURITYUTIL");
        System.out.println("═══════════════════════════════════════════════════\n");
        
        // Test 1: Hashage BCrypt
        System.out.println("Test 1: Hashage BCrypt");
        System.out.println("─────────────────────────────────────────────────");
        String password = "admin123";
        String hash = hashPassword(password);
        System.out.println("Mot de passe: " + password);
        System.out.println("Hash BCrypt:  " + hash);
        System.out.println("Longueur:     " + hash.length() + " caractères\n");
        
        // Test 2: Vérification du mot de passe
        System.out.println("Test 2: Vérification du mot de passe");
        System.out.println("─────────────────────────────────────────────────");
        boolean isValid = verifyPassword("admin123", hash);
        System.out.println("Vérification avec bon mot de passe: " + isValid + " ✓");
        boolean isInvalid = verifyPassword("wrongpassword", hash);
        System.out.println("Vérification avec mauvais mot de passe: " + isInvalid + " ✓\n");
        
        // Test 3: Validation de la force du mot de passe
        System.out.println("Test 3: Validation de la force");
        System.out.println("─────────────────────────────────────────────────");
        String[] testPasswords = {
            "weak",           // Trop court
            "nouppercase1",   // Pas de majuscule
            "NOLOWERCASE1",   // Pas de minuscule
            "NoNumbers",      // Pas de chiffre
            "Strong123"       // Fort
        };
        
        for (String pwd : testPasswords) {
            System.out.println("\nMot de passe: " + pwd);
            System.out.println(getPasswordStrengthMessage(pwd));
        }
        
        // Test 4: Génération de mot de passe
        System.out.println("\nTest 4: Génération de mot de passe aléatoire");
        System.out.println("─────────────────────────────────────────────────");
        String randomPwd = generateRandomPassword(12);
        System.out.println("Mot de passe généré: " + randomPwd);
        System.out.println(getPasswordStrengthMessage(randomPwd));
        
        // Test 5: SHA-256 (comparaison)
        System.out.println("\n\nTest 5: SHA-256 (pour comparaison)");
        System.out.println("─────────────────────────────────────────────────");
        String salt = generateSalt();
        String sha256Hash = hashPasswordSHA256("admin123", salt);
        System.out.println("Salt:       " + salt);
        System.out.println("Hash SHA256: " + sha256Hash);
        System.out.println("⚠️ Note: BCrypt est plus sécurisé que SHA-256");
        
        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("✅ TOUS LES TESTS TERMINÉS");
        System.out.println("═══════════════════════════════════════════════════\n");
    }
}
