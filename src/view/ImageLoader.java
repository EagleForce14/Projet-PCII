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
     * Charge une image à partir de son chemin.
     * Cette méthode tente d'abord de charger l'image en tant que ressource du classpath,
     * ce qui est la méthode standard pour les applications Java.
     *
     * @param path Le chemin relatif de l'image.
     * @return L'objet Image chargé, ou null si le chargement a échoué.
     */
    public static Image load(String path) {
        Image image = null;
        try {
            // Essayer de charger depuis le classpath
            URL imageUrl = ImageLoader.class.getResource(path);
            
            if (imageUrl != null) {
                image = ImageIO.read(imageUrl);
            } else {
                // Si l'image n'est pas trouvée dans le classpath, essayer le système de fichiers local
                // C'est utile si l'exécution se fait depuis la racine du projet sans que src soit dans le classpath
                File file = new File("src" + path);
                if (file.exists()) {
                    image = ImageIO.read(file);
                } else {
                    System.err.println("Image non trouvée : " + path);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur d'entrée/sortie lors du chargement de l'image : " + path);
            e.printStackTrace();
        }
        return image;
    }
}
