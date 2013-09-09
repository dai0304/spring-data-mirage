-- UserRepository.sql

SELECT *
FROM users

/*BEGIN*/
WHERE
	/*IF orders != null*/
	username = /*username*/'abc'
	/*END*/

	/*IF id != null*/
	AND username = /*id*/'abc'
	/*END*/

	/*IF ids != null*/
	AND username IN /*ids*/('abc', 'def', 'ghi')
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
