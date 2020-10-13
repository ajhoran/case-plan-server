(ns case-plan-server.backend.auth
  (:require
    [case-plan-server.db.core :as db])
  (:import
    [java.time LocalDateTime]))

(def c3-auth-where "userid = ? and case_id = ? and cust_id = ? and targetapp = ? and c3_target_id = ? and c3_token = ? and created_dttm > ?")

(defn authenticated?
  [user-id case-id client-id app id token]
  (boolean (:auth (db/get-session-security {:token token
                                            :case-id case-id
                                            :client-id client-id
                                            :user-id user-id
                                            :target app
                                            :target-id id}))))

(defn save-session
  [user-id case-id client-id app id token]
  (db/insert-session-security {:token token
                               :case-id case-id
                               :client-id client-id
                               :user-id user-id
                               :target app
                               :target-id id}))

(defn update-session
  [id token]
  (db/update-session-id {:target-id id
                         :token token}))

(defn c3-authed?
  [user-id case-id cust-id app c3-target-id c3-token]
  (let [within (.minusMinutes (LocalDateTime/now) 5)
        where [c3-auth-where user-id case-id cust-id app c3-target-id c3-token within]]
    (= 1 (db/delete-c3-auth-record where))))

(defn c3-authenticated?
  [userid caseid clientid app id token]
  (when-let [authed (c3-authed? userid caseid clientid app id token)]
    (save-session userid caseid clientid app id token)
    authed))