package model.grotte.combat;

/**
 * Projectile très léger utilisé par le combat de la grotte.

 * Il ne connait que sa géométrie et son déplacement :
 * les collisions et les dégâts restent gérés par le modèle de combat.
 */
public final class CaveProjectile {
    private final CaveProjectileOwner owner;
    private final int width;
    private final int height;
    private final int damage;
    private final double velocityX;
    private final double velocityY;

    private double x;
    private double y;
    private double previousX;
    private double previousY;
    private double remainingDistance;

    public CaveProjectile(
            CaveProjectileOwner owner,
            double startX,
            double startY,
            double velocityX,
            double velocityY,
            int width,
            int height,
            int damage,
            double maxDistance
    ) {
        this.owner = owner;
        this.x = startX;
        this.y = startY;
        this.previousX = startX;
        this.previousY = startY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.width = Math.max(2, width);
        this.height = Math.max(2, height);
        this.damage = Math.max(0, damage);
        this.remainingDistance = Math.max(0.0, maxDistance);
    }

    /**
     * Avance le projectile d'une frame.
     * Le booléen retourné indique s'il a encore une distance utile à parcourir.
     */
    public boolean advance() {
        previousX = x;
        previousY = y;
        x += velocityX;
        y += velocityY;
        remainingDistance -= Math.sqrt((velocityX * velocityX) + (velocityY * velocityY));
        return remainingDistance > 0.0;
    }

    public CaveProjectileOwner getOwner() {
        return owner;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getPreviousX() {
        return previousX;
    }

    public double getPreviousY() {
        return previousY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDamage() {
        return damage;
    }
}
