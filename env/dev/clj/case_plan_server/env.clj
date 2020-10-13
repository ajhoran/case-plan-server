(ns case-plan-server.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [case-plan-server.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[case-plan-server started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[case-plan-server has shut down successfully]=-"))
   :middleware wrap-dev})
