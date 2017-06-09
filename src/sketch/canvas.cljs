(ns sketch.canvas
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [sketch.affine :refer [dist]]
            [sketch.events :as events]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Ugly canvas stuff
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn canvas []
  (.getElementById js/document "the-canvas"))

(defn get-ctx []
  (when-let [canvas (canvas)]
    (.getContext canvas "2d")))

(defn width [] (quot js/window.innerWidth 2))
(defn height [] (.-innerHeight js/window))

(defn set-canvas-size! []
  (set! (.-width (canvas)) (- (width) 10))
  (set! (.-height (canvas)) (- (height) 10)))

(defn clear! []
  (.clearRect (get-ctx) 0 0 (width) (height)))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Drawing on canvas
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def empty-canvas {:type :squiggle :segments []})

(defmulti draw* :type)

(defmethod draw* ::bezier
  [[_ {[c1x c1y] ::c1
       [c2x c2y] ::c2
       [e1x e1y] ::e1
       [e2x e2y] ::e2}]]
  (.moveTo (get-ctx) e1x e1y)
  (.bezierCurveTo (get-ctx) c1x c1y c2x c2y e2x e2y))

(defmethod draw* :s
  [{[x1 y1] :start [x2 y2] :end}]
  (.moveTo (get-ctx) x1 y1)
  (.lineTo (get-ctx) x2 y2))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Event Handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-db
 :draw-start
 (fn [db [_ e]]
   (if-let [p (loc e)]
     (update db :active-pointers assoc (gensym) p)
     db)))

(re-frame/reg-event-fx
 :draw-move
 (fn [{{:keys [active-pointers drawings current-shape] :as db} :db
       [_ e] :event}]
   ;; FIXME: This code is unclear I think, but I think it's better and less
   ;; brittle than trying to find all of the failure points and sticking in
   ;; branching logic.
   ;;
   ;; Maybe it would be best to make a middleware that treats a {:db nil} effect
   ;; as a no-op?
   (or
    (when-let [point (get-point active-pointers e)]
      (let [p (get active-pointers point)
            q (loc e)
            t (js/Date.now)
            old-drawing (get drawings current-shape)
            drawing (update old-drawing
                            :segments conj (segment p q t))]
        (when (and point q)
          {:db (-> db
                   (assoc-in [:drawings current-shape] drawing)
                   (update :active-pointers assoc point q))})))
    {:db db})))

(re-frame/reg-event-db
 :draw-end
 (fn [{:keys [active-pointers] :as db} [_ e]]
   (update db :active-pointers dissoc (get-point active-pointers e))))

(re-frame/reg-event-fx
 :resize-canvas
 (fn [_]
   {:resize-canvas! true}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Effects
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-fx
 ::redraw-canvas!
 (fn [drawing]
   (.log js/console (-> drawing :segments count))
   (when-let [ctx (get-ctx)]
     (clear!)
     (draw! ctx drawing))))

(re-frame/reg-fx
 ::resize-canvas!
 (fn [_]
   (set-canvas-size!)))

(re-frame/reg-event-fx
 ::resize-canvas
 (fn [_ _]
   {::resize-canvas! true}))

(re-frame/reg-event-fx
 ::redraw-canvas
 (fn [{[_ d] :event :as a}]
   {::redraw-canvas! d}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn canvas-inner []
  (reagent/create-class
   {:component-did-mount  (fn [this]
                            (re-frame/dispatch [::resize-canvas])
                            (let [drawing (reagent/props this)]
                              (re-frame/dispatch [::redraw-canvas drawing])))
    :component-did-update (fn [this]
                            (let [drawing (reagent/props this)]
                              (re-frame/dispatch [::redraw-canvas drawing])))
    :reagent-render       (fn []
                            [:canvas
                             (assoc (events/event-map events/canvas-events)
                                    :id "the-canvas")])}))

(defn canvas-panel []
  (let [drawing (re-frame/subscribe [:current-drawing])]
    (fn []
      [canvas-inner @drawing])))
