(ns case-plan-server.backend.print-request)

(defn- print-document
  [{:keys [header] :as plan-or-review}]
  (if (:document-xml header)
    (let [id (or (:review-id header) (:plan-id header))
          document-type (or (when (:review-id header) "REVIEW") "CASEPLAN")]
      (-> plan-or-review
          (assoc :print-document
                 (merge {:id id
                         :document-type document-type}
                        (select-keys header [:case-id :client-id :document-xml])))
          (update :header dissoc :document-xml)))
    plan-or-review))

(defn- contact-det-document-fn
  [case-id]
  (fn [docs contact-det]
    (if (:document-xml contact-det)
      (conj docs
            (merge {:case-id case-id}
                   (select-keys contact-det [:client-id :approved-datetime :approved-by
                                             :contact-determination :issue-date :document-xml])))
      docs)))

(defn- contact-det-documents
  [{:keys [contact-determinations] :as plan-or-review} case-id]
  (let [documents (reduce (contact-det-document-fn case-id) [] contact-determinations)]
    (-> plan-or-review
        (assoc :contact-det-documents documents)
        (update :contact-determinations #(mapv (fn [cd] (dissoc cd :document-xml)) %)))))

(defn pull-up
  [{{:keys [case-id]} :header :as plan-or-review}]
  (-> plan-or-review
      (print-document)
      (contact-det-documents case-id)))