(ns cljs-showcase.core
  (:require
   ["@codemirror/state" :as cs]
   ["@codemirror/view" :as cv]
   ["@nextjournal/lang-clojure" :as lc]
   ["codemirror" :as cm]
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [medley.core]
   [promesa.core :as p]
   [sci.core :as sci]
   [sci.ctx-store :as ctx-store]
   [juxt.trip.core]))

(defn cm-string [cm-instance]
  (-> cm-instance .-state .-doc .toString))

(let [ctx (sci/init {:namespaces {}
                     :classes {'js js/globalThis
                               :allow :all
                               'Math js/Math}
                     :ns-aliases {'clojure.pprint 'cljs.pprint
                                  'clojure.spec.alpha 'cljs.spec.alpha}})]
  (ctx-store/reset-ctx! ctx))

(defn eval-codemirror [cm-instance]
  (try
    (let [code-str (cm-string cm-instance)
          v (sci/eval-string* (ctx-store/get-ctx) code-str)]
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
    (doseq [^js elt elts]
      (let [_ (js/console.log (.. elt -dataset))
            no-editable? (.. elt -dataset -cljsShowcaseNoEditable)
            no-eval? (.. elt -dataset -cljsShowcaseNoEval)
            eval? (not no-eval?)
            doc (.-innerText elt)
            _ (set! (.-innerText elt) "")
            cm-ref (atom nil)
            res (js/document.createElement "pre")
            eval-me (fn []
                      (when eval?
                        (p/let [[op v] (eval-codemirror @cm-ref)]
                          (binding [*print-length* 20]
                            (case op
                              (:success :success-promise)
                              (set! (.-innerText res)
                                    (str (when (= :success-promise op)
                                           "Promise resolved to:\n")
                                         (with-out-str (pp/pprint v))))
                              (:error :error-promise)
                              (set! (.-innerText res)
                                    (str/join "\n" (cons (.-message v)
                                                         (sci/format-stacktrace (sci/stacktrace v))))))))))
            ext (.of cv/keymap
                     (clj->js [{:key "Mod-Enter"
                                :run eval-me}]))
            state (cs/EditorState.create #js {:doc doc
                                              :extensions #js [cm/basicSetup, (lc/clojure), (.highest cs/Prec ext),
                                                               (cs/EditorState.readOnly.of no-editable?)]})
            cm (cm/EditorView. #js {:state state :parent elt})]
        (when eval?
          (let [btn (js/document.createElement "button")
                _ (set! (.-style btn) "float: right")
                _ (set! btn -innerText "Eval")
                _ (.addEventListener elt "click" eval-me)]
            (.appendChild elt btn)))
        (.appendChild elt res)
        (reset! cm-ref cm)
        (when eval? (eval-me))))))

(sci/alter-var-root sci/print-fn (constantly *print-fn*))
(sci/alter-var-root sci/print-err-fn (constantly *print-err-fn*))
(sci/enable-unrestricted-access!)

;;;; User configuration begins here

(def medley-ns (sci/copy-ns medley.core (sci/create-ns 'medley.core)))

(def tns (sci/create-ns 'juxt.trip.core))
(def trip-namespace (sci/copy-ns juxt.trip.core tns {:exclude-when-meta []}))

(ctx-store/swap-ctx! sci/merge-opts {:namespaces {'medley.core medley-ns
                                                  'juxt.trip.core trip-namespace}})

(set! cljs.core/*eval* #(sci/eval-form (ctx-store/get-ctx) %))
