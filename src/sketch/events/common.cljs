(ns sketch.events.common
  (:require [cljs.pprint :as pprint]
            [re-frame.core :as re-frame]))

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
 ::content
 (fn [_ _]
   (re-frame/subscribe [:current-drawing]))
 (fn [drawing _]
   (with-out-str (pprint/pprint drawing))))
