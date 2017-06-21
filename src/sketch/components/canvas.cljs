(ns sketch.components.canvas
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [sketch.events.canvas :as events]))

(defn event [name]
  (keyword :sketch.events.canvas name))

(defn canvas-inner []
  (reagent/create-class
   {:component-did-mount  (fn [this]
                            (re-frame/dispatch [(event :resize-canvas)])
                            (let [drawing (reagent/props this)]
                              (re-frame/dispatch [(event :redraw-canvas) drawing])))
    :component-did-update (fn [this]
                            (let [drawing (reagent/props this)]
                              (re-frame/dispatch [(event :redraw-canvas) drawing])))
    :reagent-render       (fn []
                            [:canvas
                             (assoc events/canvas-event-map
                                    :id "the-canvas")])}))

(defn canvas-panel []
  (let [drawing (re-frame/subscribe [:current-drawing])]
    (fn []
      [canvas-inner @drawing])))
