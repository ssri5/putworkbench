# README #

This repository has the source code for the Privacy-Utility Tradeoff Workbench project. The code is in Java, written over OpenJDK 8 (although it should work with JDK 7 as well).

### What is this repository for? ###

* The Privacy-Utility Tradeoff Workbench is a small tool that can be used for infering important co-relations among the various attributes in a domain. If due to any reasons, the architects are unwilling to use the all the parameters in a dataset for learning purposes, and are willing to try out a small subset of them for any practical ML application, this tool can help them find co-relations which in turn can help in choosing a right set of attributes for large scale usage (the tool has been built using "privacy" as the key reason, but there can be other reasons too).
* (Current) Version: 1.31

### How do I get set up? ###

* The project is in the form of a Maven project. You can download the source code and build it using the Maven CLI.
* If you are an Eclipse Developer (which is what we like ourselves to be called as !!), you can use the Egit and M2Eclipse plugins to make it easy for you to import the code. Go through this nerdy stackoverflow question for a little more details:
https://stackoverflow.com/questions/4869815/importing-a-maven-project-into-eclipse-from-git

### Who do I talk to? ###

* We are not yet sure how useful it is for you, but if you manage to appreciate it, or has a suggestion to improve over it (but too lazy to do it yourself), contact Saurabh Srivastava (mail: ssri AT cse DOT iitk DOT ac DOT in)