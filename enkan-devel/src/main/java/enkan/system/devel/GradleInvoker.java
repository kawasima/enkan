package enkan.system.devel;

import java.io.File;
import java.io.InputStream;

import org.apache.maven.shared.invoker.CommandLineConfigurationException;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.InvokerLogger;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.SystemOutHandler;
import org.apache.maven.shared.invoker.SystemOutLogger;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

public class GradleInvoker implements Invoker {

    public static final String ROLE_HINT = "default";

    private static final InvokerLogger DEFAULT_LOGGER = new SystemOutLogger();

    private static final InvocationOutputHandler DEFAULT_OUTPUT_HANDLER = new SystemOutHandler();

    private File localRepositoryDirectory;

    private InvokerLogger logger = DEFAULT_LOGGER;

    private File workingDirectory;

    private File gradleHome;

    private File graldeExecutable;

    private InvocationOutputHandler outputHandler = DEFAULT_OUTPUT_HANDLER;

    private InputStream inputStream;

    private InvocationOutputHandler errorHandler = DEFAULT_OUTPUT_HANDLER;

    @Override
    public InvocationResult execute( final InvocationRequest request )
        throws MavenInvocationException
    {
        final GradleCommandLineInvoker cliBuilder = new GradleCommandLineInvoker();

        final InvokerLogger logger = getLogger();
        if ( logger != null )
        {
            cliBuilder.setLogger( getLogger() );
        }

        final File localRepo = getLocalRepositoryDirectory();
        if ( localRepo != null )
        {
            cliBuilder.setLocalRepositoryDirectory( getLocalRepositoryDirectory() );
        }

        final File mavenHome = getMavenHome();
        if ( mavenHome != null )
        {
            cliBuilder.setGradleHome( getMavenHome() );
        }

        final File mavenExecutable = getMavenExecutable();
        if ( mavenExecutable != null )
        {
            cliBuilder.setGradleExecutable( mavenExecutable );
        }


        final File workingDirectory = getWorkingDirectory();
        if ( workingDirectory != null )
        {
            cliBuilder.setWorkingDirectory( getWorkingDirectory() );
        }

        Commandline cli;
        try {
            cli = cliBuilder.build( request );
        } catch ( final CommandLineConfigurationException e ) {
            throw new MavenInvocationException( "Error configuring command-line. Reason: " + e.getMessage(), e );
        }

        final GradleInvocationResult result = new GradleInvocationResult();

        try {
            final int exitCode = executeCommandLine( cli, request );
            result.setExitCode( exitCode );
        } catch ( final CommandLineException e ) {
            result.setExecutionException( e );
        }

        return result;
    }

    private int executeCommandLine( final Commandline cli, final InvocationRequest request )
            throws CommandLineException {
        int result = Integer.MIN_VALUE;

        final InputStream inputStream = request.getInputStream( this.inputStream );
        final InvocationOutputHandler outputHandler = request.getOutputHandler( this.outputHandler );
        final InvocationOutputHandler errorHandler = request.getErrorHandler( this.errorHandler );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing: " + cli );
        }
        if ( request.isInteractive() )
        {
            if ( inputStream == null )
            {
                getLogger().warn(
                                  "Maven will be executed in interactive mode"
                                      + ", but no input stream has been configured for this MavenInvoker instance." );

                result = CommandLineUtils.executeCommandLine( cli, outputHandler, errorHandler );
            }
            else
            {
                result = CommandLineUtils.executeCommandLine( cli, inputStream, outputHandler, errorHandler );
            }
        }
        else
        {
            if ( inputStream != null )
            {
                getLogger().info( "Executing in batch mode. The configured input stream will be ignored." );
            }

            result = CommandLineUtils.executeCommandLine( cli, outputHandler, errorHandler );
        }

        return result;
    }

    @Override
    public File getLocalRepositoryDirectory()
    {
        return localRepositoryDirectory;
    }

    @Override
    public InvokerLogger getLogger()
    {
        return logger;
    }

    @Override
    public Invoker setLocalRepositoryDirectory( final File localRepositoryDirectory )
    {
        this.localRepositoryDirectory = localRepositoryDirectory;
        return this;
    }

    @Override
    public Invoker setLogger( final InvokerLogger logger )
    {
        this.logger = ( logger != null ) ? logger : DEFAULT_LOGGER;
        return this;
    }

    @Override
    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    @Override
    public Invoker setWorkingDirectory( final File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
        return this;
    }

    @Override
    public File getMavenHome()
    {
        return gradleHome;
    }

    @Override
    public Invoker setMavenHome( final File mavenHome ) {
        this.gradleHome = mavenHome;
        return this;
    }

    @Override
    public File getMavenExecutable()
    {
        return graldeExecutable;
    }

    @Override
    public Invoker setMavenExecutable( final File mavenExecutable )
    {
        this.graldeExecutable = mavenExecutable;
        return this;
    }

    @Override
    public Invoker setErrorHandler( final InvocationOutputHandler errorHandler )
    {
        this.errorHandler = errorHandler;
        return this;
    }

    @Override
    public Invoker setInputStream( final InputStream inputStream )
    {
        this.inputStream = inputStream;
        return this;
    }

    @Override
    public Invoker setOutputHandler( final InvocationOutputHandler outputHandler )
    {
        this.outputHandler = outputHandler;
        return this;
    }

}
