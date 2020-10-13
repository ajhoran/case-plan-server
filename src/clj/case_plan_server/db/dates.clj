(ns case-plan-server.db.dates
  (:import
    [java.time LocalDate LocalDateTime]
    [java.time.format DateTimeFormatter]))

(def date-formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd"))
(def datetime-formatter (DateTimeFormatter/ofPattern "dd/MM/yyyy h:mm a"))

(defn format-date
  [^LocalDateTime date]
  (if date (.format date date-formatter) ""))

(defn format-datetime
  [^LocalDateTime datetime]
  (if datetime (.format datetime datetime-formatter) ""))

(defn parse-date
  [date-string]
  (when-not (or (nil? date-string)
                (= "" date-string))
    (LocalDate/parse date-string date-formatter)))

(defn parse-datetime
  [datetime-string]
  (when-not (or (nil? datetime-string)
                (= "" datetime-string))
    (LocalDateTime/parse datetime-string datetime-formatter)))

;; format: {table_name {column_name [formatter parser]}}
(def table-column-date
  {:case_plan {:review_due_date [format-date parse-date]
               :approved_datetime [format-datetime parse-datetime]
               :last_modified_datetime [format-datetime parse-datetime]
               :created_datetime [format-datetime parse-datetime]}
   :family_group_conf {:occurred_date [format-date parse-date]
                       :scheduled_date [format-date parse-date]}
   :outcomes_actions {:planned_when [format-date parse-date]}
   :placement {:consult_date [format-date parse-date]
               :registered_public_date [format-date parse-date]}
   :culture_identity {:received_lifestory_date [format-date parse-date]
                      :updated_lifestory_date [format-date parse-date]}
   :atsi {:genogram_date [format-date parse-date]
          :received_lifestory_date [format-date parse-date]
          :updated_lifestory_date [format-date parse-date]}
   :atsi_events {:event_date [format-date parse-date]}
   :atsi_carer_training {:training_date [format-date parse-date]
                         :review_date [format-date parse-date]}
   :atsi_consultation {:planned_when [format-date parse-date]}
   :cald {:genogram_date [format-date parse-date]
          :received_lifestory_date [format-date parse-date]
          :updated_lifestory_date [format-date parse-date]}
   :cald_events {:event_date [format-date parse-date]}
   :cald_carer_training {:training_date [format-date parse-date]
                         :review_date [format-date parse-date]}
   :cald_consultation {:planned_when [format-date parse-date]}
   :contact_determination {:issue_date [format-date parse-date]
                           :approved_datetime [format-datetime parse-datetime]}
   :endorsement_approval_contact {:action_datetime [format-datetime parse-datetime]
                                  :result_datetime [format-datetime parse-datetime]}
   :physical_health {:assessed_health_date [format-date parse-date]}
   :disability_devdelay {:ndis_expiry_date [format-date parse-date]
                         :consulted_date [format-date parse-date]}
   :education {:individual_plan_date [format-date parse-date]}
   :endorsement_approval {:action_datetime [format-datetime parse-datetime]
                          :result_datetime [format-datetime parse-datetime]}
   :actions {:planned_when [format-date parse-date]}
   :review_report {:approved_datetime [format-datetime parse-datetime]
                   :last_modified_datetime [format-datetime parse-datetime]
                   :created_datetime [format-datetime parse-datetime]
                   :review_due_date [format-date parse-date]}
   :rev_details {:charter_received_date [format-date parse-date]}
   :rev_outcomes_actions {:planned_when [format-date parse-date]}
   :rev_endorsement_approval {:action_datetime [format-datetime parse-datetime]
                              :result_datetime [format-datetime parse-datetime]}
   :rev_actions_case_plan {:planned_when [format-date parse-date]}
   :rev_actions_panel {:planned_when [format-date parse-date]}})

(defn format-date-columns
  [record date-columns]
  (reduce (fn [record [column [formatter _]]]
            (update record column formatter))
          record date-columns))

(defn parse-date-columns
  [record date-columns]
  (reduce (fn [record [column [_ parser]]]
            (update record column parser))
          record date-columns))

(defn date->string
  [coll table]
  (if (empty? coll)
    coll
    (if (vector? coll)
      (mapv #(format-date-columns % (get table-column-date table)) coll)
      (format-date-columns coll (get table-column-date table)))))

(defn string->date
  [coll table]
  (if (empty? coll)
    coll
    (if (vector? coll)
      (mapv #(parse-date-columns % (get table-column-date table)) coll)
      (parse-date-columns coll (get table-column-date table)))))