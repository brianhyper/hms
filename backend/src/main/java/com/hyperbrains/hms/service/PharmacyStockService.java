package com.hyperbrains.hms.service;

import com.hyperbrains.hms.domain.Drug;

/**
 * The one place pharmacy stock is allowed to change.
 *
 * <p>Reserving is a read-modify-write on the same two columns from several stations at once — two
 * prescribers can reach this method in the same instant for the same drug. Keeping every mutation
 * behind this interface is what makes that safe: the counter that matters is the one derived here,
 * not one a caller computed earlier and then acted on.
 */
public interface PharmacyStockService {

    /**
     * Set {@code quantity} units of a drug aside for a patient.
     *
     * <p>Deliberately does not touch {@code currentStock}. The medicine is still on the shelf and
     * still the pharmacy's; what changes is that it is no longer on offer to anyone else. Only the
     * physical hand-over reduces the shelf count.
     *
     * <p>Reloads the drug inside the current transaction before deciding, so the availability check
     * and the increment cannot be separated by another writer.
     *
     * @throws BusinessRuleViolationException if the drug is missing, inactive, or short by any amount
     */
    Drug reserve(Long drugId, int quantity);

    /**
     * Give back units that were set aside but will never be handed over.
     *
     * <p>The mirror of {@link #reserve} and the only way a reservation is undone before it is
     * dispensed. {@code currentStock} is untouched, because nothing ever left the shelf.
     *
     * @throws IllegalArgumentException if asked to release more than is actually reserved, which can
     *         only mean the reservation bookkeeping has already been corrupted. Failing loudly keeps
     *         the remaining reservation intact instead of driving it negative.
     */
    Drug release(Long drugId, int quantity);

    /**
     * Record a delivery: units arriving from a supplier.
     *
     * <p>This is the sanctioned way for stock to increase. The generated {@code /api/drugs} write is
     * closed to pharmacy precisely because it can set {@code reservedStock} directly, which would
     * silently break a reservation this service made.
     */
    Drug receive(Long drugId, int quantity, String reference);

    /**
     * Record stock leaving the shelf for a reason other than dispensing: breakage, expiry, loss.
     *
     * <p>Bounded by availability, not by the shelf count. Units already reserved belong to a patient
     * and are not the pharmacy's to write off; if that medicine really is unusable, the prescription
     * has to be withdrawn first.
     *
     * @throws BusinessRuleViolationException if more units are written off than are unreserved
     */
    Drug writeOff(Long drugId, int quantity, String reference);

    /**
     * Hand reserved medicine over: the units finally leave the shelf, and the promise against them is
     * discharged in the same movement.
     *
     * <p>This is the only path that reduces {@code currentStock} for a prescription, and it requires
     * the units to have been reserved first. That is the whole point of the reservation: by the time
     * anyone reaches this call, the medicine is already known to be there.
     *
     * @throws IllegalArgumentException if fewer units are reserved than are being handed over, which
     *         means the reservation bookkeeping is already broken. Failing loudly is safer than
     *         consuming stock a different patient is holding a promise against.
     */
    Drug consume(Long drugId, int quantity);
}
