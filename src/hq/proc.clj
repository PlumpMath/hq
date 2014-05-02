(ns hq.proc)

(defrecord Proc [status layer handler renderfn])

(defn make-proc []
  (Proc. :ok 0 nil nil))

(defn get-proc [game id]
  (get @(:procs game) id))

(defn ids
  "Get the id:s of all procs or all procs in <group>."
  ([game]
   (keys (deref (:procs game))))
  ([game group]
   (let [matcher (fn [[[g number] proc]] (= g group))]
     (keys (filter matcher @(:procs game))))))

(defn free-id
  "Creates a free id for <group>."
  [game group]
  (let [ids (set (ids game))]
    (loop [i 0]
      (let [id [group i]]
        (if (contains? ids id)
          (recur (inc i))
          id)))))

(defn- ensure-proc [game id]
  (if-let [proc (get-proc game id)]
    proc
    (let [new-proc (make-proc)]
      (swap! (:procs game) (fn [procs] (assoc procs id new-proc)))
      new-proc)))

(defn set-status
  "Set the <status> of a proc with <id>"
  [game id status]
  (swap! (:procs game) assoc-in [id :status] status))

(defn set-status*
  "Set the <status of all procs in <group>."
  [game group status]
  (doseq [id (ids game group)]
    (set-status game id status)))

(defn run
  "Creates/finds a proc with a specific id and restarts it, setting its status to :ok.
   The <id> must follow the pattern [group index]. The group is a keyword and the index is an integer.
   If the index of the group is negative a unique and free index will be chosen.
   The <settings> map is merged into the proc, replacing any existing entries."
  [game id settings]
  (let [[group index] id
        id (if (neg? index) (free-id game group) id)
        proc (ensure-proc game id)]
    (swap! (:procs game) assoc id (merge proc settings))
    (set-status game id :ok)))

(defn kill
  "Stops a proc with <id>."
  [game id]
  (if-let [proc (get-proc game id)]
    (swap! (:procs game) (fn [procs] (dissoc procs id)))
    (println "Proc" id "not found.")))

(defn kill*
  "Stop all procs in <group>."
  [game group]
  (doseq [id (ids game group)]
    (kill game id)))

(defn remove-key
  "Remove a key from a proc with <id>."
  [game id key]
  (if-let [proc (get-proc game id)]
    (swap! (:procs game) (fn [procs] (update-in procs [id] dissoc key)))
    (println "Proc" id "not found.")))

(defn remove-key*
  "Remove a key from all procs in <group>."
  [game group key]
  (doseq [id (ids game group)]
    (remove-key game id key)))

