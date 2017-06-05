(ns sketch.state
  (:require [re-frame.core :as re-frame]))

(def default-db
  {:drawings {}})

(re-frame/reg-event-db
 :init-db
 (fn [_ _]
   default-db))
