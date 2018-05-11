PREREQUISITES
- ant
- java sdk 1.8

LAYOUT
./                      : dadadj developer base
./bin                   : build object files
./content               : static content 
./content/config        : base/template config files
./content/content       : web/REST content files and templates for installed server
./content/content-dev   : web/REST content files and templates for dev server
./content/doc           : various documentation and licenses
./content/script        : scripts and helpers
./content/test          : scripts, configs and files for testing install
./content/MANIFEST.MF   : java package manifest.  It sets the classpath for lib jars.
./content/README.md     : readme for distribution
./content/*.sh          : various operation scripts.  
./content/server.prop   : default server configuration
./dist                  : binary distribution
./lib                   : third party libraries
./meta                  : various helpers and docker stuff
./src                   : source code
./test                  : test distribution
./build.xml             : ant build file
./environement.sh       : ignore this...
./erich.notes           : various notes for my work
./package.sh            : creates package files from distributions
./README.md             : this file
./shippable.yaml        : shippable.com configuration file
./VERSION               : released version number

SETUP
Before running any scripts from a new shell, be sure to run "source setup.sh" from the install
directory (which is either dist/ or test/ for the development environment).

BUILDING

