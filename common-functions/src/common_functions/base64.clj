(ns common-functions.base64
  (:import (java.util Base64)))

(defn str->base64bytes [to-encode]
  "Encode String to Base64."
  (.encode (Base64/getEncoder) (.getBytes to-encode)))

(defn str->base64str [to-encode]
  "Encode String to Base64 (String)."
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(defn base64->str [to-decode]
  "Decode Base64 (byte[] or String) to String."
  (String. (.decode (Base64/getDecoder) to-decode)))