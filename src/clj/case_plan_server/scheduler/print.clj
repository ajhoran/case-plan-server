(ns case-plan-server.scheduler.print
  (:require
    [case-plan-server.db.core :as db]
    [case-plan-server.config :refer [env]]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [clj-http.client :as http]))

(def timeout 120000)
(def user-agent "CasePlan Printer")
(def plan-types {"CASEPLAN"  "ECPL"
                 "REVIEW"    "EREV"
                 "CONTACT"   "ECDT"
                 "NOCONTACT" "ECDT"})

(defn- check-waiting-prints
  []
  (doseq [{:keys [case_id id document_type]} (db/get-print-document-waiting-print)]
    (log/info "check print document" (long id) document_type)
    (if (= 1 (:exist (db/get-plan-attach {:case-id case_id :id id :plan-type (get plan-types document_type)})))
      (db/update-print-document-printed {:id id :doc-type document_type})
      (db/update-print-document-retry {:id id :doc-type document_type})))
  (doseq [{:keys [case_id contact_det_id contact_determination]} (db/get-contdet-document-waiting-print)]
    (log/info "check contact det document" (long contact_det_id))
    (if (= 1 (:exist (db/get-plan-attach {:case-id case_id :id contact_det_id :plan-type (get plan-types contact_determination)})))
      (db/update-contdet-document-printed {:id contact_det_id})
      (db/update-contdet-document-retry {:id contact_det_id}))))

(defn print-check-job
  []
  (check-waiting-prints))

(defn- request-print
  [type case-id client-id id]
  (try
    (let [url (str (env :c3integration-url) "&area=DOCUMENT" "&type=" type "&caseid=" case-id "&clientid=" client-id "&id=" id)
          headers {"User-Agent" user-agent}
          response (http/get url
                             {:headers headers
                              :socket-timeout timeout
                              :connection-timeout timeout
                              :throw-exceptions false})
          http-ok (= 200 (:status response))
          c3-ok (str/includes? (:body response) "<StatusCode>0</StatusCode>")]
      (log/debug url)
      (log/debug (select-keys response [:status :reason-phrase :request-time :headers :body]))
      {:http-ok http-ok :c3-ok c3-ok})
    (catch Exception _
      {:http-ok false})))

(defn- request-prints
  []
  (doseq [{:keys [document_type case_id client_id id]} (db/get-print-document-to-request {:retry-max (env :c3integration-retry)})]
    (log/info "request print document" (long id) document_type)
    (let [{:keys [http-ok c3-ok]} (request-print document_type case_id client_id id)]
      (if (and http-ok c3-ok)
        (db/update-print-document-requested {:id id :doc-type document_type})
        (db/update-print-document-retry {:id id :doc-type document_type}))))
  (doseq [{:keys [contact_determination case_id client_id contact_det_id]} (db/get-contdet-document-to-request {:retry-max (env :c3integration-retry)})]
    (log/info "request contact det document" (long contact_det_id))
    (let [{:keys [http-ok c3-ok]} (request-print contact_determination case_id client_id contact_det_id)]
      (if (and http-ok c3-ok)
        (db/update-contdet-document-requested {:id contact_det_id})
        (db/update-contdet-document-retry {:id contact_det_id})))))


(defn print-request-job
  []
  (request-prints))