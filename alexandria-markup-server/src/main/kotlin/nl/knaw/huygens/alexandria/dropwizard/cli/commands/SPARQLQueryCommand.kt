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

import com.google.common.base.Charsets
import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import nl.knaw.huc.di.tag.sparql.SPARQLQueryHandler
import org.apache.commons.io.FileUtils
import java.io.IOException
import java.util.stream.Collectors

class SPARQLQueryCommand : AlexandriaCommand("query", "Query the document using SPARQL.") {
    override fun configure(subparser: Subparser) {
        subparser
                .addArgument("DOCUMENT")
                .metavar("<document>")
                .dest(DOCUMENT)
                .type(String::class.java)
                .required(true)
                .help("The name of the document to query.")
        subparser
                .addArgument("-q", "--query")
                .metavar("<sparql-file>")
                .dest(QUERY)
                .type(String::class.java)
                .required(true)
                .help("The file containing the SPARQL query.")
    }

    override fun run(bootstrap: Bootstrap<*>, namespace: Namespace) {
        checkAlexandriaIsInitialized()
        val docName = namespace.getString(DOCUMENT)
        val sparqlFile = namespace.getString(QUERY)
        val filePath = workFilePath(sparqlFile)
        val file = filePath.toFile()
        if (file.isFile) {
            try {
                val sparqlQuery = FileUtils.readFileToString(file, Charsets.UTF_8)
                val docId = getIdForExistingDocument(docName)
                tagStore.use { store ->
                    store.runInTransaction {
                        System.out.printf("document: %s%n%n", docName)
                        System.out.printf("query:%n  %s%n%n", sparqlQuery.replace("\\n".toRegex(), "\n  "))
                        val document = store.getDocument(docId)
                        val h = SPARQLQueryHandler(document)
                        val result = h.execute(sparqlQuery)
                        System.out.printf(
                                "result:%n%s%n",
                                result.getValues().stream().map { obj: Any -> obj.toString() }.collect(Collectors.joining("\n")))
                        if (!result.isOk) {
                            System.out.printf("errors: %s%n", result.errors)
                        }
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        } else {
            System.err.printf("%s is not a file!%n", sparqlFile)
        }
    }

    companion object {
        private const val QUERY = "query"
    }
}
