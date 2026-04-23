package view;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * Classe utilitaire dédiée à l'importation d'images.
 */
public class ImageLoader {

    /**
     * Charge une image sans journaliser d'erreur si elle est absente.
     * Utile pour tester plusieurs variantes de nommage d'un même sprite.
     */
    public static Image loadOptional(String path) {
        try {
            URL imageUrl = ImageLoader.class.getResource(path);

            if (imageUrl != null) {
                return ImageIO.read(imageUrl);
            }

            File file = new File("src" + path);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (IOException e) {
            return null;
        }

        return null;
    }

    /**
     * Charge une image à partir de son chemin.
     * Cette méthode tente d'abord de charger l'image en tant que ressource du classpath,
     * ce qui est la méthode standard pour les applications Java.
     *
     * @param path Le chemin relatif de l'image.
     * @return L'objet Image chargé, ou null si le chargement a échoué.
     */
    public static Image load(String path) {
        Image image = loadOptional(path);
        if (image == null) {
            System.err.println("Image non trouvée : " + path);
        }
        return image;
    }
}
