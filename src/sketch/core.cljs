(ns sketch.core
  (:require [clojure.set :as set]))

(enable-console-print!)

(defn pp [x]
  (.log js/console x))

;;;;; Canvas

(def canvas (.getElementById js/document "the-canvas"))

(set! (.-width canvas) (.-innerWidth js/window))
(set! (.-height canvas) (.-innerHeight js/window))

(def ctx (.getContext canvas "2d"))

;;;;; Drawing

(defonce current-path (atom []))

(defn loc [e]
  [(.-clientX e) (.-clientY e)])

(set! (.-lineWidth ctx) 1)
(set! (.-strokeStyle ctx) "#000")


(def curve-listeners
  (let [drawing? (atom false)]
    {"mousedown" (fn [e]
                   (reset! drawing? true)
                   (reset! current-path [(loc e)]))
     "mousemove" (fn [e]
                   (when @drawing?
                     (swap! current-path conj (loc e))))
     "mouseup" (fn [e]
                 (reset! drawing? false))}))

(def draw-listeners
  (let [drawing? (atom false)]
    {"mousedown" (fn [e]
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

(defn render-path! [path]
  (.beginPath ctx)
  (doall (map (fn [[x y]] (.lineTo ctx x y)) path))
  (.stroke ctx)
  (.closePath ctx))

(def active-listener-groups
  [curve-listeners
   draw-listeners])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Listener Reloading Logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- keysets [m]
  (into {} (map (fn [[k v]]
                   (if (set? v)
                     [k v]
                     [k #{v}]))
              m)))

(defn- start! [canvas]
  (let [ls (apply merge-with set/union
                  (map keysets active-listener-groups))]
    (doseq [[e fs] ls]
      (doseq [f fs]
        (.addEventListener canvas e f)))
    (fn []
      (doseq [[e fs] ls]
        (doseq [f fs]
          (.removeEventListener canvas e f))))))

(defonce started
  (let [stop! (atom (start! canvas))]
    (defn on-js-reload []
      (@stop!)
      (reset! stop! (start! canvas)))))
