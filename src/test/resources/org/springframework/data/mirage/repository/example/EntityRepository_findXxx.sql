-- EntityRepository_findXxx.sql

SELECT *
FROM samples

/*IF orders != null*/
ORDER BY /*$orders*/id
/*END*/

WHERE str = /*str*/''

/*BEGIN*/
LIMIT
	/*IF offset != null*/
	/*offset*/0,
	/*END*/

	/*IF size != null*/
	/*size*/10
	/*END*/
/*END*/
