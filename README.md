# _Alexandria_: A Command-line Application for Digital Text Editing

[![GitHub Actions](https://github.com/HuygensING/alexandria/workflows/tests/badge.svg)](https://github.com/HuygensING/alexandria/actions)
[![Project Status: Inactive – The project has reached a stable, usable state but is no longer being actively developed; support/maintenance will be provided as time allows.](https://www.repostatus.org/badges/latest/inactive.svg)](https://www.repostatus.org/#inactive)

## What is it?
In short: _Alexandria_ is a text repository system in which you can store and edit documents. It is the reference implementation of [TAG](https://huygensing.github.io/TAG/) (Text-as-Graph), a flexible graph data model for text. TAG and _Alexandria_ are under active development at the [Research & Development group](https://huc.knaw.nl/research/infrastructure-rd/) of the Humanities Cluster.

You may wonder, why use _Alexandria_ if you already have a text editing tool? Well, if you'd like to carry out advanced text analysis, and you don't mind a little command-line work, _Alexandria_ is the tool for you. If you're used to working with XML, you'll find it especially enlightening to work with a data model like TAG in which you can easily model overlapping structures, discontinuous elements, and nonlinear text without having to resort to workarounds.

## News

### Latest release: _Alexandria 2.3_ (September 2019)

##### Bugfixes:
- It is now possible, when a view other than the default view is active, to commit new and changed view definitions, and new tagml source files.
  Committing changes to tagml source files of existing documents is still only possible when the default view is active.
- Running `alexandria init` in your home directory is not allowed and will fail with an error message.
- `alexandria status` will now only search one level deep in watched directories for files/directories that may be added.
- When committing a view definition which is valid json, but does not contain at least one of the required fields `includeLayers`, `excludeLayers`, `includeMarkup`, `excludeMarkup`, alexandria would silently accept this, producing an invalid view.
  This has been fixed: committing this view definition will not fail with an error message.    


### Release: _Alexandria 2.2_ (July 2019)

###### [New/Changed commands for the command-line app](https://huygensing.github.io/alexandria/commands)
- query

##### Bugfixes:

- After a revert, the reverted file is now no longer shown as modified.
- It is now possible to run alexandria commands from any directory, provided one of its parent directories has been initialized.

### Release: _Alexandria 2.1_ (December 2018)

##### New features:

- [AlexandriaStep for use in Calabash](https://huygensing.github.io/TAG/TAGML/CALABASH-README)
- [TAGML Syntax highlighting in Sublime Text 3](https://huygensing.github.io/tagml-sublime-syntax/)

###### [New/Changed commands for the command-line app](https://huygensing.github.io/alexandria/commands)
- about
- add
- commit
- export-dot
- export-svg
- export-png
- export-xml 

##### Bugfixes:

- The first markup is now always the root markup for the default layer, even if new layers are defined on that markup.
- This means that this first markup tag must correspond with the last closing markup tag, and suspending/resuming of this markup is not allowed.
 

### Presentation
Haentjens Dekker, Ronald. Invited talk at the Workshop on Scholarly Digital Editions, Graph Data-Models and Semantic Web Technologies, Université de Lausanne, 3 June 2019.

## Documentation
Below we explain how you can download and install _Alexandria_ on your local machine, and what you need to operate it. We'll also provide links to a comprehensive tutorial and other helpful sites.

Keep in mind that both the TAG data model and the _Alexandria_ implementation are under development. This means that by using _Alexandria_ you will make a valuable contribution to the development process. We therefore encourage you to try it out and [share your thoughts](mailto:research-development@di.huc.knaw.nl).

### About that command line...
_Alexandria_ is a command-line tool. In practice this means that it doesn't have a Graphical User Interface: you run Alexandria from your command line (sometimes also called the shell, the terminal, or the command prompt) and interact with it by typing out instructions in words and then hitting the Enter key. If you're unfamiliar with the command line, you'll find a good tutorial [here](http://nbviewer.jupyter.org/github/DiXiT-eu/collatex-tutorial/blob/master/unit1/Command_line.ipynb) or [here](https://pittsburgh-neh-institute.github.io/Institute-Materials-2017/schedule/week_1/command_resources.html). 

### Sublime Text Editor
Install [Sublime Text 3](http://www.sublimetext.com/), a cross-platform editor that has syntax highlighting for TAGML. We recommend you use it to view and create TAGML transcriptions; it makes your work a lot easier. Instructions on adding the Sublime package for TAGML syntax highlighting can be found [here](https://huygensing.github.io/tagml-sublime-syntax/).

### 1. Installation

#### 1a. Download
An up-to-date version of _Alexandria_ can be downloaded from [https://github.com/HuygensING/alexandria/releases/tag/2.4](https://github.com/HuygensING/alexandria/releases/tag/2.4)

#### 1b. Build
Alternatively, you can build it yourself with
```
mvn package
```
The .zip in `alexandria-markup-server/target` contains a `lib` dir with the fat jar,  a `bin` dir with the alexandria scripts for linux and windows, and an `example` dir.

### 2. Unpack the zip
Unpack the zip to a new directory of your choice. Remember the path to that directory. Now you have to make sure that your machine can always find the `bin` directory that contains the _Alexandria_ code when you call it. You have three options:

#### 2a. Create a permanent alias in your .bash_profile
Open your .bash_profile. If you're on a Unix machine, you can type `open -a "Sublime Text" ~/.bash_profile` in your terminal window. This will open your bash_profile in the Sublime Text editor (of course you can use an editor of your choice).  

You can create an alias for _Alexandria_ by writing `alias alexandria="<path to alexandria>"`. For instance, your alias could say `alias alexandria="/Users/alexandria-markup-server/bin/alexandria"`. Save and close your bash_profile. Before the alias works, you have to resource the bash_profile: type `source ~/.bash_profile` in your terminal.

#### 2b. Add the directory to your `PATH`
In your terminal window, type: 
```
export PATH=$PATH:<path to the alexandria bin directory>
```
For example:
```
export PATH=$PATH:/Users/alexandria-markup-server/bin
```
if that's where you've stored _Alexandria_. You can check if it works by typing
```
echo $PATH
```
in your terminal window. It should return something like the following, with the path to _Alexandria_ directory newly added at the end:
```
/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/Users/alexandria-markup-server/bin/alexandria
```

#### 2c.
If you don't want to change your path, you can create a softlink.

A soft link (also known as a symbolic link or symlink) consists of a special type of file that serves as a reference to another file or directory. You can create them on your command line: 
```
$ ln -s {source-filename} {symbolic-filename}
```

For example: 
```
$ ln -s /Users/alexandria-markup-server/bin/alexandria /usr/local/bin/alexandria
```

Verify if it works by running 
```
$ ls -l /usr/local/bin/alexandria
```

Your output will look something like:
```
lrwxr-xr-x  1 veryv  wheel  5 Mar  7 22:01 alexandria -> Users/alexandria-markup-server/bin/alexandria
```
Notice the `->` that indicates the link between the link name and the file.

### Working with _Alexandria_

If you'd like to get started right away, take a look at [an overview](commands.md) of the commands with which you interact with _Alexandria_.

We also created a [tutorial](https://huygensing.github.io/alexandria/tutorial/). The tutorial takes the form of a [Jupyter Notebook](http://nbviewer.jupyter.org/github/DiXiT-eu/collatex-tutorial/blob/master/unit1/Jupyter_notebook.ipynb). The notebook contains blocks of text and small snippets of code: commands that you give to your version of Alexandria. You can run these commands from within the notebook. The notebook, in other words, is a secure environment for you to play around with and get to know Alexandria. 

## Presentations and publications

* Bleeker, Elli. 2018. “Adressing Ancient Promises: Text Modeling and _Alexandria_”. Invited talk at the DH-Kolloquium of the Berlin Brandenburgische Akademie der Wissenschaften, 2 November 2018. [Slides](https://edoc.bbaw.de/frontdoor/index/index/searchtype/latest/docId/2932/).

* Bleeker, Elli. 2018. "Advanced Text Modeling in _Alexandria_." [Presentation](https://www.huygens.knaw.nl/koning-bezoekt-het-humanities-cluster-en-het-nias-van-de-knaw/) for his Majesty the King of the Netherlands during his visit of the Royal Academy of Arts and Sciences in the Netherlands. 
 
* Haentjens Dekker, Ronald, Elli Bleeker, Bram Buitendijk, Astrid Kulsdom and David J. Birnbaum. 2018. “TAGML: A markup language of many dimensions.” Presented at Balisage: The Markup Conference 2018, Washington, DC, July 31 - August 3, 2018. In _Proceedings of Balisage: The Markup Conference 2018. Balisage Series on Markup Technologies_, vol. 21 (2018).   
doi: `https://doi.org/10.4242 BalisageVol21.HaentjensDekker01.`
	* Paper: <http://www.balisage.net/Proceedings/vol21/html/HaentjensDekker01/BalisageVol21-HaentjensDekker01.html>
	* Slides: <https://docs.google.com/presentation/d/1TpOtNJR_3FSKfMSzUvI4wuJBBbZcchVGFjG2qP4csck/edit?usp=sharing>

* Bleeker, Elli, Bram Buitendijk, Ronald Haentjens Dekker, and Astrid Kulsdom. 2018. "Perspectives on Text. Synthesising Textual Knowledge". Presented at the international [Computational Methods for Literary-Historical Textual Scholarship](http://cts.dmu.ac.uk/events/CMLHTS/) conference at De Montfort University, July 4th 2018.

