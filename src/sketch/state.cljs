(ns sketch.state
  (:require [re-frame.core :as re-frame]
            [sketch.canvas :as canvas]))

(def empty-canvas {:type :squiggle :segments []})

(def empty-drawing
  {:edit-string (str empty-canvas)
   :edit-read empty-canvas
   :shape-data empty-canvas})

(def default-db
  {:active-pointers {}
   :current-shape "shape~1"
   :drawings {"shape~1" empty-drawing}})

(re-frame/reg-event-db
 :init-db
 (fn [_ _]
   default-db))
