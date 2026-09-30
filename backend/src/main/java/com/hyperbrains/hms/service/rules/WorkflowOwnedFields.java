package com.hyperbrains.hms.service.rules;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Which workflow-owned fields a hand-written update would change.
 *
 * <p>Phase 3's domain-operation rule needs the same refusal in a dozen places, and writing it out a dozen
 * times is how one of them ends up subtly different from the rest. So it is written once, against field
 * names, and each entity says which of its fields the generated CRUD may not touch.
 *
 * <p>Reading the field by name is a little unusual for a guard, and it is the reason this class throws when a
 * name does not exist: a guard that silently protects a field that is not there is worse than no guard,
 * because it looks like one. A typo fails the build instead of opening a hole.
 */
public final class WorkflowOwnedFields {

    private WorkflowOwnedFields() {}

    /**
     * The names of the given fields whose value differs between the request and the stored row.
     *
     * @param requested the row as the request would leave it
     * @param stored the row as it is
     * @param nullMeansUnchanged true for a PATCH, where an absent field means "leave it alone", and false for a
     *     PUT, which carries the whole record and therefore means "clear it"
     * @param fieldNames the workflow-owned fields to compare
     * @return the names that changed, empty when none did
     */
    public static List<String> changed(Object requested, Object stored, boolean nullMeansUnchanged, String... fieldNames) {
        List<String> changedNames = new ArrayList<>();
        for (String fieldName : fieldNames) {
            Field field = fieldOf(stored.getClass(), fieldName);
            Object requestedValue = read(field, requested);
            Object storedValue = read(field, stored);
            if (differs(requestedValue, storedValue, nullMeansUnchanged)) {
                changedNames.add(fieldName);
            }
        }
        return changedNames;
    }

    /**
     * Whether two values count as different.
     *
     * <p>Two cases are deliberately not {@link Objects#equals}. A null on a PATCH means "leave it alone"
     * rather than "clear it". And a rate is the same rate whatever the scale says, so {@code 8000.0} and
     * {@code 8000.00} changing by name alone would refuse an edit that changed nothing.
     *
     * <p>A reference to another row is compared as it is, by identity. Comparing two references by id instead
     * was tried and reverted: reading an id off a Hibernate-backed reference threw, and a guard that throws is
     * worse than a guard that is overly cautious, because the cautious direction here refuses an edit rather
     * than allowing one.
     */
    private static boolean differs(Object requested, Object stored, boolean nullMeansUnchanged) {
        if (requested == null && nullMeansUnchanged) {
            return false;
        }
        if (requested instanceof BigDecimal requestedAmount && stored instanceof BigDecimal storedAmount) {
            return requestedAmount.compareTo(storedAmount) != 0;
        }
        return !Objects.equals(requested, stored);
    }

    /** The declared field, from the class itself or any class above it, or a failure that names the mistake. */
    private static Field fieldOf(Class<?> type, String fieldName) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ex) {
                // Walk up: a mapped superclass may hold the field.
            }
        }
        throw new IllegalArgumentException(
            "%s has no field %s, so this guard would protect nothing".formatted(type.getSimpleName(), fieldName)
        );
    }

    private static Object read(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(field.getName() + " could not be read", ex);
        }
    }
}
