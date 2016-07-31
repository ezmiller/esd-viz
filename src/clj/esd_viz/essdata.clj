(ns esd-viz.essdata
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(def data (lazy-seq))

(def col-headers
  ["cntry"
   "cname"
   "cedition"
   "cproddat"
   "cseqno"
   "name"
   "essround"
   "edition"
   "idno"
   "dweight"
   "pspwght"
   "pweight"
   "ipudrst"])

(defn import-csv
  "Opens the specified file and reads the data."
  [fname]
  (into () (with-open [in-file (io/reader fname)]
    (doall
      (csv/read-csv in-file)))))

(defn data-not-loaded []
  (= (count data) 0))

(defn load-data []
  (def data (import-csv "data/output5788607573688187329/ESS1-6e01_1_F1.csv")))

(defn get-col-names
  "Creates a map between column names and the column number."
  []
  (zipmap (map (fn [v] (keyword v)) col-headers) (range)))

(defn get-cell-val
  "Gets the value in the specified cell."
  [row col-key]
  (def cols (get-col-names))
  (nth (nth data row) (get cols col-key)))

(defn get-col-as-vec
  [col-key]
  (mapv #(get-cell-val % col-key) (range (count data))))

(defn get-col-as-vec-by-ctry
  [col-key ctry]
  (def filtered
    (filterv #(= ctry (nth % 0)) data))
  (mapv #(get-cell-val % col-key) (range (count filtered))))

(defn clean-row
  [d]
  (filter (fn [x] (= true (not-any? #(= x %) '("" 7 8 9)))) d))

(defn get-col-avg
  [col-key]
  (def cols (get-col-names))
  (def row (mapv #(Integer/parseInt %) (clean-row (get-col-as-vec :ipudrst))))
  (float (/ (reduce + row) (count row))))

(defn get-col-avg-by-ctry
  [col-key ctry]
  (def row (mapv #(Integer/parseInt %) (clean-row (get-col-as-vec-by-ctry col-key ctry))))
  (float (/ (reduce + row) (count row))))

(defn get-json-data []
  (if (= (count data) 0)
    (def data (import-csv "data/output5788607573688187329/ESS1-6e01_1_F1.csv")))
    {:data (reduce + )})

(defn getit []
  (import-csv "data/output5788607573688187329/ESS1-6e01_1_F1.csv"))
