(ns case-plan-server.handler
  (:require
    [case-plan-server.middleware :as middleware]
    [case-plan-server.routes.caseplan :refer [caseplan-routes]]
    [case-plan-server.routes.review :refer [review-routes]]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [case-plan-server.env :refer [defaults]]
    [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
    (ring/router
      [(caseplan-routes)
       (review-routes)])
    (ring/routes
      (ring/create-file-handler
        {:path "/"})
      (wrap-content-type (wrap-webjars (constantly nil)))
      (ring/create-default-handler))))

(defn app []
  (middleware/wrap-base #'app-routes))
