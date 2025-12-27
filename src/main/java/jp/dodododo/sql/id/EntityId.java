package jp.dodododo.sql.id;

import jp.dodododo.sql.types.JavaTypes;
import jp.dodododo.sql.types.TypeConverter;

import java.util.UUID;

/**
 * Holder for lazily assigned auto-increment IDs.
 *
 * <p>Designed to work with Java records to express the model:
 * "the record itself is immutable, while only the ID is determined after INSERT".</p>
 *
 * <h2>Usage with records</h2>
 * <pre>{@code
 * record Emp(EntityId id, String name) {}
 *
 * Emp e = new Emp(new EntityId(), "alice");
 * client.insert(e);          // The client calls e.id.assign(generatedId)
 * Long id = e.id.asLong();
 * }</pre>
 *
 * <h2>Constraints</h2>
 * <ul>
 *   <li>assign() can be called only once; subsequent calls throw an exception.</li>
 *   <li>raw()/asLong()/asString() throw IllegalStateException when unassigned.</li>
 *   <li>equals/hashCode are based on the assigned value; comparison is invalid before assignment.</li>
 *   <li>toString() returns "unassigned" until the value is assigned.</li>
 * </ul>
 */
public class EntityId {
    protected Object value;
    protected boolean assigned = false;

    public EntityId() {
    }

    public EntityId(Object value) {
        assign(value);
    }

    public void assign(Object value) {
        if (this.assigned) {
            throw new UnsupportedOperationException("assigned");
        }
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        this.value = value;
        this.assigned = true;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public Object raw() {
        if (!assigned) {
            throw new IllegalStateException("unassigned");
        }
        return value;
    }

    public Long asLong() {
        if (!assigned) {
            throw new IllegalStateException("unassigned");
        }
        return JavaTypes.LONG.convert(value);
    }

    public String asString() {
        if (!assigned) {
            throw new IllegalStateException("unassigned");
        }
        return JavaTypes.STRING.convert(value);
    }

    public <T> T to(Class<T> dest) {
        if (!assigned) {
            throw new IllegalStateException("unassigned");
        }
        return TypeConverter.convert(this.value, dest);
    }

    public <T> T as(Class<T> dest) {
        return to(dest);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof EntityId) {
            return this.raw().equals(((EntityId) obj).raw());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (!assigned) {
            return -1;
        }
        return this.raw().hashCode();
    }

    @Override
    public String toString() {
        if (!this.assigned) {
            return "unassigned";
        }
        return asString();
    }

    public boolean isNumber() {
        return value instanceof Number;
    }

    public boolean isUUID() {
        try {
            UUID.fromString(asString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isString() {
        return value instanceof CharSequence;
    }
}
