package nl.knaw.huygens.alexandria.dropwizard

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

import com.codahale.metrics.health.HealthCheck
import io.dropwizard.Application
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.federecio.dropwizard.swagger.SwaggerBundle
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration
import nl.knaw.huc.di.tag.tagml.exporter.TAGMLExporter
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentService
import nl.knaw.huygens.alexandria.dropwizard.api.PropertiesConfiguration
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.*
import nl.knaw.huygens.alexandria.dropwizard.health.ServerHealthCheck
import nl.knaw.huygens.alexandria.dropwizard.resources.AboutResource
import nl.knaw.huygens.alexandria.dropwizard.resources.DocumentsResource
import nl.knaw.huygens.alexandria.dropwizard.resources.HomePageResource
import nl.knaw.huygens.alexandria.markup.api.AppInfo
import nl.knaw.huygens.alexandria.storage.BDBTAGStore
import nl.knaw.huygens.alexandria.storage.TAGStore
import nl.knaw.huygens.alexandria.texmecs.importer.TexMECSImporter
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

class ServerApplication : Application<ServerConfiguration>() {
    private val LOG = LoggerFactory.getLogger(javaClass)
    private val appInfo = getAppInfo()
    private fun getAppInfo(): AppInfo {
        val properties = PropertiesConfiguration(PROPERTIES_FILE, true)
        return AppInfo()
                .setAppName(name)
                .setStartedAt(Instant.now().toString())
                .setBuildDate(properties.getProperty("buildDate").get())
                .setCommitId(properties.getProperty("commitId").get())
                .setScmBranch(properties.getProperty("scmBranch").get())
                .setVersion(properties.getProperty("version").get())
    }

    override fun getName(): String {
        return "Alexandria Markup Server"
    }

    override fun initialize(bootstrap: Bootstrap<ServerConfiguration>) {
        // Enable variable substitution with environment variables
        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
                bootstrap.configurationSourceProvider, EnvironmentVariableSubstitutor())
        bootstrap.addBundle(
                object : SwaggerBundle<ServerConfiguration>() {
                    override fun getSwaggerBundleConfiguration(
                            configuration: ServerConfiguration): SwaggerBundleConfiguration {
                        return configuration.swaggerBundleConfiguration!!
                    }
                })
        addCommands(bootstrap, appInfo)
    }

    fun addCommands(bootstrap: Bootstrap<ServerConfiguration>, appInfo: AppInfo) {
        bootstrap.addCommand(InitCommand())
        bootstrap.addCommand(AddCommand())
        bootstrap.addCommand(CommitCommand())
        bootstrap.addCommand(AboutCommand().withAppInfo(appInfo))
        bootstrap.addCommand(StatusCommand())
        bootstrap.addCommand(CheckOutCommand())
        bootstrap.addCommand(DiffCommand())
        bootstrap.addCommand(HelpCommand())
        bootstrap.addCommand(RevertCommand())
        bootstrap.addCommand(ExportDotCommand())
        bootstrap.addCommand(ExportRenderedDotCommand("svg"))
        bootstrap.addCommand(ExportRenderedDotCommand("png"))
        bootstrap.addCommand(ExportXmlCommand())
        bootstrap.addCommand(SPARQLQueryCommand())
        bootstrap.addCommand(ValidateCommand())
        //    bootstrap.addCommand(new ShowWorkdirCommand());
    }

    override fun run(configuration: ServerConfiguration, environment: Environment) {
        val documentService = DocumentService(configuration)
        val store: TAGStore = BDBTAGStore(configuration.dbDir, false)
        configuration.store = store
        val tagmlImporter = TAGMLImporter(store)
        val tagmlExporter = TAGMLExporter(store)
        val texMECSImporter = TexMECSImporter(store)
        environment.jersey().register(HomePageResource())
        environment.jersey().register(AboutResource(appInfo))
        environment
                .jersey()
                .register(
                        DocumentsResource(
                                documentService, tagmlImporter, texMECSImporter, tagmlExporter, configuration))
        environment.healthChecks().register("server", ServerHealthCheck())
        val results = environment.healthChecks().runHealthChecks()
        val healthy = AtomicBoolean(true)
        LOG.info("Healthchecks:")
        results.forEach { (name: String?, result: HealthCheck.Result) ->
            LOG.info(
                    "{}: {}, message='{}'",
                    name,
                    if (result.isHealthy) "healthy" else "unhealthy",
                    result.message)
            healthy.set(healthy.get() && result.isHealthy)
        }
        if (!healthy.get()) {
            throw RuntimeException("Failing health check(s)")
        }
    }

    companion object {
        private const val PROPERTIES_FILE = "about.properties"

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            ServerApplication().run(*args)
        }
    }
}
