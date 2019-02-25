SELECT *
FROM /*$table*/example_table

/*BEGIN*/
WHERE
	/*IF id != null*/
	  /*$id_column_name*/id = /*id*/'kk'
	/*END*/

	/*IF after != null*/
		AND
    /*IF direction != 'DESC'*/
	  	  /*$id_column_name*/id > /*after*/'aa'
-- ELSE /*$id_column_name*/id < /*after*/'zz'
		/*END*/
	/*END*/

	/*IF before != null*/
		AND
		/*IF direction != 'DESC'*/
		    /*$id_column_name*/id < /*before*/'zz'
-- ELSE	/*$id_column_name*/id > /*before*/'aa'
		/*END*/
	/*END*/

	/*IF ids != null*/
		AND
		/*IF ids.isEmpty() == false*/
		    /*$id_column_name*/id IN /*ids*/('ff', 'kk', 'pp')
-- ELSE false
		/*END*/
	/*END*/
/*END*/

/*BEGIN*/
ORDER BY
  /*IF orders == null*/
    /*$id_column_name*/id
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
