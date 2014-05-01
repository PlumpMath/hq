(ns hq.proc)

(defonce id (atom 0))

(defn make-id []
    (keyword (str "id" (swap! id inc))))

(defrecord Proc [status data handler renderfn])

(defn make-proc []
  (Proc. :ok {} nil nil))

(defn- ensure-proc [game id]
  (if-let [proc (-> game :procs deref id)]
    proc
    (let [new-proc (make-proc)]
      (swap! (:procs game) (fn [procs] (assoc procs id new-proc)))
      new-proc)))

(defn run [game id settings]
  (let [proc (ensure-proc game id)]
    (swap! (:procs game) assoc id (merge proc settings))))

(merge (make-proc) {:data 100})
