from __future__ import with_statement

from fabric.api import *
from fabric.utils import *
from fabric.contrib.console import confirm
from fabric.contrib.files import append, exists
from fabric.decorators import roles
from fabric.network import NetworkError
from subprocess import call

import os.path, glob, os, uuid, socket, thread
from traceback import print_exc

# more for rewire and scale
# from rewire_cluster_config import *
import tty, termios  # for keyboard
import aws_signed_request
import urllib2
import xml.etree.ElementTree as ET
import xml.dom.minidom
import re  # regular expression to remove the namespace from the xml amazon sends us
import json  # used to print the runningInstancesList nicely formatted
from cStringIO import StringIO
from gzip import GzipFile
import time
import sys
import curses

# from datetime import date
from datetime import datetime
# from twisted.python._release import runCommand
from types import InstanceType
from sys import stdout
import subprocess

USE_SUDO = True

env.hosts = []

if env.user != 'meinc' and env.user != 'ubuntu':
    env.user = 'meinc'
env.skip_bad_hosts = True
env.disable_known_hosts = True
env.use_ssh_config = True
env.always_use_pty = False

env.roledefs['prod-web'] = [
        'web1.shout.tv',
        #'web2.shout.tv',
        #'web3.shout.tv',
        ]
env.roledefs['prod-sync'] = [
        'sync1.shout.tv',
#        'sync2.shout.tv',
#        'sync3.shout.tv',
#        'sync4.shout.tv',
        'auth1.shout.tv',
#        'auth2.shout.tv',
        ]
env.roledefs['prod-collector'] = [
        'collector1.shout.tv',
#        'collector2.shout.tv',
        ]

env.roledefs['dc1-web'] = [
        'dc1-web1.shoutgameplay.com',
        ]
env.roledefs['dc1-sync'] = [
        'dc1-sync1.shoutgameplay.com',
        'dc1-sync2.shoutgameplay.com',
        #'dc1-sync3.shoutgameplay.com',
        #'dc1-sync4.shoutgameplay.com',
        'dc1-auth1.shoutgameplay.com',
        'dc1-auth2.shoutgameplay.com',
        ]
env.roledefs['dc1-collector'] = [
        'dc1-collector1.shoutgameplay.com',
        'dc1-collector2.shoutgameplay.com',
        ]

env.roledefs['dc11-web'] = [
        'dc11-web1.shoutgameplay.com',
        ]

env.roledefs['dc4-all'] = [
        'dc4-auth1.shoutgameplay.com',
        'dc4-auth2.shoutgameplay.com',
        'dc4-sync1.shoutgameplay.com',
        'dc4-sync2.shoutgameplay.com',
        'dc4-collector1.shoutgameplay.com',
        'dc4-collector2.shoutgameplay.com',
        ]
env.roledefs['dc4-collector'] = [
        'dc4-collector1.shoutgameplay.com',
        #'dc4-collector2.shoutgameplay.com',
        ]
env.roledefs['dc99-web'] = [
        'dc99-web1.shoutgameplay.com',
        ]
env.roledefs['dc99-sync'] = [
        'dc99-sync1.shoutgameplay.com',
        #'dc99-sync2.shoutgameplay.com',
        #'dc99-sync3.shoutgameplay.com',
        #'dc99-sync4.shoutgameplay.com',
        'dc99-auth1.shoutgameplay.com',
        #'dc99-auth2.shoutgameplay.com',
        ]
env.roledefs['dc99-collector'] = [
        'dc99-collector1.shoutgameplay.com',
        #'dc99-collector2.shoutgameplay.com',
        ]
env.roledefs['dc99-all'] = env.roledefs['dc99-sync'][:] + env.roledefs['dc99-collector'][:]

env.roledefs['stage-web'] = [
        'web1.shoutgameplay.com',
        #'web2.shoutgameplay.com',
        #'web3.shoutgameplay.com',
        ]
env.roledefs['stage-sync'] = [
        'sync1.shoutgameplay.com',
        'sync2.shoutgameplay.com',
        'sync3.shoutgameplay.com',
        'sync4.shoutgameplay.com',
        'auth1.shoutgameplay.com',
        'auth2.shoutgameplay.com',
        ]
env.roledefs['stage-collector'] = [
        'collector1.shoutgameplay.com',
        'collector2.shoutgameplay.com',
        ]

env.roledefs['vm'] = [
        'foo.meinc'
        ]

env.roledefs['lt1-client'] = [
        'lt1-client1.shoutgameplay.com',
        'lt1-client2.shoutgameplay.com',
        'lt1-client3.shoutgameplay.com',
        'lt1-client4.shoutgameplay.com',
        'lt1-client5.shoutgameplay.com',
        'lt1-client6.shoutgameplay.com',
        'lt1-client7.shoutgameplay.com',
        ]
env.roledefs['lt1-collector'] = [
        'lt1-collector1.shoutgameplay.com',
        #'lt1-collector2.shoutgameplay.com',
        ]
env.roledefs['lt1-db'] = [
        'lt1-db.shoutgameplay.com',
        ]
env.roledefs['lt1-desktop'] = [
        'lt1-desktop.shoutgameplay.com',
        ]
env.roledefs['lt1-sync'] = [
        'lt1-sync1.shoutgameplay.com',
        #'lt1-sync2.shoutgameplay.com',
        #'lt1-sync3.shoutgameplay.com',
        #'lt1-sync4.shoutgameplay.com',
        'lt1-auth1.shoutgameplay.com',
        #'lt1-auth2.shoutgameplay.com',
        ]
env.roledefs['lt1-sync-only'] = [
        'lt1-sync1.shoutgameplay.com',
        'lt1-sync2.shoutgameplay.com',
        'lt1-sync3.shoutgameplay.com',
        'lt1-sync4.shoutgameplay.com',
        ]
env.roledefs['lt1-web'] = [
        'lt1-web1.shoutgameplay.com',
        ]
env.roledefs['lt1-all'] = env.roledefs['lt1-sync'][:] + env.roledefs['lt1-collector'][:]

env.roledefs['lt2-client'] = [
        'lt2-client1.shoutgameplay.com',
        ]
env.roledefs['lt2-collector'] = [
        'lt2-collector1.shoutgameplay.com',
        ]
env.roledefs['lt2-db'] = [
        'lt2-db.shoutgameplay.com',
        ]
env.roledefs['lt2-desktop'] = [
        'lt2-desktop.shoutgameplay.com',
        ]
env.roledefs['lt2-sync'] = [
        'lt2-sync1.shoutgameplay.com',
        'lt2-auth1.shoutgameplay.com',
        'lt2-auth2.shoutgameplay.com',
        ]
env.roledefs['lt2-web'] = [
        'lt2-web1.shoutgameplay.com',
        ]
env.roledefs['test-collector'] = [
        'ec2-54-224-4-173.compute-1.amazonaws.com',		# Test Collector 1
        ]
env.roledefs['test-db'] = [
        'ec2-54-227-222-137.compute-1.amazonaws.com',		# Test DB
        ]
env.roledefs['test-desktop'] = [
        'ec2-54-211-114-39.compute-1.amazonaws.com',		# Test Desktop
        ]
env.roledefs['test-sync'] = [
        'ec2-54-242-47-118.compute-1.amazonaws.com',		# Test Sync 1
        ]
env.roledefs['test-web'] = [
        'ec2-23-20-9-234.compute-1.amazonaws.com',		# Test Web 1
        ]

PRODUCTION_ROLE_PREFIX = 'prod'
STAGING_ROLE_PREFIX = 'stage'

productionPrompt = False
for role in env.roles:
    if role.startswith(PRODUCTION_ROLE_PREFIX+'-'):
        productionPrompt = True
        break
if not productionPrompt and env.hosts:
    for (roleName, roleHosts) in env.roledefs.iteritems():
        if roleName.startswith(PRODUCTION_ROLE_PREFIX+'-'):
            for host in env.hosts:
                for roleHost in roleHosts:
                    if roleHost == host:
                        productionPrompt = True
                        break
                if productionPrompt: break
        if productionPrompt: break
if productionPrompt:
    print ''
    print '================================='
    print ' DEPLOYING TO PRODUCTION SERVER! '
    print '================================='
    print ''
    if 'skip_prod_prompt' not in env:
        response = prompt('Are you sure? (Yes/No): ')
        if response != 'Yes':
            print '\nAborting'
            sys.exit(1)

LOCAL_WORKSPACE_PATH = os.path.abspath('../../')
print '\n**** Auto-detected workspace path: %s\n' % LOCAL_WORKSPACE_PATH
LOCAL_INSTALL_SERVER_PATH = os.path.expanduser('~/Dev/install_server')
LOCAL_CERT_PATH = os.path.expanduser('~/Dev/meinc_ssl')

# Used only for Grinder Analyzer
LOCAL_JYTHON_PATH = os.path.expanduser('~/Dev/jython2.5.2')
LOCAL_GRINDER_ANALYZER_PATH = os.path.expanduser('~/Dev/GrinderAnalyzer.V2.b16')

#UBUNTU_PARTNER_DEB_REPO     = 'deb http://archive.canonical.com/ubuntu lucid partner'
#UBUNTU_PARTNER_DEB_SRC_REPO = 'deb-src http://archive.canonical.com/ubuntu lucid partner'
#UBUNTU_SUN_JAVA6_PKG        = 'sun-java6-jre'

UBUNTU_APACHE2_PKG = 'apache2'
LOCAL_APACHE_CONFIG_FILES = [os.path.join(LOCAL_INSTALL_SERVER_PATH, 'apache2.conf'),
                             os.path.join(LOCAL_INSTALL_SERVER_PATH, 'httpd.conf'),
                             os.path.join(LOCAL_INSTALL_SERVER_PATH, 'jkworkers.properties'),
                             os.path.join(LOCAL_INSTALL_SERVER_PATH, 'ports.conf')]
LOCAL_APACHE_SITES_FILES  = [os.path.join(LOCAL_INSTALL_SERVER_PATH, 'shoutgameplay'),
                             os.path.join(LOCAL_INSTALL_SERVER_PATH, 'shoutgameplay-ssl'),
                             os.path.join(LOCAL_INSTALL_SERVER_PATH, 'shouttv'),
                             os.path.join(LOCAL_INSTALL_SERVER_PATH, 'shouttv-ssl')]

UBUNTU_MRSOA_PKG = 'mrsoa-server'
LOCAL_MRSOA_DEB = os.path.join(LOCAL_INSTALL_SERVER_PATH, 'mrsoa-server_3.6.1-2_all.deb')

UBUNTU_TOMCAT_PKG = 'mrsoa-tomcat'
LOCAL_TOMCAT_DEB = os.path.join(LOCAL_INSTALL_SERVER_PATH, 'mrsoa-tomcat_7.0.27-1_all.deb')

UBUNTU_SWATCH_PKG = 'meinc-swatch'
LOCAL_SWATCH_DEB = os.path.join(LOCAL_INSTALL_SERVER_PATH, 'meinc-swatch_3.2.3-1_all.deb')

GRINDER_DEB        = 'thegrinder_3.7.1_all.deb'
LOCAL_GRINDER_PATH = os.path.join(LOCAL_INSTALL_SERVER_PATH, GRINDER_DEB)
GRINDER_PKG        = 'thegrinder'

UBUNTU_MODJK_PKG = 'libapache2-mod-jk'
LOCAL_MODJK_DEB = os.path.join(LOCAL_INSTALL_SERVER_PATH, 'libapache2-mod-jk-meinc_1.2.28-2_amd64.deb')

REPO_PATH = os.path.expanduser('~/.m2/repository')
LOCAL_GRINDER_EXTS_PATHS = [REPO_PATH+'/com/hazelcast/hazelcast/2.1/hazelcast-2.1.jar']

LOCAL_JCE_POLICY_ARCHIVE = 'UnlimitedJCEPolicyJDK7.zip'

_local = local

ssl_pw = ''
if 'SSL_PW' in os.environ:
    ssl_pw = os.environ['SSL_PW']

def tobool(val):
    if type(val) is bool:
        return val
    if type(val) is str:
        return val != '0' and val.lower() != 'false'
    return bool(val)

def isLocal(**kwargs):
    return 'local' in kwargs

@task
def config_limits():
    sudo(r"sed -i.bak -e '/^fs\.file-max\b/ d' /etc/sysctl.conf")
    append('/etc/sysctl.conf', 'fs.file-max = 983025', use_sudo=True)
    sudo('sysctl -p')
    sudo(r"sed -i.bak -e '/ nofile\|stack / d' /etc/security/limits.conf")
    append('/etc/security/limits.conf',
            ['root         -       nofile          65535',
             'mrsoa        -       nofile          65535',
             'www-data     -       nofile          65535',
             'root         -       stack           65535',
             'mrsoa        -       stack           65535',
             'www-data     -       stack           65535',
             'root         -       nproc           4096',
             'mrsoa        -       nproc           4096',
             'www-data     -       nproc           4096'], use_sudo=True)

@task
def config_user(sudouser='ubuntu', user='meinc', force=False):
    oldUser = env.user
    env.user = sudouser
    try:
        userExists = False
        with settings(hide('warnings','stdout'), warn_only=True):
            result = run('id %s' % user)
            if result and result.find('No such user') == -1:
                puts('User %s already exists' % user)
                userExists = True
                if not tobool(force):
                    return

        if not userExists:
            sudo('adduser --disabled-password --gecos "" %s' % user)

        #sudo("usermod -p '$6$Lz0UT50I$ODEtb2YsaboaM/v8hWzpwhkYt3Wz.ygdkBHEgkr8ZOiU6hnWNQJvEWTF0q1.V554sKd7pDpcCCeCVAPWvJ4hP0' %s" % user)
        sudo("usermod -p '$6$rqBpwU7K$36ThV/EFv.AWIyaFW.jukE/n.kjsbaCUYOmP83W2sUByxlP6.nyRlMzbZyoXvOsvQICuul4edJBgXxV3zcThE1' %s" % user)
        sudo("usermod -aG admin %s" % user)

        append('/etc/ssh/sshd_config', 'ClientAliveInterval 60', use_sudo=True, partial=False)
        #append('/etc/ssh/sshd_config', ['Match User %s' % user, 'PasswordAuthentication yes'], use_sudo=True)
        sudo('/etc/init.d/ssh reload')
    finally:
        env.user = oldUser

@task
def install_ssh_key(sudouser='ubuntu', user=env.user):
    oldUser = env.user
    env.user = sudouser
    try:
        sshPubKeyFiles = glob.glob(os.path.expanduser('~/.ssh/*.pub'))
        if not sshPubKeyFiles:
            abort('No public keys found in ' + os.path.expanduser('~/.ssh/'))
        pubKeyFile = open(sshPubKeyFiles[0], 'r')
        pubKey = pubKeyFile.read()
        userHomeDir = '/home/%s' % user
        remoteKeysFile = '%s/.ssh/authorized_keys' % userHomeDir
        sudo('mkdir -p %s/.ssh' % userHomeDir, user=user)
        sudo('chmod 700 %s/.ssh' % userHomeDir, user=user)
        if not exists(remoteKeysFile, use_sudo=True):
            sudo('touch '+remoteKeysFile, user=user)
        append(remoteKeysFile, pubKey, use_sudo=True)
        sudo('chmod 600 '+remoteKeysFile, user=user)
    finally:
        env.user = oldUser

@task
def config_instant_sudoer(user='meinc'):
    append('/etc/sudoers', '%s  ALL=(ALL) NOPASSWD:ALL' % user, use_sudo=True)

@task
def set_hostname():
    hn = ''
    while hn == '':
        hn = prompt('Hostname:')
    append('/etc/hosts', ('127.0.1.1\t%s' % hn), use_sudo=True, partial=False)
    sudo('echo "%s" > /etc/hostname' % hn)
    sudo('/etc/init.d/hostname restart')

#@task
#def install_java():
#    with settings(hide('warnings','stdout'), warn_only=True):
#        result = run('dpkg -s ' + UBUNTU_SUN_JAVA6_PKG)
#    if result and result.split('\n')[1] == 'Status: install ok installed':
#        puts('Sun\'s JRE 6 already installed')
#    else:
#        puts('\n\nInstalling Sun\'s JRE 6\n')
#        append('/etc/apt/sources.list', [UBUNTU_PARTNER_DEB_REPO, UBUNTU_PARTNER_DEB_SRC_REPO],
#                use_sudo=True, partial=False)
#        with hide('stdout'):
#            sudo('apt-get -qy update', pty=True)
#        # Avoid license prompts -- yes, we accept them
#        sudo('echo "sun-java6-bin   shared/accepted-sun-dlj-v1-1    boolean true" | debconf-set-selections')
#        sudo('echo "sun-java6-jre   shared/accepted-sun-dlj-v1-1    boolean true" | sudo debconf-set-selections')
#        sudo('apt-get -qy install ' + UBUNTU_SUN_JAVA6_PKG, pty=True)

@task
def install_java():
    append('/etc/apt/sources.list', 'deb http://www.duinsoft.nl/pkg debs all', use_sudo=True)
    sudo('apt-key adv --keyserver keys.gnupg.net --recv-keys 5CB26B26')
    sudo('apt-get update', pty=True)
    sudo('apt-get -qy install update-sun-jre', pty=True)

    alias_java()

    put(os.path.join(LOCAL_INSTALL_SERVER_PATH, LOCAL_JCE_POLICY_ARCHIVE), '/tmp')

    with settings(hide('warnings','stdout'), warn_only=True):
        result = sudo('dpkg -s unzip')
    if result and result.split('\n')[1] == 'Status: install ok installed':
        puts('Unzip already installed')
    else:
        sudo('apt-get -qy install unzip', pty=True)

    sudo('JSEC="$(dirname $(readlink -f $(which java)))/../lib/security" && unzip -j -o /tmp/%s \*.jar -d "$JSEC"' % LOCAL_JCE_POLICY_ARCHIVE)

@task
def alias_java():
    with settings(hide('warnings'), warn_only=True):
        sudo('J=`which java` && J2=`readlink -f "$J"` && J3=`dirname "$J2"` && cd "$J3" && ln java mrsoa_java')

@task
def install_shouttv_certs():
    put(os.path.join(LOCAL_CERT_PATH, 'shout.tv.crt'), '/tmp')
    sudo('chown root.root /tmp/shout.tv.crt')
    sudo('mv /tmp/shout.tv.crt /etc/ssl/certs')

    put(os.path.join(LOCAL_CERT_PATH, 'sf_bundle.crt'), '/tmp')
    sudo('chown root.root /tmp/sf_bundle.crt')
    sudo('mv /tmp/sf_bundle.crt /etc/ssl/certs')

    put(os.path.join(LOCAL_CERT_PATH, 'shout.tv-3des.key'), '/tmp/shouttv_ssl.key')
    sudo('chown root.root /tmp/shouttv_ssl.key')
    sudo('chmod 640 /tmp/shouttv_ssl.key')
    sudo('mv /tmp/shouttv_ssl.key /etc/ssl/private')


@task
def install_apache():
    with settings(hide('warnings','stdout'), warn_only=True):
        result = run('dpkg -s ' + UBUNTU_APACHE2_PKG)
    if result and result.split('\n')[1] == 'Status: install ok installed':
        puts('Apache already installed')
    else:
        puts('\n\nInstalling Apache\n')

        put(os.path.join(LOCAL_CERT_PATH, 'shoutgameplay.com.crt'), '/tmp')
        sudo('mv /tmp/shoutgameplay.com.crt /etc/ssl/certs')

        put(os.path.join(LOCAL_CERT_PATH, 'sf_bundle.crt'), '/tmp')
        sudo('mv /tmp/sf_bundle.crt /etc/ssl/certs')

        put(os.path.join(LOCAL_CERT_PATH, 'shoutgameplay.com-3des.key'), '/tmp/meinc_ssl.key')
        sudo('mv /tmp/meinc_ssl.key /etc/ssl/private')

        sudo('apt-get -qy install ' + UBUNTU_APACHE2_PKG, pty=True)

        #put(LOCAL_MODJK_DEB, '/tmp')
        #with settings(hide('warnings','stdout'), warn_only=True):
        #    sudo('apt-get -qy remove ' + UBUNTU_MODJK_PKG, pty=True)
        #sudo('dpkg -i /tmp/' + os.path.basename(LOCAL_MODJK_DEB), pty=True)

        #sudo('apt-get -qy install ' + UBUNTU_MODJK_PKG, pty=True)

        config_apache()

        #global ssl_pw
        #pw = ssl_pw
        #while pw == '':
        #    pw = ssl_pw = prompt('SSL Password:')
        ##TODO: secure pw better, may show up to anyone running ps or in the
        ##output to the fabric console
        #sudo('echo "%s" | service apache2 restart' % pw)

@task
def install_empty():
    with settings(hide('warnings'), warn_only=True):
        sudo('apt-get -qy install empty-expect')

@task
def config_apache():
    sudo('apt-get -qy install ' + UBUNTU_MODJK_PKG, pty=True)
    #sudo('a2enmod jk')
    sudo('a2enmod proxy')
    sudo('a2enmod proxy_http')
    sudo('a2enmod expires')
    sudo('a2enmod headers')
    sudo('a2enmod rewrite')
    sudo('a2enmod ssl')

    for lFile in LOCAL_APACHE_CONFIG_FILES:
        put(lFile, '/tmp')
        rFile = os.path.basename(lFile)
        with settings(hide('warnings','stdout'), warn_only=True):
            sudo('cp -n /etc/apache2/%s /etc/apache2/%s.orig' % (rFile, rFile))
        sudo('chown root.root /tmp/%s' % rFile)
        sudo('mv /tmp/%s /etc/apache2/' % rFile)

    for lFile in LOCAL_APACHE_SITES_FILES:
        put(lFile, '/tmp')
        rFile = os.path.basename(lFile)
        sudo('chown root.root /tmp/%s' % rFile)
        sudo('mv /tmp/%s /etc/apache2/sites-available' % rFile)
        with settings(hide('warnings','stdout'), warn_only=True):
            sudo('ln -s /etc/apache2/sites-available/%s /etc/apache2/sites-enabled/%s' % (rFile, rFile))

    install_shouttv_certs()

@task
def install_meinc_props():
    sudo('cp -n /opt/meinc/meinc.properties /opt/meinc/meinc.properties.orig')
    put(os.path.join(LOCAL_INSTALL_SERVER_PATH, 'meinc.properties'), '/tmp')
    sudo('mv /tmp/meinc.properties /opt/meinc/')
    sudo('chown meinc:mrsoa /opt/meinc/meinc.properties*')
    sudo('chmod 640 /opt/meinc/meinc.properties*')

@task
def install_mrsoa(force=False):
    with settings(hide('warnings','stdout'), warn_only=True):
        result = sudo('dpkg -s ' + UBUNTU_MRSOA_PKG)
    if result and result.split('\n')[1] == 'Status: install ok installed':
        puts('MrSOA already installed')
        doInstall = False
    else:
        doInstall = True

    if doInstall or tobool(force):
        puts('\n\nInstalling MrSOA\n')
        put(LOCAL_MRSOA_DEB, '/tmp')
        with settings(hide('warnings','stdout'), warn_only=True):
            sudo('apt-get -qy remove ' + UBUNTU_MRSOA_PKG, pty=True)
        sudo('dpkg -i /tmp/' + os.path.basename(LOCAL_MRSOA_DEB), pty=True)
        install_meinc_props()

@task
def install_tomcat(force=False):
    with settings(hide('warnings','stdout'), warn_only=True):
        result = run('dpkg -s ' + UBUNTU_TOMCAT_PKG)
    if result and result.split('\n')[1] == 'Status: install ok installed':
        puts('Tomcat already installed')
        doInstall = False
    else:
        doInstall = True

    if doInstall or tobool(force):
        puts('\n\nInstalling Tomcat\n')
        put(LOCAL_TOMCAT_DEB, '/tmp')
        with settings(hide('warnings','stdout'), warn_only=True):
            sudo('apt-get -qy remove ' + UBUNTU_TOMCAT_PKG, pty=True)
        sudo('apt-get -qy install authbind', pty=True)
        sudo('dpkg -i /tmp/' + os.path.basename(LOCAL_TOMCAT_DEB), pty=True)

        #with settings(hide('warnings','stdout'), warn_only=True):
        #    sudo('cp -n /opt/meinc/tomcat/conf/server.xml /opt/meinc/tomcat/conf/server.xml.orig')
        #put(LOCAL_INSTALL_SERVER_PATH+'/server.xml', '/tmp')
        #sudo('mv /tmp/server.xml /opt/meinc/tomcat/conf/server.xml')

        jre_path = '/opt/java/64/' + run('ls -1 /opt/java/64/ | tail -n1')
        puts("Using JAVA_HOME of " + jre_path)
        append('/etc/default/tomcat', 'JAVA_HOME="%s"' % jre_path, use_sudo=True, partial=True)

@task
def install_swatch(force=False):
    with settings(hide('warnings','stdout'), warn_only=True):
        result = run('dpkg -s ' + UBUNTU_SWATCH_PKG)
    if result and result.split('\n')[1] == 'Status: install ok installed':
        puts('Swatch already installed')
        doInstall = False
    else:
        doInstall = True

    if doInstall or tobool(force):
        puts('\n\nInstalling Swatch\n')
        put(LOCAL_SWATCH_DEB, '/tmp')
        with settings(hide('warnings','stdout'), warn_only=True):
            sudo('apt-get -qy remove ' + UBUNTU_SWATCH_PKG, pty=True)
        sudo('apt-get -qy install swatch', pty=True)
        sudo('dpkg -i /tmp/' + os.path.basename(LOCAL_SWATCH_DEB), pty=True)

@task
def install_server():
    config_user()
    install_ssh_key()
    set_hostname()
    config_limits()
    #set_etc_hosts()
    install_java()
    install_apache()
    install_mrsoa()
    #install_tomcat()
    #install_swatch()

@task
def remove_services(service=None):
    if service is not None:
        sudo('rm -vf /opt/meinc/mrsoa/deploy/*%s*' % service)
    else:
        sudo('rm -vf /opt/meinc/mrsoa/deploy/*')

remote_client_socks = [ ]
remote_client_socks_lock = thread.allocate_lock()
client_sock = None
server_sock = None

def acquireFirstProcessLock():
    global remote_client_socks, remote_client_socks_lock, client_sock, server_sock
    if client_sock or server_sock:
        #print("I'm already the first process")
        return False
    first_process_port = 4004
    s_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        s_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s_sock.bind(('127.0.0.1', 4004))
        s_sock.listen(10)
        server_sock = s_sock
        #print("I'm the first process")
        def sock_listener():
            retries = 0
            while True:
                try:
                    remote_client_sock, remote_client_address = s_sock.accept()
                    #print("I found a secondary process")
                    remote_client_socks_lock.acquire()
                    try:
                        if remote_client_socks is not None:
                            remote_client_socks.append(remote_client_sock)
                        else:
                            # First process lock was already released, this remote client is late to the party
                            remote_client_sock.send('X')
                            remote_client_sock.close()
                    finally:
                        remote_client_socks_lock.release()
                except socket.error as e:
                    if e.errno == 48 and retries < 5:  # Address already in use
                        retries += 1
                        time.sleep(1.0)
                    else:
                        print_exc()
                        abort(str(e))
        thread.start_new_thread(sock_listener, ())
        return True
    except socket.error as e:
        if e.errno == 48 or e.errno == 98:  # Address already in use. Another process beat us to it
            retries = 0
            while True:
                c_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                try:
                    c_sock.connect(('127.0.0.1', 4004))
                    client_sock = c_sock
                    #print("I'm not the first process - waiting for signal...")
                    c_sock.recv(1)
                    c_sock.close()
                    break
                except socket.error as e2:
                    if e2.errno == 61:  # Connection refused
                        if retries < 15:
                            retries += 1
                            time.sleep(1.0)
                        else:
                            abort("It looks like you've re-run fabric too quickly. Try waiting a minute and try again.")
                    else:
                        print_exc()
                        abort(str(e))
            #print("Got signal")
        else:
            print_exc()
            abort(str(e))

def releaseFirstProcessLock():
    global remote_client_socks, remote_client_socks_lock
    remote_client_socks_lock.acquire()
    try:
        if remote_client_socks is None:
            return
        for remote_client_sock in remote_client_socks:
            #print("Signalling secondary process")
            sent = remote_client_sock.send('X')
            remote_client_sock.close()
        remote_client_socks = None
    finally:
        remote_client_socks_lock.release()

def getInstallPipelineFuncs(upload_funcs, copy_funcs, name, names, local, tmp_dir):
    def upload(local_file):
        for upload_func in reversed(upload_funcs):
            proceed = upload_func(local_file)
            if not proceed:
                break

    def copy(local_file, remote_dest, user=None):
        for copy_func in reversed(copy_funcs):
            proceed = copy_func(local_file, remote_dest, user=user)
            if not proceed:
                break

    remote_tmp_dir = '%s/fab.install.%d' % (tmp_dir or '/tmp', uuid.getnode())
    if acquireFirstProcessLock():
        if tobool(local):
            if not os.path.exists(remote_tmp_dir):
                os.makedirs(remote_tmp_dir)
            # ?
        else:
            try:
                sudo('mkdir -p ' + remote_tmp_dir)
                with hide('everything'):
                    sudo('chown %s %s' % (env.user, remote_tmp_dir))
                    run('rm -f "%s"/*.uploading' % remote_tmp_dir)
            finally:
                releaseFirstProcessLock()

    if tobool(local):
        def _upload(local_file):
            _local("cp {} {}".format(local_file, remote_tmp_dir))
            return True
    else:
        def _upload(local_file):
            puts('Uploading %s -> %s' % (os.path.basename(local_file), remote_tmp_dir))
            with quiet():
                result = _local('rsync -a "%s" %s@%s:"%s"' % (local_file, env.user, env.host, remote_tmp_dir+'/'))
            if result.failed:
                puts('Using scp instead of rsync to upload %s' % os.path.basename(local_file))
                put(local_file, remote_tmp_dir)
            return True
    upload_funcs.append(_upload)

    if tobool(local):
        def _copy(local_file, remote_dest, user=None):
            # Ignore user for local deployment
            _local('cp "%s" "%s"' % (os.path.join(remote_tmp_dir, os.path.basename(local_file)), remote_dest))
    else:
        def _copy(local_file, remote_dest, user=None):
            remote_src = os.path.join(remote_tmp_dir, os.path.basename(local_file))
            remote_file = os.path.join(remote_dest, os.path.basename(local_file))
            def build_sudo_cmd(first_cmd):
                result = first_cmd + (' && chmod o-rwx "%s"' % remote_file)
                if user:
                    result += ' && chown "%s:%s" "%s"' % (user, user, remote_file)
                return result
            sudo_command = 'rsync -a "%s" "%s" && chmod o-rwx "%s"' % (remote_src, remote_dest, remote_file)
            sudo_command = 'cp -a "%s" "%s" && chmod o-rwx "%s"' % (remote_src, remote_dest, remote_file)
            puts('Copying %s -> %s' % (os.path.basename(local_file), remote_dest))
            with quiet():
                result = sudo(build_sudo_cmd('rsync -a "%s" "%s"' % (remote_src, remote_dest)))
            if result.failed:
                puts('Using cp instead of rsync to copy %s' % os.path.basename(local_file))
                with hide('everything'):
                    sudo(build_sudo_cmd('cp -a "%s" "%s"' % (remote_src, remote_dest)))
            return True
    copy_funcs.append(_copy)

    if not tobool(local):
        def _upload(local_file):
            remote_file = os.path.join(remote_tmp_dir, os.path.basename(local_file))
            remote_file_marker = remote_file + ".uploading"
            # Atomically attempt to be the first to create the remote file
            with quiet():
                result = run('export T="$(mktemp)"; mv -n "$T" "%s"; rm "$T"' % remote_file_marker)
            if result.return_code == 1:
                # We are the first
                with hide('everything'):
                    local_sum = _local('shasum -b -a 256 "%s" | awk \'{print $1}\'' % local_file, capture=True)
                    remote_sum = run('shasum -b -a 256 "%s" | awk \'{print $1}\'' % remote_file)
                if remote_sum is None or remote_sum.startswith('shasum: '):
                    puts('Uploading: %s' % os.path.basename(remote_file))
                    return True
                elif str(local_sum) == str(remote_sum):
                    puts('Skipping upload: %s is unchanged' % os.path.basename(remote_file))
                    return False
                else:
                    puts('Re-uploading: %s has changed' % os.path.basename(remote_file))
                    #puts('>'+str(local_sum)+'< != >'+str(remote_sum)+'<')
                    return True
            else:
                puts('Skipping upload: %s is handled by another thread' % os.path.basename(remote_file))
                return False
        upload_funcs.append(_upload)

    if not tobool(local):
        def _copy(local_file, remote_dest, user=None):
            with hide('everything'):
                local_sum = _local('shasum -b -a 256 "%s" | awk \'{print $1}\'' % local_file, capture=True)
            remote_file = os.path.join(remote_tmp_dir, os.path.basename(local_file))

            while True:
                with hide('everything'):
                    remote_sum = run('shasum -b -a 256 "%s" | awk \'{print $1}\'' % remote_file)
                if str(remote_sum) == str(local_sum):
                    #puts('Found complete upload on remote of %s' % remote_file)
                    return True
                #puts('Waiting for complete upload to remote of %s' % os.path.basename(remote_file))
                #puts('>'+str(local_sum)+'< != >'+str(remote_sum)+'<')
                time.sleep(1.0)
        copy_funcs.append(_copy)

    names = names and names or name
    if names is not None:
        names = names.split(';')
        def _upload(local_file):
            for name in names:
                if local_file.find(name) != -1:
                    return True
            return False
        upload_funcs.append(_upload)

        def _copy(local_file, remote_dest, user=None):
            for name in names:
                if local_file.find(name) != -1:
                    return True
            return False
        copy_funcs.append(_copy)

    return upload, copy

@task
def install_all(name=None, names=None, stop=True, start=True, force_stop=False, clean=False, local=False, tmp_dir=None):
    #print('>>1', os.getpid(), env.host)
    execute(install_bundles,  stop=False, name=name, names=names, start=False, force_stop=False,              local=local, skip_put=False, skip_copy=True, tmp_dir=tmp_dir)
    #print('>>2', os.getpid(), env.host)
    execute(install_services, stop=False, name=name, names=names, start=False, force_stop=False, clean=False, local=local, skip_put=False, skip_copy=True, tmp_dir=tmp_dir)
    #print('>>3', os.getpid(), env.host)
    execute(install_webapps,  stop=False, name=name, names=names, start=False, force_stop=False, clean=False, local=local, skip_put=False, skip_copy=True, tmp_dir=tmp_dir)
    #print('>>4', os.getpid(), env.host)

    execute(install_bundles,  stop=stop,  name=name, names=names, start=False, force_stop=force_stop,         local=local, skip_put=True, skip_copy=False, tmp_dir=tmp_dir)
    #print('>>5', os.getpid(), env.host)
    execute(install_services, stop=False, name=name, names=names, start=False, force_stop=False, clean=clean, local=local, skip_put=True, skip_copy=False, tmp_dir=tmp_dir)
    #print('>>6', os.getpid(), env.host)
    execute(install_webapps,  stop=False, name=name, names=names, start=start, force_stop=False, clean=clean, local=local, skip_put=True, skip_copy=False, tmp_dir=tmp_dir)
    #print('>>7', os.getpid(), env.host)

@task
def install_bundles(stop=True, name=None, names=None, start=True, force_stop=False, local=False, skip_put=False, skip_copy=False, tmp_dir=None):
    upload_funcs = [ ]
    copy_funcs = [ ]

    upload, copy = getInstallPipelineFuncs(upload_funcs, copy_funcs, name, names, local, tmp_dir)

    install_files = [ os.path.join(LOCAL_WORKSPACE_PATH, lf) for lf in
            [ 
              'MrSoaDistributedData/target/mrsoa-distributed-data-1.0.jar',
              'MrSoaKernel/target/mrsoa-kernel-3.6.1.jar',
              'OsgiBoneCp/target/osgi-bonecp-0.8.jar',
              'OsgiCommonsCollections/target/osgi-commons-collections-3.1.jar',
              'OsgiCommonsPool/target/osgi-commons-pool-1.3.jar',
              #'OsgiDynamoDb/target/osgi-dynamodb-0.3.jar',
              'OsgiIbatis-dep/target/osgi-mybatis-dep-1.1.jar',
              'OsgiIbatis/target/osgi-mybatis-3.2.1.jar',
              'OsgiSpringFramework/target/osgi-spring-4.2.5.RELEASE.jar',
              'OsgiVelocity/target/osgi-velocity-1.4.jar' 
            ] ]

    if not tobool(skip_put):
        for install_file in install_files:
            upload(install_file)

    if tobool(stop):
        mrsoa_stop(force=tobool(force_stop), local=local)

    if not tobool(skip_copy):
        for install_file in install_files:
            copy(install_file, '/opt/meinc/mrsoa/bundles', user='mrsoa')

    if tobool(start):
        mrsoa_start(local=local)

@task
def install_services(name=None, names=None, stop=True, start=True, force_stop=False, clean=False, local=False, skip_put=False, skip_copy=False, tmp_dir=None):
    upload_funcs = [ ]
    copy_funcs = [ ]

    upload, copy = getInstallPipelineFuncs(upload_funcs, copy_funcs, name, names, local, tmp_dir)

    ### Skip deploying gameplay and jetty services to any collector server
    def _upload(local_file):
        if env.host is None or (env.host.find('collector') == -1 and env.host not in env.roledefs['test-collector'] or (
                #not re.search(r'gameplay-service-[0-9.]+\.jar$', local_file) and
                not re.search(r'tools-service-[0-9.]+\.jar$', local_file) and
                not re.search(r'jetty-service-[0-9.]+\.jar$', local_file) )):
            return True
        puts("Skipping upload: %s doesn't belong on a collector" % os.path.basename(local_file))
        return False
    upload_funcs.append(_upload)

    def _copy(local_file, remote_dest, user=None):
        if env.host is None or (env.host.find('collector') == -1 and env.host not in env.roledefs['test-collector'] or (
                #not re.search(r'gameplay-service-[0-9.]+\.jar ', local_file) and
                not re.search(r'tools-service-[0-9.]+\.jar$', local_file) and
                not re.search(r'jetty-service-[0-9.]+\.jar$', local_file) )):
            return True
        return False
    copy_funcs.append(_copy)

    install_files = [ os.path.join(LOCAL_WORKSPACE_PATH, lf) for lf in
            [ 
              #'DailyMillionaireService/target/dailymillionaire-service-1.0.jar',
              #'JettyService/target/jetty-service-1.0.jar',
              'EncryptionService/target/encryption-service-3.0.jar',
              'FacebookService/target/facebook-service-2.0.jar',
              'HazelcastConfigService/target/hazelcast-config-service-1.0.jar',
              'HazelcastService/target/hazelcast-service-1.0.jar',
              'HttpConnectorService/target/http-connector-service-3.0.jar',
              'IdentityService/target/identity-service-1.0.jar',
              'NotificationService/target/notification-service-1.0.jar',
              'PostOfficeService/target/postoffice-service-3.0.jar',
              'PushService/target/push-service-1.0.jar',
              'ShoutContestAwardService/target/shoutcontestaward-service-1.0.jar',
              'ShoutContestService/target/shoutcontest-service-1.0.jar',
              'SnowyowlService/target/snowyowl-service-1.0.jar',
              'StoreService/target/store-service-1.0.jar',
              'SyncService/target/sync-service-1.0.jar',
              'TriggerService/target/trigger-service-1.0.jar',
              'WebCollectorService/target/webcollector-service-1.0.jar',
              'WebDataStoreService/target/webdatastore-service-1.0.jar',
              ] ]

    if not tobool(skip_put):
        for install_file in install_files:
            upload(install_file)

    if tobool(stop):
        mrsoa_stop(force=tobool(force_stop), local=local)

    if tobool(clean):
        sudo('rm -rf /opt/meinc/mrsoa/deploy/*.jar')

    if not tobool(skip_copy):
        for install_file in install_files:
            copy(install_file, '/opt/meinc/mrsoa/deploy', user='mrsoa')

    if tobool(start):
        mrsoa_start(local=local)

@task
def install_webapps(stop=True, start=True, name=None, names=None, force_stop=False, clean=False, local=False, skip_put=False, skip_copy=False, tmp_dir=None):
    upload_funcs = [ ]
    copy_funcs = [ ]

    upload, copy = getInstallPipelineFuncs(upload_funcs, copy_funcs, name, names, local, tmp_dir)

    ### Skip deploying any webapps to any collector server
    def _upload(local_file):
        if env.host is None or (env.host.find('collector') == -1 and env.host not in env.roledefs['test-collector']):
            return True
        puts("Skipping upload: %s is a webapp and doesn't belong on a collector" % os.path.basename(local_file))
        return False
    upload_funcs.append(_upload)

    def _copy(local_file, remote_dest, user=None):
        if env.host is None or (env.host.find('collector') == -1 and env.host not in env.roledefs['test-collector']):
            return True
        return False
    copy_funcs.append(_copy)

    install_files = [ os.path.join(LOCAL_WORKSPACE_PATH, lf) for lf in
            [ 'GameplayWebGateway/target/gpwebapi.war',
              'HttpConnector/target/eps.war' ] ]

    if not tobool(skip_put):
        for install_file in install_files:
            upload(install_file)

    if tobool(stop):
        mrsoa_stop(force=tobool(force_stop), local=local)

    if tobool(clean):
        sudo('rm -rf /opt/meinc/mrsoa/webapps/*.war')

    if not tobool(skip_copy):
        for install_file in install_files:
            copy(install_file, '/opt/meinc/mrsoa/webapps', user='mrsoa')

    if tobool(start):
        mrsoa_start(local=local)

'''Deploy the static website to the server.

@param testpage {boolean} optional param (default: 0) - if true, also deploy the rest test page
'''
@task
def install_website(testpage=False):
    #uncompressed tar because you can't append to a compressed archive
    local('cd %s && tar -cf /tmp/website.tar *' % (os.path.join(LOCAL_WORKSPACE_PATH, 'GameplayWebStatic/src')))

    #if testpage is specified, append the test page to the uncompressed archive
    if testpage is not False:
        local('cd %s && tar -rf /tmp/website.tar *.html' % (os.path.join(LOCAL_WORKSPACE_PATH, 'GameplayWebStatic/tools')))

    #convert the uncompressed tar to a compressed tar
    local('gzip /tmp/website.tar')
    local('mv /tmp/website.tar.gz /tmp/website.tgz')

    #stick on the server in the appropriate location with correct user permissions
    put('/tmp/website.tgz', '/tmp')
    sudo('chown www-data:www-data /var/www')
    sudo('cd /var/www && tar -xzf /tmp/website.tgz', user='www-data')

@task
def apache_start():
    global ssl_pw
    pw = ssl_pw
    while pw == '':
        pw = ssl_pw = prompt('SSL Password:')

    # This is pretty convoluted: Apache SSL password requires bash for some
    # unknown reason, and empty requires a consistent tty to start bash for
    # apache for some reason
    sudo('screen -d -m empty -i empty.in -o empty.out -f bash')
    sudo('empty -i empty.out -o empty.in -s "apache2ctl start\\n" ; sleep 1')
    sudo('echo "%s" | empty -i empty.out -o empty.in -s ; sleep 0' % pw)
    sudo('empty -i empty.out -o empty.in -s "exit\\n" ; sleep 0')

@task
def apache_stop():
    sudo('service apache2 stop')
    with settings(hide('warnings','stdout'), warn_only=True):
        sudo('killall apache2')

@task
def apache_restart():
    apache_stop()
    apache_start()

@task
def apache_reload():
    sudo('service apache2 reload')

@task
def mrsoa_start(local=False):
    if tobool(local):
        _local('/opt/meinc/mrsoa/bin/mrsoa.sh start')
    elif re.match(r"dc\d+-.*", env.host_string):
        sudo('/usr/bin/supervisorctl start mrsoa')
    else:
        sudo('service mrsoa start')

@task
def mrsoa_stop(force=False, local=False):
    if re.match(r"dc\d+-.*", env.host_string):
        if tobool(force):
            with settings(hide('warnings'), warn_only=True):
                sudo('pkill -9 mrsoa_java')
        else:
            sudo('/usr/bin/supervisorctl stop mrsoa')
    elif tobool(force):
        if tobool(local):
            _local('/opt/meinc/mrsoa/bin/mrsoa.sh force-stop')
        else:
            sudo('service mrsoa force-stop')
    else:
        if tobool(local):
            _local('/opt/meinc/mrsoa/bin/mrsoa.sh stop')
        else:
            sudo('service mrsoa stop')

@task
def mrsoa_restart(force=False):
    mrsoa_stop(force)
    mrsoa_start()

@task
def tomcat_start():
    sudo('service tomcat start')

@task
def tomcat_stop():
    sudo('service tomcat stop')

@task
def tomcat_restart():
    sudo('service tomcat restart')

@task
def bounce(mrsoa=False, force=False, tomcat=False, apache=False, grinder=False):
    args = {}
    if env.host_string:
        args['host'] = env.host_string

    if tobool(apache):
        execute(apache_stop, **args)
    if tobool(tomcat):
        execute(tomcat_stop, **args)
    if tobool(mrsoa):
        execute(mrsoa_stop, force=force, **args)
    if tobool(mrsoa):
        execute(clean_mrsoa_logs, **args)
        execute(mrsoa_start, **args)
    if tobool(tomcat):
        execute(clean_tomcat_logs, **args)
        execute(tomcat_start, **args)
    if tobool(apache):
        if tobool(tomcat):
            time.sleep(3)
        execute(clean_apache_logs, **args)
        execute(apache_start, **args)
    if tobool(grinder):
        execute(grinder_stop, **args)
        execute(clean_client_logs, **args)
        execute(grinder_start, **args)

@task
def get_games():
    run('cd /tmp && wget -p --header="Host: www.shout.tv" \
            http://localhost/gpwebapi/game/upcomingGames?serializeType=teams')

@task
def clean_mrsoa_logs(local=False):
    with settings(hide('warnings','stdout'), warn_only=True):
        if tobool(local):
            _local('rm /opt/meinc/mrsoa/logs/*')
        else:
            sudo('rm /opt/meinc/mrsoa/logs/*')

@task
def clean_tomcat_logs():
    with settings(hide('warnings','stdout'), warn_only=True):
        sudo('rm /opt/meinc/tomcat/logs/*')

@task
def clean_apache_logs():
    with settings(hide('warnings','stdout'), warn_only=True):
        sudo('rm /var/log/apache2/*')

@task
def clean_server_logs():
    clean_mrsoa_logs()
    #clean_tomcat_logs()
    clean_apache_logs()

##
## Load testing tasks
##

HOSTNAME_PREFIX = "test-"
#HOSTNAME_PREFIX = "stage-"
#HOSTNAME_PREFIX = "local-"
#HOSTNAME_PREFIX = "prod-"

# For load testing
def set_etc_hosts(subjects, func=run):
    with settings(hide('warnings'), warn_only=True):
        pysedcmd = r"""
with open('/etc/hosts', 'r') as f:
    lines = f.readlines()
with open('/etc/hosts', 'w') as f:
    for line in lines:
        if line.startswith('127.') or \
                line.find('{prefix}sync-') == -1 and \
                line.find('{prefix}server-') == -1 and \
                line.find('{prefix}collector-') == -1 and \
                line.find('{prefix}client-') == -1 and \
                line.find('{prefix}desktop') == -1 and \
                line.find('{prefix}web') == -1 and \
                line.find('{prefix}db')      == -1:
            f.write(line)""".format(prefix=HOSTNAME_PREFIX)
        
        func(r'sudo python -c "{}"'.format(pysedcmd))

        if 'test-sync' in subjects:
            for i in range(len(env.roledefs['test-sync'])):
                server = env.roledefs['test-sync'][i]
                ip = func('python -c "import socket; print socket.gethostbyname(\'%s\')"' % server)
                entry = '%s\t%ssync-%d'
                func('echo "%s" | sudo tee -a /etc/hosts' % entry % (ip, HOSTNAME_PREFIX, i+1))

        if 'test-collector' in subjects:
            for i in range(len(env.roledefs['test-collector'])):
                server = env.roledefs['test-collector'][i]
                ip = func('python -c "import socket; print socket.gethostbyname(\'%s\')"' % server)
                entry = '%s\t%scollector-%d'
                func('echo "%s" | sudo tee -a /etc/hosts' % entry % (ip, HOSTNAME_PREFIX, i+1))

        if 'test-desktop' in subjects:
            desktop = env.roledefs['test-desktop'][0]
            ip = func('python -c "import socket; print socket.gethostbyname(\'%s\')"' % desktop)
            entry = '%s\t%sdesktop'
            func('echo "%s" | sudo tee -a /etc/hosts' % entry % (ip, HOSTNAME_PREFIX))

        if 'test-db' in subjects:
            database = env.roledefs['test-db'][0]
            ip = func('python -c "import socket; print socket.gethostbyname(\'%s\')"' % database)
            entry = '%s\t%sdb'
            func('echo "%s" | sudo tee -a /etc/hosts' % entry % (ip, HOSTNAME_PREFIX))

        if 'test-client' in subjects:
            for i in range(len(env.roledefs['test-client'])):
                client = env.roledefs['test-client'][i]
                ip = func('python -c "import socket; print socket.gethostbyname(\'%s\')"' % client)
                entry = '%s\t%sclient-%d'
                func('echo "%s" | sudo tee -a /etc/hosts' % entry % (ip, HOSTNAME_PREFIX, i+1))

        if 'test-web' in subjects:
            for i in range(len(env.roledefs['test-web'])):
                server = env.roledefs['test-web'][i]
                ip = func('python -c "import socket; print socket.gethostbyname(\'%s\')"' % server)
                entry = '%s\t%sweb-%d'
                func('echo "%s" | sudo tee -a /etc/hosts' % entry % (ip, HOSTNAME_PREFIX, i+1))

# For load testing
@task
def config_desktop_dns(remove=False):
    dhcp_conf = '/etc/dhcp/dhclient.conf'
    with settings(hide('warnings','stdout','stderr'), warn_only=True):
        result = run('ls ' + dhcp_conf)
    if not result or 'No such file' in result:
        dhcp_conf = '/etc/dhcp3/dhclient.conf'
    if not tobool(remove):
        desktop = env.roledefs['test-desktop'][0]
        ip = run('python -c "import socket; print socket.gethostbyname(\'%s\')"' % desktop)
        append(dhcp_conf, 'supersede domain-name-servers %s;' % ip, use_sudo=True, partial=False)
    else:
        sudo(r"sed -i.bak -e '/^supersede domain-name-servers / d' %s" % dhcp_conf)
    sudo('dhclient')

# For load testing
@task
def config_hosts(dns=False,
                 target ='test-web;test-sync;test-collector;test-client;test-db;test-desktop;local',
                 subjects='test-web;test-sync;test-collector;test-client;test-db;test-desktop'):
    if (env.all_hosts or env.roles or env.hosts):
        print('No per-task or global hosts and/or roles allowed. Use target instead.')
        sys.exit(1)
    if (not target):
        print('Must provide target parameter with colon delimited role list')
        sys.exit(1)

    subjects = subjects.split(';')

    raw_roles = target.split(';')
    roles = []
    doLocal = False
    for role in raw_roles:
        if role == 'local':
            doLocal = True
        else:
            roles.append(role)

    if roles and tobool(dns):
        execute(config_desktop_dns, remove=True, roles=roles)

    if roles:
        execute(set_etc_hosts, subjects=subjects, roles=roles)

    if doLocal:
        def localFunc(cmd):
            return local(cmd, capture=True)
        set_etc_hosts(subjects=subjects, func=localFunc)

    if roles and tobool(dns):
        execute(config_desktop_dns, roles=roles)

# For load testing
@task
def clean_known_hosts():
    with settings(hide('warnings'), warn_only=True):
        command = r"sed -i.bak -e '/^%s/ d' ~/.ssh/known_hosts" % HOSTNAME_PREFIX
        run(command)
        local(command)

# For load testing
@task
def install_client():
    config_user()
    set_hostname()
    config_limits()
    set_etc_hosts()
    install_java()
    install_grinder()
    ext_grinder()

# For load testing
@task
def install_grinder(force=False, purge=False):
    with settings(hide('warnings','stdout'), warn_only=True):
        result = run('dpkg -s ' + GRINDER_PKG)
    if result and result.split('\n')[1].endswith('Status: install ok installed'):
        puts('The Grinder already installed')
        if not tobool(force):
            return
        puts('Re-installing')
        sudo('apt-get -qy %s %s' % (purge and 'purge' or 'remove', GRINDER_PKG), pty=True)
    uploaded = exists(os.path.join('/tmp', GRINDER_DEB))
    if uploaded:
        puts('Deb package already exists')
        if tobool(force):
            puts('Re-uploading')
    if tobool(force) or not uploaded:
        put(LOCAL_GRINDER_PATH, '/tmp')
    sudo('dpkg -i ' + os.path.join('/tmp', GRINDER_DEB), pty=True)

# For load testing
@task
def ext_grinder():
    uploaded = False
    for extPath in LOCAL_GRINDER_EXTS_PATHS:
        jarName = extPath.split('/')[-1]
        if not uploaded:
            with settings(hide('warnings'), warn_only=True):
                sudo('rm -r /tmp/ext')
            run('mkdir /tmp/ext')
        put(extPath, '/tmp/ext')
        uploaded = True
    if uploaded:
        sudo('rm -r /opt/grinder/ext ; mkdir -p /opt/grinder/ext', user='grinder')
        sudo('chown -R grinder.grinder /tmp/ext')
        sudo('mv /tmp/ext /opt/grinder', user='grinder')

# For load testing
@task
@roles('test-client')
def grinder_start():
    sudo('service grinder start')

# For load testing
@task
@roles('test-client')
def grinder_restart():
    grinder_stop()
    grinder_start()

# For load testing
@task
@roles('test-client')
def grinder_stop():
    sudo('rm -rf /opt/grinder/test-*')
    sudo('service grinder stop')

# For load testing
@task
def grinder_analyze():
    def get_grinder_logs():
        run('cd /opt/grinder/logs && tar -czvf /tmp/grinder_logs.tgz *.log')
        get('/tmp/grinder_logs.tgz', '/tmp')
        local('cd /tmp/grinder_logs && tar -xzvf /tmp/grinder_logs.tgz')

    local('rm -rf /tmp/grinder_logs')
    local('mkdir -p /tmp/grinder_logs')

    execute(get_grinder_logs, role='clients')

    tmp_logs_path = '/tmp/grinder_logs'

    grinderLogFiles = glob.glob('%s/*.log' % tmp_logs_path)
    dataLogFiles = []
    outLogFiles = []
    for grinderLogFile in grinderLogFiles:
        if os.path.basename(grinderLogFile).startswith('test-'):
            if os.path.basename(grinderLogFile).endswith('-data.log'):
                dataLogFiles.append(grinderLogFile)
            else:
                outLogFiles.append(grinderLogFile)

    local('cd %s && %s/bin/jython %s/analyzer.py "%s" %s' % 
            (LOCAL_GRINDER_ANALYZER_PATH,
             LOCAL_JYTHON_PATH,
             LOCAL_GRINDER_ANALYZER_PATH,
             ' '.join(dataLogFiles),
             outLogFiles[0]))

    local('open %s/grinderReport/report.html' % LOCAL_GRINDER_ANALYZER_PATH)

# For load testing
@task
@roles('test-client')
def clean_client_logs():
    sudo('find /opt/grinder/logs -mindepth 1 -delete')


#########################
# encryption/decryption #
#########################

# this code originally from http://stackoverflow.com/questions/16761458/how-to-aes-encrypt-decrypt-files-using-python-pycrypto-in-an-openssl-compatible

'''
 usage
 
with open(in_filename, 'rb') as in_file, open(out_filename, 'wb') as out_file:
    encrypt(in_file, out_file, password)
with open(in_filename, 'rb') as in_file, open(out_filename, 'wb') as out_file:
    decrypt(in_file, out_file, password)
'''

from hashlib import md5
from Crypto.Cipher import AES
from Crypto import Random
import getpass

def derive_key_and_iv(password, salt, key_length, iv_length):
    d = d_i = ''
    while len(d) < key_length + iv_length:
        d_i = md5(d_i + password + salt).digest()
        d += d_i
    return d[:key_length], d[key_length:key_length + iv_length]


def encrypt(in_file, out_file, password, key_length = 32):
    bs = AES.block_size
    salt = Random.new().read(bs - len('Salted__'))
    key, iv = derive_key_and_iv(password, salt, key_length, bs)
    cipher = AES.new(key, AES.MODE_CBC, iv)
    out_file.write('Salted__' + salt)
    finished = False
    while not finished:
        chunk = in_file.read(1024 * bs)
        if len(chunk) == 0 or len(chunk) % bs != 0:
            padding_length = (bs - len(chunk) % bs) or bs
            chunk += padding_length * chr(padding_length)
            finished = True
        out_file.write(cipher.encrypt(chunk))


def decrypt(in_file, out_file, password, key_length = 32):
    bs = AES.block_size
    salt = in_file.read(bs)[len('Salted__'):]
    key, iv = derive_key_and_iv(password, salt, key_length, bs)
    cipher = AES.new(key, AES.MODE_CBC, iv)
    next_chunk = ''
    finished = False
    while not finished:
        chunk, next_chunk = next_chunk, cipher.decrypt(in_file.read(1024 * bs))
        if len(next_chunk) == 0:
            padding_length = ord(chunk[-1])
            chunk = chunk[:-padding_length]
            finished = True

        out_file.write(chunk)


def decryptToString(in_file, password, key_length = 32):
    bs = AES.block_size
    salt = in_file.read(bs)[len('Salted__'):]
    key, iv = derive_key_and_iv(password, salt, key_length, bs)
    cipher = AES.new(key, AES.MODE_CBC, iv)
    next_chunk = ''
    decryptedString = ''
    finished = False
    while not finished:
        chunk, next_chunk = next_chunk, cipher.decrypt(in_file.read(1024 * bs))
        if len(next_chunk) == 0:
            padding_length = ord(chunk[-1])
            chunk = chunk[:-padding_length]
            finished = True

        decryptedString += chunk

    return decryptedString

def getEncryptionPassword():
    return getpass.getpass('Enter password for configuration file: ')


def loadConfiguration():
    in_file = open('fabfile_config.crypt', 'rb')
    password = getEncryptionPassword()
    exec(decryptToString(in_file, password))

@task
def testLoadConfig():
    loadConfiguration()
    print(hostedZones['shoutgameplay.com'])

    testAuth = RoleSetting('auth', filesCommandConstruct['mrsoa'], rsyncCommandSource['mrsoa']['user'], scaleSequenceConstruct['mrsoa'], True)
    print(testAuth.role)

##################################
#    rewire/scale config file    #
##################################

@task
def encryptConfig():
    '''
        Used to encrypt the do_not_add_to_bzr.py configuration file.
    '''
    print('encrypting do_not_add_to_bzr.py to fabfile_config.crypt')
    in_file = open('do_not_add_to_bzr.py', 'rb')
    out_file = open('fabfile_config.crypt', 'wb')
    password = getEncryptionPassword()
    encrypt(in_file, out_file, password)


@task
def decryptConfig():
    '''
        Used to decrypt the do_not_add_to_bzr.py configuration file.
    '''
    print('decrypting fabfile_config.crypt to do_not_add_to_bzr.py')
    in_file = open('fabfile_config.crypt', 'rb')
    out_file = open('do_not_add_to_bzr.py', 'wb')
    password = getEncryptionPassword()
    decrypt(in_file, out_file, password)


######################
#    rewire/scale    #
######################

'''
   static values
'''
# AwsAccessKey = 'AKIAIVXNKSOQK4F3ONXA'
# AwsSecretKey = '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'

instanceStateCodeMap = {
    '0'   : 'pending',
    '16'  : 'running',  # 272 means running with a problem reboot
    '32'  : 'shutting-down',  # 288 means shutting down from problem
    '48'  : 'terminated',
    '64'  : 'stopping',
    '80'  : 'stopped'
}

route53ParamMap = {}

serverCluster = None

runningInstanceCount = 0

AwsDate = ''  # retrieved from earlier http request then used for signing Route53 API calls

runningOnEc2Instance = None

# returns the name for the EC2 instance
def getInstanceTag(instanceXml, tagKey):
    tagList = instanceXml.findall('.//tagSet/item')
    for tagItem in tagList:
        tagItemKey = getTextFromSubElement(tagItem, 'key')
        if tagItemKey == tagKey:
            return getTextFromSubElement(tagItem, 'value').strip()

    if (tagKey == 'Name'):
        return "No Name " + instanceXml.find('./instanceId').text
    else:
        return None

def setInstanceName(instance, name, scaleAmiId = None):
    '''
        Amazon ec2 api call to change the name of an instance.
    '''
    ec2ParamMap = {}
    ec2ParamMap['Action'] = 'CreateTags'
    ec2ParamMap['ResourceId.1'] = instance['InstanceId']
    ec2ParamMap['Tag.1.Key'] = 'Name'
    ec2ParamMap['Tag.1.Value'] = name
    if (scaleAmiId is not None):
        ec2ParamMap['Tag.2.Key'] = 'ScaleAmiId'
        ec2ParamMap['Tag.2.Value'] = scaleAmiId
        if (verboseOutput):
            print('setting Tag.ScaleAmiId ' + scaleAmiId)

    if (verboseOutput):
        print('CreateTags ec2ParamMap: ')
        print('  ' + str(ec2ParamMap))

    url = aws_signed_request.aws_signed_request(ec2ParamMap, serverCluster['EC2AwsAccessKey'], serverCluster['EC2AwsSecretKey'], serverCluster['EndPoint'])

    httpResponse = urllib2.urlopen(url);

    rootElement = getRootElementFromHttpResponseXml(httpResponse)

    success = rootElement.find('./return').text
    if success != 'true':
        print('failed to set instance name')
        exit()

    instance['Name'] = name


def getAwsDate():
    ''' current datetime within 5 minutes is needed to update the cnames in DNS in Route53
        get the date from the amazon server for use in the route53 api calls '''
    httpResponse = urllib2.urlopen("https://route53.amazonaws.com/date")
    httpHeaders = httpResponse.info()
    return httpHeaders['Date']

# return signature for route53 api calls
def getSignatureAsBase64(text, key):
    import hmac, hashlib, base64
    hm = hmac.new(bytearray(key, "ascii"), bytearray(text, "utf-8"), hashlib.sha256)
    return base64.b64encode(hm.digest()).decode('utf-8')

# return the authentication header to be added to the route53 api call
def getAmazonV3AuthHeader(accessKey, signature):
    # AWS3-HTTPS AWSAccessKeyId=MyAccessKey,Algorithm=ALGORITHM,Signature=Base64( Algorithm((ValueOfDateHeader), SigningKey) )
    return "AWS3-HTTPS AWSAccessKeyId=%s,Algorithm=HmacSHA256,Signature=%s" % (accessKey, signature)

def getInput(inputValue, validList, prompt):
    '''
        lists the available valid values
        prompts for which value to work with
        @param inputValue: string, if not valid new value will be requested
        @param validList: list of valid strings
        @param prompt: string to display when requesting new value
        @return: string a valid value
    '''

    if (inputValue not in validList):
        inputValueAcceptable = False

        validValueString = ", ".join(validList)

        while not inputValueAcceptable:
            print('valid values: ' + validValueString)
            inputValue = raw_input(prompt + '  ')
            inputValue = inputValue.strip()
            inputValueAcceptable = inputValue in validList

            if not inputValueAcceptable:
                print(inputValue + " is not valid ")

    return inputValue

def setRewireGlobals(clusterName):
    global serverCluster
    serverCluster = namedClusters[clusterName]

def getRootElementFromHttpResponseXml(httpResponse):
    responseXml = httpResponse.read()
    if (verboseOutput):
        print('response:')
        print(responseXml)

    # remove the namespace from the xml to make dealing with it easier
    responseXml = re.sub(' xmlns="[^"]+"', '', responseXml, count = 1)

    # parse the xml from amazon
    rootElement = ET.fromstring(responseXml)

    return rootElement


def describeInstances(instanceIdList, endPoint):
    '''
        Amazon ec2 api call to get information about an instances.
        @param instanceIdList - list of strings
        @param endPoint url string
    '''
    ec2ParamMap = {}
    ec2ParamMap['Action'] = 'DescribeInstances'
    if (instanceIdList is not None):
        for x in range (len(instanceIdList)):
            ec2ParamMap['InstanceId.' + str(x)] = instanceIdList[x]

#     ec2ParamMap['Filter.1.Name'] = 'group-name'
#     ec2ParamMap['Filter.1.Value.1'] = 'test-' + testSuite
#     ec2ParamMap['Filter.2.Name'] = 'instance-state-code'
#     ec2ParamMap['Filter.2.Value.1'] = '16'

    if (verboseOutput):
        print('describeInstances ec2ParamMap: ')
        print('  ' + str(ec2ParamMap))

    url = aws_signed_request.aws_signed_request(ec2ParamMap, serverCluster['EC2AwsAccessKey'], serverCluster['EC2AwsSecretKey'], endPoint)

    httpResponse = urllib2.urlopen(url);

    rootElement = getRootElementFromHttpResponseXml(httpResponse)

    # get a list of the instances
    instanceList = rootElement.findall('.//instancesSet/item')

    return instanceList


def getBlockDeviceMapping(instanceFromXml):
    '''
        Take xml list of block devices from amazon ec2 api call and returns objects.
        @param xml from amazon
        @return list of block device objects 
    '''
    blockDeviceMapping = []
    blockDeviceList = instanceFromXml.findall('.//blockDeviceMapping/item')
    for blockDeviceXml in blockDeviceList:
        blockDeviceObj = {}
        blockDeviceObj['DeviceName'] = getTextFromSubElement(blockDeviceXml, './deviceName')
        blockDeviceObj['Ebs.VolumeId'] = getTextFromSubElement(blockDeviceXml, './ebs/volumeId')
        blockDeviceObj['Ebs.Status'] = getTextFromSubElement(blockDeviceXml, './ebs/status')
        blockDeviceObj['Ebs.DeleteOnTermination'] = getTextFromSubElement(blockDeviceXml, './ebs/deleteOnTermination')
        blockDeviceMapping.append(blockDeviceObj)

    return blockDeviceMapping


def setInstanceValues(server, instanceFromXml):
    '''
        Given one server object and one xml instance as returned from amazon api call,
        sets the values server object.
        @param server dict describing one instance
        @param instance xml describing one instanceFromXml
        @running boolean
    '''

    '''
        <item>
            <instanceId>i-40bcf0a1</instanceId>
            <imageId>ami-f699679e</imageId>
            <instanceState>
                <code>48</code>
                <name>terminated</name>
            </instanceState>
            <privateDnsName/>
            <dnsName/>
            <reason>User initiated (2014-11-27 23:39:07 GMT)</reason>
            <keyName>meinc-ec2-1</keyName>
            <amiLaunchIndex>0</amiLaunchIndex>
            <productCodes/>
            <instanceType>c3.large</instanceType>
            <launchTime>2014-11-27T23:32:28.000Z</launchTime>
            <placement>
                <availabilityZone>us-east-1e</availabilityZone>
                <groupName/>
                <tenancy>default</tenancy>
            </placement>
            <kernelId>aki-825ea7eb</kernelId>
            <monitoring>
                <state>disabled</state>
            </monitoring>
            <groupSet>
                <item>
                    <groupId>sg-6a368d03</groupId>
                    <groupName>test-1</groupName>
                </item>
            </groupSet>
            <stateReason>
                <code>Client.UserInitiatedShutdown</code>
                <message>Client.UserInitiatedShutdown: User initiated shutdown</message>
            </stateReason>
            <architecture>x86_64</architecture>
            <rootDeviceType>ebs</rootDeviceType>
            <rootDeviceName>/dev/sda1</rootDeviceName>
            <blockDeviceMapping>
                <item>
                    <deviceName>/dev/sda1</deviceName>
                    <ebs>
                        <volumeId>vol-c5428b8c</volumeId>
                        <status>attached</status>
                        <attachTime>2014-05-14T17:53:58.000Z</attachTime>
                        <deleteOnTermination>false</deleteOnTermination>
                    </ebs>
                </item>
            </blockDeviceMapping>
            <virtualizationType>paravirtual</virtualizationType>
            <clientToken/>
            <tagSet>
                <item>
                    <key>ScaleAmiId</key>
                    <value>ami-f2debd9a</value>
                </item>
                <item>
                    <key>Name</key>
                    <value>Test 1 Collector 1</value>
                </item>
            </tagSet>
            <hypervisor>xen</hypervisor>
            <networkInterfaceSet/>
            <ebsOptimized>false</ebsOptimized>
        </item>
    '''

    '''
        each instance eventually ends up with some or all of these key/value pairs
        {
            "DnsName": "ec2-54-159-54-76.compute-1.amazonaws.com", 
            "EbsOptimized": "false", 
            "ImageId": "ami-fef29996", 
            "InstanceId": "i-1f0727e4", 
            "InstanceState.Code": "16", 
            "InstanceType": "m1.small", 
            "IpAddress": "54.159.54.76", 
            "KeyName": "meinc-ec2-1", 
            "Monitoring.Enabled": "false", 
            "Name": "Test 1 Web 1", 
            "NewRunning": "r", 
            "Placement.AvailabilityZone": "us-east-1b", 
            "PrivateIpAddress": "10.236.167.182", 
            "Role": "web", 
            "Route53Name": "lt1-web1.shoutgameplay.com.", 
            "Running": true, 
            "SecurityGroup.1": "test-1", 
            "SecurityGroupId.1": "sg-6a368d03", 
            "Tag.Name": "Test 1 Web 1", 
            "Tag.ScaleAmiId": null or "ami-f2debd9a"
            "BlockDeviceMapping": [
                {
                    "DeviceName":"/dev/sda1",
                    "Ebs.VolumeId":"vol-c5428b8c",
                    "Ebs.Status":"attached",
                    "Ebs.DeleteOnTermination":"false"
                }
            ]
        }
    '''

    if (verboseOutput):
        print ('setting instance values from:')
        print(str(instanceFromXml))

    server['ImageId'] = getTextFromSubElement(instanceFromXml, './imageId')
    server['KeyName'] = getTextFromSubElement(instanceFromXml, './keyName')
    server['SecurityGroupId.1'] = getTextFromSubElement(instanceFromXml, './groupSet/item/groupId')
    server['SecurityGroup.1'] = getTextFromSubElement(instanceFromXml, './groupSet/item/groupName')
    server['Monitoring.Enabled'] = 'false' if (getTextFromSubElement(instanceFromXml, './monitoring/state') == 'disabled') else 'true'
    server['InstanceId'] = getTextFromSubElement(instanceFromXml, './instanceId')
    server['InstanceType'] = getTextFromSubElement(instanceFromXml, './instanceType')
    server['DnsName'] = getTextFromSubElement(instanceFromXml, './dnsName')
    server['Placement.AvailabilityZone'] = getTextFromSubElement(instanceFromXml, './placement/availabilityZone')
    server['EbsOptimized'] = getTextFromSubElement(instanceFromXml, './ebsOptimized')
    server['InstanceState.Code'] = getTextFromSubElement(instanceFromXml, './instanceState/code')

    server['PrivateIpAddress'] = getTextFromSubElement(instanceFromXml, './privateIpAddress')
    server['IpAddress'] = getTextFromSubElement(instanceFromXml, './ipAddress')
    server['Tag.Name'] = getInstanceTag(instanceFromXml, 'Name')
    server['Tag.ScaleAmiId'] = getInstanceTag(instanceFromXml, 'ScaleAmiId')

    server['BlockDeviceMapping'] = getBlockDeviceMapping(instanceFromXml)


def getTextFromSubElement(instance, match):
    '''
        finds the first subelement matching match
        if found it returns the text of the element
        if not found returns None
    '''
    subElement = instance.find(match)
    if subElement is None:
        if (verboseOutput):
            print ("subElement: None")

        return None

    return subElement.text

def getServerListInstance(instanceName, instanceId, roleName):
    '''
        Returns a server from the server cluster that matches the instanceName
        and has not yet been matched up with an actual instance at Amazon.
        If one does not exist in the server cluster, and we are tracking servers of that roleName,
        it is added to the cluster and returned.
    '''
    for serverListInstance in serverCluster['ServerList']:
        if (serverListInstance['Name'] == instanceName):
            if ('Running' not in serverListInstance):
                # server name matches and the server is not
                return serverListInstance

            instanceName += ' (' + instanceId + ')'
            break

    result = None

    if (roleName is None):
        for key in serverCluster['RoleNamePatterns']:
            result = re.match(serverCluster['RoleNamePatterns'][key]['InstanceNameRegex'], instanceName)
            if (result != None):
                roleName = key;
                break;

    if (roleName is None):
        return None

    if (result is None):
        result = re.match(serverCluster['RoleNamePatterns'][roleName]['InstanceNameRegex'], instanceName)

    route53Name = serverCluster['RoleNamePatterns'][roleName]['Route53NameRegex']
    if (verboseOutput):
        print('debug roleName ' + roleName + ' route53Name ' + route53Name + ' instanceName ' + instanceName)

    route53Name = route53Name.replace('(\d+)', result.group(1))
    serverListInstance = {'Name' : instanceName, 'Route53Name' : route53Name, 'Role' : roleName}
    serverCluster['ServerList'].append(serverListInstance)
    return serverListInstance


def clearRunningProperty(serverList):
    for instance in serverList:
        if ('Running' in instance):
            del instance['Running']


def getInstances(clusterName, roleName = None, resetServerList = False):
    '''
       get the instance data
       adds parameters to the serverCluster ServerList
       Returns boolean true if all instances in the named cluster are running or stopped (they don't all have to be the same)
       @param clusterName - String name of the cluster to select
       @param roleName - String name of the role when working with only one role withing the cluster
       @param resetServerList - boolean True if Running property is to be removed from every server in the cluster
       @return: boolean true of all servers in the cluster are either running or stopped (they don't all have to be the same)  
    '''

    setRewireGlobals(clusterName)
    if (resetServerList):
        # The Running property is set to True or False when an Amazon instance is found to match the server.
        # By removing the Running property the server list is reset to try to rematch all the servers with Amazon instances.
        clearRunningProperty(serverCluster['ServerList'])

    print('Retrieving EC2 Instances from AWS for ' + clusterName + ' at ' + regionEndPoints[serverCluster['EndPoint']])
#     global loadBalancerMap # we need to add instances to the map
    global runningInstanceCount  # save the number of instances running
    runningInstanceCount = 0

    clusterNameRegex = serverCluster['ClusterNameRegex']
    if roleName != None:
        clusterNameRegex = serverCluster['RoleNamePatterns'][roleName]

    namePattern = re.compile(clusterNameRegex)

    instanceListFromXml = describeInstances(None, serverCluster['EndPoint'])

    # build a list of named cluster instances
    serversStable = True
    for instanceFromXml in instanceListFromXml:
        instanceName = getInstanceTag(instanceFromXml, 'Name')
        instanceId = getTextFromSubElement(instanceFromXml, './instanceId')
        if (namePattern.match(instanceName)):
            serverListInstance = getServerListInstance(instanceName, instanceId, roleName)
            if (serverListInstance != None):
                instanceStateCode = getTextFromSubElement(instanceFromXml, './instanceState/code')
                instanceStateCode = str(int(instanceStateCode) & 255)  # we only use the low order byte, mask with binary 11111111
                instanceType = getTextFromSubElement(instanceFromXml, './instanceType')
                if ((instanceStateCode is not None)
                        and ((instanceStateCode == '0') or (instanceStateCode not in instanceStateCodeMap))):
                     # EC2 instance states are 0 (pending), 16 (running), 32 (shutting-down), 48 (terminated), 64 (stopping), and 80 (stopped)
                    serversStable = False
                if (instanceStateCode != '48'):  # don't record a terminated instance
                    running = instanceStateCode == '16'  # 16 means running
                    if (running):
                        runningInstanceCount += 1

                    serverListInstance['Running'] = running

                    setInstanceValues(serverListInstance, instanceFromXml)

    print(str(runningInstanceCount) + ' Running Instances')
    if (verboseOutput):
        print(json.dumps(serverCluster['ServerList'], sort_keys = True, indent = 4))

    print
    for instance in serverCluster['ServerList']:
        instanceType = instance['InstanceType'] if 'InstanceType' in instance else 'unknown'
        if ('InstanceState.Code' in instance):
            instanceStateStr = instance['InstanceState.Code'] + ' ' + instanceStateCodeMap[instance['InstanceState.Code']]
        else:
            instanceStateStr = 'no instance state'
        print(instance['Name']
              + ' {' + instanceType + '} '
              + instanceStateStr)

    print

    return serversStable

def stopAllServers():
    ''' stop all the running servers in the named cluster '''
    ec2ParamMap = {};
    ec2ParamMap['Action'] = 'StopInstances'
    x = 0
    for instance in serverCluster['ServerList']:
        if (('Running' in instance) and instance['Running']):
            x += 1
            setDeleteOnTermination(instance)
            ec2ParamMap['InstanceId.' + str(x)] = instance['InstanceId']

    url = aws_signed_request.aws_signed_request(ec2ParamMap, serverCluster['EC2AwsAccessKey'], serverCluster['EC2AwsSecretKey'], serverCluster['EndPoint'])

    httpResponse = urllib2.urlopen(url);
    if (verboseOutput):
        print('xml response for stop all instances:')
        responseXml = httpResponse.read()
        print(responseXml)

    '''
        each instance eventually ends up with some or all of these key/value pairs
        {
            "DnsName": "ec2-54-159-54-76.compute-1.amazonaws.com", 
            "EbsOptimized": "false", 
            "ImageId": "ami-fef29996", 
            "InstanceId": "i-1f0727e4", 
            "InstanceState.Code": "16", 
            "InstanceType": "m1.small", 
            "IpAddress": "54.159.54.76", 
            "KeyName": "meinc-ec2-1", 
            "Monitoring.Enabled": "false", 
            "Name": "Test 1 Web 1", 
            "NewRunning": "r", 
            "Placement.AvailabilityZone": "us-east-1b", 
            "PrivateIpAddress": "10.236.167.182", 
            "Role": "web", 
            "Route53Name": "lt1-web1.shoutgameplay.com.", 
            "Running": true, 
            "SecurityGroup.1": "test-1", 
            "SecurityGroupId.1": "sg-6a368d03", 
            "Tag.Name": "Test 1 Web 1", 
            "Tag.ScaleAmiId": null or "ami-f2debd9a"
            "BlockDeviceMapping": [
                {
                    "DeviceName":"/dev/sda1",
                    "Ebs.VolumeId":"vol-c5428b8c",
                    "Ebs.Status":"attached",
                    "Ebs.DeleteOnTermination":"false"
                }
            ]
        }

https://ec2.amazonaws.com/?Action=ModifyInstanceAttribute
&InstanceId=i-10a64379
&DisableApiTermination.Value=true
&AUTHPARAMS

&BlockDeviceMapping.1.DeviceName=%2Fdev%2Fsdc
&BlockDeviceMapping.1.VirtualName=ephemeral0
&BlockDeviceMapping.2.DeviceName=%2Fdev%2Fsdd
&BlockDeviceMapping.2.VirtualName=ephemeral1
&BlockDeviceMapping.3.DeviceName=%2Fdev%2Fsdf
&BlockDeviceMapping.3.Ebs.DeleteOnTermination=false
&BlockDeviceMapping.3.Ebs.VolumeSize=100
    '''

def setDeleteOnTermination(instance):
    ''' set all attached volumes to DeleteOnTermination true '''
    for blockDevice in instance['BlockDeviceMapping']:
        if (blockDevice['Ebs.DeleteOnTermination'] == 'false'):
            ec2ParamMap = {};
            ec2ParamMap['Action'] = 'ModifyInstanceAttribute'
            ec2ParamMap['InstanceId'] = instance['InstanceId']
            ec2ParamMap['BlockDeviceMapping.1.DeviceName'] = blockDevice['DeviceName']
            ec2ParamMap['BlockDeviceMapping.1.Ebs.DeleteOnTermination'] = 'true'
            url = aws_signed_request.aws_signed_request(ec2ParamMap, serverCluster['EC2AwsAccessKey'], serverCluster['EC2AwsSecretKey'], serverCluster['EndPoint'])
            if (verboseOutput):
                print('ModifyInstanceAttribute DeleteOnTermination: ' + str(url))
                print('  ' + str(ec2ParamMap))
            httpResponse = urllib2.urlopen(url);
            blockDevice['Ebs.DeleteOnTermination'] = 'true'
            if (verboseOutput):
                print('xml response for ModifyInstanceAttribute ' + instance['InstanceId'])
                responseXml = httpResponse.read()
                print(responseXml)


def stopInstance(instance):
    ''' stop one instance '''
    setDeleteOnTermination(instance)
    if (('Running' in instance) and instance['Running']):
        ec2ParamMap = {};
        ec2ParamMap['Action'] = 'StopInstances'
        ec2ParamMap['InstanceId.1'] = instance['InstanceId']
        url = aws_signed_request.aws_signed_request(ec2ParamMap, serverCluster['EC2AwsAccessKey'], serverCluster['EC2AwsSecretKey'], serverCluster['EndPoint'])
        if (verboseOutput):
            print('stopping server: ' + str(url))
            print('  ' + str(ec2ParamMap))
        httpResponse = urllib2.urlopen(url);
        instance['Running'] = False
        if (verboseOutput):
            print('xml response for stopping ' + instance['Name'])
            responseXml = httpResponse.read()
            print(responseXml)


def startInstance(instance):
    ''' start one instance '''
    if (('Running' in instance) and (not instance['Running'])):
        if (('NewRunning' in instance) and (len(instance['NewRunning']) > 2)):
            modifyInstance(instance)

        ec2ParamMap = {};
        ec2ParamMap['Action'] = 'StartInstances'
        ec2ParamMap['InstanceId.1'] = instance['InstanceId']
        url = aws_signed_request.aws_signed_request(ec2ParamMap, serverCluster['EC2AwsAccessKey'], serverCluster['EC2AwsSecretKey'], serverCluster['EndPoint'])
        if (verboseOutput):
            print('starting server: ' + str(url))
            print('  ' + str(ec2ParamMap))
        try:
            httpResponse = urllib2.urlopen(url);
        except urllib2.HTTPError as e:
            print e.code
            print e.read()

        instance['Running'] = True
        if (verboseOutput):
            print('xml response for starting ' + instance['Name'])
            responseXml = httpResponse.read()
            print(responseXml)


def modifyInstance(instance):
    '''
        If instance contains the NewRunning property that is a valid instanceType
        the instanceType of the amazon instance will be changed to that instanceType.
        
        Change the EbsOptimized of an existing instance to the value in EbsOptomized of instance. 
        @param instance:
    '''
    # instance['NewRunning'] will be set to an instanceType if the new instance is to be started
    if ('NewRunning' in instance and instance['NewRunning'] in instanceTypeDict):
        instance['InstanceType'] = instance['NewRunning']
        ec2ParamMap = {};
        ec2ParamMap['Action'] = 'ModifyInstanceAttribute'
        ec2ParamMap['InstanceType.Value'] = instance['InstanceType']
        ec2ParamMap['InstanceId'] = instance['InstanceId']
        url = aws_signed_request.aws_signed_request(ec2ParamMap, serverCluster['EC2AwsAccessKey'], serverCluster['EC2AwsSecretKey'], serverCluster['EndPoint'])
        if (verboseOutput):
            print('modifying instance: ' + str(url))
            print('  ' + str(ec2ParamMap))
        httpResponse = urllib2.urlopen(url);
        if (verboseOutput):
            print('xml response for modifying instance ' + instance['Name'])
            responseXml = httpResponse.read()
            print(responseXml)

    ec2ParamMap = {};
    ec2ParamMap['Action'] = 'ModifyInstanceAttribute'
    ec2ParamMap['EbsOptimized'] = instance['EbsOptimized']
    url = aws_signed_request.aws_signed_request(ec2ParamMap, serverCluster['EC2AwsAccessKey'], serverCluster['EC2AwsSecretKey'], serverCluster['EndPoint'])
    if (verboseOutput):
        print('modifying instance: ' + str(url))
        print('  ' + str(ec2ParamMap))
    try:
        httpResponse = urllib2.urlopen(url);
    finally:
        if (verboseOutput):
            print('xml response for modifying instance ' + instance['Name'])
            responseXml = httpResponse.read()
            print(responseXml)

def getInstanceIpAddress(instance):
    '''
        Returns the ip address used to access the instance from the location this script is running (public for wan or private to amazon instance).
        @param instance:
        @return: String ip address. 
    '''
    global runningOnEc2Instance
    if runningOnEc2Instance is None:
        print('running on ec2 instance?')
        whoiam = subprocess.check_output("whoami").strip()
        runningOnEc2Instance = (whoiam == 'meinc')

    if (runningOnEc2Instance):
        return instance['PrivateIpAddress']
    else:
        return instance['IpAddress']


def instanceMoveFile(getPut, instance, fromPath, toPath):
    '''
        Gets a file from an instance and stored it local to this script or puts a file on an instance from local to this script.
        @param getPut: String 'get' or 'put'
        @param instance: server instance
        @param fromPath: relative or full path of source file(s) or directory(ies)
        @param toPath: relative or full path of destination file(s) or directory(ies)
    '''
    env.host_string = getInstanceIpAddress(instance)
    if (verboseOutput):
        print(getPut + ' ' + fromPath + ' ' + toPath)

    attempt = 0
    while (True):
        attempt += 1
        try:
            if (getPut == 'get'):
                return get(fromPath, toPath)
            else:
                return put(fromPath, toPath)
        except NetworkError as e:
            print(str(e))
            if (attempt == 20):
                raise

            # got a timeout
            # try again
            print ('waiting to try again')
            sleep(30)


def executeCmdOnInstance(instance, runCommand, useSudo):
    '''
        Makes 20 attempts to connect and sudo execute the runCommand
        every 30 seconds until successful.  After 20 failed attempts,
        10 minutes, the final exception is re-raised.
        @param instance: instance to get the ip address from 
        @param runCommand: string to sudo execute on remote computer
        @return: response from server
        @raise NetworkError:  
    '''
    env.host_string = getInstanceIpAddress(instance)
    if (verboseOutput):
        print(('sudo' if useSudo else 'run') + ' on ' + getInstanceIpAddress(instance))
        print(runCommand)
    attempt = 0
    while (True):
        attempt += 1
        try:
            if (useSudo):
                return sudo(runCommand)
            else:
                return run(runCommand)
        except NetworkError as e:
            print(str(e))
            if (attempt == 20):
                raise

            # got a timeout
            # try again
            print ('waiting to try again')
            sleep(30)


def setHostname(instance):
    '''
        @param instance instance
        sets the hostname of the instance
    '''
    if (verboseOutput):
        print('setting hostname')
        print(json.dumps(instance, indent = 4, sort_keys = True))

    hostname = instance['Route53Name'][:-1]  # remove the trailing period
    runCommand = 'date ; hostn=$(cat /etc/hostname);' \
        + 'sed -i "s/$hostn/' + hostname + '/g" /etc/hosts;' \
        + 'sed -i "s/$hostn/' + hostname + '/g" /etc/hostname;' \
        + 'hostname ' + hostname

    executeCmdOnInstance(instance, runCommand, USE_SUDO)


def copySshKeys(oldInstance, newInstance):
    '''
        @param oldInstance: instance to rsync from
        @param newInstance: instance to rsync to
        The meinc user's authorized_keys file is rsynced from the old instance then rsynced to the new instance.
    '''
    print('copying meinc authorized_keys, host ssh files, and all /root files and directories')
    # get (scp) <meinc-home>/.ssh/authorized_keys from old instance to local file
#     instanceMoveFile('get', oldInstance, '~/.ssh/authorized_keys', '/tmp')
#     # put (scp) local copy of meinc's authorized_keys to new instance
#     instanceMoveFile('put', newInstance, '/tmp/authorized_keys', '~/.ssh/authorized_keys')
#
#     # keep the ssh keys the same on all servers of the same role so scripts and know_hosts files don't have to change
#     runCmd = 'cp /root/.ssh/id_rsa /home/meinc/root_id_rsa ; cp /root/.ssh/id_rsa.pub /home/meinc/root_id_rsa.pub ; cp /etc/ssh/ssh_host_*_key* /home/meinc/ ; cp /etc/ssh/sshd_config /home/meinc/ ; chmod 666 root_id_rsa* ssh_host_*_key* sshd_config'
#     executeCmdOnInstance(oldInstance, runCmd, USE_SUDO)
#     instanceMoveFile('get', oldInstance, '~/root_id_rsa', '/tmp/root_id_rsa')
#     instanceMoveFile('get', oldInstance, '~/root_id_rsa.pub', '/tmp/root_id_rsa.pub')
#     instanceMoveFile('get', oldInstance, '~/ssh_host_*_key*', '/tmp/')
#     instanceMoveFile('get', oldInstance, '~/sshd_config', '/tmp/')
#
#     instanceMoveFile('put', newInstance, '/tmp/root_id_rsa', '~/root_id_rsa')
#     instanceMoveFile('put', newInstance, '/tmp/root_id_rsa.pub', '~/root_id_rsa.pub')
#     instanceMoveFile('put', newInstance, '/tmp/ssh_host_*_key*', '~/')
#     instanceMoveFile('put', newInstance, '/tmp/sshd_config', '~/')
#     executeCmdOnInstance(newInstance, 'chmod 600 ssh_host_*_key root_id_rsa ; chmod 640 root_id_rsa.pub ; chmod 644 ssh_host_*_key.pub sshd_config; chown root:root ssh_host_*_key* root_id_rsa* sshd_config ; mv ssh_host_*_key* /etc/ssh/ ; mv sshd_config /etc/ssh/ ; mv /home/meinc/root_id_rsa /root/.ssh/id_rsa ; mv /home/meinc/root_id_rsa.pub /root/.ssh/id_rsa.pub', USE_SUDO)

    # added --ignore-failed-read for servers that do not have the shout certificate

    runCmd = 'tar -cpPvz --ignore-failed-read --same-owner --exclude-backups --exclude=dead.letter --file rootFiles.tar.gz /root/ /etc/ssh/ssh_host_*_key* /etc/ssh/sshd_config /etc/ssl/certs/*shout*.crt /etc/ssl/private/*shout*.pem /home/meinc/.ssh/authorized_keys ; chmod 666 rootFiles.tar.gz ; tail -200 /root/dead.letter > /root/dead.letter.short ; rm /root/dead.letter ; mv /root/dead.letter.short /root/dead.letter'
    executeCmdOnInstance(oldInstance, runCmd, USE_SUDO)
    instanceMoveFile('get', oldInstance, '~/rootFiles.tar.gz', '/tmp/rootFiles.tar.gz')
    instanceMoveFile('put', newInstance, '/tmp/rootFiles.tar.gz', '~/rootFiles.tar.gz')
    runCmd = 'tar -xpPvz --overwrite --same-owner --file rootFiles.tar.gz ; rm rootFiles.tar.gz ; tail -200 /root/dead.letter > /root/dead.letter.short ; rm /root/dead.letter ; mv /root/dead.letter.short /root/dead.letter'
    executeCmdOnInstance(newInstance, runCmd, USE_SUDO)

'''
tar -cpPvz --same-owner --exclude-backups --exclude=dead.letter --file rootFiles.tar.gz /root/ /etc/ssh/ssh_host_*_key* /etc/ssh/sshd_config /home/meinc/.ssh/authorized_keys ; chmod 666 rootFiles.tar.gz

'''

def rsyncOther(oldInstance, newInstance, rsyncUser, filesCommandConstruct):
    '''
        @param oldInstance: instance to rsync from
        @param newInstance: instance to rsync to
        @param filesCommandConstruct: list of strings used to create the rsync command
         A ssh key pair is created on the new instance for the rsyncUser account.
         The public key is added to the rsyncUser account authorized_keys on the old instance.
         The rsync command is executed on the new instance.
    '''
    # get the the root public ssh key
    response = executeCmdOnInstance(newInstance, "date ; cd ~root/.ssh; cat id_rsa.pub", USE_SUDO)

    if (verboseOutput):
        print('response with public key: ' + response)

    result = re.match('.*(ssh-rsa.*)', response, re.DOTALL)
    publicKey = result.group(1)

    cdSshDir = "cd /opt/meinc/mrsoa/.ssh" if rsyncUser == 'mrsoa' else 'cd .ssh'

    # make sure it is not already in authorized_keys of oldInstance
    runCmd = cdSshDir + ' ; grep "' + publicKey + '" authorized_keys | wc -l'
    response = executeCmdOnInstance(oldInstance, runCmd, USE_SUDO).strip()
    keyMissing = False
    try:
        if (int(response) == 0):
            keyMissing = True
    except:
        keyExists = True

    # if key is not in authorized_keys add it
    if (keyMissing):
        # the default login is to meinc, change to mrsoa home directory if the rsync is from that user
        runCmd = (cdSshDir + ' ; '
            + "touch authorized_keys ; "  # make sure the file exists
            + "grep -v 'root@" + newInstance['Route53Name'][:-1] + "' authorized_keys > authorized_keys.tmp ; "
            + "mv authorized_keys authorized_keys.bak ; "
            + "mv authorized_keys.tmp authorized_keys ; "
            + "echo " + publicKey + ">>authorized_keys ; "  # create the new entry
            + "chown " + rsyncUser + ":" + rsyncUser + " authorized_keys; "  # make sure the owner and group are correct
            + "chmod 600 authorized_keys")

        executeCmdOnInstance(oldInstance, runCmd, USE_SUDO)  # make sure the permissions are correct

    # rsync the files
    # run the rsync command
    # concat the pieces of the rsync command into a single string
    # do it with empty string because some of the pieces my require that
    # any required whitespace must be included in the pieces themselves
    runCommand = ''.join(filesCommandConstruct)
    # replace the <source> flag with the source ip address
    runCommand = runCommand.replace('<source>', oldInstance['PrivateIpAddress'])

    warn_only_hold = env.warn_only
    env.warn_only = True
    executeCmdOnInstance(newInstance, 'ps -ef | grep mrsoa', not USE_SUDO)
    executeCmdOnInstance(newInstance, 'date ; ' + runCommand, USE_SUDO)
    env.warn_only = warn_only_hold


def cloneToHost(instance, newInstance):
    '''
        @param instance:
        @param newinstance
        executes a CLONE instruction to the old instance
        which will clone all the global documents to the new instance
        e.g. curl -v -H "X-Clone-To-Host: dc99-wds-origin2.shoutgameplay.com" http://dc99-wds-origin1.shoutgameplay.com:40811/CLONE
    '''
    headers = { 'X-Clone-To-Host' : newInstance['PrivateIpAddress'] }
    url = 'http://' + getInstanceIpAddress(instance) + ':81/CLONE'
    # this is a test url that will return the headers sent with the request
    #    url = 'http://headers.cloxy.net/request.php'
    print('cloning global wds documents from: ' + url + ' to: ' + headers['X-Clone-To-Host'])
    req = urllib2.Request(url = url, headers = headers)
    response = urllib2.urlopen(req)
    the_page = response.read()
    print("response: " + the_page)


def setMrsoaMemory(instance):
    # instanceTypeDict memory is specified in gigs
    # MrSOA memory is specified in megs
    # allocate 6% of the memory to the OS, but never less than 500mb or more then 1 gig
    instanceMem = int(float(instanceTypeDict[instance['InstanceType']]) * 1024)  # convert gigs to megs
    maxMem = instanceMem - 1024
    startMem = 2048 if maxMem > 2048 else 1024
    runCmd = 'date ; cd /opt/meinc/mrsoa/bin ; ' \
            + "sed -ri 's/^(\s*MRSOA_OPTS=\")-Xms[0-9]+m\s+-Xmx[0-9]+m\s*\\\\\\$/\\1-Xms" + str(startMem) + "m -Xmx" + str(maxMem) + "m \\\\\\/' mrsoa.sh"
    executeCmdOnInstance(instance, runCmd, USE_SUDO)
    return


def stopAllMrSoaServices():
    '''
        Stops the MrSoa service on all running "MrSoa" servers in the serverCluster.
    '''
    for instance in serverCluster['ServerList']:
        if (('Running' in instance) and (instance['Running'])):
            if ((instance['Role'] in roleSettings) and (roleSettings[instance['Role']].isMrSoaServer)):
                alterService(instance, 'mrsoa', 'force-stop')


def startAllMrSoaServices():
    '''
        Starts MrSoa service on all running "MrSoa" servers in the serverCluster.
    '''
    for instance in serverCluster['ServerList']:
        if (('Running' in instance) and (instance['Running'])):
            if ((instance['Role'] in roleSettings) and (roleSettings[instance['Role']].isMrSoaServer)):
                alterService(instance, 'mrsoa', 'start')


def alterService(instance, service, action):
    '''
        @param instance: instance dictionary
        @param service: service name
        @param action: e.g. start stop force-stop 
        changes the state of the service on the instance
    '''
    if (verboseOutput):
        executeCmdOnInstance(instance, 'ps -ef | grep ' + service, not USE_SUDO)

    executeCmdOnInstance(instance, 'date ; service ' + service + ' ' + action, USE_SUDO)

#     # verify the service has started or stopped
#     serviceRunning = False
#     if (action == 'start'):
#         serviceRunning = True
#
#     alterComplete = False
#     while not alterComplete:
#         print('waiting for ' + service + ' to ' + action)
#         sleep(15)
#         lineCount = int(executeCmdOnInstance(instance, 'ps -ef | grep "' + service + '" | grep -v grep | wc -l', not USE_SUDO))
#         alterComplete = (serviceRunning and (lineCount > 0)) or ((not serviceRunning) and (lineCount == 0))


def startWDS(instance):
    '''
        @param instance
        starts the nginx service on the wds instance
    '''
    executeCmdOnInstance(instance, 'date ; service nginx start', USE_SUDO)


def verifyServiceIsRunning(instance):
    '''
        @param instance:
        does an http request from the root of the instance
        any http response with any status is considered verified 
    '''
    request = 'http://' + getInstanceIpAddress(instance)
    # try this request for three minutes, then fail
    attemptNum = 0
    while (True):
        try:
            attemptNum += 1
            print('attempt ' + str(attemptNum) + ' verifying service has started at ' + request)
            httpResponse = urllib2.urlopen(url = request, timeout = 15)
            # got a good status, it's running
            break
        except (urllib2.HTTPError) as e:
            # got a bad status, but that means it's running
            print(str(e))
            break
        except (urllib2.URLError) as e:
            print(str(e))
            if (attemptNum < 6):
                print('waiting for service to start')
                sleep(30)  # wait 30 seconds and try again
                continue

            raise


def verifyEmptyDirectories(instance, directoryList):
    '''
        @param instance: instance to check
        @param directoryList: list of strings of full paths to directories that must be empty 
        verify directories are empty, e.g. collector messages processed and email has all been sent
        We have already waited 6 minutes for the clients to stop asking for things from the server
        wait a maximum of 3 more minutes (6 x 30 seconds) for all directories to be empty
    '''
    attemptCount = 0
    for path in directoryList:
        runCmd = 'date ; find ' + path + ' -type f | wc -l'
        while (True):
            results = executeCmdOnInstance(instance, runCmd, USE_SUDO)
            regexSearchResult = re.search('.*\s+(\d+)$', results)
            fileCount = int(regexSearchResult.group(1))
            if (fileCount == 0):
                break  # break out of the while(true) loop

            attemptCount += 1
            if (attemptCount < 6):
                print('waiting for ' + path + ' to empty')
                sleep(30)
                continue
            else:
                errMsg = 'ERROR: ' + path + ' did not empty, ' + str(fileCount) + ' files remain'
                print(errMsg)
                raise Exception(errMsg)


def clearCache(instance):
    '''
        @param instance: wds-cache instance
        empty the cache directories
         /var/cache/nginx/cache/*
         /var/cache/nginx/tmp/*
    '''
    runCmd = r'rm -rf /var/cache/nginx/cache/* /var/cache/nginx/tmp/*'
    executeCmdOnInstance(instance, 'date ; ' + runCmd, USE_SUDO)


def connectWdsCacheToOrigin(instance):
    '''
        run this AFTER the hostname has been set
    
        #!/bin/bash
        
        DOMAIN=$(hostname -d)
        HOST=$(hostname -s)        # lt1-wds1 or wds1
        
        HOSTPREFIX=$(echo $HOST | cut -d"-" -f1)
        #previous line did not work in production wds3 because there is no dash like in lt1-wds3
        # what if we just go after the trailing digits?
        
        HOSTSUFFIX=${HOST:$((${#HOSTPREFIX}+1))}
        HOSTNUM=$(echo $HOSTSUFFIX | grep -o '[0-9]\+$' | head -n1)
        
        sed -i -r -e "s/([[:alnum:]]+-wds-origin)[0-9]+/\1$HOSTNUM/g" /etc/nginx/sites-available/shout_wds_cache
        # previous line expects all hostnames to have something before wds-origin like lt1-  production does not
        #  What if we just look for (wds-origin)[0-9]+  or  ([[:alnum:]|-]*)wds-origin)[0-9]*
        
        NSIP=$(grep nameserver /etc/resolv.conf | head -n1 | cut -d" " -f2)
        sed -i -r -e "s/^( +resolver +)[0-9.]+ /\1$NSIP /" /etc/nginx/sites-available/shout_wds_cache
        
        ORGFQDN="$HOSTPREFIX-wds-origin$HOSTNUM.$DOMAIN"
        ORGIP=$(getent hosts "$ORGFQDN" | cut -d" " -f1)
        sed -i -r -e "s/(server +)[[:alnum:]_.\-]+;/\1$ORGIP;/g" /etc/nginx/sites-available/shout_wds_cache
        
        runCmd = r'echo 1 ; HOST=$(hostname -s) ; ' \
            + r'echo 2 $HOST ; HOSTPREFIX=$(echo $HOST | cut -d"-" -f1) ; ' \
            + r'echo 3 $HOSTPREFIX ; DOMAIN=${HOSTNAME:$((${#HOST}+1))} ; ' \
            + r'echo 4 $DOMAIN ; HOSTSUFFIX=${HOST:$((${#HOSTPREFIX}+1))} ; ' \
            + r"echo 5 $HOSTSUFFIX ; HOSTNUM=$(echo $HOSTSUFFIX | grep -o '[0-9]\+$' | head -n1) ; " \
            + r'echo 6 $HOSTNUM ; sed -i -r -e "s/(.*wds-origin)[0-9]+/\1$HOSTNUM/g" /etc/nginx/sites-available/shout_wds_cache ; ' \
            + r'echo 7 ; NSIP=$(grep nameserver /etc/resolv.conf | head -n1 | cut -d" " -f2) ; ' \
            + r'echo 8 $NSIP ; sed -i -r -e "s/^( +resolver +)[0-9.]+ /\1$NSIP /" /etc/nginx/sites-available/shout_wds_cache ; '
        
    '''

    runCmd = r'echo 1 ; HOST=$(hostname -s) ; ' \
        + r"echo 2 $HOST ; HOSTNUM=$(echo $HOST | sed -r -e 's/.*wds([0-9]+)/\1/') ; " \
        + r'echo 3 $HOSTNUM ; sed -i -r -e "s/(.*wds-origin)[0-9]+/\1$HOSTNUM/g" /etc/nginx/sites-available/shout_wds_cache ; ' \
        + r'echo 4 ; NSIP=$(grep nameserver /etc/resolv.conf | head -n1 | cut -d" " -f2) ; ' \
        + r'echo 5 $NSIP ; sed -i -r -e "s/^( +resolver +)[0-9.]+ /\1$NSIP /" /etc/nginx/sites-available/shout_wds_cache ; '
        # \
        # the following lines are used by docker to change the domain name to an ip address because the servers all reside in a single docker cluster on one machine
        # we can't change to an ip address on loadtest becuase we shut down and restart servers, the ip addresses change, therefore we have to use domain names
#         + r'echo 9 ; ORGFQDN="$HOSTPREFIX-wds-origin$HOSTNUM.$DOMAIN" ; ' \
#         + r'echo 10 $ORGFQDN ; ORGIP=$(getent hosts "$ORGFQDN" | cut -d" " -f1) ; ' \
#         + r'echo 11 $ORGIP ; sed -i -r -e "s/(server +)[[:alnum:]_.\-]+;/\1$ORGIP;/g" /etc/nginx/sites-available/shout_wds_cache '

    executeCmdOnInstance(instance, 'date ; ' + runCmd, USE_SUDO)


def updateWdsEndpointsConfig(ignoreInstance = None):
    '''
        update the /opt/meinc/meinc.properties file in all mrsoa servers
            webdatastore.shoutweb.origin.hosts and webdatastore.shoutweb.cache.hosts entries
        @param instance: the instance 
    '''
    # get all the running mrsoa servers and count the running wds-origin servers
    runningInstanceList = []
    wdsOriginCount = 0
    for instance in serverCluster['ServerList']:
        if (('Running' in instance) and (instance['Running'])):
            if ((ignoreInstance is not None) and (instance['InstanceId'] == ignoreInstance['InstanceId'])):
                continue

            if (instance['Role'] == 'wds-origin'):
                wdsOriginCount += 1

            if ((instance['Role'] in roleSettings) and (roleSettings[instance['Role']].isMrSoaServer)):
                runningInstanceList.append(instance)

    # create the new origin and cache list
    # e.g.
    # <1>lt1-wds-origin1.shoutgameplay.com,<2>lt1-wds-origin2.shoutgameplay.com
    # <1>lt1-wds1.shoutgameplay.com,<2>lt1-wds2.shoutgameplay.com
    wdsOriginList = []
    wdsCacheList = []
    for i in range(1, wdsOriginCount + 1):
        wdsOriginList.append('<' + str(i) + '>' + serverCluster['RoleNamePatterns']['wds-origin']['Route53NameRegex'][:-1].replace('(\d+)', str(i)))
        wdsCacheList.append('<' + str(i) + '>' + serverCluster['RoleNamePatterns']['wds-cache']['Route53NameRegex'][:-1].replace('(\d+)', str(i)))

    wdsOriginHosts = ','.join(wdsOriginList)
    wdsCacheHosts = ','.join(wdsCacheList)

    # connect to each and update the properties file
    for instance in runningInstanceList:
        runCmd = 'date ; cd /opt/meinc/ ; ' \
                + "sed -r -i 's/webdatastore.shoutweb.origin.hosts=.*/webdatastore.shoutweb.origin.hosts=" + wdsOriginHosts + "/ ; " \
                + "s/webdatastore.shoutweb.cache.hosts=.*/webdatastore.shoutweb.cache.hosts=" + wdsCacheHosts + "/' meinc.properties"
        executeCmdOnInstance(instance, runCmd, USE_SUDO)

        # TODO: Matt do we need to restart mrsoa here?  That doesn't seem good!


def updateSrdConfig(ignoreInstance = None, verifySrd = True):
    '''
        update the /opt/meinc/meinc.properties file in all sync servers
            gameplay.srddaemon.srd.server.auth
            gameplay.srddaemon.srd.server.collector
            gameplay.srddaemon.srd.server.vscollect
            gameplay.srddaemon.srd.server.sync
            gameplay.srddaemon.srd.server.wds
            
        @param instance: the instance 
    '''
    # get all the running sync servers
    # count the number of running auth, collector, sync, and wds-cache servers
    serverCount = {
        'auth' : 0,
        'collector' : 0,
        'sync' : 0,
        'wds-cache' : 0
    }
    runningSyncInstanceList = []
    for instance in serverCluster['ServerList']:
        if (('Running' in instance) and (instance['Running'])):
            if ((ignoreInstance is not None) and (instance['InstanceId'] == ignoreInstance['InstanceId'])):
                continue

            if (instance['Role'] in serverCount):
                serverCount[instance['Role']] += 1

            if (instance['Role'] == 'sync'):
                runningSyncInstanceList.append(instance)

    # create the new meinc.properties values
    # e.g.
    # lt1-auth1.shoutgameplay.com,lt1-auth1-red1.shoutgameplay.com|lt1-auth2.shoutgameplay.com,lt1-auth2-red1.shoutgameplay.com
    # NOTE this code and rewire_cluster_config namedClusters[<name>][<role>] only supports ONE optional redirect dns name
    valueStrings = {}
    for role in serverCount:
        valueList = []
        for i in range(1, serverCount[role] + 1):
            valueStr = serverCluster['RoleNamePatterns'][role]['Route53NameRegex'][:-1].replace('(\d+)', str(i))
            if ('Route53RedirectNameRegex' in serverCluster['RoleNamePatterns'][role]):
                valueStr += ',' + serverCluster['RoleNamePatterns'][role]['Route53RedirectNameRegex'][:-1].replace('(\d+)', str(i))

            valueList.append(valueStr)

        valueStrings[role] = '|'.join(valueList)  # join with pipe

    # connect to each and update the properties file
    for syncInstance in runningSyncInstanceList:
        runCmd = 'date ; cd /opt/meinc/'
        for role in valueStrings:
            roleStr = 'wds' if role == 'wds-cache' else role
            runCmd += " ; sed -r -i 's/gameplay.srddaemon.srd.server." + roleStr \
                + "=.*/gameplay.srddaemon.srd.server." + roleStr \
                + "=" + valueStrings[role] + "/' meinc.properties "
            if (roleStr == 'collector'):
                runCmd += " ; sed -r -i 's/gameplay.srddaemon.srd.server.vscollect" \
                    + "=.*/gameplay.srddaemon.srd.server.vscollect" \
                    + "=" + valueStrings[role] + "/' meinc.properties "

        executeCmdOnInstance(syncInstance, runCmd, USE_SUDO)

    if (verifySrd):
        checkSrd2(serverCount)


def checkSrd2(serverCount):
    '''
        Gets the Srd2 document. Verifies that it matches what is expected based on number of each server role passed in serverCount.
        check every 15 seconds for up to 3 minutes
        @param serverCount: dict of server role/count
        @raise exception: if srd2 is not updated withing 3 minutes. 
    '''
    checkCnt = 12
    srdOk = False
    while(not srdOk):
        print('waiting for srd to update')
        sleep(15)  # wait 15 seconds and try again
        print('checking srd')

        try:
            httpResponse = urllib2.urlopen(serverCluster['Srd2Url'])
            httpHeaders = httpResponse.info()
            if (('Content-Encoding' in httpHeaders) and (httpHeaders['Content-Encoding'] == 'gzip')):
                zippedResponse = httpResponse.read()
                jsonStr = GzipFile('', 'r', 0, StringIO(zippedResponse)).read()
            else:
                jsonStr = httpResponse.read()

            srd2 = json.loads(jsonStr)
        except Exception, errMsg:
            e = sys.exc_info()[0]
            print('Exception: ' + str(e) + ' ' + str(errMsg))
            checkCnt -= 1
            if (checkCnt == 0):
                raise Exception('srd2 did not update in 3 minutes')

            continue

        if (srd2['docType'] != 'srd/2.0'):
            print(json.dumps(srd2, sort_keys = True, indent = 4))
            raise Exception('srd2 docType: ' + srd2['docType'])

        if ((len(srd2['server']['auth']['domainNameSets']) == serverCount['auth'])
                and (len(srd2['server']['collector']['domainNameSets']) == serverCount['collector'])
                and (len(srd2['server']['sync']['domainNameSets']) == serverCount['sync'])
                and (len(srd2['server']['wds']['domainNameSets']) == serverCount['wds-cache'])
                and (len(srd2['server']['vscollect']['domainNameSets']) == serverCount['collector'])):
            srdOk = True
            print(json.dumps(srd2, sort_keys = True, indent = 4))
        else:
            if (verboseOutput):
                print(jsonStr)
            # srd2 not correct yet, sleep and try again
            checkCnt -= 1
            if (checkCnt == 0):
                raise Exception('srd2 did not update in 3 minutes')



def scaleServer(instance, roleSetting, sequenceType, rsyncType = None, instanceName = None, instanceNumber = None, route53Name = None, instanceTypeList = [None, None], imageIdList = [None, None], noSleep = False):
    '''
        @param instance: the instance to start with (old instace to be vertically scaled
          or existing instance to be duplicated for horizontal scale or instance to stop)
        @param roleSetting: RoleSetting object
        @param sequenceType: vscale or hscaleup or hscaledown
        @param rsyncType: vscale or hscale
        @param instanceName: name to set the instance to
        @param instanceNumber: seqential number this instance is e.g. 3 for lt1-sync3 
        @param route53Name: dsn name to assign to new instance
        @param instanceTypeList: list of size of new instance to start, if None size of instance will be used.  e.g. m1.Xlarge  [0] is for role passed in, [1] is for wds-cache only if wds-origin is being scaled
        @param imageIdList: list of ami imageIds to use for the new instances [0] is for the role passed in, [1] is for wds-cache only if wds-origin is being scaled
        @param noSleep: boolean ignore sleep in the sequences (warning, if clients are using servers it can cause problems)
        @return: new server instance dictionary
        
        work on one instance at a time
        this is so we don't run into the amazon limit of instances we can have running at one time
        and to limit the impact on things like hazelcast
    '''

    newInstance = None
    for sequenceStep in roleSetting.scaleSequence[sequenceType]:

        sequenceStepLower = sequenceStep.strip().lower()

        if (sequenceStepLower.startswith('sleep')):
            if (noSleep):
                print('noSleep ' + sequenceStep)
                continue

            sleepTime = sequenceStepLower[5:].strip()
            print('Sleeping ' + sleepTime + ' seconds')
            sleep(int(sleepTime))  # sleeping seconds for services to start and dns entries to expire.
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'runnewinstance'):
            # start one new instance
            newInstance = runInstance(instance, instanceTypeList[0], imageIdList[0])
            newInstance['Route53Name'] = route53Name
            serverCluster['ServerList'].append(newInstance)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower.startswith('forcestopservice')):
            service = sequenceStepLower[16:].strip()
            # force stop the service, probably becuase it started automatically (mrsoa, nginx)
            alterService(newInstance, service, 'force-stop')
            continue

        if (sequenceStepLower == 'sethostname'):
            # start one new instance
            setHostname(newInstance)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'copyfiles'):
            # copy from existing instance to new instance
            copySshKeys(instance, newInstance)
            rsyncOther(instance, newInstance, roleSetting.rsyncUser, roleSetting.files[rsyncType])
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'clonetohost'):
            # clone the global wds files from instance to newInstance
            cloneToHost(instance, newInstance)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'setmrsoamemory'):
            # adjust the memory settings in the /opt/meinc/mrsoa/bin/mrsoa.sh file
            setMrsoaMemory(newInstance)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower.startswith('stopservice')):
            service = sequenceStepLower[12:].strip()
           # stop the service on the new instance, e.g. mrsoa, nginx
            alterService(newInstance, service, 'stop')
            continue  # go to next sequence step in for loop

        if (sequenceStepLower.startswith('startservice')):
            service = sequenceStepLower[12:].strip()
           # start the service on the new instance, e.g. mrsoa, nginx
            alterService(newInstance, service, 'start')
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'verifyservice'):
            # verify the mrsoa service is running
            verifyServiceIsRunning(newInstance)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'startwds'):
            # start the mrsoa service on the new instance
            startWDS(newInstance)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'route53'):
            # wire the route53 entry to the new instance
            updateRoute53([newInstance])
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'verifydns'):
            # wire the route53 entry to the new instance
            verifyDns(newInstance)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'verifyoldempty'):
            # wait until collector and mail directories are empty on old server
            verifyEmptyDirectories(instance, roleSetting.files['emptyDirectories'])
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'hscaleupwdscache'):
            # find the first wdscache instance
            cacheRunningInstanceList = getRunningInstancesList('wds-cache')
            cacheImageId = selectImageId(cacheRunningInstanceList, imageIdList[1])
            cacheInstanceType = selectInstanceType(cacheRunningInstanceList, instanceTypeList[1])
            cacheInstanceToCopy = cacheRunningInstanceList[0]
            cacheRoute53Name = serverCluster['RoleNamePatterns']['wds-cache']['Route53NameRegex']
            cacheRoute53Name = cacheRoute53Name.replace('(\d+)', str(instanceNumber))
            cacheInstanceName = serverCluster['RoleNamePatterns']['wds-cache']['InstanceNameRegex']
            cacheInstanceName = cacheInstanceName.replace('(\d+)(?: .*)?', str(instanceNumber))
            scaleServer(cacheInstanceToCopy, roleSettings['wds-cache'], 'hscaleup', 'hscale', cacheInstanceName, instanceNumber, cacheRoute53Name, [cacheInstanceType, None], [cacheImageId, None], noSleep)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'hscaledownwdsorigin'):
            print('scale down origin')
            # stop the wds origin server that matches this wds cache server
            originRunningInstanceList = getRunningInstancesList('wds-origin')
            originInstanceName = serverCluster['RoleNamePatterns']['wds-origin']['InstanceNameRegex']
            originInstanceName = originInstanceName.replace('(\d+)(?: .*)?', str(instanceNumber))
            print('looking for ' + originInstanceName)
            for originInstance in originRunningInstanceList:
                print('checking running origin ' + originInstance['Name'])
                if ('Name' in originInstance and originInstance['Name'] == originInstanceName):
                    scaleServer(originInstance, roleSettings['wds-origin'], 'hscaledown', noSleep = noSleep)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'updatewdsendpoints'):
            if sequenceType == 'hscaledown':
                updateWdsEndpointsConfig(instance)
            else:
                updateWdsEndpointsConfig()

            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'updatesrdconfig'):
            if sequenceType == 'hscaledown':
                updateSrdConfig(instance)  # send instance to ignore when collecting running instances
            else:
                updateSrdConfig()

            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'updateroundrobindns'):
            if (instance['Role'] == 'sync'):
                if (sequenceType == 'hscaledown'):
                    updateSyncRoundRobinDns(instance, 'DELETE')
                else:
                    updateSyncRoundRobinDns(newInstance, 'UPSERT')

        if (sequenceStepLower == 'clearcache'):
            clearCache(newInstance)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'connecttooriginwds'):
            # run this AFTER the hostname has been set
            connectWdsCacheToOrigin(newInstance)
            continue  # go to next sequence step in for loop

        if (sequenceStepLower.startswith('stopoldinstance')):
            # TODO stop of mrsoa service keeps failing, need to research and fix
            # Matt said it looks like it should have worked but did not
            # on lt1-auth1 when he took a quick look at it

#             service = sequenceStepLower[16:].strip()
#             if (len(service) > 0):
#                # stop the service on the old instance, e.g. mrsoa, nginx
#                 alterService(instance, service, 'stop')

            # stop old instance
            stopInstance(instance)
            # always rename stopped instance by appending (Stopped yyyy-mm-dd hh:mm:ss)
            setInstanceName(instance, instance['Tag.Name'] + ' (Stopped ' + str(datetime.utcnow())[:19] + ')')
            continue  # go to next sequence step in for loop

        if (sequenceStepLower == 'setnewinstancename'):
            # set new instance name
            scaleAmiId = instance['Tag.ScaleAmiId'] if ((sequenceType == 'vscale') and ('Tag.ScaleAmiId' in instance)) else None
            setInstanceName(newInstance, instanceName, scaleAmiId)
            continue  # go to next sequence step in for loop

    return newInstance


def runInstance(instance, instanceType = None, imageId = None):
    ''' run one new instance from ami using old instance as template '''
    ec2ParamMap = {};
    ec2ParamMap['Action'] = 'RunInstances'
    ec2ParamMap['ImageId'] = instance['ImageId'] if (imageId is None) else imageId
    instanceType = instance['InstanceType'] if (instanceType is None) else instanceType
    ec2ParamMap['InstanceType'] = instanceType
    ec2ParamMap['KeyName'] = instance['KeyName']
    ec2ParamMap['Monitoring.Enabled'] = instance['Monitoring.Enabled']
    ec2ParamMap['SecurityGroup.1'] = instance['SecurityGroup.1']
    ec2ParamMap['SecurityGroupId.1'] = instance['SecurityGroupId.1']

    ec2ParamMap['MinCount'] = '1'
    ec2ParamMap['MaxCount'] = '1'
    ebsOptimized = instance['EbsOptimized'] if instanceType.endswith('xlarge') else 'false'
    ec2ParamMap['EbsOptimized'] = ebsOptimized

    # letting amazon do the zone for now. We may want to set it for better comm between servers.
    # ec2ParamMap['Placement.AvailabilityZone'] = newServer['Placement.AvailabilityZone']

    url = aws_signed_request.aws_signed_request(ec2ParamMap, serverCluster['EC2AwsAccessKey'], serverCluster['EC2AwsSecretKey'], serverCluster['EndPoint'])
    if (verboseOutput):
        print('running new instance of ' + instance['Role'] + ': ' + str(url))
        print('  ' + str(ec2ParamMap))
    httpResponse = urllib2.urlopen(url);

    rootElement = getRootElementFromHttpResponseXml(httpResponse)

    newItemFromXml = rootElement.find('.//instancesSet/item')
    newInstanceId = getTextFromSubElement(newItemFromXml, './instanceId')
    instanceStateCode = getTextFromSubElement(newItemFromXml, './/instanceState/code')

    # wait for new instance to be stable
    print('waiting for new instance to be running')
    sleepTime = 20
    while (instanceStateCode != '16'):
        sleep(sleepTime)
        newItemFromXml = describeInstances([newInstanceId], serverCluster['EndPoint'])[0]
        instanceStateCode = getTextFromSubElement(newItemFromXml, './/instanceState/code')
        print('instanceStateCode: ' + instanceStateCode + ' ' + instanceStateCodeMap[instanceStateCode])
        if (int(instanceStateCode) > 16):
            print('instance state error')
            exit(1)
        # see instanceStateCodeMap
        sleepTime = 10

    newInstance = {}
    setInstanceValues(newInstance, newItemFromXml)
    newInstance['Running'] = True
    # copy rewire_cluster_config settings for this instance
    newInstance['Role'] = instance['Role']
    print('giving os and services time to start')
    sleep(90)
    return newInstance

def sleep(timeToSleep):
    curses.setupterm()
    ''' sleep for timeToSleep seconds with countdown '''
    while timeToSleep > 0:
        timeToSleep -= 1
        sys.stdout.write(str(timeToSleep))
        sys.stdout.flush()
        time.sleep(1)
        sys.stdout.write(curses.tparm(curses.tigetstr("cub"), 10))
        sys.stdout.write(curses.tigetstr("dl1"))

    sys.stdout.flush()


def getEbsOptimizedInput(instance):
    '''
        Prompt user whether server storage shoult be EbsOptimized.
    '''
    if ((len(instance['NewRunning']) < 2) or (not instance['NewRunning'].endswith('xlarge'))):
        instance['EbsOptimized'] = 'false'
        return

    while True:
        ebsOptimizedInput = raw_input('EbsOptimized? y/n [' + ('y' if instance['EbsOptimized'] else 'n') + ']')
        ebsOptimizedInput = ebsOptimizedInput.strip().lower()
        if (len(ebsOptimizedInput) == 0):
            return # no change
        
        if (ebsOptimizedInput == 'y'):
            instance['EbsOptimized'] = 'true'
            return

        if (ebsOptimizedInput == 'n'):
            instance['EbsOptimized'] = 'false'
            return

        print('Invalid input, enter y or n or leave blank')


def startOrStopInstances(clusterName):
    '''
        If there are any running instances ask if they should be stopped.  If yes stop them and terminate.
        Otherwise list each instance one at a time and ask if it should be running or stopped after this script is complete,
        then go through the list and get each instance to match the input given.
        
        TODO: fix this bug
        !!! KNOWN BUG  entering a instance type instead of r or s does not work.  I don't know what the problem is yet. !!!
    '''
    if runningInstanceCount > 0:
        # there are running instances, ask if they should be stopped
        print('Current instances running for ' + clusterName + ":")
        for instance in serverCluster['ServerList']:
            if (('Running' in instance) and instance['Running']):
                print(instance['Name'] + ' {' + instance['InstanceType'] + '}')
        stopServers = raw_input('Do you want to stop all servers? y/n ')
        if (stopServers.lower() == 'y'):
            stopAllServers()
            exit()

    # ask user if each instance should be running or stopped
    changeRequested = False
    print('Enter r for run, s for stop, or leave blank for no change:')
# bug instance type change broken
#    print('Enter r for run, s for stop, instanceType to change size and run, or leave blank for no change:')
    for instance in serverCluster['ServerList']:
        if ('Running' in instance):
            acceptableInput = False
            while not acceptableInput:
                rawInput = raw_input(instance['Name'] + ' {' + instance['InstanceType'] + '} [' + ('r' if instance['Running'] else 's') + ']')
                rawInput = rawInput.strip().lower()
                if (len(rawInput) == 0):
                    rawInput = 'r' if instance['Running'] else 's'
                elif (len(rawInput) > 1):
                    if (rawInput == instance['InstanceType']):
                        # user asked for existing instance type, just run it
                        rawInput = 'r'

                if ((rawInput != 'r') and (rawInput != 's') and (rawInput not in instanceTypeDict)):
                    print('invalid input: leave blank, enter r or s, or a new instance type from this list - ' + ", ".join(instanceTypeDict.keys()))
# bug instance type change broken
#                    print('invalid input: leave blank, enter r or s, or a new instance type from this list - ' + ", ".join(instanceTypeDict.keys()))
                else:
                    acceptableInput = True
                    instance['NewRunning'] = rawInput
                    getEbsOptimizedInput(instance)
                    if (
                        (len(rawInput) > 1)
                        or ((instance['Running']) and (instance['NewRunning'] == 's'))
                        or ((not instance['Running']) and (instance['NewRunning'] == 'r'))
                        ):
                        
                        changeRequested = True

    if (verboseOutput):
        print('changeRequested: ' + ('True' if changeRequested else 'False'))
        print(json.dumps(serverCluster['ServerList'], sort_keys = True, indent = 4))

    # process start or stop instructions in the order specified by startupSequence
    # DB and Web instances must be started and given time to completely start before starting others
    startedInstanceWaitNeeded = False
    startedInstanceSleepNeeded = False
    route53UpdateNeeded = True  # always run it at least once

    # start or stop instances as needed
    for sequenceStep in serverCluster['StartupSequence']:
        sequenceStepLower = sequenceStep.strip().lower()
        if (sequenceStepLower.startswith('sleep')):
            if (startedInstanceSleepNeeded):
                startedInstanceSleepNeeded = False
                sleepTime = sequenceStep[5:].strip()
                print('Sleeping ' + sleepTime + ' seconds for services to start and dns entries to expire before continuing....')
                sleep(int(sleepTime))  # sleeping seconds for services to start and dns entries to expire.

            continue  # go to next startup sequence instance type in for loop

        if (sequenceStepLower == 'wait'):
            if (startedInstanceWaitNeeded):
                startedInstanceWaitNeeded = False
                waitForStableServers(clusterName)

            continue  # go to next startup sequence instance type in for loop

        if (sequenceStepLower == 'route53'):
            if (route53UpdateNeeded):
                updateRoute53(serverCluster['ServerList'])
                route53UpdateNeeded = False

            continue  # go to next startup sequence instance type in for loop

        if (sequenceStepLower.startswith('prompt')):
            raw_input(sequenceStep[6:].strip() + ', press enter to continue: ')

            continue  # go to next startup sequence instance type in for loop

        # the sequenceStep should be one listed in the servers
        for instance in serverCluster['ServerList']:
            if ((instance['Role'] == sequenceStep) and ('Running' in instance)):
                if (instance['Running']):
                    if (instance['NewRunning'] != 'r'):
                        # stop one instance
                        print('stopping instance ' + instance['Name'])
                        stopInstance(instance)

                if ((not instance['Running']) and ('NewRunning' in instance) and (instance['NewRunning'] != 's')):
                    # start one instance
                    startedInstanceWaitNeeded = True
                    startedInstanceSleepNeeded = True
                    route53UpdateNeeded = True
                    print('starting instance ' + instance['Name'])
                    startInstance(instance)


def waitForStableServers(clusterName):
    # if we were only shutting down instances we still may need to update the dns
    # get the updated data
    serversStable = False
    waitTime = 30
    while (not serversStable):
        print('Waiting for servers to be stable...')
        sleep(waitTime)
        waitTime = waitTime - 10
        if (not waitTime): waitTime = 10
        # update the instanceMap
        serversStable = getInstances(clusterName, resetServerList = True)


# return an xml string for changing the route53 dns cname entries
def getXmlForDnsChanges(instanceList):
    '''
       build the xml to update the CNAME entries for the test instances in the route53 dns
       using xml formatted per http://docs.aws.amazon.com/Route53/latest/APIReference/API_ChangeResourceRecordSets.html
    '''
    instancesRunning = 0
    changesElement = ET.Element('Changes')
    if(verboseOutput):
        print('getXmlForDnsChanges from:')
        print(json.dumps(instanceList, sort_keys = True, indent = 4))
    for instance in instanceList:
        if (('Running' in instance) and instance['Running']):
            instancesRunning += 1
            changeElement = ET.Element('Change')
            actionElement = ET.Element('Action')
            actionElement.text = 'UPSERT'
            changeElement.append(actionElement)
            resourceRecordSetElement = ET.Element('ResourceRecordSet')
            nameElement = ET.Element('Name')
            nameElement.text = instance['Route53Name']
            resourceRecordSetElement.append(nameElement)
            typeElement = ET.Element('Type')
            typeElement.text = 'CNAME'
            resourceRecordSetElement.append(typeElement)
            ttlElement = ET.Element('TTL')
            ttlElement.text = '60'
            resourceRecordSetElement.append(ttlElement)
            resourceRecordsElement = ET.Element('ResourceRecords')
            resourceRecordElement = ET.Element('ResourceRecord')
            valueElement = ET.Element('Value')
            valueElement.text = instance['DnsName']
            resourceRecordElement.append(valueElement)
            resourceRecordsElement.append(resourceRecordElement)
            resourceRecordSetElement.append(resourceRecordsElement)
            changeElement.append(resourceRecordSetElement)
            changesElement.append(changeElement)

    if (instancesRunning == 0):
        return None

    return wrapXmlForRoute53Change(changesElement)


def wrapXmlForRoute53Change(changesElement):
    '''
        Prepends and wraps the changeElements in the appropriate xml tags. 
    '''
    changeBatchElement = ET.Element('ChangeBatch')
    # changeBatchElement.append(ET.Element('Comment'))
    changeBatchElement.append(changesElement)
    changeResourceRecordSetsRequestElement = ET.Element('ChangeResourceRecordSetsRequest')
    changeResourceRecordSetsRequestElement.set('xmlns', 'https://route53.amazonaws.com/doc/2013-04-01/')
    changeResourceRecordSetsRequestElement.append(changeBatchElement)

    xmlStr = '<?xml version="1.0" encoding="UTF-8"?>' + ET.tostring(changeResourceRecordSetsRequestElement)
    if (verboseOutput):
        root = xml.dom.minidom.parseString(xmlStr)
        prettyXmlStr = root.toprettyxml('    ', '\n', 'UTF-8')
        print(prettyXmlStr)

    return xmlStr


# return an xml string for adding or deleting a route53 entry for the sync round robin dns
# see http://docs.aws.amazon.com/Route53/latest/APIReference/API_ChangeResourceRecordSets_Requests.html#API_ChangeResourceRecordSets_RequestWRRAliasSyntax
def getXmlForRoundRobinDnsChange(instance, action):
    '''
       build the xml to add or delete the CNAME entries for the production sync round robin dns route 53 entries
       using xml formatted per http://docs.aws.amazon.com/Route53/latest/APIReference/API_ChangeResourceRecordSets_Requests.html#API_ChangeResourceRecordSets_RequestWRRAliasSyntax
    '''
    changesElement = ET.Element('Changes')
    if(verboseOutput):
        print('getXmlForRoundRobinDnsChange from:')
        print(json.dumps(instance, sort_keys = True, indent = 4))

    if (action != 'DELETE' and action != 'UPSERT'):
        errMsg = 'getXmlForRoundRobinDnsChange action must be passed as "DELETE" or "CREATE"'
        print(errMsg)
        raise Exception(errMsg)

    # LOOP here to do more than one
    changeElement = ET.Element('Change')

    actionElement = ET.Element('Action')
    actionElement.text = action
    changeElement.append(actionElement)

    resourceRecordSetElement = ET.Element('ResourceRecordSet')

    roundRobinName = serverCluster['RoleNamePatterns'][instance['Role']]['Route53NameRegex']
    roundRobinName = roundRobinName.replace('(\d+)', '')  # remove the numeral regex
    nameElement = ET.Element('Name')
    nameElement.text = roundRobinName
    resourceRecordSetElement.append(nameElement)

    typeElement = ET.Element('Type')
    typeElement.text = 'CNAME'
    resourceRecordSetElement.append(typeElement)

    setIdentifierElement = ET.Element('SetIdentifier')
    setIdentifierElement.text = instance['Route53Name'][:-1]  # remove the trailing dot
    resourceRecordSetElement.append(setIdentifierElement)

    weightElement = ET.Element('Weight')
    weightElement.text = '1'
    resourceRecordSetElement.append(weightElement)

    aliasTargetElement = ET.Element('AliasTarget')

    hostedZoneIdElement = ET.Element('HostedZoneId')
    hostedZoneIdElement.text = serverCluster['HostedZoneId']
    aliasTargetElement.append(hostedZoneIdElement)

    dnsNameElement = ET.Element('DNSName')
    dnsNameElement.text = instance['Route53Name']
    aliasTargetElement.append(dnsNameElement)

    evaluateTargetHealth = ET.Element('EvaluateTargetHealth')
    evaluateTargetHealth.text = 'false'
    aliasTargetElement.append(evaluateTargetHealth)

    resourceRecordSetElement.append(aliasTargetElement)

    changeElement.append(resourceRecordSetElement)

    changesElement.append(changeElement)

    # LOOP END for doing more than one

    return wrapXmlForRoute53Change(changesElement)

def printStatus(rootElement):
    '''
       display the status from the response xml
    '''
    statusElement = rootElement.find('./ChangeInfo/Status')
    status = "None" if statusElement is None else statusElement.text
    print('Route53 DNS Update Status: ' + status)
    if (status == 'PENDING'):
        print('please wait...')


def updateSyncRoundRobinDns(instance, action):
    '''
        There are round robin dns entries for the sync servers because emails are sent to tell players to request their prizes.
        If all players used the same sync server it could overload it.  From the emails we can't control which sync server
        they hit like we do in the app using the SRD.
        As sync servers are started and stopped the round robin dns entry has to be added or removed respectivly.
    '''
    xmlStr = getXmlForRoundRobinDnsChange(instance, action)
    print('Updating sync round robin dns')
    sendRoute53Request(xmlStr)


def verifyDns(instance):
    '''
        Verify that the public ip address reported by the dns matches the ip address provided to the instance by Amazon.
    '''
    print('Verifying Route53 dns responses with correct ip address')
    # get the dns entry for this instance domain name

    # from office computer
    # Jims-MacBook-Pro:fabric jwimp$ nslookup lt1-auth1.shoutgameplay.com
    # Server:        160.7.240.20
    # Address:    160.7.240.20#53
    #
    # Non-authoritative answer:
    # lt1-auth1.shoutgameplay.com    canonical name = ec2-54-196-20-178.compute-1.amazonaws.com.
    # Name:    ec2-54-196-20-178.compute-1.amazonaws.com
    # Address: 54.196.20.178


    # from server in cloud
    # meinc@auth1:~$ nslookup lt1-auth1.shoutgameplay.com
    # Server:        172.16.0.23
    # Address:    172.16.0.23#53
    #
    # Non-authoritative answer:
    # lt1-auth1.shoutgameplay.com    canonical name = ec2-54-196-20-178.compute-1.amazonaws.com.
    # Name:    ec2-54-196-20-178.compute-1.amazonaws.com
    # Address: 10.154.255.132

    # "DnsName": "ec2-54-159-54-76.compute-1.amazonaws.com",
    # "Route53Name": "lt1-web1.shoutgameplay.com.",
    # "IpAddress": "54.159.54.76",

    route53Name = instance['Route53Name'][:-1]  # remove the trailing period
    command = 'nslookup ' + route53Name + ' 8.8.4.4'  # get the answer from google's secondary dns so we get the public address
    result = local(command, True)
    print(result)

    # verify ip address from dns entry matches the instance
    if ((instance['DnsName'] not in result) or (instance['IpAddress'] not in result)):
        raise Exception(route53Name + 'does not dns resolve to ' + instance['DnsName'])


def updateRoute53(instanceList):
    '''
        Update the route53 dns entries for the instances in the list.
    '''

    # build the xml string used to change the Route53 dns CNAME entries
    xmlStr = getXmlForDnsChanges(instanceList)
    if (xmlStr is None):
        # no instances running, nothing to update
        return

    print('Updating Route53 CNAME entries')
    sendRoute53Request(xmlStr)


def sendRoute53Request(xmlStr):
    # get Route53 server datetime string
    AwsDate = getAwsDate()

    # create a signature using the amazon server datetime
    sig = getSignatureAsBase64(AwsDate, serverCluster['Route53AwsSecretKey'])

    # generate the AWS v3 authentication header
    AWS_AUTH = getAmazonV3AuthHeader(serverCluster['Route53AwsAccessKey'], sig)

    # create a signed request to update the entries and send it
    request = urllib2.Request('https://route53.amazonaws.com/2013-04-01/hostedzone/' + serverCluster['HostedZoneId'] + '/rrset', xmlStr)

    request.add_header('X-Amzn-Authorization', AWS_AUTH)
    request.add_header('x-amz-date', AwsDate)
    request.add_header('Content-Type', 'text/plain')

    try:
        httpResponse = urllib2.urlopen(request)
        # response.geturl() - return the URL of the resource retrieved, commonly used to determine if a redirect was followed
        # response.info() - return the meta-information of the page, such as headers, in the form of a mimetools.Message instance (see QuickReference to HTTP Headers)
        # http://www.cs.tut.fi/~jkorpela/http.html
        # response.getcode() - return the HTTP status code of the response.
    except urllib2.HTTPError as e:
        print(request.get_full_url())
        print(e.code)
        print(e.read())
        raise

    rootElement = getRootElementFromHttpResponseXml(httpResponse)
    '''
       get the change id
    '''
    changeIdElement = rootElement.find('./ChangeInfo/Id')
    changeId = "None" if changeIdElement is None else changeIdElement.text  # e.g. /change/C3FFXPMRUHFIW7


    printStatus(rootElement)

    '''
       check the status until it is not pending
       status is pending until the change has been applied
       to all Amazon Route 53 DNS servers.
    '''

    waitTime = 40
    maxTries = 12
    while (rootElement.find('.//ChangeInfo/Status').text == 'PENDING'):
        sleep(waitTime)
        if (waitTime > 10):
            waitTime = waitTime - 10
        request = urllib2.Request('https://route53.amazonaws.com/2013-04-01' + changeId)
        request.add_header('X-Amzn-Authorization', AWS_AUTH)
        request.add_header('x-amz-date', AwsDate)
        httpResponse = urllib2.urlopen(request)
        rootElement = getRootElementFromHttpResponseXml(httpResponse)
        printStatus(rootElement)
        maxTries = maxTries - 1
        if (maxTries < 1):
            print('not waiting any longer for Route53 to update')
            break;


def getRunningInstancesList(serverRole):
    ''' return the running instances of serverRole '''
    runningInstanceList = []
    for instance in serverCluster['ServerList']:
        if ((instance['Role'] == serverRole)
                and ('Running' in instance) and (instance['Running'])):
            runningInstanceList.append(instance)

    return runningInstanceList


'''
   Rewire
'''

verboseOutput = False

@task
def rewire_cluster(clusterName = '', verbose = False):
    '''
        Used to start and/or stop servers in a cluster, then update the dns, srd, and meinc.properties files as needed.
    '''
    global verboseOutput
    verboseOutput = verbose

    loadConfiguration()

    # check/prompt for named cluster name to work with
    clusterName = getInput(clusterName.strip().lower(), namedClusters.keys(), 'Which cluster do you want to rewire?')
    setRewireGlobals(clusterName)

    # get the instances from EC2
    serversStable = getInstances(clusterName)
    if (not serversStable):  # running or stopped
        print('All instances must be fully stopped (code 80) or running (code 16) or terminated (code 48) to continue this script.')
        exit()

    startOrStopInstances(clusterName)

    # update the meinc.properties file on all the mrsoa servers
    stopAllMrSoaServices()
    updateWdsEndpointsConfig()
    updateSrdConfig(verifySrd = False)
    startAllMrSoaServices()

    for instance in serverCluster['ServerList']:
        if (('InstanceState.Code' in instance) and (instance['InstanceState.Code'] == '16')):
            verifyDns(instance)


    '''
    TODO: JIM AND MATT, THIS IS A HACK, FIX SERVERS SO nginx DOESN'T HAVE TO BE RESTARTED ON WDS-CACHE SERVERS
    '''
    for instance in serverCluster['ServerList']:
        if (('Running' in instance) and (instance['Running'])):
            if (instance['Role'] == 'wds-cache'):
                alterService(instance, 'nginx', 'restart')


# For scale up/down

def selectImageId(runningInstanceList, imageId):
    '''
        chooses the AMI image id
        order of precidence is:
        1.  imageId specifed in command line
        2.  imageId specified in instance number 1 using tag ScaleAmiId
        3.  imageId used to create instance number 1
    '''
    if (imageId is None):
        instanceNrOne = runningInstanceList[0]
        if (('Tag.ScaleAmiId' in instanceNrOne) and (instanceNrOne['Tag.ScaleAmiId'] is not None)):
            if (verboseOutput):
                print('using Tag.ScaleAmiId')
            imageId = instanceNrOne['Tag.ScaleAmiId']
        else:
            if (verboseOutput):
                print('using instance origin AmiId')
            imageId = instanceNrOne['ImageId']

    if (verboseOutput):
        print('ami id: ' + imageId)
    return imageId


def selectInstanceType(runningInstanceList, instanceType):
    '''
        chooses the image type
        order of precidence is:
        1.  instanceType specifed in command line
        2.  instanceType of instance number 1
    '''
    if (instanceType is None):
        instanceNrOne = runningInstanceList[0]
        if (verboseOutput):
            print('using instanceType of first instance')
        instanceType = instanceNrOne['InstanceType']

    if (verboseOutput):
        print('instanceType: ' + instanceType)
    return instanceType

@task
def vscale(clusterName = '', serverRole = '', instanceType = '', imageId = None, noSleep = 'false', verbose = 'false'):
    ''' 
        Vertical scale of mrsoa servers only.
        WDS servers are not vertically scaled via this script, that must be done by hand and requires going into maintanance mode. 
        @param clusterName: e.g. production
        @param serverRole: e.g. sync
        @param instanceType: the server size e.g. "m3.xlarge"
        @param imageId: the ami imageId to use when starting new instances
        @param noSleep: ignore sleep in the sequences (warning, if clients are using servers it can cause problems)
        @param verbose: print verbose debugging
    '''
    oldHostString = env.host_string

    global verboseOutput
    verboseOutput = tobool(verbose)

    loadConfiguration()
    
    # check/prompt for named cluster name to work with
    clusterName = getInput(clusterName.strip().lower(), namedClusters.keys(), 'Which cluster do you want to resize?')
    
    # check/prompt for serverRole
    serverRole = getInput(serverRole.strip().lower(), serverRoleList, 'Which server role do you want to resize?')

    # get the instances from EC2
    serversStable = getInstances(clusterName)
    if (not serversStable):  # running or stopped
        print('All instances must be fully stopped (code 80) or running (code 16) to continue this script.')
        exit()

    print
    print('running ' + serverRole + ' instances')
    for instance in serverCluster['ServerList']:
        if (instance['Role'] == serverRole):
            print(instance['Name'] + ' {' + (instance['InstanceType'] if ('InstanceType' in instance) else "unknown") + '} ' + ('running' if ('Running' in instance) and (instance['Running']) else 'stopped'))

    print
    # check/prompt for new instanceType
    instanceType = getInput(instanceType.strip().lower(), instanceTypeDict.keys(), 'Which instance type do you want to resize to?')

    # get current instanceType
    runningInstanceList = getRunningInstancesList(serverRole)
    instancesThatNeedScaledList = []
    for instance in runningInstanceList:
        if (instance['InstanceType'] != InstanceType):
            instancesThatNeedScaledList.append(instance)

    if (len(instancesThatNeedScaledList) == 0):
        print('No running ' + serverRole + ' instances that are not already ' + instanceType)
        exit()

    if (verboseOutput):
        print('running ' + serverRole + ' instances that need scaled')
        print(json.dumps(runningInstanceList, sort_keys = True, indent = 4))

    imageId = selectImageId(runningInstanceList, imageId)

    for instance in instancesThatNeedScaledList:
        if (serverRole not in roleSettings):
            print(serverRole + ' not in roleSettings')
        elif (roleSettings[serverRole].scaleSequence is None):
            print('the vscale sequence is None for role ' + serverRole)
        elif (len(roleSettings[serverRole].scaleSequence['vscale']) == 0):
            print('the vscale sequence is empty for role ' + serverRole)
        else:
            scaleServer(instance, roleSettings[serverRole], 'vscale', 'vscale', instance['Tag.Name'], None, instance['Route53Name'], [instanceType], [imageId], tobool(noSleep))

    env.host_string = oldHostString

@task
def hscale(clusterName = '', serverRole = '', instanceCount = '', imageId = None, cacheImageId = None, instanceTypeOverride = None, cacheInstanceTypeOverride = None, noSleep = 'false', verbose = 'false'):
    ''' 
        horizontal scale of mrsoa or wds servers
        @param clusterName: e.g. production
        @param serverRole: e.g. sync
        @param instanceCount: the number of servers to scale up/down to
        @param imageId: the ami imageId to use when starting new instances, if not specified the same ami as the existing instances will be used
        @param cacheImageId: only applies if the wds is being scaled up, this is the override the cache ami image 
        @param noSleep: ignore sleep in the sequences (warning, if clients are using servers it can cause problems)
        @param verbose: print verbose debugging
    '''

    oldHostString = env.host_string

    global verboseOutput
    verboseOutput = tobool(verbose)

    loadConfiguration()

    # check/prompt for named cluster name to work with
    clusterName = getInput(clusterName.strip().lower(), namedClusters.keys(), 'Which cluster do you want to resize?')

    # check/prompt for serverRole
    serverRole = getInput(serverRole.strip().lower(), serverRoleList, 'Which server role do you want to resize?')

    # check/prompt for override instanceType(s) if provided
    if (instanceTypeOverride is not None):
        msg = 'Which instance type do you want to use for the additional ' + serverRole + ' ininstances?'
        instanceTypeOverride = getInput(instanceTypeOverride.strip().lower(), instanceTypeDict.keys(), msg)

    if (cacheInstanceTypeOverride is not None):
        msg = 'Which instance type do you want to use for the additional wds-cache instances?'
        cacheInstanceTypeOverride = getInput(cacheInstanceTypeOverride.strip().lower(), instanceTypeDict.keys(), msg)

    # set the serverCluster global
    # get the instances from EC2 with instanceType
    serversStable = getInstances(clusterName)
    if (not serversStable):  # running or stopped
        print('All instances must be fully stopped (code 80) or running (code 16) to continue this script.')
        exit()

    # check/prompt for instanceCount
    instanceCount = getInput(instanceCount.strip(), map(str, range(1, 20)), 'How many ' + serverRole + ' servers do you want running?')
    targetInstanceCount = int(instanceCount)

    # get the running instances
    runningInstanceList = getRunningInstancesList(serverRole)
    if (verboseOutput):
        print('running ' + serverRole + ' instances')
        print(json.dumps(runningInstanceList, sort_keys = True, indent = 4))

    runningInstanceCount = len(runningInstanceList)
    if (runningInstanceCount < targetInstanceCount):
        # scaling up, make sure we are not doing wds-cache
        if (serverRole == 'wds-cache'):
            print('Scaling up, changing role to wds-origin')
            serverRole = 'wds-origin';
            runningInstanceList = getRunningInstancesList(serverRole)

        # get a running instance to use as template
        instanceNrOne = runningInstanceList[0]
        imageId = selectImageId(runningInstanceList, imageId)
        instanceType = selectInstanceType(runningInstanceList, instanceTypeOverride)

        for newInstanceNumber in range(runningInstanceCount + 1, targetInstanceCount + 1):
            route53Name = serverCluster['RoleNamePatterns'][serverRole]['Route53NameRegex']
            route53Name = route53Name.replace('(\d+)', str(newInstanceNumber))
            instanceName = serverCluster['RoleNamePatterns'][serverRole]['InstanceNameRegex']
            instanceName = instanceName.replace('(\d+)(?: .*)?', str(newInstanceNumber))
            scaleServer(instanceNrOne, roleSettings[serverRole], 'hscaleup', 'hscale', instanceName, newInstanceNumber, route53Name, [instanceType, cacheInstanceTypeOverride], [imageId, cacheImageId], tobool(noSleep))

    elif (runningInstanceCount > targetInstanceCount):
        # scaling down, make sure we are not doing wds-origin
        if (serverRole == 'wds-origin'):
            print('Scaling down, changing role to wds-cache')
            serverRole = 'wds-cache'
            runningInstanceList = getRunningInstancesList(serverRole)

        for instanceIndexToDelete in range(runningInstanceCount - 1, targetInstanceCount - 1, -1):
            instanceToDelete = runningInstanceList[instanceIndexToDelete]
            result = re.match(serverCluster['RoleNamePatterns'][instanceToDelete['Role']]['InstanceNameRegex'], instanceToDelete['Name'])
            instanceNumber = result.group(1)
            scaleServer(instanceToDelete, roleSettings[serverRole], 'hscaledown', instanceNumber = instanceNumber, noSleep = tobool(noSleep))

    print(instanceCount + ' instances running')

    env.host_string = oldHostString


@task
def testRun(serverRole = ''):
    global verboseOutput
    verboseOutput = True
#     oldHostString = env.host_string
#     env.host_string = '54.196.150.138'
#     run("pwd")
#     env.host_string = oldHostString

    instance = {
            "InstanceId": "i-a9ba7a7a",
            "BlockDeviceMapping": [
                {
                    "DeviceName":"/dev/sda1",
                    "Ebs.DeleteOnTermination":"false"
                }
            ]
        }

    newInstance = {
            "BlockDeviceMapping": "2.2.2.2",
            "PrivateIpAddress": "2.2.2.2"
        }

    print('1')
    loadConfiguration()
    setRewireGlobals('test1')
    setDeleteOnTermination(instance)
    print('2')

