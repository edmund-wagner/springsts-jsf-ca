# JSF Content Assist for spring beans in eclipse

## Installation

prerequisites:
- sts eclipse 2.8.1 oder eclipse 3.7.2 jee with installed spring ide


1. download the plugin and copy it to your eclipse's dropins folder 
2. restart eclipse (safety first ;)
3. add your spring contexts to the project (project/properties/spring/beans support -> config files

## Additional Config

you can specify additional beans (without @component etc..) to be suggested in the jsfca pref page (window/preferences/jsfca)

define the base package to be scanned and a java regex for the class name.

eg:  "com.me.project.model" and  ".*Model"
