package se.traceiq.app;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
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
    private final int BG=0xff0b1220, PANEL=0xff131e30, BORDER=0xff26374c, TEXT=0xffe6edf7, MUTED=0xff9aabc1, CYAN=0xff38bdf8;
    private LinearLayout root, results; private EditText input; private TextView status; private Button lookup, own;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private String report=""; private int generation=0; private boolean busy=false;
    private int dp(float x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
    private GradientDrawable bg(int color,int stroke,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private void add(LinearLayout parent,View v,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(top);parent.addView(v,p);}
    private Button button(String s,boolean primary){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(15);b.setTextColor(primary?BG:TEXT);b.setBackground(bg(primary?CYAN:PANEL,primary?0:BORDER,12));b.setMinHeight(dp(48));return b;}
    @Override public void onCreate(Bundle state){super.onCreate(state);ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);root=column();root.setPadding(dp(20),dp(16),dp(20),dp(28));scroll.addView(root);setContentView(scroll);
        add(root,text("◉  TRACEIQ",29,CYAN,true),0);add(root,text("IP INTELLIGENCE  /  v1.0.0",12,MUTED,true),3);
        add(root,text("Nätverksanalys",25,TEXT,true),25);add(root,text("Sök en publik IP-adress och läs information om nätverket bakom den.",15,MUTED,false),5);
        input=new EditText(this);input.setSingleLine(true);input.setTextColor(TEXT);input.setHintTextColor(MUTED);input.setHint("IPv4 eller IPv6, t.ex. 8.8.8.8");input.setTextSize(16);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);input.setPadding(dp(14),dp(13),dp(14),dp(13));input.setBackground(bg(PANEL,BORDER,12));input.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);add(root,input,18);
        lookup=button("Analysera IP-adress",true);add(root,lookup,10);own=button("Visa min publika IP",false);add(root,own,8);
        lookup.setOnClickListener(v->start(false));own.setOnClickListener(v->start(true));input.setOnEditorActionListener((v,a,e)->{if(a==android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH){start(false);return true;}return false;});
        status=text("●  REDO",13,CYAN,true);add(root,status,20);results=column();add(root,results,8);showIntro();
        TextView privacy=text("Integritet & datakällor",14,CYAN,true);privacy.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Integritet").setMessage("Sökningar skickas över HTTPS till ipwho.is. Om en publik IP hittas skickas den även till RIPEstat och en omvänd DNS-fråga till Google Public DNS. Leverantörerna kan se din anslutnings IP-adress. Appen begär inte GPS, sparar ingen sökhistorik och har inga konton. IP-geolokalisering är ungefärlig och identifierar inte en person. Gratis API-villkor måste kontrolleras före kommersiell distribution.").setPositiveButton("Stäng",null).show());add(root,privacy,22);
    }
    private void showIntro(){LinearLayout card=card("ANALYSOMRÅDEN");line(card,"Geografi","Land, region och ungefärlig stad");line(card,"Nätverk","Internetleverantör, ASN och prefix");line(card,"DNS","Omvänd DNS (PTR)");}
    private LinearLayout card(String title){LinearLayout c=column();c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(bg(PANEL,BORDER,16));add(c,text(title,12,CYAN,true),0);add(results,c,10);return c;}
    private void line(LinearLayout c,String label,String val){add(c,text(label,12,MUTED,false),12);TextView value=text(val,16,TEXT,true);value.setTextIsSelectable(true);add(c,value,3);}
    private void setBusy(boolean b){busy=b;lookup.setEnabled(!b);own.setEnabled(!b);input.setEnabled(!b);}
    private void start(boolean self){if(busy)return;String ip=input.getText().toString().trim();if(!self){byte[] bytes=IpAddress.parse(ip);if(bytes==null){input.setError("Ange en giltig IPv4- eller IPv6-adress");return;}if(!IpAddress.isPublic(bytes)){input.setError("Adressen är lokal eller reserverad och skickas inte vidare");return;}}
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);setBusy(true);report="";results.removeAllViews();status.setText("●  ANALYSERAR …");final int token=++generation;
        worker.execute(()->{try{JSONObject geo=get("https://ipwho.is/"+(self?"":ip));if(!geo.optBoolean("success",false))throw new IOException(geo.optString("message","Ingen information från IP-tjänsten"));String found=geo.optString("ip",ip);byte[] addr=IpAddress.parse(found);if(!IpAddress.isPublic(addr))throw new IOException("Svaret innehåller ingen publik IP-adress");JSONObject net=null,dns=null;try{net=get("https://stat.ripe.net/data/network-info/data.json?resource="+enc(found)).optJSONObject("data");}catch(Exception ignored){}try{dns=get("https://dns.google/resolve?name="+enc(IpAddress.ptr(addr))+"&type=PTR");}catch(Exception ignored){}final JSONObject n=net,d=dns;runOnUiThread(()->{if(token!=generation||isFinishing()||isDestroyed())return;setBusy(false);render(found,geo,n,d);});}catch(Exception e){runOnUiThread(()->{if(token!=generation||isFinishing()||isDestroyed())return;setBusy(false);status.setText("●  ANALYS MISSLYCKADES");LinearLayout c=card("INGET RESULTAT");line(c,"Fel",e instanceof SocketTimeoutException?"Tjänsten svarade inte i tid.":"Kunde inte hämta IP-information. Kontrollera internet och försök igen.");});}});
    }
    private static String enc(String s)throws UnsupportedEncodingException{return URLEncoder.encode(s,"UTF-8");}
    private static JSONObject get(String address)throws Exception{URL u=new URL(address);HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(6500);c.setReadTimeout(6500);c.setInstanceFollowRedirects(false);c.setRequestProperty("Accept","application/json");c.setRequestProperty("User-Agent","TraceIQ-Android/1.0");try{int code=c.getResponseCode();if(code!=200)throw new IOException("HTTP "+code);try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int r;while((r=in.read(b))!=-1){if(out.size()+r>512000)throw new IOException("Svar för stort");out.write(b,0,r);}return new JSONObject(out.toString("UTF-8"));}}finally{c.disconnect();}}
    private String val(JSONObject o,String key){if(o==null||o.isNull(key))return "Ej tillgängligt";String v=o.optString(key,"");return v.trim().isEmpty()?"Ej tillgängligt":v;}
    private void field(LinearLayout c,String k,String v,StringBuilder r){line(c,k,v);r.append(k).append(": ").append(v).append('\n');}
    private void render(String ip,JSONObject g,JSONObject n,JSONObject d){input.setText(ip);status.setText("●  ANALYS KLAR");StringBuilder r=new StringBuilder("TRACEIQ · IP-analys\n\n");LinearLayout c=card("IDENTIFIERING");field(c,"IP-adress",ip,r);field(c,"Typ",val(g,"type"),r);
        c=card("GEOGRAFI · UPPSKATTNING");field(c,"Land",val(g,"country"),r);field(c,"Region",val(g,"region"),r);field(c,"Stad",val(g,"city"),r);field(c,"Tidszon",val(g.optJSONObject("timezone"),"id"),r);add(c,text("Visar en databasuppskattning – inte en persons eller enhets exakta position.",12,MUTED,false),12);
        JSONObject conn=g.optJSONObject("connection");c=card("NÄTVERK");field(c,"Internetleverantör",val(conn,"isp"),r);field(c,"Organisation",val(conn,"org"),r);String asn=val(conn,"asn");field(c,"ASN",asn.equals("Ej tillgängligt")?asn:"AS"+asn,r);field(c,"Domän",val(conn,"domain"),r);field(c,"Routingprefix (RIPEstat)",val(n,"prefix"),r);field(c,"Routing-ASN",val(n,"asns"),r);
        c=card("OMVÄND DNS");String ptr="Ej tillgängligt";if(d!=null&&d.optInt("Status",-1)==0){JSONArray a=d.optJSONArray("Answer");StringBuilder names=new StringBuilder();if(a!=null)for(int i=0;i<a.length();i++){JSONObject item=a.optJSONObject(i);if(item!=null&&item.optInt("type")==12){if(names.length()>0)names.append(", ");names.append(item.optString("data"));}}ptr=names.length()>0?names.toString():"Ingen PTR-post hittad";}else if(d!=null&&d.optInt("Status",-1)==3)ptr="Ingen PTR-post hittad";field(c,"Värdnamn",ptr,r);
        c=card("TOLKNING");add(c,text("En IP kan delas av många användare, eller tillhöra ett VPN, en server eller en operatör. VPN- och hotstatus är inte kontrollerad i denna version.",14,TEXT,false),10);r.append("\nGeografi är ungefärlig. VPN/hotstatus ej kontrollerad.\nKällor: ipwho.is, RIPEstat, Google Public DNS.");report=r.toString();Button share=button("Dela analysrapport",false);add(results,share,12);share.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,report);startActivity(Intent.createChooser(i,"Dela analys"));});Button copy=button("Kopiera rapport",false);add(results,copy,8);copy.setOnClickListener(v->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("TraceIQ",report));Toast.makeText(this,"Rapport kopierad",Toast.LENGTH_SHORT).show();});add(results,text("Källor: ipwho.is · RIPEstat · Google Public DNS",12,MUTED,false),12);
    }
    @Override protected void onDestroy(){generation++;worker.shutdownNow();super.onDestroy();}
}
