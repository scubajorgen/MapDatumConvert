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
public class TransverseMercatorProjection implements MapProjection
{
    /**
     * Transverse Mercator projection parameters
     */
    public static class TmProjectionParameters
    {
        public Ellipsoid    el;         // Ellipse to use
        public double       F0;         // Scaling of axis
        public double       phi0;       // Center latitude in degrees
        public double       lambda0;    // Center longitude in degrees
        public double       phi0Rad;    // Center latitude in radians
        public double       lambda0Rad; // Center longitude in radians
        public double       X0;         // Norhting constant in m
        public double       Y0;         // Easting constant in m
        
        /**
         * Constructor
         * @param el
         * @param F0
         * @param phi0
         * @param lambda0
         * @param X0
         * @param Y0 
         */
        public TmProjectionParameters(Ellipsoid el, double F0, double phi0, double lambda0, double X0, double Y0)
        {
            this.el         =el;
            this.F0         =F0;
            this.phi0       =phi0;
            this.lambda0    =lambda0;
            this.X0         =X0;
            this.Y0         =Y0;
            this.phi0Rad    =phi0/180.0*Math.PI;
            this.lambda0Rad =lambda0/180.0*Math.PI;
        }
    }
    
    public  final static TmProjectionParameters BRITISH_NATIONAL_GRID=
            new TmProjectionParameters(Ellipsoid.ELLIPSOID_AIRY, 0.9996012717, 49.0, -2.0, -100000, 400000);
    
    private TmProjectionParameters p;
   
    
    /**
     * Constructor
     * @param p Projection parameters to use
     */
    public TransverseMercatorProjection(TmProjectionParameters p)
    {
        this.p=p;
    }
    
    /**
     * Converts lat lon to northing (x) and easting (y) according to the
     * transverse mercator map projection
     * https://www.icao.int/safety/pbn/Documentation/EUROCONTROL/Eurocontrol%20WGS%2084%20Implementation%20Manual.pdf
     * Appendix G
     * and 'The ellipsoid and the Transverse Mercator Projection' 8.2
     * @param latLon Latitude/Longitude coordinate
     * @return The x,y coordinate in m
     */
    public DatumCoordinate latLonToMapDatum2(LatLonCoordinate latLon)
    {
        DatumCoordinate    tm;
        double          X;
        double          Y;
        double          nu;
        double          rho;
        double          phiRad;
        double          lambdaRad;
        double          eta2;
        double          A,B,C,D,E,F;
        double          M, Ma, Mb, Mc, Md;
        double          n;
        double          P;
     

        phiRad      =latLon.phi/180.0*Math.PI;
        lambdaRad   =latLon.lambda/180.0*Math.PI;
        nu          =p.el.a*p.F0/Math.sqrt(1-p.el.e2*Math.pow(Math.sin(phiRad), 2.0));        // N = nu = a/W
        rho         =p.el.a*p.F0*(1-p.el.e2)/Math.pow(1-p.el.e2*Math.pow(Math.sin(phiRad), 2.0), 1.5);
        eta2        =nu/rho-1.0;
        A           =nu/  2*Math.sin(phiRad)*Math.cos(phiRad); // II
        B           =nu/ 24*Math.sin(phiRad)*Math.pow(Math.cos(phiRad),3.0)*( 5-   Math.pow(Math.tan(phiRad),2.0)+9*eta2); // III
        C           =nu/720*Math.sin(phiRad)*Math.pow(Math.cos(phiRad),5.0)*(61-58*Math.pow(Math.tan(phiRad),2.0)+
                                                    Math.pow(Math.tan(phiRad), 4.0)); // IIIA
        D           =nu    *Math.cos(phiRad); // IV
        E           =nu/6  *Math.pow(Math.cos(phiRad),3.0)*(nu/rho-Math.pow(Math.tan(phiRad), 2.0)); // V
        F           =nu/120*Math.pow(Math.cos(phiRad),5.0)*(5.0-18*Math.pow(Math.tan(phiRad), 2.0)+Math.pow(Math.tan(phiRad), 4.0)+
                                                    14*eta2-58*Math.pow(Math.tan(phiRad), 2.0)*eta2); // VI
        
        n   =p.el.n;
        Ma  =(1+n+5/4*n*n+5/4*n*n*n)*(phiRad-p.phi0Rad);
        Mb  =(3*n+3*n*n+21/8*n*n*n)*Math.sin(phiRad-p.phi0Rad)*Math.cos(phiRad+p.phi0Rad);
        Mc  =(15.0/8.0*n*n+15.0/8.0*n*n*n)*Math.sin(2.0*(phiRad-p.phi0Rad))*Math.cos(2.0*(phiRad+p.phi0Rad));
        Md  =(25.0/24.0*n*n*n)*Math.sin(3.0*(phiRad-p.phi0Rad))*Math.cos(3.0*(phiRad+p.phi0Rad)); // 25.0=35.0?
        M   =p.F0*p.el.b*(Ma-Mb+Mc-Md);
        
        P       =lambdaRad-p.lambda0Rad;
        X       =p.X0+M+Math.pow(P,2.0)*A+Math.pow(P,4.0)*B+Math.pow(P,6.0)*C;
        Y       =p.Y0+P*D+Math.pow(P, 3.0)*E+Math.pow(P, 5.0)*F; // Easting
  
        tm=new DatumCoordinate(X, Y);
        return tm;
    }

    /**
     * Converts lat lon to northing (X) and easting (Y) according to the
     * transverse mercator map projection ESPG method according to
     * OGP Surveying and Positioning Guidance Note number 7, part 2 – May 2009
     * @param lat Latitude in degrees
     * @param lon Longitude in degrees
     * @return The x,y coordinate in m
     */
    @Override
    public DatumCoordinate latLonToMapDatum(LatLonCoordinate latLon)
    {
        DatumCoordinate    tm;
        double          X;
        double          Y;
        double          phiRad;
        double          lambdaRad;
        double          nu;
        double          T;
        double          C;
        double          A;
        double          M;
        double          M0;
        double          e2;

     
        phiRad      =latLon.phi/180.0*Math.PI;     // NS
        lambdaRad   =latLon.lambda/180.0*Math.PI;  // EW
        
        e2          =p.el.e2;
        nu          =p.el.a/Math.sqrt(1-e2*Math.pow(Math.sin(phiRad), 2.0));        // N = nu = a/W
        T           =Math.tan(phiRad)*Math.tan(phiRad);
        C           =e2*Math.pow(Math.cos(phiRad), 2.0)/(1-e2);
        A           =(lambdaRad-p.lambda0Rad)*Math.cos(phiRad);
        M           =p.el.a*
                     ((1-e2/4-3*e2*e2/64-5*e2*e2*e2/256)*(phiRad-p.phi0Rad)-
                      (3*e2/8+3*e2*e2/32+45*e2*e2*e2/1024)*(Math.sin(2*phiRad)-Math.sin(2*p.phi0Rad))+
                      (15*e2*e2/256+45*e2*e2*e2/1024)*(Math.sin(4*phiRad)-Math.sin(4*p.phi0Rad))-
                      (35*e2*e2*e2/3072)*(Math.sin(6*phiRad)-Math.sin(6*p.phi0Rad)));  // M - M-M0
        
        X       =p.X0+p.F0*(M+nu*Math.tan(phiRad)*((A*A/2)+(5-T+9*C+4*C*C)*A*A*A*A/24+
                                                   (61-58*T+T*T+600*C-330*e2/(1-e2))*Math.pow(A, 6)/720));      // Northing
        Y       =p.Y0+p.F0*nu*(A+(1-T+C)*A*A*A/6+(5-18*T+T*T+72*C-58*e2/(1-e2))*Math.pow(A, 5)/120); // Easting
        
        tm=new DatumCoordinate(X, Y);
        return tm;
    }
    
    /**
     * Converts x,y to lat,lon according to the transverse mercator projection
     * @param x Northing
     * @param y Easting
     * @return The WGS84 lat lon coordinate
     */
    @Override
    public LatLonCoordinate mapDatumToLatLon(DatumCoordinate dc)
    {
        LatLonCoordinate    ll;
        double              lat;
        double              lon;
        
        lat=0;
        lon=0;
        ll=new LatLonCoordinate(lat, lon);
        return ll;
    }
}
