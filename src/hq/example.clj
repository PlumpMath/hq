(ns hq.example
  (require [hq.game :as game]
           [hq.proc :as proc]
           [quil.core :as quil]))

(def g (game/make))

(game/start g)

(proc/run g :a
          {:renderfn (fn [data] (quil/rect 20 10 150 130))})

(proc/run g :b {:data [1 2 3]
                :renderfn (fn [data] (quil/rect 230 120 150 130))})

(game/procs g)

(game/proc-inspect g :a)
(game/proc-inspect g :b)

g
