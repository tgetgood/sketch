(ns sketch.core
  (:require [cljs.core.async :refer [<!]]
            [cljs.pprint :as pp]
            [paren-soup.core :as ps]
            [sketch.handlers :as handlers :refer [init!]]
            [sketch.util :as u :refer [canvas ctx current-path dts pp]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

;;;;; Paren Soup

(def editor-div (.getElementById js/document "editor"))


(def editor
  (ps/init editor-div (clj->js {})))

;;;;; Drawing

(defn np! []
  (reset! current-path [::lines []]))

;; (set! (.-lineWidth ctx) 0.1)

(defmulti draw* dts)

(defmethod draw* ::bezier
  [[_ {[c1x c1y] ::c1
       [c2x c2y] ::c2
       [e1x e1y] ::e1
       [e2x e2y] ::e2}]]
  (.moveTo ctx e1x e1y)
  (.bezierCurveTo ctx c1x c1y c2x c2y e2x e2y))

(defmethod draw* ::line
  [[_ [[x1 y1] [x2 y2]]]]
  (.moveTo ctx x1 y1)
  (.lineTo ctx x2 y2))

(defmethod draw* ::union
  [[_ data]]
  (doall (map draw* data)))

(defmethod draw* ::lines
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

(go-loop []
  (when-let [c (<! handlers/shape-chan)]
    (ps/append-text!
     editor
     (str "\n\n(def shape \n"
          (with-out-str (pp/pprint c))
          ")"))
    (ps/refresh-after-cut-paste! editor)
    (recur))) 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Page init
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce started
  (let [stop! (atom (init! canvas))]
    (.log js/console "Restarting hadlers.")
    (defn on-js-reload []
      (@stop!)
      (reset! stop! (init! canvas)))))

(defonce canvas-init
  ;; TODO: Why the hell does the canvas get stretched if you try to set its size
  ;; via CSS?
  (u/set-canvas!))

