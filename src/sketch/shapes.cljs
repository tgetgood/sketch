(ns sketch.shapes)

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

