(ns sketch.events.common
  (:require [re-frame.core :as re-frame]
            [sketch.canvas :as canvas]))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Subscriptions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 :drawings
 (fn [db _]
   (:drawings db)))

(re-frame/reg-sub
 :current-shape
 (fn [db _]
   (:current-shape db)))

(re-frame/reg-sub
 :current-drawing
 (fn [_ _]
   [(re-frame/subscribe [:current-shape])
    (re-frame/subscribe [:drawings])])
 (fn [[current drawings] _]
   (get drawings current)))

(re-frame/reg-sub
 :current-shape-data
 (fn [_ _]
   (re-frame/subscribe [:current-drawing]))
 (fn [current _]
   (:shape-data current)))

(re-frame/reg-sub
 :current-edit-string
 (fn [_ _]
   (re-frame/subscribe [:current-drawing]))
 (fn [current _]
   (:edit-string current)))

(def silly-canvas
  (js/document.createElement "canvas"))

(def silly-ctx
  (.getContext silly-canvas "2d"))

(re-frame/reg-sub
 :shape-thumbnail
 (fn [_ _]
   (re-frame/subscribe [:drawings]))
 (fn [drawings [_ shape]]
   (let [data (get-in drawings [shape :shape-data])]
     (canvas/set-canvas-size! silly-canvas)
     (canvas/clear! silly-ctx)
     (canvas/draw! silly-ctx data)
     (.toDataURL silly-canvas ))))
