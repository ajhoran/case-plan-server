(ns case-plan-server.core
  (:require
    [case-plan-server.config :refer [env]]
    [case-plan-server.handler :as handler]
    [case-plan-server.nrepl :as nrepl]
    [case-plan-server.scheduler.core :as scheduler]
    [luminus.http-server :as http]
    [clojure.tools.cli :refer [parse-opts]]
    [clojure.tools.logging :as log]
    [mount.core :as mount])
  (:gen-class))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread ex]
      (log/error {:what :uncaught-exception
                  :exception ex
                  :where (str "Uncaught exception on" (.getName thread))}))))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(mount/defstate ^{:on-reload :noop} http-server
  :start
  (http/start
    (-> env
        (update :io-threads #(or % (* 2 (.availableProcessors (Runtime/getRuntime))))) 
        (assoc  :handler (handler/app))
        (update :port #(or (-> env :options :port) %))))
  :stop
  (http/stop http-server))

(mount/defstate ^{:on-reload :noop} repl-server
  :start
  (when (env :nrepl-port)
    (nrepl/start {:bind (env :nrepl-bind)
                  :port (env :nrepl-port)}))
  :stop
  (when repl-server
    (nrepl/stop repl-server)))


(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& args]
  (mount/start #'case-plan-server.config/env)
  (cond
    (nil? (:database-url env))
    (do
      (log/error "Database configuration not found, :database-url environment variable must be set before running")
      (System/exit 1))
    (or (nil? (:c3integration-url env))
        (nil? (:c3integration-retry env)))
    (do
      (log/error "C3MS Integration configuration not found, :c3integration-url and c3integration-retry must be set")
      (System/exit 1))
    (or (nil? (:print-request-job-cron env))
        (nil? (:print-check-job-cron env))
        (nil? (:workflow-job-cron env)))
    (do
      (log/error "Job Schedule configuration not found, :print-request-job-cron, :print-check-job-cron and :workflow-job-cron must be set")
      (System/exit 1))
    :else
    (start-app args)))
  
