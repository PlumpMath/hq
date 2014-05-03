(ns hq.example
  (:require [hq.game :as game]
            [hq.proc :as proc]
            [hq.components :as comps]
            [quil.core :as quil]))

(def g (game/make))
(game/show g)

(defn simple-draw [proc]
  (quil/no-stroke)
  (comps/set-color (:color proc))
  (comps/draw-rect (:rect proc)))

(proc/run g [:bg 0] {:layer 0 :renderfn (fn [_] (quil/background 50 20 25))})

(defn little-rect [y]
  {:rect (comps/rect 60 y 200 50)
   :renderfn #'simple-draw
   :color (comps/color 10 150 y)
   :layer 1})

(doseq [i (range 7)]
  (proc/run g [:ent i] (little-rect (+ 20 (* i 70)))))

;(proc/kill* g :ent)
(proc/get-proc g [:ent 0])
(proc/all g)
(proc/ids g)

;(proc/flip-selected g [:ent 0])
