#!/usr/bin/env python3
import os
import shutil
from plumbum import FG, local, TF
from plumbum.cmd import trash, mvn, cp

api_docs_folder = '/Users/pfitzsimmons/st/core/target/site/apidocs'
assets_docs_folder = '/Users/pfitzsimmons/st/www/assets/javadocs'
if os.path.isdir(api_docs_folder):
    trash[api_docs_folder] & FG
if os.path.isdir(assets_docs_folder):
    trash[assets_docs_folder] & FG
mvn['javadoc:javadoc'] & FG
cp['-R', '/Users/pfitzsimmons/st/core/target/site/apidocs', '/Users/pfitzsimmons/st/www/assets/javadocs'] & FG
