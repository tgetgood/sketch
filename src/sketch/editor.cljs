(ns sketch.editor
  (:require [cljs.js :refer [empty-state eval js-eval]]
            [cljs.pprint :as pprint]
            [cljs.tools.reader :refer [read-string]]
            [devcards.core :refer-macros [defcard]]
            [re-frame.core :as re-frame]
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
;;;;; Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn inner-editor-panel [content]
  [:textarea
   (assoc events/editor-event-map
          :class "editor"
          :value content)])

(defn editor-panel []
  (let [content (re-frame/subscribe [:sketch.events.common/content])]
    (fn []
      [inner-editor-panel @content])))

(defn shape-token [shape current]
  [:div.shape-token
   (merge {:href "#"
           :on-click
           (re-frame/dispatch [:sketch.events.editor/set-current shape])}
          (when (= shape current)
            {:style {:color "red"}}))
   shape])

(defn new-shape-token []
  [:div.new-drawing
   [:a {:on-click (re-frame/dispatch [:sketch.events.editor/new-shape])}
    [:span "+"]]])

(defn shape-list [shapes current]
  (apply conj
         [:div.drawings-list
               [new-shape-token]]
         (mapv (fn [s] [shape-token s current]) shapes)))

(defn left-panel [shapes current code]
  [:div
   [:div.left [shape-list shapes current code]]
   (when current 
     [:div.right [inner-editor-panel code]])])
