package jp.dodododo.sql.object;

public class PropertyDescCacheScope implements AutoCloseable {
    public PropertyDescCacheScope() {
        PropertyDesc.cacheModeOn();
    }

    @Override
    public void close() {
        PropertyDesc.cacheModeOff();
    }
}
