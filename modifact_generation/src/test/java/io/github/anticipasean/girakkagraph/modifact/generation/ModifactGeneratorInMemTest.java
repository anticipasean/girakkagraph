package io.github.anticipasean.girakkagraph.modifact.generation;

import cyclops.control.Option;
import cyclops.control.Try;
import cyclops.reactive.IO;
import io.github.anticipasean.girakkagraph.modifact.generation.configuration.GeneratorConfiguration;
import io.github.anticipasean.girakkagraph.modifact.generation.configuration.GeneratorConfigurationImpl;
import io.github.anticipasean.girakkagraph.modifact.generation.configuration.GeneratorConfigurationImpl.Builder;
import io.github.anticipasean.girakkagraph.modifact.generation.configuration.jdbc.JdbcSchemaSelectionImpl;
import io.github.anticipasean.girakkagraph.modifact.generation.generator.DefaultModifactGenerator;
import io.github.anticipasean.girakkagraph.modifact.generation.stage.SchemaDefinitionStageImpl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.h2.jdbc.JdbcConnection;
import org.hibernate.tools.test.util.JdbcUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModifactGeneratorInMemTest {

  GeneratorConfiguration configuration;
  Logger logger;
  private static String verificationDbDdlScriptPath =
      "verification_database_ddl_qa32_20200506.sql";

  @Before
  public void setUp() throws Exception {
    logger = LoggerFactory.getLogger(ModifactGeneratorInMemTest.class);
    logger.info("set_up: " + ModifactGeneratorInMemTest.class.getName());
    String statement =
        IO.of(new File(verificationDbDdlScriptPath))
            .checkedMap(FileReader::new)
            .checkedMap(BufferedReader::new)
            .foldRun(bufferedReaders -> bufferedReaders.map(BufferedReader::lines))
            .mapOrCatch(
                stringStream -> stringStream.collect(Collectors.joining("\n")),
                FileNotFoundException.class,
                IOException.class)
            .orElse("");
    JdbcUtil.establishJdbcConnection(this);
    JdbcUtil.executeSql(this, new String[] {statement});

    Builder configurationBuilder =
        GeneratorConfigurationImpl.builder()
            .addSchemaSelections(
                JdbcSchemaSelectionImpl.builder()
                    .matchCatalog(".*")
                    .matchSchema(".*")
                    .matchTable(".*")
                    .build())
            .modelName("testModel");
    URL persistencePropertiesUrl =
        Thread.currentThread().getContextClassLoader().getResource("hibernate.properties");
    if (persistencePropertiesUrl != null) {
      configurationBuilder.persistenceVendorPropertiesPath(persistencePropertiesUrl.getPath());
    }
    configuration = configurationBuilder.build();
    logger.info("configuration: " + configuration);
  }

  @After
  public void tearDown() throws Exception {
    JdbcUtil.dropDatabase(this);
  }

  @Test
  public void generateModifact() {
    new DefaultModifactGenerator(
            DevelopmentStage.stage(
                () ->
                    SchemaDefinitionStageImpl.builder()
                        .configuration(configuration)
                        .build()
                        .nextDevelopmentStage()))
        .generateModifact();
  }

  private void logH2ConnectionSettingsIfH2JdbcConnection() {
    Option<Connection> connectionIfActive = JdbcUtil.getConnectionIfActive(this);
    Option<String> settingsBreakdown =
        connectionIfActive
            .filter(connection -> connection instanceof JdbcConnection)
            .map(conn -> Try.withCatch(((JdbcConnection) conn)::getSettings, SQLException.class))
            .flatMap(
                settingsTry ->
                    settingsTry
                        .map(
                            settings ->
                                Option.of(
                                    String.format(
                                        "mode: "
                                            + "%s, databaseToUpper: %s, databaseToLower: %s, caseInsensitiveIdentifiers: %s",
                                        settings.mode,
                                        settings.databaseToUpper,
                                        settings.databaseToLower,
                                        settings.caseInsensitiveIdentifiers)))
                        .orElse(Option.none()));
    settingsBreakdown.forEach(s -> logger.info("jdbc_connection_settings: " + s));
  }

  private Stream<String> verificationSqlStatementFileNamesStream() {
    return Stream.of(
        "000-DATABASECHANGELOG.sql",
        "001-DATABASECHANGELOGLOCK.sql",
        "002-EncryptionKey.sql",
        "003-ListingStateMachine.sql",
        "004-ListingStateMachineTransition.sql",
        "005-RequestVersion.sql",
        "006-StateMachineLookUp.sql",
        "007-VerificationInquiry.sql",
        "008-VerificationType.sql",
        "009-VerificationResponse.sql",
        "010-VerificationRequest.sql",
        "011-GiactResponse.sql",
        "012-Hold.sql",
        "013-LexisNexisResponse.sql",
        "014-DecisionAttribute.sql",
        "015-DecisionMessage.sql",
        "016-StatisticalServiceResponseData.sql",
        "017-HitFlags.sql",
        "018-MicroBiltResponse.sql",
        "019-TWNResponse.sql",
        "020-DecisionEngineResponseData.sql",
        "021-IdAnalyticsResponse.sql",
        "022-HoldStatus.sql",
        "023-ListingInfo.sql",
        "024-Giact.sql",
        "025-IdentityInfo.sql",
        "026-Transunion.sql",
        "027-MiiCard.sql",
        "028-LexisNexis.sql",
        "029-IdAnalyticsOLN.sql",
        "030-LexisNexisRiskView.sql",
        "031-TWN.sql",
        "032-StatisticalService.sql",
        "033-EmailAge.sql",
        "034-IncomeInfo.sql",
        "035-BankInfo.sql",
        "036-Plaid.sql",
        "037-UserInfo.sql",
        "038-IdAnalytics.sql",
        "039-MicroBilt.sql",
        "040-ThreatMetrix.sql",
        "041-Clarity.sql",
        "042-TWNPay.sql",
        "043-TWNCompensation.sql",
        "044-TWNEmployer.sql",
        "045-TWNEmployee.sql",
        "046-IdAnalyticsFraud.sql",
        "047-IdAnalyticsCom360.sql",
        "048-IdAnalyticsTimesApplied.sql",
        "049-IdAnalyticsNum.sql",
        "050-IdAnalyticsNumEvents.sql",
        "051-IdAnalyticsValidation.sql",
        "052-IdAnalyticsScore.sql",
        "053-IdAnalyticsCode.sql",
        "054-IdAnalyticsMisc.sql",
        "055-TransunionAttribute.sql",
        "056-TransunionSecurityAlert.sql",
        "057-TransunionAttributeCalculated.sql",
        "058-MiiCardIncomeSource.sql",
        "059-IdAnalyticsOLNAttribute.sql",
        "060-LexisNexisRModelAdverseActionReason.sql",
        "061-LexisNexisRiskViewAttribute.sql",
        "062-PlaidIncomeSource.sql");
  }
}
