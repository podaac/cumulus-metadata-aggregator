// Copyright 2008, by the California Institute of Technology.
// ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.

package gov.nasa.podaac.inventory.model;

import java.io.Serializable;

/**
 * @author clwong
 * @version $Id: DatasetRelationship.java 1 gangl $
 */

/*
 * Added for DMAS5 dataset relationships
 */
@SuppressWarnings("serial")
public class DatasetRelationship implements Serializable {

	public static class DatasetRelationshipPK implements Serializable{
		
		private Dataset dataset = new Dataset();
		private Dataset relatedDataset= new Dataset();
		public Dataset getDataset() {
			return dataset;
		}
		public void setDataset(Dataset dataset) {
			this.dataset = dataset;
		}
		public Dataset getRelatedDataset() {
			return relatedDataset;
		}
		public void setRelatedDataset(Dataset relatedDataset) {
			this.relatedDataset= relatedDataset;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((relatedDataset == null) ? 0 : relatedDataset.hashCode());
			result = prime * result
					+ ((dataset == null) ? 0 : dataset.hashCode());
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
			final DatasetRelationshipPK other = (DatasetRelationshipPK) obj;
			if (relatedDataset == null) {
				if (other.relatedDataset != null)
					return false;
			} else if (!relatedDataset.equals(other.relatedDataset))
				return false;
			if (dataset == null) {
				if (other.dataset != null)
					return false;
			} else if (!dataset.equals(other.dataset))
				return false;
			return true;
		}
	}
	
	private DatasetRelationshipPK datasetRelationshipPK = new DatasetRelationshipPK();
	private String relationship;
	
	
	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relation) {
		this.relationship = relation;
	}

	public DatasetRelationshipPK getDatasetRelationshipPK() {
		return datasetRelationshipPK;
	}

	public void setDatasetRelationshipPK(DatasetRelationshipPK datasetRelationshipPK) {
		this.datasetRelationshipPK = datasetRelationshipPK;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime* result
				+ ((datasetRelationshipPK == null) ? 0 : datasetRelationshipPK.hashCode());
		
		result = prime * result
				+ ((relationship== null) ? 0 : relationship.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DatasetRelationship))
			return false;
		final DatasetRelationship other = (DatasetRelationship) obj;
		if (datasetRelationshipPK == null) {
			if (other.datasetRelationshipPK != null)
				return false;
		} else if (!datasetRelationshipPK.equals(other.datasetRelationshipPK))
			return false;
		
		if (relationship == null) {
			if (other.relationship != null)
				return false;
		} else if (!relationship.equals(other.relationship))
			return false;

		return true;
	}
}
