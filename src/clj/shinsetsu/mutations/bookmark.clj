(ns shinsetsu.mutations.bookmark
  (:require
    [shinsetsu.db.bookmark :as db]
    [com.wsscode.pathom.connect :as pc :refer [defmutation]]
    [taoensso.timbre :as log]
    [malli.core :as m]
    [malli.error :as me]
    [shinsetsu.schema :as s]))

(def bookmark-input #{:bookmark/title :bookmark/url :bookmark/image :bookmark/user-id :bookmark/tab-id})
(def bookmark-output [:bookmark/id :bookmark/title :bookmark/url :bookmark/image :bookmark/created :bookmark/updated])

(defmutation create-bookmark
  [{{user-id :user/id} :request} bookmark]
  {::pc/params bookmark-input
   ::pc/output bookmark-output}
  (let [bookmark (assoc bookmark :bookmark/user-id user-id)]
    (if-let [err (m/explain s/bookmark-spec bookmark)]
      (throw (ex-info "Invalid bookmark" {:error-type :invalid-input :error-data (me/humanize err)}))
      (do
        (log/info "User with id" user-id "is attempting to create a new bookmark")
        (let [bookmark    (db/create-bookmark bookmark)
              bookmark-id (:bookmark/id bookmark)]
          (log/info "User with ID" user-id "created bookmark" bookmark-id "successfully")
          bookmark)))))

(defmutation patch-bookmark
  [{{user-id :user/id} :request} {:bookmark/keys [id] :as bookmark}]
  {::pc/params bookmark-input
   ::pc/output bookmark-output}
  (let [bookmark (assoc bookmark :bookmark/user-id user-id)]
    (if-let [err (m/explain s/bookmark-update-spec bookmark)]
      (throw (ex-info "Invalid bookmark" {:error-type :invalid-input :error-data (me/humanize err)}))
      (do
        (log/info "User with id" user-id "is attempting to patch bookmark" id)
        (let [bookmark (db/patch-bookmark bookmark)]
          (log/info "User with ID" user-id "patched bookmark" id "successfully")
          bookmark)))))

(defmutation delete-bookmark
  [{{user-id :user/id} :request} {:bookmark/keys [id] :as bookmark}]
  {::pc/params #{:bookmark/id}
   ::pc/output bookmark-output}
  (let [bookmark (assoc bookmark :bookmark/user-id user-id)]
    (if-let [err (m/explain s/bookmark-delete-spec bookmark)]
      (throw (ex-info "Invalid bookmark" {:error-type :invalid-input :error-data (me/humanize err)}))
      (do
        (log/info "User with id" user-id "is attempting to delete bookmark" id)
        (let [bookmark (db/delete-bookmark bookmark)]
          (log/info "User with ID" user-id "deleted bookmark" id "successfully")
          bookmark)))))

(defmutation create-bookmark-tag
  [{{user-id :user/id} :request} {tag-id :tag/id bookmark-id :bookmark/id :as input}]
  {::pc/params #{:bookmark/id :tag/id}
   ::pc/output [:bookmark/id :tag/id]}
  (let [input (assoc input :user/id user-id)]
    (if-let [err (m/explain s/bookmark-tag-spec input)]
      (throw (ex-info "Invalid bookmark or tag" {:error-type :invalid-input :error-data (me/humanize err)}))
      (do
        (log/info "User with id" user-id "is attempting to assign tag" tag-id "to bookmark" bookmark-id)
        (db/create-bookmark-tag input)
        (log/info "User with ID" user-id "assigned tag" tag-id "to bookmark" bookmark-id "successfully")
        {:bookmark/id bookmark-id :tag/id tag-id}))))

(defmutation delete-bookmark-tag
  [{{user-id :user/id} :request} {tag-id :tag/id bookmark-id :bookmark/id :as input}]
  {::pc/params #{:bookmark/id :tag/id}
   ::pc/output [:bookmark/id :tag/id]}
  (let [input (assoc input :user/id user-id)]
    (if-let [err (m/explain s/bookmark-tag-delete-spec input)]
      (throw (ex-info "Invalid bookmark or tag" {:error-type :invalid-input :error-data (me/humanize err)}))
      (do
        (log/info "User with id" user-id "is attempting to delete assignment of tag" tag-id "to bookmark" bookmark-id)
        (db/delete-bookmark-tag input)
        (log/info "User with ID" user-id "deleted assignment of tag" tag-id "to bookmark" bookmark-id "successfully")
        {:bookmark/id bookmark-id :tag/id tag-id}))))
