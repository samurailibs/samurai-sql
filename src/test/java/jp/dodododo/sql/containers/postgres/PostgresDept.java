package jp.dodododo.sql.containers.postgres;

import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.annotation.Property;
import jp.dodododo.sql.annotation.Table;
import jp.dodododo.sql.dialect.PostgreSQL;
import jp.dodododo.sql.id.GeneratedValue;

@Table("DEPT")
public class PostgresDept {
    @Id(@IdDefSet(strategy = GeneratedValue.class, db = PostgreSQL.class))
    @Property
    private Long deptno;

    @Property
    private String dname;

    public PostgresDept(){
    }

    public void setDName(String dname) {
        this.dname = dname;
    }

    public Long getDeptNo() {
        return deptno;
    }
}
