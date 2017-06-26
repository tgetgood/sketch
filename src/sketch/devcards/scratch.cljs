(ns sketch.devcards.scratch
  (:require [devcards.core :refer-macros [defcard]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [sketch.components.css :as css]
            [sketch.components.editor :as editor]
            [sketch.events.editor :as events]))

(defn valc [s e]
  (let [v (-> e .-target .-value)]
    (reset! s v)))

(defcard tracked-textarea
  "The value of a textarea should be tracked in the reagent app state. Changes
  to the textarea should be reflected in the app state and changes to the app
  state from other parts of the program should be reflected in the textarea.

I'm not a proponent of two way data binding, but that's the phenotype we're
looking for here."
  (devcards.core/reagent
   (let [state (reagent/atom "text")]
     (fn []
       [:div 
        [:textarea {:on-change (partial valc state)
                    :value @state}]
        [css/row
         [css/button {:on-click #(reset! state "")} "clear!"]]]))))

(defcard re-frame-textarea
  "Same thing but using re-frame app state"
  (devcards.core/reagent
   [editor/editor-panel]))
