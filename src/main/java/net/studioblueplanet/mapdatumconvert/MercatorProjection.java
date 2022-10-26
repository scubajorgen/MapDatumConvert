/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

/**
 * This class implements the ellipsoidal Mercator Projection. 
 * EPSG:42310 (tested with ESRI:54004)
 * @author jorgen
 */
public class MercatorProjection implements MapProjection
{
    private final Ellipsoid el          =Ellipsoid.ELLIPSOID_WGS84;
    private final double    lambda0;    
    
    /**
     * Constructor
     * @param lambda0 Longitude of the origin (central meridian) in degrees
     */
    public MercatorProjection(double lambda0)
    {
        this.lambda0=lambda0;
    }
    
    
    @Override
    public DatumCoordinate latLonToMapDatum(LatLonCoordinate latlon)
    {
        double          x;
        double          y;
        double          a;
        double          e;
        double          phiRad;
        double          lambdaRad;
        DatumCoordinate dc;
        
        phiRad      =Math.toRadians(latlon.phi);
        lambdaRad   =Math.toRadians(latlon.lambda);
        a           =el.a;
        e           =el.e;
        x           =Math.toRadians(latlon.lambda-lambda0)*a;     // Easting
        y           =Math.log(Math.tan(Math.PI/4.0+phiRad/2.0)*Math.pow((1-e*Math.sin(phiRad))/(1+e*Math.sin(phiRad)), e/2.0))*a;
        
        dc          =new DatumCoordinate();
        dc.easting  =x;
        dc.northing =y;
        
        return dc;        
    }
    
    @Override
    public LatLonCoordinate mapDatumToLatLon(DatumCoordinate  md)
    {
        LatLonCoordinate    llc;
        double              lambda;
        double              phiRad;
        double              a;
        double              e;
        double              t;
        
        a           =el.a;       
        e           =el.e;
        t           =Math.exp(-md.northing/a);
        
        phiRad      =Math.PI/2.0-2.0*Math.atan(t);
        for(int i=0; i<4; i++)
        {
            phiRad  =Math.PI/2.0-2.0*Math.atan(t*Math.pow((1-e*Math.sin(phiRad))/(1+e*Math.sin(phiRad)), e/2.0));
        }
        llc         =new LatLonCoordinate();
        llc.lambda  =Math.toDegrees(md.easting/a)+lambda0;
        llc.phi     =Math.toDegrees(phiRad);
        return llc;        
    }
}
