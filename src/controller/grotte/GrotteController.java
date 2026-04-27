package controller.grotte;

import model.enemy.EnemyModel;
import model.enemy.EnemyPhysicsThread;
import model.grotte.ShrineHazardThread;
import model.grotte.combat.CaveCombatModel;
import model.grotte.combat.CaveCombatThread;
import model.movement.BuildingGeometry;
import model.movement.MovementCollisionMap;
import model.movement.Unit;
import model.runtime.GamePauseController;
import model.runtime.Jour;
import view.EnemyView;
import view.FieldPanel;
import view.MovementView;
import view.SidebarPanel;
import view.grotte.GrotteCombatView;
import view.grotte.GrotteFieldPanel;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Contrôleur dédié à la grotte.
 * Toute la gestion des listeners de cette zone est centralisée ici :
 * déplacement, tir, focus clavier, clic de sélection des monstres
 * et bascule ferme <-> grotte.
 */
public final class GrotteController implements KeyListener, MouseListener, ActionListener {
    private final GamePauseController pauseController;
    private final GrotteFieldPanel grotteFieldPanel;
    private final EnemyView caveEnemyView;
    private final GrotteCombatView grotteCombatView;
    private final CardLayout centerLayout;
    private final JPanel centerPanel;
    private final SidebarPanel sidebarPanel;
    private final FieldPanel farmFieldPanel;
    private final Jour jour;
    private final Unit playerUnit;
    private final MovementCollisionMap farmCollisionMap;
    private final MovementCollisionMap caveCollisionMap;
    private final EnemyModel caveEnemyModel;
    private final CaveCombatModel caveCombatModel;
    private final EnemyPhysicsThread caveEnemyPhysicsThread;
    private final CaveCombatThread caveCombatThread;
    private final ShrineHazardThread shrineHazardThread;
    private final MovementView farmMovementView;
    private final MovementView grotteMovementView;
    private final String farmCardName;
    private final String grotteCardName;

    private JButton caveButton;
    private boolean entryTriggered;
    private boolean exitTriggered;
    private volatile boolean transitionCheckQueued;

    /**
     * On rassemble ici tout ce qu'il faut pour faire vivre la scène de grotte.
     */
    public GrotteController(
            MovementView farmMovementView,
            MovementView grotteMovementView,
            EnemyView caveEnemyView,
            GrotteCombatView grotteCombatView,
            GrotteFieldPanel grotteFieldPanel,
            FieldPanel farmFieldPanel,
            Jour jour,
            JPanel centerPanel,
            String farmCardName,
            String grotteCardName,
            SidebarPanel sidebarPanel,
            Unit playerUnit,
            MovementCollisionMap farmCollisionMap,
            MovementCollisionMap caveCollisionMap,
            EnemyModel caveEnemyModel,
            CaveCombatModel caveCombatModel,
            EnemyPhysicsThread caveEnemyPhysicsThread,
            CaveCombatThread caveCombatThread,
            ShrineHazardThread shrineHazardThread
    ) {
        this.pauseController = GamePauseController.getInstance();
        this.farmMovementView = farmMovementView;
        this.grotteMovementView = grotteMovementView;
        this.caveEnemyView = caveEnemyView;
        this.grotteCombatView = grotteCombatView;
        this.grotteFieldPanel = grotteFieldPanel;
        this.farmFieldPanel = farmFieldPanel;
        this.jour = jour;
        this.centerPanel = centerPanel;
        this.centerLayout = centerPanel == null ? null : (CardLayout) centerPanel.getLayout();
        this.farmCardName = farmCardName;
        this.grotteCardName = grotteCardName;
        this.sidebarPanel = sidebarPanel;
        this.playerUnit = playerUnit;
        this.farmCollisionMap = farmCollisionMap;
        this.caveCollisionMap = caveCollisionMap;
        this.caveEnemyModel = caveEnemyModel;
        this.caveCombatModel = caveCombatModel;
        this.caveEnemyPhysicsThread = caveEnemyPhysicsThread;
        this.caveCombatThread = caveCombatThread;
        this.shrineHazardThread = shrineHazardThread;

        registerInputLayers();
        bindCaveButton();
        markActiveCard(farmCardName);
    }

    /**
     * On branche ici les couches Swing qui peuvent recevoir le clavier ou la souris dans la grotte.
     */
    private void registerInputLayers() {
        if (grotteMovementView != null) {
            grotteMovementView.addKeyListener(this);
            grotteMovementView.addMouseListener(this);
        }
        if (caveEnemyView != null) {
            caveEnemyView.addMouseListener(this);
        }
        if (grotteCombatView != null) {
            grotteCombatView.addMouseListener(this);
        }
    }

    /**
     * On relie le bouton de grotte de la sidebar à ce contrôleur.
     */
    private void bindCaveButton() {
        if (sidebarPanel == null) {
            return;
        }

        caveButton = sidebarPanel.getCaveButton();
        if (caveButton != null) {
            caveButton.addActionListener(this);
        }
    }

    /**
     * On ne réagit ici qu'au bouton dédié à l'entrée ou à la sortie de la grotte.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == caveButton) {
            toggleCaveScene();
        }
    }

    /**
     * On demande un contrôle différé pour tester la transition avec une position joueur bien à jour.
     */
    public void checkSceneTransitionFromCurrentPosition() {
        if (pauseController.isPaused() || transitionCheckQueued) {
            return;
        }

        transitionCheckQueued = true;
        SwingUtilities.invokeLater(this::runQueuedTransitionCheck);
    }

    /**
     * On transforme chaque touche en une direction unique ou en tir continu dans la grotte.
     */
    @Override
    public void keyPressed(KeyEvent event) {
        if (pauseController.isPaused() || !isCaveSceneActive()) {
            return;
        }

        Unit player = playerUnit;
        if (player == null) {
            return;
        }

        int key = event.getKeyCode();
        if (isShootKey(key)) {
            if (caveCombatModel != null) {
                caveCombatModel.setPlayerFiring(true);
            }
            return;
        }

        if (isUpKey(key)) {
            activateSingleDirection(player, true, false, false, false);
        } else if (isDownKey(key)) {
            activateSingleDirection(player, false, true, false, false);
        } else if (isLeftKey(key)) {
            activateSingleDirection(player, false, false, true, false);
        } else if (isRightKey(key)) {
            activateSingleDirection(player, false, false, false, true);
        }
    }

    /**
     * On arrête ici le tir ou la direction relâchée pour garder un état de contrôle propre.
     */
    @Override
    public void keyReleased(KeyEvent event) {
        Unit player = playerUnit;
        if (player == null) {
            return;
        }

        if (pauseController.isPaused()) {
            stopPlayerMovement(player);
            stopPlayerFire();
            return;
        }

        int key = event.getKeyCode();
        if (isShootKey(key)) {
            stopPlayerFire();
            return;
        }

        if (isUpKey(key)) {
            player.setMoveUp(false, false);
        }
        if (isDownKey(key)) {
            player.setMoveDown(false, false);
        }
        if (isLeftKey(key)) {
            player.setMoveLeft(false, false);
        }
        if (isRightKey(key)) {
            player.setMoveRight(false, false);
        }
    }

    /**
     * On n'utilise pas la saisie de caractères dans la grotte.
     */
    @Override
    public void keyTyped(KeyEvent event) {
    }

    /**
     * On ne traite pas de logique au clic complet : seul l'appui nous intéresse.
     */
    @Override
    public void mouseClicked(MouseEvent event) {
    }

    /**
     * On redonne le focus clavier à la vue grotte et on synchronise la sélection de monstre.
     */
    @Override
    public void mousePressed(MouseEvent event) {
        if (grotteMovementView != null) {
            grotteMovementView.requestFocusInWindow();
        }
        syncEnemySelection(event);
    }

    /**
     * On ne déclenche rien au relâchement de souris dans cette scène.
     */
    @Override
    public void mouseReleased(MouseEvent event) {
    }

    /**
     * On ne gère pas d'effet d'entrée de souris ici.
     */
    @Override
    public void mouseEntered(MouseEvent event) {
    }

    /**
     * On ne gère pas d'effet de sortie de souris ici.
     */
    @Override
    public void mouseExited(MouseEvent event) {
    }

    /**
     * On convertit le clic reçu vers le repère de la vue ennemie avant de sélectionner quoi que ce soit.
     */
    private void syncEnemySelection(MouseEvent event) {
        if (caveEnemyView == null || event == null) {
            return;
        }

        Component source = event.getComponent();
        if (source == null) {
            return;
        }

        Point translatedPoint = SwingUtilities.convertPoint(source, event.getPoint(), caveEnemyView);
        caveEnemyView.handleWorldClick(translatedPoint);
    }

    /**
     * On force ici une seule direction active pour éviter les combinaisons ambiguës.
     */
    private void activateSingleDirection(Unit player, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight) {
        if (player == null) {
            return;
        }

        player.setMoveUp(moveUp, moveUp);
        player.setMoveDown(moveDown, moveDown);
        player.setMoveLeft(moveLeft, moveLeft);
        player.setMoveRight(moveRight, moveRight);
    }

    /**
     * On coupe complètement le déplacement du joueur avant chaque bascule de scène.
     */
    private void stopPlayerMovement(Unit player) {
        if (player != null) {
            player.stopMovement();
        }
    }

    /**
     * On arrête le tir du joueur pour ne pas garder un feu actif après un changement d'état.
     */
    private void stopPlayerFire() {
        if (caveCombatModel != null) {
            caveCombatModel.setPlayerFiring(false);
        }
    }

    /**
     * On bascule entre ferme et grotte en appliquant les arrêts nécessaires juste avant.
     */
    private void toggleCaveScene() {
        if (pauseController.isPaused()) {
            return;
        }

        stopPlayerMovement(playerUnit);
        stopPlayerFire();
        if (isCaveSceneActive()) {
            returnToFarm();
            return;
        }

        showCave();
    }

    /**
     * Le déplacement du joueur est calculé dans le thread physique,
     * mais la bascule de carte et le focus restent des opérations Swing.
     * On revalide donc le trigger sur l'EDT avec la position la plus récente.
     */
    private void runQueuedTransitionCheck() {
        transitionCheckQueued = false;
        if (pauseController.isPaused()) {
            return;
        }

        if (isCaveSceneActive()) {
            checkFarmExit();
        } else {
            checkFarmEntry();
        }
    }

    /**
     * On surveille ici la zone de sortie de grotte pour renvoyer le joueur à la ferme au bon moment.
     */
    private void checkFarmExit() {
        if (pauseController.isPaused() || !isCaveSceneActive()) {
            return;
        }

        Unit player = playerUnit;
        if (player == null) {
            return;
        }

        Point playerCenterCell = grotteFieldPanel.getLogicalGridPositionAt(player.getX(), player.getY());
        if (playerCenterCell == null) {
            return;
        }

        if (grotteFieldPanel.getGrotteMap().isActualFarmExitCell(playerCenterCell.x, playerCenterCell.y)) {
            exitTriggered = false;
            return;
        }

        if (exitTriggered) {
            return;
        }

        exitTriggered = true;
        stopPlayerMovement(player);
        stopPlayerFire();
        returnToFarm();
    }

    /**
     * On surveille ici l'entrée de la grotte côté ferme sans déclencher plusieurs fois la même transition.
     */
    private void checkFarmEntry() {
        if (pauseController.isPaused() || isCaveSceneActive() || playerUnit == null || farmFieldPanel == null) {
            return;
        }

        if (!isFarmPlayerInsideCaveEntryZone()) {
            entryTriggered = false;
            return;
        }

        if (entryTriggered) {
            return;
        }

        entryTriggered = true;
        stopPlayerMovement(playerUnit);
        showCave();
    }

    /**
     * L'entrée grotte est une vraie zone (couloir + bouche),
     * donc on teste l'intersection avec la hitbox du joueur,
     * pas seulement une case de centre.
     */
    private boolean isFarmPlayerInsideCaveEntryZone() {
        Rectangle caveEntryZoneBounds = farmFieldPanel.getFarmCaveEntryTriggerLogicalBounds();
        if (caveEntryZoneBounds == null) {
            return false;
        }

        Rectangle playerBounds = BuildingGeometry.buildCenteredBounds(
                playerUnit.getX(),
                playerUnit.getY(),
                Unit.SIZE,
                Unit.SIZE
        );
        return caveEntryZoneBounds.intersects(playerBounds);
    }

    /**
     * On active la carte grotte, on y place le joueur et on relance les systèmes propres à cette scène.
     */
    private void showCave() {
        if (centerLayout == null || centerPanel == null) {
            return;
        }

        centerLayout.show(centerPanel, grotteCardName);
        markActiveCard(grotteCardName);
        if (sidebarPanel != null) {
            sidebarPanel.setCaveMode(true);
        }
        if (jour != null) {
            jour.setTempsFige(true);
        }

        if (playerUnit != null) {
            Point caveSpawnOffset = grotteFieldPanel.getInitialPlayerOffset();
            stopPlayerMovement(playerUnit);
            playerUnit.setFieldObstacleMap(caveCollisionMap);
            playerUnit.setPosition(caveSpawnOffset.x, caveSpawnOffset.y);
            playerUnit.enterCave();
        }
        if (caveEnemyModel != null) {
            caveEnemyModel.enterCave();
        }
        if (caveCombatModel != null) {
            caveCombatModel.enterCave();
        }
        setCaveThreadsActive(true);
        if (grotteMovementView != null) {
            grotteMovementView.requestFocusInWindow();
        }
        exitTriggered = false;
    }

    /**
     * On revient à la ferme en restaurant la carte, la collision et l'état normal des systèmes.
     */
    private void returnToFarm() {
        if (centerLayout == null || centerPanel == null) {
            return;
        }

        centerLayout.show(centerPanel, farmCardName);
        markActiveCard(farmCardName);
        if (sidebarPanel != null) {
            sidebarPanel.setCaveMode(false);
        }
        if (jour != null) {
            jour.setTempsFige(false);
        }

        stopPlayerMovement(playerUnit);
        stopPlayerFire();
        setCaveThreadsActive(false);
        if (caveCombatModel != null) {
            caveCombatModel.exitCave();
        }
        if (caveEnemyModel != null) {
            caveEnemyModel.exitCave();
        }
        if (playerUnit != null) {
            playerUnit.exitCave();
            playerUnit.setFieldObstacleMap(farmCollisionMap);
            Point farmReturnOffset = farmFieldPanel == null ? null : farmFieldPanel.getFarmCaveReturnOffset();
            if (farmReturnOffset != null) {
                playerUnit.setPosition(farmReturnOffset.x, farmReturnOffset.y);
            }
        }
        if (farmMovementView != null) {
            farmMovementView.requestFocusInWindow();
        }
        entryTriggered = false;
    }

    /**
     * On active ou on coupe en bloc les threads qui ne doivent tourner que dans la grotte.
     */
    private void setCaveThreadsActive(boolean active) {
        if (caveEnemyPhysicsThread != null) {
            caveEnemyPhysicsThread.setThreadActive(active);
        }
        if (caveCombatThread != null) {
            caveCombatThread.setThreadActive(active);
        }
        if (shrineHazardThread != null) {
            shrineHazardThread.setThreadActive(active);
        }
    }

    /**
     * On garde ce test séparé pour rendre la lecture des contrôles plus directe.
     */
    private boolean isUpKey(int keyCode) {
        return keyCode == KeyEvent.VK_UP;
    }

    /**
     * On garde ce test séparé pour rendre la lecture des contrôles plus directe.
     */
    private boolean isDownKey(int keyCode) {
        return keyCode == KeyEvent.VK_DOWN;
    }

    /**
     * On garde ce test séparé pour rendre la lecture des contrôles plus directe.
     */
    private boolean isLeftKey(int keyCode) {
        return keyCode == KeyEvent.VK_LEFT;
    }

    /**
     * On garde ce test séparé pour rendre la lecture des contrôles plus directe.
     */
    private boolean isRightKey(int keyCode) {
        return keyCode == KeyEvent.VK_RIGHT;
    }

    /**
     * On isole ici la touche de tir pour centraliser la règle en un seul endroit.
     */
    private boolean isShootKey(int keyCode) {
        return keyCode == KeyEvent.VK_SPACE;
    }

    /**
     * On lit ici la carte active mémorisée sur le panneau central.
     */
    private boolean isCaveSceneActive() {
        return centerPanel != null
                && grotteCardName != null
                && grotteCardName.equals(centerPanel.getClientProperty("activeCard"));
    }

    /**
     * On mémorise la carte affichée pour savoir ensuite dans quelle scène on se trouve vraiment.
     */
    private void markActiveCard(String cardName) {
        if (centerPanel != null) {
            centerPanel.putClientProperty("activeCard", cardName);
        }
    }
}
