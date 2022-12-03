(ns doorman.knock
  (:require [doorman.message :as msg]
            [doorman.sign :as sign]
            [doorman.udp :as udp]))

(defn- index-of
  [fn coll]
  (loop [i 0
         coll coll]
    (cond (empty? coll) nil
          (fn (first coll)) i
          :else (recur (inc i) (rest coll)))))

(defn send
  [{:keys [users]} host port username]
  (let [socket (udp/socket (+ 50000 (rand-int 1000)) 5000)]
    ;; send the knock
    (udp/send socket {:host host
                      :data (msg/pack-knock)
                      :port port})
    ;; get a challenge
    (let [{:keys [data] :as packet} (udp/recv socket)
          {:keys [id uuid]} (msg/unpack-challenge data)
          ;; oof need to get the index of the one that has (= :name username)
          uid (index-of (fn [{:keys [name] :as user}] (= name username)) users)
          privkey (sign/read-private-key (get-in users [uid :privkey]))]
      ;; sign and respond
      (udp/send socket (assoc packet :data (msg/pack-response id uid (sign/sign privkey (.getBytes uuid)))))
      ;; TODO: check the ack
      ;; close socket
      (udp/close socket))))
