(ns cljs-showcase.core
  (:require
   ["@codemirror/state" :as cs]
   ["@codemirror/view" :as cv]
   ["@nextjournal/lang-clojure" :as lc]
   ["codemirror" :as cm]
   [promesa.core :as p]
   [sci.core :as sci]
   [clojure.string :as str]))

(defn cm-string [cm-instance]
  (-> cm-instance .-state .-doc .toString))

(def ctx (sci/init {:namespaces {}
                    :classes {'js js/globalThis
                              :allow :all
                              'Math js/Math}
                    :ns-aliases {'clojure.pprint 'cljs.pprint}}))

(defn eval-codemirror [cm-instance]
  (try
    (let [code-str (cm-string cm-instance)
          v (sci/eval-string* ctx code-str)]
      (if (instance? js/Promise v)
        (-> v
            (.then
             (fn [v]
               [:success-promise v]))
            (.catch
             (fn [err]
               [:error-promise err])))
        [:success v]))
    (catch :default err
      [:error err])))

(defonce init-instances
  (let [elts (js/document.querySelectorAll ".cljs-showcase")]
    (doseq [elt elts]
      (let [doc (.-innerText elt)
            _ (set! (.-innerText elt) "")
            cm-ref (atom nil)
            res (js/document.createElement "pre")
            eval-me (fn []
                      (p/let [[op v] (eval-codemirror @cm-ref)]
                        (case op
                          (:success :success-promise)
                          (set! (.-innerText res)
                                (str (when (= :success-promise op)
                                       "Promise resolved to:\n")
                                     (pr-str v)))
                          (:error :error-promise)
                          (set! (.-innerText res)
                                (str/join "\n" (cons (.-message v)
                                                     (sci/format-stacktrace (sci/stacktrace v))))))))
            ext (.of cv/keymap
                     (clj->js [{:key "Mod-Enter"
                                :run eval-me}
                               #_{:key (str modifier "-Enter")
                                  :shift (partial eval-top-level on-result)
                                  :run (partial eval-at-cursor on-result)}]))
            cm (cm/EditorView. #js {:doc doc
                                    :extensions #js [cm/basicSetup, (lc/clojure), (.highest cs/Prec ext)]
                                    :parent elt
                                    #_#_:dispatch (fn [tr] (-> cm (.update #js [tr])) (eval-me))})]
        (let [btn (js/document.createElement "button")
              _ (set! (.-style btn) "float: right")
              _ (set! btn -innerText "Eval")
              _ (.addEventListener elt "click" eval-me)]
          (.appendChild elt btn))
        (.appendChild elt res)
        (reset! cm-ref cm)
        (eval-me)))))

(defn linux? []
  (some? (re-find #"(Linux)|(X11)" js/navigator.userAgent)))

(defn mac? []
  (and (not (linux?))
       (some? (re-find #"(Mac)|(iPhone)|(iPad)|(iPod)" js/navigator.platform))))

;; (def default-eval-text "Eval")
;; (let [elt (js/document.getElementById "evalMe")
;;       txt default-eval-text
;;       mod-symbol (if (mac?)
;;                    "⌘"
;;                    "⌃")
;;       txt (str txt " " mod-symbol "-⏎")]
;;   (set! (.-innerHTML elt) txt)
;;   (.addEventListener elt "click" (fn []
;;                                    (eval-cell))))

(sci/alter-var-root sci/print-fn (constantly *print-fn*))
(sci/alter-var-root sci/print-err-fn (constantly *print-err-fn*))
(sci/enable-unrestricted-access!)
