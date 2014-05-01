(ns hq.game
  (require [quil.core :as quil]))

(defrecord Game [procs sketch])

(defn make []
  (Game. (atom {}) (atom nil)))

(defn draw-proc [proc]
  proc
  (when-let [renderfn (:renderfn proc)]
    (renderfn (:data proc))))

(defn draw [game]
  (quil/background 200)
  (doall (map draw-proc (vals @(:procs game)))))

(defn start [game]
  (quil/defsketch Sketch
    :title "HQ"
    :setup (fn [] )
    :draw (fn [] (draw game))
    :size [512 512])
  (reset! (:sketch game) Sketch))

(defn stop [game]
  )

(defn procs [game]
  (keys (deref (:procs game))))

(defn proc-inspect [game id]
  (id (deref (:procs game))))
