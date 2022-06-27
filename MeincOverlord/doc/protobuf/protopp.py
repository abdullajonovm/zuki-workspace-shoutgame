#!/usr/bin/env python

import sys
import re

def main():
    protofile = ''
    do_enum2int = False

    argc = len(sys.argv)
    if argc == 2:
        protofile = sys.argv[1]
    elif argc == 3:
        do_enum2int = True
        protofile = sys.argv[2]
    else:
        sys.stderr.write('usage: %s [--enum2int] protofile\n' % sys.argv[0])
        sys.exit(1)

    with open(protofile) as f:
        lines = f.readlines()

    iteration = 1
    parmtype_defs = {}
    newtype_names = {}
    while True:
        # Pass #1: find all the parameterized type definitions and their scopes
        newtype_defs = []
        new_parmtype_defs = 0
        i = 0
        while i < len(lines):
            for message_name, message in parmtype_defs.iteritems():
                parmtype_name, line_begin, line_end = message
                if i >= line_begin and i <= line_end:
                    i = line_end + 1
                    break
            if i >= len(lines):
                break

            line = lines[i]

            message_match = re.search(r'^message\s+(\w+)<(\w+)>', line)
            if message_match:
                message_name = message_match.group(1)
                parmtype_name = message_match.group(2)

                line = line.replace('<%s>'%parmtype_name, '-%s'%parmtype_name, 1)
                lines[i] = line

                begin_line = i
                depth = -1
                while i < len(lines):
                    line = lines[i]
                    if line.find('{') != -1:
                        depth = (depth == -1) and 1 or (depth + 1)
                    if line.find('}') != -1:
                        depth -= 1
                        if depth == -2:
                            sys.stderr.write('Malformed message at "%s"\n' % message_name)
                            sys.exit(1)
                        if depth == 0:
                            break
                    i += 1
                end_line = i

                parmtype_defs[message_name] = (parmtype_name, begin_line, end_line)
                new_parmtype_defs += 1

            i += 1

        # Pass #2: find all the uses of parameterized types, creating new types
        i = 0 
        while i < len(lines):
            for message_name, message in parmtype_defs.iteritems():
                parmtype_name, line_begin, line_end = message
                if i >= line_begin and i <= line_end:
                    i = line_end
                    break

            line = lines[i]
            commented_match = re.search(r'^\s*//', line)
            parmuse_match = re.search(r'\b(\w+)<([a-zA-Z0-9_.]+)>', line)
            if not commented_match and parmuse_match:
                message_name = parmuse_match.group(1)
                parm = parmuse_match.group(2)
                parmtype_def = parmtype_defs[message_name]
                parmtype_name, line_begin, line_end = parmtype_def
                full_message_name = '%s_%s' % (message_name, parm.replace('.', '_'))
                if full_message_name not in newtype_names:
                    newtype_def = []
                    for j in range(line_begin,line_end+1):
                        newtype_line = lines[j]
                        newtype_line = re.sub(r'-%s\b'%parmtype_name, '_%s'%parm.replace('.', '_'), newtype_line)
                        newtype_line = re.sub(r'\b%s\b'%parmtype_name, parm, newtype_line)
                        newtype_def.append(newtype_line)
                    newtype_defs.append(newtype_def)
                    newtype_names[full_message_name] = True

                line = re.sub(r'\b%s<%s>' % (message_name,parm), full_message_name, line)
                lines[i] = line
            i += 1

        # Insert new types
        for newtype_def in newtype_defs:
            for newtype_line in newtype_def:
                lines.append(newtype_line)

        if new_parmtype_defs == 0 and len(newtype_defs) == 0:
            break
        iteration += 1

    i = 0
    new_lines = []
    while i < len(lines):
        # Remove all parameter type definitions
        output = True
        for message_name, message in parmtype_defs.iteritems():
            parmtype_name, line_begin, line_end = message
            if i >= line_begin and i <= line_end:
                i = line_end
                output = False
                break
        if output:
            line = lines[i]
            new_lines.append(line)
        i += 1
    lines = new_lines

    if do_enum2int:
        enum_type_names = []
        for line in lines:
            enum_match = re.search(r'enum\s+(\w+)\b', line)
            if enum_match:
                enum_type_name = enum_match.group(1)
                enum_type_names.append(enum_type_name)
        for i in range(0,len(lines)):
            line = lines[i]
            for enum_type_name in enum_type_names:
                line = re.sub(r'\b(?:\w+\.)*%s(\s+\w+)'%enum_type_name, r'int32\1', line)
            lines[i] = line

    for line in lines:
        sys.stdout.write(line)

if __name__ == "__main__":
    main()

