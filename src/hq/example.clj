(ns hq.example
  (:require [hq.game :as game]
            [hq.proc :as proc]
            [hq.components :as comps]
            [quil.core :as quil]))

(def g (game/make))

(game/start g)

(defn simple-draw [proc]
  (quil/no-stroke)
  (if-let [color (:color proc)]
    (comps/set-color color)
    (quil/fill 60 180 200))
  (comps/draw-rect (:rect proc)))

(proc/run g [:a 1] {:renderfn #'simple-draw
                    :rect (comps/rect 100 50 50 50)
                    :color (comps/color 250 100 40)
                    :layer 1})

(proc/run g [:a 2] {:renderfn #'simple-draw
                    :rect (comps/rect 140 220 130 130)
                    :color (comps/color 0 200 130)
                    :layer 1})

(proc/run g [:bg 0] {:layer 0
                     :renderfn (fn [_] (quil/background 50 20 25))})

(doseq [i (range 5)]
  (proc/run g [:ent i]
            {:rect (comps/rect 60 (+ 50 (* i 60)) 150 30)
             :renderfn #'simple-draw
             :layer 1}))

;(doall (map (partial proc/kill g) (proc/get-ids g :ent)))

(proc/get-ids g)



