(ns sketch.core
  (:require [cljs.core.async :refer [<!]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            sketch.editor
            sketch.state
            [sketch.canvas :as canvas])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

(def canvas-events
  [[[:on-mouse-down :on-touch-start] [:draw-start]]
   [[:on-mouse-move :on-touch-move] [:draw-move]]
   [[:on-mouse-up :on-touch-end :on-mouse-out :on-mouse-leave] [:draw-end]]])

(def editor-events
  [[[:on-input :on-change] [:code-edit]]])


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

(defn canvas-panel []
  (reagent/create-class
   {:component-did-mount
    #(re-frame/dispatch [:resize-canvas])
    :reagent-render
    (fn []
      [:canvas (assoc (event-map canvas-events) :id "the-canvas")])}))

(defn editor-panel []
  (fn []
    [:textarea
     (assoc (event-map editor-events)
            :id "editor"
            :value @(re-frame/subscribe [:sketch.editor/content]))]))

(defn main-panel []
  (fn []
    [:div
     [editor-panel]
     [canvas-panel]]))


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
