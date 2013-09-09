SELECT *
FROM /*$table*/some_table

/*BEGIN*/
WHERE
	/*IF include_logical_deleted != null && include_logical_deleted == false*/
	/*$id_column_name*/id > 0
	/*END*/

	/*IF id != null*/
	AND /*$id_column_name*/id = /*id*/10
	/*END*/

	/*IF ids != null*/
	/*$id_column_name*/id IN /*ids*/(10, 20, 30)
	/*END*/

	/*IF absid != null*/
	AND ABS(/*$id_column_name*/id) = /*absid*/10
	/*END*/
/*END*/

/*IF orders != null*/
ORDER BY /*$orders*/id
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
