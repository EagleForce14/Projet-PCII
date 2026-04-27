package view;

/**
 * Regroupe toutes les proportions utilisées pour dessiner une clôture.
 *
 * Parce que ces valeurs ne décrivent pas vraiment le panneau lui-même :
 * elles décrivent la "géométrie visuelle" d'une clôture.
 */
final class FenceMetrics {
    // Profondeur visible de la partie extérieure de la clôture.
    private final int outsideDepth;
    // Profondeur visible de la partie intérieure de la clôture.
    private final int insideDepth;
    // Épaisseur totale de la bande principale de clôture.
    private final int bandThickness;
    // Épaisseur d'une latte horizontale.
    private final int slatThickness;
    // Espace laissé entre deux lattes.
    private final int slatGap;
    // Épaisseur d'un poteau vertical.
    private final int postThickness;
    // Dépassement du poteau par rapport à la bande.
    private final int postExtension;
    // Épaisseur de l'ombre projetée de la clôture.
    private final int shadowThickness;

    /**
     * On calcule toutes les proportions d'une clôture à partir de la taille d'une tuile.
     */
    public FenceMetrics(int tileSize) {
        this.outsideDepth = Math.max(2, tileSize / 18);
        this.insideDepth = Math.max(6, tileSize / 9);
        this.bandThickness = outsideDepth + insideDepth;
        this.slatThickness = Math.max(2, bandThickness / 3);
        this.slatGap = Math.max(1, tileSize / 20);
        this.postThickness = Math.max(7, tileSize / 8);
        this.postExtension = Math.max(4, tileSize / 12);
        this.shadowThickness = Math.max(2, tileSize / 18);
    }

    /**
     * On renvoie la profondeur visible de la partie extérieure.
     */
    int getOutsideDepth() {
        return outsideDepth;
    }

    /**
     * On renvoie la profondeur visible de la partie intérieure.
     */
    int getInsideDepth() {
        return insideDepth;
    }

    /**
     * On renvoie l'épaisseur totale de la bande principale.
     */
    int getBandThickness() {
        return bandThickness;
    }

    /**
     * On renvoie l'épaisseur d'une latte.
     */
    int getSlatThickness() {
        return slatThickness;
    }

    /**
     * On renvoie l'écart prévu entre deux lattes.
     */
    int getSlatGap() {
        return slatGap;
    }

    /**
     * On renvoie l'épaisseur du poteau vertical.
     */
    int getPostThickness() {
        return postThickness;
    }

    /**
     * On renvoie de combien le poteau dépasse de la bande principale.
     */
    int getPostExtension() {
        return postExtension;
    }

    /**
     * On renvoie l'épaisseur de l'ombre de clôture.
     */
    int getShadowThickness() {
        return shadowThickness;
    }
}
