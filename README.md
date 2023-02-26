# cljs-showcase

This repository showcases how you can showcase your ClojureScript library and
make it interactive using [SCI](https://github.com/babashka/sci).

To read the interactive version of this markdown file, go [here](https://borkdude.github.io/cljs-showcase).

To add an interactive ClojureScript snippet, you have to surround your code
blocks with a div that has a class `cljs-showcase`, like the one below. You can
add your own styling, of course.

<div style="width: 600px;" class="cljs-showcase">

    (+ 1 2 3)

</div>

Error messages also show up as results:

<div style="width: 600px;" class="cljs-showcase">

``` clojure
(require 'foo)
```

</div>

Promise values are resolved and their results are shown:

<div style="width: 600px;" class="cljs-showcase">

``` clojure
(-> (js/fetch "dude") (.then (fn [v] (.-status v))))
```

</div>

To add your own libraries, go to [src/cljs_showcase/core.cljs](https://github.com/borkdude/cljs-showcase/blob/main/src/cljs_showcase/core.cljs)

and use the SCI API to add your ClojureScript namespaces. E.g. to add [medley](https://github.com/weavejester/medley), we run:

<div style="width: 600px;" class="cljs-showcase" data-cljs-showcase-no-editable="true" data-cljs-showcase-no-eval="true">

``` clojure
(def medley-ns (sci/copy-ns medley.core (sci/create-ns 'medley.core)))
(ctx-store/swap-ctx! sci/merge-opts {:namespaces {'medley.core medley-ns}})
```

</div>

After doing so, we can use medley interactively in a codeblock:

<div style="width: 600px;" class="cljs-showcase">

``` clojure
(require '[medley.core :as medley])
(medley/index-by :id [{:id 1} {:id 2}])
```

</div>

<div style="width: 600px;" class="cljs-showcase">

``` clojure
(ns-publics 'medley.core)
```

</div>

<script src="js/main.js" type="application/javascript"></script>
