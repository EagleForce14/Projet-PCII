package view;

import model.EnemyUnit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Petit renderer dédié à la carte d'information affichée après un clic sur un lapin.
 */
public class EnemyStatusOverlay {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    private final Font titleFont;
    private final Font bodyFont;

    // Le constructeur de la classe
    public EnemyStatusOverlay() {
        this.titleFont = CustomFontLoader.loadFont(FONT_PATH, 10.0f);
        this.bodyFont = CustomFontLoader.loadFont(FONT_PATH, 8.0f);
    }

    /**
     * Dessine une carte fixe en haut de l'écran pour garder une lecture stable,
     * même si le lapin sélectionné continue de bouger.
     */
    public void paint(Graphics2D g2d, EnemyUnit enemy, int viewWidth) {
        if (enemy == null) {
            return;
        }

        // La carte reste toujours centrée horizontalement.
        // On borne sa largeur pour garder un rendu propre aussi bien sur petite que grande fenêtre.
        int cardWidth = Math.min(290, Math.max(220, viewWidth - 48));
        int cardHeight = 88;
        int cardX = (viewWidth - cardWidth) / 2;
        int cardY = 24;

        // Toute l'information affichée ici vient du lapin lui-même:
        // l'overlay ne fait que lire les informations déjà calculées par EnemyUnit.
        long countdownMaxMs = enemy.getOverlayCountdownMaxMs();
        long remainingMs = enemy.getOverlayCountdownMs();
        String status = enemy.getOverlayStatus();
        boolean isEatingCulture = enemy.isEatingCultureCountdownActive();

        // Le chrono n'est affiché que lorsqu'il existe réellement un temps à suivre.
        // Sinon on garde juste un état textuel simple ("En fuite", "Culture trouvée", etc.).
        boolean hasCountdown = remainingMs >= 0L && countdownMaxMs > 0L;
        double progress = hasCountdown ? 1.0 - (remainingMs / (double) countdownMaxMs) : 0.0;
        int percent = (int) Math.round(progress * 100.0);

        // Le titre et la phrase de détail changent selon le contexte:
        // soit le lapin est en train d'attendre avant de manger,
        // soit on suit son chrono de retour au terrier.
        String titleText = isEatingCulture ? "Manger la culture" : "Retour au terrier";
        String detailText = hasCountdown
                ? String.format(isEatingCulture ? "Fini de manger dans %.1f s" : "Depart dans %.1f s", remainingMs / 1000.0)
                : "";

        // Ombre légère sous la carte pour mieux la détacher du décor.
        g2d.setColor(new Color(0, 0, 0, 70));
        g2d.fillRoundRect(cardX + 4, cardY + 4, cardWidth, cardHeight, 18, 18);

        // Corps principal de la carte.
        g2d.setColor(new Color(57, 41, 24, 232));
        g2d.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 18, 18);

        // Bandeau haut un peu plus clair pour donner une structure "cartouche de jeu".
        g2d.setColor(new Color(123, 90, 53, 245));
        g2d.fillRoundRect(cardX, cardY, cardWidth, 26, 18, 18);
        g2d.fillRect(cardX, cardY + 13, cardWidth, 13);

        // Contour clair pour garder un style pixel-art lisible sur tous les fonds.
        g2d.setColor(new Color(230, 214, 157, 255));
        g2d.drawRoundRect(cardX, cardY, cardWidth - 1, cardHeight - 1, 18, 18);

        // Le titre est volontairement un peu plus bas qu'avant pour mieux respirer dans le bandeau.
        g2d.setFont(titleFont);
        g2d.setColor(new Color(255, 248, 220));
        g2d.drawString(titleText, cardX + 16, cardY + 24);

        // Ligne d'état + pourcentage + petite phrase explicative.
        // On garde des textes courts pour ne pas surcharger la carte.
        g2d.setFont(bodyFont);
        g2d.setColor(new Color(236, 229, 212));
        g2d.drawString(status, cardX + 16, cardY + 45);
        g2d.drawString(hasCountdown ? percent + "%" : "--", cardX + cardWidth - 42, cardY + 45);
        g2d.drawString(detailText, cardX + 16, cardY + 57);

        // La barre utilise des segments plutôt qu'un remplissage continu:
        // visuellement c'est plus proche d'une UI de jeu rétro et plus simple à lire rapidement.
        int barX = cardX + 16;
        int barY = cardY + 60;
        int barWidth = cardWidth - 32;
        int barHeight = 12;
        int segmentGap = 3;
        int segmentCount = 11;
        int filledSegments = hasCountdown ? (int) Math.round(progress * segmentCount) : 0;
        int segmentWidth = (barWidth - ((segmentCount - 1) * segmentGap)) / segmentCount;

        // Fond sombre de la barre.
        g2d.setColor(new Color(35, 24, 15, 210));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 10, 10);

        // Chaque segment est rempli ou non selon la progression.
        // Quand il n'y a pas de chrono actif, on garde une barre vide plus neutre.
        for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
            int segmentX = barX + (segmentIndex * (segmentWidth + segmentGap));
            boolean isFilled = segmentIndex < filledSegments;
            Color filledColor = new Color(216, 188, 91);
            Color emptyColor = hasCountdown ? new Color(94, 72, 49) : new Color(70, 57, 44);
            g2d.setColor(isFilled ? filledColor : emptyColor);
            g2d.fillRoundRect(segmentX, barY + 3, segmentWidth, barHeight - 6, 5, 5);
        }
    }
}
