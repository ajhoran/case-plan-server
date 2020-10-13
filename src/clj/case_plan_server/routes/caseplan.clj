(ns case-plan-server.routes.caseplan
  (:require
    [case-plan-server.backend.auth :as auth]
    [case-plan-server.backend.caseplan :as caseplan]
    [case-plan-server.backend.core :refer [->camelCase]]
    [case-plan-server.middleware.formats :as formats]
    [case-plan-server.middleware.exception :as exception]
    [case-plan-server.routes.common :refer :all]
    [clojure.tools.logging :as log]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [ring.util.http-response :refer :all]))

(defn create-plan
  [{{{:keys [userid caseid clientid id token]} :query} :parameters}]
  (log/info "create caseplan" userid caseid clientid id token)
  (if-not (auth/authenticated? userid caseid clientid "caseplan" id token)
    (do
      (log/info "unauthorised! create caseplan" userid caseid clientid id token)
      (serve-unauth))
    (let [plan (caseplan/create clientid caseid userid)]
      (auth/update-session (get-in plan [:header :plan-id]) token)
      (log/debug (:header plan))
      (-> plan
          ->camelCase
          ok))))

(defn get-plan
  [{{{:keys [userid caseid clientid id token]} :query} :parameters}]
  (log/info "get caseplan" userid caseid clientid id token)
  (if-not (auth/authenticated? userid caseid clientid "caseplan" id token)
    (do
      (log/info "unauthorised! get caseplan" userid caseid clientid id token)
      (serve-unauth))
    (let [plan (caseplan/retrieve id clientid userid)]
      (log/debug (:header plan))
      (-> plan
          ->camelCase
          ok))))

(defn save-plan
  [{{{:keys [userid caseid clientid id token]} :query} :parameters :as request}]
  (log/info "save caseplan" userid caseid clientid id token)
  (if-not (auth/authenticated? userid caseid clientid "caseplan" id token)
    (do
      (log/info "unauthorised! save caseplan" userid caseid clientid id token)
      (serve-unauth))
    (do (-> request
            :body
            slurp
            (caseplan/save userid))
        (let [plan (caseplan/retrieve id clientid userid)]
          (log/debug (:header plan))
          (-> plan
              ->camelCase
              ok)))))

(defn save-contact-determination
  [{{{:keys [userid caseid clientid id token]} :query} :parameters :as request}]
  (log/info "save caseplan contact det" userid caseid clientid id token)
  (if-not (auth/authenticated? userid caseid clientid "caseplan" id token)
    (do
      (log/info "unauthorised! save caseplan contact det" userid caseid clientid id token)
      (serve-unauth))
    (do (-> request
            :body
            slurp
            (caseplan/save-contact-determination userid))
        (let [cont-det (caseplan/retrieve-contact-determination id)]
          (log/debug (:header cont-det))
          (-> cont-det
              ->camelCase
              ok)))))

(defn caseplan-routes []
  ["/caseplan"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware]}

   [""
    {:get {:parameters {:query {:userid string?
                                :caseid pos-int?
                                :clientid string?
                                :id int?
                                :token string?}}
           :handler serve-spa-html}}]

   ["/existing"
    {:get {:parameters {:query {:userid string?
                                :caseid pos-int?
                                :clientid string?
                                :id pos-int?
                                :token string?}}
           :handler get-plan}}]

   ["/new"
    {:post {:parameters {:query {:userid string?
                                 :caseid pos-int?
                                 :clientid string?
                                 :id int?
                                 :token string?}}
            :handler create-plan}}]

   ["/save"
    {:post {:parameters {:query {:userid string?
                                 :caseid pos-int?
                                 :clientid string?
                                 :id pos-int?
                                 :token string?}}
            :handler save-plan}}]

   ["/savecontact"
    {:post {:parameters {:query {:userid string?
                                 :caseid pos-int?
                                 :clientid string?
                                 :id pos-int?
                                 :token string?}}
            :handler save-contact-determination}}]

   ["/workers"
    {:get {:handler get-workers}}]

   ["/offices"
    {:get {:handler get-offices}}]])