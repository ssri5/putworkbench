# README #

This repository has the source code for the Privacy-Utility Tradeoff Workbench project. The code is in Java, written over OpenJDK 8 (although it should work with JDK 7 as well).

### What is this repository for? ###

* The Privacy-Utility Tradeoff Workbench is a small tool that can be used for inferring important correlations among the various attributes in a domain. If due to any reasons, the architects are unwilling to use the all the parameters in a dataset for learning purposes, and are willing to try out a small subset of them for any practical ML application, this tool can help them find correlations which in turn can help in choosing a right set of attributes for large scale usage (the tool has been built using "privacy" as the key reason, but there can be other reasons too).
* (Current) Version: 1.5 (Build Date: 15-Apr-2018)

### How do I get set up? ###

* The project is in the form of a Maven project. You can download the source code and build it using the Maven CLI. Use __mvn clean install__ command from the base directory for the same.
* The build will produce two jars, one each for the CLI and GUI versions of the tool. The jars can be located in the _target_ directory under the base directory. Copy the __putwb-cli-1.5.jar__ and __putwb-ui-1.5.jar__ files to a convenient location, and preferably add them to your class path.
* If you are using __Linux__ or any other Unix based system such as __Mac__, you may wish to add some aliases to point to these jars. For example, assuming that you've put the jar files in the _PUTWorkbench_ directory which in turn is located in your _$HOME_ directory, you may add the following lines to your _.bashrc_ or _.profile_ file:

  \# set alias for PUTWorkbench  
  alias putwb='java -jar $HOME/Programs/privacyws/putwb/target/putwb-cli-1.5.jar'  
  alias putwb-ui='java -jar $HOME/PUTWorkbench/putwb-ui-1.5.jar'  
  alias putwb-rec='java -cp $HOME/PUTWorkbench/putwb-cli-1.5.jar in.ac.iitk.cse.putwb.experiment.RecoveryManager'  
  alias putwb-ver='java -cp $HOME/PUTWorkbench/putwb-cli-1.5.jar in.ac.iitk.cse.putwb.experiment.Verifier'

  You can now access the GUI version of the tool with the command __putwb-ui__, the CLI version with the command __putwb__ and the newly added Recovery Manager using the command __putwb-rec__. Version 1.5 onwards, there is a Verifier CLI tool as well, to verify (sub)sets of results of an experiment over a different size of the original dataset. The tool can be accessed by the __putwb-ver__ command, if configured the way shown above. 
  
* If you are an Eclipse Developer (which is what we like ourselves to be called as !!), you can use the Egit and M2Eclipse plugins to make it easy for you to import the code. Go through this nerdy stackoverflow question for a little more details:
https://stackoverflow.com/questions/4869815/importing-a-maven-project-into-eclipse-from-git

### Who do I talk to? ###

* We are not yet sure how useful it is for you, but if you manage to appreciate it, or has a suggestion to improve over it (but too lazy to do it yourself), contact Saurabh Srivastava (mail: ssri AT cse DOT iitk DOT ac DOT in)