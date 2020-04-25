package org.hibernate.gradle.tools

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction



class Hbm2DaoTask  extends DefaultTask{
    def Config  config
    def boolean enabled     = true
    def String  description = "Generate DAO classes"
    def String  group       = "hibernatetools"

    @TaskAction
    def run(){
        Task compileJava = project.getTasksByName('compileJava',false).iterator().next()
        compileJava.dependsOn('hbm2dao')
        hbm2dao(project)
    }

    def hbm2dao(final Project project){
        project.ant {
            taskdef(name: "hibernatetool",
                    classname: "org.hibernate.tool.ant.HibernateToolTask",
                    classpath: config.classPath
            )
            hibernatetool( destdir : config.javaSrcGeneratedDir ) {
                jdbcconfiguration(
                        configurationfile:  "${config.hibernateConfigXml.path}",
                        revengfile:         "${config.hibernateRevEngXml.path}",
                        packagename:        "${project.database.basePackage}"
                )
                hbm2dao(
                        jdk5: true,
                        ejb3: true
                )
                classpath {
                    pathelement( path: "config" )
                }
            }
        }

    }
}
