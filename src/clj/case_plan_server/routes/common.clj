(ns case-plan-server.routes.common
  (:require
    [case-plan-server.backend.auth :refer [c3-authenticated?]]
    [case-plan-server.backend.caseplan :as caseplan]
    [case-plan-server.backend.core :refer [uri->app ->camelCase]]
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [ring.util.http-response :refer :all]))

(defn serve-unauth
  [request]
  (let [app (uri->app (:uri request))]
    {:status  401
     :headers {"Content-Type" "text/html"}
     :body    (-> (str "public/" app "/unauthorised.html")
                  (io/file)
                  (io/input-stream))}))

(defn serve-spa-html
  [{{{:keys [userid caseid clientid id viewOnly token]} :query} :parameters :as request}]
  (let [app (uri->app (:uri request))]
    (log/info "get" app "html" userid caseid clientid id viewOnly token)
    (if-not (c3-authenticated? userid caseid clientid app id viewOnly token)
      (do
        (log/info "unauthorised!" userid caseid clientid id viewOnly token)
        (serve-unauth request))
      {:status  200
       :headers {"Content-Type" "text/html"}
       :body    (-> (str "public/" app "/" app ".html")
                    (io/file)
                    (io/input-stream))})))

(defn get-workers
  [_]
  (-> (caseplan/retrieve-c3-workers)
      ->camelCase
      ok))

(defn get-offices
  [_]
  (-> (caseplan/retrieve-offices)
      ->camelCase
      ok))

(defn get-audit-details
  [{{{:keys [auditid]} :query} :parameters}]
  (log/info "get audit details" auditid)
  (-> (caseplan/retrieve-audit-details auditid)
      ->camelCase
      ok))