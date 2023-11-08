package org.matsim.codeexamples.network;

import org.matsim.api.core.v01.network.Network;
//import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.matsim.utils.gis.shp2matsim.ShpGeometryUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.contrib.osm.networkReader.SupersonicOsmNetworkReader;
import org.matsim.contrib.osm.networkReader.LinkProperties;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RunCreateNetworkFromOSM {

    private static String WGS84 = "EPSG:4326"; // This is the standard CRS for OSM data
    private static Path input = Paths.get("/Users/alibishokputov/Desktop/MATSim-Term-Project/MATSimNetworkBaltimoreCounty/maryland-latest.osm.pbf");
    private static Path filterShape = Paths.get("/Users/alibishokputov/Desktop/MATSim-Term-Project/MATSimNetworkBaltimoreCounty/Output_County_Boundary.shp");

    public static void main(String[] args) throws MalformedURLException {
        new RunCreateNetworkFromOSM().create();
    }

    private void create() throws MalformedURLException {

        // Choose an appropriate coordinate transformation if you need to project your network. Otherwise, use WGS84.
        CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation(
                TransformationFactory.WGS84, "EPSG:4326"
        );

        // Load the geometries of the shape file, so they can be used as a filter during network creation
        List<PreparedGeometry> filterGeometries = ShpGeometryUtils.loadPreparedGeometries(filterShape.toUri().toURL());

        // Create an OSM network reader with a filter for Baltimore County
        SupersonicOsmNetworkReader reader = new SupersonicOsmNetworkReader.Builder()
                .setCoordinateTransformation(transformation)
                .setIncludeLinkAtCoordWithHierarchy((coord, hierarchyLevel) -> {
                    // only include links that are inside the prepared geometries
                    return ShpGeometryUtils.isCoordInPreparedGeometries(coord, filterGeometries);
//                .setIncludeLinkAtCoordWithHierarchy((coord, hierarchyLevel) -> {
//
//                    // take all links which are motorway, trunk, primary, or secondary-street regardless of their location
//                    if (hierarchyLevel <= LinkProperties.LEVEL_SECONDARY) return true;
//
//                    // within the shape, take all links which are contained in the osm-file
//                    return ShpGeometryUtils.isCoordInPreparedGeometries(coord, filterGeometries);
                })
                .build();

        // Read and parse the network data
        Network network = reader.read(input.toString());

        // Remove unconnected parts to avoid agents getting stuck
        new NetworkCleaner().run(network);

        // Write the network to an XML file
        new NetworkWriter(network).write("/Users/alibishokputov/Desktop/MATSim-Term-Project/MATSimNetworkBaltimoreCounty/Baltimore_County_Network.xml.gz");
    }
}


