# `Hash` is synonym of `Mess`, Bałagan is it's Slavic brother

A tiny library for data structure transformation in spirit on [Enlive](https://github.com/cgrand/enlive).

It's often hard to come up with a sensible DSL for data structure transformation. You either get
too close to the data domain, and your functions get messy and difficult to write (or even maybe
too nested) or you're too far away from data, and your functions operate nested structures as if
they were plain.

Bałagan solves that problem for you by providing you with predicate-based selectors and arbitrary
transformation functions that suits any taste.

## Library status

It's a couple of days old so far, this section will be updated as it matures. Plan is to use it
to avoid having anything to do with entity mappers or ORMs. Data access layer is hard enough
problem by itself, complexing it with entity mappers that hide retrieval logic from you won't
help in the long run (think ActiveRecord, Hibernate and friends). With a very little code,
you'll be able to construct and trasnform very complex entities, without hiding any of the
retrieval / implementation details. No magic involved.

## Usage

If you don't get Enlive way of doing things, most likely the approach will be foreign. Nevetheless,
as the library matures, there will be more / better use cases described here. For now, it's mostly
a quick overview. For more details, please refer test suite.

Let's say you're working on a Data Access layer for the application. You have a user entry
represented as hash:

```clojure
(def user
  {:name "Alex"
   :birth-year 1990
   :nickname "ifesdjeen"})
```

Now, we can start transforming users the way we want: add, remove fields based on certain conditions.

```clojure
(transform user
           []                  (add-field :cool-dude true) ;; adds a field :cool-dude with value true
           (new-path [:age])   #(- 2013 (:birth-year %))   ;; explicit adding of a new field, calculated from the existing data
           (new-path [:posts]) #(fetch-posts (:name %))    ;; fetching some related data from the DB
           [:posts :*]         #(transform-posts %))       ;; apply some transformations to all the fetched posts, if there are any
```


## Community

To subscribe for announcements of releases, important changes and so on, please follow
[@ClojureWerkz](https://twitter.com/#!/clojurewerkz) on Twitter.


## Artifacts

Bałagan artifacts are [released to Clojars](https://clojars.org/clojurewerkz/balagan). If you are using Maven, add the following repository definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### Most recent pre-release version

With Leiningen:

```clojure
[clojurewerkz/balagan "0.1.0"]
```

With Maven:

```xml
<dependency>
  <groupId>clojurewerkz</groupId>
  <artifactId>balagan</artifactId>
  <version>0.1.0</version>
</dependency>
```


## Balagan Is a ClojureWerkz Project

Balagan is part of the [group of libraries known as ClojureWerkz](http://clojurewerkz.org), together with
[Monger](https://clojuremongodb.info), [Welle](https://clojureriak.info), [Neocons](https://clojureneo4j.info),
[Elastisch](https://clojureelasticsearch.info) and several others.


## Continuous Integration

[![Continuous Integration status](https://secure.travis-ci.org/clojurewerkz/balagan.png)](http://travis-ci.org/clojurewerkz/balagan)

CI is hosted by [travis-ci.org](http://travis-ci.org)


## Development

Balagan uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make
sure you have it installed and then run tests against all supported Clojure versions using

```
lein2 all test
```

Then create a branch and make your changes on it. Once you are done with your changes and all
tests pass, submit a pull request on Github.


## License

Copyright © 2013 Alex P, Michael S. Klishin

Distributed under the Eclipse Public License, the same as Clojure.
