# _Alexandria_: A Command-line Application for digital text editing

## What is it?
In short: _Alexandria_ is a text repository system in which you can store and edit documents. It is the reference implementation of [TAG](https://github.com/HuygensING/TAG) (Text-as-Graph), a flexible graph data model for text. TAG and _Alexandria_ are under active development at the Research and Development group of the Humanities Cluster.

## What's the difference?
You may wonder, why use _Alexandria_ if you already have a text editing tool? Well, if you'd like to carry out advanced text analysis and you don't mind a little command-line work, Alexandria is the tool for you. If you're used to working with XML, you'll find it enlightening to work with a data model like TAG in which you can easily model overlapping structures, discontinuous elements, and nonlinear text without having to resort to workarounds.

## Can I use it?
Yes, you can. Below we explain how you can download and install _Alexandria_ on your local machine, and what you need to operate it. We'll also provide links to a comprehensive tutorial and other helpful sites.

Keep in mind that both the TAG data model and the _Alexandria_ implementation are under development. This means that by using _Alexandria_ you will make a valuable contribution to the development process. We therefore encourage you to try it out and [share your thoughts](mailto:research-development@di.huc.knaw.nl).

## Command-line
As noted above, _Alexandria_ is a command-line tool. In practice this means that it doesn't have an interface: you run Alexandria from your command line (sometimes also called the shell, the terminal, or the command prompt) and interact with it by typing out instructions in words and then hitting the Enter key. Not just any instructions, of course: the command line is very particular about how and what you tell it. If you're unfamiliar with the command line, you'll find a good tutorial [here](http://nbviewer.jupyter.org/github/DiXiT-eu/collatex-tutorial/blob/master/unit1/Command_line.ipynb) or [here](https://pittsburgh-neh-institute.github.io/Institute-Materials-2017/schedule/week_1/command_resources.html). 

## Sublime Text Editor
Install Sublime Text 3, a cross-platform editor that has syntax highlighting for TAGML. We recommend you use it to view and create TAGML transcriptions; it makes your work a lot easier. Instructions about how to download Sublime and add the TAGML package for TAGML syntax highlighting can be found [here](https://github.com/HuygensING/TAG/blob/develop/TAGML/syntax-hilite.README.md).

## Installation instructions

### 1.a. Download
An up-to-date version of _Alexandria_ can be downloaded from [https://cdn.huygens.knaw.nl/alexandria/alexandria-app.zip]

### 1.b. Build
Alternatively, you can build it yourself with `mvn package`  
The .zip in `alexandria-markup-server/target` contains a `lib` dir with the fat jar,  a `bin` dir with the alexandria scripts for linux and windows, and an `example` dir. **UPDATE the directories of the zip?**

### 2. Install _Alexandria_ from the zip

Unpack the zip to a new directory of your choice. Now you have to make sure that your machine can always find the `bin` directory that contains _Alexandria_. You have three options:

#### 2.a. Make a softlink

#### 2.b. Create an alias

#### 2.c. Add the directory your `PATH`

## Background and tutorial
If you're curious to learn more about _Alexandria_, you can take a look at the tutorial. 

We have created a tutorial for _Alexandria_ in the form of a [Jupyter Notebook](http://nbviewer.jupyter.org/github/DiXiT-eu/collatex-tutorial/blob/master/unit1/Jupyter_notebook.ipynb). The notebook contains blocks of text and small snippets of code: commands that you give to your version of Alexandria. You can run these commands from within the notebook. The notebook, in other words, is a secure environment for you to play around with and get to know Alexandria. 

## Literature

- list of publications
