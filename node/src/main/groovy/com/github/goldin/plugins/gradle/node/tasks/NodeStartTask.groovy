package com.github.goldin.plugins.gradle.node.tasks

import org.gradle.api.GradleException

import static com.github.goldin.plugins.gradle.node.NodeConstants.*
import org.gcontracts.annotations.Ensures
import org.gcontracts.annotations.Requires


/**
 * Starts Node.js application.
 */
class NodeStartTask extends NodeBaseTask
{

    @Override
    void taskAction()
    {
        bashExec( startScript(), scriptFile( START_SCRIPT ), true, ext.generateOnly )
        if ( ext.startCheckUrl ){ startCheck() }
    }


    @Ensures({ result })
    private String startScript()
    {
        """
        |${ baseBashScript() }
        |export BUILD_ID=JenkinsLetMeSpawn
        |
        |${ startCommands().join( '\n|' )}""".stripMargin()
    }


    @Requires({ ext.startCommands || ext.scriptPath })
    @Ensures({ result })
    private List<String> startCommands()
    {
        if ( ext.startCommands ) { return ext.startCommands }

        String foreverCommand = ''

        if ( ext.isCoffee )
        {
            file( COFFEE_EXECUTABLE ) // Validates existence
            foreverCommand = "\"$COFFEE_EXECUTABLE\""
        }

        [ "forever start --pidFile \"${ project.name }.pid\" $foreverCommand \"$ext.scriptPath\"" ]
    }


    @Requires({ ext.startCheckUrl })
    private void startCheck()
    {
        delay( ext.startCheckDelay )

        final response     = httpRequest( ext.startCheckUrl, 'GET', [:], 0, 0, null, false )
        final content      = response.content ? new String( response.content, 'UTF-8' ) : ''
        final statusCode   = response.connection.responseCode
        final goodResponse = ( statusCode == ext.startCheckStatusCode ) && ( content.contains( ext.startCheckContent ))
        final message      = "Requesting [$ext.startCheckUrl] resulted in status code [$statusCode]" +
                             ( ext.startCheckContent ? ", content [$content]" : '' )

        if ( goodResponse )
        {
            log{ "$message - good!" }
        }
        else
        {
            throw new GradleException( "$message - expected status code [$ext.startCheckStatusCode]" +
                                       ( ext.startCheckContent ? ", content contains [$ext.startCheckContent]" : '' ))
        }
    }
}
