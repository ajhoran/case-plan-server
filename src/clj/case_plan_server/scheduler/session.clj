(ns case-plan-server.scheduler.session
  (:require
    [case-plan-server.config :refer [env]]
    [case-plan-server.db.core :as db]
    [clojure.tools.logging :as log]))

(defn- delete-aged-sessions
  []
  (log/debug "delete aged sessions")
  (db/delete-session-security-aged {:timeout (env :session-timeout-hrs)}))

(defn session-job
  []
  (delete-aged-sessions))