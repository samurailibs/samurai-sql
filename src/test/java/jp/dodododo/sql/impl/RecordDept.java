package jp.dodododo.sql.impl;

import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.annotation.Table;
import jp.dodododo.sql.dialect.HSQL;
import jp.dodododo.sql.id.EntityId;
import jp.dodododo.sql.id.Sequence;

import java.util.List;

@Table("dept")
public record RecordDept(
        @Id({@IdDefSet(strategy = Sequence.class, name = "sequence", db = HSQL.class)})
        EntityId deptno,
        String dname,
        List<RecordEmp> empList) {

        @Table("emp")
        public record RecordEmp(String empno) {
        }
}
