/*IF before != null*/
SELECT * FROM (
/*END*/


SELECT *
FROM sample_chunk c

/*BEGIN*/
WHERE
	/*IF id != null*/
	c.feed_id = /*id*/1
	/*END*/

	/*IF after != null*/
		/*IF direction != 'DESC'*/
		AND c.feed_id > /*after*/1
-- ELSE	AND c.feedId < /*after*/1
		/*END*/
	/*END*/

	/*IF before != null*/
		/*IF direction != 'DESC'*/
		AND c.feed_id < /*before*/1
-- ELSE	AND c.feedId > /*before*/1
		/*END*/
	/*END*/

	/*IF ids != null*/
		/*IF ids.isEmpty() == false*/
		AND c.feed_id IN /*ids*/('1', '2', '3')
-- ELSE	AND false
		/*END*/
	/*END*/
/*END*/

/*IF orders == null*/
ORDER BY feed_id
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
-- ELSE ORDER BY /*$orders*/feedId
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
) sub ORDER BY feed_id /*$direction*/ASC
/*END*/
