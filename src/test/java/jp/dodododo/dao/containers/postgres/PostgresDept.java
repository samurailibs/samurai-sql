package jp.dodododo.dao.containers.postgres;

import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.annotation.Property;
import jp.dodododo.dao.annotation.Table;
import jp.dodododo.dao.dialect.PostgreSQL;
import jp.dodododo.dao.id.GeneratedValue;

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
