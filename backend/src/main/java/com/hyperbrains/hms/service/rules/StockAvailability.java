package com.hyperbrains.hms.service.rules;

/**
 * The arithmetic that decides whether a prescription can be filled later.
 *
 * <p>The whole point of the reservation system is that a drug promised to one patient is not offered
 * to another. The figure that matters is therefore never the shelf count: it is what is left after
 * subtracting everything already promised. Two nurses reading "40 in stock" and both prescribing 30
 * is exactly the failure this prevents.
 *
 * <p>Pure, so the boundary cases (exactly enough, one short, nothing reserved) are tested directly
 * rather than inferred from a database.
 */
public final class StockAvailability {

    private StockAvailability() {}

    /**
     * Units that can still be promised.
     *
     * <p>{@code reservedStock} must never exceed {@code currentStock} — {@link #canReserve} is what
     * keeps that invariant, and a negative result here means something bypassed it rather than a
     * legitimate state.
     */
    public static int available(int currentStock, int reservedStock) {
        return currentStock - reservedStock;
    }

    /** True when {@code requested} units can be set aside without breaking an existing promise. */
    public static boolean canReserve(int currentStock, int reservedStock, int requested) {
        return requested > 0 && available(currentStock, reservedStock) >= requested;
    }

    /**
     * How many units are missing, for an error that says what is actually short.
     *
     * <p>"Not enough stock" gives a pharmacist nothing to act on; "3 short" does.
     */
    public static int shortfall(int currentStock, int reservedStock, int requested) {
        return Math.max(requested - available(currentStock, reservedStock), 0);
    }

    /**
     * Below the reorder point.
     *
     * <p>Measured on availability, not shelf count: 100 units with 98 reserved is a drug about to run
     * out, however healthy the shelf looks.
     */
    public static boolean belowThreshold(int currentStock, int reservedStock, int lowStockThreshold) {
        return available(currentStock, reservedStock) < lowStockThreshold;
    }
}
