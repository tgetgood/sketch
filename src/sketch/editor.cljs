(ns sketch.editor
  (:require [re-frame.core :as re-frame]
            [cljs.js :refer [eval empty-state js-eval]]
            [cljs.tools.reader :refer [read-string]]))

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

(re-frame/reg-event-db
 :code-edit
 (fn [db [_ e]]
   (if-let [d (-> e .-target .-value try-read)]
     (assoc db :drawing d)
     db)))

(re-frame/reg-sub
 :drawing
 (fn [db _]
   (:drawing db)))

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
