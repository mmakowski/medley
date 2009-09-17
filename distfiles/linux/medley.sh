#!/bin/sh

# adjust Mozilla path below if it does not match your setup
export MOZILLA_FIVE_HOME=/usr/lib/mozilla

# in case you get an error message saying that libxpcom.so can not be found
# uncomment the line below and adjust path to one where libxpcom.so is 
# in your system
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/lib/mozilla-firefox

# start Medley
java -Djava.library.path=lib -classpath lib/medley.jar:lib/mmakowski-java.jar:lib/mmakowski-swt.jar:lib/jargs.jar:lib/hsqldb.jar:lib/swt.jar:lib/swt-cairo.jar:lib/swt-pi.jar:lib/swt-mozilla.jar com.mmakowski.medley.Medley $@ &
