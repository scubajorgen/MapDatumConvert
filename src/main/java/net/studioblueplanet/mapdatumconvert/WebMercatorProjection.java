/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

/**
 * This class represents the Sperical Mercator projection 
 * @author jorgen
 */
public class WebMercatorProjection implements MapProjection
{
    Ellipsoid el=Ellipsoid.ELLIPSOID_WGS84;
    
    
    @Override
    public DatumCoordinate latLonToMapDatum(LatLonCoordinate latlon)
    {
        double          x;
        double          y;
        double          k0;
        double          r;
        DatumCoordinate dc;
        
        r           =el.a;
//        k0          =Math.cos(Math.toRadians(latlon.phi));
        k0          =1.0;
        x           =k0*Math.toRadians(latlon.lambda)*r;     // Easting
        y           =k0*Math.log(Math.tan(Math.PI/4.0+Math.toRadians(latlon.phi)/2.0))*r;
        
        dc=new DatumCoordinate();
        dc.easting  =x;
        dc.northing =y;
        
        return dc;
    }
    
    @Override
    public LatLonCoordinate mapDatumToLatLon(DatumCoordinate  md)
    {
        LatLonCoordinate    llc;
        double              lambda;
        double              phi;
        double              r;
        
        r           =el.a;        
        phi         =Math.toDegrees(Math.atan(Math.exp(md.northing / r)) * 2 - Math.PI/2);
        lambda      =Math.toDegrees(md.easting/r);
        llc         =new LatLonCoordinate();
        llc.lambda  =lambda;
        llc.phi     =phi;
        return llc;
    }
}
