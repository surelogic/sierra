---------------------
-- Sierra Ant Task --
---------------------

This Ant task can scan a project and produce Sierra results. It is, by design
intended to be similar to the javac Ant task. This task results in a Zip
file that can be loaded into Eclipse to examine its results.

Requirements: Ant 1.7 (or higher) running on a Java 7 (or higher) JRE

NOTE in the examples below you have to change sierra.ant.home to point
to your unzipped "sierra-ant" directory (the directory this README.txt
is located within).

-- Reference --

The "sierra-scan" task is similar to the Ant javac task so you specify the
source and build classpath per that task. You do not need to specify a destdir.

Attributes added by sierra-scan task are:

o "projectname" (required) set this to the name of your project. This
   value is used to name the resulting Zip file
   e.g., projectname="SierraTutorial_SmallWorld"

o "sierraanthome" (required) set this to the location of this task. Typically
   you copy the pattern illustrated in the Ant script below and set this
   path as the property "sierra.ant.home" so that it can be used to specify
   the task classpath as well as the value of this attribute.
   e.g., <property name="sierra.ant.home" location="C:\\Users\\Tim\\sierra-ant" />
         ...
         sierraanthome="${sierra.ant.home}"
         
o "sierrascandir" (optional) This sets a directory to create the scan Zip file
   within. If it is not set then output is written to the current directory.
   This may be useful if you want to gather results in a particular location
   on your disk.
   e.g., sierrascandir="C:\\Users\\Tim\\myscans"
   
o "surelogictoolspropertiesfile" (optional) This sets the location of a
  'surelogic-tools.properties' file to be read to control the scan. This file
  can control aspects of the Sierra scan (please see the Sierra documentation).
  If this attribute is not set the tool looks for a 'surelogic-tools.properties'
  file in the current directory and uses that if it is found.
  e.g., surelogictoolspropertiesfile="C:\\Users\\Tim\\surelogic-tools.properties"

-- Example --

For the SierraTutorial_SmallWorld project create a build.xml at the
project root:

<?xml version="1.0" encoding="UTF-8"?>
<project name="SierraTutorial_SmallWorld" default="scan" basedir=".">

  <!-- (CHANGE) path to the unzipped SureLogic Ant tasks -->
  <property name="sierra.ant.home" location="C:\\Users\\Tim\\sierra-ant" />

  <!-- (COPY) SureLogic Ant task setup stuff -->
  <path id="sierra-ant.classpath">
    <dirset  dir="${sierra.ant.home}" includes="lib/com.surelogic.*" />
    <fileset dir="${sierra.ant.home}" includes="lib/**/*.jar" />
  </path>
  <taskdef name="sierra-scan" classname="com.surelogic.sierra.ant.SierraScan">
    <classpath refid="sierra-ant.classpath" />
  </taskdef>

  <path id="tf.classpath">
    <fileset dir="${basedir}" includes="**/*.jar" />
  </path>

  <target name="scan">
    <javac srcdir="${basedir}/src"
           destdir="${basedir}/bina"
           source="1.7"
           includeantruntime="false">
       <classpath refid="tf.classpath" />
    </javac>

    <!-- Make sure 'bina' is populated with class files for FindBugs -->

    <sierra-scan srcdir="${basedir}/src"
                 destdir="${basedir}/bina"
                 source="1.7"
                 includeantruntime="false"
                 sierraanthome="${sierra.ant.home}"
                 projectname="SierraTutorial_SmallWorld">
       <classpath refid="tf.classpath" />
    </sierra-scan>
  </target>
</project>


Note we include a javac compile to illustrate how this is similar (and different)
from a sierra-scan and also because FindBugs wants the generated .class files to
be available to it to examine (it is a binary analysis tool).

To run a scan open a prompt to this directory and run "ant" or
run as Ant task in your Eclipse. The output will look like

{ohio:/cygdrive/c/Users/Tim/Source/eclipse-work/meta-work/SierraTutorial_SmallWorld}ant
Buildfile: C:\Users\Tim\Source\eclipse-work\meta-work\SierraTutorial_SmallWorld\build.xml

scan:
    [javac] Compiling 15 source files to C:\Users\Tim\Source\eclipse-work\meta-work\SierraTutorial_SmallWorld\bina
    [javac] warning: [options] bootstrap class path not set in conjunction with -source 1.7
    [javac] 1 warning
[sierra-scan] Project to scan w/Sierra = SierraTutorial_SmallWorld
[sierra-scan] Scan output directory    = .

BUILD SUCCESSFUL
Total time: 8 seconds
{ohio:/cygdrive/c/Users/Tim/Source/eclipse-work/meta-work/SierraTutorial_SmallWorld}ls
SierraTutorial_SmallWorld.2015.06.30-at-12.26.48.481.sierra-scan.zip  bin/  bina/  build.xml  jdom-1.0.jar  src/
{ohio:/cygdrive/c/Users/Tim/Source/eclipse-work/meta-work/SierraTutorial_SmallWorld}

Next you can load the 'SierraTutorial_SmallWorld.2015.06.30-at-12.26.48.481.sierra-scan.zip' file into
your Eclipse by choosing the "Sierra" -> "Import Ant/Maven Scan..." menu item from the
Eclipse main menu. The file is located at the root of the SierraTutorial_SmallWorld
project on your disk.

#