# alexandria command-line app

## download

An up-to-date version can be downloaded from [https://cdn.huygens.knaw.nl/alexandria/alexandria-app.zip]

Alternatively, you can build it yourself:

## build
- `mvn package`

The .zip in `alexandria-markup-lmnl-server/target` contains a `lib` dir with the fat jar,
 a `bin` dir with the alexandria scripts for linux and windows,
 and an `example` dir.

## install from zip

Unpack the zip to a new directory of your choice, and add the `bin` directory to your `PATH`

## usage: example

Go to the directory that you unpacked the zip into, and enter:

* `cd example`

* `alexandria init`
  This will prepare the directory for alexandria usage.

* `alexandria register-document -n d -f frost-quote.lmnl`
  This will set up a TAG document from the lmnl in the frost-quote file.
  This document can later be referred to by its name: `d`

* `alexandria define-view --name l --file view-l.json`
  In `view-l.json` we've defined a view that only shows the `[l}` markup.
  The view definition registered by this command can later be referred to by its name: `l`

* `alexandria define-view -n s -f view-s.json`
  In `view-s.json` we've defined a view that only shows the `[s}` markup.
  The view definition registered by this command can later be referred to by its name: `s`

* `alexandria checkout -v s -d d`
  This will export a view of document `d` using view definition `s`.
  The view will be exported to file `d-s.lmnl`
