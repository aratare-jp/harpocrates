(ns shinsetsu.db.bookmark
  (:require
    [next.jdbc :as jdbc]
    [honey.sql.helpers :as helpers]
    [honey.sql :as sql]
    [taoensso.timbre :as log]
    [shinsetsu.db.db :refer [ds]]
    [shinsetsu.schema :as s]
    [malli.core :as m]
    [malli.error :as me])
  (:import [java.time Instant]
           [org.postgresql.util PSQLException]))

(defn create-bookmark
  [{:bookmark/keys [tab-id user-id] :as bookmark}]
  (if-let [err (m/explain s/bookmark-spec bookmark)]
    (throw (ex-info "Invalid bookmark" {:error-type :invalid-input :error-data (me/humanize err)}))
    (try
      (log/info "Create new bookmark in tab" tab-id "for user" user-id)
      (jdbc/execute-one! ds (-> (helpers/insert-into :bookmark)
                                (helpers/values [bookmark])
                                (helpers/returning :*)
                                (sql/format)))
      (catch PSQLException e
        (log/error e)
        (case (.getSQLState e)
          "23503" (throw (ex-info "Nonexistent tab" {:error-type :invalid-input
                                                     :error-data {:bookmark/tab-id ["nonexistent"]}}))
          (throw (ex-info "Unknown error" {:error-type :unknown} e)))))))

(defn patch-bookmark
  [{:bookmark/keys [id user-id] :as bookmark}]
  (if-let [err (m/explain s/bookmark-update-spec bookmark)]
    (throw (ex-info "Invalid bookmark" {:error-type :invalid-input :error-data (me/humanize err)}))
    (let [bookmark (assoc bookmark :bookmark/updated (Instant/now))]
      (log/info "Update bookmark" id "for user" user-id)
      (try
        (jdbc/execute-one! ds (-> (helpers/update :bookmark)
                                  (helpers/set bookmark)
                                  (helpers/where [:= :bookmark/id id] [:= :bookmark/user-id user-id])
                                  (helpers/returning :*)
                                  (sql/format)))
        (catch PSQLException e
          (log/error e)
          (case (.getSQLState e)
            "23503" (throw (ex-info "Nonexistent tab" {:error-type :invalid-input
                                                       :error-data {:bookmark/tab-id ["nonexistent"]}}))
            (throw (ex-info "Unknown error" {:error-type :unknown} e))))))))

(defn delete-bookmark
  [{:bookmark/keys [id user-id] :as bookmark}]
  (if-let [err (m/explain s/bookmark-delete-spec bookmark)]
    (throw (ex-info "Invalid bookmark" {:error-type :invalid-input :error-data (me/humanize err)}))
    (do
      (log/info "Delete bookmark" id "for user" user-id)
      (jdbc/execute-one! ds (-> (helpers/delete-from :bookmark)
                                (helpers/where [:= :bookmark/id id] [:= :bookmark/user-id user-id])
                                (helpers/returning :*)
                                (sql/format))))))

#_(defn delete-bookmarks
    [bookmarks user-id]
    (if-let [err (m/explain [:cat [:vector :uuid] :uuid] [bookmarks user-id])]
      (throw (ex-info "Invalid bookmark" {:error-type :invalid-input :error-data (me/humanize err)}))
      (let [ids (map :bookmark/id bookmarks)]
        (log/info "Delete bookmarks" ids "from user" user-id)
        (jdbc/execute! ds (-> (helpers/delete-from :bookmark)
                              (helpers/where [:in :bookmark/id bookmarks] [:= :bookmark/user-id user-id])
                              (helpers/returning :*)
                              (sql/format))))))

(defn fetch-bookmark
  [{bookmark-id :bookmark/id user-id :user/id :as input}]
  (if-let [err (m/explain [:map {:closed true} [:bookmark/id :uuid] [:user/id :uuid]] input)]
    (throw (ex-info "Invalid bookmark or user ID" {:error-type :invalid-input :error-data (me/humanize err)}))
    (do
      (log/info "Fetch bookmark" bookmark-id "for user" user-id)
      (jdbc/execute-one! ds (-> (helpers/select :*)
                                (helpers/from :bookmark)
                                (helpers/where [:= :bookmark/user-id user-id] [:= :bookmark/id bookmark-id])
                                (sql/format))))))

(defn fetch-bookmarks
  [{tab-id :tab/id user-id :user/id :as input}]
  (if-let [err (m/explain [:map {:closed true} [:bookmark/tab-id :uuid] [:bookmark/user-id :uuid]] input)]
    (throw (ex-info "Invalid tab or user ID" {:error-type :invalid-input :error-data (me/humanize err)}))
    (do
      (log/info "Fetch all bookmarks in tab" tab-id "for user" user-id)
      (jdbc/execute! ds (-> (helpers/select :*)
                            (helpers/from :bookmark)
                            (helpers/where [:= :bookmark/user-id user-id] [:= :bookmark/tab-id tab-id])
                            (sql/format))))))

(defn create-bookmark-tag
  [{bookmark-id :bookmark/id tag-id :tag/id user-id :user/id :as input}]
  (if-let [err (m/explain s/bookmark-tag-spec input)]
    (throw (ex-info "Invalid bookmark, tag or user ID" {:error-type :invalid-input :error-data (me/humanize err)}))
    (try
      (log/info "Assign tag" tag-id "to bookmark" bookmark-id "for user" user-id)
      (jdbc/execute-one! ds (-> (helpers/insert-into :bookmark-tag)
                                (helpers/values [{:bookmark-tag/bookmark-id bookmark-id
                                                  :bookmark-tag/tag-id      tag-id
                                                  :bookmark-tag/user-id     user-id}])
                                (helpers/returning :*)
                                (sql/format)))
      (catch PSQLException e
        (log/error e)
        (case (.getSQLState e)
          "23503" (throw (ex-info "Nonexistent bookmark or tag" {:error-type :invalid-input
                                                                 :error-data {:bookmark/id ["nonexistent"]
                                                                              :tag/id      ["nonexistent"]}}))
          (throw (ex-info "Unknown error" {:error-type :unknown} e)))))))

(defn fetch-tags-by-bookmark
  [{bookmark-id :bookmark/id user-id :user/id :as input}]
  (if-let [err (m/explain [:map {:closed true} [:bookmark/id :uuid] [:user/id :uuid]] input)]
    (throw (ex-info "Invalid bookmark or user ID" {:error-type :invalid-input :error-data (me/humanize err)}))
    (do
      (log/info "Fetch tags of bookmark" bookmark-id "for user" user-id)
      (jdbc/execute! ds (-> (helpers/select :*)
                            (helpers/from :bookmark-tag)
                            (helpers/where [:= :bookmark-tag/bookmark-id bookmark-id]
                                           [:= :bookmark-tag/user-id user-id])
                            (sql/format))))))

(defn fetch-bookmarks-by-tag
  [{tag-id :tag/id user-id :user/id :as input}]
  (if-let [err (m/explain [:map {:closed true} [:tag/id :uuid] [:user/id :uuid]] input)]
    (throw (ex-info "Invalid tag or user ID" {:error-type :invalid-input :error-data (me/humanize err)}))
    (do
      (log/info "Fetch bookmarks that have tag" tag-id "for user" user-id)
      (jdbc/execute! ds (-> (helpers/select :*)
                            (helpers/from :bookmark-tag)
                            (helpers/where [:= :bookmark-tag/tag-id tag-id]
                                           [:= :bookmark-tag/user-id user-id])
                            (sql/format))))))

(defn delete-bookmark-tag
  [{bookmark-id :bookmark/id tag-id :tag/id user-id :user/id :as input}]
  (if-let [err (m/explain s/bookmark-tag-delete-spec input)]
    (throw (ex-info "Invalid bookmark, tag or user ID" {:error-type :invalid-input :error-data (me/humanize err)}))
    (do
      (log/info "Delete tag" tag-id "from bookmark" bookmark-id "for user" user-id)
      (jdbc/execute-one! ds (-> (helpers/delete-from :bookmark-tag)
                                (helpers/where [:= :bookmark-tag/bookmark-id bookmark-id]
                                               [:= :bookmark-tag/tag-id tag-id]
                                               [:= :bookmark-tag/user-id user-id])
                                (helpers/returning :*)
                                (sql/format))))))
