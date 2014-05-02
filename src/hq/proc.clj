(ns hq.proc)

(defrecord Proc [status layer handler renderfn])

(defn make-proc []
  (Proc. :ok 0 nil nil))

(defn get-proc [game id]
  (get @(:procs game) id))

(defn- ensure-proc [game id]
  (if-let [proc (get-proc game id)]
    proc
    (let [new-proc (make-proc)]
      (swap! (:procs game) (fn [procs] (assoc procs id new-proc)))
      new-proc)))

(defn set-status [game id status]
  (swap! (:procs game) assoc-in [id :status] status))

(defn run [game id settings]
  (let [proc (ensure-proc game id)]
    (swap! (:procs game) assoc id (merge proc settings))
    (set-status game id :ok)))

(defn kill [game id]
  (if-let [proc (get-proc game id)]
    (swap! (:procs game) (fn [procs] (dissoc procs id)))
    (println "Proc" id "not found.")))

(defn get-ids
  ([game]
   (keys (deref (:procs game))))
  ([game key-word]
   (let [matcher (fn [[[k number] proc]] (= k key-word))]
     (keys (filter matcher @(:procs game))))))

(defn inspect [game id]
  (id (deref (:procs game))))
