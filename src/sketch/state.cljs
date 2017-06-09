(ns sketch.state
  (:require [re-frame.core :as re-frame]
            [sketch.canvas :as canvas]))

(def default-db
  {:active-pointers {}
   :current-shape "shape~1"
   :drawings {"shape~1" canvas/empty-canvas}})

(re-frame/reg-event-db
 :init-db
 (fn [_ _]
   default-db))
