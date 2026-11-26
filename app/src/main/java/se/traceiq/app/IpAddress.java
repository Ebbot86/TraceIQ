package se.traceiq.app;
import java.net.InetAddress;
final class IpAddress {
    static byte[] parse(String s) {
        if (s == null || s.length() > 45) return null;
        if (s.indexOf(':') >= 0) {
            if (!s.matches("[0-9a-fA-F:.]+")) return null;
            try { return InetAddress.getByName(s).getAddress(); } catch(Exception e) { return null; }
        }
        String[] parts=s.split("\\.",-1); if(parts.length!=4) return null;
        byte[] out=new byte[4];
        for(int i=0;i<4;i++){ if(!parts[i].matches("0|[1-9][0-9]{0,2}"))return null; int n=Integer.parseInt(parts[i]); if(n>255)return null; out[i]=(byte)n; }
        return out;
    }
    static boolean isPublic(byte[] b) {
        if(b==null)return false;
        int a=b[0]&255,c=b[1]&255,d=b[2]&255;
        if(b.length==4) return !(a==0||a==10||a==127||a>=224||(a==100&&c>=64&&c<=127)||(a==169&&c==254)||(a==172&&c>=16&&c<=31)||(a==192&&(c==168||(c==0&&(d==0||d==2))||(c==88&&d==99)))||(a==198&&(c==18||c==19||(c==51&&d==100)))||(a==203&&c==0&&d==113));
        if(b.length!=16 || (a&0xe0)!=0x20)return false;
        if(a==0x20&&c==0x01&&d==0x0d&&(b[3]&255)==0xb8)return false;
        if(a==0x3f&&(c&0xf0)==0xf0)return false;
        return true;
    }
    static String ptr(byte[] b) {
        StringBuilder s=new StringBuilder();
        if(b.length==4){for(int i=3;i>=0;i--)s.append(b[i]&255).append('.'); return s+"in-addr.arpa";}
        for(int i=15;i>=0;i--){int n=b[i]&255;s.append(Integer.toHexString(n&15)).append('.').append(Integer.toHexString(n>>4)).append('.');}return s+"ip6.arpa";
    }
}
