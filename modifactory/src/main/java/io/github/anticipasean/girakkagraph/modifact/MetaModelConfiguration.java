package io.github.anticipasean.girakkagraph.modifact;

import akka.japi.Pair;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.Path;
import org.hibernate.boot.Metadata;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.OverrideRepository;
import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.tool.ant.ExporterTask;
import org.hibernate.tool.ant.Hbm2JavaExporterTask;
import org.hibernate.tool.ant.HibernateToolTask;
import org.hibernate.tool.ant.JDBCConfigurationTask;
import org.hibernate.tool.api.metadata.MetadataDescriptor;
import org.hibernate.tool.api.metadata.MetadataDescriptorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaModelConfiguration {

  private static final String javafySchemaTargetName = "javafy_schema_target";
  private final String DEFAULT_PACKAGE_NAME = "girakkagraph.springboot.metamodel";
  private final Logger logger;
  private final String exportDirectoryStr;

  public MetaModelConfiguration() {
    logger = LoggerFactory.getLogger(MetaModelConfiguration.class);
    exportDirectoryStr =
        "springboot/build/generated/sources/hibernate/java/main";
  }

  private void createJavaClassesFromDBSchema() {
    logger.info("beginning ant project for creating java entity classes from db schema");
    Project antProject = createAntProject();
    try {
      antProject.init();
    } catch (BuildException e) {
      logger.error("ant could not initialize the project properly: ", e);
      throw e;
    }
    try {
      antProject.executeTarget(javafySchemaTargetName);
    } catch (BuildException e) {
      logger.error(
          "ant could not complete the execution of target: " + antProject.getDefaultTarget(), e);
      throw e;
    }
    packageJavafiedSchemaClassesIntoJar("some_dir");
  }

  private void packageJavafiedSchemaClassesIntoJar(String jarPath) {
    File exportDirectory = new File(exportDirectoryStr);
    try {
      JarFile jarFile = new JarFile(jarPath);

    } catch (IOException e) {
      logger.error("unable to create a jar file at " + jarPath);
    }
  }

  public Project createAntProject() {
    Project javafySchemaProject = new Project();
    DefaultLogger consoleLogger = new DefaultLogger();
    consoleLogger.setErrorPrintStream(System.err);
    consoleLogger.setOutputPrintStream(System.out);
    consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
    javafySchemaProject.addBuildListener(consoleLogger);
    javafySchemaProject.setName("javafy_schema_project");
    Target javafiedSchemaTarget = new Target();
    javafiedSchemaTarget.setName(javafySchemaTargetName);
    HibernateToolTask hibernateToolTask = new HibernateToolTask();
    hibernateToolTask.setProject(javafySchemaProject);
    createJDBCConfigurationTaskOnHibernateToolTask(javafySchemaProject, hibernateToolTask);
    createJavaExporterTaskOnHibernateToolTask(hibernateToolTask);
    javafiedSchemaTarget.addTask(hibernateToolTask);
    javafySchemaProject.addOrReplaceTarget(javafiedSchemaTarget);
    return javafySchemaProject;
  }

  public JDBCConfigurationTask createJDBCConfigurationTaskOnHibernateToolTask(
      Project javafyProject, HibernateToolTask hibernateToolTask) {
    JDBCConfigurationTask jdbcConfiguration = hibernateToolTask.createJDBCConfiguration();
    jdbcConfiguration.setRevEngFile(getHibernateReverseEngineeringXmlAsAntPath(javafyProject));
    jdbcConfiguration.setPackageName(DEFAULT_PACKAGE_NAME);
    jdbcConfiguration.setPropertyFile(createHibernatePropertiesFileObject());
    return jdbcConfiguration;
  }

  public ExporterTask createJavaExporterTaskOnHibernateToolTask(
      HibernateToolTask hibernateToolTask) {
    ExporterTask javaFileExporterTask = hibernateToolTask.createHbm2Java();
    File destinationDirectoryForClassSourceFiles = new File(exportDirectoryStr);
    if (destinationDirectoryForClassSourceFiles.mkdirs()) {
      logger.info("created directory: " + exportDirectoryStr);
    }
    javaFileExporterTask.setDestdir(destinationDirectoryForClassSourceFiles);
    if (javaFileExporterTask instanceof Hbm2JavaExporterTask) {
      ((Hbm2JavaExporterTask) javaFileExporterTask).setJdk5(true);
      ((Hbm2JavaExporterTask) javaFileExporterTask).setEjb3(true);
    }
    return javaFileExporterTask;
  }

  public void createMetaModelFromDatabase() {
    Properties hibernateConnectionProperties = createHibernatePropertiesObject();
    File reversEngConfigXmlFile = getHibernateReverseEngineeringXmlFile();
    MetadataDescriptor metadataDescriptor =
        createMetadataDescriptorUsingPropertiesAndReverseEngineeringConfigurationXml(
            hibernateConnectionProperties, reversEngConfigXmlFile);
    Metadata metadata = createMetadataFromDescriptor(metadataDescriptor);
    logger.info(
        "metadata: "
            + metadata.getEntityBindings().stream()
                .map(mapPersistantClassToOutputString())
                .collect(Collectors.joining(", ")));
  }

  private File createHibernatePropertiesFileObject() {
    Properties properties = createHibernatePropertiesObject();
    try {
      File tempPropertiesFile = File.createTempFile("javafyProject", "properties");
      tempPropertiesFile.deleteOnExit();
      tempPropertiesFile.setWritable(true);
      try (FileOutputStream tempPropsFileOutputStream = new FileOutputStream(tempPropertiesFile)) {
        properties.store(tempPropsFileOutputStream, "temporarily store properties for ant project");
      }
      return tempPropertiesFile;
    } catch (IOException e) {
      logger.error("io exception occurred when creating temp file", e);
      throw new RuntimeException(e);
    }
  }

  private Properties createHibernatePropertiesObject() {
    String url =
        "jdbc:sqlserver://LOCALDB;portNumber=1433;databaseName=LOCALDB;integratedSecurity=true;authenticationScheme=JavaKerberos";
    Map<String, Object> hibernateConnectionProviderPropertiesMap =
        Stream.<Pair<String, Object>>of(
                Pair.create(
                    AvailableSettings.DRIVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
                Pair.create(
                    AvailableSettings.DIALECT, "org.hibernate.dialect.SQLServer2012Dialect"),
                Pair.create(AvailableSettings.URL, url),
                Pair.create(AvailableSettings.USER, "local_user"))
            .collect(Collectors.toMap(Pair::first, Pair::second));
    Properties hibernateConnectionProperties = new Properties();
    hibernateConnectionProperties.putAll(hibernateConnectionProviderPropertiesMap);
    return hibernateConnectionProperties;
  }

  private Path getHibernateReverseEngineeringXmlAsAntPath(Project javafyProject) {
    Path antPath = new Path(javafyProject);
    antPath.setLocation(getHibernateReverseEngineeringXmlFile());
    return antPath;
  }

  private File getHibernateReverseEngineeringXmlFile() {
    return new File(
        "springboot/src/test/resources/hibernate_reverse_eng.xml");
  }

  private Function<PersistentClass, String> mapPersistantClassToOutputString() {
    return persistentClass -> {
      Stream<Pair<String, String>> pairStream =
          Stream.of(
              Pair.create("persistent_class_name", persistentClass.getClassName()),
              Pair.create("jpa_entity_name", persistentClass.getJpaEntityName()),
              Pair.create("entity_name", persistentClass.getEntityName()));
      return "[ "
          + pairStream
              .map(pair -> String.join(": ", pair.first(), pair.second()))
              .collect(Collectors.joining(", "))
          + " ]\n";
    };
  }

  private Metadata createMetadataFromDescriptor(MetadataDescriptor metadataDescriptor) {
    try {
      return metadataDescriptor.createMetadata();
    } catch (Exception e) {
      logger.error("an error occurred creating metadata from descriptor: " + metadataDescriptor, e);
      throw e;
    }
  }

  private MetadataDescriptor
      createMetadataDescriptorUsingPropertiesAndReverseEngineeringConfigurationXml(
          Properties properties, File reversEngConfigXmlFile) {
    ReverseEngineeringStrategy res = createReverseEngineeringStrategy(reversEngConfigXmlFile);
    try {
      boolean preferBasicCompositeIds = true;
      return MetadataDescriptorFactory.createJdbcDescriptor(
          res, properties, preferBasicCompositeIds);
    } catch (Exception e) {
      logger.error("an error occurred when creating the jdbc descriptor: ", e);
      throw e;
    }
  }

  private ReverseEngineeringStrategy createReverseEngineeringStrategy(File reversEngConfigXmlFile) {
    DefaultReverseEngineeringStrategy defaultStrategy = new DefaultReverseEngineeringStrategy();

    ReverseEngineeringStrategy strategy = defaultStrategy;

    if (reversEngConfigXmlFile != null) {
      OverrideRepository or = new OverrideRepository();
      or.addFile(reversEngConfigXmlFile);
      strategy = or.getReverseEngineeringStrategy(defaultStrategy);
    }

    boolean detectOneToOne = true;
    boolean detectManyToMany = true;
    boolean detectOptimisticLock = true;
    ReverseEngineeringSettings combinedReverseEngineeringSettings =
        new ReverseEngineeringSettings(strategy)
            .setDefaultPackageName(DEFAULT_PACKAGE_NAME)
            .setDetectManyToMany(detectManyToMany)
            .setDetectOneToOne(detectOneToOne)
            .setDetectOptimisticLock(detectOptimisticLock);

    defaultStrategy.setSettings(combinedReverseEngineeringSettings);
    strategy.setSettings(combinedReverseEngineeringSettings);

    return strategy;
  }

  private Properties loadPropertiesFile(File propertyFile) {
    if (propertyFile != null) {
      Properties properties = new Properties();
      FileInputStream is = null;
      try {
        is = new FileInputStream(propertyFile);
        properties.load(is);
        return properties;
      } catch (FileNotFoundException e) {
        throw new BuildException(propertyFile + " not found.", e);
      } catch (IOException e) {
        throw new BuildException("Problem while loading " + propertyFile, e);
      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (IOException e) {
          }
        }
      }
    } else {
      return null;
    }
  }
}
