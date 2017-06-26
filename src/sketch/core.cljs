(ns sketch.core
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [sketch.components.canvas :as canvas]
            [sketch.components.css :as css]
            [sketch.components.editor :as editor]
            [sketch.state :as sketch.state]))

(enable-console-print!)

;;FIXME: Is there no better way to foil cljr-clean-ns???
(def nothing sketch.state/default-db)

(def debug?
  ^boolean goog.DEBUG)

(defn main-panel []
  (fn []
    [css/row
     [editor/wired-panel]
     [canvas/wired-panel]]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:init-db])
  #_(when debug?
    (dev-setup))
  (mount-root))
