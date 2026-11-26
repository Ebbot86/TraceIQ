package se.traceiq.app;
public class ThreatValuesTest {
 public static void main(String[] args){
  if(!ThreatValues.flag(null).equals("Okänt")||!ThreatValues.flag("false").equals("Okänt")||!ThreatValues.flag(true).equals("Indikation finns")||!ThreatValues.flag(false).equals("Ingen indikation i källan"))throw new AssertionError("flag");
  if(ThreatValues.number(null,100)!=null||ThreatValues.number(101,100)!=null||ThreatValues.number(-1,100)!=null||ThreatValues.number("0",100)!=null||ThreatValues.number(1.5,100)!=null||ThreatValues.number(0,100)!=0||ThreatValues.number(100,100)!=100)throw new AssertionError("score");
  java.util.Set<String> list=ThreatValues.parseExitList("# list\n8.8.8.8\n10.0.0.1\n<html>\n1.1.1.1\n");
  if(list.size()!=2||!ThreatValues.exitStatus(list,"8.8.8.8").startsWith("Match")||!ThreatValues.exitStatus(list,"9.9.9.9").startsWith("Ingen match")||!ThreatValues.exitStatus(list,"2001:4860::1").startsWith("Okänt")||!ThreatValues.exitStatus(java.util.Collections.emptySet(),"8.8.8.8").startsWith("Okänt"))throw new AssertionError("Tor");
  System.out.println("ThreatValues tests passed");
 }
}
