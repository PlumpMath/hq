(ns hq.game
  (:require [quil.core :as quil]
            [hq.proc :as proc]
            [hq.components :as comps]))

(defrecord Game [procs sketch])

(defn make []
  (Game. (atom {}) (atom nil)))

(defn- draw-failed-proc [proc]
  (quil/fill 255 0 255)
  (if-let [rect (:rect proc)]
    (do
      (comps/draw-rect rect)
      (quil/stroke 0 255 255)
      (quil/line (:x rect) (:y rect) (+ (:x rect) (:w rect)) (+ (:y rect) (:h rect)))
      (quil/line (+ (:x rect) (:w rect)) (:y rect) (:x rect) (+ (:y rect) (:h rect))))
    (do
      (quil/stroke 0)
      (quil/rect 10 10 20 20))))

(defn- draw-proc [game [id proc]]
  (if (= (:status proc) :ok)
    (when-let [renderfn (:renderfn proc)]
      (try (renderfn proc)
        (catch Exception e (do
                             (proc/set-status game id :render-fail)
                             (println (str id " renderfn error: " e))))))
    (draw-failed-proc proc)))

(defn- draw [game]
  (quil/background 200)
  (doall (map (partial draw-proc game) (sort-by (fn [[id proc]] (:layer proc)) @(:procs game)))))

(defn start [game]
  (quil/defsketch Sketch
    :title "HQ"
    :setup (fn [] )
    :draw (fn [] (draw game))
    :size [512 512])
  (reset! (:sketch game) Sketch))

(defn stop [game]
  )
