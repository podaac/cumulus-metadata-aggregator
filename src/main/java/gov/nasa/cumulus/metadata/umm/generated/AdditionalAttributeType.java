
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * A reference to an additional attribute in the parent collection. The attribute reference may contain a granule specific value that will override the value in the parent collection for this granule. An attribute with the  same name must exist in the parent collection.
 * 
 */
public class AdditionalAttributeType {

    /**
     * The additional attribute's name.
     * (Required)
     * 
     */
    @SerializedName("Name")
    @Expose
    private String name;
    /**
     * Values of the additional attribute.
     * (Required)
     * 
     */
    @SerializedName("Values")
    @Expose
    private List<String> values = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public AdditionalAttributeType() {
    }

    /**
     * 
     * @param values
     * @param name
     */
    public AdditionalAttributeType(String name, List<String> values) {
        super();
        this.name = name;
        this.values = values;
    }

    /**
     * The additional attribute's name.
     * (Required)
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * The additional attribute's name.
     * (Required)
     * 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Values of the additional attribute.
     * (Required)
     * 
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Values of the additional attribute.
     * (Required)
     * 
     */
    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AdditionalAttributeType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("values");
        sb.append('=');
        sb.append(((this.values == null)?"<null>":this.values));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.values == null)? 0 :this.values.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AdditionalAttributeType) == false) {
            return false;
        }
        AdditionalAttributeType rhs = ((AdditionalAttributeType) other);
        return (((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name)))&&((this.values == rhs.values)||((this.values!= null)&&this.values.equals(rhs.values))));
    }

}
