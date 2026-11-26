package se.traceiq.app;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.net.Uri;
import android.webkit.*;
import android.content.*;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
public class MainActivity extends Activity {
    private final int BG=0xff020c19, PANEL=0xff082237, BORDER=0xff087ba5, TEXT=0xffedf8ff, MUTED=0xffa3c4d7, CYAN=0xff22e3ff;
    private LinearLayout root, results; private EditText input; private TextView status; private Button lookup, own;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private WebView mapView;
    private LinearLayout securityPanel;
    private String ipapiKey="", abuseKey="";
    private final ThreatIntel intel=new ThreatIntel();
    private final ScrollView[] pages=new ScrollView[5];
    private final TextView[] tabs=new TextView[5];
    private android.widget.FrameLayout pageHost;
    private LinearLayout historyList;
    private TextView homeSummary;
    private final java.util.ArrayList<String> sessionHistory=new java.util.ArrayList<>();
    private int currentPage=0;
    private String lastIp="";
    private final String[] tabNames={"Hem","Spåra IP","Verktyg","Historik","Mer"};
    private String report=""; private int generation=0; private boolean busy=false;
    private int dp(float x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
    private GradientDrawable bg(int color,int stroke,int radius){GradientDrawable d=new GradientDrawable();if(color==PANEL)d.setColors(new int[]{0xff0b304a,0xff041523});else if(color==CYAN)d.setColors(new int[]{0xff39f1ff,0xff00a8e8});else d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private void add(LinearLayout parent,View v,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(top);parent.addView(v,p);}
    private Button button(String s,boolean primary){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(15);b.setTextColor(primary?BG:TEXT);b.setBackground(bg(primary?CYAN:PANEL,primary?0:BORDER,12));b.setMinHeight(dp(48));return b;}
    @Override public void onCreate(Bundle state){super.onCreate(state);
        LinearLayout shell=column();shell.setBackgroundColor(BG);
        LinearLayout header=column();header.setPadding(dp(20),dp(10),dp(20),dp(12));header.setBackgroundColor(BG);
        add(header,text("◈  T R A C E I Q",27,CYAN,true),0);add(header,text("IP INTELLIGENCE  ·  TRACK • ANALYZE • PROTECT",10,MUTED,true),3);
        shell.addView(header,new LinearLayout.LayoutParams(-1,-2));
        pageHost=new android.widget.FrameLayout(this);shell.addView(pageHost,new LinearLayout.LayoutParams(-1,0,1));
        for(int i=0;i<pages.length;i++){pages[i]=new ScrollView(this);pages[i].setFillViewport(true);pages[i].setBackgroundColor(BG);pages[i].setVisibility(View.GONE);pageHost.addView(pages[i],new android.widget.FrameLayout.LayoutParams(-1,-1));}
        root=pageContent(0);
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);nav.setPadding(dp(4),dp(6),dp(4),dp(6));nav.setBackgroundColor(PANEL);
        String[] glyph={"⌂","◎","⚙","◷","⋯"};
        for(int i=0;i<tabs.length;i++){final int index=i;TextView tab=text(glyph[i]+"\n"+tabNames[i],12,MUTED,true);tab.setGravity(Gravity.CENTER);tab.setMinHeight(dp(56));tab.setContentDescription(tabNames[i]);tab.setFocusable(true);tab.setOnClickListener(v->showPage(index));tabs[i]=tab;nav.addView(tab,new LinearLayout.LayoutParams(0,-2,1));}
        shell.addView(nav,new LinearLayout.LayoutParams(-1,-2));setContentView(shell);
        CyberHero hero=new CyberHero(this);root.addView(hero,new LinearLayout.LayoutParams(-1,dp(210)));add(root,text("GLOBAL IP SEARCH",12,CYAN,true),10);
        input=new EditText(this);input.setSingleLine(true);input.setTextColor(TEXT);input.setHintTextColor(MUTED);input.setHint("IP-adress, t.ex. 8.8.8.8");input.setTextSize(16);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);input.setPadding(dp(14),dp(13),dp(14),dp(13));input.setBackground(bg(PANEL,BORDER,12));input.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);LinearLayout search=new LinearLayout(this);search.setOrientation(LinearLayout.HORIZONTAL);search.setGravity(Gravity.CENTER_VERTICAL);add(root,search,8);search.addView(input,new LinearLayout.LayoutParams(0,dp(52),1));
        lookup=button("Analysera",true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(112),dp(52));lp.leftMargin=dp(6);search.addView(lookup,lp);LinearLayout quick=new LinearLayout(this);quick.setOrientation(LinearLayout.HORIZONTAL);add(root,quick,8);own=button("◎ Min IP",false);quick.addView(own,new LinearLayout.LayoutParams(0,dp(48),1));Button map=button("⌖ Visa på karta",false);LinearLayout.LayoutParams qp=new LinearLayout.LayoutParams(0,dp(48),1);qp.leftMargin=dp(8);quick.addView(map,qp);map.setOnClickListener(v->{if(mapView!=null)scrollTo(mapView);else if(!busy)start(false);});buildHome();
        lookup.setOnClickListener(v->start(false));own.setOnClickListener(v->start(true));input.setOnEditorActionListener((v,a,e)->{if(a==android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH){start(false);return true;}return false;});
        status=text("●  REDO",13,CYAN,true);add(root,status,20);results=column();add(root,results,8);showIntro();
        TextView privacy=text("Integritet & datakällor",14,CYAN,true);privacy.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Integritet").setMessage("Sökningar skickas över HTTPS till ipwho.is. Om en publik IP hittas skickas den även till RIPEstat och en omvänd DNS-fråga till Google Public DNS. Kartan laddas från OpenStreetMap med avrundade koordinater. Om du öppnar Google Maps skickas koordinaterna dit. Tor Project tillhandahåller en publik exitlista; den sökta IP-adressen skickas inte dit. Med egna API-nycklar skickas sökt IP även till ipapi.is och AbuseIPDB. Leverantörerna kan se din anslutnings IP-adress. Appen begär inte GPS, sparar ingen sökhistorik på disk och har inga konton. IP-geolokalisering är ungefärlig och identifierar inte en person. Gratis API-villkor måste kontrolleras före kommersiell distribution.").setPositiveButton("Stäng",null).show());add(root,privacy,22);
        buildTools();buildHistory();buildSettings();showPage(0);
    }

    private LinearLayout pageContent(int index){LinearLayout c=column();c.setPadding(dp(12),dp(8),dp(12),dp(24));pages[index].addView(c);return c;}
    private void showPage(int index){
        if(index!=currentPage&&input!=null){InputMethodManager im=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);if(im!=null)im.hideSoftInputFromWindow(input.getWindowToken(),0);input.clearFocus();}
        currentPage=index;
        for(int i=0;i<pages.length;i++){boolean active=i==index;pages[i].setVisibility(i==(index==1?0:index)?View.VISIBLE:View.GONE);tabs[i].setTextColor(active?CYAN:MUTED);tabs[i].setBackground(active?bg(0xff06446a,CYAN,10):null);tabs[i].setSelected(active);}
        if(index==0)pages[0].post(()->pages[0].smoothScrollTo(0,0));else if(index==1)scrollTo(status==null?input:status);
    }
    private LinearLayout surface(LinearLayout parent,String heading,int top){LinearLayout c=column();c.setPadding(dp(16),dp(15),dp(16),dp(15));c.setBackground(bg(PANEL,BORDER,16));if(!heading.isEmpty())add(c,text(heading,12,CYAN,true),0);add(parent,c,top);return c;}
    private void pageTitle(LinearLayout p,String heading,String desc){add(p,text(heading,25,TEXT,true),0);add(p,text(desc,14,MUTED,false),5);}
    private void action(LinearLayout p,String title,boolean primary,Runnable run){Button b=button(title,primary);add(p,b,10);b.setOnClickListener(v->run.run());}
    private void tileRow(LinearLayout parent,String a,String ad,Runnable ar,String b,String bd,Runnable br){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.topMargin=dp(10);parent.addView(row,rp);tile(row,a,ad,ar,false);tile(row,b,bd,br,true);}
    private void tile(LinearLayout row,String title,String desc,Runnable run,boolean second){LinearLayout c=column();c.setPadding(dp(10),dp(10),dp(10),dp(10));c.setBackground(bg(PANEL,BORDER,14));c.setMinimumHeight(dp(88));c.setFocusable(true);c.setClickable(true);c.setContentDescription(title+", "+desc);add(c,text(title,14,CYAN,true),0);add(c,text(desc,11,TEXT,false),5);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);if(second)p.leftMargin=dp(10);row.addView(c,p);c.setOnClickListener(v->run.run());}

    private void scrollTo(View v){if(v==null)return;pages[0].post(()->{int y=0;View p=v;while(p!=root&&p!=null){y+=p.getTop();if(!(p.getParent() instanceof View))break;p=(View)p.getParent();}pages[0].smoothScrollTo(0,Math.max(0,y-dp(8)));});}
    private void planned(String name){new AlertDialog.Builder(this).setTitle(name).setMessage("Planerad funktion. Ingen analys eller riskbedömning utförs i denna version.").setPositiveButton("Stäng",null).show();}
    private void buildHome(){LinearLayout p=root;
        add(p,text("INTELLIGENCE TOOLS",12,MUTED,true),16);
        tileRow(p,"⌖ IP Lookup","Spåra IP & plats",()->scrollTo(input),"◈ Hotkontroll","Tor & missbruksrapporter",()->openSecurity());
        tileRow(p,"◈ VPN / Proxy","Databasindikationer",()->openSecurity(),"⌘ Nätverksskanner","Planerad",()->planned("Nätverksskanner"));
        tileRow(p,"▤ ASN / Nätverk","Operatör & IP-prefix",()->scrollTo(status),"◎ Omvänd DNS","PTR-värdnamn",()->scrollTo(status));
        tileRow(p,"↗ Live Ping","Planerad",()->planned("Live Ping"),"▧ Rapport","Dela aktuell textanalys",()->{if(report.isEmpty()){Toast.makeText(this,"Gör en IP-analys först",Toast.LENGTH_SHORT).show();return;}Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,report);startActivity(Intent.createChooser(i,"Dela analys"));});
    }
    private void buildTools(){LinearLayout p=pageContent(2);pageTitle(p,"Verktyg","Tillgängliga funktioner i version 1.4.0.");
        LinearLayout c=surface(p,"IP INTELLIGENCE",18);add(c,text("Geo Tracker, nätverksägare, ASN, routingprefix och omvänd DNS ingår i samma analys.",15,TEXT,false),10);action(c,"Öppna IP-analys",true,()->showPage(1));action(c,"Kontrollera min publika IP",false,()->{showPage(1);start(true);});
        c=surface(p,"SECURITY INTELLIGENCE",12);add(c,text("Tor-lista, VPN/proxy och missbruksrapporter. Vissa kontroller kräver egna API-nycklar.",14,TEXT,false),8);action(c,"Öppna säkerhetsanalys",false,()->openSecurity());action(c,"Ange API-nycklar",false,()->keyDialog());
        c=surface(p,"RAPPORTER",12);add(c,text("Kopiera eller dela resultatet som text efter en slutförd analys.",14,MUTED,false),10);action(c,"Öppna aktuell analys",false,()->showPage(1));
        c=surface(p,"PLANERADE VERKTYG",12);add(c,text("Ping, traceroute och WHOIS/RDAP. Inte aktiverade ännu.",14,MUTED,false),10);
    }
    private void buildHistory(){LinearLayout p=pageContent(3);pageTitle(p,"Historik","Tillfälliga IP-sökningar. Inget sparas på disk.");action(p,"Rensa sessionshistorik",false,()->{sessionHistory.clear();refreshHistory();});historyList=column();add(p,historyList,14);refreshHistory();}
    private void remember(String ip){lastIp=ip;sessionHistory.remove(ip);sessionHistory.add(0,ip);while(sessionHistory.size()>20)sessionHistory.remove(sessionHistory.size()-1);if(homeSummary!=null)homeSummary.setText(ip);refreshHistory();}
    private void refreshHistory(){if(historyList==null)return;historyList.removeAllViews();if(sessionHistory.isEmpty()){add(historyList,text("Inga tidigare sökningar i denna session.",14,MUTED,false),4);return;}for(String ip:sessionHistory){action(historyList,ip,false,()->{if(busy){Toast.makeText(this,"En analys pågår redan",Toast.LENGTH_SHORT).show();return;}showPage(1);input.setText(ip);start(false);});}}
    private void buildSettings(){LinearLayout p=pageContent(4);pageTitle(p,"Mer","Appinformation och integritet.");
        LinearLayout c=surface(p,"TRACEIQ",18);add(c,text("Version 1.4.0 · Android testversion",16,TEXT,true),8);action(c,"Kontrollera nya versioner",true,()->openMap("https://github.com/Ebbot86/TraceIQ/releases"));add(c,text("Testsignering: du kan behöva avinstallera föregående version. Automatiska installationer är inte aktiverade.",13,MUTED,false),10);
        c=surface(p,"SÄKERHETSDATA",12);add(c,text("API-nycklar är frivilliga och lagras bara i minnet under denna session. Tor-listan fungerar utan nyckel.",14,MUTED,false),8);action(c,"Ange API-nycklar",false,()->keyDialog());action(c,"Skapa konto hos ipapi.is",false,()->openMap("https://ipapi.is/"));action(c,"Skapa konto hos AbuseIPDB",false,()->openMap("https://www.abuseipdb.com/"));
        c=surface(p,"INTEGRITET",12);add(c,text("Ingen platsbehörighet, inget konto och ingen historik på disk. Sökningar skickas till ipwho.is, RIPEstat och Google Public DNS. OpenStreetMap får avrundade kartkoordinater; Google Maps får dem om du väljer att öppna kartan. Tor Project levererar en publik lista. Med frivilliga nycklar skickas IP även till ipapi.is och AbuseIPDB. Leverantörer kan se din anslutnings IP.",14,TEXT,false),8);
        action(c,"Källkod och dokumentation",false,()->openMap("https://github.com/Ebbot86/TraceIQ"));
    }
    @Override public void onBackPressed(){if(currentPage!=0){showPage(0);}else{super.onBackPressed();}}
    private void showIntro(){LinearLayout card=card("ANALYSOMRÅDEN");line(card,"Geografi","Land, region och ungefärlig stad");line(card,"Nätverk","Internetleverantör, ASN och prefix");line(card,"DNS","Omvänd DNS (PTR)");}
    private LinearLayout card(String title){LinearLayout c=column();c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(bg(PANEL,BORDER,16));add(c,text(title,12,CYAN,true),0);add(results,c,10);return c;}
    private void line(LinearLayout c,String label,String val){View rule=new View(this);rule.setBackgroundColor(0xff15425b);LinearLayout.LayoutParams div=new LinearLayout.LayoutParams(-1,dp(1));div.topMargin=dp(8);c.addView(rule,div);LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);add(c,row,7);TextView key=text(label,12,MUTED,true);row.addView(key,new LinearLayout.LayoutParams(0,-2,0.42f));TextView value=text(val,14,TEXT,true);value.setTextIsSelectable(true);row.addView(value,new LinearLayout.LayoutParams(0,-2,0.58f));}
    private void setBusy(boolean b){busy=b;lookup.setEnabled(!b);own.setEnabled(!b);input.setEnabled(!b);}
    private void start(boolean self){if(busy)return;String ip=input.getText().toString().trim();if(!self){byte[] bytes=IpAddress.parse(ip);if(bytes==null){input.setError("Ange en giltig IPv4- eller IPv6-adress");return;}if(!IpAddress.isPublic(bytes)){input.setError("Adressen är lokal eller reserverad och skickas inte vidare");return;}}
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);setBusy(true);report="";securityPanel=null;clearMap();results.removeAllViews();status.setText("●  ANALYSERAR …");final int token=++generation;final String ik=ipapiKey,ak=abuseKey;
        worker.execute(()->{try{JSONObject geo=get("https://ipwho.is/"+(self?"":ip));if(!geo.optBoolean("success",false))throw new IOException(geo.optString("message","Ingen information från IP-tjänsten"));String found=geo.optString("ip",ip);byte[] addr=IpAddress.parse(found);if(!IpAddress.isPublic(addr))throw new IOException("Svaret innehåller ingen publik IP-adress");JSONObject net=null,dns=null;try{net=get("https://stat.ripe.net/data/network-info/data.json?resource="+enc(found)).optJSONObject("data");}catch(Exception ignored){}try{dns=get("https://dns.google/resolve?name="+enc(IpAddress.ptr(addr))+"&type=PTR");}catch(Exception ignored){}final JSONObject n=net,d=dns;final ThreatIntel.Result security=intel.check(found,ik,ak);runOnUiThread(()->{if(token!=generation||isFinishing()||isDestroyed())return;setBusy(false);render(found,geo,n,d,security);});}catch(Exception e){runOnUiThread(()->{if(token!=generation||isFinishing()||isDestroyed())return;setBusy(false);status.setText("●  ANALYS MISSLYCKADES");LinearLayout c=card("INGET RESULTAT");line(c,"Fel",e instanceof SocketTimeoutException?"Tjänsten svarade inte i tid.":"Kunde inte hämta IP-information. Kontrollera internet och försök igen.");});}});
    }
    private static String enc(String s)throws UnsupportedEncodingException{return URLEncoder.encode(s,"UTF-8");}
    private static JSONObject get(String address)throws Exception{URL u=new URL(address);HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(6500);c.setReadTimeout(6500);c.setInstanceFollowRedirects(false);c.setRequestProperty("Accept","application/json");c.setRequestProperty("User-Agent","TraceIQ-Android/1.4");try{int code=c.getResponseCode();if(code!=200)throw new IOException("HTTP "+code);try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int r;while((r=in.read(b))!=-1){if(out.size()+r>512000)throw new IOException("Svar för stort");out.write(b,0,r);}return new JSONObject(out.toString("UTF-8"));}}finally{c.disconnect();}}
    private String val(JSONObject o,String key){if(o==null||o.isNull(key))return "Ej tillgängligt";String v=o.optString(key,"");return v.trim().isEmpty()?"Ej tillgängligt":v;}
    private void field(LinearLayout c,String k,String v,StringBuilder r){line(c,k,v);r.append(k).append(": ").append(v).append('\n');}
    private void render(String ip,JSONObject g,JSONObject n,JSONObject d,ThreatIntel.Result security){input.setText(ip);status.setText("●  ANALYS KLAR");remember(ip);StringBuilder r=new StringBuilder("TRACEIQ · IP-analys\n\n");LinearLayout c=card("IP-ANALYS · "+ip);field(c,"IP-adress",ip,r);field(c,"Typ",val(g,"type"),r);showSecurity(security,r);showMap(g,r);
        c=card("GEOGRAFI · UPPSKATTNING");field(c,"Land",val(g,"country"),r);field(c,"Region",val(g,"region"),r);field(c,"Stad",val(g,"city"),r);field(c,"Postnummer (uppskattat)",val(g,"postal"),r);field(c,"Tidszon",val(g.optJSONObject("timezone"),"id"),r);add(c,text("Visar en databasuppskattning – inte en persons eller enhets exakta position.",12,MUTED,false),12);
        JSONObject conn=g.optJSONObject("connection");c=card("NÄTVERK");field(c,"Internetleverantör",val(conn,"isp"),r);field(c,"Organisation",val(conn,"org"),r);String asn=val(conn,"asn");field(c,"ASN",asn.equals("Ej tillgängligt")?asn:"AS"+asn,r);field(c,"Domän",val(conn,"domain"),r);field(c,"Routingprefix (RIPEstat)",val(n,"prefix"),r);field(c,"Routing-ASN",val(n,"asns"),r);
        c=card("OMVÄND DNS");String ptr="Ej tillgängligt";if(d!=null&&d.optInt("Status",-1)==0){JSONArray a=d.optJSONArray("Answer");StringBuilder names=new StringBuilder();if(a!=null)for(int i=0;i<a.length();i++){JSONObject item=a.optJSONObject(i);if(item!=null&&item.optInt("type")==12){if(names.length()>0)names.append(", ");names.append(item.optString("data"));}}ptr=names.length()>0?names.toString():"Ingen PTR-post hittad";}else if(d!=null&&d.optInt("Status",-1)==3)ptr="Ingen PTR-post hittad";field(c,"Värdnamn",ptr,r);
        c=card("TOLKNING");add(c,text("En IP kan delas av många användare, eller tillhöra ett VPN, en server eller en operatör. VPN, proxy och Tor är inte i sig säkerhetshot. Databasuppgifter kan vara felaktiga eller gamla.",14,TEXT,false),10);r.append("\nGeografi är ungefärlig. Säkerhetsdata är indikationer, inte bevis.\nKällor: ipwho.is, RIPEstat, Google Public DNS.");report=r.toString();if(currentPage<=1)scrollTo(status);Button share=button("Dela analysrapport",false);add(results,share,12);share.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,report);startActivity(Intent.createChooser(i,"Dela analys"));});Button copy=button("Kopiera rapport",false);add(results,copy,8);copy.setOnClickListener(v->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("TraceIQ",report));Toast.makeText(this,"Rapport kopierad",Toast.LENGTH_SHORT).show();});add(results,text("Källor: ipwho.is · RIPEstat · Google Public DNS",12,MUTED,false),12);
    }

    private void clearMap(){if(mapView!=null){mapView.stopLoading();if(mapView.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)mapView.getParent()).removeView(mapView);mapView.destroy();mapView=null;}}

    private void openSecurity(){showPage(0);if(securityPanel!=null)scrollTo(securityPanel);else if(!busy){scrollTo(input);input.requestFocus();Toast.makeText(this,"Ange en IP-adress för säkerhetsanalys",Toast.LENGTH_SHORT).show();}}
    private void keyDialog(){LinearLayout c=column();c.setPadding(dp(20),dp(10),dp(20),dp(8));add(c,text("Frivilliga personliga nycklar. Sparas endast i minnet och skickas till respektive leverantör. Gör en ny analys efter ändring.",14,TEXT,false),0);
        EditText i=new EditText(this),a=new EditText(this);for(EditText e:new EditText[]{i,a}){e.setSingleLine(true);e.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);e.setTextColor(TEXT);e.setHintTextColor(MUTED);}i.setHint("ipapi.is API-nyckel");a.setHint("AbuseIPDB API-nyckel");i.setText(ipapiKey);a.setText(abuseKey);add(c,i,12);add(c,a,8);
        new AlertDialog.Builder(this).setTitle("Säkerhetsdatakällor").setView(c).setPositiveButton("Spara för sessionen",(d,w)->{ipapiKey=i.getText().toString().trim();abuseKey=a.getText().toString().trim();Toast.makeText(this,"Nycklar uppdaterade. Kör en ny IP-analys.",Toast.LENGTH_LONG).show();}).setNegativeButton("Avbryt",null).setNeutralButton("Rensa",(d,w)->{ipapiKey="";abuseKey="";}).show();
    }
    private void showSecurity(ThreatIntel.Result s,StringBuilder r){
        LinearLayout c=card("SECURITY INTELLIGENCE");securityPanel=c;
        int accent=s.score==null?MUTED:s.score>=75?0xffff746e:s.score>0?0xffffc15a:0xff59dfa7;
        add(c,text(s.score==null?"— / 100":s.score+" / 100",30,accent,true),10);
        add(c,text("AbuseIPDB · rapportbaserad missbruksbedömning (90 dagar)",12,MUTED,false),3);
        field(c,"Rapporter (90 dagar)",s.reports==null?"Okänt":String.valueOf(s.reports),r);
        field(c,"AbuseIPDB-poäng",s.score==null?"Okänt":s.score+" / 100",r);
        field(c,"VPN (ipapi.is)",s.vpn,r);field(c,"Proxy (ipapi.is)",s.proxy,r);field(c,"Datacenter (ipapi.is)",s.hosting,r);field(c,"Tor (ipapi.is)",s.tor,r);field(c,"Missbruksindikator (ipapi.is)",s.abuser,r);
        field(c,"Tor Projects exitlista",s.torList,r);
        field(c,"ipapi.is",s.ipapiStatus,r);field(c,"AbuseIPDB",s.abuseStatus,r);
        add(c,text("Ingen träff betyder inte att en IP är säker. VPN, proxy och Tor är inte i sig bevis på missbruk.",12,MUTED,false),12);
        action(c,"API-nycklar",false,()->keyDialog());
    }
    private void openMap(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(ActivityNotFoundException e){Toast.makeText(this,"Ingen kartapp eller webbläsare hittades",Toast.LENGTH_LONG).show();}}
    private void showMap(JSONObject geo,StringBuilder report){
        LinearLayout c=card("IP-KARTA · UPPSKATTAD POSITION");
        double lat=geo.optDouble("latitude",Double.NaN),lon=geo.optDouble("longitude",Double.NaN);
        if(!GeoMap.valid(lat,lon)){line(c,"Position","Koordinater saknas eller kan inte visas på kartan");return;}
        GeoMap p=new GeoMap(lat,lon);
        add(c,text(val(geo,"city")+" · "+val(geo,"country"),18,TEXT,true),8);
        add(c,text("Nålen visar en grov databasuppskattning, inte en bostad eller en GPS-position. Kartans utsnitt anger inte noggrannheten.",13,MUTED,false),5);
        mapView=new WebView(this);
        mapView.setBackgroundColor(PANEL);
        WebSettings settings=mapView.getSettings();
        settings.setJavaScriptEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setGeolocationEnabled(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setSupportMultipleWindows(false);
        settings.setDomStorageEnabled(true);
        final TextView message=text("Laddar karta …",12,MUTED,false);
        add(c,message,8);
        mapView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest request){return !p.embed().equals(request.getUrl().toString());}
            @Override public void onPageFinished(WebView v,String url){if(v==mapView)message.setText("Dra och zooma i kartan · © OpenStreetMap-bidragsgivare");}
            @Override public void onReceivedError(WebView v,WebResourceRequest request,WebResourceError error){if(request.isForMainFrame()&&v==mapView){message.setText("Kartan kunde inte laddas. Använd Google Maps nedan.");v.setVisibility(View.GONE);}}
        });
        mapView.setOnTouchListener((v,event)->{v.getParent().requestDisallowInterceptTouchEvent(event.getActionMasked()!=MotionEvent.ACTION_UP&&event.getActionMasked()!=MotionEvent.ACTION_CANCEL);return false;});
        LinearLayout.LayoutParams mapParams=new LinearLayout.LayoutParams(-1,dp(300));mapParams.topMargin=dp(8);c.addView(mapView,mapParams);mapView.loadUrl(p.embed());
        TextView attribution=text("© OpenStreetMap-bidragsgivare · Licens",12,CYAN,false);attribution.setOnClickListener(v->openMap("https://www.openstreetmap.org/copyright"));add(c,attribution,6);
        field(c,"Avrundade koordinater",p.coordinates(),report);
        Button open=button("Öppna i Google Maps",false);add(c,open,10);open.setOnClickListener(v->openMap(p.google()));
        Button satellite=button("Satellitvy i Google Maps",false);add(c,satellite,8);satellite.setOnClickListener(v->openMap(p.satellite()));
        report.append("Kartposition: grov uppskattning, ingen verifierad adress.\n");
    }
    @Override protected void onDestroy(){generation++;clearMap();worker.shutdownNow();super.onDestroy();}
}
