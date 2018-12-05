# _Alexandria_: A Command-line Application for Digital Text Editing

## What is it?
In short: _Alexandria_ is a text repository system in which you can store and edit documents. It is the reference implementation of [TAG](https://huygensing.github.io/TAG/) (Text-as-Graph), a flexible graph data model for text. TAG and _Alexandria_ are under active development at the [Research & Development group](https://huc.knaw.nl/research/infrastructure-rd/) of the Humanities Cluster.

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

Unpack the zip to a new directory of your choice. Remember the path to that directory. Now you have to make sure that your machine can always find the `bin` directory that contains the _Alexandria_ code when you call it. You have three options:

#### 2.a. Create a permanent alias in your .bash_profile
Open your .bash_profile. If you're on a Unix machine, you can type `open -a "Sublime Text" ~/.bash_profile` in your terminal window. This will open your bash_profile in the Sublime Text editor (of course you can use an editor of your choice).  

You can create an alias for _Alexandria_ by writing `alias alexandria="<path to alexandria>"`. For instance, your alias could say `alias alexandria="/Users/alexandria-markup-server/bin/alexandria"`. Save and close your bash_profile. Before the alias works, you have to resource the bash_profile: type `source ~/.bash_profile` in your terminal.

#### 2.b. Add the directory your `PATH`
In your terminal window, type: `export PATH=$PATH:<path to alexandria>`. For example: `export PATH=$PATH:/Users/alexandria-markup-server/bin/alexandria` if that's where you've stored _Alexandria_. You can check if it works by typing `echo $PATH` in your terminal window. It should return something like the following, with the path to _Alexandria_ directory newly added at the end:
`/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/Users/alexandria-markup-server/bin/alexandria`

#### 2.c.
If you don't like to change your path, you can create a softlink.

A soft link (also known as a symbolic link or symlink) consists of a special type of file that serves as a reference to another file or directory. You can create them on your command line: 
`$ ln -s {source-filename} {symbolic-filename}`

For example: 
`$ ln -s /Users/alexandria-markup-server/bin/alexandria /usr/local/bin/alexandria`

Verify if it works by running 
`$ ls -l /Users/alexandria-markup-server/bin/alexandria /usr/local/bin/alexandria`

Your output will look something like:
```
-rw-r--r--  1 veryv  wheel  0 Mar  7 22:01 file1
lrwxr-xr-x  1 veryv  wheel  5 Mar  7 22:01 alexandria -> Users/alexandria-markup-server/bin/alexandria
```
Notice the `->` that indicates the link between the link name and the file.

## Background and tutorial
For an overview of the _Alexandria_ commands, see [here](https://github.com/HuygensING/alexandria-markup-server/commands.md)

If you're curious to learn more about _Alexandria_, you can take a look at a tutorial we created. The tutorial takes the form of a [Jupyter Notebook](http://nbviewer.jupyter.org/github/DiXiT-eu/collatex-tutorial/blob/master/unit1/Jupyter_notebook.ipynb). The notebook contains blocks of text and small snippets of code: commands that you give to your version of Alexandria. You can run these commands from within the notebook. The notebook, in other words, is a secure environment for you to play around with and get to know Alexandria. 

## Literature

* Bleeker, Elli. 2018. “Adressing Ancient Promises: Text Modeling and _Alexandria_”. Invited talk at the DH-Kolloquium of the Berlin Brandenburgische Akademie der Wissenschaften, 2 November 2018. Slides [here](https://edoc.bbaw.de/frontdoor/index/index/searchtype/latest/docId/2932/).
 
* Haentjens Dekker, Ronald, Elli Bleeker, Bram Buitendijk, Astrid Kulsdom and David J. Birnbaum. 2018. “TAGML: A markup language of many dimensions.” Presented at Balisage: The Markup Conference 2018, Washington, DC, July 31 - August 3, 2018. In _Proceedings of Balisage: The Markup Conference 2018. Balisage Series on Markup Technologies_, vol. 21 (2018).   
doi: `https://doi.org/10.4242 BalisageVol21.HaentjensDekker01.`
	* Paper: <http://www.balisage.net/Proceedings/vol21/html/HaentjensDekker01/BalisageVol21-HaentjensDekker01.html>
	* Slides: <https://docs.google.com/presentation/d/1TpOtNJR_3FSKfMSzUvI4wuJBBbZcchVGFjG2qP4csck/edit?usp=sharing>

* Bleeker, Elli, Bram Buitendijk, Ronald Haentjens Dekker, and Astrid Kulsdom. 2018. "Perspectives on Text. Synthesising Textual Knowledge". Presented at the international [Computational Methods for Literary-Historical Textual Scholarship](http://cts.dmu.ac.uk/events/CMLHTS/) conference at De Montfort University, July 4th 2018.
