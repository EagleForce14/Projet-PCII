package view;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *  Classe permettant d'importer une police d'écriture personnalisée
 */
public class CustomFontLoader {
    private static Font pixelFont;
    /**
     * Méthode statique (l'instanciation n'est donc pas nécessaire) qui charge une police à partir d'un fichier .ttf depuis le package resources.
     * @param resourcePath Chemin relatif dans les ressources (ex: "src/resources/fonts/PixelFont.ttf")
     * @param size Taille de la police souhaitée
     * @return Police personnalisée
     */
    public static Font loadFont(String resourcePath, float size) {
        if (pixelFont == null) {
            try {
                File fontFile = new File(resourcePath);
                if (!fontFile.exists()) {
                    throw new IOException("Police non trouvée au chemin spécifié: " + resourcePath);
                }
                
                try (InputStream is = new FileInputStream(fontFile)) {
                    pixelFont = Font.createFont(Font.TRUETYPE_FONT, is);
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    ge.registerFont(pixelFont);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de la police: " + e.getMessage());
                pixelFont = new Font("Monospaced", Font.PLAIN, 12); // fallback avec taille par défaut
            }
        }
        return pixelFont.deriveFont(size);
    }
}