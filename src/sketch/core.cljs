(ns sketch.core
  (:require [cljs.core.async :refer [<!]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            sketch.state
            )
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

;;;;; Paren Soup

#_(let [last-state (atom nil)]
  (defn animate! [t]
    (when (not= @last-state @draw-state)
      (update-canvas!)
      (update-editor!)
      (reset! last-state @draw-state))
    (js/window.requestAnimationFrame
     animate!)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Page init
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_(go-loop []
  (when-let [c (<! handlers/shape-chan)]
    (swap! draw-state update :segments conj c)
    (recur))) 

#_(defonce started
  (let [stop! (atom (init! canvas))]
    (js/window.requestAnimationFrame animate!)
    (.log js/console "Restarting hadlers.")
    (defn on-js-reload []
      (@stop!)
      (reset! stop! (init! canvas)))))

#_(defonce canvas-init
  ;; TODO: Why the hell does the canvas get stretched if you try to set its size
  ;; via CSS?
  (u/set-canvas!))

(defn main-panel []
  (fn []
    [:div
     [:textarea {:id "editor"}]
     [:canvas {:id "the-canvas"}]]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:init-db])
  #_(dev-setup)
  (mount-root))
