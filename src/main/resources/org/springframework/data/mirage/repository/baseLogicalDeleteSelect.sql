/*IF before != null*/
SELECT * FROM (
/*END*/


SELECT *
FROM /*$table*/some_table

/*BEGIN*/
WHERE
	/*IF include_logical_deleted != null && include_logical_deleted == false*/
	/*$id_column_name*/id > 0
	/*END*/

	/*IF after != null*/
		/*IF direction != 'DESC'*/
		AND /*$id_column_name*/id > /*after*/1
-- ELSE	AND /*$id_column_name*/id < /*after*/1
		/*END*/
	/*END*/

	/*IF before != null*/
		/*IF direction != 'DESC'*/
		AND /*$id_column_name*/id < /*before*/1
-- ELSE	AND /*$id_column_name*/id > /*before*/1
		/*END*/
	/*END*/
	
	/*IF id != null*/
	AND /*$id_column_name*/id = /*id*/10
	/*END*/

	/*IF ids != null*/
		/*IF ids.isEmpty() == false*/
		AND /*$id_column_name*/id IN /*ids*/(10, 20, 30)
-- ELSE	AND false
		/*END*/
	/*END*/

	/*IF absid != null*/
	AND ABS(/*$id_column_name*/id) = /*absid*/10
	/*END*/
/*END*/

/*IF orders == null*/
ORDER BY /*$id_column_name*/id
	/*IF direction == null || direction == 'ASC'*/
		/*IF before == null*/
		ASC
-- ELSE DESC
		/*END*/
	/*END*/
	/*IF direction == 'DESC'*/
		/*IF before != null*/
		ASC
-- ELSE DESC
		/*END*/
	/*END*/
-- ELSE ORDER BY /*$orders*/id
/*END*/

/*BEGIN*/
LIMIT
	/*IF offset != null*/
	/*offset*/0,
	/*END*/

	/*IF size != null*/
	/*size*/10
	/*END*/
/*END*/


/*IF before != null*/
) sub ORDER BY /*$id_column_name*/id /*$direction*/ASC
/*END*/
