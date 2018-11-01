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
  or  
  `alexandria register-document --name d --file frost-quote.tagml`

  This will set up a TAG document from the tagml in the frost-quote file.
  
  This document can later be referred to by its name: `d`

* `alexandria define-view -n l -f view-l-markup.json`  
  or  
  `alexandria define-view --name l --file view-l-markup.json`
  
  In `view-l-markup.json` we've defined a view that only shows the `[l>` markup.
  
  The view definition registered by this command can later be referred to by its name: `l`

* `alexandria define-view -n s -f view-s-layer.json`  
  or  
  `alexandria define-view --name s --file view-s-layer.json`

  In `view-s-layer.json` we've defined a view that only shows the `S` layer.
  
  The view definition registered by this command can later be referred to by its name: `s`

* `alexandria checkout -d d -v s`  
  or  
  `alexandria checkout --document d --view s`

  This will export a view of document `d` using view definition `s`.
  
  The view will be exported to file `d-s.tagml`
  
* `alexandria diff d-s.tagml`

  This will show the changes made to the view in file `d-s.tagml`.
  
* `alexandria revert d-s.tagml`

  This will revert the changes made to the view in file `d-s.tagml`.

* `alexandria info`

  This will show the version and build date of the app, and the names of the documents and views that are registered.

* `alexandria export-dot -d d`  
  or  
  `alexandria export-dot --document d`
  
  This will export the graph of document `d` using the dot format, to file d.dot.

* `alexandria export-svg -d d`  
  or  
  `alexandria export-svg --document d`
  
  This will export the graph of document `d` as svg to file d.svg.
  For this command, graphviz needs to be installed.

* `alexandria export-png -d d`  
  or  
  `alexandria export-png --document d`
  
  This will export the graph of document `d` as png to file d.png.
  For this command, graphviz needs to be installed.

* `alexandria export-xml -d d -v v`  
  or  
  `alexandria export-xml --document d --view v`
  
  This will export view `v` on document `d` as xml, to file d-v.xml.
  
* `alexandria export-tagml -d d [-f frost-quote.tagml]`  
  or  
  `alexandria export-tagml --document d [--file frost-quote.tagml]`

  This will export document `d` as a TAGML.  
  The `-f` / `--file` parameter is optional, when omitted the document will be exported to `{document name}.tagml`, so `d.tagml` in this case.

* `alexandria import-tagml -d d [-f edited.tagml]`  
  or  
  `alexandria import-tagml --document d [--file edited.tagml]`

  This will import the `edited.tagml` TAGML file into document `d`.  
  The `-f` / `--file` parameter is optional, when omitted the document will be imported from `d.tagml`
  
* `alexandria -h`
  or
  `alexandria --help`
  
  This will show a list of the available commands (positional arguments)

####NOTE:
 In all of the previous commands, the parameters are order independent, so   
  `alexandria register-document -n d -f frost-quote.tagml`  
  gives the same result as  
  `alexandria register-document -f frost-quote.tagml -n d`
  
  To show more information about an alexandria command, add `-h` or `--help`
  
  For example:
  ```
   alexandria register-document -h
   usage: java -jar alexandria.jar
          register-document -n NAME -f FILE [-h]
   
   Parse a TAGML document and store it as TAG
   
   named arguments:
     -n NAME, --name NAME   The name of the document
     -f FILE, --file FILE   The file containing the document TAGML source
     -h, --help             show this help message and exit
```
  
  