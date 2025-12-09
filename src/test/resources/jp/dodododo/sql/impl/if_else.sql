select
	*
from
	EMP
/*BEGIN*/
where
	/*IF no != null*/ EMPNO = /*no*/0 --ELSE EMPNO = 2 /*END*/
/*END*/
