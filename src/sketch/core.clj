(ns sketch.core
  (:require [quil.core :as q]))

(defn setup []
  (q/frame-rate 1)
  (q/background 0xFFFFFF))

(defn draw []
  )



(defmacro defalias [spec aliases]
  `(do
     ~@(for [a aliases]
         `(s/def ~a ~spec))))

;;;;; Fast code for comparing bitmaps

;; Takes ~100ms for l = 2206948

(def x 348)
