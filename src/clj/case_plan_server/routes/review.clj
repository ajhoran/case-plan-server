(ns case-plan-server.routes.review
  (:require
    [case-plan-server.backend.auth :as auth]
    [case-plan-server.backend.core :refer [->camelCase]]
    [case-plan-server.backend.review :as review]
    [case-plan-server.middleware.formats :as formats]
    [case-plan-server.middleware.exception :as exception]
    [case-plan-server.routes.common :refer :all]
    [clojure.tools.logging :as log]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [ring.util.http-response :refer :all]))

(defn create-review
  [{{{:keys [userid caseid clientid id viewOnly token]} :query} :parameters :as request}]
  (log/info "create review" userid caseid clientid id viewOnly token)
  (if-not (auth/authenticated? userid caseid clientid "review" id viewOnly token)
    (do
      (log/info "unauthorised! create review" userid caseid clientid id viewOnly token)
      (serve-unauth request))
    (let [review (review/create clientid caseid userid)]
      (auth/update-session (get-in review [:header :review-id]) token)
      (log/debug (:header review))
      (-> review
          ->camelCase
          ok))))

(defn get-review
  [{{{:keys [userid caseid clientid id viewOnly token]} :query} :parameters :as request}]
  (log/info "get review" userid caseid clientid id viewOnly token)
  (if-not (auth/authenticated? userid caseid clientid "review" id viewOnly token)
    (do
      (log/info "unauthorised! get review" userid caseid clientid id viewOnly token)
      (serve-unauth request))
    (let [review (review/retrieve id clientid userid)]
      (log/debug (:header review))
      (-> review
          ->camelCase
          ok))))

(defn get-all-reviews-and-open-plan
  [{{{:keys [caseid]} :query} :parameters}]
  (log/info "get all reviews and open plan" caseid)
  (-> (review/retrieve-all-reviews-and-open-plan caseid)
      ->camelCase
      ok))

(defn save-review
  [{{{:keys [userid caseid clientid id viewOnly token]} :query} :parameters :as request}]
  (log/info "save review" userid caseid clientid id viewOnly token)
  (if-not (auth/authenticated? userid caseid clientid "review" id viewOnly token)
    (do
      (log/info "unauthorised! save review" userid caseid clientid id viewOnly token)
      (serve-unauth request))
    (do (-> request
            :body
            slurp
            (review/save userid))
        (let [review (review/retrieve id clientid userid)]
          (log/debug (:header review))
          (-> review
              ->camelCase
              ok)))))

(defn review-routes []
  ["/review"
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
                                :viewOnly string?
                                :token string?}}
           :handler serve-spa-html}}]

   ["/existing"
    {:get {:parameters {:query {:userid string?
                                :caseid pos-int?
                                :clientid string?
                                :id pos-int?
                                :viewOnly string?
                                :token string?}}
           :handler get-review}}]

   ["/new"
    {:get {:parameters {:query {:userid string?
                                :caseid pos-int?
                                :clientid string?
                                :id int?
                                :viewOnly string?
                                :token string?}}
            :handler create-review}}]

   ["/save"
    {:post {:parameters {:query {:userid string?
                                 :caseid pos-int?
                                 :clientid string?
                                 :id pos-int?
                                 :viewOnly string?
                                 :token string?}}
            :handler save-review}}]

   ["/allreviewsandopenplan"
    {:get {:parameters {:query {:caseid pos-int?}}
           :handler get-all-reviews-and-open-plan}}]

   ["/workers"
    {:get {:handler get-workers}}]

   ["/offices"
    {:get {:handler get-offices}}]])