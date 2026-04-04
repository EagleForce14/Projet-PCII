package model.movement;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Petit utilitaire partagé par les bâtiments fixes :
 * chargement des dimensions réelles de l'image et construction d'une hitbox.
 */
public final class BuildingGeometry {
    private BuildingGeometry() {}

    /**
     * Construit une hitbox plus compacte que l'image affichée,
     * recentrée horizontalement et légèrement remontée depuis le bas du sprite.
     */
    public static Rectangle buildCollisionBounds(
            Rectangle drawBounds,
            double widthRatio,
            double heightRatio,
            double bottomInsetRatio
    ) {
        if (drawBounds == null) {
            return null;
        }

        int hitboxWidth = Math.max(1, (int) Math.round(drawBounds.width * widthRatio));
        int hitboxHeight = Math.max(1, (int) Math.round(drawBounds.height * heightRatio));
        int hitboxX = drawBounds.x + ((drawBounds.width - hitboxWidth) / 2);
        int hitboxY = drawBounds.y
                + drawBounds.height
                - hitboxHeight
                - Math.max(1, (int) Math.round(drawBounds.height * bottomInsetRatio));

        return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }

    /**
     * Convertit une position donnée par son centre et sa taille
     * en rectangle classique Java (coin haut-gauche + largeur + hauteur).
     */
    public static Rectangle buildCenteredBounds(double centerX, double centerY, int width, int height) {
        int left = (int) Math.round(centerX - (width / 2.0));
        int top = (int) Math.round(centerY - (height / 2.0));
        return new Rectangle(left, top, width, height);
    }

    /**
     * Prend un objet défini par son centre et sa taille,
     * on construit son rectangle, puis on vérifie s'il touche l'obstacle.
     */
    public static boolean collidesWithCenteredBox(
            Rectangle obstacleBounds,
            double centerX,
            double centerY,
            int width,
            int height
    ) {
        if (obstacleBounds == null) {
            return false;
        }

        Rectangle entityBounds = buildCenteredBounds(centerX, centerY, width, height);
        return obstacleBounds.intersects(entityBounds);
    }

    /**
     * Lit les dimensions natives d'une image depuis les ressources du projet,
     * avec une taille de secours si le fichier n'est pas disponible.
     */
    public static Dimension loadSpriteSize(
            Class<?> ownerClass,
            String assetPath,
            Dimension fallbackSize
    ) {
        BufferedImage image = null;

        try {
            URL imageUrl = ownerClass.getResource(assetPath);
            if (imageUrl != null) {
                image = ImageIO.read(imageUrl);
            } else {
                File file = new File("src" + assetPath);
                if (file.exists()) {
                    image = ImageIO.read(file);
                }
            }
        } catch (IOException e) {
            System.err.println("Impossible de lire les dimensions de l'image : " + assetPath);
            e.printStackTrace();
        }

        if (image != null) {
            return new Dimension(image.getWidth(), image.getHeight());
        }

        return new Dimension(fallbackSize);
    }
}
