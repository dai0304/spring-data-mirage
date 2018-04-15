-- EntityRepository_findByStr.sql

SELECT *
FROM samples

WHERE str LIKE concat(/*str*/'abc', '%')

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
