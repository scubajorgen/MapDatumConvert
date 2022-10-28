/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This view presents and compars the outline of the Netherlands 
 * assuming two projections.
 * @author jorgen
 */
public class MainView extends javax.swing.JFrame
{
    public static class Polygon
    {
        List<LatLonCoordinate> coordinates=new ArrayList<>();
    }
    
    // Martinitoren
    private static final LatLonCoordinate   MARTINI =new LatLonCoordinate(53.2193683, 6.56827);

    // OLV Amersfoort
    private static final LatLonCoordinate   OLV     =new LatLonCoordinate(52.1551722, 5.3872035);    
    
    private final   LatLonCoordinate        topLeftLL    =new LatLonCoordinate(53.80, 3.10);
    private final   LatLonCoordinate        bottomRightLL=new LatLonCoordinate(50.70, 7.30);
    private         DatumCoordinate         topLeft;
    private         DatumCoordinate         bottomRight;
    private int                             topLeftX;
    private int                             topLeftY;
    private int                             bottomRightX;
    private int                             bottomRightY;
    private double                          meterPerPixel;
    
    private List<Polygon> polygons=new ArrayList<>();    
   
    /**
     * Creates new form MainView
     */
    public MainView()
    {
        readMap();
        initComponents();
    }

    /**
     * Reads the outline of the Netherlands from JSON file as a series of
     * polygons consisting of WGS84 coordinates.
     */
    private void readMap()
    {
        double lat;
        double lon;
        
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader("landsdeel_WGS84.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            JSONObject map = (JSONObject) obj;
            
            JSONArray parts=(JSONArray)map.get("features");
            for (Object part : parts)
            {
                JSONObject geometry=(JSONObject)((JSONObject)part).get("geometry");
                JSONArray pgons=(JSONArray)geometry.get("coordinates");
                for (Object pgon : pgons)
                {
                    for (Object exterior : (JSONArray)pgon)
                    {
                        Polygon polygon=new Polygon();
                        for (Object coordinate : (JSONArray)exterior)
                        {
                            lon=Double.parseDouble(((JSONArray)coordinate).get(0).toString());
                            lat=Double.parseDouble(((JSONArray)coordinate).get(1).toString());
                            LatLonCoordinate llc=new LatLonCoordinate(lat, lon);
                            polygon.coordinates.add(llc);
                        }
                        polygons.add(polygon);
                    }
                }
            }
            System.out.println("Done reading map");
        } 
        catch (IOException | ParseException e) 
        {
            System.err.println(e.getMessage());
        }
    }
    
    /**
     * Draws the outline map.
     * @param g Graphics to use
     * @param projection Projection to use.
     */
    private void drawMap(Graphics g, MapProjection projection)
    {
        LatLonCoordinate    c;
        LatLonCoordinate    prevC;
        DatumCoordinate     d;
        DatumCoordinate     prevD;
        double              x1, y1, x2, y2;

        for (Polygon p : polygons)
        {
            c       =p.coordinates.get(0);
            prevD   =projection.latLonToMapDatum(c);
            for(int i=1; i<p.coordinates.size(); i++)
            {
                c   =p.coordinates.get(i);
                d   =projection.latLonToMapDatum(c);
                x1  =topLeftX+(d.easting-topLeft.easting)/meterPerPixel;
                y1  =topLeftY+(topLeft.northing-d.northing)/meterPerPixel;
                x2  =topLeftX+(prevD.easting-topLeft.easting)/meterPerPixel;
                y2  =topLeftY+(topLeft.northing-prevD.northing)/meterPerPixel;
                g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
                prevD=d;
            }
        }
    }
    
    /**
     * Draws a reference point
     * @param g Graphics to use
     * @param projection Projection to use
     * @param llc Coordinate of the reference point
     */
    private DatumCoordinate drawReferencePoint(Graphics g, MapProjection projection, LatLonCoordinate llc)
    {
        DatumCoordinate dc;
        double          x;
        double          y;

        
        dc=projection.latLonToMapDatum(llc);
        x=topLeftX+(dc.easting -topLeft.easting )/meterPerPixel;
        y=topLeftY+(topLeft.northing-dc.northing)/meterPerPixel;
        g.fillOval((int)x, (int)y, 10, 10);    

        System.out.println("Ref1 "+dc.easting+" "+dc.northing);
        return dc;
    }
    
    /**
     * This method calibrates the map canvas parameters. It uses the top left
     * coordinate and bottom right coordinate and fit them to the canvas.
     * @param projection Projection on which the calibration must take place
     */
    private void calibrate(MapProjection projection)
    {
        Dimension d;
        double          dxPixel;
        double          dyPixel;
        
        d           =this.getSize();
        topLeftX    =0;
        topLeftY    =0;
        bottomRightX=d.width;
        bottomRightY=d.height;        
        topLeft     =projection.latLonToMapDatum(topLeftLL);
        bottomRight =projection.latLonToMapDatum(bottomRightLL);
        dxPixel=(bottomRight.easting-topLeft.easting     )/(bottomRightX-topLeftX);
        dyPixel=(topLeft.northing   -bottomRight.northing)/(bottomRightY-topLeftY);
        meterPerPixel=Math.max(dxPixel, dyPixel);
        
    }
    
    /**
     * Comparison between mercator and Web mercator
     * @param g Graphics to use
     */
    private void mercatorVsWebMercator(Graphics g)
    {
        MapProjection   projection;
        DatumCoordinate dc1, dc2;
        
        projection           =new MercatorProjection(5.3872035);
        calibrate(projection);
        
        g.setColor(Color.red);
        drawMap(g, projection);
        drawReferencePoint(g, projection, OLV);
        dc1=drawReferencePoint(g, projection, MARTINI);

        projection           =new WebMercatorProjection(5.3872035);
        g.setColor(Color.blue);
        drawMap(g, projection);
        drawReferencePoint(g, projection, OLV);
        dc2=drawReferencePoint(g, projection, MARTINI);
        
        System.out.println("Diff "+Math.sqrt(Math.pow((dc2.easting-dc1.easting), 2.0)+Math.pow((dc2.northing-dc1.northing), 2.0))+" meter");
    }

    /**
     * Comparison between Stereographic and Transverse Mercator.
     * The calculation radius has been chosen as fit parameter
     * @param g Graphics to use
     */
    private void stereographicVsTransverseMercator(Graphics g)
    {
        MapProjection   projection;
        DatumCoordinate dc1, dc2;

        projection           =          new StereographicProjection(Ellipsoid.ELLIPSOID_WGS84, 
                                        52.1551722, 5.3872035, 
                                        0.9999079, 
                                        6383300, 
                                        0, 0, 
                                        -0.113);
        
        calibrate(projection);
        
        g.setColor(Color.red);
        drawReferencePoint(g, projection, OLV);
        dc1=drawReferencePoint(g, projection, MARTINI);
        drawMap(g, projection);

        projection           =new TransverseMercatorProjection(Ellipsoid.ELLIPSOID_WGS84, 
                                        0.9999079,
                                        52.1551722, 5.3872035, 
                                        0, 0);
        g.setColor(Color.blue);
        drawReferencePoint(g, projection, OLV);
        dc2=drawReferencePoint(g, projection, MARTINI);
        drawMap(g, projection);

        System.out.println("Diff "+Math.sqrt(Math.pow((dc2.easting-dc1.easting), 2.0)+Math.pow((dc2.northing-dc1.northing), 2.0))+" meter");
    }

    /**
     * The paint() method responsible for painting the canvas
     * @param g Graphics of the canvas
     */
    @Override
    public void paint(Graphics g)
    {
        stereographicVsTransverseMercator(g);
//        mercatorVsWebMercator(g);
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1200, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
