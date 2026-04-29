package model.movement;
import model.culture.Type;
import model.shop.FacilityType;

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

    // La case sur laquelle le joueur est réellement positionné dans le champ.
    private volatile Point activeFieldCell;

    // Une seule entrée d'inventaire peut être active à la fois :
    // soit une graine, soit une installation.
    // Type de graine actuellement sélectionné par le joueur.
    private volatile Type selectedSeedType;
    // Type d'installation actuellement sélectionné par le joueur.
    private volatile FacilityType selectedFacilityType;

    /**
     * On démarre avec un modèle vide, prêt à recevoir ses unités.
     */
    public MovementModel() {
        units = new ArrayList<>();
    }

    /**
     * On désigne l'unité pilotée par le joueur et on s'assure qu'elle est bien suivie par le modèle.
     */
    public void setPlayerUnit(Unit unit) {
        this.playerUnit = unit;
        // On s'assure qu'il est aussi dans la liste générale pour l'affichage/update
        if (!units.contains(unit)) {
            units.add(unit);
        }
    }

    /**
     * On met à jour toutes les unités suivies pendant ce tick de physique.
     */
    public void update() {
        for (Unit u : units) {
            u.updatePosition();
        }
    }

    /**
     * On renvoie l'unité actuellement contrôlée par le joueur.
     */
    public Unit getPlayerUnit() {
        return playerUnit;
    }

    /**
     * Mémorise la case active du champ. Le contrôleur s'appuie dessus pour
     * déclencher les actions au bon endroit sans lire directement la vue.
     */
    public void setActiveFieldCell(Point activeFieldCell) {
        this.activeFieldCell = activeFieldCell == null ? null : new Point(activeFieldCell);
    }

    /**
     * Renvoie une copie pour éviter qu'un appelant modifie l'état interne.
     */
    public Point getActiveFieldCell() {
        return activeFieldCell == null ? null : new Point(activeFieldCell);
    }

    /**
     * On sélectionne une graine et on efface toute sélection d'installation.
     */
    public void selectSeed(Type type) {
        selectedSeedType = type;
        selectedFacilityType = null;
    }

    /**
     * On sélectionne une installation et on efface toute sélection de graine.
     */
    public void selectFacility(FacilityType type) {
        selectedFacilityType = type;
        selectedSeedType = null;
    }

    /**
     * On vide toute sélection d'objet d'inventaire active.
     */
    public void clearSelectedInventoryItem() {
        selectedSeedType = null;
        selectedFacilityType = null;
    }

    /**
     * On renvoie le type de graine actuellement sélectionné.
     */
    public Type getSelectedSeedType() {
        return selectedSeedType;
    }

    /**
     * On renvoie le type d'installation actuellement sélectionné.
     */
    public FacilityType getSelectedFacilityType() {
        return selectedFacilityType;
    }

    /**
     * on préfère donner au contrôleur des méthodes explicites
     * plutôt que de le laisser répéter des comparaisons d'énumérations partout.
     */
    public boolean isFencePlacementSelected() {
        return !isSelectedFacility(FacilityType.CLOTURE);
    }

    /**
     * On dit si le joueur a actuellement sélectionné un chemin.
     */
    public boolean isPathPlacementSelected() {
        return isSelectedFacility(FacilityType.CHEMIN);
    }

    /**
     * On dit si le joueur a actuellement sélectionné un compost.
     */
    public boolean isCompostPlacementSelected() {
        return isSelectedFacility(FacilityType.COMPOST);
    }

    /**
     * On dit si le joueur a actuellement sélectionné un pont.
     */
    public boolean isBridgePlacementSelected() {
        return isSelectedFacility(FacilityType.PONT);
    }

    /**
     * On expose la liste des unités suivies pour le rendu et les contrôles.
     */
    public List<Unit> getUnits() {
        return units;
    }

    /**
     * On factorise ici le test de sélection d'une installation donnée.
     */
    private boolean isSelectedFacility(FacilityType type) {
        return selectedFacilityType == type;
    }
}
