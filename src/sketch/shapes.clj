(ns sketch.shapes)

(defn keyify [s]
  [(keyword (name s)) s])

(defmacro defshape
  [type args]
  (let [rec (gensym)
        constructor (symbol (str "->" rec))]
    `(do
       (defrecord ~rec ~(mapv (comp symbol name) args)
         IShaped
         (shape [_#] ~type))
       (spec/def ~type (spec/keys :req-un ~args))
       (spec/fdef ~constructor
                  :args (spec/cat ~@(mapcat keyify args))
                  :ret ~type)
       (defmethod ~'construct ~type
         [_# & args#]
         (apply ~constructor args#)))))
