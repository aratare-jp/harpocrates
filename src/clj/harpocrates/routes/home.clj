(ns harpocrates.routes.home
  (:require
    [harpocrates.layout :as layout]
    [harpocrates.db.core :as db]
    [clojure.java.io :as io]
    [harpocrates.middleware :as middleware]
    [ring.util.response]
    [ring.util.http-response :as response]))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}])
