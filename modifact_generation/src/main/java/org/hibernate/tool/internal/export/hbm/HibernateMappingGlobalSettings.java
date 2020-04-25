/*
 * Created on 2004-12-26
 *
 */
package org.hibernate.tool.internal.export.hbm;

import org.hibernate.internal.util.StringHelper;

/**
 * This class replicates the global settings that can be selected within the mapping document. This
 * is provided to allow a GUI too to choose these settings and thus the generated mapping document
 * will include them.
 *
 * @author David Channon
 */
public class HibernateMappingGlobalSettings {

  private String schemaName;
  private String catalogName;
  private String defaultCascade;
  private String defaultPackage;
  private String defaultAccess;
  private boolean autoImport = true;
  private boolean defaultLazy = true;

  /** */
  public HibernateMappingGlobalSettings() {}

  public boolean hasNonDefaultSettings() {
    return this.hasDefaultPackage()
        || this.hasSchemaName()
        || this.hasCatalogName()
        || this.hasNonDefaultCascade()
        || this.hasNonDefaultAccess()
        || !this.isDefaultLazy()
        || !this.isAutoImport();
  }

  public boolean hasDefaultPackage() {
    return !StringHelper.isEmpty(defaultPackage);
  }

  public boolean hasSchemaName() {
    return !StringHelper.isEmpty(schemaName);
  }

  public boolean hasCatalogName() {
    return !StringHelper.isEmpty(catalogName);
  }

  public boolean hasNonDefaultCascade() {
    return !StringHelper.isEmpty(defaultCascade) && !"none".equals(defaultCascade);
  }

  public boolean hasNonDefaultAccess() {
    return !StringHelper.isEmpty(defaultAccess) && !"property".equals(defaultAccess);
  }

  public String getSchemaName() {
    return schemaName;
  }

  /**
   * Sets the schemaName.
   *
   * @param schemaName The schemaName to set
   */
  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getCatalogName() {
    return catalogName;
  }

  /**
   * Sets the catalogName.
   *
   * @param catalogName The catalogName to set
   */
  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  public String getDefaultCascade() {
    return defaultCascade;
  }

  /**
   * Sets the defaultCascade.
   *
   * @param defaultCascade The defaultCascade to set
   */
  public void setDefaultCascade(String defaultCascade) {
    this.defaultCascade = defaultCascade;
  }

  public String getDefaultAccess() {
    return defaultAccess;
  }

  /**
   * sets the default access strategy
   *
   * @param defaultAccess the default access strategy.
   */
  public void setDefaultAccess(String defaultAccess) {
    this.defaultAccess = defaultAccess;
  }

  public String getDefaultPackage() {
    return defaultPackage;
  }

  /** @param defaultPackage The defaultPackage to set. */
  public void setDefaultPackage(String defaultPackage) {
    this.defaultPackage = defaultPackage;
  }

  public boolean isDefaultLazy() {
    return defaultLazy;
  }

  /**
   * Sets the defaultLazy.
   *
   * @param defaultLazy The defaultLazy to set
   */
  public void setDefaultLazy(boolean defaultLazy) {
    this.defaultLazy = defaultLazy;
  }

  /**
   * Returns the autoImport.
   *
   * @return boolean
   */
  public boolean isAutoImport() {
    return autoImport;
  }

  /**
   * Sets the autoImport.
   *
   * @param autoImport The autoImport to set
   */
  public void setAutoImport(boolean autoImport) {
    this.autoImport = autoImport;
  }
}
