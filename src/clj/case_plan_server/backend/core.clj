(ns case-plan-server.backend.core
  (:require
    [camel-snake-kebab.core :refer [->camelCaseKeyword
                                    ->kebab-case-keyword
                                    ->snake_case_keyword]]
    [camel-snake-kebab.extras :refer [transform-keys]]))

(defn uri->app
  [uri]
  (re-find #"[a-z]+" uri))

(defn ->camelCase
  [coll]
  (transform-keys ->camelCaseKeyword coll))

(defn ->kebab-case
  [coll]
  (transform-keys ->kebab-case-keyword coll))

(defn ->snake_case
  [coll]
  (transform-keys ->snake_case_keyword coll))
