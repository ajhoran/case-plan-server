(ns case-plan-server.backend.workflow-request)

(defn- workflow-plan-review-fn
  [case-id client-id workflow-type]
  (fn [wfs approval]
    (if (:trigger-workflow approval)
      (conj wfs
            (merge {:case-id case-id
                    :client-id client-id
                    :relative-id nil
                    :workflow-type workflow-type
                    :action (:trigger-workflow approval)}
                   (select-keys approval [:to-worker-id :from-worker-id :result-worker-id])))
      wfs)))

(defn- workflow-plan-review
  [{:keys [endorsement-approval] :as plan-or-review} case-id client-id workflow-type]
  (let [workflows (reduce (workflow-plan-review-fn case-id client-id workflow-type)
                          [] endorsement-approval)]
    (-> plan-or-review
        (update :workflows concat workflows)
        (update :endorsement-approval #(mapv (fn [ea] (dissoc ea :trigger-workflow)) %)))))

(defn- workflow-contact-fn
  [case-id client-id cont-dets]
  (fn [wfs {:keys [trigger-workflow] :as approval}]
    (if trigger-workflow
      (->> cont-dets
           (filter #(= trigger-workflow (:generate-status %)))
           (map :client-id)
           (map #(merge {:case-id case-id
                         :client-id client-id
                         :relative-id %
                         :workflow-type "CONTACT"
                         :action trigger-workflow}
                        (select-keys approval [:to-worker-id :from-worker-id :result-worker-id])))
           (concat wfs))
      wfs)))

(defn- workflow-contact
  [{:keys [endorsement-approval-contact contact-determinations] :as plan-or-review} case-id client-id]
  (let [workflows (reduce (workflow-contact-fn case-id client-id contact-determinations)
                          [] endorsement-approval-contact)]
    (-> plan-or-review
        (update :workflows concat workflows)
        (update :endorsement-approval-contact #(mapv (fn [ea] (dissoc ea :trigger-workflow)) %)))))

(defn pull-up
  [{{:keys [case-id client-id review-type]} :header :as plan-or-review}]
  (let [workflow-type (or review-type "CASEPLAN")]
    (-> plan-or-review
        (workflow-plan-review case-id client-id workflow-type)
        (workflow-contact case-id client-id))))