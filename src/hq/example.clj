(ns hq.example
  (require [hq.game :as game]
           [hq.proc :as proc]
           [quil.core :as quil]))

(def g (game/make))

(game/start g)

(defn simple-draw [proc]
  (quil/no-stroke)
  (quil/fill 60 180 200)
  (quil/rect (:x proc) (:y proc) (:w proc) (:h proc)))

(proc/run g [:a 1] {:renderfn #'simple-draw
                    :x 120 :y 130 :w 30 :h 30})

(doseq [i (range 5)]
  (proc/run g [:ent i] {:x 50 :y (+ 50 (* i 60)) :w 30 :h 30 :renderfn #'simple-draw}))

(doall (map (partial proc/kill g) (proc/get-ids g :a)))

(proc/get-ids g)
(count (proc/get-ids g))

(proc/get-proc g [:ent 0])
