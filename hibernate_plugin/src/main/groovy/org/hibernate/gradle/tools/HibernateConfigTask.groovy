/*
 * Copyright LABGeM 13/01/15
 *
 * author: Jonathan MERCIER
 *
 *
 * This software is a computer program whose purpose is to annotate a complete genome.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package org.hibernate.gradle.tools

import groovy.swing.SwingBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

import java.util.function.Supplier

class HibernateConfigTask extends DefaultTask {
    boolean enabled = true
    String description = "Generate hibernate config files"
    String group = "hibernatetools"
    Config config = null
    Logger logger

    @TaskAction
    def run() {
        logger = project.logger
        logger.info("current_config: ${config}")
        logger.info("creating resources directory within ${config.srcGeneratedDir}")
        config.resourcesSrcGeneratedDir.exists() || config.resourcesSrcGeneratedDir.mkdirs()
        config.srcGeneratedDir.exists() || config.srcGeneratedDir.mkdirs()
        if (project.database.revEngXml.isEmpty()) {
            logger.info("creating hibernate reverse engineering settings file: ${config.hibernateRevEngXml.path}")
            if (config.hibernateRevEngXml.exists()) {
                logger.info("deleting old settings file")
                config.hibernateRevEngXml.delete();
            }
            checkDataBase(project)
            writeRevengConfigFile(project)
        } else if (!project.database.revEngXml.isEmpty() && new File(project.database.revEngXml).exists()) {
            config.hibernateRevEngXml = new File(project.database.revEngXml)
            logger.info("using hibernate reverse engineering settings in: ${config.hibernateRevEngXml.path}")
        } else {
            def message = "${project.database.revEngXml} does not exist and cannot be used as a reverse engineering settings xml."
            IllegalStateException illegalStateException = new IllegalStateException(message.toString())
            logger.error("invalid reverse engineering file", illegalStateException)
            throw illegalStateException
        }
        loadHibernateConfigPropertiesIntoPropertiesFile()
        logger.info("updated_config: ${config}")
    }

    void loadHibernateConfigPropertiesIntoPropertiesFile() {
        def hibernateConfigMap = createHibernateConfigMap(project)
        Properties hibernateConfigPropertiesFromMap = new Properties()
        hibernateConfigPropertiesFromMap.putAll(hibernateConfigMap)
        try {
            if(config.hibernateConfigProperties.exists()){
                logger.info("deleting current hibernate config properties file: ${config.hibernateConfigProperties.path}")
                config.hibernateConfigProperties.delete()
            }
            logger.info("creating new hibernate config properties file: ${config.hibernateConfigProperties.path}")
            config.hibernateConfigProperties.createNewFile()
            hibernateConfigPropertiesFromMap.store(config.hibernateConfigProperties.newOutputStream(), "hibernate config properties for reverse engineering data schema")
        } catch (IOException e) {
            logger.error("an error occurred when loading the hibernate configuration properties from a map to the file: ${config.hibernateConfigProperties.path}", e)
            throw e
        }
    }

    void checkDataBase(Project project) {
        logger.info("checking database properties provided: ${project.database}")
        def console = System.console()
        if (console == null) {
            new SwingBuilder().edt {
                dialog(modal: true, title: 'Enter password', alwaysOnTop: true, resizable: false, locationRelativeTo: null, pack: true, show: true) {
                    vbox { // Put everything below each other
                        label(text: "Please enter your username:")
                        textField(id: 'usernameField')
                        label(text: "Please enter your password:")
                        passwordField(id: 'passwordField')
                        button(defaultButton: true, text: 'OK', actionPerformed: {
                            //project.database.user       = input1.password;
                            //project.database.password   = input2.password;
                            dispose();
                        })
                        bind(source: usernameField, sourceProperty: 'text', target: project.database, targetProperty: 'user')
                        bind(source: passwordField, sourceProperty: 'text', target: project.database, targetProperty: 'password')
                    }
                }
            }
        } else {
            console.writer().println()
            console.writer().println("== User definition ==")
            if (project.database.user == "")
                project.database.user = console.readLine('> Please enter your username: ')
            if (project.database.password == "")
                project.database.password = new String(console.readPassword('> Please enter your password: '))
            console.writer().println("========")
        }
    }

    Map<String, String> createHibernateConfigMap(Project project) {
        logger.info('creating hibernate config map for task')
        Map<String, String> hibernateConfigMap = new HashMap<>();
        hibernateConfigMap.put("hibernate.dialect", "${project.database.dialect}".toString())
        hibernateConfigMap.put("hibernate.connection.driver_class", "${project.database.driver}".toString())
        hibernateConfigMap.put("hibernate.connection.url", "${project.database.url}".toString())
        hibernateConfigMap.put("hibernate.connection.username", "${project.database.user}".toString())
        hibernateConfigMap.put("hibernate.connection.password", "${project.database.password}".toString())
        hibernateConfigMap.put("hibernate.current_session_context_class", "thread")
        hibernateConfigMap.put("hibernate.connection.zeroDateTimeBehavior", "convertToNull")
        return hibernateConfigMap
    }

    def writeRevengConfigFile(final Project project) {

        config.hibernateRevEngXml.append(
                """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-reverse-engineering
    SYSTEM "http://hibernate.sourceforge.net/hibernate-reverse-engineering-3.0.dtd">

<hibernate-reverse-engineering>
"""
        )
        project.database.catalog.each { catalogName, schema ->
            schema.tables.each { tableName ->
                config.hibernateRevEngXml.append(
                        """
    <schema-selection match-catalog="${catalogName}" match-schema="${schema.name}" match-table="${tableName}" />
"""
                )
            }
        }
        config.hibernateRevEngXml.append(
                """
</hibernate-reverse-engineering>
"""
        )

    }

}
