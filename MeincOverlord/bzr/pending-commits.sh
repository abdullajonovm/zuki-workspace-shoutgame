#!/bin/bash

cd `dirname $0`

for path in `find ../.. -type d -name .bzr -depth 2`; do
    path=`dirname $path`
    CHECKPROJPATHS+=("$path")
done

TMPDIR=/tmp/bzrsh
mkdir -p $TMPDIR

cleanup()  {
    rm $TMPDIR/*
}

check_changes() {
    i=0
    PROJPATHS=(); PROJNAMES=(); OUTFILES=(); ERRFILES=()
    for projpath in "${CHECKPROJPATHS[@]}"; do
        PROJPATHS+=("$projpath")
        projname=`basename $projpath`
        PROJNAMES+=("$projname")
        outfile=`mktemp $TMPDIR/out.$projname.XXX`
        OUTFILES+=("$outfile")
        errfile=`mktemp $TMPDIR/err.$projname.XXX`
        ERRFILES+=("$errfile")
        echo "Checking $projname for changes"
        bash -c "cd \"$projpath\"; bzr stat -S | grep \"^-D\| M\|+N\" >$outfile 2>$errfile" &
        (( i += 1 ))
        if [ $i = 10 ]; then
            i=0
            wait
        fi
    done
    wait

    i=0
    RETRY_PROJPATHS=()
    for projname in "${PROJNAMES[@]}"; do
        errsize=`ls -lt "${ERRFILES[$i]}" | head -n1 | awk '{print $5}'`
        if [[ $errsize > 0 ]]; then
            echo "Error checking $projname"
            RETRY_PROJPATHS+=("${PROJPATHS[$i]}")
        else
            outsize=`ls -lt "${OUTFILES[$i]}" | head -n1 | awk '{print $5}'`
            if [ $outsize -gt 0 ]; then
                CHANGED_PROJPATHS+=("${PROJPATHS[$i]}")
                CHANGED_PROJNAMES+=("$projname")
                CHANGED_OUTFILES+=("${OUTFILES[$i]}")
            fi
        fi

        (( i += 1 ))
    done
}

while [ 1 ]; do
    check_changes
    
    if [ -z "$RETRY_PROJPATHS" ]; then
        break
    fi

    echo "Retrying some projects..."
    CHECKPROJPATHS=("${RETRY_PROJPATHS[@]}")
done

if [ ${#CHANGED_PROJNAMES[@]} = 0 ]; then
    echo
    echo "No pending changes"
    echo
    cleanup
    exit 0
fi

echo
echo "Projects with pending changes:"
for cproj in "${CHANGED_PROJNAMES[@]}"; do
    echo " $cproj"
done
echo

cleanup
exit 0


