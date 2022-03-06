(ns app.resolvers
  (:require
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc]))

(def people-table
  ;; changed to an atom so we can update these "databases"
  (atom
    {1 {:person/id 1 :person/name "Sally" :person/age 32}
     2 {:person/id 2 :person/name "Joe" :person/age 22}
     3 {:person/id 3 :person/name "Fred" :person/age 11}
     4 {:person/id 4 :person/name "Bobby" :person/age 55}}))

(def list-table
  (atom
    {:friends {:list/id     :friends
               :list/label  "Friends"
               :list/people [1 2]}
     :enemies {:list/id     :enemies
               :list/label  "Enemies"
               :list/people [4 3]}}))

;; Given :person/id, this can generate the details of a person
#_(pc/defresolver person-resolver [env {:person/keys [id]}]
    {::pc/input  #{:person/id}
     ::pc/output [:person/name :person/age]}
    (get @people-table id))

;; Given a :list/id, this can generate a list label and the people
;; in that list (but just with their IDs)
#_(pc/defresolver list-resolver [env {:list/keys [id]}]
    {::pc/input  #{:list/id}
     ::pc/output [:list/label {:list/people [:person/id]}]}
    (when-let [list (get @list-table id)]
      (assoc list
        :list/people (mapv (fn [id] {:person/id id}) (:list/people list)))))

#_(pc/defresolver friends-resolver [env input]
    {::pc/output [{:friends [:list/id]}]}
    {:friends {:list/id :friends}})

#_(pc/defresolver enemies-resolver [env input]
    {::pc/output [{:enemies [:list/id]}]}
    {:enemies {:list/id :enemies}})

(def resolvers [])
