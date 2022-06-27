#!/usr/bin/python

import sys, os, os.path
import subprocess
import re
import fileinput

if len(sys.argv) != 2:
    print('usage: %s server-prefix' % (sys.argv[0]))
    sys.exit(1)

server_prefix = sys.argv[1]
#server_prefix = 'Test'

#if not 'EC2_HOME' in os.environ:
#    print('Missing EC2_HOME environment variable')
#    sys.exit(1)

EC2_HOME = None
if 'EC2_HOME' in os.environ:
    EC2_HOME = os.environ['EC2_HOME']
    cmd = '%s/bin/ec2-describe-instances' % EC2_HOME
else:
    cmd = 'ec2-describe-instances'

proc = subprocess.Popen([cmd], stdout=subprocess.PIPE)
stdout, stderr = proc.communicate()

if proc.returncode != 0:
    print('Error running ec2-describe-instances')
    sys.exit(1)

#with open('/tmp/instances.txt') as f:
#    stdout = f.read()

instance_details = stdout.split('\n')

server_names = []
server_urls = {}
found_instance = False
for instance_line in instance_details:
    if instance_line.startswith('INSTANCE'):
        instance_data = instance_line.split('\t')
        instance_id = instance_data[1]
        instance_url = instance_data[3]
        instance_state = instance_data[5]
        if instance_state == 'running':
            found_instance = True
    elif instance_line.startswith('TAG'):
        if found_instance:
            tag_data = instance_line.split('\t')
            tag_instance_id = tag_data[2]
            tag_name = tag_data[3]
            tag_value = tag_data[4]
            if tag_name == 'Name' and tag_instance_id == instance_id and tag_value.startswith(server_prefix):
                server_names.append(tag_value)
                server_urls[tag_value] = instance_url
        found_instance = False

urls = dict()
server_names.sort()
for server_name in server_names:   
    url = "'%s',\t\t# %s" % (server_urls[server_name], server_name)
    print url
    urls[server_name] = url

for line in fileinput.input("fabfile.py", inplace=1): 
    if re.search(".*\.compute-1\.amazonaws\.com.*#", line):
        serverName = line.split('#')[1].strip()
        if urls.has_key(serverName):
            line = "        " + urls[serverName] + "\n"
    print line,




