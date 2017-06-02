(ns sketch.handlers
  (:require [cljs.core.async :refer [chan put!]]
            [clojure.set :as set]
            [sketch.affine :refer [dist]]
            [sketch.util :refer [ctx now pp]]))

(def shape-chan (chan))

(def ^:private drawings (atom {}))

(defn segment [prev l t]
  {:type :s
   :start prev
   :end l
   :timestamp t})

(defn loc*
  [e]
  (when (and (.-clientX e) (.-clientY e))
    (let [w (quot js/window.innerWidth 2)]
      [(- (.-clientX e) w) (.-clientY e)])))

;;FIXME: The switch in this multimethod is terrible
(defmulti loc (fn [e] (subs (.-type e) 0 5)))

(defmethod loc "mouse" [e] (loc* e))

(defmethod loc "touch"
  [e]
  (let [ts (.-changedTouches e)]
    (when (> (.-length ts) 0)
      (-> ts (aget 0) loc*))))

(defn get-point [e]
  (when-let [p (loc e)]
    (pp (->> @drawings
         (map (fn [[k v]] [k (dist v p)]))
         #_(sort-by second)
             ))
    (->> @drawings
         (map (fn [[k v]] [k (dist v p)]))
         (sort-by second)
         first
         first)))

(defn draw-start-handler [e]
  (swap! drawings assoc (gensym) [(loc e)]))

(defn draw-move-handler [e]
  (when-let [point (get-point e)]
    (let [p (get @drawings point)
          q (loc e)
          t (js/Date.now)]
      (put! shape-chan (segment p q t))
      (swap! drawings assoc point q))))

(defn draw-end-handler [e] 
  (swap! drawings dissoc (get-point e)))


(def draw-events
  [{:events ["mousedown" "touchstart"] :handler draw-start-handler}
   {:events ["mousemove" "touchmove"] :handler draw-move-handler}
   {:events ["mouseup" "touchend"] :handler draw-end-handler}])

(defn handle-handlers [f]
  (doseq [{:keys [events handler]} draw-events]
    (doseq [evt events]
      (f evt handler))))

(defn init! [canvas]
  (handle-handlers
   (fn [e f] (.addEventListener canvas e f)))
  (fn []
    (handle-handlers
     (fn [e f] (.removeEventListener canvas e f)))))

;; (defn- keysets [m]
;;   (into {} (map (fn [[k v]]
;;                    (if (set? v)
;;                      [k v]
;;                      [k #{v}]))
;;               m)))

;; (defn- init! [canvas]
;;   (let [ls (apply merge-with set/union
;;                   (map keysets active-listener-groups))]
;;     (doseq [[e fs] ls]
;;       (doseq [f fs]
;;         (.addEventListener canvas e f)))
;;     (fn []
;;       (doseq [[e fs] ls]
;;         (doseq [f fs]
;;           (.removeEventListener canvas e f))))))

