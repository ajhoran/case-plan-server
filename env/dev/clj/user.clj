(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [case-plan-server.config :refer [env]]
    [clojure.pprint]
    [clojure.spec.alpha :as s]
    [expound.alpha :as expound]
    [mount.core :as mount]
    [case-plan-server.core :refer [start-app]]
    [case-plan-server.db.core]
    [conman.core :as conman]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'case-plan-server.core/repl-server))

(defn stop
  "Stops application."
  []
  (mount/stop-except #'case-plan-server.core/repl-server))

(defn restart
  "Restarts application."
  []
  (stop)
  (start))

(defn restart-db
  "Restarts database."
  []
  (mount/stop #'case-plan-server.db.core/*db*)
  (mount/start #'case-plan-server.db.core/*db*)
  (binding [*ns* 'case-plan-server.db.core]
    (conman/bind-connection case-plan-server.db.core/*db* "sql/queries.sql")))
