#!python3
import os
import sys
import subprocess
import time
import traceback
from plumbum import local, FG
from plumbum.cmd import mvn, cat, chmod
selenium_nashorn = local['selenium-nashorn']


def build():
    mvn['-DskipTests=true', 'compile', 'package', 'assembly:single', 'install'] & FG
    script = '''#!/bin/sh
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
exec java -classpath "$DIR/../jars/*"  -jar $0 "$@"


'''
    out_folder = os.environ['HOME'] + '/build-artifacts/' + os.environ['BUILD_NUMBER']
    if not os.path.isdir(out_folder):
        os.makedirs(out_folder)
    out_file = out_folder + '/stallion'
    with open(out_file, 'w') as f:
        f.write(script)
    cat['target/stallion-core-1.0-SNAPSHOT-jar-with-dependencies.jar'] >> out_file
    os.chmod(out_file, 0o700)
    
def run_selenium_for_build(build_number):
    stallion_exec = os.environ['HOME'] + '/build-artifacts/' + build_number + '/stallion'
    app_target_path = os.getcwd() + '/src/test/resources/a_minimal_site'
    print('Serve command: %s serve -targetPath=%s -logLevel=FINER -env=local' % (stallion_exec, app_target_path))
    print('Selenium command: selenium-nashorn selenium/test-users.js')
    p = subprocess.Popen([stallion_exec, 'serve', '-targetPath=' + app_target_path, '-logLevel=FINER', '-env=local'])
    try: 
        for x in range(0, 50):
            try: 
                r = requests.get('http://localhost:8090/')
            except:
                time.sleep(.1)
                continue
            print(r)
            if r.status_code == 200:
                break
            time.sleep(.1)
        selenium_nashorn['selenium/test-users.js'] & FG
    finally:
        p.terminate()
        p.wait()
    
    
if '--build' in sys.argv:
    build()
    run_selenium_for_build(os.environ['BUILD_NUMBER'])
    mvn['compile', '-DskipTests=true', 'install', 'source:jar', 'deploy'] & FG
elif '--selenium' in sys.argv:
    run_selenium_for_build(os.environ['BUILD_NUMBER'])
    pass

