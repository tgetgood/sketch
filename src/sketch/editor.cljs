(ns sketch.editor)

(def editor (.getElementById js/document "code"))

;; canvas <=> ds <=> string code buffer <=> code editor
;; The string code buffer is important because we want to keep non-functional
;; code while the user works on it, but we don't want to kill the UI by trying
;; to read it.
;;
;; TODO: Look at how both paren-soup and figwheel manage this.

(defn code-edit-cb [{:keys [error value]}]
  (if value
    (reset! draw-state value)
    (.error js/console error)))

(defn code-edit [e]
  (let [code (.-value editor)]
    (eval (empty-state)
              (read-string code)
              {:eval js-eval
               :source-map true
               :context :expr}
              code-edit-cb)))

(defonce editor-listeners
  (do
    (.addEventListener editor "input" code-edit)
    (.addEventListener editor "onpropertychange" code-edit)))

(defn update-editor! []
  (set! (.-value editor)
        (with-out-str (pp/pprint @draw-state))))
