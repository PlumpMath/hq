(ns tictac
  (:require
   [hq.proc :as proc]
   [hq.game :as game]
   [hq.components :as comps]
   [quil.core :as quil]
   [clojure.core.async :as async]
   [clojure.core.match :refer [match]]))

(def g (game/make))
(game/show g [512 512])
;(game/stop g)

(defn nice-fill [_]
  (quil/background 70 100 125))

(proc/run g [:bg 0]
          {:layer 0
           :renderfn #'nice-fill})

(defn move-and-wrap [proc]
  (let [{:keys [y]} (:rect proc)]
    (if (> y 400)
      (assoc-in proc [:rect :y] 0)
      (update-in proc [:rect :y] #(+ 3.5 %)))))

(defn player-handler [proc msg]
  (match msg
         :down (update-in proc [:rect :y] #(+ 50 %))
         :tick (move-and-wrap proc)
         :crash nil
         [:mouse-pressed [x y]] (assoc-in proc [:rect :y] y)
         [:key-pressed k] (case k
                            68 (update-in proc [:rect :x] + 10)
                            65 (update-in proc [:rect :x] - 10)
                            proc)
         :bang (throw (Exception. "BANG!"))
         :else proc))

(doseq [i (range 7)]
  (proc/run g [:enemy i]
          {:layer 1
           :color (comps/color (+ 100 (* 10 i)) 100 100)
           :rect (comps/rect (+ 50 (* i 60)) 100 50 50)
           :handler #'player-handler
           :renderfn comps/color-circle}))

(proc/run* g)

; (proc/message g [:enemy 0] :crash)
; (proc/message g [:enemy 0] :bang)
; (proc/message-all g :tick)

;(proc/kill* g)
