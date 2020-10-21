(ns case-plan-server.scheduler.workflow
  (:require
    [case-plan-server.db.core :as db]
    [case-plan-server.config :refer [env]]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [clj-http.client :as http]))

(def user-agent "CasePlan Workflow")

(defn- request-workflow
  [type action to from client-id relative-id case-id]
  (try
    (let [url (str (get env :c3integration-url) "&area=WORKFLOW" "&type=" type "&action=" action "&fromuser=" from "&caseid=" case-id
                   (when to (str "&touser=" to))
                   (when client-id (str "&clientid=" client-id))
                   (when relative-id (str "&relativeid=" relative-id)))
          headers {"User-Agent" user-agent}
          response (http/get url
                             {:headers headers
                              :socket-timeout (get env :c3integration-timeout 30000)
                              :connection-timeout (get env :c3integration-timeout 30000)
                              :throw-exceptions false})
          http-ok (= 200 (:status response))
          c3-ok (str/includes? (:body response) "<StatusCode>0</StatusCode>")]
      (log/debug url)
      (log/debug (select-keys response [:status :reason-phrase :request-time :headers :body]))
      {:http-ok http-ok :c3-ok c3-ok})
    (catch Exception e
      (log/error e)
      {:http-ok false})))

(defn- request-workflows
  []
  (log/debug "check workflows to request")
  (doseq [{:keys [type action to from client_id relative_id case_id workflow_id]} (db/get-workflows-to-request
                                                                                    {:retry-max (get env :c3integration-retry 3)})]
    (log/info "request workflow" (long workflow_id))
    (let [{:keys [http-ok c3-ok]} (request-workflow type action to from client_id relative_id case_id)]
      (if (and http-ok c3-ok)
        (db/update-workflows-requested {:id workflow_id})
        (db/update-workflows-retry {:id workflow_id})))))

(defn workflow-job
  []
  (request-workflows))