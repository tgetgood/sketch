(ns sketch.canvas
  "Utils for manipulation HTML canvas"
  (:require [sketch.affine :refer [dist]]))

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

(defn set-canvas-size! []
  (set! (.-width (canvas)) (- (width) 10))
  (set! (.-height (canvas)) (- (height) 10)))

(defn clear! []
  (.clearRect (get-ctx) 0 0 (width) (height)))

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

(def empty-canvas {:type :squiggle :segments []})

(defmulti draw* :type)

(defmethod draw* ::bezier
  [[_ {[c1x c1y] ::c1
       [c2x c2y] ::c2
       [e1x e1y] ::e1
       [e2x e2y] ::e2}]]
  (.moveTo (get-ctx) e1x e1y)
  (.bezierCurveTo (get-ctx) c1x c1y c2x c2y e2x e2y))

(defmethod draw* :s
  [{[x1 y1] :start [x2 y2] :end}]
  (.moveTo (get-ctx) x1 y1)
  (.lineTo (get-ctx) x2 y2))

(defmethod draw* :squiggle
  [{:keys [segments]}]
  (doall (map draw* segments)))

(defmethod draw* :fg
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

(defn draw! [ctx path]
  ;; TODO: Save old style
  ;; TODO: Set path style
  (.beginPath ctx)
  (draw* path)
  (.stroke ctx)
  (.closePath ctx))

