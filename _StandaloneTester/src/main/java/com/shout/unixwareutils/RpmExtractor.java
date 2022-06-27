package com.shout.unixwareutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RpmExtractor
implements FileHandler
{
    private static final String FILENAME_ALREADY_PROCESSED_FILES_PREFIX = "AlreadyProcessedRpmFiles_";
    private static final String CONFIG_DIR = "src/main/resources";

    private static final Set<String> KNOWN_FILETYPES = new HashSet<>(Arrays.asList(
        "patch", "README", "spec", "logrotate", "conf", "sig", "initscript", "sysconf",
        "sh", "h", "c", "py", "java", "d", "xpi", "perl", "php", "ru", "js", "rb", "yml", "python", "tcl", "lua",
        "1", "2", "3", "4", "5", "8",
        "init", "abrt1_to_abrt2", "sysconfig", "aic94xx", "quickstart", "alsaunmute", "rules", "alsa-delay", "filter", "ppd", "convs", "png", "backend", "pstopdf", "pstoraster", "cups-lpd",
        "README-rpm", "crontab", "amanda-xinetd", "amandahosts", "disklist", "pom", "el", "antlr-script", "xml", "gcj", "xinetd", "pam", "magic", "cvt_cyrusdb_all", "cron-daily", "autosievefolder",
        "MF", "56atd", "pl", "RHEL", "sort-getfattr-output", "diff", "Fedora", "openfwwf", "desktop", "console_apps", "pam-config", "x-conf", "texi", "56dhclient", "dhclient-script", "10-dhclient",
        "bash41-017", "dot-bash_profile", "bash41-016", "bash41-002", "dot-bash_logout", "bash41-001", "dot-bashrc", "cat", "redhat", "Makefile", "db2html", "dsl", "dovecot", "manpatch", "html",
        "script", "xsl", "LICENSE", "schema", "txt", "sdb_pgsql", "caching-nameserver", "in", "portreserve", "NetworkManager", "libhover", "properties", "st", "esc", "hispavoces", "scm", "asc",
        "sample", "sed", "odg", "odt", "ods", "modules", "config", "trust-fixes", "ca-legacy", "etc", "usr", "src", "openssl", "lang-exceptions", "finger-xinetd", "so", "man", "update-gdk-pixbuf-loaders",
        "pem", "extr", "update-ca-trust", "ca-certificates", "keys", "nm-dispatcher", "dhclient", "zh_TW", "zh_CN", "cmake", "freeradius-logrotate", "freeradius-pam-conf", "freeradius-radiusd-init",
        "readline52-003", "readline52-014", "readline52-002", "readline52-013", "readline52-005", "readline52-004", "gdm-autologin-pam", "gdm-pam", "ggz", "cidfmap", "CIDFnmap", "httpd", "update-gio-modules",
        "readline52-010", "readline52-001", "readline52-011", "readline52-007", "readline52-006", "readline52-009", "readline52-008", "entries", "m4", "schemas", "menu", "ui", "libgnutls-config",
        "compiz-gtk", "custom-LINGUAS", "apply-extra-translations", "extra-translations", "po", "policy", "setregdomain", "cs", "nroff", "gstreamer", "prov", "gthumb-importer", "update-gtk-immodules",
        "256color", "coreutils-DIR_COLORS", "lightbgcolor", "csh", "pamd", "crontab1", "run-parts", "cron", "copying", "COPYING", "gutenprint-foomaticppdupdate", "fdi", "rc", "cfg", "confd", "USER-GUIDE",
        "ispellaff2myspell", "wordlist2hunspell", "oxt", "pdf", "dic", "aff", "myspell-header", "hunspell-header", "hypervkvpd", "hypervfcopyd", "hypervvssd", "xinput-ibus", "icu-config", "iptables-config",
        "iptraf", "initd", "04-iscsi", "cnf", "pre-2009-spec-changelog", "sub", "guess", "jpackage-utils-README", "k3brc", "kde4", "teamnames", "applnk-hidden-directory", "subdirs-kde-i18n", "subdirs-kde-l10n",
        "protocol", "console", "config-i686-nodebug", "config-x86_64-debug-rhel", "kabi_ppc64", "genkey", "kabi_greylist_ppc64", "config-debug-rhel", "config-i686", "check-kabi", "config-i686-debug",
        "config-s390x-kdump-rhel", "kabi_greylist_i686", "config-framepointer", "config-i686-nodebug-rhel", "config-powerpc64-debug-rhel", "config-powerpc64-rhel", "config-s390x-debug", "config-nodebug-rhel",
        "kabi_greylist_x86_64", "common", "kabi_x86_64", "config-s390x-rhel", "config-s390x-kdump", "config-x86_64-nodebug-rhel", "kabi_greylist_s390x", "config-i686-debug-rhel", "config-powerpc-generic",
        "config-x86_64-generic-rhel", "config-x86_64-generic", "perf", "config-x86-generic-rhel", "config-x86-generic", "config-ia64-generic-rhel", "config-powerpc-generic-rhel", "kabitool", "config-generic-rhel",
        "config-powerpc64", "config-s390x-debug-rhel", "config-x86_64-debug", "kabi_i686", "config-nodebug", "config-i686-rhel", "config-debug", "config-powerpc64-debug,", "config-generic", "kabi_s390x",
        "config-s390x", "find-provides", "perf-archive", "config-s390x-generic-rhel", "config-x86_64-nodebug", "pub", "config-powerpc64-kdump-rhel", "config-powerpc64-kdump", "config-powerpc64-debug",
        "fadump-functions", "x86_64", "ia64", "mkdumprd", "i386", "s390x", "ppc64", "service", "acl", "krlogin", "krsh", "ChangeLog", "rhs", "dotkshrc", "LD_PRELOAD", "blas", "ps", "lapack", "rhel", "theme",
        "req", "hml", "RedHat", "bin", "md5", "com", "hobble-libgcrypt", "theora_player", "ver", "sensors-detect", "catalog", "path", "ttf", "mf", "lshw-gui", "mim", "mailman-migrate-fhs",
        "mailman-crontab-edit", "mailman-update-cfg", "usbboot", "lang", "crondaily", "Man_Page_Copyright", "mdadm-cron", "mdadm-raid-check-sysconfig", "raid-check", "sysv", "new-memtest-pkg",
        "memtest-setup", "vgetty", "mgetty", "sendfax", "vm", "dat", "dotmkshrc", "ASL", "conf-httpd", "weak-modules", "sign", "macros", "consolehelper", "mutt_ldap_query", "mvapich", "mvapich-psm",
        "mvapich2-psm", "mvapich2", "mysql-license", "mysql-docs", "pe", "nanorc", "Z", "net-snmp-config", "make", "netlabel", "consoleapps", "cert", "db", "nss-softokn-dracut-install", "db-getent-Makefile",
        "step-tickers", "cryptopw", "opencv-samples-Makefile", "evolution", "xmbind", "pam_ssh_agent-rmheaders", "renew-dummy-cert", "FIPS", "hobble-openssl", "certificate", "make-dummy-cert", "x509", "TLS",
        "ini", "pear", "LICENSE-XML_RPC", "xml2changelog", "blacklist-visor", "perms", "pinentry-wrapper", "plymouth-update-initrd", "boot-duration", "plymouth", "pm-utils-99hd-apm-restore", "d-postfix",
        "postgresql-bashprofile", "regress", "rpm-dist", "hh", "procmailrc", "FAQ", "psutils-remove-copyrighted-files", "pa-for-gdm", "pynche", "egg-info", "modulator", "stp", "fb4", "fedora",
        "twisted-dropin-cache", "ksmtuned", "sasldb", "gfs2_double", "gfs2_re", "gfs2_Don_t_flag_GFS1_non", "USAGE", "ipvsadm-config", "virt-inspector", "rng", "gem", "nvsetenv", "svg", "dtd", "cxgb4-sysmodprobe",
        "awk", "ifdown-ib", "install", "installkernel", "mlx4-sysmodprobe", "check", "cxgb3-sysmodprobe", "ifup-ib", "mlx4-usermodprobe", "readline60-003", "readline60-004", "readline60-001", "readline60-002",
        "dot-tcshrc", "dot-cshrc", "pppoe-setup", "pppoe-start", "pppoe-status", "pppoe-connect", "pppoe-sto", "pppoe-stop", "old", "rlogin-xinetd", "rexec-xinetd", "rsh-xinetd", "log", "cw_init", "device_cio_free",
        "udev", "ccw_init", "default", "smbprint", "downgrade", "dc", "prepend", "scl_source", "scl-filesystem", "24", "tgtd", "users-targeted", "users-olpc", "securetty_types-minimum", "customizable_types",
        "securetty_types-olpc", "users-mls", "securetty_types-mls", "securetty_types-targeted", "devel", "users-minimum", "policygentool", "sendmail-etc-mail-local-host-names", "sendmail-etc-mail-virtusertable",
        "sendmail-etc-mail-trusted-users", "mc", "readme", "sendmail-etc-mail-mailertable", "sendmail-etc-mail-access", "etc-mail-make", "etc-mail-Makefile", "sendmail-etc-mail-domaintable", "57", "soc", "dcl",
        "defs", "useradd", "list", "sip", "sfd", "force-sysconfig", "cf", "cronscript", "pm", "spice-xpi-client-spicec", "nm", "Certificate-Creation", "6p3-sudoers", "t1libconfig", "talk-xinetd", "ntalk-xinetd",
        "telnet-xinetd", "wmconfig", "example", "info-dir", "ls-R", "generic", "texlive-errata-readme", "daily", "Security", "SSL", "genOpenPegasusSSLCerts", "wrapper", "start_udev", "anchor", "unbound_munin_",
        "munin", "key", "tmpfs", "vim", "spec-template", "rhpatched", "patches", "new", "rpm", "vimrc", "pod", "exe", "ftpusers", "user_list", "w3mconfig", "ipmi", "AUTHORS", "xinf", "radeon", "mkxinf", "commitid",
        "git", "release", "gitignore", "mkxauth", "Xsetup_0", "xinitrc", "Xclients", "Xresources", "Xsession", "xinitrc-common", "Xmodmap", "xulrunner-mozconfig", "find-external-requires", "forth", "dotzshrc",

        "dif", "keyring", "cgred", "License", "preamble", "vmtoolsd", "vmware-user-autostart-wrapper", "SUSE", "test", "postgresql-rpmlintrc", "attributes", "mkinitrd_setup_dummy", "dracut-installkernel",
        "purge-kernels", "dracut-rpmlintrc", "ruby-find-versioned", "erb", "rpm-macros", "gemrc", "attr", "gem_build_cleanup", "libgcj-gcc6-rpmlintrc", "change_spec", "gcc6-rpmlintrc", "libffi-gcc6-rpmlintrc",
        "packagers", "glib2-branding-SLE-COPYING", "ceph-rpmlintrc", "suse", "cmexsong", "cmexkai", "rtslib-add-qla2xxx_wwn-wwn-type", "xlock-wrapper_xorg6", "xlock-wrapper", "Mesa-rpmlintrc", "updates",
        "automake-rpmlintrc", "gif", "yudit", "yast2-rpmlintrc", "logwatch-rpmlintrc", "changelog", "symset-table", "rpmsort", "rpm-suse_macros", "rpmconfigcheck", "services-rpm", "check_mail_queue",
        "rpmlintrc", "qt4", "changes", "ts", "pythonstart", "sysctl", "tgt",  "tgt-fix-libs-for-current-systemd-linker-name", "tgt-systemd-notification-support", "tgt-missing-module-directory-not-an-error",
        "tgt-git-update", "tgt-fix-build", "services", "tgt-handle-access-of-a-target-that-has-been-removed", "supported", "3-rpmlintrc", "getpatches", "system-config-printer-rpmlintrc", "libvirt-cim-rpmlintrc",
        "perl-rpmlintrc", "selinux-ready", "pth", "template", "ids", "zsh", "glib2", "tex", "license", "firewall", "socket", "fw", "meta", "jpg", "linux", "raw", "xpm", "ogg", "SuSE",

        "bz2", "zip", "gz", "tgz", "tbz", "lzma", "xz", "tbz2", "tar", "jar"
    ));

    private static final Set<String> SKIPPED_FOR_NOW_FILES = new HashSet<>(Arrays.asList(
        "cyrus-imapd-2.3.16-13.el6_6.src.rpm"   //embedded rpm (cyrus = imap [email])
        ,"firefox-45.0.1-1.el6.src.rpm"         //embedded rpm (firefox = browser)
        ,"mesa-11.0.7-4.el6.src.rpm"            //embedded rpm (mesa = 3d graphics library)
        ,"mesa-private-llvm-3.6.2-1.el6.src.rpm"
        ,"perl-Mozilla-LDAP-1.5.3-4.el6.src.rpm"
    ));

    File getAlreadyProcessedFilesDataFile(String sourceDir)
    {
        String filename = FILENAME_ALREADY_PROCESSED_FILES_PREFIX + sourceDir + ".dat";
        File alreadyProcessedFilesDataFile = new File(CONFIG_DIR, filename);
        return alreadyProcessedFilesDataFile;
    }

    @SuppressWarnings("unchecked")
    private List<String> getAlreadyProcessedRpmFiles(String sourceDir)
    {
        //get the list of RPM files that have already been processed
        List<String> alreadyProcessedRpmFiles = new ArrayList<>();
        File alreadyProcessedFilesDataFile = getAlreadyProcessedFilesDataFile(sourceDir);
        if (alreadyProcessedFilesDataFile.exists()) {
            try {
                alreadyProcessedRpmFiles = (ArrayList<String>) readFromFile(alreadyProcessedFilesDataFile);
            } catch (ClassNotFoundException | IOException e) {
                throw new IllegalStateException("unable to read file: " + alreadyProcessedFilesDataFile.getName(), e);
            }
        }

        return alreadyProcessedRpmFiles;
    }

    private List<File> getUnprocessedRpmFiles(String sourceDir, List<String> alreadyProcessedRpmFiles)
    {
        for (int i=0; i<1000; i++) {
            if (i < 10) {
                KNOWN_FILETYPES.add("00" + i);
            } else if (i<100) {
                KNOWN_FILETYPES.add("0" + i);
            } else {
                KNOWN_FILETYPES.add(i+"");
            }
        }
        //get the list of RPM files
        List<File> rpmFiles;
        try {
            rpmFiles = Files.list(Paths.get(sourceDir))
                .filter(path -> path.toString().endsWith(".rpm"))
                .map(path -> {
                    try {
                        return path.toRealPath(LinkOption.NOFOLLOW_LINKS).toFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("unable to read rpm file list", e);
        }

        //filter out files that have already been processed
        List<File> unprocessedRpmFiles = new ArrayList<>();
        rpmFiles.forEach(rpmFile -> {
//            if (rpmFile.getName().equals("acl-2.2.49-6.el6.src.rpm")) {
//                unprocessedRpmFiles.add(rpmFile);
//            }

            if (SKIPPED_FOR_NOW_FILES.contains(rpmFile.getName())) {
                ;//no-op for now
            } else if (!alreadyProcessedRpmFiles.contains(rpmFile.getAbsolutePath())) {
                unprocessedRpmFiles.add(rpmFile);
            }
        });

        System.out.println(">>> TOTAL LEFT: " + unprocessedRpmFiles.size() + " <<<");

        //processing too many at once caused a white screen crash on my mac after a while
//        if (unprocessedRpmFiles.size() > 50) {
//            return unprocessedRpmFiles.subList(0, 50);
//        } else {
            return unprocessedRpmFiles;
//        }

//        return unprocessedRpmFiles.subList(0, 1); //just return 1 for initial testing
    }

    public static String getConsoleInput(String message)
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        try {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean processRpmFile(String sourceDir, String outputDir, File rpmFile, List<String> alreadyProcessedRpmFiles, int fileNof, int totalFileCount, String endOfDirNameMarker)
    {
        System.out.println(MessageFormat.format("\nabout to extract: {0} [T-{1}]", rpmFile.getName(), totalFileCount-fileNof));

        //don't extract, but get all of the file extensions in the rpm file to see what needs to be extracted
        Set<String> filenames = new HashSet<>();
        filenames.addAll(executeCommand(new String[] {"tar", "-tf", rpmFile.getAbsolutePath()}, true));

//System.out.println(MessageFormat.format("\narchive contains files of the following files: {0}", filenames));

        //determine if there are any files in the archive which do not have a path
        boolean anyWithoutPath = false;
        for (String filename : filenames) {
            if (filename.indexOf("/") == -1) {
                anyWithoutPath = true;
                break;
            }
        }
//System.out.println("any files without a path? " + anyWithoutPath);

        //get the extensions (ignore files with a dash or underscore)
        Set<String> fileExtentions = filenames.stream()
            .map(fn -> {
                String[] parts = fn.split("\\.");
                return parts[parts.length-1];
            })
            .filter(ext -> !(KNOWN_FILETYPES.contains(ext)))
            .filter(ext -> !ext.contains("_") && !ext.contains("-"))
            .collect(Collectors.toSet());


        //ask for user confirmation before proceeding (only if there are unknown filetypes)
        if (fileExtentions.size() > 0) {
            System.out.println(MessageFormat.format("\narchive contains files of the following unknown types: {0}", fileExtentions));
            String userInput = getConsoleInput("> proceed? ");
            if (!"y".equals(userInput)) {
                return false;
            }
        }

        //determine the dir name to use if one isn't used inside the archive
        int idxOfEndOfDirNameMarker = rpmFile.getName().indexOf(endOfDirNameMarker);
        if (idxOfEndOfDirNameMarker == -1) {
            idxOfEndOfDirNameMarker = rpmFile.getName().length()-1;
        }
        String dirName = rpmFile.getName().substring(0, idxOfEndOfDirNameMarker-1);
//System.out.println("DIR NAME: " + dirName);


        String modifiedOutputDir = anyWithoutPath ? outputDir + "/" + dirName : outputDir;
        //System.out.println("final output dir: " + modifiedOutputDir);
        File modOutputDir = new File(modifiedOutputDir);
        if (!modOutputDir.exists()) {
            modOutputDir.mkdirs();
        }

        //untar the rpm file, but only extract files of known archive types
        executeCommand(new String[] {"tar", "-xvzf", rpmFile.getAbsolutePath(), "-C", outputDir, "*.bz2"}, false);
        executeCommand(new String[] {"tar", "-xvzf", rpmFile.getAbsolutePath(), "-C", outputDir, "*.tar.gz"}, false);
        executeCommand(new String[] {"tar", "-xvzf", rpmFile.getAbsolutePath(), "-C", outputDir, "*.tgz"}, false);
        executeCommand(new String[] {"tar", "-xvzf", rpmFile.getAbsolutePath(), "-C", outputDir, "*.zip"}, false);
        executeCommand(new String[] {"tar", "-xvzf", rpmFile.getAbsolutePath(), "-C", outputDir, "*.tbz"}, false);
        executeCommand(new String[] {"tar", "-xvzf", rpmFile.getAbsolutePath(), "-C", outputDir, "*.lzma"}, false);
        executeCommand(new String[] {"tar", "-xvzf", rpmFile.getAbsolutePath(), "-C", outputDir, "*.xz"}, false);
        executeCommand(new String[] {"tar", "-xvzf", rpmFile.getAbsolutePath(), "-C", outputDir, "*.tbz2"}, false);
        executeCommand(new String[] {"tar", "-xvzf", rpmFile.getAbsolutePath(), "-C", outputDir, "*.tar"}, false);
        executeCommand(new String[] {"tar", "-xvzf", rpmFile.getAbsolutePath(), "-C", outputDir, "*.jar"}, false);

        //delete any "." files
        List<File> dotFiles;
        try {
            dotFiles = Files.list(Paths.get(outputDir))
                .filter(path -> path.getFileName().toString().startsWith("."))
                .map(path -> {
                    try {
                        return path.toRealPath(LinkOption.NOFOLLOW_LINKS).toFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("unable to read dot file list", e);
        }

        dotFiles.forEach(f -> {
            f.delete();
        });

        //untar each of the archive files
        List<File> tarFiles;
        try {
            tarFiles = Files.list(Paths.get(outputDir))
                .filter(path ->
                    path.getFileName().toString().endsWith(".bz2") ||
                    path.getFileName().toString().endsWith(".tar.gz") ||
                    path.getFileName().toString().endsWith(".tgz") ||
                    path.getFileName().toString().endsWith(".zip") ||
                    path.getFileName().toString().endsWith(".tbz") ||
                    path.getFileName().toString().endsWith(".lzma") ||
                    path.getFileName().toString().endsWith(".xz") ||
                    path.getFileName().toString().endsWith(".tbz2") ||
                    path.getFileName().toString().endsWith(".tar") ||
                    path.getFileName().toString().endsWith(".jar")
                )
                .map(path -> {
                    try {
                        return path.toRealPath(LinkOption.NOFOLLOW_LINKS).toFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("unable to read archive file list", e);
        }

        tarFiles.forEach(f -> {
            String dir;
            if (f.getName().endsWith(".zip") ||
                f.getName().endsWith(".jar")
            ) {
                dir = modifiedOutputDir;
            } else {
                dir = outputDir;
            }
            executeCommand(new String[] {"tar", "-xvzf", f.getAbsolutePath(), "-C", dir}, false);
        });

        //delete each of the tar files
        tarFiles.forEach(f -> {
            f.delete();
        });

        //mark this rpm file as having been processed
        alreadyProcessedRpmFiles.add(rpmFile.getAbsolutePath());
        updateProcessedFileList(sourceDir, alreadyProcessedRpmFiles);

        return true;
    }

    private void updateProcessedFileList(String sourceDir, List<String> alreadyProcessedRpmFiles)
    {
        //mark this rpm file as having been processed
        File alreadyProcessedFilesDataFile = getAlreadyProcessedFilesDataFile(sourceDir);
        try {
            writeToFile(alreadyProcessedFilesDataFile, alreadyProcessedRpmFiles);
        } catch (IOException e) {
            throw new IllegalStateException("unable to write already processed file", e);
        }

    }

    private Set<String> executeCommand(String[] command, boolean isTarExtraction)
    {
        Set<String> fileExtensions = new HashSet<>();

        try {
            Process proc = Runtime.getRuntime().exec(command);

            ProcessOutputterForFileExtensions errorStream = new ProcessOutputterForFileExtensions(proc.getErrorStream(), System.err, isTarExtraction);
            ProcessOutputterForFileExtensions outputStream = new ProcessOutputterForFileExtensions(proc.getInputStream(), System.out, isTarExtraction);

            errorStream.start();
            outputStream.start();

            proc.waitFor();

            fileExtensions.addAll(errorStream.getFilenamesFromTarExtraction());
            fileExtensions.addAll(outputStream.getFilenamesFromTarExtraction());

        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(MessageFormat.format("unable to execute command: {0}", (Object[])command), e);
        }

        return fileExtensions;
    }

    public static void main(String[] args)
    {
        //String sourceDir = "/Volumes/RHEL-6.8 Server.";
        //String outputDir = "/Volumes/rhel-src/redhat-6.8-source";
        //String outputDir = "/Users/shawker/temp/z2";
        //String endOfDirNameMarker = "el6";

        String sourceDir = "/Users/shawker/dev/suse/rpms";
        String outputDir = "/Users/shawker/dev/suse/SLE-12-SP2-Server";
        String endOfDirNameMarker = "src.rpm";

        RpmExtractor rpmExtractor = new RpmExtractor();

        List<String> alreadyProcessedRpmFiles = rpmExtractor.getAlreadyProcessedRpmFiles(sourceDir);
        List<File> unprocessedRpmFiles = rpmExtractor.getUnprocessedRpmFiles(sourceDir, alreadyProcessedRpmFiles);

        //for (File rpm : unprocessedRpmFiles) {
        for (int i=0; i<unprocessedRpmFiles.size(); i++) {
            File rpm = unprocessedRpmFiles.get(i);
            boolean shouldContinue = rpmExtractor.processRpmFile(sourceDir, outputDir, rpm, alreadyProcessedRpmFiles, i, unprocessedRpmFiles.size(), endOfDirNameMarker);
            if (!shouldContinue) break;
        }
    }

    public static void main2(String[] args)
    {
        String sourceDir = "/Volumes/RHEL-6.8 Server.";

        RpmExtractor rpmExtractor = new RpmExtractor();
        List<String> alreadyProcessedRpmFiles = rpmExtractor.getAlreadyProcessedRpmFiles(sourceDir);
        System.out.println(MessageFormat.format("{0}", alreadyProcessedRpmFiles));

        alreadyProcessedRpmFiles.remove("/Volumes/RHEL-6.8 Server./DeviceKit-power-014-3.el6.src.rpm");
        rpmExtractor.updateProcessedFileList(sourceDir, alreadyProcessedRpmFiles);
    }

    /*

to recursively search all files in a directory:
    grep -r "search string" *

to recursively search some files in a directory:
    find . -type f -name 'filename_criteria' -exec grep "search string" /dev/null {};

     */
}
