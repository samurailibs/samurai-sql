package jp.dodododo.sql.util;

import jp.dodododo.sql.annotation.Internal;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArgsUtil {

    @Internal
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<?> getList(Object[] entity) {
        for (Object object : entity) {
            if (object instanceof List) {
                return (List<?>) object;
            }
            if (object instanceof Map) {
                Set<Map.Entry> entrySet = ((Map) object).entrySet();
                for (Map.Entry entry : entrySet) {
                    Object value = entry.getValue();
                    if (value instanceof List) {
                        return (List<?>) value;
                    }
                }
            }
        }
        return null;
    }
}
