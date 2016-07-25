/*IF after == null && before == null*/
	SELECT *
	FROM /*$table*/some_table
	
	/*BEGIN*/
	WHERE
		/*IF id != null*/
		/*$id_column_name*/id = /*id*/10
		/*END*/
	
		/*IF ids != null*/
			/*IF ids.isEmpty() == false*/
			AND /*$id_column_name*/id IN /*ids*/(10, 20, 30)
-- ELSE		AND false
			/*END*/
		/*END*/
	/*END*/
	
	/*BEGIN*/
	ORDER BY
		/*IF orders == null*/
		/*$id_column_name*/id ASC
-- ELSE	/*$orders*/id
		/*END*/
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
/*END*/



/*IF after != null && before == null && (direction == null || direction == 'ASC')*/
	SELECT *
	FROM /*$table*/some_table
	WHERE /*$id_column_name*/id > /*after*/1
	ORDER BY /*$id_column_name*/id ASC
	
	/*IF size != null*/
	LIMIT /*size*/10
	/*END*/
/*END*/



/*IF after != null && before == null && direction == 'DESC'*/
	SELECT * FROM (
		
		SELECT *
		FROM /*$table*/some_table
		WHERE /*$id_column_name*/id > /*after*/1
		ORDER BY /*$id_column_name*/id ASC
		
		/*IF size != null*/
		LIMIT /*size*/10
		/*END*/
		
	) sub ORDER BY /*$id_column_name*/id DESC
/*END*/



/*IF after == null && before != null && direction == 'DESC'*/
	SELECT *
	FROM /*$table*/some_table
	WHERE /*$id_column_name*/id > /*before*/1
	ORDER BY /*$id_column_name*/id DESC
	
	/*IF size != null*/
	LIMIT /*size*/10
	/*END*/
/*END*/



/*IF after == null && before != null && (direction == null || direction == 'ASC')*/
	SELECT * FROM (
		
		SELECT *
		FROM /*$table*/some_table
		WHERE /*$id_column_name*/id > /*before*/1
		ORDER BY /*$id_column_name*/id DESC
		
		/*IF size != null*/
		LIMIT /*size*/10
		/*END*/
		
	) sub ORDER BY /*$id_column_name*/id ASC
/*END*/
