package se.traceiq.app;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.*;

final class ThreatIntel {
    private Set<String> exits;
    private long fetchedAt;
    static final class Result {
        String vpn="Okänt",proxy="Okänt",hosting="Okänt",tor="Okänt",abuser="Okänt",torList="Okänt";
        String ipapiStatus="API-nyckel saknas",abuseStatus="API-nyckel saknas";
        Integer score,reports;
    }
    Result check(String ip,String key,String abuseKey){
        Result r=new Result();
        try {
            long now=android.os.SystemClock.elapsedRealtime();
            if(exits==null||now-fetchedAt>=3600000L){Set<String> fresh=ThreatValues.parseExitList(request("https://check.torproject.org/torbulkexitlist",null));if(fresh.isEmpty())throw new IOException("Empty exit list");exits=fresh;fetchedAt=now;}
            r.torList=ThreatValues.exitStatus(exits,ip);
        }catch(Exception e){r.torList="Okänt – listan kunde inte hämtas";}
        if(!key.isEmpty()){
            try{
                JSONObject o=new JSONObject(request("https://api.ipapi.is/?q="+enc(ip)+"&key="+enc(key),null));
                if(!sameIp(ip,o.optString("ip")))throw new IOException("Invalid response");
                r.vpn=ThreatValues.flag(o.opt("is_vpn"));r.proxy=ThreatValues.flag(o.opt("is_proxy"));r.hosting=ThreatValues.flag(o.opt("is_datacenter"));r.tor=ThreatValues.flag(o.opt("is_tor"));r.abuser=ThreatValues.flag(o.opt("is_abuser"));
                r.ipapiStatus=(o.opt("is_vpn") instanceof Boolean||o.opt("is_proxy") instanceof Boolean||o.opt("is_tor") instanceof Boolean||o.opt("is_abuser") instanceof Boolean||o.opt("is_datacenter") instanceof Boolean)?"Svar mottaget":"Inga säkerhetsfält i svaret";
            }catch(Exception e){r.ipapiStatus="Otillgänglig – kontrollera nyckel, kvot eller nät";}
        }
        if(!abuseKey.isEmpty()){
            try{
                JSONObject o=new JSONObject(request("https://api.abuseipdb.com/api/v2/check?ipAddress="+enc(ip)+"&maxAgeInDays=90",abuseKey)).optJSONObject("data");
                if(o==null||!sameIp(ip,o.optString("ipAddress")))throw new IOException("Invalid response");
                r.score=ThreatValues.number(o.opt("abuseConfidenceScore"),100);r.reports=ThreatValues.number(o.opt("totalReports"),Integer.MAX_VALUE);
                r.abuseStatus=r.score==null?"Riskpoäng saknas i svaret":"Svar mottaget";
            }catch(Exception e){r.abuseStatus="Otillgänglig – kontrollera nyckel, kvot eller nät";}
        }
        return r;
    }
    private static boolean sameIp(String a,String b){byte[] x=IpAddress.parse(a),y=IpAddress.parse(b);return x!=null&&y!=null&&Arrays.equals(x,y);}
    private static String enc(String s)throws UnsupportedEncodingException{return URLEncoder.encode(s,"UTF-8");}
    private static String request(String address,String key)throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection();
        c.setConnectTimeout(6000);c.setReadTimeout(6000);c.setInstanceFollowRedirects(false);c.setRequestProperty("Accept",address.contains("torproject.org")?"text/plain":"application/json");c.setRequestProperty("User-Agent","TraceIQ-Android/1.4");
        if(key!=null)c.setRequestProperty("Key",key);
        try{if(c.getResponseCode()!=200)throw new IOException("HTTP error");try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){if(out.size()+n>1000000)throw new IOException("Response too large");out.write(b,0,n);}return out.toString("UTF-8");}}finally{c.disconnect();}
    }
}
