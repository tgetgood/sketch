(ns sketch.analysis
  (:require [sketch.util :refer [canvas ctx]]))

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

(defn d [path t delta]
  (let [[x2 y2] (nth path (+ t delta))
        [x1 y1] (nth path t)]
    [(/ (- x2 x1) delta) (/ (- y2 y1) delta)]))

(defn tangent [path t delta l]
  (let [[dx dy] (d path t delta)
        [x y] (nth path t)]
    [[:sketch.core/line [[x y] [(+ x (* dx l)) (+ y (* dy l))]]]]))

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
