package view;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.imageio.ImageIO;

/**
 * Classe utilitaire dédiée à l'importation d'images.
 */
public class ImageLoader {
    private static final ConcurrentMap<String, Optional<Image>> IMAGE_CACHE = new ConcurrentHashMap<>();

    /**
     * Essaie de charger une image et renvoie simplement `null` si elle n'existe pas.
     * Cette méthode sert surtout quand on a plusieurs noms possibles pour une même image.
     */
    public static Image loadOptional(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        return IMAGE_CACHE.computeIfAbsent(path, ImageLoader::readImage).orElse(null);
    }

    /**
     * Charge une image à partir de son chemin.
     * Si l'image n'est pas trouvée, la méthode l'indique dans la console.
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

    /**
     * Demande le chargement d'une liste d'images à l'avance.
     * Le but est que ces images soient déjà prêtes quand le jeu en aura besoin plus tard.
     */
    public static void warmup(String... paths) {
        if (paths == null) {
            return;
        }

        for (String path : paths) {
            loadOptional(path);
        }
    }

    /**
     * Fait le vrai travail de lecture d'une image depuis les ressources du projet
     * ou depuis le dossier `src` si la ressource n'est pas trouvée dans le classpath.
     */
    private static Optional<Image> readImage(String path) {
        try {
            URL imageUrl = ImageLoader.class.getResource(path);

            if (imageUrl != null) {
                return Optional.ofNullable(ImageIO.read(imageUrl));
            }

            File file = new File("src" + path);
            if (file.exists()) {
                return Optional.ofNullable(ImageIO.read(file));
            }
        } catch (IOException e) {
            return Optional.empty();
        }

        return Optional.empty();
    }
}
