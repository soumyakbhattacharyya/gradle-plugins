package com.github.goldin.plugins.gradle.common

import com.github.goldin.plugins.gradle.common.helpers.*
import org.gcontracts.annotations.Ensures
import org.gcontracts.annotations.Requires
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.text.SimpleDateFormat


/**
 * Base helper task class to be extended by other tasks
 */
abstract class BaseTask<T> extends DefaultTask
{
    final SimpleDateFormat dateFormatter      = new SimpleDateFormat( 'dd MMM, EEEE, yyyy, HH:mm:ss (zzzzzz:\'GMT\'ZZZZZZ)', Locale.ENGLISH )
    final long             startTime          = System.currentTimeMillis()
    final String           startTimeFormatted = this.dateFormatter.format( new Date( this.startTime ))
    final String           osName             = System.getProperty( 'os.name', 'unknown' ).toLowerCase()
    final boolean          isWindows          = osName.contains( 'windows' )
    final boolean          isLinux            = osName.contains( 'linux'   )
    final boolean          isMac              = osName.contains( 'mac os'  )
    final String           projectName        = project.name.replaceAll( ~/^.*\//, '' )
    final File             projectDir         = project.projectDir

    @Delegate GeneralHelper generalHelper
    @Delegate IOHelper      ioHelper
    @Delegate JsonHelper    jsonHelper
    @Delegate MatcherHelper matcherHelper

    /**
     * Retrieves task's extension type in run-time
     */
    @Ensures ({ result })
    abstract Class extensionType()

    /**
     * Extension instance and its name are set by {@link BasePlugin#addTask}
     */
    String extensionName
    T      ext

    /**
     * Configuration closure allowing to configure and use task-named extension when task is called.
     * This allows to
     * 1) Configure the task lazily so if any computations are involved they're not executed unless the task is called.
     * 2) Use several tasks named differently each one configuring its own extension.
     */
    Closure config
    @Requires({ c })
    void config( Closure c ){ this.config = c }

    @Requires({ this.ext && description })
    abstract void verifyUpdateExtension ( String description )

    @Requires({ ext && extensionName })
    abstract void taskAction()


    Map<String,?> helperInitMap (){[ task : this, ext : ext, project : project ]}


    @TaskAction
    final void doTask()
    {
        assert project.name && this.name

        if ( this.config )
        {
            this.extensionName = this.name
            this.ext           = project.extensions.create( this.extensionName, extensionType())
            this.config( this.ext )
        }

        assert this.ext && this.extensionName

        generalHelper = new GeneralHelper( helperInitMap())
        ioHelper      = new IOHelper     ( helperInitMap())
        jsonHelper    = new JsonHelper   ( helperInitMap())
        matcherHelper = new MatcherHelper( helperInitMap())

        verifyUpdateExtension( "$project => ${ this.extensionName } { .. }" )
        taskAction()
    }
}
