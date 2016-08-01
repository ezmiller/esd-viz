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

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn esdviz-component-did-mount []
  (GET "/data" {:handler handler
                :error-handler error-handler
                :format {:content-type "application/json"}}))

(defn esdviz []
  (reagent/create-class
    {:component-did-mount esdviz-component-did-mount
     :reagent-render
     (fn []
       [:div#esd-viz {:style {:width 750 :height 420}} [:svg]])}))

;; -------------------------
;; Views

(defn citation []
  [:p {:style {:font-size "10px" :line-height 1.2}}
      "Citation: European Social Survey Rounds 1-6. Data file edition 6.4.
       NSD - Norwegian Centre for Research Data, Norway - Data Archive and
       distributor of ESS data for ESS ERIC."])

(defn home-page []
  [:div [:h3 {:style {:margin-bottom 0}} "The Desire to Undersand Others in Europe"]
        [:h4 {:style {:margin 0}} "(By Year & By Country)"]
   [esdviz]
   [citation]])

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
