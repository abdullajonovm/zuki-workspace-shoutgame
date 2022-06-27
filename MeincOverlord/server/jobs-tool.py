#!/usr/bin/python3

from datetime import datetime
import tempfile
import sys, os, os.path, signal, re
import glob, fnmatch, shutil
import subprocess, queue, threading, select
import termios, curses, curses.ascii
# pip install pyyaml
import yaml, json

MNT_DIR_PATH = '/mnt/host'
BACKUP_DIR_PATH = '/mnt/host/jobs-backups'

READ_TERM_HISTORY = {}

def get_history_list(history_key):
    if history_key in READ_TERM_HISTORY:
        history_list = READ_TERM_HISTORY[history_key]
    else:
        history_list = []
        READ_TERM_HISTORY[history_key] = history_list
    return history_list

def undo_user_input(user_input, history_list):
    while user_input in history_list:
        user_input_index = history_list.index(user_input)
        del history_list[user_input_index]

def read_term_input(prompt, history_key=None, history_list=None, prefill='', alternate=None, stdin_itc=None):
    '''
    prompt       is the text to show the user
    history_key  is a key to a map that stores past history of user input. The user can use up and
                 down arrow keys to scroll through past history for the key specified.
    prefill      is a string value to print as if the user had typed it in. The user can
                 delete/modify this if they choose or just hit enter to accept it.
    alternate    is a string value that can replace whatever the user typed (or whatever was
                 specified by 'prefill') when the user hits the tab key.
    '''
    history_index = -1
    history_saved_input = None

    if history_key is not None and history_list is None:
        history_list = get_history_list(history_key)

    stdin_fd = sys.stdin.fileno()
    old = termios.tcgetattr(stdin_fd)
    new = termios.tcgetattr(stdin_fd)
    new[3] = new[3] & ~termios.ICANON & ~termios.ECHO
    new[6][termios.VMIN] = 1
    new[6][termios.VTIME] = 0
    termios.tcsetattr(stdin_fd, termios.TCSANOW, new)

    if prompt:
        sys.stdout.write(prompt)

    user_input = prefill or ''
    if user_input:
        sys.stdout.write(user_input)

    # Begin insert mode
    sys.stdout.buffer.write(curses.tigetstr("smir"))
    sys.stdout.flush()

    user_input_index = len(user_input)
    is_alternate = False

    try:
        remainder = ''
        while True:
            if remainder:
                c = remainder[0]
                remainder = remainder[1:]
            else:
                if stdin_itc is not None:
                    stdin_data_queue, stdin_command_pipe = stdin_itc
                    _, stdin_command_pipe_out_fd = stdin_command_pipe
                    os.write(stdin_command_pipe_out_fd, b'6')
                    c = stdin_data_queue.get()
                else:
                    c = os.read(stdin_fd, 6).decode()
            if len(c) == 1:
                if ord(c) == curses.ascii.ESC:
                    # Escape key
                    user_input = None
                    print()
                    break
                elif ord(c) == curses.ascii.DEL:
                    # Backspace key
                    if user_input_index:
                        #sys.stdout.write(chr(curses.ascii.BS))
                        sys.stdout.buffer.write(curses.tparm(curses.tigetstr("cub"), 1))
                        sys.stdout.buffer.write(curses.tigetstr("dch1"))
                        user_input = user_input[:user_input_index-1] + user_input[user_input_index:]
                        user_input_index -= 1
                elif ord(c) == curses.ascii.TAB:
                    if alternate is not None:
                        is_alternate = not is_alternate
                        if user_input_index:
                            sys.stdout.buffer.write(curses.tparm(curses.tigetstr("cub"), user_input_index))
                        sys.stdout.buffer.write(curses.tparm(curses.tigetstr("dch"), len(user_input)))
                        old_user_input = user_input
                        user_input = alternate
                        alternate = old_user_input
                        sys.stdout.write(user_input)
                        user_input_index = len(user_input)
                elif ord(c) in (curses.ascii.NL, curses.ascii.CR):
                    if history_list is not None and user_input:
                        if user_input not in history_list:
                            history_list.append(user_input)
                        elif user_input != history_list[-1]:
                            undo_user_input(user_input, history_list)
                            history_list.append(user_input)
                    print()
                    break
                elif c == curses.ascii.ctrl('a'):
                    # Go to input beginning
                    if user_input_index:
                        sys.stdout.buffer.write(curses.tparm(curses.tigetstr("cub"), user_input_index))
                    user_input_index = 0
                elif c == curses.ascii.ctrl('e'):
                    # Go to input ending
                    sys.stdout.buffer.write(curses.tparm(curses.tigetstr("cuf"), len(user_input)-user_input_index))
                    user_input_index = len(user_input)
                elif c == curses.ascii.ctrl('k'):
                    # Clear to end of input
                    sys.stdout.buffer.write(curses.tparm(curses.tigetstr("dch"), len(user_input)-user_input_index))
                    user_input = user_input[:user_input_index]
                    user_input_index = len(user_input)
                elif c == curses.ascii.ctrl('u'):
                    # Clear to start of input
                    user_input = user_input[user_input_index:]
                    if user_input_index:
                        sys.stdout.buffer.write(curses.tparm(curses.tigetstr("cub"), user_input_index))
                    sys.stdout.buffer.write(curses.tparm(curses.tigetstr("dch"), user_input_index))
                    user_input_index = 0
                elif c == curses.ascii.ctrl('c'):
                    sys.stdout.buffer.write('^C'.encode())
                    user_input = None
                    print()
                    break
                elif not curses.ascii.iscntrl(c):
                    # Character input
                    user_input = user_input[:user_input_index] + c + user_input[user_input_index:]
                    user_input_index += 1
                    sys.stdout.write(c)
                else:
                    #sys.stdout.write('<'+repr(c)+'>')
                    pass
            elif c == '\x1b[3~':
                # Delete key
                sys.stdout.buffer.write(curses.tigetstr("dch1"))
                user_input = user_input[:user_input_index] + user_input[user_input_index+1:]
            elif c == '\x1b[D':
                # Left arrow key
                user_input_index -= 1
                if user_input_index < 0:
                    user_input_index = 0
                else:
                    sys.stdout.buffer.write(curses.tparm(curses.tigetstr("cub"), 1))
            elif c == '\x1b[C':
                # Right arrow key
                user_input_index += 1
                if user_input_index > len(user_input):
                    user_input_index = len(user_input)
                else:
                    sys.stdout.buffer.write(curses.tparm(curses.tigetstr("cuf"), 1))
            elif c == '\x1b[A':
                # Up arrow key
                if history_list:
                    if user_input_index:
                        sys.stdout.buffer.write(curses.tparm(curses.tigetstr("cub"), user_input_index))
                    sys.stdout.buffer.write(curses.tparm(curses.tigetstr("dch"), len(user_input)))
                    if history_index == -1:
                        history_saved_input = user_input
                        history_index = len(history_list)
                    history_index -= 1
                    if history_index == -1:
                        user_input = history_saved_input
                    else:
                        user_input = history_list[history_index]
                    user_input_index = len(user_input)
                    sys.stdout.write(user_input)
            elif c == '\x1b[B':
                # Down arrow key
                if history_list:
                    if user_input_index:
                        sys.stdout.buffer.write(curses.tparm(curses.tigetstr("cub"), user_input_index))
                    sys.stdout.buffer.write(curses.tparm(curses.tigetstr("dch"), len(user_input)))
                    if history_index == -1:
                        history_saved_input = user_input
                    history_index += 1
                    if history_index == len(history_list):
                        user_input = history_saved_input
                        history_index = -1
                    else:
                        user_input = history_list[history_index]
                    user_input_index = len(user_input)
                    sys.stdout.write(user_input)
            elif c[0] != '\x1b':
                remainder += c
            else:
                sys.stdout.write('<'+repr(c)+'>')
            sys.stdout.flush()
    finally:
        # Exit insert mode
        sys.stdout.buffer.write(curses.tigetstr("rmir"))
        sys.stdout.flush()
        termios.tcsetattr(stdin_fd, termios.TCSAFLUSH, old)

    return user_input, is_alternate

class Config:
    config_dir_path = os.path.expanduser('~/.jobs-tool')
    config_file_path = os.path.expanduser(os.path.join(config_dir_path, 'config'))

    def __init__(self):
        self.config = {
            'username': None,
            'history': {},
        }

        if os.path.exists(Config.config_file_path):
            with open(Config.config_file_path, 'r') as config_stream:
                self.config = yaml.load(config_stream)

    def save(self):
        os.makedirs(Config.config_dir_path, mode=0o750, exist_ok=True)
        with open(Config.config_file_path, 'w') as config_file:
            yaml.dump(self.config, config_file)

    @property
    def username(self):
        if 'username' not in self.config or self.config['username'] is None:
            username = read_term_input('Choose a username: ')[0]
            if username is not None:
                self.config['username'] = username.strip()
                self.save()
        return self.config['username']

    @username.setter
    def username(self, username):
        self.config['username'] = username
        self.save()

    @property
    def history(self):
        if 'history' not in self.config:
            self.config['history'] = {}
        return self.config['history']

    @history.setter
    def history(self, history):
        self.config['history'] = history
        self.save()

class Project:
    def __init__(self, nomad_job_file_path):
        self.nomad_job_file_path = nomad_job_file_path
        self.project_dir_path    = os.path.dirname(self.nomad_job_file_path)
        self.project_name        = os.path.basename(self.project_dir_path)

    def __eq__(self, other):
        return self.project_name == other.project_name

def scan_projects(script_dir_path):
    jobs_file_path = os.path.join(script_dir_path, 'jobs.yml')
    if not os.path.isfile(jobs_file_path):
        print_color('red', 'Missing jobs.yml')
        exit(1)
    with open(jobs_file_path, 'r') as jobs_file:
        jobs_yaml_string = jobs_file.read()
    jobs_yaml = yaml.load(jobs_yaml_string)
    if type(jobs_yaml) is not list:
        print_color('red', 'Invalid jobs.yml')
        exit(1)
    jobs_projects = list(map(lambda job_name:
                                Project(os.path.join(script_dir_path, job_name, 'nomad-job.hcl')),
                         jobs_yaml))

    found_projects = []
    for job_spec_path in glob.iglob(os.path.join(script_dir_path, '*', 'nomad-job.hcl')):
        project = Project(job_spec_path)
        found_projects.append(project)
    found_projects.sort(key=lambda p: p.project_name)

    missing_projects = list(filter(lambda jp: jp not in found_projects, jobs_projects))
    if missing_projects:
        for missing_project in missing_projects:
            print_color('red', 'Job {} specified in jobs.yml but was not found at {}'
                            .format(missing_project.project_name,
                                    missing_project.nomad_job_file_path))
        exit(1)

    extra_projects = list(filter(lambda p: jobs_yaml.count(p.project_name) == 0, found_projects))
    if extra_projects:
        print_color('yellow', 'Projects found but not included in jobs.yml:')
        for extra_project in extra_projects:
            print_color('yellow', '  '+extra_project.project_name)

    return jobs_projects

TERM_NUM_BY_COLOR = {'black':    0, 'silver':   7,
                     'red':      9, 'grey':     8,
                     'yellow':  11, 'green':   10,
                     'magenta': 13, 'blue':    12,
                     'white':   15, 'cyan':    14}

def projects_from_project_specs(project_specs, projects):
    if not project_specs:
        project_specs = ['*']
    elif type(project_specs) is str:
        project_specs = [project_specs]
    result_projects = []
    for project_spec in project_specs:
        exact_match_projects = [p for p in projects if p.project_name == project_spec]
        if exact_match_projects:
            result_projects.extend(exact_match_projects)
        else:
            if len(project_spec.strip('*?[]')) == len(project_spec):
                project_spec = '*' + project_spec + '*'
            matching_projects = [p for p in projects if fnmatch.fnmatchcase(p.project_name, project_spec)]
            if matching_projects:
                result_projects.extend(matching_projects)
    return result_projects

def print_tree_and_user_select_path(tree_dir_path, selectable, max_tree_levels, stdin_itc, history_list):
    tree_exec = ['tree', '--noreport', '-nFL', str(max_tree_levels)]
    if selectable == 'text':
        tree_exec += ['-I', '*.jar|*.zip|*.gz|*.xz|*.t?z']
    elif selectable == 'dir':
        tree_exec += ['-d']
    tree_exec.append(tree_dir_path)
    subproc = subprocess.run(tree_exec, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if subproc.returncode != 0:
        print_color('red', 'Error finding container mount paths\n')
        print(subproc.stdout.decode())
        return None
    mnt_dir_tree_string = subproc.stdout.decode()

    tree_lines = []
    tree_line_paths = []
    dir_paths = []
    empty_text_paths = []
    offer_paths = []
    current_dir = []
    current_dir_level = 0
    p = re.compile(r'^(.*?)(/?\w.*)$')
    for tree_line in mnt_dir_tree_string.splitlines():
        m = p.match(tree_line)
        if not m:
            print_color('red', '(1) Error parsing tree output at line: {}'.format(tree_line))
            return None
        tree_line_indent = m.group(1) or ''
        tree_line_name = m.group(2)
        if not tree_line_name:
            print_color('red', '(2) Error parsing tree output at line: {}'.format(tree_line))
            return None
        tree_lines.append((tree_line_indent, tree_line_name))
        current_dir_level = len(current_dir) - 1
        current_tree_level = int(len(tree_line_indent) / 4)
        if current_tree_level == 0:
            current_dir = [tree_line_name]
            current_dir_level += 1
            tree_line_paths.append(tree_line_name)
            dir_paths.append(tree_line_name)
            if selectable == 'dir':
                offer_paths.append(tree_line_name)
        elif current_tree_level <= current_dir_level:
            while current_tree_level <= current_dir_level:
                current_dir.pop()
                current_dir_level -= 1
        if current_tree_level == current_dir_level + 1:
            tree_line_path = os.path.join(*(current_dir + [tree_line_name]))
            tree_line_path = tree_line_path.rstrip('/=*>|')
            tree_line_paths.append(tree_line_path)
            subproc = subprocess.run(['file', '-bi', tree_line_path],
                                     stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            if subproc.returncode == 0:
                file_type = subproc.stdout.decode().strip()
                do_offer_path = False
                if 'directory' in file_type:
                    if selectable == 'dir':
                        do_offer_path = True
                    current_dir.append(tree_line_name)
                    dir_paths.append(tree_line_path)
                elif selectable == 'text':
                    if file_type.startswith('text/') or 'ascii' in file_type:
                        do_offer_path = True
                    elif 'x-empty' in file_type:
                        do_offer_path = True
                        empty_text_paths.append(tree_line_path)
                if do_offer_path:
                    offer_paths.append(tree_line_path)

    max_offer_text_len = len(str(len(offer_paths))) + 2  #includes brackets
    offer_path_index = 0
    for i, tree_line in enumerate(tree_lines):
        tree_line_indent, tree_line_name = tree_line
        tree_line_path = tree_line_paths[i]

        next_offer_path = offer_path_index < len(offer_paths) \
                                and offer_paths[offer_path_index] \
                                or  ''
        offer_text_len = 0
        if next_offer_path and tree_line_path == next_offer_path:
            offer_path_index += 1
            print_color(None, '[{}]'.format(offer_path_index), newline=False)
            offer_text_len = len(str(offer_path_index)) + 2  #includes brackets

        print_color(None,
                    '{}  {}'.format(
                        ' ' * (max_offer_text_len - offer_text_len),
                        tree_line_indent),
                    newline=False)

        name_color = None
        if i == 0:
            name_color = 'green'
        elif tree_line_path in dir_paths:
            name_color = 'blue'
        print_color(name_color,
                    tree_line_name,
                    newline=False)

        if tree_line_path not in empty_text_paths:
            print_color(None, '')
        else:
            max_tree_line_len = len(max(mnt_dir_tree_string.splitlines(), key=len))
            print_color('grey',
                        '{}(empty)'.format(
                            ' ' * (max_tree_line_len - (len(tree_line_indent) + len(tree_line_name)) + 1)))

    def strip_tree_dir_prefix(target_path):
        if target_path.startswith(tree_dir_path):
            target_path = target_path[len(tree_dir_path):]
            if target_path.startswith(os.sep):
                target_path = target_path[1:]
        return target_path

    # Clean up history entries left over from old versions of jobs-tool
    for i, history_item in enumerate(history_list):
        history_list[i] = strip_tree_dir_prefix(history_item)

    user_input, _ = read_term_input('\nWhich path do you want (1-{})? '.format(len(offer_paths)),
                                    history_list=history_list,
                                    stdin_itc=stdin_itc)
    if user_input is None:
        return None
    try:
        offer_path_index = int(user_input) - 1
        offer_path = offer_paths[offer_path_index]
        if user_input == history_list[-1]:
            stripped_offer_path = strip_tree_dir_prefix(offer_path)
            undo_user_input(stripped_offer_path, history_list)
            history_list[-1] = stripped_offer_path
        return offer_path
    except (ValueError, IndexError):
        user_input_path = os.path.join(tree_dir_path, user_input)
        if user_input_path in offer_paths:
            return user_input_path
        print('Invalid input')
        undo_user_input(user_input, history_list)
        return None

def query_nomad_node(node_id):
    cmd = ['nomad', 'node-status', '-json', node_id and node_id or '-self']
    subproc = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    node_json = subproc.stdout.decode()
    if subproc.returncode != 0:
        print_color(None, subproc.stdout.decode())
        print_color('red', '*** ERROR RETURNED ***\n')
        return None

    node = json.loads(node_json)
    node_id = node['ID']
    node_name = node['Name']

    return (node_name, node_id)

def query_nomad_node_allocations(node_id):
    allocations = []
    subproc = subprocess.run(['nomad', 'node-status', '-no-color', node_id],
                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    job_status = subproc.stdout.decode()
    if subproc.returncode != 0:
        print_color('red', '*** ERROR RETURNED ***\n')
        return None

    job_status_lines = job_status.splitlines()
    i = 0
    while i < len(job_status_lines):
        job_status_line = job_status_lines[i]
        i += 1
        if job_status_line.startswith('Allocations'):
            break
    i += 1  #skip table header
    while i < len(job_status_lines):
        job_status_line = job_status_lines[i]
        i += 1
        jobs_status_line_parts = job_status_line.split()
        allocation_id = jobs_status_line_parts[0]
        job_id = jobs_status_line_parts[2]
        allocations.append((allocation_id, job_id))
    return allocations

def query_nomad_job_statuses():
    job_status_by_job_name = {}
    subproc = subprocess.run(['nomad', 'status', '-no-color'],
                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if subproc.returncode != 0:
        print('Error querying nomad for job statuses: ' + subproc.stdout.decode())
    else:
        for i, nomad_status_row in enumerate(subproc.stdout.decode().splitlines()):
            if i == 0:  #skip table header
                continue
            nomad_status_row_parts = nomad_status_row.split()
            job_status_by_job_name[nomad_status_row_parts[0]] = nomad_status_row_parts[3]

    return job_status_by_job_name

def query_nomad_job_allocation_ids(job_name, print_query_output=False, include_dead_allocs=False):
    return [a[0] for a in query_nomad_job_allocations(job_name, print_query_output, include_dead_allocs)]

def query_nomad_job_allocations(job_name, print_query_output=False, include_dead_allocs=False):
    allocations = []
    cmd = ['nomad', 'status', '-no-color', '-verbose']
    if include_dead_allocs:
        cmd.append('-all-allocs')
    cmd.append(job_name)
    subproc = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    job_status = subproc.stdout.decode()
    if subproc.returncode != 0:
        return []
    job_status_lines = job_status.splitlines()
    i = 0
    while i < len(job_status_lines):
        job_status_line = job_status_lines[i]
        i += 1
        if job_status_line.startswith('Allocations'):
            break
        print_query_output and print(job_status_line)
    i += 1  #skip table header
    while i < len(job_status_lines):
        job_status_line = job_status_lines[i]
        i += 1
        jobs_status_line_parts = job_status_line.split()
        allocation_id = jobs_status_line_parts[0]
        node_id = jobs_status_line_parts[2]
        allocation_status = jobs_status_line_parts[5]
        if allocation_status == 'running' or include_dead_allocs:
            allocations.append((allocation_id, node_id, allocation_status))

    return allocations

def query_nomad_allocation(allocation_id):
    subproc = subprocess.run(['nomad', 'alloc-status', '-json', allocation_id],
                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    allocation_status_json = subproc.stdout.decode()
    if subproc.returncode != 0:
        print_color(None, allocation_status_json)
        print_color('red', '*** ERROR RETURNED ***\n')
        return None

    allocation_status = json.loads(allocation_status_json)
    node_id = allocation_status['NodeID']

    allocation_name = allocation_status['Name']
    p = re.compile(r'[^[]+\[(\d)\]')
    m = p.match(allocation_name)
    if not m:
        print_color('red', 'Invalid Allocation Data')
        return None
    try:
        allocation_index = int(m.group(1))
    except ValueError:
        print_color('red', 'Invalid Allocation Data')
        return None

    return (allocation_name, allocation_index, node_id)

def query_nomad_docker_containers(nomad_allocation_ids):
    subproc = subprocess.run(['docker', 'ps', '--format', '{{.ID}} {{.Names}}'],
                              stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if subproc.returncode != 0:
        print_color(None, subproc.stdout.decode())
        print_color('red', '*** ERROR RETURNED ***\n')
        return None
    docker_ps = subproc.stdout.decode()

    docker_container_ids = []
    for docker_ps_line in docker_ps.splitlines():
        docker_ps_line_parts = docker_ps_line.split()
        container_id = docker_ps_line_parts[0]
        container_name = docker_ps_line_parts[1]
        for allocation_id in nomad_allocation_ids:
            if '{}'.format(allocation_id) in container_name:
                docker_container_ids.append(container_id)
    return docker_container_ids

def stop_job(project_spec, projects, output_prefix='', verbose=False, quiet=False):
    command_projects = projects_from_project_specs(project_spec, projects)
    if not command_projects:
        print('No matching projects')
    for command_project in reversed(command_projects):
        if not quiet:
            if not verbose:
                print(output_prefix+'Stopping {}'.format(command_project.project_name))
            else:
                print(output_prefix+'========================================================')
                print(output_prefix+'Stopping {}'.format(command_project.project_name))
                print(output_prefix+'========================================================\n')
        subproc = subprocess.run(['nomad', 'stop', command_project.project_name],
                                 stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        subproc_output = subproc.stdout.decode()
        if subproc.returncode == 0:
            if not quiet and verbose:
                print(subproc.stdout.decode())
        else:
            if not quiet:
                if subproc_output.startswith('No job(s) with'):
                    print_color('yellow', subproc_output)
                else:
                    print(subproc.stdout.decode())
                    print_color('red', '*** ERROR RETURNED ***\n')

def launch_less(file_path=None, stdin=None):
    signal.signal(signal.SIGINT, signal.SIG_IGN)
    try:
        if file_path is not None:
            subproc = subprocess.Popen(['less', file_path], cwd=os.path.dirname(file_path))
        elif stdin is not None:
            subproc = subprocess.Popen(['less', '-'],
                                       stdin=subprocess.PIPE)
            subproc.stdin.write(stdin.encode())
            subproc.stdin.close()
        else:
            raise ValueError('missing file_path or stdin')
        subproc.wait()
    finally:
        signal.signal(signal.SIGINT, on_sigint)

def launch_shell(launch_in_dir):
    orig_cwd = os.getcwd()
    signal.signal(signal.SIGINT, signal.SIG_IGN)
    try:
        os.chdir(launch_in_dir)
        subproc = subprocess.Popen(['bash'])
        subproc.wait()
    finally:
        signal.signal(signal.SIGINT, on_sigint)
        os.chdir(orig_cwd)

def launch_vim(launch_in_dir, edit_path):
    orig_cwd = os.getcwd()
    signal.signal(signal.SIGINT, signal.SIG_IGN)
    try:
        os.chdir(launch_in_dir)
        if edit_path.startswith(launch_in_dir):
            edit_path = edit_path[len(launch_in_dir):]
            if edit_path.startswith('/'):
                edit_path = edit_path[1:]
        subproc = subprocess.Popen(['vim', edit_path])
        subproc.wait()
    finally:
        signal.signal(signal.SIGINT, on_sigint)
        os.chdir(orig_cwd)

def print_job_mount_paths(job_name, mount_paths, local_node_id, prefix_spaces=2, is_offer=False, path_color=None):
    for i, mount_path in enumerate(mount_paths):
        alloc_prefix = is_offer and '[{}] '.format(i+1) or ''
        alloc_suffix_color = None
        alloc_suffix = ''
        try:
            path_alloc_index = int(os.path.basename(mount_path))
        except ValueError:
            path_alloc_index = None
        if path_alloc_index is not None:
            local_allocs = query_nomad_node_allocations(local_node_id)
            local_job_allocs_ids = [a[0] for a in local_allocs
                                    if a[1] == job_name]
            path_alloc_name = None
            for local_alloc_id in local_job_allocs_ids:
                alloc = query_nomad_allocation(local_alloc_id)
                if alloc[1] == path_alloc_index:
                    path_alloc_name = alloc[0]
                    break

            if path_alloc_name:
                alloc_suffix_color = 'grey'
                alloc_suffix = '\t(mounted by {})'.format(path_alloc_name)
            else:
                alloc_suffix_color = 'grey'
                alloc_suffix = '\t(unmounted)'

        print_color(path_color, '{}{}{}'.format(' ' * prefix_spaces, alloc_prefix, mount_path), newline=False)
        print_color(alloc_suffix_color, alloc_suffix)

def print_color(color_name, message, newline=True):
    if color_name:
        sys.stdout.buffer.write(curses.tparm(curses.tigetstr("setaf"), TERM_NUM_BY_COLOR[color_name]))
    sys.stdout.buffer.write(message.encode())
    if newline:
        sys.stdout.buffer.write('\n'.encode())
    sys.stdout.buffer.write(curses.tigetstr("op"))

def print_help():
    print('''\
Commands:
  ?, help             Print this help output
  q, quit, exit       Exit
  ls, list            Lists the available projects

  status PROJECTS     Displays job statuses

  backup PROJECTS     Captures a local backup of the project source directories, job files and logs,
                      mount directories, and docker images
  restore PROJECTS    Offers to restore from previous backups

  build               Builds the docker images for the specified projects. A delta build is
    [--no-delta]      attempted if possible. A delta build packages only the files that have changed
    [--no-cache]      since the last build. The --no-delta argument forces a regular build which is
    PROJECTS          a simpler less error-prone build but it often packages files that have not
                      changed. The --no-cache option forces docker to rebuild the image from
                      scratch. Using --no-cache forces --no-delta to be set.

  push PROJECTS       Push the Docker images for the specified projects to the Docker registry
  pull PROJECTS       Pull the Docker images for the specified projects from the Docker registry

  run PROJECTS        Runs the jobs from the specified projects
  stop PROJECTS       Stops the jobs from the specified projects

  rerun PROJECTS
  restart PROJECTS    Restarts the jobs from the specified projects

  sedit PROJECT       Edits a file from a project's source directory
  sshell PROJECT      Launches a command shell in a project's source directory
  ssync PROJECT       Synchronizes configuration files from the specified project workspace
                      directories to their associated project source directories.

  jlog PROJECT        Displays the job log

  mless PROJECT       Displays the content of a file from a container's mount directory
  mshell PROJECT      Launches a command shell in a container's mount directory
  mclean PROJECTS     Deletes all files from container mount directories
  msync PROJECTS      Synchronizes configuration files from the specified project source directories
                      to their associated container mount directories. This may forgo the need to
                      rebuild a container.
  msignal PROJECTS    Send a POSIX signal to the root processes of project containers

  dshell PROJECT      Launches a command shell inside a container. Use this command with care: Any
                      changes you make will be lost upon rerunning the project/container.

Commands may be separated by newlines or semicolons
''')

def main(stdin_itc):
    curses.setupterm()

    script_dir_path = os.path.abspath(os.path.dirname(sys.argv[0]))
    os.chdir(script_dir_path)

    overlord_path = os.path.abspath(os.path.join(script_dir_path, '..'))
    workspace_path = os.path.abspath(os.path.join(overlord_path, '..'))

    docker_common_project_path = os.path.join(workspace_path, 'docker-common')
    if 'DOCKER_COMMON_HOME' not in os.environ and os.path.isdir(docker_common_project_path):
        os.putenv('DOCKER_COMMON_HOME', docker_common_project_path)

    global config, READ_TERM_HISTORY
    config = Config()
    READ_TERM_HISTORY = config.history

    projects = scan_projects(script_dir_path)
    if not projects:
        print_color('red', 'No projects found')
        exit(1)

    local_node = query_nomad_node(None)
    if not local_node:
        print_color('red', 'No local node found')
        exit(1)
    local_node_name = local_node[0]
    local_node_id = local_node[1]

    command_line_history_key = 'command-line'

    print("Enter '?' for help")
    while True:
        user_input, _ = read_term_input('{}> '.format(local_node_name),
                                        history_key=command_line_history_key,
                                        stdin_itc=stdin_itc)

        if user_input is None:
            continue

        user_commands = user_input.split(';')
        for i, user_command in enumerate(user_commands):
            user_command = user_command.strip()
            if user_command == '':
                continue

            elif user_command == '?' or user_command == 'help':
                print_help()
                continue

            elif user_command in ('q', 'quit', 'exit'):
                undo_user_input(user_command, READ_TERM_HISTORY[command_line_history_key])
                for history_list in READ_TERM_HISTORY.values():
                    while len(history_list) > 10:
                        del history_list[0]
                config.save()
                exit(0)

            elif user_command in ('ls', 'list'):
                job_status_by_job_name = query_nomad_job_statuses()
                for project in projects:
                    project_status = job_status_by_job_name.get(project.project_name, 'stopped')
                    node_names = []
                    if project_status in ('running', 'complete'):
                        project_status_color = 'green'
                        if project_status == 'running':
                            allocations = query_nomad_job_allocations(project.project_name)
                            for allocation in allocations:
                                allocation_id = allocation[0]
                                node_id = allocation[1]
                                allocation_status = allocation[2]
                                node = query_nomad_node(node_id)
                                if node:
                                    node_names.append(node[0])

                    elif project_status in ('failed', 'stopped', 'lost'):
                        project_status_color = 'red'
                    else:
                        project_status_color = 'yellow'
                    print_color(None, ' ' + project.project_name + ' is ', newline=False)
                    print_color(project_status_color, project_status, newline=False)
                    if node_names:
                        print_color(None, ' on ', newline=False)
                        print_color('magenta', '{}'.format(', '.join(node_names)))
                    else:
                        print_color(None, '')
                continue

            user_command_parts = user_command.split()
            user_command_name = user_command_parts[0]

            if user_command_name in ('build',):
                no_cache_build = False
                try_delta_build = True
                if len(user_command_parts) >= 2 and \
                        '--no-cache' in user_command_parts[1:]:
                    no_cache_build = True
                    try_delta_build = False
                    user_command_parts.remove('--no-cache')
                if len(user_command_parts) >= 2 and \
                        '--no-delta' in user_command_parts[1:]:
                    try_delta_build = False
                    user_command_parts.remove('--no-delta')
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                for command_project in command_projects:
                    project_delta_build_file_path = os.path.join(command_project.project_dir_path, 'build-delta.sh')
                    project_build_file_path = os.path.join(command_project.project_dir_path, 'build.sh')
                    if not os.path.isfile(project_delta_build_file_path) and not os.path.isfile(project_build_file_path):
                        print('Skipping {}: no build scripts found'.format(command_project.project_name))
                        continue
                    print('========================================================')
                    print('Building {}'.format(command_project.project_name))
                    print('========================================================\n')
                    do_regular_build = True
                    if try_delta_build and os.path.isfile(project_delta_build_file_path):
                        do_regular_build = False
                        print('Attempting delta build...\n')
                        subproc = subprocess.run(['/bin/bash', project_delta_build_file_path],
                                                 stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                        print(subproc.stdout.decode())
                        if subproc.returncode not in (0, 2):
                            print_color('red', '*** ERROR RETURNED - attempting regular build...\n')
                            do_regular_build = True
                    if do_regular_build:
                        cmd = ['/bin/bash', project_build_file_path]
                        if no_cache_build:
                            print('Attempting no-cache build')
                            cmd.append('--no-cache')
                        subproc = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                        print(subproc.stdout.decode())
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')

            elif user_command_name in ('push',):
                docker_image_tag_script_path = 'docker_image_tag.sh'
                if not os.path.isfile(docker_image_tag_script_path):
                    print_color('red', 'Missing {}'.format(docker_image_tag_script_path))
                    continue
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                for command_project in command_projects:
                    subproc = subprocess.run(['/bin/bash', docker_image_tag_script_path, command_project.nomad_job_file_path],
                                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                    if subproc.returncode != 0:
                        print(subproc.stdout.decode())
                        print_color('red', '*** ERROR RETURNED ***')
                        continue
                    docker_image_tag = subproc.stdout.decode().strip()

                    print('========================================================')
                    print('Pushing {}'.format(command_project.project_name))
                    print('========================================================\n')
                    subproc = subprocess.Popen(['docker', 'push', docker_image_tag])
                    subproc.wait()
                    if subproc.returncode != 0:
                        print_color('red', '*** ERROR RETURNED ***\n')
                    else:
                        print('\nAdding preservation tag')
                        if config.username is None or len(config.username) == 0:
                            continue
                        new_docker_image_tag = '{}-{}'.format(docker_image_tag, config.username)
                        subproc = subprocess.run(['docker', 'tag', docker_image_tag, new_docker_image_tag], stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                        if subproc.returncode != 0:
                            continue
                        subproc = subprocess.run(['docker', 'push', new_docker_image_tag], stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                        if subproc.returncode != 0:
                            continue
                    print_color(None, '')

            elif user_command_name in ('pull',):
                docker_image_tag_script_path = 'docker_image_tag.sh'
                if not os.path.isfile(docker_image_tag_script_path):
                    print_color('red', 'Missing {}'.format(docker_image_tag_script_path))
                    continue
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                for command_project in command_projects:
                    subproc = subprocess.run(['/bin/bash', docker_image_tag_script_path, command_project.nomad_job_file_path],
                                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                    if subproc.returncode != 0:
                        print(subproc.stdout.decode())
                        print_color('red', '*** ERROR RETURNED ***')
                        continue
                    docker_image_tag = subproc.stdout.decode().strip()

                    print('========================================================')
                    print('Pulling {}'.format(command_project.project_name))
                    print('========================================================\n')
                    subproc = subprocess.Popen(['docker', 'pull', docker_image_tag])
                    subproc.wait()
                    if subproc.returncode != 0:
                        print_color('red', '*** ERROR RETURNED ***\n')
                    print_color(None, '')

            elif user_command_name in ('msync',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                for command_project in command_projects:
                    project_sync_file_path = os.path.join(command_project.project_dir_path, 'sync.sh')
                    if not os.path.isfile(project_sync_file_path):
                        print('Skipping {}: sync.sh not found'.format(command_project.project_name))
                        continue
                    sync_to_dirs = glob.glob(os.path.join(MNT_DIR_PATH, command_project.project_name, '*'))
                    if len(sync_to_dirs) == 0:
                        print('Skipping {}: no mnt directories found'.format(command_project.project_name))
                        continue
                    for sync_to_dir in sync_to_dirs:
                        if not os.path.isdir(sync_to_dir):
                            continue
                        print('========================================================')
                        print('Syncing {} to {}'.format(command_project.project_name, sync_to_dir))
                        print('========================================================\n')
                        subproc = subprocess.run(['/bin/bash', project_sync_file_path, sync_to_dir],
                                                 stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                        print(subproc.stdout.decode())
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')

            elif user_command_name in ('msignal',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                    continue

                print('''\
[1]  SIGHUP
[2]  SIGINT 
[3]  SIGQUIT
[9]  SIGKILL
[15] SIGTERM
[16] SIGUSR1
[17] SIGUSR2
[23] SIGSTOP
[25] SIGCONT''')
                user_input, _ = read_term_input('\nWhich Signal? ', stdin_itc=stdin_itc)
                try:
                    signal_num = int(user_input)
                except ValueError:
                    print('Invalid input')
                    continue
                if signal_num not in (1, 2, 3, 9, 15, 16, 17, 23, 25):
                    print('Invalid choice')
                    continue

                for command_project in command_projects:
                    allocation_ids = query_nomad_job_allocation_ids(command_project.project_name)
                    if not allocation_ids:
                        print('No allocations found for project {}'
                                .format(command_project.project_name))
                        continue
                    project_container_ids = query_nomad_docker_containers(allocation_ids)
                    if not project_container_ids:
                        print('No matching local containers found for {}'
                                .format(command_project.project_name))
                        continue
                    for container_id in project_container_ids:
                        signal_exec = []
                        subproc = subprocess.run(['docker', 'exec', '-it', container_id, 'kill', '-'+str(signal_num), '1'])
                        print('Sent signal to project/container {}/{}'
                                .format(command_project.project_name, container_id))
                        if subproc.returncode != 0:
                            print_color('red', 'Error sending signal to project/container {}/{}\n'
                                    .format(command_project.project_name, container_id))
                            print(subproc.stdout.decode())

            elif user_command_name in ('run',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                for command_project in command_projects:
                    print('========================================================')
                    print('Running {}'.format(command_project.project_name))
                    print('========================================================\n')
                    subproc = subprocess.run(['nomad', 'run', command_project.nomad_job_file_path],
                                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                    print(subproc.stdout.decode())
                    if subproc.returncode != 0:
                        print_color('red', '*** ERROR RETURNED ***\n')

            elif user_command_name in ('stop',):
                stop_job(user_command_parts[1:], projects, verbose=True)

            elif user_command_name in ('restart', 'rerun'):
                user_commands.insert(i+1, 'stop '+' '.join(user_command_parts[1:]))
                user_commands.insert(i+2, 'run '+' '.join(user_command_parts[1:]))

            elif user_command_name in ('status',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                for command_project in command_projects:
                    print('========================================================')
                    print('Status for {}'.format(command_project.project_name))
                    print('========================================================\n')
                    allocations = query_nomad_job_allocations(command_project.project_name, print_query_output=True)
                    for allocation in allocations:
                        allocation_id = allocation[0]
                        node_id = allocation[1]
                        node = query_nomad_node(node_id)
                        node_name = node and node[0] or '?'
                        print('  -----------------------------------')
                        print('  Allocation {}'.format(allocation_id))
                        print('  -----------------------------------')
                        subproc = subprocess.run(['nomad', 'alloc-status', '-no-color', allocation_id],
                                                 stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                        allocation_status = subproc.stdout.decode()
                        if subproc.returncode != 0:
                            print_color(None, allocation_status)
                            print_color('red', '*** ERROR RETURNED ***\n')
                        else:
                            for allocation_status_line in allocation_status.splitlines():
                                if allocation_status_line.startswith('Node ID'):
                                    allocation_status_line += ' ({})'.format(node_name)
                                print('  ' + allocation_status_line)
                            print()

            elif user_command_name in ('backup',):
                docker_image_tag_script_path = 'docker_image_tag.sh'
                if not os.path.isfile(docker_image_tag_script_path):
                    print_color('red', 'Missing {}'.format(docker_image_tag_script_path))
                    continue
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                backup_datestamp = datetime.now().strftime('%Y%m%d-%H%M%S')
                for command_project in command_projects:
                    print('========================================================')
                    print('Backing up {}'.format(command_project.project_name))
                    print('========================================================\n')

                    subproc = subprocess.run(['/bin/bash', docker_image_tag_script_path, command_project.nomad_job_file_path],
                                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                    if subproc.returncode != 0:
                        print(subproc.stdout.decode())
                        print_color('red', '*** ERROR RETURNED ***')
                        continue
                    docker_image_tag = subproc.stdout.decode().strip()

                    with tempfile.TemporaryDirectory() as parent_backup_dir:
                        print('Backing up docker image {}'.format(docker_image_tag))
                        docker_backup_dir = os.path.join(parent_backup_dir, 'docker')
                        docker_backup_path = os.path.join(docker_backup_dir, 'image.tar')
                        os.makedirs(docker_backup_dir, mode=0o750)
                        subproc = subprocess.Popen(['docker', 'save', '-o', docker_backup_path, docker_image_tag])
                        subproc.wait()
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')
                            continue

                        mnt_host_dir = os.path.join(MNT_DIR_PATH, command_project.project_name)
                        mnt_backup_dir = os.path.join(parent_backup_dir, 'mnt')
                        mnt_host_backup_dir = os.path.join(mnt_backup_dir, 'host')
                        print('Backing up mount directory {}'.format(mnt_host_dir))
                        os.makedirs(mnt_backup_dir, mode=0o750)
                        subproc = subprocess.Popen(['cp', '-a', mnt_host_dir, mnt_host_backup_dir])
                        subproc.wait()
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')
                            continue

                        print('Backing up Nomad job specification {}'.format(command_project.nomad_job_file_path))
                        subproc = subprocess.Popen(['cp', '-a', command_project.nomad_job_file_path, parent_backup_dir])
                        subproc.wait()
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')
                            continue

                        final_backup_dir = os.path.join(BACKUP_DIR_PATH, command_project.project_name)
                        final_backup_name = '{}_{}.tgz'.format(command_project.project_name, backup_datestamp)
                        final_backup_path = os.path.join(final_backup_dir, final_backup_name)
                        print('Storing backup at {}'.format(final_backup_path))
                        os.makedirs(final_backup_dir, mode=0o750, exist_ok=True)
                        backup_paths = [p[len(parent_backup_dir)+1:]
                                for p in [docker_backup_path, mnt_backup_dir]]
                        project_paths = [p[len(command_project.project_dir_path)+1:]
                                for p in [command_project.nomad_job_file_path]]
                        subproc = subprocess.Popen(['tar', '-czf', final_backup_path] +
                            ['-C', parent_backup_dir] + backup_paths +
                            ['-C', command_project.project_dir_path] + project_paths)
                        subproc.wait()
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')
                            continue

                        backup_file_paths = glob.glob(os.path.join(final_backup_dir, '*.tgz'))
                        preserve_backups_count = 2
                        if len(backup_file_paths) > preserve_backups_count:
                            backup_file_paths.sort(reverse=True)
                            delete_backup_file_paths = backup_file_paths[preserve_backups_count:]
                            print('\nMore than two backups exist for this project. These are the oldest backups:')
                            for delete_backup_file_path in delete_backup_file_paths:
                                print('  {}'.format(os.path.basename(delete_backup_file_path)))
                            print()
                            user_input, _ = read_term_input('Delete these backups (y/n) [n]? ', stdin_itc=stdin_itc)
                            if user_input.lower() == 'y':
                                print()
                                for delete_backup_file_path in delete_backup_file_paths:
                                    print('Deleting {}'.format(delete_backup_file_path))
                                    try:
                                        os.remove(delete_backup_file_path)
                                    except OSError as e:
                                        print_color('red', 'Error: {}'.format(e.strerror))
                        print()

            elif user_command_name in ('restore',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')

                restore_datestamp = datetime.now().strftime('%Y%m%d-%H%M%S')

                backed_project_dirs = glob.glob(os.path.join(BACKUP_DIR_PATH, '*'))
                backed_project_names = [os.path.basename(d) for d in backed_project_dirs]

                for command_project in command_projects:
                    print('========================================================')
                    print('Restoring {}'.format(command_project.project_name))
                    print('========================================================\n')

                    if command_project.project_name not in backed_project_names:
                        print_color('yellow', 'No backup found for {}'.format(command_project.project_name))
                        continue
                    backed_project_dir_index = backed_project_names.index(command_project.project_name)
                    backed_project_dir = backed_project_dirs[backed_project_dir_index]

                    print(' * Backups are listed in reverse chronological order\n')
                    backed_project_filepaths = glob.glob(os.path.join(backed_project_dir, '*.tgz'))
                    if not backed_project_filepaths:
                        print_color('yellow', 'No backup found for {}'.format(command_project.project_name))
                        continue
                    backed_project_filepaths.sort(reverse=True)
                    backed_project_filenames = [os.path.basename(p) for p in backed_project_filepaths]

                    max_offer_number_text_len = len(str(len(backed_project_filenames))) + 2  #includes brackets
                    for i, filename in enumerate(backed_project_filenames):
                        offer_number = i + 1
                        offer_number_text_len = len(str(offer_number)) + 2  #includes brackets
                        print('[{}]'.format(offer_number), end='')
                        print('{} {}'.format(
                            ' ' * (max_offer_number_text_len - offer_number_text_len),
                            filename))

                    user_input, _ = read_term_input('\nWhich backup do you wish to restore (1-{})? '.format(
                        len(backed_project_filenames)),
                        stdin_itc=stdin_itc)

                    if user_input is None:
                        break
                    try:
                        backed_project_index = int(user_input) - 1
                        backed_project_filepath = backed_project_filepaths[backed_project_index]
                    except (ValueError, IndexError):
                        print('Invalid input - skipping\n')
                        continue

                    print_color('yellow', '''
Restoring {} will:
  1. Stop all its running containers
  2. Replace its Docker image
  3. Delete and replace all its mounted files
  4. Replace its Nomad job specification'''.format(
                        command_project.project_name))
                    user_input, _ = read_term_input('\nProceed (yes/no) [no]? ', stdin_itc=stdin_itc)
                    if user_input is None:
                        break
                    if user_input != 'yes':
                        print('Skipping\n')
                        continue

                    with tempfile.TemporaryDirectory() as parent_restore_dir:
                        print('\nExtracting backup from {}'.format(backed_project_filepath))
                        subproc = subprocess.run(['tar', '-xzf', backed_project_filepath, '-C', parent_restore_dir])
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')
                            continue

                        print('Stopping job {}'.format(command_project.project_name))
                        stop_job(command_project.project_name, projects, quiet=True)

                        print('Restoring docker image')
                        src_image_file_path = os.path.join(parent_restore_dir, 'docker', 'image.tar')
                        subprocess.run(['docker', 'load', '-i', src_image_file_path])
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')
                            continue

                        src_mnt_host_dir = os.path.join(parent_restore_dir, 'mnt', 'host')
                        dest_project_dir = os.path.join(MNT_DIR_PATH, command_project.project_name)

                        print('Deleting mount directory {}'.format(dest_project_dir))
                        subproc = subprocess.run(['rm', '-rf', dest_project_dir])
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')
                            continue

                        print('Restoring mount directory {}'.format(dest_project_dir))
                        os.makedirs(dest_project_dir)
                        subproc = subprocess.run(['/bin/bash', '-c', '--', 'cp -a {} {}'.format(
                            os.path.join(src_mnt_host_dir, '*'),
                            os.path.join(dest_project_dir, '')
                        )])
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')
                            continue

                        print('Restoring Nomad job specification {}'.format(command_project.nomad_job_file_path))
                        src_nomad_job_path = os.path.join(parent_restore_dir, 'nomad-job.hcl')
                        subproc = subprocess.run(['cp', '-a', src_nomad_job_path, command_project.nomad_job_file_path])
                        if subproc.returncode != 0:
                            print_color('red', '*** ERROR RETURNED ***\n')
                            continue
                        print()

            elif user_command_name in ('jlog',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                elif len(command_projects) > 1:
                    print('Multiple projects match:')
                    for command_project in command_projects:
                        print('  ' + command_project.project_name)
                else:
                    command_project = command_projects[0]
                    print('Matched ' + command_project.project_name)

                    live_allocation_ids = query_nomad_job_allocation_ids(command_project.project_name)
                    all_allocation_ids = query_nomad_job_allocation_ids(command_project.project_name, include_dead_allocs=True)
                    dead_allocation_ids = [aid for aid in all_allocation_ids if aid not in live_allocation_ids]
                    if len(all_allocation_ids) == 0:
                        print_color('yellow', 'No job logs found')
                        continue

                    local_logs_dir_template = '/var/lib/nomad/alloc/{}/alloc/logs'

                    local_allocation_ids = []
                    for allocation_id in all_allocation_ids:
                        local_logs_dir = local_logs_dir_template.format(allocation_id)
                        local_logs_paths = glob.glob(os.path.join(local_logs_dir, '*'))
                        if local_logs_paths:
                            local_allocation_ids.append(allocation_id)

                    def build_offers(offer_allocation_ids):
                        offers = []
                        for offer_allocation_id in offer_allocation_ids:
                            allocation = query_nomad_allocation(offer_allocation_id)
                            if not allocation:
                                continue
                            allocation_name = allocation[0]
                            node_id = allocation[2]
                            node = query_nomad_node(node_id)
                            node_name = node[0]

                            if offer_allocation_id in local_allocation_ids:
                                local_logs_dir = local_logs_dir_template.format(offer_allocation_id)
                                local_logs_paths = glob.glob(os.path.join(local_logs_dir, '*'))
                                for local_logs_path in local_logs_paths:
                                    offers.append(('local', node_name, allocation_name, local_logs_path))
                            else:
                                subproc = subprocess.run(['nomad', 'logs', '-tail', '-c', '1', offer_allocation_id],
                                                         stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                                if subproc.returncode == 0:
                                    offers.append(('stream:stdout', node_name, allocation_name, offer_allocation_id))
                                    offers.append(('stream:stderr', node_name, allocation_name, offer_allocation_id))
                        offers.sort(key=lambda line: line[1]+line[2]+line[3]+line[0])
                        return offers

                    def print_offers(offers, first_offer_index):
                        for offer in offers:
                            offer_type = offer[0]
                            if offer_type == 'local':
                                offer_name = '{} (local)'.format(os.path.basename(offer[3]))
                            elif offer_type.startswith('stream:'):
                                if offer_type.endswith(':stdout'):
                                    offer_name = 'stdout (remote)'
                                elif offer_type.endswith(':stderr'):
                                    offer_name = 'stderr (remote)'
                                else:
                                    raise Exception('illegal state')
                            print('    [{}] {}/{}/{}'.format(first_offer_index, offer[1], offer[2], offer_name))
                            first_offer_index += 1
                        return first_offer_index

                    print('Found {} matching job allocations'.format(len(all_allocation_ids)))

                    offered = []
                    first_offer_index = 1
                    if live_allocation_ids:
                        offers = build_offers(live_allocation_ids)
                        if offers:
                            print('\n  Running job logs:')
                            first_offer_index = print_offers(offers, first_offer_index)
                            offered += offers

                    if dead_allocation_ids:
                        offers = build_offers(dead_allocation_ids)
                        if offers:
                            print('\n  Recent job logs:')
                            first_offer_index = print_offers(offers, first_offer_index)
                            offered += offers

                    user_input, _ = read_term_input('\nWhich log? ', stdin_itc=stdin_itc)
                    if user_input is None or user_input.strip() == '':
                        continue
                    try:
                        offered_index = int(user_input)
                    except ValueError:
                        print('Invalid input')
                        continue
                    if offered_index < 1 or offered_index > len(offered):
                        print('Invalid choice')
                        continue
                    offered_index -= 1

                    offer = offered[offered_index]
                    offer_type = offer[0]
                    if offer_type == 'local':
                        local_log_path = offer[3]
                        launch_less(file_path=local_log_path)
                    elif offer_type.startswith('stream:'):
                        log_allocation_id = offer[3]
                        cmd = ['nomad', 'logs']
                        if offer_type.endswith(':stderr'):
                            cmd.append('-stderr')
                        cmd.append(log_allocation_id)
                        subproc = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                        log = subproc.stdout.decode()
                        launch_less(stdin=log)
                    else:
                        raise Exception('illegal state')

            elif user_command_name in ('mless',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                    continue
                elif len(command_projects) > 1:
                    print('Multiple projects match:')
                    for command_project in command_projects:
                        print('  ' + command_project.project_name)
                    continue
                command_project = command_projects[0]
                print('Matched ' + command_project.project_name)

                mnt_alloc_dir_paths = glob.glob(os.path.join(MNT_DIR_PATH, command_project.project_name, '*'))
                if len(mnt_alloc_dir_paths) == 0:
                    print('No container mount directories found for this project')
                    continue
                elif len(mnt_alloc_dir_paths) > 1:
                    print('Found {} container mount directories:'.format(len(mnt_alloc_dir_paths)))
                    print_job_mount_paths(command_project.project_name, mnt_alloc_dir_paths, local_node_id, is_offer=True)
                    user_input = input('\nWhich mount directory do you want ({}-{})? '.format(1, len(mnt_alloc_dir_paths)))
                    try:
                        mnt_alloc_dir_path_index = int(user_input) - 1
                    except ValueError:
                        print('Invalid choice')
                        continue
                else:
                    mnt_alloc_dir_path_index = 0

                mnt_alloc_dir_path = mnt_alloc_dir_paths[mnt_alloc_dir_path_index]
                history_key = 'mless-history/{}'.format(command_project.project_name)
                mnt_dir_file_path = print_tree_and_user_select_path(mnt_alloc_dir_path, 'text', 2, stdin_itc,
                                                                    get_history_list(history_key))
                if not mnt_dir_file_path:
                    continue

                launch_less(file_path=mnt_dir_file_path)

            elif user_command_name in ('mshell',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                    continue
                elif len(command_projects) > 1:
                    print('Multiple projects match:')
                    for command_project in command_projects:
                        print('  ' + command_project.project_name)
                    continue
                command_project = command_projects[0]
                print('Matched ' + command_project.project_name)

                mnt_alloc_dir_paths = glob.glob(os.path.join(MNT_DIR_PATH, command_project.project_name, '*'))
                if len(mnt_alloc_dir_paths) == 0:
                    print('No container mount directories found for this project')
                    continue
                elif len(mnt_alloc_dir_paths) > 1:
                    print('Found {} container mount directories:')
                    for mnt_alloc_dir_path in mnt_alloc_dir_paths:
                        print('  ' + mnt_alloc_dir_path)
                    user_input = input('Which mount directory do you want [{}]? '.format(mnt_alloc_dir_path[0]))
                    if user_input.strip() == '':
                        mnt_alloc_dir_path_index = 0
                    try:
                        mnt_alloc_dir_path_index = mnt_alloc_dir_paths.index(user_input.strip())
                    except ValueError:
                        print('Invalid choice')
                        continue
                else:
                    mnt_alloc_dir_path_index = 0

                mnt_alloc_dir_path = mnt_alloc_dir_paths[mnt_alloc_dir_path_index]
                history_key = 'mshell-history/{}'.format(command_project.project_name)
                mnt_dir_shell_path = print_tree_and_user_select_path(mnt_alloc_dir_path, 'dir', 3, stdin_itc,
                                                                     get_history_list(history_key))
                if not mnt_dir_shell_path:
                    continue

                launch_shell(mnt_dir_shell_path)

            elif user_command_name in ('mclean',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                    continue
                job_status_by_job_name = query_nomad_job_statuses()
                prestop_all = False
                for command_project in command_projects:
                    mnt_alloc_dir_paths = glob.glob(os.path.join(MNT_DIR_PATH, command_project.project_name, '*'))
                    print_color(None, '\nCleaning mounts for {}'.format(command_project.project_name))
                    if len(mnt_alloc_dir_paths) == 0:
                        print_color(None, '  No container mount directories found')
                        continue

                    def on_rmtree_error(function, path, excinfo):
                        print_color('red',
                                    '  Error cleaning {}: {}'
                                        .format(command_project.project_name, excinfo[1]))
                    delete_all = False
                    for mnt_alloc_dir_path in mnt_alloc_dir_paths:
                        if not delete_all:
                            print_job_mount_paths(command_project.project_name, [mnt_alloc_dir_path], local_node_id,
                                                  path_color='green')
                            user_input, _ = read_term_input('  Delete this path (y/n/a) [n]? '.format(command_project.project_name),
                                                            stdin_itc=stdin_itc)
                            if not user_input or user_input.strip() in ('n', ''):
                                continue
                            if user_input.strip() == 'a':
                                delete_all = True

                        do_stop_job = prestop_all
                        if not prestop_all and \
                                job_status_by_job_name.get(command_project.project_name) == 'running':
                            user_input, _ = read_term_input('  Do you want to stop this job before deleting it (y/n/a) [y]? ',
                                                            stdin_itc=stdin_itc)
                            if not user_input:
                                continue
                            elif user_input.strip() in ('y', ''):
                                do_stop_job = True
                            elif user_input.strip() == 'a':
                                do_stop_job = prestop_all = True
                        if do_stop_job:
                            stop_job(command_project.project_name, projects, output_prefix='  ')

                        print_color(None, '  Deleting {}'.format(mnt_alloc_dir_path))
                        shutil.rmtree(mnt_alloc_dir_path, ignore_errors=False, onerror=on_rmtree_error)

            elif user_command_name in ('sedit',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                    continue
                elif len(command_projects) > 1:
                    print('Multiple projects match:')
                    for command_project in command_projects:
                        print('  ' + command_project.project_name)
                    continue
                command_project = command_projects[0]
                print('Matched ' + command_project.project_name)

                history_key = 'sedit-history/{}'.format(command_project.project_name)
                edit_file_path = print_tree_and_user_select_path(command_project.project_dir_path,
                                                                 'text', 10, stdin_itc,
                                                                 get_history_list(history_key))
                if not edit_file_path:
                    continue

                launch_vim(command_project.project_dir_path, edit_file_path)

            elif user_command_name in ('sshell',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                    continue
                elif len(command_projects) > 1:
                    print('Multiple projects match:')
                    for command_project in command_projects:
                        print('  ' + command_project.project_name)
                    continue
                command_project = command_projects[0]
                print('Matched ' + command_project.project_name)

                history_key = 'sshell-history/{}'.format(command_project.project_name)
                shell_dir = print_tree_and_user_select_path(command_project.project_dir_path,
                                                            'dir', 10, stdin_itc,
                                                            get_history_list(history_key))
                if not shell_dir:
                    continue

                launch_shell(shell_dir)

            elif user_command_name in ('ssync',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                for command_project in command_projects:
                    project_sync_file_path = os.path.join(command_project.project_dir_path, 'sync.sh')
                    if not os.path.isfile(project_sync_file_path):
                        print('Skipping {}: no sync script found'.format(command_project.project_name))
                        continue
                    print('========================================================')
                    print('Syncing workspace to {}'.format(command_project.project_name))
                    print('========================================================\n')
                    subproc = subprocess.run(['/bin/bash', project_sync_file_path],
                                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                    print(subproc.stdout.decode())
                    if subproc.returncode != 0:
                        print_color('red', '*** ERROR RETURNED ***\n')

            elif user_command_name in ('dshell',):
                command_projects = projects_from_project_specs(user_command_parts[1:], projects)
                if not command_projects:
                    print('No matching projects')
                    continue
                elif len(command_projects) > 1:
                    print('Multiple projects match:')
                    for command_project in command_projects:
                        print('  ' + command_project.project_name)
                    continue
                command_project = command_projects[0]
                print('Matched ' + command_project.project_name)

                docker_image_tag_script_path = 'docker_image_tag.sh'
                if not os.path.isfile(docker_image_tag_script_path):
                    print_color('red', 'Missing {}'.format(docker_image_tag_script_path))
                    continue

                job_status_by_job_name = query_nomad_job_statuses()
                if job_status_by_job_name.get(command_project.project_name) != 'running':
                    print_color('red', '{} is not running'.format(command_project.project_name))
                    continue

                allocation_ids = query_nomad_job_allocation_ids(command_project.project_name)
                if not allocation_ids:
                    print('No allocations found for project')
                    continue

                project_container_ids = query_nomad_docker_containers(allocation_ids)
                if not project_container_ids:
                    print('No matching local containers found')
                    continue
                elif len(project_container_ids) > 1:
                    print('Multiple matching local containers found')
                    user_input, _ = read_term_input('Which container do you want (1-{}) [1]? '
                            .format(len(project_container_ids)), stdin_itc=stdin_itc)
                    if user_input is None or user_input.strip() == '':
                        project_container_ids_index = 1
                    try:
                        project_container_ids_index = int(user_input)
                    except ValueError:
                        print('Invalid input')
                        continue
                    if project_container_ids_index < 1 or project_container_ids_index > len(allocation_ids):
                        print('Invalid choice')
                        continue
                    project_container_ids_index -= 1
                else:
                    project_container_ids_index = 0
                user_container_id = project_container_ids[project_container_ids_index]

                signal.signal(signal.SIGINT, signal.SIG_IGN)
                try:
                    subproc = subprocess.Popen(['docker', 'exec', '-it', user_container_id, 'bash'])
                    subproc.wait()
                finally:
                    signal.signal(signal.SIGINT, on_sigint)

            else:
                print('Illegal command')
                continue

def exit(exit_status):
    sys.exit(exit_status)

def start_stdin_thread(stdin_itc):
    def run():
        stdin_data_queue, stdin_command_pipe = stdin_itc
        stdin_command_pipe_in_fd, _ = stdin_command_pipe
        command_queue = []
        while True:
            read_ready, _, _ = select.select([sys.stdin, stdin_command_pipe_in_fd], [], [])

            if stdin_command_pipe_in_fd in read_ready:
                command = os.read(stdin_command_pipe_in_fd, 1).decode()
                if command == 'q':
                    return
                elif command == curses.ascii.ctrl('c'):
                    if len(command_queue) > 0 and type(command_queue[0]) is int:
                        command_queue.pop(0)
                        stdin_data_queue.put(command)
                elif command >= '0' and command <= '9':
                    byte_count = int(command)
                    command_queue.append(byte_count)

            if sys.stdin in read_ready:
                if len(command_queue) > 0 and type(command_queue[0]) is int:
                    byte_count = command_queue.pop(0)
                    bytes_read = os.read(sys.stdin.fileno(), byte_count)
                    stdin_data_queue.put(bytes_read.decode())

    stdin_thread = threading.Thread(target=run, daemon=True)
    stdin_thread.start()

if __name__ == "__main__":
    stdin_data_queue = queue.Queue()
    stdin_command_pipe = os.pipe()
    # inter thread communication
    stdin_itc = (stdin_data_queue, stdin_command_pipe)

    def on_sigint(signum, stackframe):
        _, stdin_command_pipe = stdin_itc
        _, stdin_command_pipe_out_fd = stdin_command_pipe
        os.write(stdin_command_pipe_out_fd, curses.ascii.ctrl('c').encode())
    signal.signal(signal.SIGINT, on_sigint)

    start_stdin_thread(stdin_itc)

    main(stdin_itc)
