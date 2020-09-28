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
import nl.knaw.huygens.alexandria.query.TAGQLQueryHandler
import java.util.stream.Collectors

class QueryCommand : AlexandriaCommand("query", "Query the document.") {
    override fun configure(subparser: Subparser) {
        subparser
                .addArgument("-d", "--document")
                .dest(DOCUMENT)
                .type(String::class.java)
                .required(true)
                .help("The name of the document to query.")
        subparser
                .addArgument("-q", "--query")
                .dest(QUERY)
                .type(String::class.java)
                .required(true)
                .help("The TAGQL query.")
    }

    override fun run(bootstrap: Bootstrap<*>?, namespace: Namespace) {
        checkAlexandriaIsInitialized()
        val docName = namespace.getString(DOCUMENT)
        val statement = namespace.getString(QUERY)
        val docId = getIdForExistingDocument(docName)
        tagStore.use { store ->
            store.runInTransaction {
                System.out.printf("document: %s%n", docName)
                System.out.printf("query: %s%n", statement)
                val document = store.getDocument(docId)
                val h = TAGQLQueryHandler(document)
                val result = h.execute(statement)
                System.out.printf(
                        "result:%n%s%n",
                        result.getValues().stream().map { obj: Any -> obj.toString() }.collect(Collectors.joining("\n")))
                if (!result.isOk()) {
                    System.out.printf("errors: %s%n", result.getErrors())
                }
            }
        }
    }

    companion object {
        private const val DOCUMENT = "document"
        private const val QUERY = "query"
    }
}
