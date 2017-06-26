(ns sketch.events.editor
  (:require [cljs.tools.reader :refer [read-string]]
            [re-frame.core :as re-frame]
            [sketch.state :as state]))

(defn try-read
  [s]
  (try
    (read-string s)
    (catch js/Error e nil)))

(defn textarea-change
  [e]
  (-> e .-target .-value))

(def editor-event-map
  {:on-change (fn [e] (re-frame/dispatch [::edit (textarea-change e)]))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn update-drawing
  "Given a drawing and a new string representing code, update the drawing."
  [old s]
  (let [r (try-read s)]
    (cond-> old
      s                                  (assoc :edit-string s)
      r                                  (assoc :edit-read r)
      (and r (not= r (:shape-data old))) (assoc :shape-data r))))

(re-frame/reg-event-fx
 ::edit
 (fn [{:keys [db]} [_ s]]
   {:db (update-in db [:drawings (:current-shape db)] update-drawing s)}))

(re-frame/reg-event-db
 ::new-shape
 (fn [db _]
   (let [new-shape (str (gensym))]
     (-> db
         (update :drawings assoc new-shape state/empty-drawing)
         (assoc :current-shape new-shape)))))

(re-frame/reg-event-db
 ::set-current
 (fn [db [_ shape]]
   (assoc db :current-shape shape)))

