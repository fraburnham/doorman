(ns doorman.daemon
  (:require [clojure.core.async :as async]
            [clojure.java.shell :as shell]
            [doorman.message :as msg]
            [doorman.sign :as sign]
            [doorman.udp :as udp])
  (:import [java.util UUID]))

(defonce challenges (atom {}))

(defn over-30s-ago
  [[k v]]
  (let [offset (* 30 1000)]
    (when (>= k (- (System/currentTimeMillis) offset)) [k v])))

(defn handle-knock
  [socket packet]
  (let [id (System/currentTimeMillis)
        uuid (.toString (UUID/randomUUID))
        challenge (msg/pack-challenge id uuid)]
    (swap! challenges assoc id uuid)
    (udp/send socket (assoc packet :data challenge))))

(defn handle-response
  [{:keys [action] :as config} socket {:keys [data] :as packet}]
  (let [{:keys [id uid response]} (msg/unpack-response data)
        pubkey (sign/read-public-key (get-in config [:users uid :pubkey]))
        data (.getBytes (@challenges id))]
    (swap! challenges dissoc id)
    (swap! challenges #(into {} (filter over-30s-ago %)))
    (if (sign/verify pubkey data response)
      (do
        ;; TODO: log the result of the action
        (case (:type action)
          :shell (println (apply shell/sh (:cmd action))))
        (udp/send socket (assoc packet :data (byte-array [0]))))
      (udp/send socket (assoc packet :data (byte-array [1]))))))

(defn start
  [{:keys [port] :as config}]
  (let [socket (udp/socket port)]
    {:socket socket
     :chan (async/go
             (loop []
               (try
                 (let [{:keys [data] :as packet} (udp/recv socket)]
                   (cond (msg/is-knock? data) (handle-knock socket packet)
                         (msg/is-response? data) (handle-response config socket packet)))
                 (catch Exception _
                   ;; TODO: Logging!
                   ))
               (recur)))}))

