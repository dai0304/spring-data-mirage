SELECT *
	FROM /*$table*/some_table

	/*BEGIN*/
	WHERE
		/*IF include_logical_deleted == null */
		id > 0
		/*END*/

		/*IF id != null*/
		AND id = /*id*/10
		/*END*/

		/*IF absid != null*/
		AND ABS(id) = /*absid*/10
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
