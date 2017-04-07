(ns sketch.core
  (:require-macros
   [sketch.core :refer [defalias]])
  (:require [clojure.set :as set]
            [clojure.spec :as s]))

(enable-console-print!)

(defn pp [x]
  (.log js/console x))

;;;;; Canvas

(def canvas (.getElementById js/document "the-canvas"))

(def width (.-innerWidth js/window))
(def height (.-innerHeight js/window))

(set! (.-width canvas) width)
(set! (.-height canvas) height)

(def ctx (.getContext canvas "2d"))

;;;;; Drawing

(defonce current-path (atom [::lines []]))

(defn np! []
  (reset! current-path [::lines []]))

(defn loc [e]
  [(.-clientX e) (.-clientY e)])

(set! (.-lineWidth ctx) 1)

(defn set-colour! [c]
  (set! (.-strokeStyle ctx) c))

(defn red! []
  (set-colour! "#FF0000"))

(defn black! []
  (set-colour! "#000"))

(def curve-listeners
  (let [drawing? (atom false)
        lp (atom nil)]
    {"mousedown" (fn [e]
                   (reset! drawing? true)
                   (reset! lp (loc e)))
     "mousemove" (fn [e]
                   (when @drawing?
                     (swap! current-path update 1 conj (loc e))
                     (reset! lp (loc e)) ))
     "mouseup" (fn [e]
                 (reset! lp nil)
                 (reset! drawing? false))}))


(def draw-listeners
  (let [drawing? (atom false)]
    {"mousedown" (fn [e]
                   (pp e)
                   (reset! drawing? true)
                   (.beginPath ctx)
                   (let [[x y] (loc e)]
                     (.moveTo ctx x y)))
     "mousemove" (fn [e]
                   (when @drawing?
                     (let [[x y] (loc e)]
                       (.lineTo ctx x y)
                       (.stroke ctx))))
     "mouseup" (fn [e]
                 (.closePath ctx)
                 (reset! drawing? false))}))

(defn clear! []
  (.clearRect ctx 0 0 width height))

(def dts
  "Draw type switch. Keyfn for multimethods that take drawings."
  (fn [[t & _] & _] t))

(defmulti rotate
  ;; Rotation matrix
  ;; [[cos \theta -sin \theta]
  ;;  [sin \theta cos \theta]]
  "Returns an image rotated *clockwise* by angle about the centre point."
  dts)
(defmulti scale
  "Scales image by scalar z about centre point"
  dts)

(defmulti translate
  "Translates image by given vector"
  dts)

(defn rad [d]
  (/ (* d js/Math.PI) 180))

(defn rotate-p
  "Rotate [x2 y2] around [x1 y1] clockwise by angle as in degrees."
  [[x2 y2] [x1 y1] a]
  (let [x (- x2 x1)
        y (- y2 y1)
        c (js/Math.cos (rad a))
        s (js/Math.sin (rad a))]
    [(+ x1 (- (* x c) (* y s))) (+ (* x s) (* y c) y1)]))

(defmethod rotate ::lines
  [[_ data] centre angle]
  [::lines (map #(rotate-p % centre angle) data)])

(defn translate-p
  "Translate second arg by first.
  N.B.: It's symettric so it doesn't matter."
  [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defmethod translate ::lines
  [[_ data] v]
  [::lines (map (partial translate-p v) data)])

(defn scale-p
  "Scales [x2 y2] by s around [x1 y1]"
  [[x2 y2] [x1 y1] s]
  [(+ x1 (* s (- x2 x1))) (+ y1 (* s (- y2 y1)))])

(defmethod scale ::lines
  [[_ data] centre z]
  [::lines (map #(scale-p % centre z) data)])

(defmulti draw* dts)

(defmethod draw* ::bezier
  [[_ {[c1x c1y] ::c1
       [c2x c2y] ::c2
       [e1x e1y] ::e1
       [e2x e2y] ::e2}]]
  (.moveTo ctx e1x e1y)
  (.bezierCurveTo ctx c1x c1y c2x c2y e2x e2y))

(defmethod draw* ::line
  [[_ [[x1 y1] [x2 y2]]]]
  (.moveTo ctx x1 y1)
  (.lineTo ctx x2 y2))

(defmethod draw* ::union
  [[_ data]]
  (doall (map draw* data)))

(defmethod draw* ::lines
  [[_ data]]
  ;; TODO: Would it make more sense to have a normalisation preprocessor and
  ;; keep this purely side effectful?
  (draw* 
   [::union
    (mapv (fn [p q] [::line [p q]]) data (rest data))]))

(defn get-style [ctx]
  {})

(defn set-style [ctx style]
  {})

(defn draw! [path]
  ;; TODO: Save old style
  ;; TODO: Set path style
  (.beginPath ctx)
  (draw* path)
  (.stroke ctx)
  (.closePath ctx))

(def active-listener-groups
  [curve-listeners
   draw-listeners])

;;;;; Curves

(def shapes
  "Higher level shapes"
  [::line
   ::circle
   ::ellipse
   ::polynomial
   ::exponential])

;; Everything below is 2D

(s/def ::point (s/tuple number? number?))

(defalias ::point [::e1 ::e2 ::c1 ::c2])

(s/def ::bezier
  (s/keys :req [::e1 ::e2 ::c1 ::c2]))

;;;;; Pixel mappings

(defn- get-pixels []
  (.-data (.getImageData ctx 0 0 (.-width canvas) (.-height canvas))))

(defn- pixel-distance
  "Returns the number of pixels that differ between the 2 images.
  N.B.: This is intended for monochrome images and so only compares the alpha
  bytes."
  [p1 p2]
  (let [c (volatile! 0)]
    (doseq [i (range 0 (.-length p1) 4)]
      (when (not= (aget p1 (+ i 3)) (aget p2 (+ i 3)))
        (vswap! c inc)))
    @c))

;;;;; Discrete math

(defn d [path t delta]
  (let [[x2 y2] (nth path (+ t delta))
        [x1 y1] (nth path t)]
    [(/ (- x2 x1) delta) (/ (- y2 y1) delta)]))

(defn draw-tangent! [path t delta l]
  (let [[dx dy] (d path t delta)
        [x y] (nth path t)]
    (draw! {::type ::line
            ::data [[x y] [(+ x (* dx l)) (+ y (* dy l))]]})))

(defn aff [[x y] [dx dy] l]
  [(+ x (* dx l)) (+ y (* dy l))])

(defn bfit [path l1 l2]
  (let [points (mapv (comp first second) (second path))
        e1 (first points)
        e2 (last points)
        d1 (d points 0 5)
        d2 (d points (dec (count points)) -5)]
    [::bezier {::e1 e1 ::e2 e2
               ::c1 (aff e1 d1 l1) ::c2 (aff e2 d2 (- l2))}]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Listener Reloading Logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- keysets [m]
  (into {} (map (fn [[k v]]
                   (if (set? v)
                     [k v]
                     [k #{v}]))
              m)))

(defn- start! [canvas]
  (let [ls (apply merge-with set/union
                  (map keysets active-listener-groups))]
    (doseq [[e fs] ls]
      (doseq [f fs]
        (.addEventListener canvas e f)))
    (fn []
      (doseq [[e fs] ls]
        (doseq [f fs]
          (.removeEventListener canvas e f))))))

(defonce started
  (let [stop! (atom (start! canvas))]
    (defn on-js-reload []
      (@stop!)
      (reset! stop! (start! canvas)))))
