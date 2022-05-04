//Copyright 2008, by the California Institute of Technology.
//ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.

package gov.nasa.podaac.inventory.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author clwong
 * @version Jul 30, 2007
 * $Id: Collection.java 5773 2010-09-08 23:57:09Z gangl $
 */
@SuppressWarnings("serial")
public class Collection implements Serializable {

	private Integer collectionId;
	private String shortName;
	private String longName;
	private String description;

	
	private Set<CollectionDataset> collectionDatasetSet = new HashSet<CollectionDataset>();


	public Integer getCollectionId() {
		return collectionId;
	}
	public void setCollectionId(Integer collectionId) {
		this.collectionId = collectionId;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getLongName() {
		return longName;
	}
	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public Set<CollectionDataset> getCollectionDatasetSet() {
		return collectionDatasetSet;
	}
	public void setCollectionDatasetSet(Set<CollectionDataset> collectionDatasetSet) {
		this.collectionDatasetSet = collectionDatasetSet;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((collectionId == null) ? 0 : collectionId.hashCode());
		result = prime * result
				+ ((shortName == null) ? 0 : shortName.hashCode());
		//result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Collection other = (Collection) obj;
		if (collectionId == null) {
			if (other.collectionId != null)
				return false;
		} else if (!collectionId.equals(other.collectionId))
			return false;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;

		return true;
	}

}
