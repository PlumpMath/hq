(ns hq.example
  (:require [hq.game :as game]
            [hq.proc :as proc]
            [hq.components :as comps]
            [quil.core :as quil]))

(def g (game/make))
(game/start g)
(proc/ids g)

(defn simple-draw [proc]
  (quil/no-stroke)
  (if-let [color (:color proc)]
    (comps/set-color color)
    (quil/fill 60 180 200))
  (comps/draw-rect (:rect proc)))

(proc/run g [:bg 0] {:layer 0
                     :renderfn (fn [_] (quil/background 50 20 25))})

(defn little-rect [y]
  {:rect (comps/rect 60 y 50 50)
   :renderfn #'simple-draw
   :color (comps/color 10 150 y)
   :layer 1})

(doseq [i (range 7)]
  (proc/run g [:ent i] (little-rect (+ 20 (* i 70)))))

;(proc/run g [:master 0] {:renderfn (fn [_] (quil/background 10 10 10))})

(proc/kill* g :ent)

(proc/remove-key g [:ent 1] :color)


