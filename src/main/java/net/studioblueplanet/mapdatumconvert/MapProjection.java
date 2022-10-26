/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

/**
 *
 * @author jorgen
 */
public interface MapProjection
{
    /**
     * Converts a latitude/longitude in degrees to easting, northing in m
     * @param latlon The latitude/longitude coordinate
     * @return The easting/northing coordinate
     */
    public DatumCoordinate          latLonToMapDatum(LatLonCoordinate latlon);
    
    /**
     * Converts an easting/northing coordinate to a latitude/longitude coordinate.
     * @param md Easting/northing coordinate in meter
     * @return Latitude/Longitude in degrees
     */
    public LatLonCoordinate         mapDatumToLatLon(DatumCoordinate  md);
}
