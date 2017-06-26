(ns sketch.devcards
  (:require [cljs.pprint :as pprint]
            [devcards.core :refer-macros [defcard]]
            [re-frame.core :as re-frame]
            [sketch.components.canvas :as canvas]
            [sketch.components.editor :as editor]
            sketch.devcards.scratch
            sketch.state))

;;;; cljr hack
(def d1 sketch.state/default-db)

;;;;; Devcards entrypoint
(defn ^:export init []
  (re-frame/dispatch-sync [:init-db])
  (devcards.core/start-devcard-ui!))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;; Devcards
;;;;;
;;;;; I'm going to leave all the devcards here until that's no longer tenable.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def dummy-code
  "Placeholder code for editor cards"
  {:type :squiggle
   :segments [{:type :s :start [0 0] :end [100 334]}
              {:type :s :start [100 334] :end [110 432]}]})

(defcard main-editor
  "Textarea with code formatted via pprint.

pprint is pretty slow, so updating the code formatting in real time this way
isn't an option. Let's just leave auto formatting for a later date.'"
  (devcards.core/reagent
   [editor/inner-editor-panel dummy-code]))

;; TODO: The canvas should resize fluidly and the stuff you draw should always
;; be in the right place. I.e. Dynamic resizing and recalculation of the pointer
;; offset.
(defcard canvas-card
  "the canvas"
  (devcards.core/reagent
   [canvas/canvas-panel]))

(defcard shape-list-test
  ;; FIXME: Set cursor to a clicker hovering over the + and the shapes.
  ;; TODO: New shape inheriting from shape.
  ;; TODO: Edit history (previous versions) of shape.
  (devcards.core/reagent
   [editor/shape-list ["shape~1" "shape~2" "shape~3"] "shape~2" {:test "ode"}]))

(defcard left-panel
  (devcards.core/reagent
   [editor/left-panel ["A" "B" "C" "D"] "C" {:test "code"}]))

(defcard interactive-left-panel
  "Interactive functional left panel"
  (devcards.core/reagent
   (fn []
     (let [drawings (re-frame/subscribe [:drawings])
           current (re-frame/subscribe [:current-shape])
           code (re-frame/subscribe [:current-shape-data])]
       (fn []
         (let [shapes (keys @drawings)]
           [editor/left-panel shapes @current @code]))))))

