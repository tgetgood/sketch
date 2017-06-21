(ns sketch.util)

(def dts
  "Draw type switch. Keyfn for multimethods that take drawings."
  (fn [[t & _] & _] t))

(defn now []
  (js/Date.now))
