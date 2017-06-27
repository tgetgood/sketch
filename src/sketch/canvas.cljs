(ns sketch.canvas
  "Utils for manipulation HTML canvas"
  (:require [sketch.affine :refer [dist]]
            [sketch.shapes :as shapes]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Ugly canvas stuff
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn canvas []
  ;;FIXME: This won't do very shortly
  (.getElementById js/document "the-canvas"))

(defn get-ctx []
  (when-let [canvas (canvas)]
    (.getContext canvas "2d")))

(defn width [] (quot js/window.innerWidth 2))
(defn height [] (.-innerHeight js/window))

(defn set-canvas-size! [canvas]
  (set! (.-width canvas) (- (width) 10))
  (set! (.-height canvas) (- (height) 10)))

(defn clear! [ctx]
  (.clearRect ctx 0 0 (width) (height)))

(defn loc*
  [e]
  (when (and (.-clientX e) (.-clientY e))
    (let [w (quot js/window.innerWidth 2)]
      [(- (.-clientX e) w) (.-clientY e)])))

(defn loc
  [e]
  (or (loc* e)
      (let [ts (.-changedTouches e)]
        (when (> (.-length ts) 0)
          (-> ts (aget 0) loc*)))))

(defn get-point [drawings e]
  (when-let [p (loc e)]
    (->> drawings
         (map (fn [[k v]] [k (dist v p)]))
         (sort-by second)
         first
         first)))

;;;;; Shapes

;; TODO: revisit the drawing schema
(defn segment [prev l t]
  {:type :s
   :start prev
   :end l
   :timestamp t})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Drawing on canvas
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol IDraw
  (draw [this ctx] "Draw yourself on the canvas"))

(defmulti draw* (fn [_ x] (shapes/shape x)))

;; If told to draw an invalid shape we just don't draw anything. User error
;; notification should happen at the editor level.
(defmethod draw* :default [_ _] nil)

#_(defmethod draw* ::bezier
  [ctx [_ {[c1x c1y] ::c1
           [c2x c2y] ::c2
           [e1x e1y] ::e1
           [e2x e2y] ::e2}]]
  (.moveTo ctx e1x e1y)
  (.bezierCurveTo ctx c1x c1y c2x c2y e2x e2y))

(defmethod draw* :sketch.shapes/line
  [ctx {[x1 y1] :start [x2 y2] :end}]
  (.moveTo ctx x1 y1)
  (.lineTo ctx x2 y2))

(defmethod draw* :sketch.shapes/squiggle
  [ctx {:keys [segments]}]
  (doall (map (partial draw* ctx) segments)))

(defmethod draw* :fg
  [ctx [_ data]]
  ;; TODO: Would it make more sense to have a normalisation preprocessor and
  ;; keep this purely side effectful?
  (draw* ctx
   [::union
    (mapv (fn [p q] [::line [p q]]) data (rest data))]))

(defn get-style [ctx]
  {})

(defn set-style [ctx style]
  {})

(defn draw! [ctx path]
  ;; TODO: Save old style
  ;; TODO: Set path style
  (.beginPath ctx)
  (draw* ctx path)
  (.stroke ctx)
  (.closePath ctx))

