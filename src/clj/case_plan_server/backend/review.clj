(ns case-plan-server.backend.review
  (:require
    [case-plan-server.backend.auto-update :as auto]
    [case-plan-server.backend.caseplan :refer [assoc-c3-current-details]]
    [case-plan-server.backend.core :refer [->kebab-case]]
    [case-plan-server.db.core :as db]
    [jsonista.core :as json]))

(defn- get-related-plan
  [client-id case-id]
  (let [{:keys [plan-id client care-team professionals outcomes outcomes-actions atsi-reconnect actions]}
        (db/retrieve-review-related-plan client-id case-id)]
    (-> {:plan-id plan-id
         :client client}
        (assoc :contributors (concat
                               (map (fn [{:keys [client-id display-name relationship]}]
                                      {:ord 0
                                       :client-id client-id
                                       :name display-name
                                       :contributor-role relationship}) care-team)
                               (map (fn [{:keys [ord display-name person-role]}]
                                      {:ord ord
                                       :client-id nil
                                       :name display-name
                                       :contributor-role person-role}) professionals))
               :outcomes (map #(assoc % :progress-summary "") outcomes)
               :outcomes-actions (map #(assoc % :status "") outcomes-actions)
               :atsi-reconnect (map #(assoc % :status "") atsi-reconnect)
               :actions-case-plan (map #(assoc % :status "") actions)))))

(defn create
  [client-id case-id user-id]
  (let [next-review-id (:nextval (db/get-next-review-id))
        related-plan (get-related-plan client-id case-id)]
    (-> {:header {:review-id next-review-id
                  :client-id client-id
                  :case-id case-id
                  :status "NEW"
                  :plan-id (:plan-id related-plan)}}
        (merge (dissoc related-plan :plan-id))
        (assoc-c3-current-details client-id user-id))))

(defn retrieve
  [review-id client-id user-id]
  (-> review-id
      db/retrieve-review
      (assoc-c3-current-details client-id user-id)))

(defn save
  [review-json user-id]
  (let [review (-> review-json (json/read-value (json/object-mapper {:decode-key-fn true})) ->kebab-case)
        audit {:user-id user-id :target "review" :target-id (get-in review [:header :review-id]) :save-type "review" :json review-json}
        user-name (:worker_name (db/get-c3worker {:worker-id user-id}))]
    (-> review
        (auto/update-fields user-id user-name)
        (db/save-review audit))))