(ns tictac
  (:require
   [hq.proc :as proc]
   [hq.game :as game]
   [hq.components :as comps]
   [quil.core :as quil]
   [clojure.core.async :as async]
   [clojure.core.match :refer [match]]))

(def g (game/make "WAND" [512 512]))

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

(defn steer [proc]
  (let [{:keys [x]} (:rect proc)]
    (update-in proc [:rect :x] #(+ (:speed proc) %))))

(defn player-handler [proc msg]
  (match msg
         :down (update-in proc [:rect :y] #(+ 50 %))
         :tick (-> proc move-and-wrap steer)
         :crash nil
         [:mouse-pressed [x y]] (assoc-in proc [:rect :y] y)
         [:key-pressed k] (case k
                            68 (assoc proc :speed 2)
                            65 (assoc proc :speed -2)
                            proc)
         [:key-released k] (case k
                             68 (assoc proc :speed 0)
                             65 (assoc proc :speed 0)
                             proc)
         :bang (throw (Exception. "BANG!"))
         :else proc))

(doseq [i (range 7)]
  (proc/run g [:enemy i]
          {:layer 1
           :speed 0
           :color (comps/color (+ 100 (* 10 i)) 100 (+ 200 (* 30 i -1)))
           :rect (comps/rect (+ 50 (* i 60)) 100 50 50)
           :handler #'player-handler
           :renderfn comps/color-circle}))

; (proc/run* g)

; (proc/message g [:enemy 0] :crash)
; (proc/message g [:enemy 0] :bang)
; (proc/message-all g :tick)

; (proc/kill* g)
