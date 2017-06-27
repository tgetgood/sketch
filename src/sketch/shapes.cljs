(ns sketch.shapes
  (:require [cljs.spec.alpha :as spec]
            [sketch.canvas :as canvas])
  (:require-macros [sketch.shapes :refer [defshape]]))

;; ::doodle represents anything which can be drawn
;; ::squiggle represents an ordered sequence of line segments
;;
;; So all squiggles are doodles, and all doodles are made up ultimately of
;; squiggles. Other than that it's basically a type system plus affine
;; transformations. And equivalences that I have no idea how to implement at
;; present. We can easily detect equivalent txs of the same image, but how do
;; you figure out if two images can be transformed one into another?
;; Normalisation? Better yet, how do you figure out if one image contains
;; another as a subimage?

(spec/def ::coord
  (spec/and vector? #(= 2 (count %)) #(every? number? %)))

(spec/def ::point
  (spec/keys :req [::coord]
             :opt [::timestamp]))

(spec/def ::start ::coord)
(spec/def ::end ::coord)

(spec/def ::segments
  (spec/* ::line))

(defmulti construct (fn [t & _] t))

(defprotocol IShaped
  (shape [this] "Returns qualified keyword corresponding to the shape"))

(defshape ::line [::start ::end ::timestamp])

(defshape ::squiggle [::segments ::timestamp])


