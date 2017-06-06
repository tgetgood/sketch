(ns sketch.specs)

(def shapes
  "Higher level shapes"
  [::line
   ::circle
   ::ellipse
   ::polynomial
   ::exponential])

;; Everything below is 2D

;; (s/def ::point (s/tuple number? number?))

;; (defalias ::point [::e1 ::e2 ::c1 ::c2])

;; (s/def ::bezier
;;   (s/keys :req [::e1 ::e2 ::c1 ::c2]))

