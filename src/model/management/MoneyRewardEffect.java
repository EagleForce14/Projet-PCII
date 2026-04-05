package model.management;

/**
 * Représente une animation de gain d'argent en attente d'affichage.
 *
 * Le modèle d'argent ne connaît pas l'écran ni les coordonnées Swing.
 * Il mémorise donc seulement :
 * - le montant gagné,
 * - un point de départ logique dans le monde quand on en dispose,
 * - et l'instant de départ de l'effet.
 *
 * La vue convertit ensuite ces informations en trajectoire visuelle.
 */
public class MoneyRewardEffect {
    private static final long MONEY_REWARD_EFFECT_MS = 720L;

    private final int amount;
    private final int sourceWorldX;
    private final int sourceWorldY;
    private final boolean explicitSource;
    private final long startedAtMs;

    public MoneyRewardEffect(int amount, long startedAtMs) {
        this(amount, 0, 0, false, startedAtMs);
    }

    public MoneyRewardEffect(int amount, int sourceWorldX, int sourceWorldY, boolean explicitSource, long startedAtMs) {
        this.amount = amount;
        this.sourceWorldX = sourceWorldX;
        this.sourceWorldY = sourceWorldY;
        this.explicitSource = explicitSource;
        this.startedAtMs = startedAtMs;
    }

    public int getAmount() {
        return amount;
    }

    public int getSourceWorldX() {
        return sourceWorldX;
    }

    public int getSourceWorldY() {
        return sourceWorldY;
    }

    public boolean hasExplicitSource() {
        return explicitSource;
    }

    public boolean isExpired(long now) {
        return now - startedAtMs >= MONEY_REWARD_EFFECT_MS;
    }

    public double getProgress(long now) {
        double elapsed = Math.max(0L, now - startedAtMs);
        return Math.min(1.0, elapsed / MONEY_REWARD_EFFECT_MS);
    }
}
