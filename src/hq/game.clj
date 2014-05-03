(ns hq.game
  (:require [quil.core :as quil]
            [hq.proc :as proc]
            [hq.components :as comps])
  (:import [java.awt.event KeyEvent]))

(def wand-key 192)
(def backspace 8)
(def shift-key 16)

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

(defn- draw-proc [game proc]
  (let [id (:id proc)]
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
        (quil/fill 255 100)
        (quil/rect (:x rect) (:y rect) (+ (quil/text-width (str id)) 10) 20)
        (quil/fill 0)
        (quil/text (str id) (+ (:x rect) 5) (+ (:y rect) 15))
        (when (-> proc :editor :selected)
          (quil/stroke 0 255 255)
          (quil/no-fill)
          (quil/stroke-weight 5)
          (comps/draw-rect rect))))))

(defn- draw [game]
  (quil/background 200)
  (doall (map (partial draw-proc game) (sort-by :layer (proc/all game))))
  (when @(:edit game)
    (quil/stroke-weight 5)
    (quil/stroke 255 0 0)
    (quil/no-fill)
    (quil/rect 0 0 (dec (quil/width)) (dec (quil/height)))))

(defn- swap-edit-mode [game]
  (swap! (:edit game) #(not %)))

(defn- key-pressed [game]
  (println "Key pressed:" (quil/key-code))
  (condp = (quil/key-code)
    wand-key (swap-edit-mode game)
    :no-match)
  (when @(:edit game)
    (condp = (quil/key-code)
      backspace (proc/kill-selected game)
      :no-match)))

(defn get-mouse-pos []
  [(quil/mouse-x) (quil/mouse-y)])

(defn- mouse-pressed [game]
  (when @(:edit game)
    (if-let [selection-id (first (proc/ids-at-pos game (get-mouse-pos)))]
      (let [selected (-> (proc/get-proc game selection-id) :editor :selected)]
        (when (not (and (quil/key-pressed?) (= shift-key (quil/key-code))))
          (proc/set-selected* game false))
        (proc/set-selected game selection-id (not selected)))
      (proc/set-selected* game false))))

(defn show [game]
  (quil/sketch
    :title "HQ"
    :setup (fn [] )
    :draw (fn [] (draw game))
    :key-pressed (fn [] (key-pressed game))
    :mouse-pressed (fn [] (mouse-pressed game))
    :size [512 512])
  (reset! (:sketch game) Sketch))
