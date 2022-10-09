(ns case-plan-server.backend.caseplan
  (:require
    [case-plan-server.backend.auto-update :as auto]
    [case-plan-server.backend.core :refer [->kebab-case]]
    [case-plan-server.backend.print-request :as print-request]
    [case-plan-server.backend.workflow-request :as workflow-request]
    [case-plan-server.db.core :as db]
    [jsonista.core :as json]))

(def incomplete-action-statuses #{"PROG" "NTCM"})

(defn assoc-c3-current-details
  [plan-or-review client-id user-id]
  (-> plan-or-review
      (assoc :c3client (db/get-c3client {:client-id client-id})
             :c3relationsRoles (db/get-c3relsroles {:client-id client-id})
             :c3relations (db/get-c3relations {:client-id client-id})
             :workerDetails (db/get-c3worker {:worker-id user-id}))))

(defn retrieve-all-plans
  [case-id]
  (db/retrieve-all-plans case-id))

(defn retrieve-all-plans-and-linked-review
  [case-id plan-id]
  (let [all-plans (retrieve-all-plans case-id)
        has-linked-review (db/has-plan-been-reviewed {:plan-id plan-id})]
    {:all-plans all-plans
     :plan-has-review (:reviewed has-linked-review)}))

(defn retrieve-audit-history
  [plan-id]
  (db/retrieve-audit-history plan-id))

(defn retrieve-audit-details
  [audit-id]
  (db/retrieve-audit-details audit-id))

(defn retrieve-c3-workers
  []
  (db/get-c3workers))

(defn retrieve-offices
  []
  (db/get-offices))

(defn- get-previous-plan
  [client-id case-id]
  (let [{:keys [header placement culture-identity atsi cald contact-arrangements physical-health disability emotional education recreation independent-living life-skills finances] :as plan}
        (db/retrieve-plan-previous-plan client-id case-id)]
    (-> plan
        (select-keys [:plan-id :general :about-me :care-team :professionals :atsi-orgs :cald-orgs :contact-determinations :disabilities])
        (assoc :plan-goal (:plan-goal header)
               :placement placement
               :culture-identity culture-identity
               :atsi atsi
               :cald cald
               :contact-arrangements contact-arrangements
               :physical-health physical-health
               :disability disability
               :emotional emotional
               :education education
               :recreation recreation
               :independent-living independent-living
               :life-skills life-skills
               :finances finances))))

(defn- get-related-review
  [plan-id]
  (let [{:keys [outcomes outcomes-actions outcomes-actions-create actions-panel actions-case-plan] :as review}
        (db/retrieve-plan-related-review plan-id)
        incomplete-outcomes-actions (->> outcomes-actions
                                         (filter #(contains? incomplete-action-statuses (:status %)))
                                         (map #(dissoc % :status)))
        incomplete-outcomes-ords (->> incomplete-outcomes-actions
                                      (map :outcome-ord)
                                      (set))
        incomplete-outcomes (->> outcomes
                                 (filter #(contains? incomplete-outcomes-ords (:ord %)))
                                 (map #(dissoc % :progress-summary)))
        incomplete-actions-case-plan (->> actions-case-plan
                                          (filter #(contains? incomplete-action-statuses (:status %)))
                                          (map #(dissoc % :status)))]
    (-> review
        (select-keys [:review-id :client])
        (assoc :outcomes incomplete-outcomes
               :outcomes-actions (concat outcomes-actions-create incomplete-outcomes-actions)
               :actions (concat actions-panel incomplete-actions-case-plan)))))

(defn create
  [client-id case-id user-id]
  (let [next-plan-id (:nextval (db/get-next-id))
        previous-plan (get-previous-plan client-id case-id)
        related-review (get-related-review (:plan-id previous-plan))
        subsequent-case-plan (if (:plan-id previous-plan) "Y" "N")]
    (-> {:header {:plan-id next-plan-id
                  :client-id client-id
                  :case-id case-id
                  :status "NEW"
                  :plan-goal (:plan-goal previous-plan)
                  :subsequent-case-plan subsequent-case-plan}}
        (merge (when (:plan-id previous-plan)
                 (dissoc previous-plan :plan-id :plan-goal))
               (when (:review-id related-review)
                 (dissoc related-review :review-id)))
        (assoc-c3-current-details client-id user-id))))

(defn retrieve
  [plan-id client-id user-id]
  (-> plan-id
      db/retrieve-plan
      (assoc-c3-current-details client-id user-id)))

(defn retrieve-contact-determination
  [plan-id]
  (-> plan-id
      db/retrieve-contact-determination))

(defn save
  [plan-json user-id]
  (let [plan (-> plan-json (json/read-value (json/object-mapper {:decode-key-fn true})) ->kebab-case)
        audit {:user-id user-id :target "caseplan" :target-id (get-in plan [:header :plan-id]) :save-type "caseplan" :json plan-json}
        user-name (:worker_name (db/get-c3worker {:worker-id user-id}))]
    (-> plan
        (auto/update-fields user-id user-name)
        print-request/pull-up
        workflow-request/pull-up
        (db/save-plan audit))))

(defn save-contact-determination
  [plan-json user-id]
  (let [plan (-> plan-json (json/read-value (json/object-mapper {:decode-key-fn true})) ->kebab-case)
        audit {:user-id user-id :target "caseplan" :target-id (get-in plan [:header :plan-id]) :save-type "contact_det" :json plan-json}
        user-name (:worker_name (db/get-c3worker {:worker-id user-id}))]
    (-> plan
        (auto/update-fields user-id user-name)
        print-request/pull-up
        workflow-request/pull-up
        (db/save-contact-determination audit))))