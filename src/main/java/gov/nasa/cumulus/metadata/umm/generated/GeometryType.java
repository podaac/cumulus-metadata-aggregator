
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * This entity holds the geometry representing the spatial coverage information of a granule.
 * 
 */
@Generated("jsonschema2pojo")
public class GeometryType {

    /**
     * The horizontal spatial coverage of a point.
     * 
     */
    @SerializedName("Points")
    @Expose
    private Set<PointType> points = new LinkedHashSet<PointType>();
    /**
     * This entity holds the horizontal spatial coverage of a bounding box.
     * 
     */
    @SerializedName("BoundingRectangles")
    @Expose
    private Set<BoundingRectangleType> boundingRectangles = new LinkedHashSet<BoundingRectangleType>();
    /**
     * A GPolygon specifies an area on the earth represented by a main boundary with optional boundaries for regions excluded from the main boundary.
     * 
     */
    @SerializedName("GPolygons")
    @Expose
    private Set<GPolygonType> gPolygons = new LinkedHashSet<GPolygonType>();
    /**
     * This entity holds the horizontal spatial coverage of a line. A line area contains at least two points.
     * 
     */
    @SerializedName("Lines")
    @Expose
    private Set<LineType> lines = new LinkedHashSet<LineType>();

    /**
     * The horizontal spatial coverage of a point.
     * 
     */
    public Set<PointType> getPoints() {
        return points;
    }

    /**
     * The horizontal spatial coverage of a point.
     * 
     */
    public void setPoints(Set<PointType> points) {
        this.points = points;
    }

    /**
     * This entity holds the horizontal spatial coverage of a bounding box.
     * 
     */
    public Set<BoundingRectangleType> getBoundingRectangles() {
        return boundingRectangles;
    }

    /**
     * This entity holds the horizontal spatial coverage of a bounding box.
     * 
     */
    public void setBoundingRectangles(Set<BoundingRectangleType> boundingRectangles) {
        this.boundingRectangles = boundingRectangles;
    }

    /**
     * A GPolygon specifies an area on the earth represented by a main boundary with optional boundaries for regions excluded from the main boundary.
     * 
     */
    public Set<GPolygonType> getGPolygons() {
        return gPolygons;
    }

    /**
     * A GPolygon specifies an area on the earth represented by a main boundary with optional boundaries for regions excluded from the main boundary.
     * 
     */
    public void setGPolygons(Set<GPolygonType> gPolygons) {
        this.gPolygons = gPolygons;
    }

    /**
     * This entity holds the horizontal spatial coverage of a line. A line area contains at least two points.
     * 
     */
    public Set<LineType> getLines() {
        return lines;
    }

    /**
     * This entity holds the horizontal spatial coverage of a line. A line area contains at least two points.
     * 
     */
    public void setLines(Set<LineType> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(GeometryType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("points");
        sb.append('=');
        sb.append(((this.points == null)?"<null>":this.points));
        sb.append(',');
        sb.append("boundingRectangles");
        sb.append('=');
        sb.append(((this.boundingRectangles == null)?"<null>":this.boundingRectangles));
        sb.append(',');
        sb.append("gPolygons");
        sb.append('=');
        sb.append(((this.gPolygons == null)?"<null>":this.gPolygons));
        sb.append(',');
        sb.append("lines");
        sb.append('=');
        sb.append(((this.lines == null)?"<null>":this.lines));
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
        result = ((result* 31)+((this.lines == null)? 0 :this.lines.hashCode()));
        result = ((result* 31)+((this.boundingRectangles == null)? 0 :this.boundingRectangles.hashCode()));
        result = ((result* 31)+((this.points == null)? 0 :this.points.hashCode()));
        result = ((result* 31)+((this.gPolygons == null)? 0 :this.gPolygons.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GeometryType) == false) {
            return false;
        }
        GeometryType rhs = ((GeometryType) other);
        return (((((this.lines == rhs.lines)||((this.lines!= null)&&this.lines.equals(rhs.lines)))&&((this.boundingRectangles == rhs.boundingRectangles)||((this.boundingRectangles!= null)&&this.boundingRectangles.equals(rhs.boundingRectangles))))&&((this.points == rhs.points)||((this.points!= null)&&this.points.equals(rhs.points))))&&((this.gPolygons == rhs.gPolygons)||((this.gPolygons!= null)&&this.gPolygons.equals(rhs.gPolygons))));
    }

}
