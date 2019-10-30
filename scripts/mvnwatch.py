#!/usr/bin/python
from datetime import datetime
print "Starting mvnwatch at " + datetime.now().isoformat()
import os
import sys
import time
import logging
import subprocess
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

def main():
    path = os.getcwd() + "/src/main/java"
    maven_args = sys.argv[1:]
    print "Path ", path
    print "Args ", maven_args
    handler = MvnHandler(maven_args)
    handler.start_maven()
    observer = make_observer(handler, path)
    conf_path = os.getcwd() + "/../site/conf"
    if os.path.isdir(conf_path):
        observer2 = make_observer(handler, conf_path)
    
    try:
        time.sleep(100000000)
    finally:
        handler.kill_maven()

class MvnHandler(FileSystemEventHandler):
    def __init__(self, maven_args):
        self.maven_args = maven_args
        self.process = None

    def on_modified(self, event):
        self.restart_maven()

    def restart_maven(self):
        self.kill_maven()
        self.start_maven()
        
    def kill_maven(self):
        if self.process:
            self.process.terminate()
            print "Terminating maven"
            for x in xrange(0, 50):
                result = self.process.poll()
                if result != None:
                    break
                sys.stdout.write(".")
                sys.stdout.flush()
                time.sleep(.1)
                if x > 40:
                    print "Soft terminate failed. Killing maven"
                    self.process.kill()
        
        
    def start_maven(self):
        args = ['mvn'] + self.maven_args
        print "Run mvn at " + datetime.now().isoformat()                
        self.process = subprocess.Popen(args)

def make_observer(handler, path):
    observer = Observer()
    observer.schedule(handler, path, recursive=True)
    observer.start()
    return observer


main()
