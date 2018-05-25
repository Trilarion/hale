#!/bin/bash

java -Djava.library.path=lib/native -classpath hale*.jar:lib/*.jar net.sf.hale.Game
