package model.movement;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle de gestion des entités.
 * Actuellement, il n'y a qu'une unité principale mais la structure est prête pour N unités.
 */
public class MovementModel {
    // La liste de toutes les unités présentes sur le terrain
    private final List<Unit> units;
    
    // Référence directe vers le joueur (pour le contrôler au clavier)
    private Unit playerUnit;

    // Vaut true si une unité déplaçable (ici le joueur) est sur une case du champ highlightée.
    private volatile boolean actionOverlayEnabled;

    // La case sur laquelle le joueur est réellement positionné dans le champ.
    private volatile Point activeFieldCell;

    public MovementModel() {
        units = new ArrayList<>();
    }

    // Permet l'ajout d'unités contrôlables au clavier (non utilisée pour le moment mais elle le sera par la suite)
    public void addUnit(Unit unit) {
        units.add(unit);
    }

    // Définit l'unité spécifique qui sera contrôlée par le joueur
    public void setPlayerUnit(Unit unit) {
        this.playerUnit = unit;
        // On s'assure qu'il est aussi dans la liste générale pour l'affichage/update
        if (!units.contains(unit)) {
            units.add(unit);
        }
    }

    // On met à jour la position pour chaque unité de la liste
    public void update() {
        for (Unit u : units) {
            u.updatePosition();
        }
    }

    // Accesseur pour le contrôleur (qui a besoin de piloter le joueur)
    public Unit getPlayerUnit() {
        return playerUnit;
    }

    /**
     * Getter pour activer/désactiver visuellement les boutons.
     */
    public boolean isActionOverlayEnabled() {
        return actionOverlayEnabled;
    }

    /**
     * Mémorise la case active du champ. Le contrôleur s'appuie dessus pour
     * déclencher les actions au bon endroit sans lire directement la vue.
     */
    public void setActiveFieldCell(Point activeFieldCell) {
        this.activeFieldCell = activeFieldCell == null ? null : new Point(activeFieldCell);
        this.actionOverlayEnabled = activeFieldCell != null;
    }

    /**
     * Renvoie une copie pour éviter qu'un appelant modifie l'état interne.
     */
    public Point getActiveFieldCell() {
        return activeFieldCell == null ? null : new Point(activeFieldCell);
    }

    // Accesseur pour la vue (qui a besoin d'afficher TOUTES les unités)
    public List<Unit> getUnits() {
        return units;
    }
}
