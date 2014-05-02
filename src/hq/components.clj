(ns hq.components
  (:require [quil.core :as quil]))

(defn rect [x y w h]
  {:x x :y y :w w :h h})

(defn draw-rect [rect]
  (quil/rect (:x rect) (:y rect) (:w rect) (:h rect)))

(defn color [r g b]
  {:r r :g g :b b})

(defn set-color [color]
  (quil/fill (:r color) (:g color) (:b color)))
