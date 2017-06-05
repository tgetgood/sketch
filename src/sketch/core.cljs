(ns sketch.core
  (:require [cljs.core.async :refer [<!]]
            [cljs.pprint :as pp]
            cljsjs.codemirror
            cljsjs.codemirror.addon.fold.comment-fold
            cljsjs.codemirror.addon.fold.brace-fold
            cljsjs.codemirror.addon.fold.foldcode
            cljsjs.codemirror.addon.fold.foldgutter
            cljsjs.codemirror.mode.clojure
            cljsjs.codemirror.mode.javascript
            cljsjs.codemirror.keymap.sublime
            [sketch.handlers :as handlers :refer [init!]]
            [sketch.util :as u :refer [canvas ctx current-path pp]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

;;;;; Paren Soup

(def editor-div (.getElementById js/document "left"))

(defonce editor
  (js/CodeMirror
   editor-div
   (clj->js
    {"value" ""
     "mode" "clojure"
     "readOnly" false
     "lineNumbers" true
     "foldGutter" true
     "gutters" ["CodeMirror-linenumbers" "CodeMirror-foldgutter"]})))


;;;;; Drawing

(defn np! []
  (reset! current-path [::lines []]))

;; (set! (.-lineWidth ctx) 0.1)

(defmulti draw* :type)

(defmethod draw* ::bezier
  [[_ {[c1x c1y] ::c1
       [c2x c2y] ::c2
       [e1x e1y] ::e1
       [e2x e2y] ::e2}]]
  (.moveTo ctx e1x e1y)
  (.bezierCurveTo ctx c1x c1y c2x c2y e2x e2y))

(defmethod draw* :s
  [{[x1 y1] :start [x2 y2] :end}]
  (.moveTo ctx x1 y1)
  (.lineTo ctx x2 y2))

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

(defn draw! [path]
  ;; TODO: Save old style
  ;; TODO: Set path style
  (.beginPath ctx)
  (draw* path)
  (.stroke ctx)
  (.closePath ctx))

;; canvas <=> ds <=> string code buffer <=> code editor
;; The string code buffer is important because we want to keep non-functional
;; code while the user works on it, but we don't want to kill the UI by trying
;; to read it.
;;
;; TODO: Look at how both paren-soup and figwheel manage this.

(defonce draw-state (atom {:type :squiggle :segments []}))

(go-loop []
  (when-let [c (<! handlers/shape-chan)]
    (swap! draw-state update :segments conj c)
    (recur))) 

(defn update-editor! []
  (.setValue (.getDoc editor) (with-out-str (pp/pprint @draw-state)))
  (.setOption editor "readOnly"))

(defn update-canvas! []
  (u/clear!)
  (draw! @draw-state))

(let [last-state (atom nil)]
  (defn animate! [t]
    (when (not= @last-state @draw-state)
      (update-canvas!)
      ;; (update-editor!)
      (reset! last-state @draw-state))
    (js/window.requestAnimationFrame
     animate!)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Page init
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce started
  (let [stop! (atom (init! canvas))]
    (js/window.requestAnimationFrame animate!)
    (.log js/console "Restarting hadlers.")
    (defn on-js-reload []
      (@stop!)
      (reset! stop! (init! canvas)))))

(defonce canvas-init
  ;; TODO: Why the hell does the canvas get stretched if you try to set its size
  ;; via CSS?
  (u/set-canvas!))

