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
import nl.knaw.huc.di.tag.schema.TAGMLSchemaFactory.parseYAML
import nl.knaw.huc.di.tag.schema.TAGMLSchemaParseResult
import nl.knaw.huc.di.tag.validate.TAGValidator
import nl.knaw.huygens.alexandria.dropwizard.cli.AlexandriaCommandException
import nl.knaw.huygens.alexandria.storage.TAGDocument
import nl.knaw.huygens.alexandria.storage.TAGStore
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.net.URL
import java.util.stream.Collectors

class ValidateCommand : AlexandriaCommand("schema-validate", "Validate a document against a TAG schema.") {
    override fun configure(subparser: Subparser) {
        subparser
                .addArgument("DOCUMENT")
                .metavar("<document>")
                .dest(DOCUMENT)
                .type(String::class.java)
                .required(true)
                .help(
                        "The name of the document to validate. It must have a valid URL to a valid schema file (in YAML) defined using '[!schema <schemaLocationURL>]' in the TAGML source file.")
        //    subparser
        //        .addArgument("-s", "--schema")
        //        .dest(SCHEMA)
        //        .type(String.class)
        //        .required(true)
        //        .help("The TAG schema file.");
    }

    @Throws(IOException::class)
    override fun run(bootstrap: Bootstrap<*>?, namespace: Namespace) {
        checkAlexandriaIsInitialized()
        val docName = namespace.getString(DOCUMENT)
        val docId = getIdForExistingDocument(docName)
        tagStore.use { store ->
            store.runInTransaction {
                val document = store.getDocument(docId)
                try {
                    val schemaLocation = document.schemaLocation
                    if (schemaLocation.isPresent) {
                        continueWithSchemaLocation(docName, store, document, schemaLocation.get())
                    } else {
                        throw AlexandriaCommandException(
                                """There was no schema location defined in $docName, please add
  [!schema <schemaLocationURL>]
to the tagml sourcefile.""")
                    }
                } catch (e: IOException) {
                    throw AlexandriaCommandException(
                            "The schema location in $docName is invalid.")
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun continueWithSchemaLocation(
            docName: String, store: TAGStore, document: TAGDocument, url: URL) {
        var tmpSchemaLocationURL = url.toString()
        if (url.protocol == "file" && !tmpSchemaLocationURL.contains("file://")) {
            tmpSchemaLocationURL = (url.protocol
                    + "://"
                    + url.path) // because url.toString() somehow loses     '//', but only on
            // Windows?!
        }
        val schemaLocationURL = tmpSchemaLocationURL
        println("Parsing schema from $schemaLocationURL:")
        val schemaYAML = IOUtils.toString(url, Charsets.UTF_8)
        val schemaParseResult = parseYAML(schemaYAML)
        if (schemaParseResult.errors.isEmpty()) {
            continueWithValidSchema(docName, store, document, schemaParseResult, schemaLocationURL)
        } else {
            println("  errors:\n"
                    + schemaParseResult.errors.stream()
                    .map { e: String -> "  - " + e.replace("\\(StringReader\\)".toRegex(), schemaLocationURL) }
                    .collect(Collectors.joining("\n")))
        }
    }

    private fun continueWithValidSchema(
            docName: String,
            store: TAGStore,
            document: TAGDocument,
            schemaParseResult: TAGMLSchemaParseResult,
            schemaLocationURL: String) {
        println("  done\n")
        val result = TAGValidator(store).validate(document, schemaParseResult.schema)
        println("Document $docName is ")
        if (!result.isValid) {
            println("  not valid:")
            println(
                    result.errors.stream().map { e: String -> "  - error: $e" }.collect(Collectors.joining("\n")))
        } else {
            println("  valid")
        }
        println(
                result.warnings.stream().map { e: String -> "  - warning: $e" }.collect(Collectors.joining("\n")))
        println("according to the schema defined in $schemaLocationURL")
    }

    companion object {
        private const val SCHEMA = "schema"
    }
}
