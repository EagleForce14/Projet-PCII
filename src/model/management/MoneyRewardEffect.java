package model.management;

/**
 * Représente une animation de gain d'argent en attente d'affichage.

 * Le modèle d'argent ne connaît pas l'écran ni les coordonnées Swing.
 * Il mémorise donc seulement :
 * - un point de départ logique dans le monde quand on en dispose,
 * - et l'instant de départ de l'effet.

 * La vue convertit ensuite ces informations en trajectoire visuelle.
 */
public class MoneyRewardEffect {
    // Durée totale pendant laquelle un gain reste affichable à l'écran.
    private static final long MONEY_REWARD_EFFECT_MS = 720L;

    // Position logique d'origine du gain sur l'axe X.
    private final int sourceWorldX;
    // Position logique d'origine du gain sur l'axe Y.
    private final int sourceWorldY;
    // Indique si l'effet connaît vraiment un point de départ dans le monde.
    private final boolean explicitSource;
    // Instant de création de l'effet, utilisé pour calculer sa durée de vie.
    private final long startedAtMs;

    /**
     * On crée un effet simple quand on ne connaît pas la position d'origine du gain.
     */
    public MoneyRewardEffect(long startedAtMs) {
        this.sourceWorldX = 0;
        this.sourceWorldY = 0;
        this.explicitSource = false;
        this.startedAtMs = startedAtMs;
    }

    /**
     * On mémorise ici toutes les données nécessaires pour animer un gain d'argent.
     */
    public MoneyRewardEffect(int sourceWorldX, int sourceWorldY, boolean explicitSource, long startedAtMs) {
        this.sourceWorldX = sourceWorldX;
        this.sourceWorldY = sourceWorldY;
        this.explicitSource = explicitSource;
        this.startedAtMs = startedAtMs;
    }

    /**
     * On renvoie l'abscisse logique du point de départ du gain.
     */
    public int getSourceWorldX() {
        return sourceWorldX;
    }

    /**
     * On renvoie l'ordonnée logique du point de départ du gain.
     */
    public int getSourceWorldY() {
        return sourceWorldY;
    }

    /**
     * On dit si l'effet doit vraiment partir d'une position du monde.
     */
    public boolean hasExplicitSource() {
        return explicitSource;
    }

    /**
     * On dit si l'effet a terminé toute sa durée d'affichage.
     */
    public boolean isExpired(long now) {
        return now - startedAtMs >= MONEY_REWARD_EFFECT_MS;
    }

    /**
     * On renvoie l'avancement normalisé de l'effet entre 0 et 1.
     */
    public double getProgress(long now) {
        double elapsed = Math.max(0L, now - startedAtMs);
        return Math.min(1.0, elapsed / MONEY_REWARD_EFFECT_MS);
    }
}
