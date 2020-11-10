(ns case-plan-server.backend.auto-update
  (:require
    [case-plan-server.db.dates :refer [datetime-formatter]]
    [clojure.string :refer [blank?]])
  (:import
    [java.time LocalDateTime]))

(defn- is-newly-created?
  [{:keys [created-datetime]}]
  (blank? created-datetime))

(defn- is-newly-approved?
  [{:keys [approved-datetime status]}]
  (and (= "APPROVED" status)
       (blank? approved-datetime)))

(defn- update-header
  [header set-datetime set-user set-user-name]
  (let [newly-created (is-newly-created? header)
        newly-approved (is-newly-approved? header)]
    (as-> header h
          (update h :last-modified-datetime set-datetime)
          (update h :last-modified-by set-user)
          (update h :last-modified-by-name set-user-name)
          (if newly-created (update h :created-datetime set-datetime) h)
          (if newly-created (update h :created-by set-user) h)
          (if newly-created (update h :created-by-name set-user-name) h)
          (if newly-approved (update h :approved-datetime set-datetime) h)
          (if newly-approved (update h :approved-by set-user) h)
          (if newly-approved (update h :approved-by-name set-user-name) h))))

(defn- set-action?
  [{:keys [action-datetime action]}]
  (and (not (blank? action))
       (blank? action-datetime)))

(defn- set-result?
  [{:keys [result-datetime result]}]
  (and (not (blank? result))
       (blank? result-datetime)))

(defn- update-endorsement-approval
  [endorsement-approval set-datetime]
  (let [set-action (set-action? endorsement-approval)
        set-result (set-result? endorsement-approval)]
    (as-> endorsement-approval ea
          (if set-action (update ea :action-datetime set-datetime) ea)
          (if set-result (update ea :result-datetime set-datetime) ea))))

(defn- update-endorsement-approvals
  [endorsement-approvals set-datetime]
  (mapv #(update-endorsement-approval % set-datetime) endorsement-approvals))

(defn- update-contact-determination
  [contact-determination set-datetime set-user set-user-name newly-approved]
  (let [approved (and (or newly-approved
                          (= "APPV" (:generate-status contact-determination)))
                      (blank? (:approved-datetime contact-determination)))]
    (as-> contact-determination cd
          (if approved (update cd :approved-datetime set-datetime) cd)
          (if approved (update cd :approved-by set-user) cd)
          (if approved (update cd :approved-by-name set-user-name) cd))))

(defn- update-contact-determinations
  [contact-determinations set-datetime set-user set-user-name newly-approved]
  (mapv #(update-contact-determination % set-datetime set-user set-user-name newly-approved) contact-determinations))

(defn update-fields
  [plan-or-review user-id user-name]
  (let [set-datetime (constantly (.format (LocalDateTime/now) datetime-formatter))
        set-user (constantly user-id)
        set-user-name (constantly user-name)
        newly-approved (is-newly-approved? (:header plan-or-review))]
    (-> plan-or-review
        (update :header update-header set-datetime set-user set-user-name)
        (update :endorsement-approval update-endorsement-approvals set-datetime)
        (update :contact-determinations update-contact-determinations set-datetime set-user set-user-name newly-approved)
        (update :endorsement-approval-contact update-endorsement-approvals set-datetime))))