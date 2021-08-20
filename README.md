# CHATBOT Plugin Readme

Copy jar into the plugins directory of your Openfire installation. The plugin will then be
automatically deployed.

After installation, the functionality provided by the plugin is automatically available to clients. While exchanging
files, the plugin by default stores the files that are being transferred in a temporary directory that is removed
when Openfire is shut down. The content of this directory is purged when its total size is larger than the remaining
disc space.

# Build

- Install maven if not allready done.
- Change to plugin root directory.
- Use "mvn package" command to build.
- The result can be found in the target directory: 
- Rename chatbot-openfire-plugin-assembly.jar to chatbot.jar and use/install it into Openfire.