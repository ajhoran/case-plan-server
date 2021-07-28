-- :name get-next-id :? :1
SELECT CASEPLAN_ID_SEQ.NEXTVAL
FROM DUAL

-- :name get-recent-approved-plan-id :? :1
SELECT MAX(PLAN_ID) PLAN_ID
FROM CASE_PLAN
WHERE CLIENT_ID = :client-id
  AND CASE_ID = :case-id
  AND STATUS = 'APPROVED'

-- :name get-plan-approved-review-id :? :1
SELECT MAX(REVIEW_ID) REVIEW_ID
FROM REVIEW_REPORT
WHERE PLAN_ID = :plan-id
  AND STATUS = 'APPROVED'

-- :name get-offices :? :*
SELECT OFFICE,
       NAME,
       STREET_ADDR_1,
       STREET_ADDR_2,
       STREET_CITY_POSTCODE,
       POSTAL_ADDR,
       POSTAL_CITY_POSTCODE,
       TELEPHONE,
       TELEPHONE_TOLL_FREE,
       FAX,
       ABN,
       EMAIL,
       WEBSITE
FROM OFFICE_DETAIL

-- :name get-c3client :? :1
SELECT CLIENT_ID,
       DISPLAY_NAME,
       FIRST_NAME,
       TO_CHAR(C3MS_DOB, 'YYYY-MM-DD') C3MS_DOB,
       C3MS_SEX,
       C3MS_INDIGENOUS
FROM CASEPLAN_CLIENT_VW
WHERE CLIENT_ID = :client-id

-- :name get-c3relsroles :? :*
SELECT C3MS_ID,
       DISPLAY_NAME,
       PERSON_ROLE
FROM CASEPLAN_RELS_ROLES_VW
WHERE CLIENT_ID = :client-id

-- :name get-c3relations :? :*
SELECT C3MS_ID,
       DISPLAY_NAME,
       RELATIONSHIP,
       ADDR_STREET,
       ADDR_CITY,
       ADDR_POSTCODE
FROM CASEPLAN_RELS_VW
WHERE CLIENT_ID = :client-id

-- :name get-c3worker :? :1
SELECT WORKER_ID,
       DISPLAY_NAME WORKER_NAME
FROM CASEPLAN_WORKERS_VW
WHERE WORKER_ID = :worker-id

-- :name get-c3workers :? :*
SELECT WORKER_ID,
       DISPLAY_NAME WORKER_NAME
FROM CASEPLAN_WORKERS_VW

-- :name get-session-security :? :1
SELECT 1 AUTH
FROM SESSION_SECURITY
WHERE TOKEN = :token
  AND CASE_ID = :case-id
  AND CLIENT_ID = :client-id
  AND USER_ID = :user-id
  AND TARGET = :target
  AND TARGET_ID = :target-id
  AND READONLY = :readonly
  AND INSERTED_DATETIME > SYSDATE - (:timeout / 24)

-- :name insert-session-security :! :n
INSERT INTO SESSION_SECURITY (
    TOKEN, CASE_ID, CLIENT_ID, USER_ID, TARGET, TARGET_ID, READONLY, INSERTED_DATETIME)
VALUES (
    :token, :case-id, :client-id, :user-id, :target, :target-id, :readonly, SYSDATE)

-- :name delete-session-security-aged :! :n
DELETE FROM SESSION_SECURITY
WHERE INSERTED_DATETIME < SYSDATE - (:timeout / 24)

-- :name update-session-id :! :n
UPDATE SESSION_SECURITY
SET TARGET_ID = :target-id
WHERE TOKEN = :token

-- :name insert-audit-log :! :n
INSERT INTO AUDIT_LOG (
    AUDIT_ID, TIME_STAMP, USER_ID, TARGET, TARGET_ID, SAVE_TYPE, JSON)
VALUES (
    AUDIT_ID_SEQ.NEXTVAL, SYSDATE, :user-id, :target, :target-id, :save-type, :json)

-- :name has-plan-been-reviewed :? :1
SELECT CASE
           WHEN EXISTS (SELECT REVIEW_ID
                        FROM REVIEW_REPORT
                        WHERE PLAN_ID = :plan-id AND STATUS <> 'CANCELLED')
               THEN 'Y'
           ELSE 'N'
           END AS REVIEWED
FROM DUAL
