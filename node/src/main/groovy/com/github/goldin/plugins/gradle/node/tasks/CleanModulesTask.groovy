package com.github.goldin.plugins.gradle.node.tasks

import static com.github.goldin.plugins.gradle.node.NodeConstants.*


/**
 * Deletes Node.js local modules.
 */
class CleanModulesTask extends NodeBaseTask
{
    boolean requiresScriptPath(){ false }

    @Override
    void taskAction()
    {
        delete( NODE_MODULES_DIR )
    }
}
