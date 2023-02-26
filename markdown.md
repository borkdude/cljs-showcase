# Hi! Welcome to my CLJS showcase!

Here starts the tutorial. We show that in ClojureScript you can add multiple numbers in one go:

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

<script src="js/main.js" type="application/javascript"></script>
