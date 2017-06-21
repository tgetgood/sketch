(ns sketch.events.canvas
  (:require [re-frame.core :as re-frame]
            [sketch.events.common :as common]
            [sketch.canvas :as canvas]))

(def canvas-events
  [[[:on-mouse-down :on-touch-start] [:draw-start]]
   [[:on-mouse-move :on-touch-move] [:draw-move]]
   [[:on-mouse-up :on-touch-end :on-mouse-out :on-mouse-leave] [:draw-end]]])

(def canvas-event-map
  (common/event-map canvas-events))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Event Handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-db
 :draw-start
 (fn [db [_ e]]
   (if-let [p (canvas/loc e)]
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
    (when-let [point (canvas/get-point active-pointers e)]
      (let [p (get active-pointers point)
            q (canvas/loc e)
            t (js/Date.now)
            old-drawing (get drawings current-shape)
            drawing (update old-drawing
                            :segments conj (canvas/segment p q t))]
        (when (and point q)
          {:db (-> db
                   (assoc-in [:drawings current-shape] drawing)
                   (update :active-pointers assoc point q))})))
    {:db db})))

(re-frame/reg-event-db
 :draw-end
 (fn [{:keys [active-pointers] :as db} [_ e]]
   (update db :active-pointers dissoc (canvas/get-point active-pointers e))))

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
   (when drawing
     (when-let [ctx (canvas/get-ctx)]
       (canvas/clear!)
       (canvas/draw! ctx drawing)))))

(re-frame/reg-fx
 ::resize-canvas!
 (fn [_]
   (canvas/set-canvas-size!)))

(re-frame/reg-event-fx
 ::resize-canvas
 (fn [_ _]
   {::resize-canvas! true}))

(re-frame/reg-event-fx
 ::redraw-canvas
 (fn [{[_ d] :event :as a}]
   {::redraw-canvas! d}))
