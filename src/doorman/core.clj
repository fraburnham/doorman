(ns doorman.core
  (:gen-class)
  (:require [clojure.core.async :as async]
            [clojure.edn :as edn]
            [doorman.daemon :as daemon]
            [doorman.knock :as knock]))

;; TODO: generate-keys - simplify generating the needed keys

(defn help []
  (println "doorman is a port knocking client and daemon\n")
  (println "To start a daemon (it will block in the foreground):")
  (println "\tdoorman daemon\n")
  (println "To knock:")
  (println "\tdoorman knock [host] [port] [username]\n")
  (println "Config file location can be exported as DOORMAN_CONFIG")
  (println "If DOORMAN_CONFIG isn't in the environment then the default value '~/.doorman.edn' is used")
  (println "See https://github.com/fraburnham/doorman/blob/main/doorman.sample.edn for a sample config"))

(defn daemon
  [config]
  (-> (daemon/start config)
      (:chan)
      (async/<!!)))

(defn knock
  [config args]
  (if (= (count args) 3)
    (let [host (first args)
          port (Integer/parseInt (second args))
          username (nth args 2)]
      (knock/send config host port username))
    (help)))

(defn -main
  [& args]
  (let [config (-> (or (System/getenv "DOORMAN_CONFIG")
                       (format "%s/.doorman.edn" (System/getProperty "user.home")))
                   (slurp)
                   (edn/read-string))]
    (case (first args)
      "daemon" (daemon config)
      "knock" (knock config (rest args))
      (help))))
