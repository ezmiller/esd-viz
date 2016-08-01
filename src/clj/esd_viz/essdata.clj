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

(def yr-to-rnd
  {:2014 7
   :2012 6
   :2010 5
   :2008 4
   :2006 3
   :2004 2
   :2002 1})

(defn import-csv
  "Opens the specified file and reads the data."
  [fname]
  (into () (with-open [in-file (io/reader fname)]
    (doall
      (csv/read-csv in-file)))))

(defn data-not-loaded []
  (= (count data) 0))

(defn load-data []
  (if (data-not-loaded)
    ; (def data (import-csv "data/output5788607573688187329/ESS1-6e01_1_F1.csv"))
    (def data (import-csv "data/output3248285030537870775/ESS1-6e01_1_F1.csv"))
    ))

(defn get-col-names
  "Creates a map between column names and the column number."
  []
  (zipmap (map (fn [v] (keyword v)) col-headers) (range)))

(defn clean
  [d]
  (filter (fn [x] (= true (not-any? #(= x %) '("" 7 8 9)))) d))

(defn filter-by-ctry
  [ctry d]
  (filterv #(= ctry (nth % 0)) d))

(defn filter-by-yr
  [year d]
  (filterv #(= (get yr-to-rnd (keyword year)) (Integer/parseInt (nth % 6))) d))

(defn get-cell-val
  "Gets the value in the specified cell."
  [row col-key d]
  (def cols (get-col-names))
  (nth (nth d row) (get cols col-key)))

(defn get-col-as-vec
  [col-key d]
  (mapv #(get-cell-val % col-key d) (range (count d))))

(defn get-col-avg
  [d]
  (def row (mapv #(Integer/parseInt %) (clean d)))
  (if (> (count row) 0)
    (do
      (float (/ (reduce + row) (count row))))
    nil))

(defn get-col-avg-by-ctry-by-yr
  [col-key ctry year]
  (get-col-avg (get-col-as-vec col-key (filter-by-ctry ctry (filter-by-yr year data)))))

(defn get-yr-avgs-by-ctry
  [col-key ctry years]
  (mapv #(hash-map
           :x (Integer/parseInt %)
           :y (get-col-avg-by-ctry-by-yr :ipudrst ctry %)) years))

(defn get-json-data []
  (load-data)
  (def ctrys '("GB" "ES" "DE" "FR" "HU" "FI" "PT" "SE"))
  (def years '("2002" "2004" "2006" "2008" "2010" "2012"))
  (mapv (fn [ctry]
          {:key ctry
           :values (get-yr-avgs-by-ctry :ipudrst ctry years)}) ctrys))
