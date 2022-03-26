(ns shinsetsu.ui.elastic
  (:require
    [com.fulcrologic.fulcro.algorithms.react-interop :as ri]
    ["@elastic/eui" :refer [EuiProvider
                            EuiButton
                            EuiForm
                            EuiFormRow
                            EuiFieldText
                            EuiSwitch
                            EuiFlexGroup
                            EuiFlexItem
                            EuiPageTemplate
                            EuiEmptyPrompt
                            EuiSpacer
                            EuiPage
                            EuiPageHeader
                            EuiPageBody
                            EuiPageContent
                            EuiPageContentBody
                            EuiLoadingSpinner
                            EuiModal
                            EuiModalHeader
                            EuiModalHeaderTitle
                            EuiModalBody
                            EuiModalFooter
                            EuiIcon
                            EuiCard]]))

(def provider (ri/react-factory EuiProvider))
(def empty-prompt (ri/react-factory EuiEmptyPrompt))
(def spacer (ri/react-factory EuiSpacer))
(def loading-spinner (ri/react-factory EuiLoadingSpinner))
(def icon (ri/react-factory EuiIcon))
(def card (ri/react-factory EuiCard))

;; Form
(def form (ri/react-factory EuiForm))
(def form-row (ri/react-factory EuiFormRow))

;; Form controls
(def field-text (ri/react-factory EuiFieldText))
(def switch (ri/react-factory EuiSwitch))
(def button (ri/react-factory EuiButton))

;; Flex
(def flex-group (ri/react-factory EuiFlexGroup))
(def flex-item (ri/react-factory EuiFlexItem))

;; Page
(def page (ri/react-factory EuiPage))
(def page-template (ri/react-factory EuiPageTemplate))
(def page-header (ri/react-factory EuiPageHeader))
(def page-body (ri/react-factory EuiPageBody))
(def page-content (ri/react-factory EuiPageContent))
(def page-content-body (ri/react-factory EuiPageContentBody))

;; Modal
(def modal (ri/react-factory EuiModal))
(def modal-header (ri/react-factory EuiModalHeader))
(def modal-header-title (ri/react-factory EuiModalHeaderTitle))
(def modal-body (ri/react-factory EuiModalBody))
(def modal-footer (ri/react-factory EuiModalFooter))
