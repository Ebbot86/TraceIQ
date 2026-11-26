package se.traceiq.app;
import java.util.Locale;
final class GeoMap {
    final double lat,lon;
    static boolean valid(double lat,double lon){return Double.isFinite(lat)&&Double.isFinite(lon)&&Math.abs(lat)<=85&&Math.abs(lon)<=180;}
    GeoMap(double lat,double lon){if(!valid(lat,lon))throw new IllegalArgumentException("Invalid coordinates");this.lat=Math.round(lat*10)/10.0;this.lon=Math.round(lon*10)/10.0;}
    private static String num(double d){return String.format(Locale.US,"%.3f",d);}
    String coordinates(){return String.format(Locale.US,"%.1f, %.1f",lat,lon);}
    private String pair(){return num(lat)+"%2C"+num(lon);}
    String embed(){double dx=Math.min(10,0.65/Math.max(0.1,Math.cos(Math.toRadians(lat))));double west=Math.max(-180,lon-dx),east=Math.min(180,lon+dx),south=Math.max(-85.051,lat-0.45),north=Math.min(85.051,lat+0.45);return "https://www.openstreetmap.org/export/embed.html?bbox="+num(west)+"%2C"+num(south)+"%2C"+num(east)+"%2C"+num(north)+"&layer=mapnik&marker="+pair();}
    String google(){return "https://www.google.com/maps/search/?api=1&query="+pair();}
    String satellite(){return "https://www.google.com/maps/@?api=1&map_action=map&center="+pair()+"&zoom=9&basemap=satellite";}
}
