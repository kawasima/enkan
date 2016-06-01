package enkan.system.devel;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.shared.invoker.CommandLineConfigurationException;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvokerLogger;
import org.apache.maven.shared.invoker.SystemOutLogger;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

public class GradleCommandLineInvoker {

    private static final InvokerLogger DEFAULT_LOGGER = new SystemOutLogger();

    private InvokerLogger logger = DEFAULT_LOGGER;

    private File workingDirectory;

    private File localRepositoryDirectory;

    private File gradleHome;

    private File gradleExecutable;

    private Properties systemEnvVars;

    public Commandline build(final InvocationRequest request)
            throws CommandLineConfigurationException {
        try {
            checkRequiredState();
        } catch (final IOException e) {
            throw new CommandLineConfigurationException(e.getMessage(), e);
        }
        File mvn = null;
        try {
            mvn = findMavenExecutable();
        } catch (final IOException e) {
            throw new CommandLineConfigurationException(e.getMessage(), e);
        }
        final Commandline cli = new Commandline();

        cli.setExecutable(mvn.getAbsolutePath());

        // handling for OS-level envars
        setShellEnvironment(request, cli);

        // interactive, offline, update-snapshots,
        // debug/show-errors, checksum policy
        setFlags(request, cli);

        // failure behavior and [eventually] forced-reactor
        // includes/excludes, etc.
        setReactorBehavior(request, cli);

        // working directory and local repository location
        setEnvironmentPaths(request, cli);

        // pom-file and basedir handling
        setPomLocation(request, cli);

        setSettingsLocation(request, cli);

        setToolchainsLocation(request, cli);

        setProperties(request, cli);

        setProfiles(request, cli);

        setGoals(request, cli);

        setThreads(request, cli);

        return cli;
    }

    protected void checkRequiredState()
            throws IOException {
        if (logger == null) {
            throw new IllegalStateException("A logger instance is required.");
        }

        if ((gradleHome == null) && (System.getProperty("maven.home") == null))
        // can be restored with 1.5
        // && ( System.getenv( "M2_HOME" ) != null ) )
        {
            if (!getSystemEnvVars().containsKey("M2_HOME")) {
                throw new IllegalStateException("Maven application directory was not "
                        + "specified, and ${maven.home} is not provided in the system "
                        + "properties. Please specify at least on of these.");
            }
        }
    }

    protected void setSettingsLocation(final InvocationRequest request, final Commandline cli) {
        File userSettingsFile = request.getUserSettingsFile();

        if (userSettingsFile != null) {
            try {
                final File canSet = userSettingsFile.getCanonicalFile();
                userSettingsFile = canSet;
            } catch (final IOException e) {
                logger.debug("Failed to canonicalize user settings path: " + userSettingsFile.getAbsolutePath()
                        + ". Using as-is.", e);
            }

            cli.createArg().setValue("-s");
            cli.createArg().setValue(userSettingsFile.getPath());
        }

        File globalSettingsFile = request.getGlobalSettingsFile();

        if (globalSettingsFile != null) {
            try {
                final File canSet = globalSettingsFile.getCanonicalFile();
                globalSettingsFile = canSet;
            } catch (final IOException e) {
                logger.debug("Failed to canonicalize global settings path: " + globalSettingsFile.getAbsolutePath()
                        + ". Using as-is.", e);
            }

            cli.createArg().setValue("-gs");
            cli.createArg().setValue(globalSettingsFile.getPath());
        }

    }

    protected void setToolchainsLocation(final InvocationRequest request, final Commandline cli) {
        File toolchainsFile = request.getToolchainsFile();

        if (toolchainsFile != null) {
            try {
                final File canSet = toolchainsFile.getCanonicalFile();
                toolchainsFile = canSet;
            } catch (final IOException e) {
                logger.debug("Failed to canonicalize toolchains path: " + toolchainsFile.getAbsolutePath()
                        + ". Using as-is.", e);
            }

            cli.createArg().setValue("-t");
            cli.createArg().setValue(toolchainsFile.getPath());
        }
    }

    protected void setShellEnvironment(final InvocationRequest request, final Commandline cli)
            throws CommandLineConfigurationException {
        if (request.isShellEnvironmentInherited()) {
            try {
                cli.addSystemEnvironment();
                cli.addEnvironment("MAVEN_TERMINATE_CMD", "on");
                // MSHARED-261: Ensure M2_HOME is not inherited, but gets a
                // proper value
                cli.addEnvironment("M2_HOME", getGradleHome().getAbsolutePath());
            } catch (final IOException e) {
                throw new CommandLineConfigurationException("Error reading shell environment variables. Reason: "
                        + e.getMessage(), e);
            } catch (final Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    final IllegalStateException error = new IllegalStateException(
                            "Unknown error retrieving shell environment variables. Reason: "
                                    + e.getMessage());
                    error.initCause(e);

                    throw error;
                }
            }
        }

        if (request.getJavaHome() != null) {
            cli.addEnvironment("JAVA_HOME", request.getJavaHome().getAbsolutePath());
        }

        if (request.getMavenOpts() != null) {
            cli.addEnvironment("MAVEN_OPTS", request.getMavenOpts());
        }

        for (final Map.Entry<String, String> entry : request.getShellEnvironments().entrySet()) {
            cli.addEnvironment(entry.getKey(), entry.getValue());
        }
    }

    protected void setProfiles(final InvocationRequest request, final Commandline cli) {
        final List<String> profiles = request.getProfiles();

        if ((profiles != null) && !profiles.isEmpty()) {
            cli.createArg().setValue("-P");
            cli.createArg().setValue(StringUtils.join(profiles.iterator(), ","));
        }

    }

    protected void setGoals(final InvocationRequest request, final Commandline cli) {
        final List<String> goals = request.getGoals();

        if ((goals != null) && !goals.isEmpty()) {
            cli.createArg().setLine(StringUtils.join(goals.iterator(), " "));
        }
    }

    protected void setProperties(final InvocationRequest request, final Commandline cli) {
        final Properties properties = request.getProperties();

        if (properties != null) {
            for (final Iterator it = properties.entrySet().iterator(); it.hasNext();) {
                final Map.Entry entry = (Map.Entry) it.next();

                final String key = (String) entry.getKey();
                final String value = (String) entry.getValue();

                cli.createArg().setValue("-D");
                cli.createArg().setValue(key + '=' + value);
            }
        }
    }

    protected void setPomLocation(final InvocationRequest request, final Commandline cli) {
        boolean pomSpecified = false;

        File pom = request.getPomFile();
        final String pomFilename = request.getPomFileName();
        final File baseDirectory = request.getBaseDirectory();

        if (pom != null) {
            pomSpecified = true;
        } else if (baseDirectory != null) {
            if (baseDirectory.isDirectory()) {
                if (pomFilename != null) {
                    pom = new File(baseDirectory, pomFilename);

                    pomSpecified = true;
                } else {
                    pom = new File(baseDirectory, "build.gradle");
                }
            } else {
                logger.warn("Base directory is a file. Using base directory as POM location.");

                pom = baseDirectory;

                pomSpecified = true;
            }
        }

        if (pomSpecified) {
            try {
                final File canPom = pom.getCanonicalFile();
                pom = canPom;
            } catch (final IOException e) {
                logger.debug("Failed to canonicalize the build.gradle path: " + pom + ". Using as-is.", e);
            }

            if (!"build.gradle".equals(pom.getName())) {
                logger.debug("Specified build.gradle file is not named \'build.gradle\'. "
                        + "Using the \'-f\' command-line option to accommodate non-standard filename...");

                cli.createArg().setValue("-f");
                cli.createArg().setValue(pom.getName());
            }
        }
    }

    protected void setEnvironmentPaths(final InvocationRequest request, final Commandline cli) {
        File workingDirectory = request.getBaseDirectory();

        if (workingDirectory == null) {
            final File pomFile = request.getPomFile();
            if (pomFile != null) {
                workingDirectory = pomFile.getParentFile();
            }
        }

        if (workingDirectory == null) {
            workingDirectory = this.workingDirectory;
        }

        if (workingDirectory == null) {
            workingDirectory = new File(System.getProperty("user.dir"));
        } else if (workingDirectory.isFile()) {
            logger.warn("Specified base directory (" + workingDirectory + ") is a file."
                    + " Using its parent directory...");

            workingDirectory = workingDirectory.getParentFile();
        }

        try {
            cli.setWorkingDirectory(workingDirectory.getCanonicalPath());
        } catch (final IOException e) {
            logger.debug("Failed to canonicalize base directory: " + workingDirectory + ". Using as-is.", e);

            cli.setWorkingDirectory(workingDirectory.getAbsolutePath());
        }

        File localRepositoryDirectory = request.getLocalRepositoryDirectory(this.localRepositoryDirectory);

        if (localRepositoryDirectory != null) {
            try {
                final File canLRD = localRepositoryDirectory.getCanonicalFile();
                localRepositoryDirectory = canLRD;
            } catch (final IOException e) {
                logger.debug("Failed to canonicalize local repository directory: " + localRepositoryDirectory
                        + ". Using as-is.", e);
            }

            if (!localRepositoryDirectory.isDirectory()) {
                throw new IllegalArgumentException("Local repository location: \'" + localRepositoryDirectory
                        + "\' is NOT a directory.");
            }

            cli.createArg().setValue("-D");
            cli.createArg().setValue("maven.repo.local=" + localRepositoryDirectory.getPath());
        }
    }

    protected void setReactorBehavior(final InvocationRequest request, final Commandline cli) {
        // NOTE: The default is "fail-fast"
        final String failureBehavior = request.getFailureBehavior();

        if (StringUtils.isNotEmpty(failureBehavior)) {
            if (InvocationRequest.REACTOR_FAIL_AT_END.equals(failureBehavior)) {
                cli.createArg().setValue("-fae");
            } else if (InvocationRequest.REACTOR_FAIL_NEVER.equals(failureBehavior)) {
                cli.createArg().setValue("-fn");
            }
        }

        if (request.isActivatedReactor()) {
            cli.createArg().setValue("-r");
            final String[] includes = request.getActivatedReactorIncludes();
            final String[] excludes = request.getActivatedReactorExcludes();
            if (includes != null) {
                cli.createArg().setValue("-D");
                cli.createArg().setValue("maven.reactor.includes=" + StringUtils.join(includes, ","));
            }
            if (excludes != null) {
                cli.createArg().setValue("-D");
                cli.createArg().setValue("maven.reactor.excludes=" + StringUtils.join(excludes, ","));
            }
        }

        if (StringUtils.isNotEmpty(request.getResumeFrom())) {
            cli.createArg().setValue("-rf");
            cli.createArg().setValue(request.getResumeFrom());
        }

        final List<String> projectList = request.getProjects();
        if (projectList != null) {
            cli.createArg().setValue("-pl");
            cli.createArg().setValue(StringUtils.join(projectList.iterator(), ","));

            if (request.isAlsoMake()) {
                cli.createArg().setValue("-am");
            }

            if (request.isAlsoMakeDependents()) {
                cli.createArg().setValue("-amd");
            }
        }
    }

    protected void setFlags(final InvocationRequest request, final Commandline cli) {

        if (request.isOffline()) {
            cli.createArg().setValue("--offline");
        }

        if (request.isDebug()) {
            cli.createArg().setValue("-d");
        }
        // this is superceded by -X, if it exists.
        else if (request.isShowErrors()) {
            cli.createArg().setValue("-q");
        }

        if (request.isShowVersion()) {
            cli.createArg().setValue("-v");
        }
    }

    protected void setThreads(final InvocationRequest request, final Commandline cli) {
        final String threads = request.getThreads();
        if (StringUtils.isNotEmpty(threads)) {
            cli.createArg().setValue("--parallel-threads");
            cli.createArg().setValue(threads);
        }

    }

    protected File findMavenExecutable()
            throws CommandLineConfigurationException, IOException {
        if (gradleHome == null) {
            final String mavenHomeProperty = System.getProperty("gradle.home");
            if (mavenHomeProperty != null) {
                gradleHome = new File(mavenHomeProperty);
                if (!gradleHome.isDirectory()) {
                    final File binDir = gradleHome.getParentFile();
                    if (binDir != null && "bin".equals(binDir.getName())) {
                        // ah, they specified the mvn
                        // executable instead...
                        gradleHome = binDir.getParentFile();
                    } else {
                        throw new IllegalStateException("${maven.home} is not specified as a directory: \'"
                                + mavenHomeProperty + "\'.");
                    }
                }
            }

            if ((gradleHome == null) && (getSystemEnvVars().getProperty("M2_HOME") != null)) {
                gradleHome = new File(getSystemEnvVars().getProperty("M2_HOME"));
            }
        }

        logger.debug("Using ${maven.home} of: \'" + gradleHome + "\'.");

        if (gradleExecutable == null || !gradleExecutable.isAbsolute()) {
            String executable;
            if (gradleExecutable != null) {
                executable = gradleExecutable.getPath();
            } else if (Os.isFamily("windows")) {
                if (new File(gradleHome, "/bin/gradle.cmd").exists()) {
                    executable = "gradle.cmd";
                } else {
                    executable = "gradle.bat";
                }
            } else {
                executable = "gradle";
            }

            final String interpath = gradleHome.getName().contains("bin") ? "" : "/bin/";
            gradleExecutable = new File(gradleHome, interpath + executable);

            try {
                final File canonicalMvn = gradleExecutable.getCanonicalFile();
                gradleExecutable = canonicalMvn;
            } catch (final IOException e) {
                logger.debug("Failed to canonicalize maven executable: " + gradleExecutable + ". Using as-is.", e);
            }

            if (!gradleExecutable.isFile()) {
                throw new CommandLineConfigurationException("Maven executable not found at: " + gradleExecutable);
            }
        }

        return gradleExecutable;
    }

    /**
     * Wraps a path with quotes to handle paths with spaces. If no spaces are
     * found, the original string is returned.
     *
     * @param path
     *            string to wrap if containing spaces
     * @return quote wrapped string
     * @deprecated Quoting of command line arguments should be left to the
     *             Commandline from plexus-utils.
     */
    @Deprecated
    public String wrapStringWithQuotes(final String path) {
        if (path.indexOf(" ") > -1) {
            return "\"" + path + "\"";
        } else {
            return path;
        }
    }

    private Properties getSystemEnvVars()
            throws IOException {
        if (this.systemEnvVars == null) {
            // with 1.5 replace with System.getenv()
            this.systemEnvVars = CommandLineUtils.getSystemEnvVars();
        }
        return this.systemEnvVars;
    }

    public File getLocalRepositoryDirectory() {
        return localRepositoryDirectory;
    }

    public void setLocalRepositoryDirectory(final File localRepositoryDirectory) {
        this.localRepositoryDirectory = localRepositoryDirectory;
    }

    public InvokerLogger getLogger() {
        return logger;
    }

    public void setLogger(final InvokerLogger logger) {
        this.logger = logger;
    }

    public File getGradleHome() {
        return gradleHome;
    }

    public void setGradleHome(final File gradleHome) {
        this.gradleHome = gradleHome;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(final File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * {@code mavenExecutable} can either be relative to ${maven.home}/bin/ or
     * absolute
     *
     * @param mavenExecutable
     *            the executable
     */
    public void setGradleExecutable(final File mavenExecutable) {
        this.gradleExecutable = mavenExecutable;
    }

    public File getGradleExecutable() {
        return gradleExecutable;
    }

}
