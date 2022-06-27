#!/bin/bash

cd `dirname $0`

if [ "$1" = "" ]; then
    echo "usage: $0 <other-bzr-workspace-path>" 1>&2
    exit 1
fi

for path in `find "$1" -type d -name .bzr -depth 2`; do
    path=`dirname $path`
    OTHERPROJPATHS+=("$path")
done

for path in `find ../.. -type d -name .bzr -depth 2`; do
    path=`dirname $path`
    CHECKPROJPATHS+=("$path")
done

TMPDIR=/tmp/bzrsh
mkdir -p $TMPDIR

cleanup()  {
    rm $TMPDIR/*
}

for projpath in "${CHECKPROJPATHS[@]}"; do
    found_project=0
    for otherpath in "${OTHERPROJPATHS[@]}"; do
        if [ `basename $otherpath` = `basename $projpath` ]; then
            found_project=1
            break
        fi
    done
    if [ $found_project = 0 ]; then
        echo "** No matching project for $projpath"
    fi
done
for otherpath in "${OTHERPROJPATHS[@]}"; do
    found_project=0
    for projpath in "${CHECKPROJPATHS[@]}"; do
        if [ `basename $otherpath` = `basename $projpath` ]; then
            found_project=1
            break
        fi
    done
    if [ $found_project = 0 ]; then
        echo "** No matching project for $otherpath"
    fi
done

i=0
PROJNAMES=();
OUTFILES=(); ERRFILES=()
for projpath in "${CHECKPROJPATHS[@]}"; do
    for otherpath in "${OTHERPROJPATHS[@]}"; do
        if [ `basename $otherpath` = `basename $projpath` ]; then
            projname=`basename $projpath`
            PROJNAMES+=("$projname")
            OTHERPROJECTS+=("$otherpath")
            outfile=`mktemp $TMPDIR/out.$projname.XXX`
            OUTFILES+=("$outfile")
            errfile=`mktemp $TMPDIR/err.$projname.XXX`
            ERRFILES+=("$errfile")
            echo "Comparing $projname"
            bash -c "cd \"$projpath\"; bzr missing \"$otherpath\" >$outfile 2>$errfile" &
            (( i += 1 ))
            if [ $i = 10 ]; then
                i=0
                wait
            fi
        fi
    done
done
wait

i=0
for outfile in "${OUTFILES[@]}"; do
    outsize=`ls -lt "$outfile" | head -n1 | awk '{print $5}'`
    if [ $outsize -ne 25 ]; then
        echo
        echo "********************************************************************************"
        echo " ${PROJNAMES[$i]} has the following differences:"
        echo
        cat "$outfile"
        echo
    fi
    (( i += 1 ))
done

if [ ${#OUTFILES[@]} = 0 ]; then
    echo
    echo "No differences"
    echo
fi

cleanup
exit 0

