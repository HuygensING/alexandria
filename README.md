# alexandria command-line app

## download

An up-to-date version can be downloaded from [https://cdn.huygens.knaw.nl/alexandria/alexandria-app.zip]

Alternatively, you can build it yourself:

## build
- `mvn package`

The .zip in `alexandria-markup-server/target` contains a `lib` dir with the fat jar,
 a `bin` dir with the alexandria scripts for linux and windows,
 and an `example` dir.

## install from zip

Unpack the zip to a new directory of your choice, and add the `bin` directory to your `PATH`

## usage: example

Go to the directory that you unpacked the zip into, and enter:

* `cd example`

* `alexandria init`

  This will prepare the directory for alexandria usage.

* `alexandria register-document -n d -f frost-quote.tagml`  
  `alexandria register-document --name d --file frost-quote.tagml`

  This will set up a TAG document from the tagml in the frost-quote file.
  
  This document can later be referred to by its name: `d`

* `alexandria define-view -n l -f view-l-markup.json`  
  `alexandria define-view --name l --file view-l-markup.json`
  
  In `view-l-markup.json` we've defined a view that only shows the `[l>` markup.
  
  The view definition registered by this command can later be referred to by its name: `l`

* `alexandria define-view -n s -f view-s-layer.json`  
  `alexandria define-view --name s --file view-s-layer.json`

  In `view-s-layer.json` we've defined a view that only shows the `S` layer.
  
  The view definition registered by this command can later be referred to by its name: `s`

* `alexandria checkout -v s -d d`  
  `alexandria checkout --view s --document d`

  This will export a view of document `d` using view definition `s`.
  
  The view will be exported to file `d-s.tagml`
  
* `alexandria diff d-s.tagml`

  This will show the changes made to the view in file `d-s.tagml`.
  
* `alexandria revert d-s.tagml`

  This will revert the changes made to the view in file `d-s.tagml`.

* `alexandria query -d d -q "select text from markup('l')"`  
  `alexandria query --document d --query "select text from markup('l')"`

  This will run the query given after `-q` or `--query` against the document given after `-d` or `--document`, and show the results.

* `alexandria info`

  This will show the version and build date of the app, and the names of the documents and views that are registered.
