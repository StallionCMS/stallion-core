#!/usr/bin/env python3


from datetime import datetime
import shutil
import os
import re
import sys
from plumbum import local, FG
from plumbum.cmd import mvn, cat, chmod

def main():
    build()
    copy()



        
def build():
    all = '--all' in sys.argv
    quiet = "-q"
    test = '-DskipTests'
    if "--verbose" in sys.argv or "-v" in sys.argv:
        quiet = ""
    should_test = None
    if "--test" in sys.argv:
        should_test = True
    if "--skip-tests" in sys.argv:
        should_test = False
    if should_test == None or should_test == True:
        test = 'test'
    
    if all or '--core' in sys.argv:
        print("Compiling st/core")
        os.chdir(os.environ['HOME'] + "/st/core")
        args = []
        if quiet:
            args.append(quiet)
        if test:
            args.append(test)
        args = [quiet, test, 'compile', 'assembly:single', 'install']
        mvn[tuple([a for a in args if a])] & FG
        print("Creating 'stallion' self-executing jar")
        # Write the self-executing jar
        script = '''#!/bin/sh
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
exec java -classpath "$DIR/../jars/*"  -jar $0 "$@"


'''
        with open(os.environ['HOME'] + '/st/core/target/stallion', 'w') as f:
            f.write(script)
        (cat[os.environ['HOME'] + '/st/core/target/stallion-core-1.0.2-beta-01-SNAPSHOT-jar-with-dependencies.jar'] >> os.environ['HOME'] + '/st/core/target/stallion') & FG
        os.chmod(os.environ['HOME'] + '/st/core/target/stallion', 0o700)
        if '--dist' in sys.argv:
            make_dist(os.environ['HOME'] + "/st/core", 'stallion')
        if '--deploy' in sys.argv:
            mvn['-DskipTests=true', 'install', 'source:jar', 'deploy'] & FG
            
            
    #if all or '--contacts' in sys.argv:        
    #    print "Compiling plugins/contacts"
    #    os.chdir(os.environ['HOME'] + "/st/plugins/contacts")
    #    !mvn $quiet $test compile install package
    #    check_fail()

    if all or '--blog' in sys.argv or '--flat' in sys.argv:
        print("Compiling plugins/flat-blog")
        path = os.environ['HOME'] + "/st/flat-blog"
        swap_pom_scope_and_build(path, quiet, test)
        if '--dist' in sys.argv:
            build_exec(path, '-complete', 'stallion-flat-blog-complete-.1-SNAPSHOT-jar-with-dependencies.jar', 'stallion-flat', quiet, '-DskipTests', True)
        #os.chdir()
        #!mvn $quiet $test compile assembly:single install package 

    if all or '--publisher' in sys.argv:
        print("Compiling publisher")
        path = os.environ['HOME'] + "/st/publisher"
        swap_pom_scope_and_build(path, quiet, test)
        if '--dist' in sys.argv:
            build_exec(path, '-complete', 'publisher-complete-1.1.0-SNAPSHOT-jar-with-dependencies.jar', 'stallion-publisher', quiet, '-DskipTests', True)
        if '--deploy' in sys.argv:
            os.chdir(path)
            mvn['-DskipTests=true', 'install', 'source:jar', 'deploy'] & FG
        #os.chdir()
        #!mvn $quiet $test compile assembly:single install package 

        

    #if all or '--comments' in sys.argv:
    #    print "Compiling plugins/comments"
    #    os.chdir(os.environ['HOME'] + "/st/plugins/comments")
    #    !mvn $quiet $test compile assembly:single install package
    #    check_fail()

def swap_pom_scope_and_build(path, quiet, test):
    os.chdir(path)
    
    with open(path + '/pom.xml') as f:
        org_xml = f.read()
        xml = org_xml.replace('<scope>compile</scope>', '<scope>provided</scope>')
    try:
        print("Packaging plugin version of " + path)
        with open(path + '/pom.xml', 'w') as f:
            f.write(xml)
        mvn[tuple([a for a in [quiet, test, 'compile', 'assembly:single', 'install'] if a])] & FG
    finally:
        with open(path + '/pom.xml', 'w') as f:
            f.write(org_xml)

def build_exec(path, postfix, jar_name, exec_name, quiet, test, should_make_dist):
    os.chdir(path)
    
    with open(path + '/pom.xml') as f:
        org_xml = f.read()
        xml = org_xml.replace('<scope>provided</scope>', '<scope>compile</scope>')
        xml = re.sub("(<artifactId>stallion-[^<]+)(</artifactId>)", r"\g<1>" + postfix + r"\g<2>", xml)
        xml = re.sub("(<artifactId>publisher)(</artifactId>)", r"\g<1>" + postfix + r"\g<2>", xml)
        xml = xml.replace('stallion-core-complete', 'stallion-core')
        xml = xml.replace('stallion-assets-pipeline-maven-plugin-complete', 'stallion-assets-pipeline-maven-plugin')
    f = None
    try:
        with open(path + '/pom.xml', 'w') as f:
            f.write(xml)
        print("Packaging single jar executable for " + path)
        mvn[tuple([a for a in [quiet, test, 'compile', 'assembly:single', 'install'] if a])] & FG
    finally:
        with open(path + '/pom.xml', 'w') as f:
            f.write(org_xml)
    script = '''#!/bin/sh
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
exec java -classpath "$DIR/../jars/*"  -jar $0 "$@"


'''
    exec_file = path + '/target/' + exec_name
    with open(exec_file, 'w') as f:
        f.write(script)
    (cat[path + '/target/' + jar_name]  >> exec_file) & FG
    if should_make_dist:
        make_dist(path, exec_name)
            
def make_dist(project_folder, exec_name):
    print('Make distribution for ' + exec_name)
    date_format = datetime.now().strftime('%Y-%m-%d')
    folder = '/Users/pfitzsimmons/st/distributions/' + date_format
    if not os.path.isdir(folder):
        os.makedirs(folder)
    shutil.copy(project_folder + '/target/' + exec_name, folder + '/' + exec_name)
    os.chmod(project_folder + '/target/' + exec_name, 0o700)
    os.chmod(folder + '/' + exec_name, 0o700)
    folder = '/Users/pfitzsimmons/st/distributions/dev/'
    if not os.path.isdir(folder):
        os.makedirs(folder)
    shutil.copy(project_folder + '/target/' + exec_name, folder + '/' + exec_name)        
    os.chmod(folder + '/' + exec_name, 0o700)
    
def copy():        
    print("Copy files")
    #!cp ~/st/plugins/comments/target/comments-1.0-SNAPSHOT-jar-with-dependencies.jar ~/st/www/jars/comments.jar
    #!cp ~/st/flat-blog/target/stallion-flat-blog-.1-SNAPSHOT-jar-with-dependencies.jar ~/st/www/jars/flat-blog.jar
    #!cp ~/st/plugins/contacts/target/stallion-contacts-1.0-SNAPSHOT.jar ~/st/www/jars/contacts.jar

    for folder in []:
        folder = folder.replace('~/', os.environ['HOME'] + '/')
        shutil.copy(os.environ['HOME'] + '/st/distributions/dev/stallion-publisher', folder + '/bin/stallion-publisher')
        
    for folder in ['~/repos/oldbooks', '~/repos/upfor', '~/repos/devinhelton', '~/st/sandbox-site', '~/repos/clubby-www', '~/repos/patfitzsimmons', '~/st/www']:
        folder = folder.replace('~/', os.environ['HOME'] + '/')
        shutil.copy(os.environ['HOME'] + '/st/core/target/stallion', folder + '/bin/stallion')

main()                
