(ns dandelion.core
  (:require [clojure.data.json :as json])
  (:import [com.amazon.ion.system
            IonBinaryWriterBuilder
            IonReaderBuilder
            IonSystemBuilder
            IonTextWriterBuilder]
           java.io.ByteArrayOutputStream))

(defn ion->json
  "Transforms an IonValue to a JSON value serialised as String."
  [ion-value]
  (let [sb (StringBuilder.)
        writer (.build (.withJsonDowngrade (IonTextWriterBuilder/json)) sb)
        reader (.build (IonReaderBuilder/standard) ion-value)]
    (.writeValues writer reader)
    (str sb)))

(defn json->ion
  "Transforms a JSON value serialised as String to an immutable IonValue."
  [^String json-value]
  (let [system (.build (IonSystemBuilder/standard))
        value  (.singleValue system json-value)]
    (.makeReadOnly value)
    value))

(defn clj->ion-binary
  "Transforms a Clojure value to a byte array of Ion binary"
  [clj-value]
  (with-open [os (ByteArrayOutputStream.)
              writer (.build (IonBinaryWriterBuilder/standard) os)
              reader (.build (IonReaderBuilder/standard) (json/write-str clj-value))]
    (.writeValues writer reader)
    (.finish writer)
    (.toByteArray os)))

(defn clj->ion
  "Transforms a Clojure value, usually a Map to an immutable IonValue."
  [clj-value]
  (-> clj-value
      json/write-str
      json->ion))

(defn ion->clj
  "Transforms a IonValue to a Clojure value"
  [ion-value]
  (-> ion-value
      ion->json
      json/read-str))
