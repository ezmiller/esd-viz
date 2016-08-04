(ns esd-viz.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.json :as json-middleware]))

(defn wrap-middleware [handler]
  (-> handler
        (wrap-defaults site-defaults)
        (json-middleware/wrap-json-body {:keywords? true})
        json-middleware/wrap-json-response))
