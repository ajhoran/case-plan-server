(ns case-plan-server.backend.caseplan
  (:require
    [case-plan-server.backend.auto-update :as auto]
    [case-plan-server.backend.core :refer [->kebab-case]]
    [case-plan-server.backend.print-request :as print-request]
    [case-plan-server.backend.workflow-request :as workflow-request]
    [case-plan-server.db.core :as db]
    [jsonista.core :as json]))

(defn assoc-c3-current-details
  [plan-or-review client-id user-id]
  (-> plan-or-review
      (assoc :c3client (db/get-c3client {:client-id client-id})
             :c3relationsRoles (db/get-c3relsroles {:client-id client-id})
             :c3relations (db/get-c3relations {:client-id client-id})
             :workerDetails (db/get-c3worker {:worker-id user-id}))))

(defn retrieve-c3-workers
  []
  (db/get-c3workers))

(defn retrieve-offices
  []
  (db/get-offices))

(defn create
  [client-id case-id user-id]
  (let [next-plan-id (:nextval (db/get-next-plan-id))]
    (-> {:header {:plan-id next-plan-id
                  :client-id client-id
                  :case-id case-id
                  :status "NEW"}}
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