(ns doorman.message
  (:import [java.nio ByteBuffer]))

(def uuid-byte-count 36)
(def long-byte-count 8)
(def int-byte-count 4)
(def sig-byte-count 512)

(defn pack-knock []
  (.getBytes "knock"))

(defn is-knock?
  [buf]
  (let [data-bytes (.array buf)
        knock-msg (pack-knock)]
    (every? #(= (aget data-bytes %) (aget knock-msg %)) (range (alength knock-msg)))))

(defn pack-challenge
  [id uuid]
  (-> (ByteBuffer/allocate (+ long-byte-count
                              uuid-byte-count))
      (.putLong id)
      (.put (.getBytes uuid))))

(defn unpack-challenge
  [buf]
  (let [buf (.position buf 0)
        id (.getLong buf)
        uuid (byte-array uuid-byte-count)
        _ (.get buf uuid)]
    {:id id
     :uuid (String. uuid)}))

(defn pack-response
  [id uid response]
  (-> (ByteBuffer/allocate (+ 4
                              long-byte-count
                              1
                              sig-byte-count))
      (.put (.getBytes "resp"))
      (.putLong id)
      (.put (byte uid))
      (.put response)))

(defn unpack-response
  [buf]
  (let [buf (.position buf 4) ; skip past `resp` preamble
        id (.getLong buf)
        uid (.get buf)
        response (byte-array sig-byte-count)
        _ (.get buf response)]
    {:id id
     :uid (int uid)
     :response response}))

(defn is-response?
  [buf]
  (let [data-bytes (.array buf)
        resp-preamble (.getBytes "resp")]
    (every? #(= (aget data-bytes %) (aget resp-preamble %)) (range (alength resp-preamble)))))
