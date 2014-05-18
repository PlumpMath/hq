(ns hq.proc
  (:require [clojure.core.async :as async]))

;; Concepts
;; ========
;;
;; - Proc -
;; Used to model a subsystem or object. Can be in an 'ok' status or different kinds of fail states.
;; The 'layer' key should be set to an integer and is used for determening the order of rendering,
;; lowest first.
;;
;; - Id -
;; An id is used to keep track of procs and is always a vector with two parts, 'group' and 'index'.
;; An example is [:ai-manager 0] or [:enemy 10].
;; When a new proc is created the index (the integer part) can be specified or automatically
;; generated by setting it to -1.
;;
;; - Handler -
;; The handler is function that is run every time a proc receives a message.
;; You supply your own handler function for every proc you create.
;; A handler function should take two arguments, <proc> and <message>.
;; It should return a (potentially) modified proc.

(def editor {:selected false})

;; Proc members
;;
;; status - :ok :handler-fail :handler-exception :render-fail
;; id - [<group> <index>]
;; layer - integer describing the render layer
;; editor - map of editor related settings
;; channel - the core.async channel where this proc receives messages
;; handler - the function running in an async loop, handling messages and mutating the state of the proc
;; renderfn - a function that runs in the main thread of the app, used for drawing stuff

(defrecord Proc [status id layer editor channel handler renderfn])

(defn get-proc
  "Get the proc with <id>."
  [game id]
  (get @(:procs game) id))

(defn set-status
  "Set the <status> of a proc with <id>"
  [game id status]
  (swap! (:procs game) assoc-in [id :status] status))

(defn safe-update [handler old-proc msg]
  (let [new-proc (handler old-proc msg)]
      (if (or
             (nil? new-proc)
             (not (map? new-proc)))
        (do
          (println "Safe guard for proc" (:id old-proc) "failed. State:" new-proc "Message:" msg)
          (assoc old-proc :status :handler-fail))
        new-proc)))

(defn default-handler [proc msg]
  proc)

(defn- internal-kill-proc [game id]
  (swap! (:procs game) (fn [procs] (dissoc procs id))))

(defn- start-go-loop [game id channel]
  (async/go
   (println "Proc" id "started.")
   (flush)
   (loop []
     (let [[message c] (async/alts! [channel])
           proc (get-proc game id)
           status (:status proc)]
       (when proc
         (if (= :ok status)
           (let [handler (or (:handler proc) default-handler)]
             (try
               (do
                 (swap! (:procs game) update-in [id] #(safe-update handler % message)))
               (catch Exception e
                 (println (str "Exception " e " when processing message " message " in proc " id))
                 (set-status game id :handler-exception)))
             (recur))
           (recur) ; Status is not :ok, ignore message and recur
           ))))
   (println "Proc" id "stopped.")
   (internal-kill-proc game id)))

(defn ids
  "Get the id:s of all procs or all procs in <group>."
  ([game]
   (keys (deref (:procs game))))
  ([game group]
   (let [matcher (fn [[[g number] proc]] (= g group))]
     (keys (filter matcher @(:procs game))))))

(defn set-status*
  "Set the <status of all procs in <group>."
  [game group status]
  (doseq [id (ids game group)]
    (set-status game id status)))

(defn message [game id msg]
  (let [proc (get-proc game id)
        channel (:channel proc)]
    (cond
     (nil? proc) (throw (Exception. (str "Proc " id " not found, can't send message.")))
     (nil? channel) (throw (Exception. (str "Channel for proc " id " not found.")))
     :else (async/go
            (async/>! channel msg)))))

(defn message* [game group msg]
  (doseq [id (ids game group)]
     (message game id msg)))

(defn message-all [game msg]
  (doseq [id (ids game)]
     (message game id msg)))

(defn- make-proc [game id]
  (let [channel (async/chan)
        proc (Proc. :ok id 0 editor channel nil nil)]
    (start-go-loop game id channel)
    proc))

(defn- free-id
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
    (let [new-proc (make-proc game id)]
      (swap! (:procs game) (fn [procs] (assoc procs id new-proc)))
      new-proc)))

(defn run
  "Creates/finds a proc with a specific id and restarts it, setting its status to :ok.
   The <id> must follow the pattern [group index].
   The group is a keyword and the index is an integer.
   If the index of the group is negative a unique and free index will be chosen.
   The <settings> map is merged into the proc, replacing any existing entries."
  [game id settings]
  (let [[group index] id
        id (if (neg? index) (free-id game group) id)
        proc (ensure-proc game id)]
    (swap! (:procs game) assoc id (merge proc settings))
    (set-status game id :ok)))

(defn all
  "Get all the procs in a <game>."
  [game]
  (vals @(:procs game)))

(defn run*
  ([game]
   (doseq [id (ids game)]
     (run game id {})))
  ([game group]
   (run* game group {}))
  ([game group settings]
   (doseq [id (ids game group)]
     (run game id settings))))

(defn kill
  "Stops a proc with <id>."
  [game id]
  (if-let [proc (get-proc game id)]
    (let [channel (:channel proc)]
      (swap! (:procs game) (fn [procs] (dissoc procs id)))
      (async/go
       (async/>! channel :killed)))
    (println "Proc" id "not found.")))

(defn kill*
  "Stop all procs or all procs in <group>."
  ([game]
   (doseq [id (ids game)]
     (kill game id)))
  ([game group]
   (doseq [id (ids game group)]
     (kill game id))))

(defn kill-selected
  "Stop all selected procs."
  [game]
  (let [selected-ids (keys (filter (fn [[id proc]] (-> proc :editor :selected)) @(:procs game)))]
    (doseq [id selected-ids]
      (kill game id))))

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

(defn set-selected
  "Turn on/off editor selection for a proc with <id>."
  [game id on]
  (swap! (:procs game) (fn [procs] (assoc-in procs [id :editor :selected] on))))

(defn flip-selected
  "Flip (invert) editor selection for a proc with <id>."
  [game id]
  (swap! (:procs game) (fn [procs] (update-in procs [id :editor :selected] not))))

(defn set-selected*
  "Turn on/off editor selection for all procs in <group>."
  [game on]
  (doseq [id (ids game)]
     (set-selected game id on)))

(defn hit? [[px py] proc]
  (when-let [rect (:rect proc)]
    (let [x (:x rect)
          y (:y rect)
          w (:w rect)
          h (:h rect)]
      (and (< x px (+ x w))
           (< y py (+ y h))))))

(defn ids-at-pos [game pos]
  (map :id (filter (partial hit? pos) (all game))))
