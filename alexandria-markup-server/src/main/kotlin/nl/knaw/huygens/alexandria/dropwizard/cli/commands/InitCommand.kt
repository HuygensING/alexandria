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
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class InitCommand : AlexandriaCommand("init", "Initializes current directory as an alexandria workspace.") {
    override fun configure(subparser: Subparser) {}
    @Throws(IOException::class)
    override fun run(bootstrap: Bootstrap<*>?, namespace: Namespace) {
        checkWeAreNotInUserHomeDir()
        val context = CLIContext()
        initPaths(Paths.get("").toAbsolutePath())
        //    context.getWatchedDirectories().add("");
        println("initializing...")
        val alexandriaPath = Paths.get(alexandriaDir)
        println("  mkdir $alexandriaPath")
        if (!File(alexandriaDir).mkdir()) {
            throw AlexandriaCommandException(
                    "init failed: could not create directory $alexandriaPath")
        }
        val transcriptionsPath = Paths.get(workDir, SOURCE_DIR)
        println("  mkdir $transcriptionsPath")
        mkdir(transcriptionsPath)
        context.watchedDirectories.add(SOURCE_DIR)
        val viewsPath = Paths.get(workDir, VIEWS_DIR)
        println("  mkdir $viewsPath")
        mkdir(viewsPath)
        context.watchedDirectories.add(VIEWS_DIR)
        val sparqlPath = Paths.get(workDir, SPARQL_DIR)
        println("  mkdir $sparqlPath")
        mkdir(sparqlPath)
        //    context.getWatchedDirectories().add(SPARQL_DIR);
        storeContext(context)
        println("done!")
    }

    private fun checkWeAreNotInUserHomeDir() {
        val homeDir = System.getProperty("user.home")
        val currentPath = Paths.get("").toAbsolutePath().toString()
        if (homeDir == currentPath) {
            throw AlexandriaCommandException(
                    "You are currently in your home directory, which can't be used as an alexandria directory. Please choose a different directory to initialize.")
        }
    }

    @Throws(IOException::class)
    private fun mkdir(path: Path) {
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
    }
}
