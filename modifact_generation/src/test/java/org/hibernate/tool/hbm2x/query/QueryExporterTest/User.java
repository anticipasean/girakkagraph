//$Id: User.java 7085 2005-06-08 17:59:47Z oneovthafew $
package org.hibernate.tool.hbm2x.query.QueryExporterTest;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class User implements Serializable {
	
	private static final long serialVersionUID = 
			ObjectStreamClass.lookup(User.class).getSerialVersionUID();
	
	private String org;
	private String name;
	private Set<Group> groups = new HashSet<Group>();

	public User(String name, String org) {
		this.org = org;
		this.name = name;
	}

	public User() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

}
