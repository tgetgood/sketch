(ns sketch.events
  (:require [re-frame.core :as re-frame]))


(def canvas-events
  [[[:on-mouse-down :on-touch-start] [:draw-start]]
   [[:on-mouse-move :on-touch-move] [:draw-move]]
   [[:on-mouse-up :on-touch-end :on-mouse-out :on-mouse-leave] [:draw-end]]])

(def editor-events
  [[[:on-input :on-change] [:sketch.editor/edit]]])

(defn event-map [m]
  (into
   {}
   (mapcat (fn [[ks v]]
             (map (fn [k]
                    [k (fn [e]
                         (.persist e)
                         (.preventDefault e)
                         (re-frame/dispatch (conj v e)))])
               ks))
           m)))
