-- EntityRepository_findChunk.sql

/*IF after == null && before == null*/
-- first
	SELECT *
	FROM samples

    WHERE str LIKE 'b%'
	/*BEGIN*/
		/*IF id != null*/
		AND id = /*id*/10
		/*END*/

		/*IF ids != null*/
			/*IF ids.isEmpty() == false*/
			AND id IN /*ids*/(10, 20, 30)
-- ELSE		AND false
			/*END*/
		/*END*/
	/*END*/

	/*BEGIN*/
	ORDER BY
		/*IF orders == null*/
		id /*$direction*/ASC
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
-- ascending forward
	SELECT *
	FROM samples
    WHERE str LIKE 'b%'
	    AND id > /*after*/1
	ORDER BY id ASC

	/*IF size != null*/
	LIMIT /*size*/10
	/*END*/
/*END*/


/*IF after != null && before == null && direction == 'DESC'*/
-- descending forward
	SELECT *
	FROM samples
	WHERE str LIKE 'b%'
	    AND id < /*after*/1
	ORDER BY id DESC

	/*IF size != null*/
	LIMIT /*size*/10
	/*END*/
/*END*/


/*IF after == null && before != null && direction == 'DESC'*/
-- descending backword
	SELECT * FROM (

		SELECT *
		FROM samples
		WHERE str LIKE 'b%'
	    AND id > /*before*/1
		ORDER BY id ASC

		/*IF size != null*/
		LIMIT /*size*/10
		/*END*/

	) sub ORDER BY id DESC
/*END*/


/*IF after == null && before != null && (direction == null || direction == 'ASC')*/
-- ascending backword
	SELECT * FROM (

		SELECT *
		FROM samples
		WHERE str LIKE 'b%'
	    AND id < /*before*/1
		ORDER BY id DESC

		/*IF size != null*/
		LIMIT /*size*/10
		/*END*/

	) sub ORDER BY id ASC
/*END*/


/*IF forUpdate != null && forUpdate == true*/
FOR UPDATE
/*END*/
