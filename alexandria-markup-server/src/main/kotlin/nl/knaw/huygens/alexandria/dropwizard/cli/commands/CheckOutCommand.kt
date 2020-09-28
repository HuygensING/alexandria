package nl.knaw.huygens.alexandria.dropwizard.cli.commands

/*
* #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
*/

import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import nl.knaw.huygens.alexandria.dropwizard.cli.AlexandriaCommandException
import java.io.IOException

class CheckOutCommand : AlexandriaCommand("checkout", "Activate or deactivate a view in this directory.") {
    override fun configure(subparser: Subparser) {
        subparser
                .addArgument("view")
                .metavar("<view>")
                .dest(VIEW)
                .type(String::class.java)
                .required(true)
                .help("The name of the view to use")
    }

    @Throws(IOException::class)
    override fun run(bootstrap: Bootstrap<*>?, namespace: Namespace) {
        checkAlexandriaIsInitialized()
        checkDirectoryHasNoUnCommittedChanges()
        val viewName = namespace.getString(VIEW)
        checkoutView(viewName)
        println("done!")
    }

    @Throws(IOException::class)
    private fun checkDirectoryHasNoUnCommittedChanges() {
        val fileStatus = readWorkDirStatus(readContext())
        val changedFiles = fileStatus[FileStatus.CHANGED]
        val deletedFiles = fileStatus[FileStatus.DELETED]
        if (!(changedFiles.isEmpty() && deletedFiles.isEmpty())) {
            showChanges(fileStatus)
            val message = "Uncommitted changes found, cannot checkout another view."
            throw AlexandriaCommandException(message)
        }
    }

    companion object {
        private const val VIEW = "view"
        private const val DOCUMENT = "document"
    }
}
