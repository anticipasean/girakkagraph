package org.hibernate.gradle.tools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaPlugin

import javax.swing.plaf.nimbus.State

/*
 * Copyright LABGeM 15/01/15
 *
 * author: Jonathan MERCIER
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

class HibernatePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply JavaPlugin
        project.extensions.create('database', Database)
        project.afterEvaluate {
            def gradleProjectConfiguration = project.configurations.create('reveng')
            addHibernateToolsAsDependencyIfNotPresent(project, gradleProjectConfiguration)
            gradleProjectConfiguration.extendsFrom(project.configurations.findByName('compile'))
            gradleProjectConfiguration.extendsFrom(project.configurations.findByName('runtime'))
            String classpath = project.configurations.findByName("default").asPath
            def conf = new Config(
                    new File("${project.buildDir}${File.separator}generated${File.separator}src${File.separator}"),
                    classpath
            )
            if (project.hasProperty("database")) {
                Database db = project.getProperties().get("database") as Database
                if (db.revEngXml != null && !db.revEngXml.isEmpty()) {
                    conf.hibernateRevEngXml = new File(db.revEngXml)
                }
            }
            Task hibernateConfigTask = project.task("hibernateConfig", type: HibernateConfigTask) {
                config = conf
                inputs.files conf.hibernateRevEngXml
                outputs.dir conf.srcGeneratedDir
                outputs.upToDateWhen { false }
            }
            Task hbm2JavaTask = project.task("hbm2java", type: Hbm2JavaTask, dependsOn: "hibernateConfig") {
                config = conf
                inputs.files conf.hibernateRevEngXml
                outputs.dir conf.srcGeneratedDir
            }.onlyIf { hibernateConfigTask.state.failure == null}
            Task hbm2DaoTask = project.task("hbm2dao", type: Hbm2DaoTask, dependsOn: "hbm2java") {
                config = conf
                inputs.files conf.hibernateRevEngXml
                outputs.dir conf.srcGeneratedDir
            }.onlyIf { hbm2JavaTask.state.failure == null}
            addGeneratedToSource(project)
        }
    }

    private void addHibernateToolsAsDependencyIfNotPresent(Project project, Configuration configuration) {
        Dependency dependencyForHibernateTools = findDependencyForHibernateTools(project)
        if (dependencyForHibernateTools != null) {
            configuration.dependencies.add(dependencyForHibernateTools.copy())
        } else {
            configuration.dependencies.add(project.dependencies.create('org.hibernate:hibernate-tools:5.2.0.Final'))
        }
    }

    Dependency findDependencyForHibernateTools(Project project) {
        project.configurations.findByName("implementation")?.dependencies?.find { Dependency d -> (d.group == "org.hibernate" && d.name == 'hibernate-tools') }
    }

    void addGeneratedToSource(Project project) {
        project.sourceSets.matching { it.name == "main" }.all {
            it.java.srcDir "${project.buildDir}${File.separator}generated${File.separator}src${File.separator}java"
        }
    }
}


class Config {
    def File hibernateRevEngXml
    def File srcGeneratedDir
    def File javaSrcGeneratedDir
    def File resourcesSrcGeneratedDir
    def File hibernateConfigProperties
    def String classPath

    public Config(final File srcGeneratedDir, final String classPath) {
        this.srcGeneratedDir = srcGeneratedDir
        this.javaSrcGeneratedDir = new File(srcGeneratedDir, "java")
        this.resourcesSrcGeneratedDir = new File(srcGeneratedDir, "resources")
        this.hibernateRevEngXml = new File(resourcesSrcGeneratedDir, "hibernate_reveng.xml")
        this.hibernateConfigProperties = new File(resourcesSrcGeneratedDir, "hibernate_config.properties")
        this.classPath = classPath
    }


    @Override
    public String toString() {
        return "Config{" +
                "hibernateRevEngXml=" + hibernateRevEngXml +
                ", srcGeneratedDir=" + srcGeneratedDir +
                ", javaSrcGeneratedDir=" + javaSrcGeneratedDir +
                ", resourcesSrcGeneratedDir=" + resourcesSrcGeneratedDir +
                ", hibernateConfigProperties=" + hibernateConfigProperties +
                ", classPath='" + classPath + '\'' +
                '}';
    }
}
