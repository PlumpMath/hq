(ns hq.game
  (:require [quil.core :as quil]
            [hq.proc :as proc]
            [hq.components :as comps])
  (:import [java.awt.event KeyEvent]))

(defrecord Game [procs sketch edit])

(defn make []
  (Game. (atom {}) (atom nil) (atom false)))

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
    (draw-failed-proc proc))
  (when @(:edit game)
    (quil/fill 255)
    (when-let [rect (:rect proc)]
      (quil/text (str id) (+ (:x rect) 5) (+ (:y rect) 15))
      (when (-> proc :editor :selected)
        (quil/stroke 0 255 255)
        (quil/no-fill)
        (quil/stroke-weight 5)
        (comps/draw-rect rect)))))

(defn- draw [game]
  (quil/background 200)
  (doall (map (partial draw-proc game) (sort-by (fn [[id proc]] (:layer proc)) @(:procs game))))
  (when @(:edit game)
    (quil/stroke 255 0 0)
    (quil/no-fill)
    (quil/rect 0 0 (dec (quil/width)) (dec (quil/height)))))

(defn- swap-edit-mode [game]
  (swap! (:edit game) #(not %)))

(defn- kill-selected [game]
  (let [selected-ids (keys (filter (fn [[id proc]] (-> proc :editor :selected)) @(:procs game)))]
    (doseq [id selected-ids]
      (proc/kill game id))))

(defn- key-pressed [game]
  (println "Key pressed:" (quil/key-code))
  (case (quil/key-code)
    192 (swap-edit-mode game)
    :no-match)
  (when @(:edit game)
    (case (quil/key-code)
      8 (kill-selected game)
      :no-match)))

(defn get-mouse-pos []
  [(quil/mouse-x) (quil/mouse-y)])

(defn hit? [[px py] [id proc]]
  (when-let [rect (:rect proc)]
    (let [x (:x rect)
          y (:y rect)
          w (:w rect)
          h (:h rect)]
      (and (< x px (+ x w))
           (< y py (+ y h))))))

(defn procs-at-pos [game pos]
  (keys (filter (partial hit? pos) @(:procs game))))

(defn- mouse-clicked [game]
  ;(println "Mouse clicked:" (quil/mouse-x) (quil/mouse-y))
  (when @(:edit game)
    (proc/set-selected* game false)
    (when-let [selection-id (first (procs-at-pos game (get-mouse-pos)))]
      (println "Proc at mouse click:" selection-id)
      (proc/set-selected game selection-id true))))

(defn start [game]
  (quil/defsketch Sketch
    :title "HQ"
    :setup (fn [] )
    :draw (fn [] (draw game))
    :key-pressed (fn [] (key-pressed game))
    :mouse-clicked (fn [] (mouse-clicked game))
    :size [512 512])
  (reset! (:sketch game) Sketch))

(defn stop [game]
  )
