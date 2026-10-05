package com.hyperbrains.hms.service.dto.view;

import java.io.Serializable;

/**
 * The password an administrator has just been given to hand over.
 *
 * <p>It is returned once, in the response to the call that created it, and there is no route that reads it back. It
 * is not mailed, not stored anywhere in readable form, and not written to the audit trail: the trail records that a
 * password was set, by whom and why, never what it was.
 *
 * <p>If it is lost before it is handed over, the answer is to call the same route again: that replaces it, and ends
 * any session that was opened with the previous one.
 */
public record InitialPasswordDTO(String password) implements Serializable {}
