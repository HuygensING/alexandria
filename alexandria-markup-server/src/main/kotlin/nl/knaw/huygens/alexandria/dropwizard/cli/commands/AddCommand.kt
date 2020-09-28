package nl.knaw.huygens.alexandria.dropwizard.cli.commands

import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import java.io.IOException
import java.nio.file.Files

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
*/   class AddCommand : AlexandriaCommand("add", "Add file context to the index.") {
    override fun configure(subparser: Subparser) {
        subparser
                .addArgument(ARG_FILE)
                .metavar("<file>")
                .dest(FILE)
                .type(String::class.java)
                .nargs("+")
                .required(true)
                .help("the files to watch")
    }

    override fun run(bootstrap: Bootstrap<*>?, namespace: Namespace) {
        checkAlexandriaIsInitialized()
        val files = relativeFilePaths(namespace)
        val cliContext = readContext()
        val watchedFiles = cliContext.watchedFiles
        for (file in files) {
            val filePath = workFilePath(file)
            if (filePath.toFile().exists()) {
                try {
                    if (Files.isRegularFile(filePath)) {
                        val fileInfo = makeFileInfo(filePath)
                        watchedFiles[file] = fileInfo
                    } else if (Files.isDirectory(filePath)) {
                        cliContext.watchedDirectories.add(file)
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            } else {
                System.err.printf("%s is not a file!%n", file)
            }
        }
        storeContext(cliContext)
        println()
    }
}
