(ns server
  (:require [hikari-cp.core :as hikari]
            [honeyeql.db :as heql-db]
            [io.pedestal.http :as server]
            [com.walmartlabs.lacinia.pedestal :as lacinia-pedestal]
            [graphqlize.lacinia.core :as l]))

(def db-spec (hikari/make-datasource {:adapter           "postgresql"
                                      :database-name     "sakila"
                                      :server-name       "localhost"
                                      :port-number       5432
                                      :maximum-pool-size 1
                                      :username          "postgres"
                                      :password          "postgres"}))

(def heql-db-adapter (heql-db/initialize db-spec))

(def lacinia-schema (l/schema heql-db-adapter))

(def service (lacinia-pedestal/service-map lacinia-schema {:graphiql true
                                                           :port     8080}))

(defonce runnable-service (server/create-server service))

(.addShutdownHook (Runtime/getRuntime) (Thread. (fn []
                                                  (server/stop runnable-service)
                                                  (.close db-spec))))

(defn -main []
  (server/start runnable-service))