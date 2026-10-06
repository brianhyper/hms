package com.hyperbrains.hms.service.dto.view;

/**
 * One half of a discharge, as the caller sends it.
 *
 * <p>There is no field for who is signing, and that is deliberate: the signer is the authenticated caller, taken
 * from the security context. A request that nominated its own signer could attribute a discharge to somebody who
 * never saw the patient, and the entire value of two sign-offs is that the record says which two people gave them.
 *
 * @param note free text for the discharge note. A sign-off that says nothing leaves whatever is already there, so
 *     the first signer's note survives a second signer who has nothing to add.
 * @param acknowledgeOutstandingOrders whether the caller has seen the orders that are still running and is
 *     proceeding anyway. Required when there are any: Phase 2 §7 says the discharge surfaces them and the caller
 *     either resolves them or explicitly acknowledges and proceeds — and the acknowledgement is logged.
 */
public record DischargeSignOffRequestDTO(String note, boolean acknowledgeOutstandingOrders) {}
