package com.shout.unixwareutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilesContainingText
{
    private static final String CONFIG_DIR = "src/main/resources";

    private Set<String> getFilesContainingText(String searchText, File sourceDirectory)
    {
        Set<String> output = new HashSet<>();
        List<String> fifOutput = executeFindInFile(searchText, sourceDirectory);

        //the format of this output is as follows:
        //    [path][filename].[extension]:[linenumber]:[output]

        //for each line, split on the :, take the first item to get the path/filename
        //then add to the set so that duplicate entries (i.e. if a file contains the match more than once), it gets collapsed
        fifOutput.forEach(l -> {
            String[] parts = l.split(":");
            output.add(parts[0]);
        });

        return output;
    }

    private List<String> executeFindInFile(String searchText, File sourceDirectory)
    {
        String fifScriptPath = Paths.get(CONFIG_DIR).toFile().getAbsolutePath() + "/fif.sh";
        return executeCommand(new String[] {fifScriptPath, searchText}, sourceDirectory);
    }

    private List<String> executeCommand(String[] command, File executionDir)
    {
        List<String> output = new ArrayList<>();

        try {
            Process proc = Runtime.getRuntime().exec(command, null, executionDir);

            ProcessOutputter errorStream = new ProcessOutputter(proc.getErrorStream());
            ProcessOutputter outputStream = new ProcessOutputter(proc.getInputStream());

            errorStream.start();
            outputStream.start();

            proc.waitFor();

            output.addAll(errorStream.getOutput());
            output.addAll(outputStream.getOutput());

        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(MessageFormat.format("unable to execute command: {0}", (Object[])command), e);
        }

        return output;
    }

    public static void main(String[] args)
    {
        FilesContainingText obj = new FilesContainingText();

        //Set<String> files = obj.getFilesContainingText("LD_LIBRARY_PATH", new File("/Users/shawker/dev/xinous/uw7.1.2"));
        Set<String> files = obj.getFilesContainingText("LD_LIBRARY_PATH", new File("/Users/shawker/dev/redhat/redhat-6.8-source"));

        files.forEach(System.out::println);
    }

}

/*
RH 6.8 - LD_LIBRARY_PATH

x ./openmpi-1.8.1/ompi/contrib/vt/vt/vtlib/vt_plugin_cntr.c
x ./openjdk/jdk/src/macosx/bin/jexec.c
x ./openmpi-1.4.3/orte/mca/plm/tm/plm_tm_module.c
./jdk/src/solaris/bin/jexec.c
./hotspot/agent/src/os/linux/libproc_impl.c
./glibc-2.12-2-gc4ccff1/stdio-common/bug5.c
./uClibc-0.9.30.1/ldso/ldso/dl-startup.c
./xmlsec1-1.2.20/examples/xkms-server.c
./apache-tomcat-5.5.28-src/connectors/jk/native/common/jk_global.h
./hotspot/agent/src/os/bsd/libproc_impl.c
./openmpi-1.5.4/orte/mca/plm/tm/plm_tm_module.c
./uClibc-0.9.30.1/include/elf.h
./xmlsec1-1.2.20/examples/sign3.c
./openmpi-1.10.2/orte/mca/plm/rsh/plm_rsh_module.c
./elfutils-0.164/src/ldgeneric.c
./glibc-2.12-2-gc4ccff1/elf/dl-load.c
./elfutils-0.164/src/ld.c
./uClibc-0.9.30.1/include/link.h
./mozilla-esr17/xpcom/build/nsXPCOMPrivate.h
./binutils-2.20.51.0.2/include/elf/mips.h
./xmlsec1-1.2.20/examples/verify4.c
./openmpi-1.5.4/orte/mca/plm/rsh/plm_rsh_module.c
./elfutils-0.164/src/ld.h
./gcc-3.2.3-20040701/gcc/collect2.c
./hal-0.5.14/hald/hald_runner.c
./openmpi-1.5.3/orte/mca/plm/tm/plm_tm_module.c
./openmpi-1.8.1/orte/mca/odls/base/odls_base_default_fns.c
./xmlsec1-1.2.20/apps/xmlsec.c
./elfutils-0.164/libelf/elf.h
./cmake-2.8.12.2/Source/cmUnsetCommand.h
./openmpi-1.5.4/orte/mca/plm/slurm/plm_slurm_module.c
./valgrind-3.8.1/coregrind/m_libcproc.c
./gettext-0.17/gettext-tools/gnulib-lib/csharpexec.c
./glibc-2.12-2-gc4ccff1/stdio-common/test-popen.c
./libhugetlbfs-2.16/hugectl.c
./openmpi-1.5.4/orte/mca/plm/process/plm_process_module.c
./xmlsec1-1.2.20/examples/encrypt2.c
./openmpi-1.5.4/orte/tools/orterun/orterun.c
./gdb-7.2/gdb/solist.h
./glibc-2.5-20061008T1257/sysdeps/generic/unsecvars.h
./gcc-3.4.6-20060404/gcc/collect2.c
./mozilla-esr17/nsprpub/pr/tests/ipv6.c
./jdk/src/solaris/bin/java_md.c
./openmpi-1.5.4/orte/mca/odls/base/odls_base_default_fns.c
./gdb-7.2/gdb/solib.c
./test-suite/gcc-2.95.3/gcc/collect2.c
./cups-1.4.2/scheduler/env.c
./openmpi-1.8.1/orte/mca/plm/rsh/plm_rsh_module.c
./readline-5.2/examples/rl-fgets.c
./glibc-2.5-20061008T1257/elf/dl-load.c
./openjdk/hotspot/src/share/tools/launcher/java.c
./xmlsec1-1.2.20/examples/sign2.c
./openmpi-1.8.1/orte/mca/plm/tm/plm_tm_module.c
./ghostscript-8.70/contrib/gomni.c
./jdk/src/solaris/native/sun/jdga/jdgadevice.h
./openjdk/jdk/src/solaris/demo/jni/Poller/Poller.c
./openjdk/hotspot/src/os/posix/launcher/java_md.c
./openmpi-1.10.2/orte/mca/plm/slurm/plm_slurm_module.c
./corosync-1.4.7/lcr/lcr_ifact.c
./xmlsec1-1.2.20/examples/verify1.c
./openmpi-1.10.2/orte/mca/plm/rsh/plm_rsh_component.c
./openmpi-1.4.3/orte/mca/plm/process/plm_process_module.c
./openmpi-1.10.2/ompi/contrib/vt/vt/vtlib/vt_plugin_cntr.c
./valgrind-3.8.1/perf/tinycc.c
./glibc-2.12-2-gc4ccff1/elf/dl-support.c
./openmpi-1.10.2/orte/tools/orterun/orterun.c
./uClibc-0.9.30.1/utils/ldd.c
./xmlsec1-1.2.20/examples/decrypt1.c
./openmpi-1.5.4/orte/mca/plm/alps/plm_alps_module.c
./mozilla-esr17/other-licenses/android/linker.c
./openjdk/hotspot/agent/src/os/bsd/libproc_impl.c
./gdb-7.2/readline/examples/rl-fgets.c
./glibc-2.5-20061008T1257/stdio-common/bug5.c
./gdb-7.2/gdb/symtab.h
./openmpi-1.10.2/orte/mca/odls/base/odls_base_default_fns.c
./glibc-2.5-20061008T1257/elf/rtld.c
./openmpi-1.5.3/orte/mca/plm/slurm/plm_slurm_module.c
./abrt-2.0.8/src/plugins/abrt-action-install-debuginfo-to-abrt-cache.c
./gimp-2.6.9/libgimpbase/gimpenv.c
./eclipse-3.6.1-src/features/org.eclipse.equinox.executable/library/motif/eclipseMotif.c
./openmpi-1.4.3/orte/mca/plm/ccp/plm_ccp_module.c
./uClibc-0.9.30.1/ldso/libdl/libdl.c
./openmpi-1.10.2/orte/mca/plm/tm/plm_tm_module.c
./hotspot/src/share/tools/launcher/java.c
./openmpi-1.8.1/orte/tools/orterun/orterun.c
./gcc-2.96-20000731/gcc/config/i386/beos-elf.h
./anaconda-13.21.254/loader/selinux.c
./opencryptoki/usr/sbin/pkcsconf/pkcsconf.c
./xmlsec1-1.2.20/examples/encrypt1.c
./openmpi-1.5.4/orte/mca/plm/base/plm_base_rsh_support.c
./glibc-2.5-20061008T1257/elf/link.h
./busybox-1.15.1/libbb/login.c
./glibc-2.12-2-gc4ccff1/elf/elf.h
./anaconda-13.21.254/loader/loader.c
./openmpi-1.10.2/ompi/mca/common/cuda/common_cuda.c
./gcc-2.96-20000731/gcc/collect2.c
./mozilla-esr17/nsprpub/pr/src/linking/prlink.c
./uClibc-0.9.30.1/ldso/ldso/dl-elf.c
./openmpi-1.4.3/orte/mca/plm/slurm/plm_slurm_module.c
./gcc-3.2.3-20040701/gcc/config/i386/beos-elf.h
./openmpi-1.8.1/orte/mca/plm/slurm/plm_slurm_module.c
./openmpi-1.5.3/orte/mca/plm/alps/plm_alps_module.c
./openjdk/jdk/src/solaris/bin/jexec.c
./anaconda-13.21.254/loader/init.c
./hotspot/src/os/posix/launcher/java_md.c
./xmlsec1-1.2.20/examples/sign1.c
./fakechroot-2.9/src/libfakechroot.c
./openjdk/jdk/src/solaris/bin/java_md_solinux.c
./test-suite/sane-backends-1.0.21/backend/dll.c
./glibc-2.5-20061008T1257/elf/dl-support.c
./apr-1.3.9/build/jlibtool.c
./test-suite/sblim-cmpi-base-1.6.1/cmpiOSBase_UnixProcess.c
./xmlsec1-1.2.20/examples/xmldsigverify.c
./xmlsec1-1.2.20/examples/verify2.c
./openmpi-1.8.1/orte/mca/plm/alps/plm_alps_module.c
./httpd-2.2.15/srclib/apr/build/jlibtool.c
./openjdk/jdk/src/solaris/bin/java_md_solinux.h
./openmpi-1.4.3/orte/tools/orterun/orterun.c
./xmlsec1-1.2.20/examples/decrypt3.c
./xmlsec1-1.2.20/examples/decrypt2.c
./jdk/src/solaris/demo/jni/Poller/Poller.c
./gst-plugins-base-0.10.29/ext/gio/gstgio.c
./openmpi-1.5.4/orte/mca/plm/ccp/plm_ccp_module.c
./test-suite/readline-6.0/examples/rl-fgets.c
./gcc-2.96-20000731/gcc/config/i386/beos-pe.h
./openjdk/hotspot/agent/src/os/linux/libproc_impl.c
./openmpi-1.5.3/orte/mca/plm/ccp/plm_ccp_module.c
./glibc-2.12-2-gc4ccff1/stdio-common/xbug.c
./openmpi-1.5.3/orte/mca/plm/process/plm_process_module.c
./ImageMagick-6.7.2-7/magick/magick-config.h
./openjdk/jdk/src/macosx/bin/java_md_macosx.c
./daemon-1.0.1/src/native/unix/native/jsvc-unix.c
./binutils-2.20.51.0.2/include/aout/sun4.h
./SDL-1.2.14/src/loadso/macosx/SDL_dlcompat.c
./eclipse-3.6.1-src/features/org.eclipse.equinox.executable/library/photon/eclipsePhoton.c
./openmpi-1.4.3/orte/mca/plm/alps/plm_alps_module.c
./zsh-4.3.11/Src/options.c
./389-ds-base-1.2.11.15/ldap/servers/slapd/tools/ldclt/ldclt.c
./gcc-3.4.6-20060404/gcc/config/i386/beos-elf.h
./glibc-2.12-2-gc4ccff1/elf/rtld.c
./openmpi-1.4.3/orte/mca/plm/rsh/plm_rsh_module.c
./glibc-2.12-2-gc4ccff1/sysdeps/generic/unsecvars.h
./389-ds-base-1.2.11.15/ldap/servers/slapd/tools/ldclt/ldclt.h
./glibc-2.5-20061008T1257/stdio-common/xbug.c
./openjdk/jdk/src/solaris/native/sun/jdga/jdgadevice.h
./openmpi-1.10.2/orte/mca/plm/alps/plm_alps_module.c
./eclipse-3.6.1-src/features/org.eclipse.equinox.executable/library/eclipseMozilla.c
./openmpi-1.5.3/orte/mca/odls/base/odls_base_default_fns.c
./xmlsec1-1.2.20/examples/verify3.c
./cdrkit-1.1.9/include/glibc_elf.h
./gcc-5.2.1-20150902/liboffloadmic/runtime/offload_host.h
./eclipse-3.6.1-src/features/org.eclipse.equinox.executable/library/eclipseNix.c
./glibc-2.12-2-gc4ccff1/elf/link.h
./openmpi-1.8.1/ompi/mca/common/cuda/common_cuda.c
./gdb-7.2/include/aout/sun4.h
./openmpi-1.4.3/orte/mca/odls/base/odls_base_default_fns.c
./jdk/src/share/bin/java.c
./gdb-7.2/include/elf/mips.h
./uClibc-0.9.30.1/ldso/include/unsecvars.h
./uClibc-0.9.30.1/ldso/ldso/ldso.c
./openmpi-1.5.3/orte/tools/orterun/orterun.c
./glibc-2.5-20061008T1257/elf/elf.h
./openmpi-1.5.3/orte/mca/plm/base/plm_base_rsh_support.c
./openmpi-1.5.3/orte/mca/plm/rsh/plm_rsh_module.c
./xmlsec1-1.2.20/examples/encrypt3.c
./eclipse-3.6.1-src/features/org.eclipse.equinox.executable/library/carbon/eclipseCarbon.c
./glibc-2.5-20061008T1257/stdio-common/test-popen.c
*/