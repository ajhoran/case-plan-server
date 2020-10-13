(ns case-plan-server.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[case-plan-server started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[case-plan-server has shut down successfully]=-"))
   :middleware identity})
