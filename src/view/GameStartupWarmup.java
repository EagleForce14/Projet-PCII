package view;

import view.grotte.GrotteTileFactory;

/**
 * Précharge en arrière-plan les ressources les plus coûteuses du premier lancement.
 * Le but est simple : profiter du temps passé sur l'écran d'accueil
 * pour éviter que le clic "Lancer la partie" déclenche tout le travail d'un coup.
 */
public final class GameStartupWarmup {
    // On évite de lancer deux fois le même préchargement en parallèle.
    private static boolean started;

    /**
     * Empêche de créer un objet de cette classe.
     * Cette classe ne sert que de boîte à outils statique.
     */
    private GameStartupWarmup() {
    }

    /**
     * Lance le préchargement dans un thread séparé.
     * Si ce travail a déjà été démarré une fois, la méthode ne fait rien.
     */
    public static synchronized void startAsync() {
        if (started) {
            return;
        }
        started = true;

        // `GameStartupWarmup::warmup` veut dire :
        // "quand ce thread démarre, il exécute la méthode `warmup()` de cette classe".
        Thread warmupThread = new Thread(GameStartupWarmup::warmup);
        warmupThread.setDaemon(true);
        warmupThread.start();
    }

    /**
     * Charge à l'avance les images, tuiles, sprites et polices utilisés
     * dans les premières scènes du jeu.
     */
    private static void warmup() {
        ImageLoader.warmup(
                "/assets/grass.png",
                "/assets/Terre.png",
                "/assets/stone_with_grass.png",
                "/assets/river.png",
                "/assets/marecages.png",
                "/assets/marecagesCenter.png",
                "/assets/marecagesGauche.png",
                "/assets/TerreMouillee.png",
                "/assets/bush.png",
                "/assets/bush_vertical.png",
                "/assets/bush_vertical_right.png",
                "/assets/entreeRiviere.png",
                "/assets/river2.png",
                "/assets/cave_entrance.png",
                "/assets/jeune_pousse.png",
                "/assets/croissance_inter.png",
                "/assets/maturite.png",
                "/assets/fletrie.png",
                "/assets/carotte_jeune_pousse.png",
                "/assets/carotte_intermediaire.png",
                "/assets/carotte_mature.png",
                "/assets/carotte_fletrie.png",
                "/assets/marguerithe_jeune_pousse.png",
                "/assets/rose_inter.png",
                "/assets/rose_mature.png",
                "/assets/rose_fletrie.png",
                "/assets/marguerithe_jeune_pousse.png",
                "/assets/tulipe_inter.png",
                "/assets/tulipe_mature.png",
                "/assets/tulipe_fletrie.png",
                "/assets/marguerithe_jeune_pousse.png",
                "/assets/marguerithe_inter.png",
                "/assets/marguerithe_mature.png",
                "/assets/marguerithe_fletrie.png",
                "/assets/radis_jeune.png",
                "/assets/radis_inter.png",
                "/assets/radis_mature.png",
                "/assets/radis_fletri.png",
                "/assets/choufleur_jeune.png",
                "/assets/choufleur_inter.png",
                "/assets/choufleur_mature.png",
                "/assets/choufleur_fletri.png",
                "/assets/nenuphar_jeune.png",
                "/assets/nenuphar_inter.png",
                "/assets/nenuphar_mature.png",
                "/assets/nenuphar_fletri.png",
                "/assets/iris_marais_jeune.png",
                "/assets/iris_marais_inter.png",
                "/assets/iris_marais_mature.png",
                "/assets/iris_marais_fletrie.png",
                "/assets/Compost.png",
                "/assets/bridge.png",
                "/assets/barn.png",
                "/assets/echoppe.png",
                "/assets/menuiserie.png",
                "/assets/wood.png",
                "/assets/arbre.png",
                "/assets/arbre2.png",
                "/assets/Saule pleureur.png",
                "/assets/tronc_arbre.png",
                "/assets/tronc_sombre.png",
                "/assets/ennemi_ferme_front.png",
                "/assets/ennemi_ferme_back.png",
                "/assets/ennemi_ferme_left.png",
                "/assets/ennemi_ferme_right.png",
                "/assets/ennemi_ferme_eating.png",
                "/assets/monsterTop.png",
                "/assets/monsterTop2.png",
                "/assets/monsterBasArret.png",
                "/assets/monsterBasAvance.png",
                "/assets/monsterBasAvance2.png",
                "/assets/monsterGaucheArret.png",
                "/assets/monsterGaucheAvance1.png",
                "/assets/monsterGaucheAvance2.png",
                "/assets/monsterDroitArret.png",
                "/assets/monsterDroitAvance1.png",
                "/assets/monsterDroitAvance2.png",
                "/assets/stone_grotte.png",
                "/assets/grotte_mur_droit.png",
                "/assets/grotte_mur_vertical.png",
                "/assets/statue_grotte.png",
                "/assets/Immobile/JardinierImmobile.png",
                "/assets/Immobile/JardinierImmobileGun.png",
                "/assets/MarcheBas/JardinierDesc1.png",
                "/assets/MarcheBas/JardinierDesc2.png",
                "/assets/MarcheBas/JardinierDesc3.png",
                "/assets/MarcheBas/JardinierDesc4.png",
                "/assets/MarcheDroite/JardinierDroite1.png",
                "/assets/MarcheDroite/JardinierDroite2.png",
                "/assets/MarcheDroite/JardinierDroite3.png",
                "/assets/MarcheDroite/JardinierDroite4.png",
                "/assets/MarcheGauche/JardinierGauche1.png",
                "/assets/MarcheGauche/JardinierGauche2.png",
                "/assets/MarcheGauche/JardinierGauche3.png",
                "/assets/MarcheGauche/JardinierGauche4.png",
                "/assets/MarcheHaut/JardinierMonte1.png",
                "/assets/MarcheHaut/JardinierMonte2.png",
                "/assets/MarcheHaut/JardinierMonte3.png",
                "/assets/MarcheHaut/JardinierMonte4.png",
                "/assets/MarcheHaut/JardinierMonte5.png",
                "/assets/Labourer/JardinierLabourer1.png",
                "/assets/Labourer/JardinierLabourer2.png",
                "/assets/Labourer/JardinierLabourer3.png",
                "/assets/Labourer/JardinierLabourer4.png",
                "/assets/Labourer/JardinierLabourer5.png",
                "/assets/Planter/JardinierPlanter1.png",
                "/assets/Planter/JardinierPlanter2.png",
                "/assets/Planter/JardinierPlanter3.png",
                "/assets/Recolter/JardinierRecolter1.png",
                "/assets/Recolter/JardinierRecolter2.png",
                "/assets/Recolter/JardinierRecolter3.png",
                "/assets/MarcheBas/JardinierBasGun1.png",
                "/assets/MarcheBas/JardinierBasGun2.png",
                "/assets/MarcheBas/JardinierBasGun3.png",
                "/assets/MarcheBas/JardinierBasGun4.png",
                "/assets/MarcheDroite/JardinierDroiteGun1.png",
                "/assets/MarcheDroite/JardinierDroiteGun2.png",
                "/assets/MarcheDroite/JardinierDroiteGun3.png",
                "/assets/MarcheDroite/JardinierDroiteGun4.png",
                "/assets/MarcheGauche/JardinierGaucheGun1.png",
                "/assets/MarcheGauche/JardinierGaucheGun2.png",
                "/assets/MarcheGauche/JardinierGaucheGun3.png",
                "/assets/MarcheGauche/JardinierGaucheGun4.png",
                "/assets/MarcheHaut/JardinierHautGun1.png",
                "/assets/MarcheHaut/JardinierHautGun2.png",
                "/assets/MarcheHaut/JardinierHautGun3.png",
                "/assets/MarcheHaut/JardinierHautGun4.png"
        );

        TerrainTileFactory.warmupSharedTiles();
        GrotteTileFactory.warmupSharedTiles();
        MovementView.warmupSharedSprites();
        EnemyView.warmupSharedSprites();
        EnvironmentView.warmupSharedAssets();
        CustomFontLoader.loadFont("src/assets/fonts/Minecraftia.ttf", 12.0f);
    }
}
