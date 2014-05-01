(ns hq.core
  (:gen-class)
  (:require [quil.core :refer :all]
            [clojure.core.async :as async :refer [<! >! <!! >!! timeout chan alt! alts!! go]]
            [clojure.core.match :refer [match]]))

(defn -main [& args])

;(-main)
