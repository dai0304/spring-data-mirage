-- IdGeneratedEntityRepository_findByStr.sql

SELECT *
FROM id_generated_entities

WHERE str = /*str*/'abc'

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
