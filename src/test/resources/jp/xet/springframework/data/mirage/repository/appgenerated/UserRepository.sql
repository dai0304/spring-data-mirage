-- UserRepository.sql

SELECT *
FROM users

/*BEGIN*/
WHERE
	/*IF username != null*/
	  username = /*username*/'abc'
	/*END*/

	/*IF id != null*/
	  AND username = /*id*/'abc'
	/*END*/

	/*IF after != null*/
		AND
    /*IF direction != 'DESC'*/
	  	  username > /*after*/'aa'
-- ELSE username < /*after*/'zz'
		/*END*/
	/*END*/

	/*IF before != null*/
		AND
		/*IF direction != 'DESC'*/
		    username < /*before*/'zz'
-- ELSE	username > /*before*/'aa'
		/*END*/
	/*END*/

	/*IF ids != null*/
		AND
		/*IF ids.isEmpty() == false*/
		    username IN /*ids*/('ff', 'kk', 'pp')
-- ELSE false
		/*END*/
	/*END*/
/*END*/

/*BEGIN*/
ORDER BY
  /*IF orders == null*/
    username
    /*IF (direction != 'DESC' && before == null) || (direction == 'DESC' && before != null)*/
        ASC
-- ELSE DESC
    /*END*/
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

/*IF forUpdate == true*/
FOR UPDATE
/*END*/
