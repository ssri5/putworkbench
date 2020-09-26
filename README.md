# README #

This repository has the source code for the Privacy-Utility Tradeoff Workbench project. The code is in Java, written over OpenJDK 8 (although it should work with JDK 7 as well).

### What is this repository for? ###

* The Privacy-Utility Tradeoff Workbench is a small tool that can be used for inferring important correlations among the various attributes in a domain. If due to any reasons, the architects are unwilling to use the all the parameters in a dataset for learning purposes, and are willing to try out a small subset of them for any practical ML application, this tool can help them find correlations which in turn can help in choosing a right set of attributes for large scale usage (the tool has been built using "privacy" as the key reason, but there can be other reasons too).
* (Current) Version: 1.62 (Build Date: 26-Sep-2020)

### How do I get set up? ###

#### I want to try the tool ####
* Download the tool for Windows (putwb-1.62-windows.zip) or Linux (putwb-1.62-linux.zip) from the Downloads section of the repository.  
* Install a JRE (Java Runtime Environment) if you don't have one already, preferably one for Java 8.
* Extract the contents of the Zip file to a folder of your choice. The tool doesn't require any installation as such, but there are certain things that an installation script can do for you, like adding the folder to the PATH variable in Windows or creating some aliases in Linux. Open the README file within the zip for more details.
* The UI tool has an **Autopilot** that can select some common settings for you, if you just wish to explore the tool over your dataset.

#### I want to contribute ####
* The repository is in the form of a Maven project. You can download the source code and build it using the Maven CLI. Use __mvn clean install__ command from the base directory for the same.
* The build will produce a jar file called __putwb-1.62-complete.jar__ (as well as some other files for platform dependent installations).
* The main classes of interest are __PUTExperiment__ (The CLI tool), __PUTWb__ (The UI tool), __RecoveryManager__ (An auxiliary tool for Recovery Management) and __Verifier__ (An auxiliary tool for results verification).  
* If you are an Eclipse Developer (which is what we like ourselves to be called as !!), you can use the Egit and M2Eclipse plugins to make it easy for you to import the code. Go through [this](https://stackoverflow.com/questions/4869815/importing-a-maven-project-into-eclipse-from-git) nerdy stackoverflow question for a little more details.

### Other Resources
* There is a [Quick Look](https://www.youtube.com/watch?v=xcPq8Y0ZeeM) video which can help you get started.
* The [Downloads](https://bitbucket.org/ssri5/putworkbench-maven/downloads/) Section has a brief User Manual that provides details of various features, as well as a zip file called _docs.zip_ containing the Javadoc files for the code.

### Who do I talk to? ###

* We are not yet sure how useful it is for you, but if you manage to appreciate it, or has a suggestion to improve over it (but too lazy to do it yourself), contact Saurabh Srivastava (mail: ssri AT cse DOT iitk DOT ac DOT in)
