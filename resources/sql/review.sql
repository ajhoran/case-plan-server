-- :name get-review-report :? :1
SELECT REVIEW_ID,
       PLAN_ID,
       REVIEW_TYPE,
       CLIENT_ID,
       CASE_ID,
       STATUS,
       PLAN_GOAL,
       ACIST,
       APPROVED_DATETIME,
       APPROVED_BY,
       APPROVED_BY_NAME,
       LAST_MODIFIED_DATETIME,
       LAST_MODIFIED_BY,
       LAST_MODIFIED_BY_NAME,
       CREATED_DATETIME,
       CREATED_BY,
       CREATED_BY_NAME,
       REVIEW_DUE_DATE
FROM REVIEW_REPORT
WHERE REVIEW_ID = :review-id

-- :name get-rev-client :? :1
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
FROM REV_CLIENT
WHERE REVIEW_ID = :review-id

-- :name get-rev-details :? :1
SELECT PANEL_CHAIR,
       PANEL_CHAIR_ROLE,
       PANEL_CHAIR_ROLE_OTHER,
       CHARTER_RECEIVED,
       CHARTER_RECEIVED_DATE,
       CHILD_INVITED,
       CHILD_INV_NO_OTHER,
       CHILD_PARTICIPATED,
       CHILD_PART_NO_OTHER,
       CHILD_PART_YES_OTHER,
       CARER_INVITED,
       CARER_INV_NO_REASON,
       CARER_PARTICIPATED,
       CARER_PART_NO_OTHER,
       CARER_PART_YES_OTHER,
       PARENT_PARTICIPATED,
       PARENT_PART_NO_OTHER,
       PARENT_PART_YES_OTHER
FROM REV_DETAILS
WHERE REVIEW_ID = :review-id

-- :name get-rev-panel-members :? :*
SELECT ORD,
       NAME,
       PANEL_ROLE
FROM REV_PANEL_MEMBERS
WHERE REVIEW_ID = :review-id

-- :name get-rev-partic-answers :? :*
SELECT ANSWER_TYPE,
       ORD,
       ANSWER
FROM REV_PARTIC_ANSWERS
WHERE REVIEW_ID = :review-id

-- :name get-rev-contributors :? :*
SELECT ORD,
       CLIENT_ID,
       NAME,
       CONTRIBUTOR_ROLE
FROM REV_CONTRIBUTORS
WHERE REVIEW_ID = :review-id

-- :name get-rev-outcomes :? :*
SELECT ORD,
       CONCERN,
       OUTCOME,
       PROGRESS_SUMMARY
FROM REV_OUTCOMES
WHERE REVIEW_ID = :review-id

-- :name get-rev-outcomes-actions :? :*
SELECT OUTCOME_ORD,
       ORD,
       ACTION,
       WHO,
       PLANNED_WHEN,
       MEASURE,
       STATUS
FROM REV_OUTCOMES_ACTIONS
WHERE REVIEW_ID = :review-id

-- :name get-rev-atsi :? :1
SELECT PROGRESS_SUMMARY
FROM REV_ATSI
WHERE REVIEW_ID = :review-id

-- :name get-rev-atsi-reconnect :? :*
SELECT ORD,
       FREQUENCY,
       TRAVEL,
       SUPERVISOR,
       CONTACT,
       STATUS
FROM REV_ATSI_RECONNECT
WHERE REVIEW_ID = :review-id

-- :name get-rev-cald :? :1
SELECT PROGRESS_SUMMARY
FROM REV_CALD
WHERE REVIEW_ID = :review-id

-- :name get-rev-contact-arrangements :? :1
SELECT PROGRESS_SUMMARY
FROM REV_CONTACT_ARRANGEMENTS
WHERE REVIEW_ID = :review-id

-- :name get-rev-rep-physical-health :? :1
SELECT PROGRESS_SUMMARY
FROM REV_REP_PHYSICAL_HEALTH
WHERE REVIEW_ID = :review-id

-- :name get-rev-rep-disability-devdelay :? :1
SELECT PROGRESS_SUMMARY
FROM REV_REP_DISABILITY_DEVDELAY
WHERE REVIEW_ID = :review-id

-- :name get-rev-rep-emotional :? :1
SELECT PROGRESS_SUMMARY
FROM REV_REP_EMOTIONAL
WHERE REVIEW_ID = :review-id

-- :name get-rev-rep-education :? :1
SELECT PROGRESS_SUMMARY
FROM REV_REP_EDUCATION
WHERE REVIEW_ID = :review-id

-- :name get-rev-rep-recreation :? :1
SELECT PROGRESS_SUMMARY
FROM REV_REP_RECREATION
WHERE REVIEW_ID = :review-id

-- :name get-rev-rep-independent-living :? :1
SELECT PROGRESS_SUMMARY
FROM REV_REP_INDEPENDENT_LIVING
WHERE REVIEW_ID = :review-id

-- :name get-rev-rep-finances :? :1
SELECT PROGRESS_SUMMARY
FROM REV_REP_FINANCES
WHERE REVIEW_ID = :review-id

-- :name get-rev-rep-conclusions :? :1
SELECT BEST_INTEREST,
       BEST_INTEREST_NO,
       SUPPORT_NEEDS,
       SUPPORT_NEEDS_NO,
       PANEL_BEST_INTEREST,
       PANEL_BEST_INTEREST_NO,
       REVIEW_OUTCOME
FROM REV_REP_CONCLUSIONS
WHERE REVIEW_ID = :review-id

-- :name get-rev-endorsement-approval :? :*
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
FROM REV_ENDORSEMENT_APPROVAL
WHERE REVIEW_ID = :review-id

-- :name get-rev-actions-case-plan :? :*
SELECT CATEGORY,
       ORD,
       ACTION,
       WHO,
       PLANNED_WHEN,
       OUTCOMES,
       STATUS
FROM REV_ACTIONS_CASE_PLAN
WHERE REVIEW_ID = :review-id

-- :name get-rev-actions-panel :? :*
SELECT CATEGORY,
       ORD,
       ACTION,
       WHO,
       PLANNED_WHEN,
       OUTCOMES
FROM REV_ACTIONS_PANEL
WHERE REVIEW_ID = :review-id
