(ns sketch.util)

#_(defn set-colour! [c]
  (set! (.-strokeStyle ctx) c))

(defn red! []
  (set-colour! "#FF0000"))

(defn black! []
  (set-colour! "#000"))

(defn pp [x]
  (.log js/console x))

(defonce current-path (atom [::lines []]))

(def dts
  "Draw type switch. Keyfn for multimethods that take drawings."
  (fn [[t & _] & _] t))

(defn now []
  (js/Date.now))
