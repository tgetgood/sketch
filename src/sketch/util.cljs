(ns sketch.util)

(def canvas (.getElementById js/document "the-canvas"))
(def ctx (.getContext canvas "2d"))

(def width (quot js/window.innerWidth  2))
(def height (.-innerHeight js/window))

(defn set-canvas! []
  (set! (.-width canvas) width)
  (set! (.-height canvas) height))

(defn clear! []
  (.clearRect ctx 0 0 width height))

(defn set-colour! [c]
  (set! (.-strokeStyle ctx) c))

(defn red! []
  (set-colour! "#FF0000"))

(defn black! []
  (set-colour! "#000"))

(defn pp [x]
  (.log js/console x))

(defn loc [e]
  (let [w (quot js/window.innerWidth 2)]
    [(- (.-clientX e) w) (.-clientY e)]))

(defonce current-path (atom [::lines []]))

(def dts
  "Draw type switch. Keyfn for multimethods that take drawings."
  (fn [[t & _] & _] t))

(defn now []
  (js/Date.now))
