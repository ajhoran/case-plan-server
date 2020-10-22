(ns case-plan-server.scheduler.core
  (:require
    [case-plan-server.config :refer [env]]
    [case-plan-server.scheduler.print :as p]
    [case-plan-server.scheduler.session :as s]
    [case-plan-server.scheduler.workflow :as w]
    [immutant.scheduling :as sch]
    [mount.core :refer [defstate]]))

(defstate scheduler
          :start
          (do (sch/schedule p/print-request-job {:id   "print-request-job"
                                                 :cron (env :print-request-job-cron)
                                                 :allow-concurrent-exec? false})
              (sch/schedule p/print-check-job {:id   "print-check-job"
                                               :cron (env :print-check-job-cron)
                                               :allow-concurrent-exec? false})
              (sch/schedule w/workflow-job {:id   "workflow-job"
                                            :cron (env :workflow-job-cron)
                                            :allow-concurrent-exec? false})
              (sch/schedule s/session-job {:id "session-job"
                                           :cron (env :session-job-cron)
                                           :allow-concurrent-exec? false}))
          :stop
          (do (sch/stop {:id "print-request-job"})
              (sch/stop {:id "print-check-job"})
              (sch/stop {:id "workflow-job"})
              (sch/stop {:id "session-job"})))
