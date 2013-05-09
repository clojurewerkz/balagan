# `Hash` is synonym of `Mess`, Bałagan is it's Slavic brother

A tiny library for data structure transformation in spirit on [Enlive](https://github.com/cgrand/enlive).

It's often hard to come up with a sensible DSL for data structure transformation. You either get
too close to the data domain, and your functions get messy and difficult to write (or even maybe
too nested) or you're too far away from data, and your functions operate nested structures as if
they were plain.

Balagan solves that problem for you by providing you with predicate-based selectors and arbitrary
transformation functions that suits any taste.

Let's say you're working on a Data Access layer for the application. You have a user entry
represented as hash:

```clojure
{:name "Alex"
 :nickname "ifesdjeen"}
```

## Usage



## License

Copyright © 2013 Alex P

Distributed under the Eclipse Public License, the same as Clojure.
