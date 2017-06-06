(ns sketch.core
  (:require [cljs.core.async :refer [<!]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            re-frame.registrar
            sketch.state
            sketch.canvas)
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

(re-frame/reg-fx
 :redraw-canvas
 (fn [image]
   {}))


(re-frame/reg-event-db
 :code-edit
 (fn [_ _]
   (js/alert "asd")))

(def canvas-events
  [[[:on-mouse-down :on-touch-start] [:draw-start]]
   [[:on-mouse-move :on-touch-move] [:draw-move]]
   [[:on-mouse-up :on-touch-end] [:draw-end]]])

(def editor-events
  [[[:on-input] [:code-edit]]])

(defn event-map [m]
  (into
   {}
   (mapcat (fn [[ks v]]
             (map (fn [k]
                    [k (fn [e]
                         (.preventDefault e)
                         (.persist e)
                         (re-frame/dispatch (conj v e)))])
               ks))
           m)))

(defn dummy []
  (let [v @(re-frame/subscribe [:drawn-canvas])]
    (fn []
      (into [] (cons :div (doall (map (fn [x] [:div x]) v)))))))

(defn main-panel []
  [:div
   [:textarea (assoc (event-map editor-events) :id "editor")]
   [:canvas (assoc (event-map canvas-events) :id "the-canvas")]
   [dummy]])

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:init-db])
  #_(dev-setup)
  (mount-root))

(defn on-js-reload []
  )
