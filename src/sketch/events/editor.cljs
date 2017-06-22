(ns sketch.events.editor
  (:require [cljs.tools.reader :refer [read-string]]
            [re-frame.core :as re-frame]
            [sketch.canvas :as canvas]
            [sketch.events.common :as common]))

(def editor-events
  [[[:on-input :on-change] [::edit]]])

(def editor-event-map
  (common/event-map editor-events))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn try-read
  [s]
  (try
    (read-string s)
    (catch js/Error e nil)))

(re-frame/reg-event-fx
 ::edit
 (fn [{:keys [db]} [_ e]]
   (if-let [d (-> e .-target .-value try-read)]
     {:db (assoc db :drawing d)
      :sketch.events.canvas/redraw-canvas d}
     {:db db})))

(re-frame/reg-event-db
 ::new-shape
 (fn [db _]
   (let [new-shape (str (gensym))]
     (-> db
         (update :drawings assoc new-shape canvas/empty-canvas)
         (assoc :current-shape new-shape)))))

(re-frame/reg-event-db
 ::set-current
 (fn [db [_ shape]]
   (assoc db :current-shape shape)))

