(ns doorman.udp
  (:import [java.net DatagramPacket DatagramSocket InetAddress]
           [java.nio ByteBuffer]))

(defprotocol Transport
  (send [this packet] "Send packet over socket")
  (recv [this] "Receive a message from socket")
  (close [this] "Close a socket"))

;; TODO: use spec to define a packet dict
;; TODO: log all packets!

(defrecord Socket [socket]
  Transport

  (send [_ {:keys [data host port]}]
    (let [data (if (bytes? data)
                data
                (.array data))
          host (if (instance? InetAddress host)
                 host
                 (InetAddress/getByName host))]
    (.send socket (DatagramPacket. data (alength data) host port))))

  (recv [_]
    (let [max-bytes 1024
          buffer (ByteBuffer/allocate max-bytes)
          packet (DatagramPacket. (.array buffer) max-bytes)]
      (.receive socket packet)
      {:host (.getAddress packet)
       :data buffer
       :port (.getPort packet)}))

  (close [_]
    (.close socket)))

(defn socket
  ([port]
   (Socket. (DatagramSocket. port)))
  ([port timeout-ms]
   (Socket. (doto (DatagramSocket. port)
              (.setSoTimeout timeout-ms)))))
