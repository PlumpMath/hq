(ns hq.minigame
  (:require [hq.game :as game]
            [hq.proc :as proc]
            [hq.components :as comps]
            [quil.core :as quil]
            [clojure.core.async :as async]
            [clojure.core.match :refer [match]]))

;; (def g (game/make))

;; (proc/all g)

;; (game/show g)

;; (defn nice-fill [_]
;;   (quil/background 50 20 25))

;; (proc/run g [:bg 0]
;;           {:layer 0
;;            :renderfn #'nice-fill})

;; (defn player-handler [proc msg]
;;   (println "Player got message" msg)
;;   (match msg
;;          [:move x] (update-in proc [:rect :x] (partial + x))
;;          :crash nil
;;          :else proc))

;; (proc/run g [:player 0]
;;           {:layer 1
;;            :color (comps/color 150 50 100)
;;            :rect (comps/rect 100 100 50 50)
;;            :handler #'player-handler
;;            :renderfn comps/color-rect})

;; (doseq [x (range 0 11)]
;;   (proc/run g [:stuff x] {:layer 2
;;                           :color (comps/color (* x 50) 25 255)
;;                           :rect (comps/rect (* x 50) 200 40 50)
;;                           :renderfn comps/color-rect}))


;; (proc/get-proc g [:player 0])

;; (proc/message g [:player 0] [:move 20])
;; (proc/message g [:player 0] :crash)

;; (proc/all g)
