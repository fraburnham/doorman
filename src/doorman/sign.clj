(ns doorman.sign
  (:import [java.net URI]
           [java.nio.file Files Paths]
           [java.security KeyFactory Signature]
           [java.security.spec PKCS8EncodedKeySpec X509EncodedKeySpec]))

;; keygen steps
;; openssl genrsa -out ~/.ssh/doorman.pem 4096
;; openssl pkcs8 -topk8 -inform pem -outform der -in ~/.ssh/doorman.pem -out ~/.ssh/doorman.der -nocrypt ; private key
;; openssl rsa -in ~/.ssh/doorman.pem -pubout -outform der -out ~/.ssh/doorman.pub.der ; public key
;; java likes the der format
;; TODO: make doorman generate pub and priv keys when needed

;; ONLY SUPPORTING RSA

(defn read-key
  [path]
  (as-> (URI. (format "file://%s" path)) *
    (Paths/get *)
    (Files/readAllBytes *)))

(defn read-private-key
  [path]
  (as-> (read-key path) *
    (PKCS8EncodedKeySpec. *)
    (.generatePrivate (KeyFactory/getInstance "RSA") *)))

(defn read-public-key
  [path]
  (as-> (read-key path) *
    (X509EncodedKeySpec. *)
    (.generatePublic (KeyFactory/getInstance "RSA") *)))

;; https://nakkaya.com/2012/10/28/public-key-cryptography/
(defn sign
  [private-key data]
  (let [sig (doto (Signature/getInstance "SHA1withRSA")
              (.initSign private-key (java.security.SecureRandom.))
              (.update data))]
    (.sign sig)))

(defn verify
  [public-key data signuture]
  (let [sig (doto (Signature/getInstance "SHA1withRSA")
              (.initVerify public-key)
              (.update data))]
    (.verify sig signuture)))
