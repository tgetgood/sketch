(ns sketch.components.editor
  (:require [cljs.js :refer [empty-state eval js-eval]]
            [cljs.pprint :as pprint]
            [cljs.tools.reader :refer [read-string]]
            [re-frame.core :as re-frame]
            [sketch.components.css :as css]
            [sketch.events.editor :as events]))

;; canvas <=> ds <=> string code buffer <=> code editor
;; The string code buffer is important because we want to keep non-functional
;; code while the user works on it, but we don't want to kill the UI by trying
;; to read it.
;;
;; TODO: Look at how both paren-soup and figwheel manage this.
;; TODO: These guys needs to go -mapnto some common subscription ns.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; cljs eval demo
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn format-code
  "Returns (str s) formatted as Clojure code.
  Results are undefined if s is not a readable Clojure sexp."
  [s]
  ;; Currently does nothing. pprint is too slow to use here. str is pretty slow
  ;; as well but tolerable.
  (str s)
  #_(with-out-str (pprint/pprint s)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn inner-editor-panel [content]
  [:textarea
   (assoc events/editor-event-map
          :style {:resize "none"
                  :width "100%"
                  :height "50vh"}
          :value (format-code content))])

(defn editor-panel []
  (let [content (re-frame/subscribe [:current-drawing])]
    (fn []
      [inner-editor-panel @content])))

(defn shape-token [shape current]
  [css/button
   (merge {:on-click
           #(re-frame/dispatch [:sketch.events.editor/set-current shape])
           :draggable true
           :on-drag-start
           (fn [e]
             (let [x (re-frame/subscribe [:thumbnail shape])
                   img (js/document.createElement "image")]
               (.setAttribute img "src" @x)
               (.appendChild (js/document.getElementById "app") img)
               (-> e .-dataTransfer (.setDragImage img 0 0))))}
          (when (= shape current)
            {:style {:color "red"}}))
   shape])

(defn new-shape-token []
  [css/button {:on-click #(re-frame/dispatch [:sketch.events.editor/new-shape])}
   [:span "+"]])

(defn shape-list [shapes current]
  [:div
   [css/row [new-shape-token]]
   (map (fn [s]
          (vary-meta [css/row [shape-token s current]]
                     assoc :key s))
     shapes)])

(defn left-panel [shapes current code]
  [css/row
   ^{:width 3} [shape-list shapes current]
   (when current
     ^{:width 9} [inner-editor-panel code])])

(defn wired-panel []
  (let [drawings (re-frame/subscribe [:drawings])
        current (re-frame/subscribe [:current-shape])
        code (re-frame/subscribe [:current-drawing])]
    (fn []
      (let [shapes (keys @drawings)]
        [left-panel shapes @current @code]))))
