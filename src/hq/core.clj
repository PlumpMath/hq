(ns hq.core
  (:gen-class)
  (:require [quil.core :refer :all]
            [clojure.core.async :as async :refer [<! >! <!! >!! timeout chan alt! alts!! go]]
            [clojure.core.match :refer [match]]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (println "Hello, World!"))
