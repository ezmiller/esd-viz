(ns esd-viz.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [GET POST]]))

;; -------------------------
;; Components

(defn handler [response]
  (.addGraph js/nv
    (fn []
      (def thedata (.parse js/JSON response))
      (.log js/console thedata)
      (let [my-chart (.. js/nv -models lineChart
                         (width 750)
                         (height 420)
                         (yDomain (clj->js [1.5 4]))
                         (xDomain (clj->js [2002 2012]))
                         ; (useInteractiveGuideline true)
                         (pointShape "circle")
                         (pointSize 0.5)
                         (noData "Where is the data?")
                         (duration 350))
            my-data thedata]

        (.. my-chart -xAxis
            (axisLabel "Year"));
        
        (.. my-chart -yAxis
            (axisLabel "Importance of Understanding Others"))

        (.. js/d3 (select "#esd-viz svg")
            (datum my-data)
            (call my-chart))))))

(defn esdviz-component-did-mount []
  (GET "/data" {:handler handler}))

(defn esdviz []
  (reagent/create-class
    {:component-did-mount esdviz-component-did-mount
     :reagent-render
     (fn []
       [:div#esd-viz {:style {:width 750 :height 820}} [:svg]])}))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h3 "The Desire to Undersand Others"]
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
