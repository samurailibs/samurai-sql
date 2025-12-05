package jp.dodododo.dao.impl;

import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.annotation.Table;
import jp.dodododo.dao.dialect.HSQL;
import jp.dodododo.dao.id.EntityId;
import jp.dodododo.dao.id.Sequence;

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
