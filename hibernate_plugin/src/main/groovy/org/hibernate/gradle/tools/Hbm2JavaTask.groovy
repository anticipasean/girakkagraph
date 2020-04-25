package org.hibernate.gradle.tools
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction


class Hbm2JavaTask  extends DefaultTask{
    def Config  config      = null
    def boolean enabled     = true
    def String  description = "Generate java classes"
    def String  group       = "hibernatetools"

    @TaskAction
    def run(){
        Task compileJava = project.getTasksByName('compileJava',false).iterator().next()
        compileJava.dependsOn('hbm2java')
        hbm2java(project)
    }

    def hbm2java(final Project project){
        project.ant {
            taskdef(name: "hibernatetool",
                    classname: "org.hibernate.tool.ant.HibernateToolTask",
                    classpath: config.classPath
            )
            hibernatetool( destdir : config.javaSrcGeneratedDir, templatepath : 'templates' ) {
                jdbcconfiguration(
                        revengfile:         "${config.hibernateRevEngXml.path}",
                        packagename:        "${project.database.basePackage}",
                        propertyFile:        config.hibernateConfigProperties
                )
                hbm2java(
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
