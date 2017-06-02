(ns sketch.affine
  (:require [sketch.util :refer [dts]]))

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

(defn dist
  "Returns Euclidean distance between two points (in 2D)"
  [[x1 y1] [x2 y2]]
  (let [dx (- x2 x1)
        dy (- y2 y1)]
    (js/Math.sqrt (+ (* dx dx) (* dy dy)))))
