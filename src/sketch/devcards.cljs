(ns sketch.devcards
  (:require devcards.core
            sketch.canvas
            sketch.editor
            sketch.state
            [re-frame.core :as re-frame]))

(defn ^:export init []

  (re-frame/dispatch-sync [:init-db])
  (devcards.core/start-devcard-ui!))
