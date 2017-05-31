(ns sketch.handlers
  (:require [clojure.set :as set]
            [sketch.util :refer [canvas ctx current-path loc pp]]))

(def curve-listeners
  (let [drawing? (atom false)
        lp (atom nil)]
    {"mousedown" (fn [e]
                   (reset! drawing? true)
                   (reset! lp (loc e)))
     "mousemove" (fn [e]
                   (when @drawing?
                     (swap! current-path update 1 conj (loc e))
                     (reset! lp (loc e)) ))
     "mouseup" (fn [e]
                 (reset! lp nil)
                 (reset! drawing? false))}))


(def draw-listeners
  (let [drawing? (atom false)]
    {"mousedown" (fn [e]
                   (pp e)
                   (pp (loc e))
                   (reset! drawing? true)
                   (.beginPath ctx)
                   (let [[x y] (loc e)]
                     (.moveTo ctx x y)))
     "mousemove" (fn [e]
                   (when @drawing?
                     (let [[x y] (loc e)]
                       (.lineTo ctx x y)
                       (.stroke ctx))))
     "mouseup" (fn [e]
                 (.closePath ctx)
                 (reset! drawing? false))}))


(def active-listener-groups
  [curve-listeners
   draw-listeners])


(defn- keysets [m]
  (into {} (map (fn [[k v]]
                   (if (set? v)
                     [k v]
                     [k #{v}]))
              m)))

(defn- start* [canvas]
  (let [ls (apply merge-with set/union
                  (map keysets active-listener-groups))]
    (doseq [[e fs] ls]
      (doseq [f fs]
        (.addEventListener canvas e f)))
    (fn []
      (doseq [[e fs] ls]
        (doseq [f fs]
          (.removeEventListener canvas e f))))))

(defn start! []
    (let [stop! (atom (start* canvas))]
    (.log js/console "Restarting hadlers.")
    (defn on-js-reload []
      (@stop!)
      (reset! stop! (start* canvas)))))
