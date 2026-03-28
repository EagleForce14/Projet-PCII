package view;

/**
 * Regroupe toutes les proportions utilisées pour dessiner une clôture.
 *
 * Parce que ces valeurs ne décrivent pas vraiment le panneau lui-même :
 * elles décrivent la "géométrie visuelle" d'une clôture.
 */
final class FenceMetrics {
    private final int outsideDepth;
    private final int insideDepth;
    private final int bandThickness;
    private final int slatThickness;
    private final int slatGap;
    private final int postThickness;
    private final int postExtension;
    private final int shadowThickness;

    // Le constructeur
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

    int getOutsideDepth() {
        return outsideDepth;
    }

    int getInsideDepth() {
        return insideDepth;
    }

    int getBandThickness() {
        return bandThickness;
    }

    int getSlatThickness() {
        return slatThickness;
    }

    int getSlatGap() {
        return slatGap;
    }

    int getPostThickness() {
        return postThickness;
    }

    int getPostExtension() {
        return postExtension;
    }

    int getShadowThickness() {
        return shadowThickness;
    }
}
