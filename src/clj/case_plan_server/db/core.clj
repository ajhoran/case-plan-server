(ns case-plan-server.db.core
  (:require
    [case-plan-server.backend.core :refer [->kebab-case ->snake_case]]
    [case-plan-server.config :refer [env]]
    [case-plan-server.db.dates :refer [date->string string->date]]
    [conman.core :as conman]
    [hugsql.core :as hugsql]
    [mount.core :refer [defstate]]
    [next.jdbc]
    [next.jdbc.date-time]
    [next.jdbc.result-set]
    [next.jdbc.sql])
  (:import
    [java.sql ResultSet ResultSetMetaData]
    [oracle.sql CLOB]))

(defstate ^:dynamic *db*
          :start (conman/connect! {:jdbc-url (env :database-url)})
          :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql" "sql/printing.sql" "sql/workflow.sql")
(hugsql/def-sqlvec-fns "sql/caseplan.sql")
(hugsql/def-sqlvec-fns "sql/review.sql")

(extend-protocol next.jdbc.result-set/ReadableColumn
  java.sql.Timestamp
  (read-column-by-label [^java.sql.Timestamp v _]
    (.toLocalDateTime v))
  (read-column-by-index [^java.sql.Timestamp v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (read-column-by-label [^java.sql.Date v _]
    (.toLocalDate v))
  (read-column-by-index [^java.sql.Date v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (read-column-by-label [^java.sql.Time v _]
    (.toLocalTime v))
  (read-column-by-index [^java.sql.Time v _2 _3]
    (.toLocalTime v)))

(def case-plan-table-key
  [[:case_plan :header]
   [:case_plan_client :client]
   [:about_me :about-me]
   [:case_plan_general :general]
   [:family_group_conf :fgc]
   [:outcomes :outcomes]
   [:outcomes_actions :outcomes-actions]
   [:care_team_family :care-team]
   [:care_team_professionals :professionals]
   [:placement :placement]
   [:culture_identity :culture-identity]
   [:atsi :atsi]
   [:atsi_orgs :atsi-orgs]
   [:atsi_events :atsi-events]
   [:atsi_reconnect :atsi-reconnect]
   [:atsi_carer_training :atsi-carer-training]
   [:atsi_consultation :atsi-consultation]
   [:cald :cald]
   [:cald_orgs :cald-orgs]
   [:cald_events :cald-events]
   [:cald_carer_training :cald-carer-training]
   [:cald_consultation :cald-consultation]
   [:contact_arrangements :contact-arrangements]
   [:contact_determination :contact-determinations]
   [:endorsement_approval_contact :endorsement-approval-contact]
   [:physical_health :physical-health]
   [:disability_devdelay :disability]
   [:disabilities :disabilities]
   [:emotional :emotional]
   [:education :education]
   [:recreation :recreation]
   [:independent_living :independent-living]
   [:finances :finances]
   [:person_views :person-views]
   [:person_involvement :person-involvement]
   [:endorsement_approval :endorsement-approval]
   [:actions :actions]])
(def contact-determination-table-key
  [[:case_plan :header]
   [:contact_determination :contact-determinations]
   [:endorsement_approval_contact :endorsement-approval-contact]])
(def review-table-key
  [[:review_report :header]
   [:rev_client :client]
   [:rev_details :review]
   [:rev_panel_members :panel-members]
   [:rev_partic_answers :participation-answers]
   [:rev_contributors :contributors]
   [:rev_outcomes :outcomes]
   [:rev_outcomes_actions :outcomes-actions]
   [:rev_atsi :acist]
   [:rev_atsi_reconnect :atsi-reconnect]
   [:rev_cald :cald]
   [:rev_contact_arrangements :contact-arrangements]
   [:rev_rep_physical_health :physical-health]
   [:rev_rep_disability_devdelay :disability]
   [:rev_rep_emotional :emotional]
   [:rev_rep_education :education]
   [:rev_rep_recreation :recreation]
   [:rev_rep_independent_living :independent-living]
   [:rev_rep_finances :finances]
   [:rev_rep_conclusions :conclusions]
   [:rev_endorsement_approval :endorsement-approval]
   [:rev_actions_case_plan :actions-case-plan]
   [:rev_actions_panel :actions-panel]])

(defn- clob->string
  "Given a CLOB column value, read it as a string."
  [^CLOB clob]
  (with-open [rdr (.getCharacterStream clob)]
    (slurp rdr)))

(defn- clob-column-reader
  "A column-reader that reads column values by `.getObject`
   but expands CLOB columns into strings."
  [^ResultSet rs ^ResultSetMetaData _ ^Integer i]
  (when-let [value (.getObject rs i)]
    (cond-> value
            (instance? CLOB value)
            (clob->string))))

(def resultset-builder
  {:builder-fn (next.jdbc.result-set/as-maps-adapter
                 next.jdbc.result-set/as-lower-maps
                 clob-column-reader)})

(defn- select
  [db id table sqlvec]
  (-> (next.jdbc/execute! db (sqlvec id) resultset-builder)
      (date->string table)
      ->kebab-case))

(defn- select-one
  [db id table sqlvec]
  (or (-> (next.jdbc/execute-one! db (sqlvec id) resultset-builder)
          (date->string table)
          ->kebab-case)
      {}))

(defn- insert-record
  [tx id-map table record]
  (if (vector? record)
    (doseq [r record]
      (insert-record tx id-map table r))
    (when-not (empty? record)
      (next.jdbc.sql/insert! tx table (-> record
                                          (merge id-map)
                                          ->snake_case
                                          (string->date table))))))

(defn- insert
  [tx table-key id-map plan-or-review]
  (doseq [[table key] table-key]
    (insert-record tx id-map table (get plan-or-review key))))

(defn- delete
  [tx table-key id-map]
  (doseq [[table _] table-key]
    (next.jdbc.sql/delete! tx table (->snake_case id-map))))

(defn retrieve-plan
  [plan-id]
  (conman/with-transaction
    [*db* {:isolation :read-committed}]
    (let [s (partial select *db* {:plan-id plan-id})
          s1 (partial select-one *db* {:plan-id plan-id})]
      (assoc {}
        :header (s1 :case_plan get-case-plan-sqlvec)
        :client (s1 :case_plan_client get-case-plan-client-sqlvec)
        :about-me (s1 :about_me get-about-me-sqlvec)
        :general (s1 :case_plan_general get-case-plan-general-sqlvec)
        :fgc (s1 :family_group_conf get-family-group-conf-sqlvec)
        :outcomes (s :outcomes get-outcomes-sqlvec)
        :outcomes-actions (s :outcomes_actions get-outcomes-actions-sqlvec)
        :care-team (s :care_team_family get-care-team-family-sqlvec)
        :professionals (s :care_team_professionals get-care-team-professionals-sqlvec)
        :placement (s1 :placement get-placement-sqlvec)
        :culture-identity (s1 :culture_identity get-culture-identity-sqlvec)
        :atsi (s1 :atsi get-atsi-sqlvec)
        :atsi-orgs (s :atsi_orgs get-atsi-orgs-sqlvec)
        :atsi-events (s :atsi_events get-atsi-events-sqlvec)
        :atsi-reconnect (s :atsi_reconnect get-atsi-reconnect-sqlvec)
        :atsi-carer-training (s :atsi_carer_training get-atsi-carer-training-sqlvec)
        :atsi-consultation (s :atsi_consultation get-atsi-consultation-sqlvec)
        :cald (s1 :cald get-cald-sqlvec)
        :cald-orgs (s :cald_orgs get-cald-orgs-sqlvec)
        :cald-events (s :cald_events get-cald-events-sqlvec)
        :cald-carer-training (s :cald_carer_training get-cald-carer-training-sqlvec)
        :cald-consultation (s :cald_consultation get-cald-consultation-sqlvec)
        :contact-arrangements (s1 :contact_arrangements get-contact-arrangements-sqlvec)
        :contact-determinations (s :contact_determination get-contact-determination-sqlvec)
        :endorsement-approval-contact (s :endorsement_approval_contact get-endorsement-approval-contact-sqlvec)
        :physical-health (s1 :physical_health get-physical-health-sqlvec)
        :disability (s1 :disability_devdelay get-disability-devdelay-sqlvec)
        :disabilities (s :disabilities get-disabilities-sqlvec)
        :emotional (s1 :emotional get-emotional-sqlvec)
        :education (s1 :education get-education-sqlvec)
        :recreation (s1 :recreation get-recreation-sqlvec)
        :independent-living (s1 :independent_living get-independent-living-sqlvec)
        :finances (s1 :finances get-finances-sqlvec)
        :person-views (s1 :person_views get-person-views-sqlvec)
        :person-involvement (s :person_involvement get-person-involvement-sqlvec)
        :endorsement-approval (s :endorsement_approval get-endorsement-approval-sqlvec)
        :actions (s :actions get-actions-sqlvec)))))

(defn retrieve-all-plans
  [case-id]
  (->> (select *db* {:case-id case-id} :case_plan get-all-plans-sqlvec)
        (map #(select-keys % [:plan-id :status :approved-datetime]))))

(defn retrieve-plan-previous-plan
  [client-id case-id]
  (conman/with-transaction
    [*db* {:isolation :read-committed}]
    (let [previous-plan-id (:plan_id (get-recent-approved-plan-id {:client-id client-id :case-id case-id}))
          s (partial select *db* {:plan-id previous-plan-id})
          s1 (partial select-one *db* {:plan-id previous-plan-id})]
      (assoc {:plan-id previous-plan-id}
        :header (s1 :case_plan get-case-plan-sqlvec)
        :about-me (s1 :about_me get-about-me-sqlvec)
        :general (s1 :case_plan_general get-case-plan-general-sqlvec)
        :care-team (s :care_team_family get-care-team-family-sqlvec)
        :professionals (s :care_team_professionals get-care-team-professionals-sqlvec)
        :culture-identity (s1 :culture_identity get-culture-identity-sqlvec)
        :atsi (s1 :atsi get-atsi-sqlvec)
        :atsi-orgs (s :atsi_orgs get-atsi-orgs-sqlvec)
        :cald (s1 :cald get-cald-sqlvec)
        :cald-orgs (s :cald_orgs get-cald-orgs-sqlvec)
        :contact-determinations (s :contact_determination get-contact-determination-sqlvec)
        :physical-health (s1 :physical_health get-physical-health-sqlvec)
        :disability (s1 :disability_devdelay get-disability-devdelay-sqlvec)
        :disabilities (s :disabilities get-disabilities-sqlvec)
        :emotional (s1 :emotional get-emotional-sqlvec)
        :education (s1 :education get-education-sqlvec)
        :independent-living (s1 :independent_living get-independent-living-sqlvec)
        :finances (s1 :finances get-finances-sqlvec)))))

(defn retrieve-plan-related-review
  [plan-id]
  (conman/with-transaction
    [*db* {:isolation :read-committed}]
    (let [related-review-id (:review_id (get-plan-approved-review-id {:plan-id plan-id}))
          s (partial select *db* {:review-id related-review-id})
          s1 (partial select-one *db* {:review-id related-review-id})]
      (assoc {:review-id related-review-id}
        :client (s1 :rev_client get-rev-client-sqlvec)
        :outcomes (s :rev_outcomes get-rev-outcomes-sqlvec)
        :outcomes-actions (s :rev_outcomes_actions get-rev-outcomes-actions-sqlvec)
        :actions-panel (s :rev_actions_panel get-rev-actions-panel-sqlvec)
        :actions-case-plan (s :rev_actions_case_plan get-rev-actions-case-plan-sqlvec)))))

(defn retrieve-contact-determination
  [plan-id]
  (conman/with-transaction
    [*db* {:isolation :read-committed}]
    (let [s (partial select *db* {:plan-id plan-id})
          s1 (partial select-one *db* {:plan-id plan-id})]
      (assoc {}
        :header (s1 :case_plan get-case-plan-sqlvec)
        :contact-determinations (s :contact_determination get-contact-determination-sqlvec)
        :endorsement-approval-contact (s :endorsement_approval_contact get-endorsement-approval-contact-sqlvec)))))

(defn retrieve-review
  [review-id]
  (conman/with-transaction
    [*db* {:isolation :read-committed}]
    (let [s (partial select *db* {:review-id review-id})
          s1 (partial select-one *db* {:review-id review-id})]
      (assoc {}
        :header (s1 :review_report get-review-report-sqlvec)
        :client (s1 :rev_client get-rev-client-sqlvec)
        :review (s1 :rev_details get-rev-details-sqlvec)
        :panel-members (s :rev_panel_members get-rev-panel-members-sqlvec)
        :participation-answers (s :rev_partic_answers get-rev-partic-answers-sqlvec)
        :contributors (s :rev_contributors get-rev-contributors-sqlvec)
        :outcomes (s :rev_outcomes get-rev-outcomes-sqlvec)
        :outcomes-actions (s :rev_outcomes_actions get-rev-outcomes-actions-sqlvec)
        :acist (s1 :rev_atsi get-rev-atsi-sqlvec)
        :atsi-reconnect (s :rev_atsi_reconnect get-rev-atsi-reconnect-sqlvec)
        :cald (s1 :rev_cald get-rev-cald-sqlvec)
        :contact-arrangements (s1 :rev_contact_arrangements get-rev-contact-arrangements-sqlvec)
        :physical-health (s1 :rev_rep_physical_health get-rev-rep-physical-health-sqlvec)
        :disability (s1 :rev_rep_disability_devdelay get-rev-rep-disability-devdelay-sqlvec)
        :emotional (s1 :rev_rep_emotional get-rev-rep-emotional-sqlvec)
        :education (s1 :rev_rep_education get-rev-rep-education-sqlvec)
        :recreation (s1 :rev_rep_recreation get-rev-rep-recreation-sqlvec)
        :independent-living (s1 :rev_rep_independent_living get-rev-rep-independent-living-sqlvec)
        :finances (s1 :rev_rep_finances get-rev-rep-finances-sqlvec)
        :conclusions (s1 :rev_rep_conclusions get-rev-rep-conclusions-sqlvec)
        :endorsement-approval (s :rev_endorsement_approval get-rev-endorsement-approval-sqlvec)
        :actions-case-plan (s :rev_actions_case_plan get-rev-actions-case-plan-sqlvec)
        :actions-panel (s :rev_actions_panel get-rev-actions-panel-sqlvec)))))

(defn retrieve-review-related-plan
  [client-id case-id]
  (conman/with-transaction
    [*db* {:isolation :read-committed}]
    (let [related-plan-id (:plan_id (get-recent-approved-plan-id {:client-id client-id :case-id case-id}))
          s (partial select *db* {:plan-id related-plan-id})
          s1 (partial select-one *db* {:plan-id related-plan-id})]
      (assoc {:plan-id related-plan-id}
        :header (s1 :case_plan get-case-plan-sqlvec)
        :client (s1 :case_plan_client get-case-plan-client-sqlvec)
        :care-team (s :care_team_family get-care-team-family-sqlvec)
        :professionals (s :care_team_professionals get-care-team-professionals-sqlvec)
        :outcomes (s :outcomes get-outcomes-sqlvec)
        :outcomes-actions (s :outcomes_actions get-outcomes-actions-sqlvec)
        :atsi-reconnect (s :atsi_reconnect get-atsi-reconnect-sqlvec)
        :actions (s :actions get-actions-sqlvec)))))

(defn save-plan
  [{{:keys [plan-id]} :header :as plan} audit]
  (conman/with-transaction
    [*db*]
    (delete *db* case-plan-table-key {:plan-id plan-id})
    (insert *db* case-plan-table-key {:plan-id plan-id} plan)
    (when (:print-document plan)
      (delete-print-document (:print-document plan))
      (insert-print-document (:print-document plan)))
    (doseq [doc (not-empty (:contact-det-documents plan))]
      (insert-contdet-document doc))
    (doseq [wf (not-empty (:workflows plan))]
      (insert-workflows wf))
    (insert-audit-log audit)))

(defn save-contact-determination
  [{{:keys [plan-id]} :header :as plan} audit]
  (conman/with-transaction
    [*db*]
    (delete *db* contact-determination-table-key {:plan-id plan-id})
    (insert *db* contact-determination-table-key {:plan-id plan-id} plan)
    (when (:print-document plan)
      (delete-print-document (:print-document plan))
      (insert-print-document (:print-document plan)))
    (doseq [doc (not-empty (:contact-det-documents plan))]
      (insert-contdet-document doc))
    (doseq [wf (not-empty (:workflows plan))]
      (insert-workflows wf))
    (insert-audit-log audit)))

(defn save-review
  [{{:keys [review-id]} :header :as review} audit]
  (conman/with-transaction
    [*db*]
    (delete *db* review-table-key {:review-id review-id})
    (insert *db* review-table-key {:review-id review-id} review)
    (when (:print-document review)
      (delete-print-document (:print-document review))
      (insert-print-document (:print-document review)))
    (doseq [wf (not-empty (:workflows review))]
      (insert-workflows wf))
    (insert-audit-log audit)))

(defn delete-c3-auth-record
  [where-clause]
  (-> (next.jdbc.sql/delete! *db* :c3ms_security_vw where-clause)
      :next.jdbc/update-count))