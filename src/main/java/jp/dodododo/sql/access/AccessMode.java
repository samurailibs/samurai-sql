package jp.dodododo.sql.access;

public enum AccessMode {
    READ_WRITE(true, true),
    READ_ONLY(true, false),
    WRITE_ONLY(false, true),
    IGNORE(false, false);

    boolean readable;
    boolean writeable;

    AccessMode(boolean readable, boolean writeable) {
        this.readable = readable;
        this.writeable = writeable;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWriteable() {
        return writeable;
    }
}
