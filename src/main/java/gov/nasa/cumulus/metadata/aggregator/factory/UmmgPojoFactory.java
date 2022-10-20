package gov.nasa.cumulus.metadata.aggregator.factory;

import gov.nasa.cumulus.metadata.umm.generated.AdditionalAttributeType;
import gov.nasa.cumulus.metadata.umm.generated.TrackPassTileType;
import gov.nasa.cumulus.metadata.umm.generated.TrackType;

import java.util.ArrayList;
import java.util.List;

public class UmmgPojoFactory {
    private static UmmgPojoFactory _self = null;

    private UmmgPojoFactory(){}

    public static UmmgPojoFactory getInstance() {
        if (_self == null){
            _self = new UmmgPojoFactory();
        }
        return _self;
    }

    /**
     * create a single pass -> tiles:[] association
     * @param pass
     * @param tiles
     * @return
     */
    public TrackPassTileType createTrackPassTileType(Integer pass, List<String> tiles) {
        TrackPassTileType trackPassTileType = new TrackPassTileType();
        trackPassTileType.setPass(pass);
        if(tiles !=null && tiles.size() >0) {
            trackPassTileType.setTiles(tiles);
        }
        return trackPassTileType;
    }

    /**
     * Create the TrackType POJO which represents the cycle and passes array
     * each pass is a combination of Pass and Tiles Array
     * @return
     */
    public TrackType createTrackType(Integer cycle, List<TrackPassTileType> trackPassTileTypes) {
        TrackType trackType = new TrackType();
        trackType.setCycle(cycle);
        trackType.setPasses(trackPassTileTypes);
        return trackType;
    }

    public List<AdditionalAttributeType> trackTypeToAdditionalAttributeTypes(TrackType trackType) {
        List<TrackPassTileType> trackPassTileTypes = trackType.getPasses();
        ArrayList<AdditionalAttributeType> additionalAttributeTypes = new ArrayList<>();
        trackPassTileTypes.stream().forEach(trackPassTileType ->  {
            AdditionalAttributeType additionalAttributeType = new AdditionalAttributeType();
            List<String> tilesStrs = trackPassTileType.getTiles();
            additionalAttributeType.setName("TILE");
            additionalAttributeType.setValues(tilesStrs);
            additionalAttributeTypes.add(additionalAttributeType);
        });
        return additionalAttributeTypes;
    }
}
