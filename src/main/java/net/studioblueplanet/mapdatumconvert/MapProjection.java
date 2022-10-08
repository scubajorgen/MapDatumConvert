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
    public DatumCoordinate          latLonToMapDatum(LatLonCoordinate latlon);
    public LatLonCoordinate         mapDatumToLatLon(DatumCoordinate  md);
}
