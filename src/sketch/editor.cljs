(ns sketch.editor
  (:require [cljs.js :refer [empty-state eval js-eval]]
            [cljs.pprint :as pprint]
            [cljs.tools.reader :refer [read-string]]
            [re-frame.core :as re-frame]))

;; canvas <=> ds <=> string code buffer <=> code editor
;; The string code buffer is important because we want to keep non-functional
;; code while the user works on it, but we don't want to kill the UI by trying
;; to read it.
;;
;; TODO: Look at how both paren-soup and figwheel manage this.

(defn try-read
  [s]
  (try
    (read-string s)
    (catch js/Error e nil)))

(re-frame/reg-event-fx
 ::edit
 (fn [{:keys [db]} [_ e]]
   (.log js/console (-> e .-target .-value))
   (if-let [d (-> e .-target .-value try-read)]
     {:db (assoc db :drawing d)
      :redraw-canvas d}
     {:db db})))

(re-frame/reg-sub
 :drawing
 (fn [db _]
   (:drawing db)))

(re-frame/reg-sub
 ::content
 (fn [_ _]
   (re-frame/subscribe [:drawing]))
 (fn [drawing _]
   (with-out-str (pprint/pprint drawing))))

(defn code-edit-cb [{:keys [error value]}]
  )

(defn code-edit [e]
  (let [code 444]
    (eval (empty-state)
              (read-string code)
              {:eval js-eval
               :source-map true
               :context :expr}
              code-edit-cb)))
