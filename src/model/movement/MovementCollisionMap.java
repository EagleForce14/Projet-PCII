package model.movement;

/**
 * Interface partagée par toutes les zones de déplacement.
 *
 * Le joueur n'a pas besoin de connaître le détail d'une ferme, d'une grotte
 * ou d'une future autre map : il lui suffit de demander si sa hitbox
 * peut occuper une position donnée.
 */
public interface MovementCollisionMap {
    boolean canOccupyCenteredBox(double centerX, double centerY, int width, int height);
}
