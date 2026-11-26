package se.traceiq.app;
import java.util.*;
final class ThreatValues {
 static String flag(Object o){return !(o instanceof Boolean)?"Okänt":Boolean.TRUE.equals(o)?"Indikation finns":"Ingen indikation i källan";}
 static Integer number(Object o,int max){if(!(o instanceof Number))return null;double x=((Number)o).doubleValue();return Double.isFinite(x)&&x>=0&&x<=max&&x==Math.floor(x)?(int)x:null;}
 static Set<String> parseExitList(String raw){Set<String> s=new HashSet<>();for(String line:raw.split("\\r?\\n")){String ip=line.trim();byte[] b=IpAddress.parse(ip);if(b!=null&&b.length==4&&IpAddress.isPublic(b))s.add(ip);}return s;}
 static String exitStatus(Set<String> list,String ip){byte[] b=IpAddress.parse(ip);if(list==null||list.isEmpty()||b==null||b.length!=4)return "Okänt – ingen IPv4-kontroll";return list.contains(ip)?"Match i exitlistan":"Ingen match i exitlistan";}
}
