To create a Sierra tool plugin for a typical third-party tool:
1. Creata an Eclipse plugin project without an Activator class

   Alternatively, you can copy this project and customize META-INF/MANIFEST.MF

2. Copy the appropriate library jars to the project, and add them to the Runtime Classpath
   (via the Plugin Manifest Editor)
   
3. Rename the package "sierra_tool_template" appropriately

4. Edit Factory.java, filling in the various TODOs
