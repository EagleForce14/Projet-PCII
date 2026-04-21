package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * Helpers graphiques partagés pour les trajectoires de récompense :
 * lueur, arc de déplacement et petite canopée de parachute.
 */
public final class RewardAnimationUtils {
    private RewardAnimationUtils() {
    }

    public static void drawCenteredGlow(Graphics2D g2d, int centerX, int centerY, int diameter, Color color, int alpha) {
        if (g2d == null || color == null || diameter <= 0) {
            return;
        }

        g2d.setColor(withAlpha(color, alpha));
        g2d.fillOval(centerX - (diameter / 2), centerY - (diameter / 2), diameter, diameter);
    }

    public static void drawParachuteCanopy(
            Graphics2D g2d,
            int centerX,
            int canopyBaseY,
            double progress,
            Color canopyColor
    ) {
        if (g2d == null || canopyColor == null) {
            return;
        }

        int canopyWidth = 14;
        int canopyHeight = 7;
        int canopyX = centerX - (canopyWidth / 2);
        int canopyY = canopyBaseY - 9 - (int) Math.round(Math.sin(progress * Math.PI) * 4.0);

        g2d.setColor(withAlpha(canopyColor, (int) Math.round(230 * (1.0 - (progress * 0.35)))));
        g2d.fillArc(canopyX, canopyY, canopyWidth, canopyHeight, 0, 180);
        g2d.drawLine(centerX - 4, canopyY + canopyHeight - 1, centerX - 2, canopyBaseY);
        g2d.drawLine(centerX + 4, canopyY + canopyHeight - 1, centerX + 2, canopyBaseY);
    }

    public static Point computeArcPosition(
            int startX,
            int startY,
            int destinationX,
            int destinationY,
            double progress,
            double arcHeight
    ) {
        double easedProgress = easeOutCubic(progress);
        int currentX = (int) Math.round(lerp(startX, destinationX, easedProgress));
        int currentY = (int) Math.round(lerp(startY, destinationY, easedProgress) - (Math.sin(progress * Math.PI) * arcHeight));
        return new Point(currentX, currentY);
    }

    public static Color withAlpha(Color color, int alpha) {
        if (color == null) {
            return new Color(0, 0, 0, 0);
        }

        int clampedAlpha = Math.max(0, Math.min(255, alpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), clampedAlpha);
    }

    private static double lerp(double start, double end, double progress) {
        return start + ((end - start) * progress);
    }

    private static double easeOutCubic(double progress) {
        double inverse = 1.0 - progress;
        return 1.0 - (inverse * inverse * inverse);
    }
}
