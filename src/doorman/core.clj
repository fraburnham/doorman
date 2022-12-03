(ns doorman.core
  (:require [clojure.edn :as edn]))

;; keep this for the command dispatching
;; there are two major commands
;; daemon - to listen to a udp port(s) and run scripts when a _valid_ knock is heard based on an edn config
;;    daemon should send a randomized message to sign when it hears a specific datagram this will make replay impossible
;; knock - to send a knock (signing the message)
;; TODO: generate-keys - simplify generating the needed keys

;; check for config in $HOME/.doorman.edn by default

;;(System/getProperty "user.home")
;;(System/getProperty "user.name")
