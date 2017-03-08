(ns sketch.core
  (:require [monet.canvas :as c]))

(enable-console-print!)

;;;;; Canvas

(def canvas (.getElementById js/document "the-canvas"))

(set! (.-width canvas) (.-innerWidth js/window))
(set! (.-height canvas) (.-innerHeight js/window))

(def ctx (.getContext canvas "2d"))

;;;;; Drawing

(def drawing? (atom false))
(defonce current-path (atom []))

(defn loc [e]
  [(.-clientX e) (.-clientY e)])

(set! (.-lineWidth ctx) 1)
(set! (.-strokeStyle ctx) "#000")


(def drag-listeners
  {"mousedown" (fn [e]
                 (reset! drawing? true)
                 (reset! current-path [(loc e)]))
   "mousemove" (fn [e]
                 (when @drawing?
                   (swap! current-path conj (loc e))))
   "mouseup" (fn [e]
               (reset! drawing? false))})

(defn render-path! [path]
  (.beginPath ctx)
  (doall (map (fn [[x y]] (.lineTo ctx x y)) path))
  (.stroke ctx)
  (.closePath ctx))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Listener Reloading Logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def listeners
  (atom {}))

(defn add-listener! [event fn]
  (swap! listeners update event (fnil conj #{}) fn))

(doall (map (partial apply add-listener!) drag-listeners))

(defonce ^:private loaded-listeners (atom {}))

(defn reset-listeners! [canvas]
  (do
    (doseq [[e fns] @loaded-listeners]
      (doseq [fn fns]
        (.removeEventListener canvas e fn)))
    (doseq [[e fns] @listeners]
      (doseq [fn fns]
        (.addEventListener canvas e fn)))
    (reset! loaded-listeners @listeners)))

(defn on-js-reload []
  (reset-listeners! canvas)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
