#!/usr/bin/python
# coding=utf-8

import sys, os, os.path
import xml.dom.minidom as xml
import glob
import subprocess
import re
import math
import termios, curses, curses.ascii
import Queue
from multiprocessing import Process, Queue as MpQueue
import traceback

class Project:
    def __init__(self):
        self.localPath             = None
        self.localProjectName      = None

        self.mavenGroupId           = None
        self.mavenArtifactId        = None
        self.mavenVersion           = None
        self.mavenVersionIsSnaphsot = False

        self.bazaarRemoteUri             = None
        self.bazaarRemoteProjectName     = None
        self.bazaarTagVersion            = None
        self.bazaarPathName              = None
        self.bazaarPathCategory          = None
        self.bazaarPathProduct           = None
        self.bazaarHasUnversionedCommits = False
        self.bazaarHasWorkspaceChanges   = False
        self.bazaarHasWorkspaceUnrevisionedChanges = False
        self.bazaarHasWorkspaceShelf     = False

        self.dependencies = []

def parseMavenGAV(xmlNode):
    groupIdNodes = xmlNode.getElementsByTagName('groupId')
    groupId = None
    if groupIdNodes:
        groupIdNodes = groupIdNodes[0].childNodes
        for groupIdNode in groupIdNodes:
            if groupIdNode.nodeType == xml.Node.TEXT_NODE:
                groupId = groupIdNodes[0].data
                break
    artifactId = None
    artifactIdNodes = xmlNode.getElementsByTagName('artifactId')
    if artifactIdNodes:
        artifactIdNodes = artifactIdNodes[0].childNodes
        for artifactIdNode in artifactIdNodes:
            if artifactIdNode.nodeType == xml.Node.TEXT_NODE:
                artifactId = artifactIdNodes[0].data
                break
    version = None
    versionNodes = xmlNode.getElementsByTagName('version')
    if versionNodes:
        versionNodes = versionNodes[0].childNodes
        for versionNode in versionNodes:
            if versionNode.nodeType == xml.Node.TEXT_NODE:
                version = versionNodes[0].data
                break
    versionIsSnapshot = False
    if version.strip().lower().endswith('-snapshot'):
        m = re.match(r'^(.*?)-SNAPSHOT$', version.strip(), re.I)
        if m:
            versionIsSnapshot = True
            version = m.group(1)
    return (groupId, artifactId, version, versionIsSnapshot)

def readInput(prompt):
    return raw_input(prompt)

readTermHistory = {}

def getTermHistory(historyKey):
    if historyKey not in readTermHistory:
        return []
    return readTermHistory[historyKey]

def readTermInput(prompt, historyKey=None, historyList=[], prefill='', alternate=None):
    '''
    prompt       is the text to show the user
    historyKey   is a key to a map that stores past history of user input. The user can use up and
                 down arrow keys to scroll through past history for the key specified.
    prefill      is a string value to print as if the user had typed it in. The user can
                 delete/modify this if they choose or just hit enter to accept it.
    alternate    is a string value that can replace whatever the user typed (or whatever was
                 specified by 'prefill') when the user hits the tab key.
    '''
    #historyList = []
    historyIndex = -1
    historySavedInput = None
    if not historyList and historyKey in readTermHistory:
        historyList = readTermHistory[historyKey]
    else:
        readTermHistory[historyKey] = historyList

    fd = sys.stdin.fileno()
    old = termios.tcgetattr(fd)
    new = termios.tcgetattr(fd)
    new[3] = new[3] & ~termios.ICANON & ~termios.ECHO
    new[6][termios.VMIN] = 1
    new[6][termios.VTIME] = 0
    termios.tcsetattr(fd, termios.TCSANOW, new)

    if prompt:
        sys.stdout.write(prompt)

    userInput = prefill or ''
    if userInput:
        sys.stdout.write(userInput)

    # Begin insert mode
    sys.stdout.write(curses.tigetstr("smir"))
    sys.stdout.flush()

    userInputIndex = len(userInput)
    isAlternate = False

    try:
        remainder = ''
        while True:
            if remainder:
                c = remainder[0]
                remainder = remainder[1:]
            else:
                c = os.read(fd, 6)
            if len(c) == 1:
                if ord(c) == curses.ascii.ESC:
                    # Escape key
                    userInput = None
                    print
                    break
                elif ord(c) == curses.ascii.DEL:
                    # Backspace key
                    if userInputIndex:
                        #sys.stdout.write(chr(curses.ascii.BS))
                        sys.stdout.write(curses.tparm(curses.tigetstr("cub"), 1))
                        sys.stdout.write(curses.tigetstr("dch1"))
                        userInput = userInput[:userInputIndex-1] + userInput[userInputIndex:]
                        userInputIndex -= 1
                elif ord(c) == curses.ascii.TAB:
                    if alternate is not None:
                        isAlternate = not isAlternate
                        if userInputIndex:
                            sys.stdout.write(curses.tparm(curses.tigetstr("cub"), userInputIndex))
                        sys.stdout.write(curses.tparm(curses.tigetstr("dch"), len(userInput)))
                        oldUserInput = userInput
                        userInput = alternate
                        alternate = oldUserInput
                        sys.stdout.write(userInput)
                        userInputIndex = len(userInput)
                elif ord(c) in (curses.ascii.NL, curses.ascii.CR):
                    if not historyList or userInput != historyList[-1]:
                        historyList.append(userInput)
                    print
                    #print repr(historyList)
                    break
                elif c == curses.ascii.ctrl('a'):
                    # Go to input beginning
                    if userInputIndex:
                        sys.stdout.write(curses.tparm(curses.tigetstr("cub"), userInputIndex))
                    userInputIndex = 0
                elif c == curses.ascii.ctrl('e'):
                    # Go to input ending
                    sys.stdout.write(curses.tparm(curses.tigetstr("cuf"), len(userInput)-userInputIndex))
                    userInputIndex = len(userInput)
                elif c == curses.ascii.ctrl('k'):
                    # Clear to end of input
                    sys.stdout.write(curses.tparm(curses.tigetstr("dch"), len(userInput)-userInputIndex))
                    userInput = userInput[:userInputIndex]
                    userInputIndex = len(userInput)
                elif c == curses.ascii.ctrl('u'):
                    # Clear to start of input
                    userInput = userInput[userInputIndex:]
                    if userInputIndex:
                        sys.stdout.write(curses.tparm(curses.tigetstr("cub"), userInputIndex))
                    sys.stdout.write(curses.tparm(curses.tigetstr("dch"), userInputIndex))
                    userInputIndex = 0
                elif not curses.ascii.iscntrl(c):
                    # Character input
                    userInput = userInput[:userInputIndex] + c + userInput[userInputIndex:]
                    userInputIndex += 1
                    sys.stdout.write(c)
            elif c == '\x1b[3~':
                # Delete key
                sys.stdout.write(curses.tigetstr("dch1"))
                userInput = userInput[:userInputIndex] + userInput[userInputIndex+1:]
            elif c == '\x1b[D':
                # Left arrow key
                userInputIndex -= 1
                if userInputIndex < 0:
                    userInputIndex = 0
                else:
                    sys.stdout.write(curses.tparm(curses.tigetstr("cub"), 1))
            elif c == '\x1b[C':
                # Right arrow key
                userInputIndex += 1
                if userInputIndex > len(userInput):
                    userInputIndex = len(userInput)
                else:
                    sys.stdout.write(curses.tparm(curses.tigetstr("cuf"), 1))
            elif c == '\x1b[A':
                # Up arrow key
                if historyList:
                    if userInputIndex:
                        sys.stdout.write(curses.tparm(curses.tigetstr("cub"), userInputIndex))
                    sys.stdout.write(curses.tparm(curses.tigetstr("dch"), len(userInput)))
                    if historyIndex == -1:
                        historySavedInput = userInput
                        historyIndex = len(historyList)
                    historyIndex -= 1
                    if historyIndex == -1:
                        userInput = historySavedInput
                    else:
                        userInput = historyList[historyIndex]
                    userInputIndex = len(userInput)
                    sys.stdout.write(userInput)
            elif c == '\x1b[B':
                # Down arrow key
                if historyList:
                    if userInputIndex:
                        sys.stdout.write(curses.tparm(curses.tigetstr("cub"), userInputIndex))
                    sys.stdout.write(curses.tparm(curses.tigetstr("dch"), len(userInput)))
                    if historyIndex == -1:
                        historySavedInput = userInput
                    historyIndex += 1
                    if historyIndex == len(historyList):
                        userInput = historySavedInput
                        historyIndex = -1
                    else:
                        userInput = historyList[historyIndex]
                    userInputIndex = len(userInput)
                    sys.stdout.write(userInput)
            elif c[0] != '\x1b':
                remainder = c
            else:
                sys.stdout.write('<'+repr(c)+'>')
            sys.stdout.flush()
    finally:
        # Exit insert mode
        sys.stdout.write(curses.tigetstr("rmir"))
        sys.stdout.flush()
        termios.tcsetattr(fd, termios.TCSAFLUSH, old)

    return userInput, isAlternate

def scanProjectPaths(workspacePath, scanPaths=None, quiet=False):
    ### Parse POMs and capture Bazaar metadata

    if not scanPaths:
        pomPaths = glob.glob(os.path.join(workspacePath, '*/pom.xml'))
    else:
        pomPaths = []
        for scanPath in scanPaths:
            if not scanPath.endswith('/pom.xml'):
                scanPath += '/pom.xml'
            pomPaths.append(scanPath)

    pomPathQueue = MpQueue(len(pomPaths))
    for pomPath in pomPaths:
        pomPathQueue.put(pomPath)

    projectResultQueue = MpQueue(len(pomPaths))

    processes = []
    for i in range( min(len(pomPaths),5) ):
        process = Process(target=scanProjectPathsThread, args=(pomPathQueue, projectResultQueue, quiet))
        process.start()
        processes.append(process)

    projects = []
    while len(projects) < len(pomPaths):
        project = projectResultQueue.get()
        projects.append(project)
    projects.sort(lambda x,y: cmp(x.localProjectName, y.localProjectName))

    for process in processes:
        process.join()

    return projects

def scanProjectPathsThread(pomPathQueue, projectResultQueue, quiet):
    try:
        i = 0
        try:
            pomPath = pomPathQueue.get(True, 1.0)
        except Queue.Empty:
            sys.stderr.write("No work received\n")
            return
        while True:
            i += 1
            #if i == 2: break
            pomDirPath = os.path.dirname(pomPath)
            pomDirname = os.path.basename(pomDirPath)
            if not quiet:
                print(pomDirname)

            pom = xml.parse(pomPath)
            (groupId, artifactId, version, versionIsSnapshot) = parseMavenGAV(pom)

            project = Project()

            project.localPath = pomDirPath
            project.localProjectName = pomDirname

            project.mavenGroupId = groupId
            project.mavenArtifactId = artifactId
            project.mavenVersion = version
            project.mavenVersionIsSnaphsot = versionIsSnapshot

            dependenciesNodes = pom.getElementsByTagName('dependencies')
            if dependenciesNodes:
                dependencyNodes = dependenciesNodes[0].getElementsByTagName('dependency')
                for dependencyNode in dependencyNodes:
                    (groupId, artifactId, version, versionIsSnapshot) = parseMavenGAV(dependencyNode)
                    dependency = Project()
                    dependency.mavenGroupId = groupId
                    m = re.match(r'^client-(.*)$', artifactId)
                    dependency.mavenArtifactId = m and m.group(1) or artifactId
                    dependency.mavenVersion = version
                    dependency.mavenVersionIsSnaphsot = versionIsSnapshot
                    project.dependencies.append(dependency)
                    #print('Adding dependency from ' + project.localProjectName + ' -> ' + dependency.mavenArtifactId)

            if os.path.exists(os.path.join(pomDirPath, '.bzr')):
                bzrInfoOutput = subprocess.check_output(['bzr', 'info'], cwd=pomDirPath, stderr=subprocess.STDOUT)
                m = re.search(r'checkout of branch: (.*)\n', bzrInfoOutput)
                if m:
                    project.bazaarRemoteUri = m.group(1)
                    bazaarRemoteUriParts = project.bazaarRemoteUri.split('/')
                    for part in reversed(bazaarRemoteUriParts):
                        if part.strip() == '':
                            continue
                        if project.bazaarRemoteProjectName is None:
                            project.bazaarRemoteProjectName = part
                            continue
                        if project.bazaarPathName is None:
                            project.bazaarPathName = part
                            continue
                        if project.bazaarPathCategory is None:
                            project.bazaarPathCategory = part
                            continue
                        if project.bazaarPathProduct is None:
                            project.bazaarPathProduct = part
                            break

                bzrStatOutput = subprocess.check_output(['bzr', 'stat', '-S'], cwd=pomDirPath, stderr=subprocess.STDOUT)
                m = re.search(r'^(?:-D| M|\+N)', bzrStatOutput, re.MULTILINE)
                if m:
                    project.bazaarHasWorkspaceChanges = True

                m = re.search(r'^\?', bzrStatOutput, re.MULTILINE)
                if m:
                    project.bazaarHasWorkspaceUnrevisionedChanges = True

                m = re.search(r'^\d+ shel(:?f|ves) exists?\.', bzrStatOutput, re.MULTILINE)
                if m:
                    project.bazaarHasWorkspaceShelf = True

                bzrLogOutput = subprocess.check_output(['bzr', 'log'], cwd=pomDirPath, stderr=subprocess.STDOUT)
                for m in re.finditer(r'revno: .*\n(?:tags: (.*)\n)?', bzrLogOutput):
                    if not m.group(1):
                        project.bazaarHasUnversionedCommits = True
                    else:
                        project.bazaarTagVersion = m.group(1)
                        break

            projectResultQueue.put(project)

            try:
                pomPath = pomPathQueue.get(False)
            except Queue.Empty:
                return
    except:
        sys.stderr.write(traceback.format_exc()+'\n')
        return

def scanProjects(workspacePath, workspaceProjects, scanProjects):
    if scanProjects:
        print
        print 'Rescanning projects...'
        scanPaths = [p.localPath for p in scanProjects]
        scannedProjects = scanProjectPaths(workspacePath, scanPaths, quiet=True)
        for scannedProject in scannedProjects:
            for i in range(len(workspaceProjects)):
                workspaceProject = workspaceProjects[i]
                if scannedProject.localPath == workspaceProject.localPath:
                    workspaceProjects[i] = scannedProject
                    break
    return workspaceProjects

def displayProjects(workspaceProjects, displayProjects, title, showIndex=True):
    projectStrings = []
    for project in displayProjects:
        projectHasMavenVersionMismatch = False
        for dependency in project.dependencies:
            for p in workspaceProjects:
                if p.mavenGroupId == dependency.mavenGroupId and p.mavenArtifactId == dependency.mavenArtifactId:
                    if p.mavenVersion != dependency.mavenVersion or \
                            p.mavenVersionIsSnaphsot != dependency.mavenVersionIsSnaphsot:
                        projectHasMavenVersionMismatch = True
                        break
            if projectHasMavenVersionMismatch:
                break

        bzrTagVersion = u''
        if project.bazaarTagVersion:
            bzrTagVersion = project.bazaarTagVersion[-12:].decode('utf-8')
            if len(project.bazaarTagVersion) > len(bzrTagVersion):
                bzrTagVersion = u'…' + bzrTagVersion
            if project.bazaarHasUnversionedCommits:
                bzrTagVersion += u'*'

        projectString = [bzrTagVersion,
                         project.mavenVersion and
                             (project.mavenVersionIsSnaphsot and
                                 (project.mavenVersion+'-S') or project.mavenVersion)
                             or '',
                         project.bazaarPathProduct or '',
                         project.bazaarPathCategory or '',
                         project.bazaarPathName or '',
                         project.localProjectName or '',
                         project.bazaarRemoteProjectName and
                             ((project.bazaarHasWorkspaceChanges and
                                 (project.bazaarRemoteProjectName+'*') or project.bazaarRemoteProjectName) +
                                    (project.bazaarHasWorkspaceUnrevisionedChanges and '%' or '') +
                                    (project.bazaarHasWorkspaceShelf and '#' or ''))
                             or '',
                         project.mavenArtifactId and
                            (projectHasMavenVersionMismatch and
                                (project.mavenArtifactId+'*') or project.mavenArtifactId)
                            or '']
        projectStrings.append(projectString)

    projectStringLengths = [3] * 8
    for i, projectString in enumerate(projectStrings):
        for j, oldProjectStringLen in enumerate(projectStringLengths):
            if projectString[j]:
                newProjectStringLen = len(projectString[j])
                if newProjectStringLen > oldProjectStringLen:
                    projectStringLengths[j] = newProjectStringLen

    indexLen = showIndex and int(math.log10(len(displayProjects)))+1 or 0
    print
    print(title)
    print((' '*indexLen) + '    Bazaar tag version (*=commits since tag)')
    print((' '*indexLen) + '     |'+(' '*(projectStringLengths[0]-1))+'Maven version (S=snapshot version)')
    print((' '*indexLen) + '     |'+(' '*(projectStringLengths[0]-1))+' |'+(' '*(projectStringLengths[1]-1))+'Bazaar branch product')
    print((' '*indexLen) + '     |'+(' '*(projectStringLengths[0]-1))+' |'+(' '*(projectStringLengths[1]-1))+' |'+(' '*(projectStringLengths[2]-1))+'Bazaar branch category')
    print((' '*indexLen) + '     |'+(' '*(projectStringLengths[0]-1))+' |'+(' '*(projectStringLengths[1]-1))+' |'+(' '*(projectStringLengths[2]-1))+' |'+(' '*(projectStringLengths[3]-1))+'Bazaar branch name')
    print((' '*indexLen) + '     |'+(' '*(projectStringLengths[0]-1))+' |'+(' '*(projectStringLengths[1]-1))+' |'+(' '*(projectStringLengths[2]-1))+' |'+(' '*(projectStringLengths[3]-1))+' |'+(' '*(projectStringLengths[4]-1))+'Local Dir'+(' '*(projectStringLengths[5]-8))+'Bazaar Name (*=has uncommited workspace changes; %=has unrevisioned workspace changes; #=has shelf)')
    print((' '*indexLen) + '     |'+(' '*(projectStringLengths[0]-1))+' |'+(' '*(projectStringLengths[1]-1))+' |'+(' '*(projectStringLengths[2]-1))+' |'+(' '*(projectStringLengths[3]-1))+' |'+(' '*(projectStringLengths[4]-1))+' |'       +(' '*(projectStringLengths[5]-1))+' |'+(' '*(projectStringLengths[6]-1))+'Maven Name (*=has workspace dependency version mismatch)')
    dashesList = [('-'*l) for l in projectStringLengths]
    print((' '*indexLen) + '    '+(' '.join(dashesList)))

    for i, ps in enumerate(projectStrings):
        print(u' [{}] {} {} {} {} {} {} {} {}'.format(showIndex and str(i+1).rjust(indexLen) or '',
                                                     ps[0].ljust(projectStringLengths[0]),
                                                     ps[1].ljust(projectStringLengths[1]),
                                                     ps[2].ljust(projectStringLengths[2]),
                                                     ps[3].ljust(projectStringLengths[3]),
                                                     ps[4].ljust(projectStringLengths[4]),
                                                     ps[5].ljust(projectStringLengths[5]),
                                                     ps[6].ljust(projectStringLengths[6]),
                                                     ps[7].ljust(projectStringLengths[7])) )

def chooseProjects(workspaceProjects):
    displayProjects(workspaceProjects, workspaceProjects, 'Found Projects:')
    print
    print('  [a] All projects')
    print('  [c] Projects with workspace changes')
    print(' [bv] Select by Bazaar branch version')
    print(' [tv] Select by Bazaar tag version')
    print('  [q] Quit')
    print('======================================')

    while True:
        userProjectNumbersString = readInput('Project numbers (space delimited): ')
        userProjectNumbersList = userProjectNumbersString.split(' ')
        userProjectNumbersList = [x for x in userProjectNumbersList if x != '']

        if not userProjectNumbersList:
            return []

        illegalChoice = False
        for userProjectNumber in userProjectNumbersList:
            userProjectNumber = userProjectNumber.strip().lower()
            if userProjectNumber == 'a' or \
                    userProjectNumber == 'q' or \
                    userProjectNumber == 'c' or \
                    userProjectNumber == 'bv' or \
                    userProjectNumber == 'tv':
                userProjectNumbersList = [userProjectNumber]
                break

            try:
               userProjectNumber = int(userProjectNumber)
            except ValueError:
                illegalChoice = True
            if userProjectNumber <= 0 or userProjectNumber > len(workspaceProjects):
                illegalChoice = True
            if illegalChoice:
                print('Illegal project number "{}"'.format(userProjectNumber))
                break
        if illegalChoice:
            continue

        if userProjectNumbersList[0] == 'q':
            return None

        if userProjectNumbersList[0] == 'a':
            userProjectNumbersList = [i+1 for i in range(len(workspaceProjects))]

        if userProjectNumbersList[0] == 'c':
            userProjectNumbersList = []
            for i in range(len(workspaceProjects)):
                if workspaceProjects[i].bazaarHasWorkspaceChanges:
                    userProjectNumbersList.append(i+1)

        if userProjectNumbersList[0] == 'bv':
            print
            bbName = readInput('Bazaar branch name: ')
            if not bbName or not bbName.strip():
                return []
            bbName = bbName.strip()
            userProjectNumbersList = []
            for i in range(len(workspaceProjects)):
                if workspaceProjects[i].bazaarPathName == bbName:
                    userProjectNumbersList.append(i+1)

        if userProjectNumbersList[0] == 'tv':
            print
            btVersion = readInput('Bazaar tag version: ')
            if not btVersion or not btVersion.strip():
                return []
            btVersion = btVersion.strip()
            userProjectNumbersList = []
            for i in range(len(workspaceProjects)):
                if workspaceProjects[i].bazaarTagVersion:
                    bazaarTagVersions = workspaceProjects[i].bazaarTagVersion.split(',')
                    for bazaarTagVersion in bazaarTagVersions:
                        if bazaarTagVersion.strip() == btVersion:
                            userProjectNumbersList.append(i+1)
                            break

        chosenProjects = [workspaceProjects[int(upn)-1] for upn in userProjectNumbersList]
        return chosenProjects

def displayDependencyVersionMismatch(workspaceProjects, displayProjects):
    print
    foundMismatch = False
    for project in displayProjects:
        for dependency in project.dependencies:
            for p in workspaceProjects:
                if p.mavenGroupId == dependency.mavenGroupId and p.mavenArtifactId == dependency.mavenArtifactId:
                    if p.mavenVersion != dependency.mavenVersion or \
                            p.mavenVersionIsSnaphsot != dependency.mavenVersionIsSnaphsot:
                        print(' {} depends on {}:{}:{}{} but found version {}{} in workspace'.format(project.localProjectName,
                                                                                                     dependency.mavenGroupId,
                                                                                                     dependency.mavenArtifactId,
                                                                                                     dependency.mavenVersion,
                                                                                                     dependency.mavenVersionIsSnaphsot and '-SNAPSHOT' or '',
                                                                                                     p.mavenVersion,
                                                                                                     p.mavenVersionIsSnaphsot and '-SNAPSHOT' or ''))
                        foundMismatch = True
                    break
    if not foundMismatch:
        print('No dependency version mismatches found')

def revertProjects(revertProjects):
    print
    print 'This will revert changes for {} projects'.format(len(revertProjects))
    revertConfirmation = readInput('Enter YES to proceed: ')
    if revertConfirmation != 'YES':
        print 'Aborting revert'
    else:
        for revertProject in revertProjects:
            if revertProject.bazaarRemoteUri:
                print '\n'
                print 'Reverting {}:'.format(revertProject.localProjectName)
                bzrRevert = subprocess.check_output(['bzr', 'revert'], cwd=revertProject.localPath, stderr=subprocess.STDOUT)
                print bzrRevert

def pushAndBindProjects(workspacePath, workspaceProjects, branchBindProjects, prompt, push=True, bind=True):
    changedProjects = []
    for project in branchBindProjects:
        print
        print project.localProjectName
        remotePath, isUri = readTermInput(prompt, historyKey='bzrpath', prefill=project.bazaarPathName, alternate=project.bazaarRemoteUri)
        if not remotePath or isUri and remotePath == project.bazaarRemoteUri or not isUri and remotePath == project.bazaarPathName:
            print 'Nothing to do for ' + project.localProjectName
            continue

        if not isUri:
            bazaarRemoteProjectName = None
            bazaarRemoteUriParts = project.bazaarRemoteUri.split('/')
            for i  in range(len(bazaarRemoteUriParts)-1,-1,-1):
                part = bazaarRemoteUriParts[i]
                if part.strip() == '':
                    continue
                if bazaarRemoteProjectName is None:
                    bazaarRemoteProjectName = part
                    continue
                bazaarRemoteUriParts[i] = remotePath
                break
            remotePath = '/'.join(bazaarRemoteUriParts)

        if push or bind:
            print

        if push:
            print 'Pushing {} to {}'.format(project.localProjectName, remotePath)
            try:
                bzrPushOutput = subprocess.check_output(['bzr', 'push', '--create-prefix', remotePath], cwd=project.localPath, stderr=subprocess.STDOUT)
            except subprocess.CalledProcessError as e:
                print
                print 'Bazaar returned exit-status {}:\n{}'.format(e.returncode, e.output)
                continue

        if bind:
            print 'Binding {} to {}'.format(project.localProjectName, remotePath)
            try:
                bzrBindOutput = subprocess.check_output(['bzr', 'bind', remotePath], cwd=project.localPath, stderr=subprocess.STDOUT)
            except subprocess.CalledProcessError as e:
                print
                print 'Bazaar returned exit-status {}:\n{}'.format(e.returncode, e.output)
                continue

        if push or bind:
            changedProjects.append(project)

    workspaceProjects = scanProjects(workspacePath, workspaceProjects, changedProjects)
    return workspaceProjects

def tagProjects(workspacePath, workspaceProjects, tagProjects, prompt):
    changedProjects = []
    newTagName = ''
    for project in tagProjects:
        print
        print project.localProjectName
        historyList = getTermHistory('bzrtag')
        if project.bazaarTagVersion not in historyList:
            historyList.append(project.bazaarTagVersion)
        if newTagName in historyList:
            historyList.remove(newTagName)
        newTagName, _ = readTermInput(prompt, historyKey='bzrtag', historyList=historyList, prefill=newTagName)
        if not newTagName or newTagName == project.bazaarTagVersion:
            print 'Nothing to do for ' + project.localProjectName
            continue

        print
        print 'Tagging {} with "{}"'.format(project.localProjectName, newTagName)
        try:
            bzrTagOutput = subprocess.check_output(['bzr', 'tag', newTagName], cwd=project.localPath, stderr=subprocess.STDOUT)
        except subprocess.CalledProcessError as e:
            print
            print 'Bazaar returned exit-status {}:\n{}'.format(e.returncode, e.output)
            continue

        changedProjects.append(project)

    workspaceProjects = scanProjects(workspacePath, workspaceProjects, changedProjects)
    return workspaceProjects

def updateProjects(workspacePath, workspaceProjects, updateProjects):
    changedProjects = []
    for project in updateProjects:
        print
        print 'Updating {}'.format(project.localProjectName)
        try:
            bzrTagOutput = subprocess.check_output(['bzr', 'up'], cwd=project.localPath, stderr=subprocess.STDOUT)
        except subprocess.CalledProcessError as e:
            print
            print 'Bazaar returned exit-status {}:\n{}'.format(e.returncode, e.output)
            continue

        changedProjects.append(project)

    workspaceProjects = scanProjects(workspacePath, workspaceProjects, changedProjects)
    return workspaceProjects

def filterProjectsByBzrStat(sourceProjects):
    chosenProjects = []
    for sourceProject in sourceProjects:
        print
        print '-' * len(sourceProject.localProjectName)
        print sourceProject.localProjectName

        if not sourceProject.bazaarRemoteUri:
            print 'Not a Bazaar project'
            print
        else:
            try:
                bzrStatOutput = subprocess.check_output(['bzr', 'stat'], cwd=sourceProject.localPath, stderr=subprocess.STDOUT)
            except subprocess.CalledProcessError as e:
                print
                print 'Bazaar returned exit-status {}:\n{}'.format(e.returncode, e.output)
                continue
            print
            print bzrStatOutput

        userInput = readInput('Remove {} from selection [n]: '.format(sourceProject.localProjectName))

        if userInput and userInput.strip().lower() == 'y':
            print 'Removing from selection'
        else:
            chosenProjects.append(sourceProject)

    return chosenProjects

def refreshChosenProjects(workspaceProjects, chosenProjects):
    newChosenProjects = []
    for chosenProject in chosenProjects:
        for workspaceProject in workspaceProjects:
            if workspaceProject.localPath == chosenProject.localPath:
                newChosenProjects.append(workspaceProject)
                break
    return newChosenProjects

def commitProjects(workspaceProjects, commitProjects):
    print
    for commitProject in commitProjects:
        print '-' * len(commitProject.localProjectName)
        print commitProject.localProjectName

        if not commitProject.bazaarRemoteUri:
            print 'Not a Bazaar project'
            print
        elif not commitProject.bazaarHasWorkspaceChanges:
            print 'Not changes to commit'
            print
        else:
            try:
                bzrStatOutput = subprocess.check_output(['bzr', 'stat'], cwd=commitProject.localPath, stderr=subprocess.STDOUT)
            except subprocess.CalledProcessError as e:
                print
                print 'Bazaar returned exit-status {}:\n{}'.format(e.returncode, e.output)
                continue
            print
            print bzrStatOutput

            commitMessage = ''
            while commitMessage == '':
                commitMessage, _ = readTermInput('Commit message: ', historyKey='bzrcommit')
            if commitMessage is None:
                print
                print 'Skipping'
                print
                continue

            try:
                bzrCommitOutput = subprocess.check_output(['bzr', 'commit', '-m', commitMessage], cwd=commitProject.localPath, stderr=subprocess.STDOUT)
            except subprocess.CalledProcessError as e:
                print
                print 'Bazaar returned exit-status {}:\n{}'.format(e.returncode, e.output)
                continue
            print
            print bzrCommitOutput

        for workspaceProject in workspaceProjects:
            if workspaceProject.localPath == commitProject.localPath:
                workspaceProject.bazaarHasWorkspaceChanges = False
                break

    return workspaceProjects

def main():
    curses.setupterm()

    scriptPath = os.path.abspath(os.path.dirname(sys.argv[0]))
    os.chdir(scriptPath)

    overlordPath = os.path.abspath(os.path.join(scriptPath, '..'))
    print
    print 'Using MeincOverlord directory of {}'.format(overlordPath)

    workspacePath = os.path.abspath(os.path.join(overlordPath, '..'))
    print 'Using workspace directory of {}'.format(workspacePath)

    print
    print 'Scanning workspace projects...'
    print
    workspaceProjects = scanProjectPaths(workspacePath)

    while True:
        chosenProjects = chooseProjects(workspaceProjects)

        if chosenProjects is None:
            print
            print('Exiting')
            sys.exit(0)

        if chosenProjects == []:
            continue

        while True:
            chosenProjectNames = [p.localProjectName for p in chosenProjects]
            print
            print 'Selected projects: ' + (', '.join(chosenProjectNames))
            print '======================================'
            print 'Actions:'
            print '  [b] Bind projects'
            print '  [c] Commit projects'
            print ' [fs] Filter selection by bzr stat'
            print '  [l] List selected projects'
            print '  [p] Push projects'
            print ' [pb] Push and bind projects'
            print '  [r] Revert workspace changes'
            print '  [t] Tag projects'
            print '  [u] Update projects'
            print '  [v] Display dependency version mismatches'
            print
            print '  [q] Back to project list'
            print '======================================'
            userActionString = readInput('Action: ')
            userActionString = userActionString.strip().lower()

            if userActionString == '':
                continue
            elif userActionString == 'q':
                break
            elif userActionString == 'l':
                displayProjects(workspaceProjects, chosenProjects, 'Selected Projects:', showIndex=False)
            elif userActionString == 'v':
                displayDependencyVersionMismatch(workspaceProjects, chosenProjects)
            elif userActionString == 'r':
                revertProjects(chosenProjects)
            elif userActionString == 'b':
                workspaceProjects = pushAndBindProjects(workspacePath, workspaceProjects, chosenProjects,
                                                        'Bind to remote version (Tab toggles full path): ',
                                                        push=False)
            elif userActionString == 'pb':
                workspaceProjects = pushAndBindProjects(workspacePath, workspaceProjects, chosenProjects,
                                                        'Push and bind to remote version (Tab toggles full path): ')
            elif userActionString == 'p':
                workspaceProjects = pushAndBindProjects(workspacePath, workspaceProjects, chosenProjects,
                                                        'Push version (Tab toggles full path): ',
                                                        bind=False)
            elif userActionString == 't':
                workspaceProjects = tagProjects(workspacePath, workspaceProjects, chosenProjects,
                                                'New tag name: ')
            elif userActionString == 'u':
                workspaceProjects = updateProjects(workspacePath, workspaceProjects, chosenProjects)
            elif userActionString == 'fs':
                chosenProjects = filterProjectsByBzrStat(chosenProjects)
            elif userActionString == 'c':
                workspaceProjects = commitProjects(workspaceProjects, chosenProjects)

            chosenProjects = refreshChosenProjects(workspaceProjects, chosenProjects)

if __name__ == "__main__":
    main()
