/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

/**
 * This class represents the Sperical Mercator projection EPSG:3857.
 * This projection takes in WGS84 elliptical latitude/longitude coordinates
 * but treats them like spherical. This results in less calculation at the
 * expense of pretty large projection errors.
 * @author jorgen
 */
public class WebMercatorProjection implements MapProjection
{
    private final Ellipsoid     el=Ellipsoid.ELLIPSOID_WGS84;
    private final double        lambda0;
    
    /**
     * Constructor
     * @param lambda0 Longitude of the origin (central meridian) in degrees
     */
    public WebMercatorProjection(double lambda0)
    {
        this.lambda0=lambda0;
    }
    
    @Override
    public DatumCoordinate latLonToMapDatum(LatLonCoordinate latlon)
    {
        double          x;
        double          y;
        double          a;
        DatumCoordinate dc;
        
        a           =el.a;
        x           =Math.toRadians(latlon.lambda-lambda0)*a;     // Easting
        y           =Math.log(Math.tan(Math.PI/4.0+Math.toRadians(latlon.phi)/2.0))*a;
        
        dc=new DatumCoordinate();
        dc.easting  =x;
        dc.northing =y;
        
        return dc;
    }
    
    @Override
    public LatLonCoordinate mapDatumToLatLon(DatumCoordinate  md)
    {
        LatLonCoordinate    llc;
        double              phiRad;
        double              a;
        
        a           =el.a;        
        phiRad      =Math.atan(Math.exp(md.northing / a)) * 2 - Math.PI/2;
        llc         =new LatLonCoordinate();
        llc.lambda  =Math.toDegrees(md.easting/a)+lambda0;
        llc.phi     =Math.toDegrees(phiRad);
        return llc;
    }
}
