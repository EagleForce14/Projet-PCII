package controller;

import java.awt.Point;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import model.culture.Culture;
import model.culture.GrilleCulture;
import model.culture.Stade;
import model.culture.Type;
import model.enemy.EnemyModel;
import model.environment.FieldObstacleMap;
import model.environment.TreeInstance;
import model.environment.TreeManager;
import model.movement.MovementModel;
import model.movement.Unit;
import model.management.Inventaire;
import model.management.Money;
import model.runtime.GamePauseController;
import model.shop.FacilityType;
import view.*;
import view.shop.ShopOverlay;
import view.workshop.WorkshopOverlay;

/**
 * Le contrôleur chargé de gérer les intéractions.
 */
public class MovementController implements KeyListener, MouseListener, MouseMotionListener {
    private final MovementModel model;
    private final GrilleCulture grilleCulture;
    private final Money playerMoney;
    private final Inventaire inventaire;
    private final MovementView movementView;
    private final EnemyView enemyView;
    private final FieldPanel fieldPanel;
    private final InventoryStatusOverlay inventoryStatusOverlay;
    private final ShopOverlay shopOverlay;
    private final WorkshopOverlay workshopOverlay;
    private final TreeManager treeManager;
    private final GamePauseController pauseController;

    // Constructeur de la classe
    public MovementController(MovementModel model, MovementView view, EnemyModel enemyModel, EnemyView enemyView, SidebarPanel sidebarPanel,
                              GrilleCulture grilleCulture, Money playerMoney, Inventaire inventaire,
                              TreeManager treeManager,
                              FieldPanel fieldPanel, InventoryStatusOverlay inventoryStatusOverlay, ShopOverlay shopOverlay,
                              WorkshopOverlay workshopOverlay,
                              GameOverOverlay gameOverOverlay, Runnable restartGameAction) {
        this.model = model;
        this.grilleCulture = grilleCulture;
        this.playerMoney = playerMoney;
        this.inventaire = inventaire;
        this.movementView = view;
        this.enemyView = enemyView;
        this.fieldPanel = fieldPanel;
        this.inventoryStatusOverlay = inventoryStatusOverlay;
        this.shopOverlay = shopOverlay;
        this.workshopOverlay = workshopOverlay;
        this.treeManager = treeManager;
        this.pauseController = GamePauseController.getInstance();
        // On s'abonne aux événements clavier.
        view.addKeyListener(this);
        // On s'abonne aussi a la couche du joueur.
        // Selon l'ordre des couches Swing, c'est parfois elle qui recoit le clic.
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
        enemyView.addMouseListener(this);
        enemyView.addMouseMotionListener(this);
        inventoryStatusOverlay.addMouseListener(this);

        JButton labourButton = sidebarPanel.getLabourButton();
        labourButton.addActionListener(this::labourerCaseActive);

        JButton plantButton = sidebarPanel.getPlantButton();
        plantButton.addActionListener(this::planterSurCaseActive);

        JButton harvestButton = sidebarPanel.getHarvestButton();
        harvestButton.addActionListener(this::recolterCaseActive);

        JButton waterButton = sidebarPanel.getWaterButton();
        waterButton.addActionListener(this::arroserCaseActive);

        JButton cleanButton = sidebarPanel.getCleanButton();
        cleanButton.addActionListener(this::nettoyerCaseActive);

        JButton pathButton = sidebarPanel.getPathButton();
        pathButton.addActionListener(this::poserCheminCaseActive);

        JButton compostButton = sidebarPanel.getCompostButton();
        compostButton.addActionListener(this::gererBoutonCompostCaseActive);

        JButton cutTreeButton = sidebarPanel.getCutTreeButton();
        cutTreeButton.addActionListener(this::couperArbreProche);

        JButton replayButton = gameOverOverlay.getReplayButton();
        replayButton.addActionListener(event -> relancerPartie(restartGameAction));
    }

    /**
     * Labourer transforme simplement une case d'herbe en case cultivable.
     * On reste volontairement alignés avec les autres actions :
     * le bouton agit sur la case actuellement occupée par le joueur.
     */
    private void labourerCaseActive(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null
                || !fieldPanel.isFarmableCell(activeFieldCell)
                || !grilleCulture.canLabourCell(activeFieldCell.x, activeFieldCell.y)) {
            return;
        }

        grilleCulture.labourerCase(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * On plante uniquement sur la case actuellement surlignée.
     * Le type planté dépend de l'emplacement de graine sélectionné dans l'inventaire.
     */
    private void planterSurCaseActive(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null
                || !fieldPanel.isFarmableCell(activeFieldCell)
                || grilleCulture.hasPath(activeFieldCell.x, activeFieldCell.y)
                || !grilleCulture.isLabouree(activeFieldCell.x, activeFieldCell.y)) {
            return;
        }

        Type selectedSeedType = model.getSelectedSeedType();
        if (selectedSeedType == null || !inventaire.possedeGraine(selectedSeedType)) {
            return;
        }

        if (grilleCulture.getCulture(activeFieldCell.x, activeFieldCell.y) == null) {
            grilleCulture.planterCulture(activeFieldCell.x, activeFieldCell.y, selectedSeedType, inventaire);
            if (!inventaire.possedeGraine(selectedSeedType)) {
                model.clearSelectedInventoryItem();
                inventoryStatusOverlay.repaint();
            }
        }
    }

    /**
     * La récolte ne s'exécute que sur une plante mature, puis on crédite le joueur.
     */
    private void recolterCaseActive(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        // Si le jardinier n'est sur aucune case du champ, il n'y a rien à récolter.
        if (activeFieldCell == null || !fieldPanel.isFarmableCell(activeFieldCell)) {
            return;
        }

        Culture culture = grilleCulture.getCulture(activeFieldCell.x, activeFieldCell.y);
        // On garde la vérification ici même si le bouton est déjà filtré par la vue.
        // Comme ça, la règle reste vraie même si cette méthode est réutilisée ailleurs plus tard.
        if (culture == null || culture.getStadeCroissance() != Stade.MATURE) {
            return;
        }

        // On calcule le gain associé.
        int gain = grilleCulture.recolterCulture(activeFieldCell.x, activeFieldCell.y);

        // Le portefeuille du joueur est mis à jour à part pour garder une logique bien découpée.
        Unit playerUnit = model.getPlayerUnit();
        if (playerUnit == null) {
            playerMoney.credit(gain);
            return;
        }

        playerMoney.creditFromWorld(gain, playerUnit.getX(), playerUnit.getY());
    }

    /**
     * Le bouton nettoyer retire uniquement une culture flétrie sur la case du jardinier.
     */
    private void nettoyerCaseActive(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null || !fieldPanel.isFarmableCell(activeFieldCell)) {
            return;
        }
        grilleCulture.nettoyerCultureFletrie(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * L'arrosage n'est disponible que sur une case occupée par une culture intermédiaire.
     */
    private void arroserCaseActive(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null || !fieldPanel.isFarmableCell(activeFieldCell)) {
            return;
        }
        grilleCulture.arroserCulture(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * Le chemin se pose comme les autres actions de la sidebar :
     * sur la case actuellement occupee par le joueur.
     *
     * Cela remplace volontairement l'ancien placement a la souris,
     * pour garder une logique de controle plus uniforme.
     */
    private void poserCheminCaseActive(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null
                || model.getSelectedFacilityType() != FacilityType.CHEMIN
                || !fieldPanel.isFarmableCell(activeFieldCell)
                || !grilleCulture.canPlacePath(activeFieldCell.x, activeFieldCell.y)
                || inventaire.possedeInstallation(FacilityType.CHEMIN)) {
            return;
        }

        grilleCulture.placePath(activeFieldCell.x, activeFieldCell.y, inventaire);
        if (inventaire.possedeInstallation(FacilityType.CHEMIN)) {
            model.clearSelectedInventoryItem();
        }
        inventoryStatusOverlay.repaint();
    }

    /**
     * Le compost se pose comme le chemin :
     * uniquement via un bouton dédié, sur la case actuellement occupée par le joueur.
     */
    private void poserCompostCaseActive(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null
                || model.getSelectedFacilityType() != FacilityType.COMPOST
                || !fieldPanel.isFarmableCell(activeFieldCell)
                || !grilleCulture.canPlaceCompost(activeFieldCell.x, activeFieldCell.y)
                || inventaire.possedeInstallation(FacilityType.COMPOST)) {
            return;
        }

        grilleCulture.placeCompost(activeFieldCell.x, activeFieldCell.y, inventaire);
        fieldPanel.clearCompostInfluenceHighlight();
        if (inventaire.possedeInstallation(FacilityType.COMPOST)) {
            model.clearSelectedInventoryItem();
        }
        inventoryStatusOverlay.repaint();
    }

    /**
     * Le bouton compost peut avoir 2 fonctionnalités :
     * - sur une case d'herbe libre, il pose le compost,
     * - directement sur le compost déjà posé, il le remise dans l'inventaire.
     */
    private void gererBoutonCompostCaseActive(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null || !fieldPanel.isFarmableCell(activeFieldCell)) {
            return;
        }

        if (grilleCulture.hasCompostAt(activeFieldCell.x, activeFieldCell.y)) {
            remiserCompostCaseActive(activeFieldCell);
            return;
        }

        poserCompostCaseActive(event);
    }

    /**
     * Replace le compost dans l'inventaire pour qu'il puisse être réutilisé plus tard.
     * On efface aussi la surbrillance éventuelle, car elle n'a plus de sens sans compost posé.
     */
    private void remiserCompostCaseActive(Point activeFieldCell) {
        if (activeFieldCell == null
                || !fieldPanel.isFarmableCell(activeFieldCell)
                || !grilleCulture.hasCompostAt(activeFieldCell.x, activeFieldCell.y)) {
            return;
        }

        grilleCulture.storeCompost(activeFieldCell.x, activeFieldCell.y, inventaire);
        fieldPanel.clearCompostInfluenceHighlight();
        inventoryStatusOverlay.repaint();
    }

    /**
     * La coupe d'arbre suit exactement la même philosophie que les autres boutons :
     * le contrôleur valide l'action, met à jour le modèle, puis laisse les vues
     * se contenter d'afficher l'état courant.
     *
     * Ici, chaque clic ajoute simplement un impact.
     * Au quatrième, l'arbre disparaît et le bois est crédité.
     */
    private void couperArbreProche(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        TreeInstance interactableTree = getInteractableTreeForPlayer();
        if (interactableTree == null || treeManager == null) {
            return;
        }

        boolean treeFelled = treeManager.cutTree(interactableTree.getGridX(), interactableTree.getGridY());
        if (treeFelled) {
            inventaire.ajoutBois(treeManager.getWoodRewardQuantity());
        }

        inventoryStatusOverlay.repaint();
        movementView.requestFocusInWindow();
    }

    /**
     * Ouvre la boutique plein écran et fige le jeu tant que le panneau reste visible.
     */
    private void ouvrirBoutique() {
        if (pauseController.isPaused()) {
            return;
        }

        stopPlayerMovement();
        fieldPanel.clearFencePreview();
        fieldPanel.clearCompostInfluenceHighlight();
        showOverlay(shopOverlay);
        shopOverlay.openShop();
    }

    /**
     * La menuiserie ouvre son propre overlay,
     * mais suit exactement la même règle de pause que la boutique.
     */
    private void ouvrirMenuiserie() {
        if (pauseController.isPaused()) {
            return;
        }

        stopPlayerMovement();
        fieldPanel.clearFencePreview();
        fieldPanel.clearCompostInfluenceHighlight();
        showOverlay(workshopOverlay);
        workshopOverlay.openWorkshop();
    }

    /**
     * Le redémarrage n'est autorisé que quand le jeu est déjà figé.
     * En pratique, cela correspond à l'écran de défaite où le bouton apparaît.
     */
    private void relancerPartie(Runnable restartGameAction) {
        if (!pauseController.isPaused() || restartGameAction == null) {
            return;
        }
        restartGameAction.run();
    }

    /**
     * Le survol de clôture n'existe que si le joueur a explicitement sélectionné la clôture dans l'inventaire.
     */
    private void updateFencePlacementPreview(MouseEvent event) {
        if (pauseController.isPaused()
                || model.isFencePlacementSelected()
                || inventaire.possedeInstallation(FacilityType.CLOTURE)) {
            fieldPanel.clearFencePreview();
            return;
        }

        if (!(event.getSource() instanceof Component)) {
            fieldPanel.clearFencePreview();
            return;
        }

        Point pointInFieldPanel = SwingUtilities.convertPoint(
                (Component) event.getSource(),
                event.getPoint(),
                fieldPanel
        );
        fieldPanel.setFencePreview(fieldPanel.getFencePreviewAt(pointInFieldPanel));
    }

    /**
     * Si la clôture est l'outil actif, un clic sur une case VALIDE du champ tente de la poser sur le bord de la case survolé.
     */
    private boolean tryPlaceSelectedFence(MouseEvent event) {
        if (pauseController.isPaused()
                || model.isFencePlacementSelected()
                || inventaire.possedeInstallation(FacilityType.CLOTURE)) {
            return false;
        }

        updateFencePlacementPreview(event);
        FencePreview preview = fieldPanel.getFencePreview();
        if (preview == null) {
            return false;
        }

        Point cell = preview.getCell();
        assert cell != null;
        grilleCulture.placeFence(cell.x, cell.y, preview.getSide(), inventaire);

        // Tant qu'il reste des clôtures et que le joueur garde cet outil actif,
        // on laisse le preview se recalculer naturellement.
        updateFencePlacementPreview(event);
        if (inventaire.possedeInstallation(FacilityType.CLOTURE)) {
            model.clearSelectedInventoryItem();
            fieldPanel.clearFencePreview();
            inventoryStatusOverlay.repaint();
        }

        return true;
    }

    /**
     * Un clic sur l'inventaire choisit explicitement l'action en attente.
     * Recliquer sur le slot déjà sélectionné annule simplement ce mode.
     *
     * Cette méthode joue donc un rôle central dans le "mode courant" du joueur:
     * elle transforme un simple clic UI en intention de gameplay.
     * Après son passage, le contrôleur sait si le joueur veut planter une graine
     * précise, poser une clôture, ou au contraire annuler la sélection en cours.
     */
    private boolean handleInventoryClick(MouseEvent event) {
        // Premier filtre:
        // si l'événement ne vient pas de la barre d'inventaire, cette méthode
        // n'a rien à faire et rend la main au reste du contrôleur.
        if (event.getSource() != inventoryStatusOverlay) {
            return false;
        }

        // On convertit la position du clic en numéro de slot.
        // Toute la géométrie de la hotbar est déjà centralisée dans l'overlay,
        // donc on ne recalcule pas ça ici dans le contrôleur.
        int slotIndex = inventoryStatusOverlay.getSlotIndexAt(event.getPoint());
        if (slotIndex < 0) {
            // Le clic a bien eu lieu sur la zone de l'inventaire,
            // mais pas sur un slot exploitable.
            // On considère tout de même le clic comme "consommé" par l'UI,
            // puis on rend le focus au panneau principal du jeu.
            movementView.requestFocusInWindow();
            return true;
        }

        // On tente d'abord l'interprétation "graine".
        // L'ordre a son importance: les slots de graines occupent la majorité de la hotbar
        // et servent de sélection de type pour l'action planter.
        Type clickedSeedType = inventoryStatusOverlay.getSeedTypeForSlot(slotIndex);
        if (clickedSeedType != null) {
            // Cas 1: le slot est visuellement présent mais vide dans l'inventaire.
            // On nettoie alors la sélection courante pour éviter de garder un outil fantôme.
            if (!inventaire.possedeGraine(clickedSeedType)) {
                model.clearSelectedInventoryItem();
            // Cas 2: le joueur reclique sur la graine déjà active.
            // Ici on interprète ce geste comme une annulation du mode courant.
            } else if (model.getSelectedSeedType() == clickedSeedType) {
                model.clearSelectedInventoryItem();
            // Cas 3: le slot contient bien des graines et devient la nouvelle sélection active.
            } else {
                model.selectSeed(clickedSeedType);
            }

            // Quel que soit le cas, un clic sur une graine doit faire sortir
            // du mode clôture, donc on retire le preview éventuel.
            fieldPanel.clearFencePreview();
            fieldPanel.clearCompostInfluenceHighlight();
            // On redessine la barre pour refléter immédiatement le changement de sélection.
            inventoryStatusOverlay.repaint();
            // Enfin, on rend le focus au panneau principal pour que le clavier
            // continue de piloter le joueur juste après le clic.
            movementView.requestFocusInWindow();
            return true;
        }

        // Si ce n'était pas une graine, on tente maintenant l'interprétation "outil / installation".
        // Aujourd'hui, cela revient essentiellement à la clôture, mais le flux restera valable
        // si d'autres installations sélectionnables apparaissent plus tard.
        FacilityType clickedFacilityType = inventoryStatusOverlay.getFacilityTypeForSlot(slotIndex);
        if (clickedFacilityType != null) {
            if (!isSelectableFacilityType(clickedFacilityType)) {
                movementView.requestFocusInWindow();
                return true;
            }

            // Même logique que pour les graines:
            // slot vide -> on nettoie,
            // slot déjà actif -> on désélectionne,
            // sinon -> on active cet outil.
            if (inventaire.possedeInstallation(clickedFacilityType)) {
                model.clearSelectedInventoryItem();
            } else if (model.getSelectedFacilityType() == clickedFacilityType) {
                model.clearSelectedInventoryItem();
            } else {
                model.selectFacility(clickedFacilityType);
            }

            // À partir du moment où un outil est choisi, l'overlay doit refléter
            // le nouvel état, et le focus doit retourner au jeu.
            fieldPanel.clearFencePreview();
            fieldPanel.clearCompostInfluenceHighlight();
            inventoryStatusOverlay.repaint();
            movementView.requestFocusInWindow();
            return true;
        }

        movementView.requestFocusInWindow();
        return true;
    }

    // On implémente KeyListener (Gestion des Touches)
    @Override
    public void keyPressed(KeyEvent e) {
        if (pauseController.isPaused()) {
            return;
        }

        int key = e.getKeyCode();
        
        // On récupère l'unité du joueur "actif" à contrôler
        Unit player = model.getPlayerUnit();
        if (player == null) return; // Sécurité si pas de joueur
        
        // Mise à jour des flags selon la touche clavier enfoncée
        if (key == KeyEvent.VK_UP) {
            player.setMoveUp(true);
            player.setMoveDown(false);
            player.setMoveLeft(false);
            player.setMoveRight(false);
        }
        else if (key == KeyEvent.VK_DOWN) {
            player.setMoveDown(true);
            player.setMoveUp(false);
            player.setMoveLeft(false);
            player.setMoveRight(false);
        }
        else if (key == KeyEvent.VK_LEFT) {
            player.setMoveLeft(true);
            player.setMoveRight(false);
            player.setMoveUp(false);
            player.setMoveDown(false);
        }
        else if (key == KeyEvent.VK_RIGHT) {
            player.setMoveRight(true);
            player.setMoveLeft(false);
            player.setMoveUp(false);
            player.setMoveDown(false);
        }
    }

    // Désactivation de tous les flags de déplacement de l'unité lors du relâchement d’une touche.
    @Override
    public void keyReleased(KeyEvent e) {
        if (pauseController.isPaused()) {
            stopPlayerMovement();
            return;
        }

        int key = e.getKeyCode();
        
        Unit player = model.getPlayerUnit();
        if (player == null) return;
        
        // Désactivation des flags sur l'unité
        if (key == KeyEvent.VK_UP) player.setMoveUp(false);
        if (key == KeyEvent.VK_DOWN) player.setMoveDown(false);
        if (key == KeyEvent.VK_LEFT) player.setMoveLeft(false);
        if (key == KeyEvent.VK_RIGHT) player.setMoveRight(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (pauseController.isPaused()) {
            return;
        }

        if (handleInventoryClick(e)) {
            return;
        }

        if (handleWorkshopClick(e)) {
            movementView.requestFocusInWindow();
            return;
        }

        if (handleBarnShopClick(e)) {
            movementView.requestFocusInWindow();
            return;
        }

        if (tryPlaceSelectedFence(e)) {
            movementView.requestFocusInWindow();
            return;
        }

        if (handleCompostInspection(e)) {
            movementView.requestFocusInWindow();
            return;
        }

        // Quand le chemin est selectionne, on ne veut plus de pose a la souris.
        // Le clic ne doit donc pas declencher d'action "cachee" sur le terrain.
        if (model.isPathPlacementSelected() || model.isCompostPlacementSelected()) {
            movementView.requestFocusInWindow();
            return;
        }

        Point pointInEnemyView = e.getSource() instanceof Component
                ? SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), enemyView)
                : e.getPoint();
        enemyView.handleWorldClick(pointInEnemyView);
        // On rend immédiatement le focus au panneau de déplacement
        // pour que les flèches continuent de répondre après un clic souris.
        movementView.requestFocusInWindow();
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
        updateFencePlacementPreview(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        fieldPanel.clearFencePreview();
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        updateFencePlacementPreview(e);
    }

    /**
     * On remet tous les flags a false pour eviter qu'une direction reste "collee"
     * pendant l'ouverture de la boutique.
     */
    private void stopPlayerMovement() {
        Unit player = model.getPlayerUnit();
        if (player == null) {
            return;
        }

        player.setMoveUp(false);
        player.setMoveDown(false);
        player.setMoveLeft(false);
        player.setMoveRight(false);
    }

    /**
     * Un clic sur le compost posé sert uniquement à afficher sa zone d'effet.
     *
     * On ne consomme pas ce clic comme une action de pose :
     * c'est un clic d'inspection / lecture pour aider le joueur à comprendre le bonus.
     */
    private boolean handleCompostInspection(MouseEvent event) {
        Point clickedCell = getClickedFieldCell(event);
        if (clickedCell == null) {
            fieldPanel.clearCompostInfluenceHighlight();
            return false;
        }

        if (!grilleCulture.hasCompostAt(clickedCell.x, clickedCell.y)) {
            fieldPanel.clearCompostInfluenceHighlight();
            return false;
        }

        fieldPanel.toggleCompostInfluenceHighlight();
        return true;
    }

    /**
     * La boutique s'ouvre maintenant directement en cliquant sur la grange.
     * On teste l'image visible de la grange, pas seulement sa hitbox de collision,
     * car le joueur raisonne avec ce qu'il voit à l'écran.
     */
    private boolean handleBarnShopClick(MouseEvent event) {
        Point pointInFieldPanel = getPointInFieldPanel(event);
        if (pointInFieldPanel == null || !fieldPanel.getBarnScreenBounds().contains(pointInFieldPanel)) {
            return false;
        }

        ouvrirBoutique();
        return true;
    }

    private boolean handleWorkshopClick(MouseEvent event) {
        Point pointInFieldPanel = getPointInFieldPanel(event);
        if (pointInFieldPanel == null || !fieldPanel.getWorkshopScreenBounds().contains(pointInFieldPanel)) {
            return false;
        }

        ouvrirMenuiserie();
        return true;
    }

    /**
     * Convertit un clic provenant d'une vue Swing quelconque
     * vers une case logique du champ.
     */
    private Point getClickedFieldCell(MouseEvent event) {
        Point pointInFieldPanel = getPointInFieldPanel(event);
        if (pointInFieldPanel == null) {
            return null;
        }

        return fieldPanel.getGridPositionAt(pointInFieldPanel.x, pointInFieldPanel.y);
    }

    /**
     * Convertit n'importe quel clic reçu par le contrôleur
     * vers le repère du FieldPanel.
     */
    private Point getPointInFieldPanel(MouseEvent event) {
        if (!(event.getSource() instanceof Component)) {
            return null;
        }

        return SwingUtilities.convertPoint(
                (Component) event.getSource(),
                event.getPoint(),
                fieldPanel
        );
    }

    private TreeInstance getInteractableTreeForPlayer() {
        Unit player = model.getPlayerUnit();
        FieldObstacleMap obstacleMap = fieldPanel.getFieldObstacleMap();
        if (player == null || obstacleMap == null) {
            return null;
        }

        return obstacleMap.findInteractableTree(
                player.getX(),
                player.getY(),
                Unit.SIZE,
                Unit.SIZE
        );
    }

    private void showOverlay(JComponent overlay) {
        if (overlay == null || movementView.getRootPane() == null) {
            return;
        }
        if (movementView.getRootPane().getGlassPane() != overlay) {
            movementView.getRootPane().setGlassPane(overlay);
        }
    }

    private boolean isSelectableFacilityType(FacilityType facilityType) {
        return facilityType == FacilityType.CLOTURE
                || facilityType == FacilityType.CHEMIN
                || facilityType == FacilityType.COMPOST;
    }
}
