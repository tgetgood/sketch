(defproject sketch "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.562"]
                 [org.clojure/core.async "0.3.443"
                  :exclusions [org.clojure/tools.reader]]
                 [devcards "0.2.3"]
                 [reagent "0.6.2"]
                 [re-frame "0.9.4"]]

  :plugins [[lein-figwheel "0.5.10"]
            [lein-cljsbuild "1.1.6" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "devcards"
                :source-paths ["src"]
                :figwheel { :devcards true}
                :compiler {:main       "sketch.devcards"
                           :asset-path "js/compiled/devcards_out"
                           :output-to  "resources/public/js/compiled/devcards.js"
                           :output-dir "resources/public/js/compiled/devcards_out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "dev"
                :source-paths ["src"]

                ;; the presence of a :figwheel configuration here
                ;; will cause figwheel to inject the figwheel client
                ;; into your build
                :figwheel {:on-jsload "sketch.core/mount-root"}

                :compiler {:main sketch.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/sketch.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           ;; To console.log CLJS data-structures make sure you enable devtools in Chrome
                           ;; https://github.com/binaryage/cljs-devtools
                           :preloads [devtools.preload]}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/sketch.js"
                           :main sketch.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             :nrepl-port 7888


             ;; if you are using emacsclient you can just use
             :open-file-command "emacsclient"


             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             }


  ;; setting up nREPL for Figwheel and ClojureScript dev
  ;; Please see:
  ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl


  :profiles {:dev {:dependencies [[binaryage/devtools "0.8.2"]
                                  [figwheel-sidecar "0.5.10"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :plugins [[cider/cider-nrepl "0.14.0"]
                             [refactor-nrepl "2.2.0"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {; for nREPL dev you really need to limit output
                                  :init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

)
