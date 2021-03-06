(ns esd-viz.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [esd-viz.middleware :refer [wrap-middleware]]
            [esd-viz.essdata :as essdata]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css "https://cdnjs.cloudflare.com/ajax/libs/nvd3/1.8.4/nv.d3.css")
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(def loading-page
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.3/d3.js")
     (include-js "https://cdnjs.cloudflare.com/ajax/libs/nvd3/1.8.4/nv.d3.js")
     (include-js "/js/app.js")]))

(def load-data
  {:status 200
   :body (essdata/get-json-data)})

(defroutes routes
  (GET "/" [] loading-page)
  (GET "/about" [] loading-page)
  (GET "/data" [] load-data)
  
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
