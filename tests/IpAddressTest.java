package se.traceiq.app;
public class IpAddressTest {
 public static void main(String[] args) {
  String[] good={"8.8.8.8","1.1.1.1","2606:4700:4700::1111","2001:4860:4860::8888"};
  for(String s:good)if(!IpAddress.isPublic(IpAddress.parse(s)))throw new AssertionError(s);
  String[] bad={"localhost","example.com","https://8.8.8.8","8.8.8.256","010.1.2.3","127.0.0.1","10.0.0.1","192.168.1.1","100.64.0.1","198.51.100.1","203.0.113.1","::1","fc00::1","fe80::1","2001:db8::1","::ffff:192.168.0.1","8.8.8.8/24","1:2:3:4:5:6:7:8:9"};
  for(String s:bad)if(IpAddress.isPublic(IpAddress.parse(s)))throw new AssertionError(s);
  if(!IpAddress.ptr(IpAddress.parse("8.8.4.4")).equals("4.4.8.8.in-addr.arpa"))throw new AssertionError("IPv4 PTR");
  if(!IpAddress.ptr(IpAddress.parse("2001:4860::1")).startsWith("1.0.0.0."))throw new AssertionError("IPv6 PTR");
  System.out.println("IP validation and PTR tests passed: "+(good.length+bad.length+2));
 }
}
