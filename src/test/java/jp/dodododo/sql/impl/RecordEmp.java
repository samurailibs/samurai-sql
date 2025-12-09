package jp.dodododo.sql.impl;

import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.annotation.Table;
import jp.dodododo.sql.dialect.HSQL;
import jp.dodododo.sql.id.EntityId;
import jp.dodododo.sql.id.Sequence;

@Table("emp")
public record RecordEmp(
        @Id({@IdDefSet(strategy = Sequence.class, name = "sequence", db = HSQL.class)})
        EntityId empno,
        String ename,
        @Column("job") String jobName,
        String mgr,
        String hiredate,
        String sal,
        String comm,
        Dept dept,
        String tstamp) {
}
