(ns sketch.core
  (:require [quil.core :as q]
            [clojure.core.reducers :as r]))

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

(defn rf
  "Adds up the differing pixels"
  ([] 0)
  ([x] x)
  ([acc [x y]]
   (if (= x y)
     acc
     (inc acc))))

(def l 256)

(def p (into [] (take l (repeatedly (fn [] [(rand-int 255) (rand-int 255)])))))
