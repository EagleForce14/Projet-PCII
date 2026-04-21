package model.grotte.drop;

import model.culture.Type;
import model.shop.FacilityType;

/**
 * Décrit quel objet peut lâcher un monstre et quel type d'objet peut être lâché.
 * Le type d'objet, la quantité (1 par défaut), le poids de la sélection (la rareté)
 */
public final class CaveDropDefinition {
    private final CaveDropKind kind;
    private final Type seedType;
    private final FacilityType facilityType;
    private final int quantity;
    private final double selectionWeight;

    public CaveDropDefinition(Type seedType, int quantity, double selectionWeight) {
        if (seedType == null) {
            throw new IllegalArgumentException("Le type de graine ne peut pas être null.");
        }
        this.kind = CaveDropKind.SEED;
        this.seedType = seedType;
        this.facilityType = null;
        this.quantity = Math.max(1, quantity);
        this.selectionWeight = Math.max(0.0, selectionWeight);
    }

    public CaveDropDefinition(FacilityType facilityType, int quantity, double selectionWeight) {
        if (facilityType == null) {
            throw new IllegalArgumentException("Le type d'installation ne peut pas être null.");
        }
        this.kind = CaveDropKind.FACILITY;
        this.seedType = null;
        this.facilityType = facilityType;
        this.quantity = Math.max(1, quantity);
        this.selectionWeight = Math.max(0.0, selectionWeight);
    }

    public CaveDropDefinition(int woodQuantity, double selectionWeight) {
        this.kind = CaveDropKind.WOOD;
        this.seedType = null;
        this.facilityType = null;
        this.quantity = Math.max(1, woodQuantity);
        this.selectionWeight = Math.max(0.0, selectionWeight);
    }

    public CaveDropKind getKind() {
        return kind;
    }

    public Type getSeedType() {
        return seedType;
    }

    public FacilityType getFacilityType() {
        return facilityType;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSelectionWeight() {
        return selectionWeight;
    }

    public boolean isSeed() {
        return kind == CaveDropKind.SEED;
    }

    public boolean isFacility() {
        return kind == CaveDropKind.FACILITY;
    }

    public boolean isWood() {
        return kind == CaveDropKind.WOOD;
    }
}
