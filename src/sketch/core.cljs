(ns sketch.core
  (:require-macros
   [sketch.core :refer [defalias]])
  (:require [clojure.set :as set]
            [clojure.spec :as s]))

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

(defn render-bezier [{[c1x c1y] ::c1
                      [c2x c2y] ::c2
                      [e1x e1y] ::e1
                      [e2x e2y] ::e2}]
  (.beginPath ctx)
  (.moveTo ctx e1x e1y)
  (.bezierCurveTo ctx c1x c1y c2x c2y e2x e2y)
  (.stroke ctx)
  (.closePath ctx))

(def active-listener-groups
  [curve-listeners
   draw-listeners])

;;;;; Curves

(def shapes
  "Higher level shapes"
  [::line
   ::circle
   ::ellipse
   ::polynomial
   ::exponential])

;; Everything below is 2D

(s/def ::point (s/tuple number? number?))

(defalias ::point [::e1 ::e2 ::c1 ::c2])

(s/def ::bezier
  (s/keys :req [::e1 ::e2 ::c1 ::c2]))

;;;;; Pixel mappings

(defn- get-pixels []
  (.-data (.getImageData ctx 0 0 (.-width canvas) (.-height canvas))))

(defn- pixel-distance
  "Returns the number of pixels that differ between the 2 images.
  N.B.: This is intended for monochrome images and so only compares the alpha
  bytes."
  [p1 p2]
  (let [c (volatile! 0)]
    (doseq [i (range 0 (.-length p1) 4)]
      (when (not= (aget p1 (+ i 3)) (aget p2 (+ i 3)))
        (vswap! c inc)))
    @c))

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
