SELECT *
FROM chunk_entities c

/*BEGIN*/
WHERE
	/*IF id != null*/
	c.id = /*id*/'1'
	/*END*/

	/*IF after != null*/
		/*IF direction != 'DESC'*/
		AND c.id > /*after*/'1'
-- ELSE	AND c.id < /*after*/1
		/*END*/
	/*END*/

	/*IF before != null*/
		/*IF direction != 'DESC'*/
		AND c.id < /*before*/'1'
-- ELSE	AND c.id > /*before*/1
		/*END*/
	/*END*/

	/*IF ids != null*/
		/*IF ids.isEmpty() == false*/
		AND c.id IN /*ids*/('1', '2', '3')
-- ELSE	AND false
		/*END*/
	/*END*/
/*END*/

/*IF orders == null*/
ORDER BY id
	/*IF (direction != 'DESC' && before == null) || (direction == 'DESC' && before != null)*/
		ASC
-- ELSE	DESC
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
