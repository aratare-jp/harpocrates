(ns shinsetsu.ui.bookmark
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.dom :refer [h1 div]]
    [com.fulcrologic.fulcro.mutations :as m]
    [malli.core :as mc]
    [shinsetsu.schema :as s]
    [shinsetsu.mutations.bookmark :refer [create-bookmark]]
    [shinsetsu.ui.elastic :as e]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]))

(defsc BookmarkModal
  [this {:bookmark/keys [id title url tab-id] :ui/keys [loading? error-type]} {:keys [on-close]}]
  {:ident         :bookmark/id
   :query         [:bookmark/id :bookmark/title :bookmark/url :bookmark/tab-id
                   :ui/loading? :ui/error-type fs/form-config-join]
   :form-fields   #{:bookmark/title :bookmark/url}
   :initial-state {:bookmark/title "" :bookmark/url ""}
   :pre-merge     (fn [{:keys [data-tree]}] (fs/add-form-config BookmarkModal data-tree))}
  (let [on-title-changed (fn [e] (m/set-string! this :bookmark/title :event e))
        on-url-changed   (fn [e] (m/set-string! this :bookmark/url :event e))
        on-blur          (fn [f] (comp/transact! this [(fs/mark-complete! {:field f})]))
        tab-valid?       (mc/validate s/bookmark-form-spec #:bookmark{:title title :url url})
        on-close         (fn [_]
                           (comp/transact! this [(fs/reset-form! {:form-ident (comp/get-ident this)})])
                           (on-close))
        on-tab-save      #(let [args #:bookmark{:id id :title title :url url :tab-id tab-id}]
                            (comp/transact! this [(create-bookmark args)]))
        on-clear         #(comp/transact! this [(fs/reset-form! {:form-ident (comp/get-ident this)})])
        errors           (case error-type
                           :invalid-input ["Unable to create new tab." "Please try again."]
                           :internal-server-error ["Unknown error encountered"]
                           nil)]
    (e/modal {:onClose on-close}
      (e/modal-header {}
        (e/modal-header-title {}
          (h1 (if (tempid/tempid? id) "Edit Bookmark" "Create New Bookmark"))))
      (e/modal-body {}
        (e/form {:component "form" :isInvalid (boolean errors) :error errors}
          (e/form-row {:label "Title"}
            (e/field-text {:name     "title"
                           :value    title
                           :onChange on-title-changed
                           :onBlur   #(on-blur :bookmark/title)
                           :disabled loading?}))
          (e/form-row {:label "URL"}
            (e/field-text {:name     "url"
                           :value    url
                           :type     "url"
                           :onBlur   #(on-blur :bookmark/url)
                           :disabled loading?
                           :onChange on-url-changed}))))
      (e/modal-footer {}
        (e/button {:onClick on-close} "Cancel")
        (e/button {:type      "submit"
                   :fill      true
                   :onClick   on-tab-save
                   :isLoading loading?
                   :disabled  (not tab-valid?)
                   :form      "tab-modal-form"} "Save")
        (e/button {:onClick on-clear} "Clear")))))

(def ui-bookmark-modal (comp/factory BookmarkModal {:keyfn :bookmark/id}))

(defsc Bookmark
  [this {:bookmark/keys [id title url] :as bookmark}]
  {:ident :bookmark/id
   :query [:bookmark/id :bookmark/title :bookmark/url]}
  (e/card {:title       title
           :description url
           :onClick     #()}))

(def ui-bookmark (comp/factory Bookmark {:keyfn :bookmark/id}))
