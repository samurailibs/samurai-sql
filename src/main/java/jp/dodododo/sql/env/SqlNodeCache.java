package jp.dodododo.sql.env;

import jp.dodododo.sql.sql.node.Node;
import jp.dodododo.sql.sql.parse.SqlParser;
import jp.dodododo.sql.util.CacheUtil;

import java.util.Map;

public class SqlNodeCache {
    protected static final Map<String, Node> NODE_CACHE = CacheUtil.cacheMap();

    public Node getOrParse(String sql) {
        Node node = NODE_CACHE.get(sql);
        if (node != null) {
            return node;
        }
        node = new SqlParser(sql).parse();
        NODE_CACHE.put(sql, node);
        return node;
    }
}
