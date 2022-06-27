from __future__ import with_statement

from fabric.api import *
from fabric.utils import *
from fabric.contrib.files import append, exists

import os.path, glob, urllib

if env.user != 'meinc' and env.user != 'ubuntu':
    env.user = 'meinc'

env.skip_bad_hosts = True
env.disable_known_hosts = True
env.use_ssh_config = True
env.always_use_pty = False

env.roledefs['nc10'] = [
    'nc10-1.shoutgameplay.com',
    'nc10-2.shoutgameplay.com',
    'nc10-3.shoutgameplay.com',
]

env.roledefs['nc11'] = [
    'nc11-1.shoutgameplay.com',
    'nc11-2.shoutgameplay.com',
    'nc11-3.shoutgameplay.com',
]

@task
def ec2_install_all():
    puts('\n\nInstalling Full Nomad Server\n')

    hostname_set()
    user_config()
    docker_install()
    docker_login()
    ec2_install_cluster_env()
    install_vagrant_bootstrap()
    nomad_provision_unified()
    nomad_install_jobs()
    install_cloudwatch_agent()
    install_aws_cli()

@task
def hostname_set(sudo_user='ubuntu'):
    puts('\n\nSetting Server Hostname\n')

    fqdn = ('fqdn' in env and env['fqdn'] or prompt('Fully Qualified Hostname [<skip>]:')).strip()
    if fqdn:
        host = fqdn.split('.')[0]
        old_user = env.user
        env.user = sudo_user
        try:
            append('/etc/hosts', ('127.0.0.1\t%s %s' % (fqdn, host)), use_sudo=True)
            sudo('echo "%s" >/etc/hostname' % host)
            sudo('hostname -F /etc/hostname')
        finally:
            env.user = old_user

def tobool(val):
    if type(val) is bool:
        return val
    if type(val) is str:
        return val != '0' and val.lower() != 'false'
    return bool(val)

@task
def user_config(sudo_user='ubuntu', user='meinc', force=False):
    puts('\n\nConfiguring Standard User\n')

    old_user = env.user
    env.user = sudo_user
    try:
        user_exists = False
        with settings(hide('warnings','stdout'), warn_only=True):
            result = run('id %s' % user)
            if result and result.find('no such user') == -1:
                puts('User %s already exists' % user)
                user_exists = True
                if not tobool(force):
                    return

        if not user_exists:
            sudo('adduser --disabled-password --gecos "" %s' % user)

        sudo("usermod -aG sudo %s" % user)

        append('/etc/ssh/sshd_config', 'ClientAliveInterval 60', use_sudo=True)
        #append('/etc/ssh/sshd_config', ['Match User %s' % user, 'PasswordAuthentication yes'], use_sudo=True)
        sudo('systemctl reload ssh')

        append('/etc/sudoers', 'meinc ALL=(ALL:ALL) NOPASSWD: ALL', use_sudo=True)

        user_home_dir = '/home/%s' % user
        sudo('mkdir -p %s/.ssh' % user_home_dir, user=user)
        sudo('chmod 700 %s/.ssh' % user_home_dir, user=user)
        remote_keys_file = '%s/.ssh/authorized_keys' % user_home_dir
        if not exists(remote_keys_file, use_sudo=True):
            sudo('touch '+remote_keys_file, user=user)
        sudo('chmod 600 '+remote_keys_file, user=user)

        ssh_pub_key_files = glob.glob(os.path.expanduser('~/.ssh/*.pub'))
        id_rsa_files = [f for f in ssh_pub_key_files if f.endswith('/id_rsa.pub')]
        ssh_pub_key_files = id_rsa_files and id_rsa_files or ssh_pub_key_files
        if not ssh_pub_key_files:
            abort('No public keys found in ' + os.path.expanduser('~/.ssh/'))
        for ssh_pub_key_file in ssh_pub_key_files:
            pub_key_file = open(ssh_pub_key_files[0], 'r')
            pub_key = pub_key_file.read()
            pub_key_file.close()
            append(remote_keys_file, pub_key, use_sudo=True)
    finally:
        env.user = old_user

@task
def docker_install():
    puts('\n\nInstalling Docker\n')

    #sudo('apt-get update')
    #sudo('apt-get install \
    #        apt-transport-https \
    #        ca-certificates \
    #        curl \
    #        software-properties-common')
    #sudo('curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -')
    #sudo('add-apt-repository \
    #        "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"')
    #sudo('apt-get update')
    #sudo('apt-get install docker-ce')

    sudo('DEBIAN_FRONTEND=noninteractive \
          apt-get update')
    sudo('DEBIAN_FRONTEND=noninteractive \
          apt-get install -y libltdl7')
    sudo('wget -N -nv https://download.docker.com/linux/ubuntu/dists/xenial/pool/stable/amd64/docker-ce_17.06.1~ce-0~ubuntu_amd64.deb')
    sudo('dpkg -i docker-ce_17.06.1~ce-0~ubuntu_amd64.deb')

@task
def docker_login():
    registry_pass = ('registry_pass' in env and env['registry_pass'] or prompt('Shout Docker Registry Password [<skip>]:')).strip()
    if registry_pass and registry_pass not in ('x', 'skip'):
        sudo('set +o history ; HOME=/root docker login -u meinc -p "%s" scm.shoutgameplay.com:5000' % registry_pass)

def save_kv_in_env_file(key, value, env_file_path):
    sudo("sed -i'' '/^{}\>.*$/ d' {}".format(key, env_file_path))
    append(env_file_path, '{}={}'.format(key, value), use_sudo=True)

@task
def ec2_install_cluster_env():
    puts('\n\nInstalling Consul/Nomad Environment for an EC2 Cluster\n')

    cluster_env_path = '/etc/cluster_env'
    sudo('touch {0} && chmod 644 {0}'.format(cluster_env_path))

    with settings(hide('running','stdout')):
        public_ip = sudo('curl -s http://169.254.169.254/latest/meta-data/public-ipv4')
    if public_ip:
        save_kv_in_env_file('CLUSTER_HOST_PUBLIC_IP', public_ip, cluster_env_path)

    nomad_client_cpu_mhz = 0
    try:
        nomad_client_cpu_mhz = int('nomad_mhz' in env and env['nomad_mhz'] or prompt('Nomad Client CPU MHz [<auto>]:'))
    except ValueError:
        pass
    if nomad_client_cpu_mhz:
        save_kv_in_env_file('NOMAD_CLIENT_CPU_MHZ', nomad_client_cpu_mhz, cluster_env_path)

    server_count = 0
    while not server_count:
        try:
            server_count = int('consul_bootstrap_count' in env and env['consul_bootstrap_count'] or prompt('Consul/Nomad Server Count:'))
        except ValueError:
            pass
    save_kv_in_env_file('CLUSTER_HOST_COUNT', server_count, cluster_env_path)

    ec2_tag_key = ('consul_ec2_tag_key' in env and env['consul_ec2_tag_key'] or prompt('Consul Auto-Join EC2 Tag Key [<skip>]:')).strip()
    if ec2_tag_key:
        ec2_tag_key = urllib.quote_plus(ec2_tag_key)
        ec2_tag_value = ('consul_ec2_tag_value' in env and env['consul_ec2_tag_value'] or prompt('Consul Auto-Join EC2 Tag Value:')).strip()
        if not ec2_tag_value:
            puts('Invalid tag value; Skipping Auto-Join')
        else:
            ec2_tag_value = urllib.quote_plus(ec2_tag_value)
            consul_join_string = '"provider=aws tag_key=%s tag_value=%s"' % (ec2_tag_key, ec2_tag_value)
            with settings(hide('running')):
                save_kv_in_env_file('CONSUL_JOIN_STRING', consul_join_string, cluster_env_path)

    with settings(hide('running')):
        # don't use aws dns servers because they return internal ip addresses which are useless
        # outside of the aws network
        save_kv_in_env_file('UPSTREAM_DNS', '8.8.8.8', cluster_env_path)

@task
def install_vagrant_bootstrap():
    puts('\n\nInstalling Bootstrap\n')

    result = local('rsync -e \'ssh -o "StrictHostKeyChecking no"\' -ia --delete "../vagrant-bootstrap" %s@%s:"/tmp/"' % (env.user, env.host))
    if not result.failed:
        sudo('rm -rf /opt/vagrant-bootstrap' \
             '&& cp -a /tmp/vagrant-bootstrap /opt/' \
             '&& chown -R root:root /opt/vagrant-bootstrap')
    else:
        puts('Using scp instead of rsync to upload vagrant-bootstrap')
        put('../vagrant-bootstrap', '/opt/', use_sudo=True, mirror_local_mode=True)

@task
def nomad_provision_unified():
    puts('\n\nExecuting Unified Provisioning\n')
    sudo('bash /opt/vagrant-bootstrap/provision.sh')

@task
def consul_import_data():
    puts('\n\nImporting Snowl Consul Data\n')

    export_file = open('../snowl-consul-export.json')
    export_file_data = export_file.read()
    export_file.close()
    with settings(hide('running')):
        sudo('consul kv import - <<EOF\n%s\nEOF\n' % export_file_data, shell_escape=True)

@task
def nomad_install_jobs():
    puts('\n\nInstalling Nomad Jobs and Tools\n')

    sudo('mkdir -p /opt/nomad-jobs' \
         ' && chmod 750 /opt/nomad-jobs')

    result = local("rsync -ia --delete" \
                   " --include='/snowl-*/' --include='nomad-job.hcl' --exclude='*'" \
                   " ../* %s@%s:'/tmp/nomad-jobs/'" % (env.user, env.host))
    if not result.failed:
        sudo('rsync -a --chown=root:root --delete /tmp/nomad-jobs/ /opt/nomad-jobs/')
    else:
        puts('Using scp instead of rsync to upload')
        job_file_paths = glob.glob('../*/nomad-job.hcl')
        job_names = [p.split('/')[1] for p in job_file_paths]
        for i, job_name in enumerate(job_names):
            sudo('P="/opt/nomad-jobs/%s" && mkdir -p "$P" && chmod 750 "$P"' % job_name)
            put(job_file_paths[i], '/opt/nomad-jobs/%s/nomad-job.hcl' % job_name, use_sudo=True)
        sudo('chown -R root:root /opt/nomad-jobs')

    with settings(hide('stdout')):
        sudo('DEBIAN_FRONTEND=noninteractive' \
             ' apt-get install -y tree python3 python3-pip')
        run('pip3 install --upgrade pip' \
             ' && pip3 install pyyaml')

    put('../docker_image_tag.sh', '/opt/nomad-jobs/', use_sudo=True)
    put('../jobs.yml', '/opt/nomad-jobs/', use_sudo=True)
    put('../jobs-tool.py', '/opt/nomad-jobs/', use_sudo=True)
    sudo('chmod 750 /opt/nomad-jobs/{jobs-tool.py,docker_image_tag.sh}' \
         ' && chown -R root:root /opt/nomad-jobs')

@task
def install_cloudwatch_agent():
    puts('\n\nInstalling CloudWatch Agent')

    sudo('mkdir -p /tmp/acwa' \
         '  && cd /tmp/acwa' \
         '  && wget -q https://s3.amazonaws.com/amazoncloudwatch-agent/linux/amd64/latest/AmazonCloudWatchAgent.zip' \
         '  && unzip AmazonCloudWatchAgent.zip' \
         '  && ./install.sh' \
         '  && rm -rf /tmp/acwa')

    AGENT_HOME = '/opt/aws/amazon-cloudwatch-agent'
    SCRIPT_NAME = 'update-and-restart-cloudwatch-agent-service.sh'
    JSON_NAME = 'amazon-cloudwatch-agent.json'

    put(JSON_NAME, os.path.join(AGENT_HOME, 'etc'), use_sudo=True)
    put(SCRIPT_NAME, AGENT_HOME, use_sudo=True)

    sudo('chmod +x {0}/{1}' \
         '  && {0}/{1}'.format(AGENT_HOME, SCRIPT_NAME))

@task
def install_aws_cli():
    puts('\n\nInstalling AWS CLI')

    sudo('mkdir -p /tmp/awscli' \
         '  && cd /tmp/awscli' \
         '  && wget -q https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip' \
         '  && unzip awscli-exe-linux-x86_64.zip' \
         '  && aws/install' \
         '  && rm -rf /tmp/awscli')
