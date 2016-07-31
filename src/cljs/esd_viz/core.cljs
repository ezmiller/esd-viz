(ns esd-viz.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; -------------------------
;; Components

(defn esdviz-component-did-mount []
  (.addGraph js/nv
    (fn []
      (let [my-chart (.. js/nv -models discreteBarChart
                         (noData "Where is the data?")
                         (showValues true)
                         (duration 350))
            my-data [{:x "Label A" :y 29}
                     {:x "Label B" :y 4}
                     {:x "Label C" :y 10}
                     {:x "Label D" :y 18}]]
        (.. js/d3 (select "#esd-viz svg")
            (datum (clj->js [{:values my-data}]))
            (call my-chart))))))

(defn esdviz []
  (reagent/create-class
    {:component-did-mount esdviz-component-did-mount
     :reagent-render
     (fn []
       [:div#esd-viz {:style {:width 750 :heigth 420}} [:svg]])}))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "European Social Data Visualization"]
   [esdviz]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
