## Overview of _Alexandria_ commands

Go to the directory that you unpacked the zip into. The zip contained three subdirectories: a `bin`, a `lib`, and an `example` directory.

* `cd example`

* `alexandria init`

  This will prepare the directory for alexandria usage.   
  It creates subdirectories `tagml` and `views` for the tagml source files and the view definition files, respectively.

* `alexandria add tagml/frost-quote.tagml views/l-markup.json`  

  This will add `tagml/frost-quote.tagml` and `views/l-markup.json` to the alexandria watchlist, so changes to theses file will be registered by alexandria.

* `alexandria commit -a`  
  or  
  `alexandria commit tagml/frost-quote.tagml views/l-markup.json`    
  or  
  `alexandria commit tagml/*.tagml views/*.json`    

  This will check the indicated or all (`-a`) watched files for changes since the last commit.  
  For changed tagml files, it will set up or update a TAG document.  
  For changed view definition files, it will set up or update a view.
  
* `alexandria checkout l-markup`  

  This will activate view `l-markup`, defined by `views/l-markup.json`  
  All watched tagml files will be overwritten by a tagml serialization of the text and markup of the TAG defined by the view.

* `alexandria checkout -`  

  This will deactivate the active view, the tagml files will be overwritten with the complete tagml serialization of the TAG.
  
* `alexandria diff tagml/frost-quote.tagml`

  This will show the changes made to the tagml file `tagml/frost-quote.tagml` since the last commit.
  
* `alexandria revert tagml/frost-quote.tagml`

  This will revert the changes made to the tagml file `tagml/frost-quote.tagml`.

* `alexandria about`

  This will show the version and build date of the app, and the names of the documents and views that are registered.

* `alexandria status`

  This will show the the active view, and the names of the files that have been changed since the last commit.

* `alexandria export-dot frost-quote`  
  or  
  `alexandria export-dot frost-qoute -o fq.dot`
  
  This will export the graph of document `frost-quote` (defined by ``tagml/frost-quote.tagml``) using the dot format, to either stdout or (using `-o`) to the indicated output file ).

* `alexandria export-svg frost-quote`  
  or  
  `alexandria export-svg frost-quote -o fq.svg`
  
  This will export the graph of document `frost-quote` as svg to stdout or file fq.svg.  
  For this command, graphviz needs to be installed.

* `alexandria export-png frost-quote`  
  or  
  `alexandria export-png frost-quote -o fq.png`
  
  This will export the graph of document `frost-quote` as png to stdout or file fq.png.

  For this command, graphviz needs to be installed.

* `alexandria export-xml frost-quote`  
  or  
  `alexandria export-xml frost-quote -o fq.xml`
  
  This will export document `frost-quote` as xml, to stdout or file fq.xml.  
  [Trojan Horse markup](http://www.balisage.net/Proceedings/vol21/html/Sperberg-McQueen01/BalisageVol21-Sperberg-McQueen01.html) is used to deal with overlapping hierarchies.  
  If a view is active, this view will be used for the export.
  
* `alexandria help`
  
  This will show a list of the available commands and their short descriptions.

* `alexandria query frost-quote -q sparql/markup-count.sparql`
  
  This will execute the `markup-count` SPARQL query on the knowledge graph of document `frost-quote`

#### NOTE:
- In all of the previous commands with multiple parameters, those parameters are order independent, so   
  `alexandria export-xml frost-quote -o fq.xml`  
  will give the same result as  
  `alexandria export-xml -o fq.xml frost-quote`
  
-  To show more information about an alexandria command, add `-h` or `--help`
