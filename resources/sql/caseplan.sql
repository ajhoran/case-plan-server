-- :name get-case-plan :? :1
SELECT PLAN_ID,
       CLIENT_ID,
       CASE_ID,
       STATUS,
       PLAN_GOAL,
       ACIST,
       REVIEW_DUE_DATE,
       APPROVED_DATETIME,
       APPROVED_BY,
       APPROVED_BY_NAME,
       LAST_MODIFIED_DATETIME,
       LAST_MODIFIED_BY,
       LAST_MODIFIED_BY_NAME,
       CREATED_DATETIME,
       CREATED_BY,
       CREATED_BY_NAME
FROM CASE_PLAN
WHERE PLAN_ID = :plan-id

-- :name get-case-plan-client :? :1
SELECT CLIENT_ID,
       DISPLAY_NAME,
       FIRST_NAME,
       SEX,
       DOB,
       IS_ABORIGINAL,
       IS_TSI,
       ATSI_NAME,
       ATSI_TOTEM,
       ATSI_NATION,
       ATSI_LANG,
       ATSI_INTERP,
       ATSI_INTERP_LANG,
       CALD_ANSWER,
       CALD_COUNTRY,
       CALD_ETHNIC,
       CALD_LANG,
       CALD_RELIGION,
       CALD_INTERP,
       CALD_INTERP_LANG
FROM CASE_PLAN_CLIENT
WHERE PLAN_ID = :plan-id

-- :name get-case-plan-general :? :1
SELECT BACKGROUND_STORY,
       DISPUTE
FROM CASE_PLAN_GENERAL
WHERE PLAN_ID = :plan-id

-- :name get-about-me :? :1
SELECT THINGS_IMPORTANT,
       PEOPLE_IMPORTANT,
       GOOD_AT,
       HELP_ME,
       NEED_HELP
FROM ABOUT_ME
WHERE PLAN_ID = :plan-id

-- :name get-family-group-conf :? :1
SELECT HAS_OCCURRED,
       OCCURRED_DATE,
       SCHEDULED_FUTURE,
       SCHEDULED_DATE,
       NOT_SCHEDULED_REASON
FROM FAMILY_GROUP_CONF
WHERE PLAN_ID = :plan-id

-- :name get-outcomes :? :*
SELECT ORD,
       CONCERN,
       OUTCOME
FROM OUTCOMES
WHERE PLAN_ID = :plan-id

-- :name get-outcomes-actions :? :*
SELECT OUTCOME_ORD,
       ORD,
       ACTION,
       WHO,
       PLANNED_WHEN,
       MEASURE
FROM OUTCOMES_ACTIONS
WHERE PLAN_ID = :plan-id

-- :name get-care-team-family :? :*
SELECT CLIENT_ID,
       DISPLAY_NAME,
       RELATIONSHIP,
       CULTURAL_BACKGROUND,
       COUNTRY_ORIGIN,
       LANGUAGE_GROUP,
       LANGUAGE_SPOKEN
FROM CARE_TEAM_FAMILY
WHERE PLAN_ID = :plan-id

-- :name get-care-team-professionals :? :*
SELECT PERSON_TYPE,
       ORD,
       DISPLAY_NAME,
       PERSON_ROLE,
       CONTACT_DETAILS
FROM CARE_TEAM_PROFESSIONALS
WHERE PLAN_ID = :plan-id

-- :name get-placement :? :1
SELECT CONSULT_OCCURRED,
       CONSULT_DATE,
       CONSULT_OUTCOME,
       REGISTERED_PUBLIC,
       REGISTERED_PUBLIC_DATE,
       ASSESSMENT_SUMMARY
FROM PLACEMENT
WHERE PLAN_ID = :plan-id

-- :name get-culture-identity :? :1
SELECT GENOGRAM_COMPLETED,
       GENOGRAM_DATE,
       HAS_LIFESTORY,
       RECEIVED_LIFESTORY_DATE,
       UPDATED_LIFESTORY_DATE,
       RESPONSIBLE_LIFESTORY,
       ASSESSMENT_SUMMARY
FROM CULTURE_IDENTITY
WHERE PLAN_ID = :plan-id

-- :name get-atsi :? :1
SELECT GENOGRAM_COMPLETED,
       GENOGRAM_DATE,
       HAS_LIFESTORY,
       RECEIVED_LIFESTORY_DATE,
       UPDATED_LIFESTORY_DATE,
       RESPONSIBLE_LIFESTORY,
       HAS_MENTOR,
       MENTOR_DETAILS,
       HAS_SERVICES_LINKS,
       ATTENDS_CULTURAL,
       LIVING_OWN_COUNTRY,
       CARE_TEAM_SUPPORT,
       LAST_VISIT,
       RECONNECT_OTHER_DETAILS,
       CARER_ACK,
       CARER_NOT_ACK_DETAILS,
       CARER_DISCUSS,
       CARER_HAD_TRAINING,
       CARER_SUPPORTS,
       CARER_OTHER_SUPPORTS,
       CONSULTATION_OCCURRED,
       ASSESSMENT_SUMMARY
FROM ATSI
WHERE PLAN_ID = :plan-id

-- :name get-atsi-orgs :? :*
SELECT ORD,
       NAME,
       SERVICE
FROM ATSI_ORGS
WHERE PLAN_ID = :plan-id

-- :name get-atsi-events :? :*
SELECT ORD,
       NAME,
       WHO,
       EVENT_DATE,
       LOCATION,
       DETAILS
FROM ATSI_EVENTS
WHERE PLAN_ID = :plan-id

-- :name get-atsi-reconnect :? :*
SELECT ORD,
       FREQUENCY,
       TRAVEL,
       SUPERVISOR,
       CONTACT
FROM ATSI_RECONNECT
WHERE PLAN_ID = :plan-id

-- :name get-atsi-carer-training :? :*
SELECT ORD,
       PROVIDER,
       TRAINING_DATE,
       LOCATION,
       REVIEW_DATE,
       OTHER_DETAILS
FROM ATSI_CARER_TRAINING
WHERE PLAN_ID = :plan-id

-- :name get-atsi-consultation :? :*
SELECT ORD,
       WHO,
       RELATIONSHIP,
       PLANNED_WHEN,
       OUTCOME
FROM ATSI_CONSULTATION
WHERE PLAN_ID = :plan-id

-- :name get-cald :? :1
SELECT GENOGRAM_COMPLETED,
       GENOGRAM_DATE,
       HAS_LIFESTORY,
       RECEIVED_LIFESTORY_DATE,
       UPDATED_LIFESTORY_DATE,
       RESPONSIBLE_LIFESTORY,
       RELIGIOUS_REQ,
       RELIGIOUS_REQ_DETAILS,
       HAS_SERVICES_LINKS,
       ATTENDS_CULTURAL,
       CARER_ACK,
       CARER_NOT_ACK_DETAILS,
       CARER_DISCUSS,
       CARER_HAD_TRAINING,
       CARER_SUPPORTS,
       CARER_OTHER_SUPPORTS,
       CONSULTATION_OCCURRED,
       ASSESSMENT_SUMMARY
FROM CALD
WHERE PLAN_ID = :plan-id

-- :name get-cald-orgs :? :*
SELECT ORD,
       NAME,
       SERVICE
FROM CALD_ORGS
WHERE PLAN_ID = :plan-id

-- :name get-cald-events :? :*
SELECT ORD,
       NAME,
       WHO,
       EVENT_DATE,
       LOCATION,
       DETAILS
FROM CALD_EVENTS
WHERE PLAN_ID = :plan-id

-- :name get-cald-carer-training :? :*
SELECT ORD,
       PROVIDER,
       TRAINING_DATE,
       LOCATION,
       REVIEW_DATE,
       OTHER_DETAILS
FROM CALD_CARER_TRAINING
WHERE PLAN_ID = :plan-id

-- :name get-cald-consultation :? :*
SELECT ORD,
       WHO,
       RELATIONSHIP,
       PLANNED_WHEN,
       OUTCOME
FROM CALD_CONSULTATION
WHERE PLAN_ID = :plan-id

-- :name get-contact-arrangements :? :1
SELECT ASSESSMENT_SUMMARY
FROM CONTACT_ARRANGEMENTS
WHERE PLAN_ID = :plan-id

-- :name get-contact-determination :? :*
SELECT ORD,
       CLIENT_ID,
       DISPLAY_NAME,
       CONTACT_DETERMINATION,
       ADDR_STREET,
       ADDR_CITY,
       ADDR_POSTCODE,
       OFFICE,
       WORKER_NAME,
       WORKER_ID,
       SUPERVISOR_NAME,
       SUPERVISOR_ID,
       ISSUE_DATE,
       PREVIOUS_ISSUE_DATE,
       CONTACT_PURPOSE,
       CONTACT_ATTEND,
       CONTACT_TYPE,
       CONTACT_LOCATION,
       CONTACT_FREQUENCY,
       CONTACT_SUPERVISION,
       CONTACT_OTHER_CONDITIONS,
       NO_CONTACT_PREFACE,
       NO_CONTACT_REASON,
       GENERATE_STATUS,
       APPROVED_BY,
       APPROVED_BY_NAME,
       APPROVED_DATETIME
FROM CONTACT_DETERMINATION
WHERE PLAN_ID = :plan-id

-- :name get-endorsement-approval-contact :? :*
SELECT ORD,
       STATUS,
       ACTION,
       ACTION_DATETIME,
       TO_WORKER_ID,
       TO_WORKER_NAME,
       FROM_WORKER_ID,
       FROM_WORKER_NAME,
       RESULT,
       RESULT_WORKER_ID,
       RESULT_WORKER_NAME,
       RESULT_DATETIME,
       WORKER_COMMENT
FROM ENDORSEMENT_APPROVAL_CONTACT
WHERE PLAN_ID = :plan-id

-- :name get-physical-health :? :1
SELECT MEDICARE_NUMBER,
       MEDICARE_EXPIRY,
       ASSESSED_HEALTH,
       ASSESSED_HEALTH_DATE,
       RECOMMENDED_HEALTH,
       IMMUNISATIONS_COPY,
       IMMUNISATIONS_CURRENT,
       HAS_HEALTH_CARDS,
       HAS_MEDICAL_REPORTS,
       ASSESSMENT_SUMMARY
FROM PHYSICAL_HEALTH
WHERE PLAN_ID = :plan-id

-- :name get-disability-devdelay :? :1
SELECT HAS_DISABILITY,
       DISABILITY_OTHER,
       NDIS_HAS_PLAN,
       NDIS_PLAN_NUMBER,
       NDIS_EXPIRY_DATE,
       NDIS_SUPPORTS,
       NDIS_SUPPORTS_NO,
       NDIS_TRANSITION,
       NDIS_ACCESS_SUBMITTED,
       CONSULTANT_NOTIFIED,
       CONSULTED_DATE,
       ASSESSMENT_SUMMARY
FROM DISABILITY_DEVDELAY
WHERE PLAN_ID = :plan-id

-- :name get-disabilities :? :*
SELECT ORD,
       DISABILITY_TYPE
FROM DISABILITIES
WHERE PLAN_ID = :plan-id

-- :name get-emotional :? :1
SELECT YJ_INVOLVE,
       YJ_SUMMARY,
       ASSESSMENT_SUMMARY
FROM EMOTIONAL
WHERE PLAN_ID = :plan-id

-- :name get-education :? :1
SELECT INDIVIDUAL_PLAN,
       INDIVIDUAL_PLAN_DATE,
       NEGOTIATED_PLAN,
       ASSESSMENT_SUMMARY
FROM EDUCATION
WHERE PLAN_ID = :plan-id

-- :name get-recreation :? :1
SELECT ASSESSMENT_SUMMARY
FROM RECREATION
WHERE PLAN_ID = :plan-id

-- :name get-independent-living :? :1
SELECT HAS_DOCUMENTS,
       HAS_CREATE,
       HAS_SORTLI,
       HAS_POST_INFO,
       ASSESSMENT_SUMMARY
FROM INDEPENDENT_LIVING
WHERE PLAN_ID = :plan-id

-- :name get-life-skills :? :1
SELECT ASSESSMENT_SUMMARY
FROM LIFE_SKILLS
WHERE PLAN_ID = :plan-id

-- :name get-finances :? :1
SELECT HAS_BANK_ACCOUNT,
       HAS_TAX_FILE_NUMBER,
       CENTRELINK,
       CENTRELINK_NUMBER,
       TRANS_IND_LIVING,
       PAYMENT_BROKERAGE,
       ROMA_MITCHELL,
       CREATE_FUTURE,
       WYATT_TRUST,
       ASSESSMENT_SUMMARY
FROM FINANCES
WHERE PLAN_ID = :plan-id

-- :name get-person-views :? :1
SELECT CHILD_INVOLVED,
       CHILD_YES_OTHER,
       CHILD_NO_OTHER,
       CHILD_NO_REASON,
       CHILD_SUMMARY,
       CHILD_VIEWS,
       PARENT_INVOLVED,
       PARENT_YES_OTHER,
       PARENT_NO_OTHER,
       PARENT_NO_REASON,
       PARENT_SUMMARY,
       PARENT_VIEWS,
       CARER_INVOLVED,
       CARER_YES_OTHER,
       CARER_NO_OTHER,
       CARER_NO_REASON,
       CARER_SUMMARY,
       CARER_VIEWS
FROM PERSON_VIEWS
WHERE PLAN_ID = :plan-id

-- :name get-person-involvement :? :*
SELECT ORD,
       PERSON_TYPE,
       INVOLVE_YES_NO,
       INVOLVE_TYPE
FROM PERSON_INVOLVEMENT
WHERE PLAN_ID = :plan-id

-- :name get-endorsement-approval :? :*
SELECT ORD,
       STATUS,
       ACTION,
       ACTION_DATETIME,
       TO_WORKER_ID,
       TO_WORKER_NAME,
       FROM_WORKER_ID,
       FROM_WORKER_NAME,
       RESULT,
       RESULT_WORKER_ID,
       RESULT_WORKER_NAME,
       RESULT_DATETIME,
       WORKER_COMMENT
FROM ENDORSEMENT_APPROVAL
WHERE PLAN_ID = :plan-id

-- :name get-actions :? :*
SELECT CATEGORY,
       ORD,
       ACTION,
       WHO,
       PLANNED_WHEN,
       OUTCOMES
FROM ACTIONS
WHERE PLAN_ID = :plan-id

-- :name get-all-plans :? :*
SELECT PLAN_ID,
       STATUS,
       APPROVED_DATETIME
FROM CASE_PLAN
WHERE CASE_ID = :case-id

-- :name get-plan-audit-history :? :*
SELECT AUDIT_ID,
       TIME_STAMP,
       USER_ID
FROM AUDIT_LOG
WHERE TARGET = 'caseplan'
AND TARGET_ID = :plan-id

-- :name get-audit-details :? :1
SELECT JSON
FROM AUDIT_LOG
WHERE AUDIT_ID = :audit-id




