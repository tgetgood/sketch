(ns sketch.core
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [sketch.components.canvas :as canvas]
            [sketch.editor :as editor]
            sketch.state))

(enable-console-print!)

(def debug?
  ^boolean goog.DEBUG)

(defn main-panel []
  (fn []
    [:div
     [editor/editor-panel]
     [canvas/canvas-panel]]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:init-db])
  #_(when debug?
    (dev-setup))
  (mount-root))
