(ns cljs-showcase.core
  (:require ["codemirror" :as cm]
            ["@nextjournal/lang-clojure" :as lc]
            ["@codemirror/view" :as cv]
            ["@codemirror/state" :as cs]
            [clojure.string :as str]
            [sci.core :as sci]))

(declare eval-me)

(def extension
  (.of cv/keymap
       (clj->js [{:key "Mod-Enter"
                  :run (fn []
                          (eval-me))}
                 #_{:key (str modifier "-Enter")
                    :shift (partial eval-top-level on-result)
                    :run (partial eval-at-cursor on-result)}])))
(def cm-instance
  (let [doc (str/trim "
(require '[reagent.core :as r]
         '[reagent.dom :as rdom]
         '[re-frame.core :as rf])

(rf/reg-event-fx ::click (fn [{:keys [db]} _] {:db (update db :clicks (fnil inc 0))}))
(rf/reg-sub ::clicks (fn [db] (:clicks db)))

(defn my-component []
  (let [clicks (rf/subscribe [::clicks])]
    [:div
      [:p \"Clicks: \" @clicks]
      [:p [:button {:on-click #(rf/dispatch [::click])}
            \"Click me!\"]]]))

(rdom/render [my-component] (.getElementById js/document \"reagent\"))
")]
    (cm/EditorView. #js {:doc doc
                         :extensions #js [cm/basicSetup, (lc/clojure), (.highest cs/Prec extension)]
                         :parent (js/document.querySelector "#app")
                         #_#_:dispatch (fn [tr] (-> cm (.update #js [tr])) (eval-me))})))

(defn eval-me []
  (-> cm-instance .-state .-doc .toString))

(defn linux? []
  (some? (re-find #"(Linux)|(X11)" js/navigator.userAgent)))

(defn mac? []
  (and (not (linux?))
       (some? (re-find #"(Mac)|(iPhone)|(iPad)|(iPod)" js/navigator.platform))))

#_(let [elt (js/document.getElementById "evalMe")
      txt (.-innerText elt)
      mod-symbol (if (mac?)
                   "⌘"
                   "⌃")
      txt (str txt " " mod-symbol "-⏎")]
  (set! (.-innerHTML elt) txt))

(eval-me)


(def ctx (sci/init {:namespaces {}
                    :classes {'js js/globalThis
                              :allow :all
                              'Math js/Math}
                    :ns-aliases {'clojure.pprint 'cljs.pprint}}))
(prn :hello)
