(ns sketch.canvas
  (:require [re-frame.core :as re-frame]
            [sketch.affine :refer [dist]]))

;;;;; Ugly canvas stuff

(defn canvas []
  (.getElementById js/document "the-canvas"))

(defn ctx []
  (when-let [canvas (canvas)]
    (.getContext canvas "2d")))

(def width (quot js/window.innerWidth 2))
(def height (.-innerHeight js/window))

(defn set-canvas! []
  (set! (.-width canvas) width)
  (set! (.-height canvas) height))

(defn clear! []
  (.clearRect (ctx) 0 0 width height))

(defn loc*
  [e]
  (when (and (.-clientX e) (.-clientY e))
    (let [w (quot js/window.innerWidth 2)]
      [(- (.-clientX e) w) (.-clientY e)])))

(defn loc
  [e]
  (or (loc* e)
      (let [ts (.-changedTouches e)]
        (when (> (.-length ts) 0)
          (-> ts (aget 0) loc*)))))

(defn get-point [drawings e]
  (when-let [p (loc e)]
    (->> drawings
         (map (fn [[k v]] [k (dist v p)]))
         (sort-by second)
         first
         first)))

;;;;; Shapes

;; TODO: revisit the drawing schema
(defn segment [prev l t]
  {:type :s
   :start prev
   :end l
   :timestamp t})

;;;;; Touch / Click Handlers

(re-frame/reg-event-db
 :draw-start
 (fn [db [_ e]]
   (if-let [p (loc e)]
     (update db :active-pointers assoc (gensym) p)
     db)))

(re-frame/reg-event-db
 :draw-move
 (fn [{:keys [active-pointers] :as db} [_ e]]
   (if-let [point (get-point active-pointers e)]
     (let [p (get active-pointers point)
          q (loc e)
          t (js/Date.now)]
       (.log js/console (-> db :drawing :segments count))
       (if (and point q)
        (-> db
            (update :active-pointers assoc point q)
            (update-in [:drawing :segments]
                       conj (segment p q t)))
        db)))
   db))

(re-frame/reg-event-db
 :draw-end
 (fn [{:keys [active-pointers] :as db} [_ e]]
   (update db :active-pointers dissoc (get-point active-pointers e))))

;;;;; Drawing on canvas

(def empty-canvas {:type :squiggle :segments []})

(defmulti draw* :type)

(defmethod draw* ::bezier
  [[_ {[c1x c1y] ::c1
       [c2x c2y] ::c2
       [e1x e1y] ::e1
       [e2x e2y] ::e2}]]
  (.moveTo (ctx) e1x e1y)
  (.bezierCurveTo (ctx) c1x c1y c2x c2y e2x e2y))

(defmethod draw* :s
  [{[x1 y1] :start [x2 y2] :end}]
  (.moveTo (ctx) x1 y1)
  (.lineTo (ctx) x2 y2))

(defmethod draw* :squiggle
  [{:keys [segments]}]
  (doall (map draw* segments)))

(defmethod draw* :fg
  [[_ data]]
  ;; TODO: Would it make more sense to have a normalisation preprocessor and
  ;; keep this purely side effectful?
  (draw* 
   [::union
    (mapv (fn [p q] [::line [p q]]) data (rest data))]))

(defn get-style [ctx]
  {})

(defn set-style [ctx style]
  {})

(defn draw! [ctx path]
  ;; TODO: Save old style
  ;; TODO: Set path style
  (.beginPath ctx)
  (draw* path)
  (.stroke ctx)
  (.closePath ctx))

(re-frame/reg-sub
 :drawing
 (fn [db _]
   (.log js/console db)
   (:drawing db)))

(re-frame/reg-sub
 :drawn-canvas
 (fn [_ _] (re-frame/subscribe [:drawing]))
 (fn [drawing _]
   (.log js/console drawing)
   (when-let [c (ctx)]
     (draw! ctx drawing))
   (:segments drawing)))
