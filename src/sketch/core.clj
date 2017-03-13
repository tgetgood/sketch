(ns sketch.core)

(defmacro defalias [spec aliases]
  `(do
     ~@(for [a aliases]
         `(s/def ~a ~spec))))
