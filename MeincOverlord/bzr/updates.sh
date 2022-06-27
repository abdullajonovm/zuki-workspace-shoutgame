#!/bin/bash

cd `dirname $0`

for path in `find ../.. -mindepth 2 -maxdepth 2 -type d -name .bzr`; do
    path=`dirname $path`
    CHECKPROJPATHS+=("$path")
done

TMPDIR=/tmp/bzrsh
mkdir -p $TMPDIR

cleanup()  {
    rm -f $TMPDIR/*
}

check_updates() {
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
        echo "Checking $projname"
        bash -c "cd \"$projpath\"; bzr info | grep 'checkout of branch: ' | awk '{print \$4}' | xargs bzr missing >$outfile 2>$errfile" &
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
            if [ $outsize -gt 25 ]; then
                CHANGED_PROJPATHS+=("${PROJPATHS[$i]}")
                CHANGED_PROJNAMES+=("$projname")
                CHANGED_OUTFILES+=("${OUTFILES[$i]}")
            fi
        fi

        (( i += 1 ))
    done
}

while [ 1 ]; do
    check_updates
    
    if [ -z "$RETRY_PROJPATHS" ]; then
        break
    fi

    echo "Retrying some projects..."
    CHECKPROJPATHS=("${RETRY_PROJPATHS[@]}")
done


if [ ${#CHANGED_PROJNAMES[@]} = 0 ]; then
    echo
    echo "No available updates"
    cleanup
    exit 0
fi

SELECTED_PROJECTS=
select_projects() {
    echo
    echo "================================"
    echo "Projects with available updates:"
    i=1
    for cproj in "${CHANGED_PROJNAMES[@]}"; do
        echo " [$i] $cproj"
        (( i += 1 ))
    done
    echo
    echo " [a] All projects"
    echo " [q] Quit"
    echo "================================"
    read -p "Project numbers (space delimited) [q]: " -a SELECTED_PROJECTS
}

SELECTED_ACTION=
select_action() {
    while [ 1 ]; do
        echo
        echo -n "Selected projects: "
        for proj in "${SELECTED_PROJECTS[@]}"; do
            projname="${CHANGED_PROJNAMES[$((proj-1))]}"
            echo -n "$projname, "
        done
        echo
        echo "======================================"
        echo "Available actions:"
        echo " [d] qdiff"
        echo " [l] show available update revison log"
        echo " [u] update"
        echo
        echo " [q] back to projects"
        echo "======================================"

        read -p "Action [q]: " SELECTED_ACTION

        case "$SELECTED_ACTION" in
            "d" | "l" | "u" | "q" | "" )
                return ;;
            *)
                echo
                echo "Invalid action" ;;
        esac
    done
}

while [ 1 ]; do
    select_projects

    if [[ ${#SELECTED_PROJECTS[@]} = 0 || "${SELECTED_PROJECTS[0]}" = "q" ]]; then
        cleanup
        exit 0
    fi

    if [ ${SELECTED_PROJECTS[0]} = "a" ]; then
        SELECTED_PROJECTS=(`jot ${#CHANGED_PROJNAMES[@]}`)
    fi

    while [ 1 ]; do
        select_action

        if [[ $SELECTED_ACTION = "" || $SELECTED_ACTION = "q" ]]; then
            break
        fi

        for proj in "${SELECTED_PROJECTS[@]}"; do
            projname=${CHANGED_PROJNAMES[$((proj-1))]}
            case "$SELECTED_ACTION" in
                "d")
                    echo
                    echo
                    echo "**** Qdiff for $projname:"
                    bzruri=`bzr info ${CHANGED_PROJPATHS[$((proj-1))]} | grep 'checkout of branch: ' | awk '{print $4}'`
                    bash -c "cd ${CHANGED_PROJPATHS[$((proj-1))]}; bzr qdiff --new=$bzruri >/dev/null 2>&1" ;;
                "l")
                    echo
                    echo
                    echo "**** Available update revision log for $projname:"
                    cat "${CHANGED_OUTFILES[$((proj-1))]}" ;;
                "u")
                    echo
                    echo
                    echo "**** Updating $projname:"
                    bash -c "cd ${CHANGED_PROJPATHS[$((proj-1))]}; bzr up" ;;
            esac
        done
    done
done

cleanup
exit 0


