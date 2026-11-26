package se.traceiq.app;
public class GeoMapTest {
 public static void main(String[] args){
  if(GeoMap.valid(Double.NaN,0)||GeoMap.valid(91,0)||GeoMap.valid(0,181)||GeoMap.valid(0,Double.POSITIVE_INFINITY))throw new AssertionError("invalid");
  GeoMap m=new GeoMap(59.3293,18.0686);
  if(!m.coordinates().equals("59.3, 18.1"))throw new AssertionError("rounding");
  if(!m.embed().contains("marker=59.300%2C18.100"))throw new AssertionError("marker");
  if(!m.google().endsWith("59.300%2C18.100"))throw new AssertionError("google");
  if(!m.satellite().endsWith("zoom=9&basemap=satellite"))throw new AssertionError("satellite");
  if(!new GeoMap(-85,-180).embed().contains("bbox=-180.000%2C-85.051"))throw new AssertionError("boundary");
  if(!new GeoMap(0,0).coordinates().equals("0.0, 0.0"))throw new AssertionError("zero");
  System.out.println("GeoMap tests passed");
 }
}
