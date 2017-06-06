(ns sketch.state
  (:require [re-frame.core :as re-frame]
            [sketch.canvas :as canvas]))

(def default-db
  {:active-pointers {}
   :drawing canvas/empty-canvas})

(re-frame/reg-event-db
 :init-db
 (fn [_ _]
   default-db))
