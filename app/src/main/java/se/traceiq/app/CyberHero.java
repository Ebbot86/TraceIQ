package se.traceiq.app;
import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Offline decorative illustration. Pins and arcs are artwork, not live locations. */
final class CyberHero extends View {
 private final Paint p=new Paint(3);
 private final Path world=new Path();
 private final float[][] land={
 {18,40,47,19,80,20,104,10,140,21,155,37,137,47,113,48,100,65,84,67,75,90,54,78,40,61,25,62},
 {82,91,105,94,125,109,119,132,105,158,92,172,87,146,78,122,72,105},
 {148,13,171,8,186,18,176,35,155,36},
 {175,44,189,25,219,25,229,37,213,50,195,49,185,59},
 {181,57,211,55,232,72,223,105,208,128,194,111,185,87,170,75},
 {222,25,250,15,276,24,304,16,338,24,366,40,354,55,327,59,314,79,293,76,281,99,263,78,244,76,231,53},
 {291,94,312,100,327,111,314,115,300,108},
 {320,131,344,117,367,131,371,151,344,158,322,148},
 {232,115,239,124,235,140,230,128}};
 CyberHero(Context c){super(c);setContentDescription("Dekorativ världskarta med sköld och positionssymboler. Visar inga verkliga IP-positioner.");for(float[] a:land){world.moveTo(a[0],a[1]);for(int i=2;i<a.length;i+=2)world.lineTo(a[i],a[i+1]);world.close();}}
 private void color(int c){p.setShader(null);p.setColor(c);p.setStyle(Paint.Style.FILL);}
 private void stroke(int c,float w){color(c);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w);}
 private void pin(Canvas c,float x,float y,float s){c.save();c.translate(x,y);c.scale(s,s);Path pin=new Path();pin.moveTo(0,15);pin.cubicTo(-2,10,-10,2,-10,-4);pin.cubicTo(-10,-17,10,-17,10,-4);pin.cubicTo(10,2,2,10,0,15);pin.close();color(0xff1ce5ff);c.drawPath(pin,p);color(0xff075579);c.drawCircle(0,-4,4,p);c.restore();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();c.scale(getWidth()/400f,getHeight()/220f);
  color(0xff03101d);p.setShader(new RadialGradient(195,114,205,new int[]{0xff0a3f63,0xff041a30,0xff020a16},null,Shader.TileMode.CLAMP));c.drawRoundRect(0,0,400,220,16,16,p);
  c.save();c.clipPath(world);for(int y=5;y<176;y+=4)for(int x=5;x<395;x+=4){color(0xff137eaa);c.drawCircle(x,y,1.05f,p);}c.restore();
  float[][] pins={{59,43},{86,108},{192,31},{230,60},{283,43},{337,61},{344,132}};
  for(float[] a:pins){Path arc=new Path();arc.moveTo(199,99);arc.quadTo((199+a[0])/2,Math.min(99,a[1])-48,a[0],a[1]);stroke(0x8850dcff,0.85f);c.drawPath(arc,p);pin(c,a[0],a[1],0.65f);}
  for(int i=0;i<5;i++){stroke(0xff124964,0.6f);c.drawLine(0,151+i*16,400,151+i*16,p);}for(int i=0;i<12;i++){stroke(0xff12374f,0.6f);c.drawLine(200,143,i*40-20,220,p);}
  for(int i=0;i<4;i++){stroke(i==1?0xff19dcff:0x9924a2df,i==1?2:1);c.drawOval(117-i*10,167-i*3,283+i*10,199+i*3,p);}
  color(0xff07a7e7);p.setShader(new RadialGradient(200,183,70,new int[]{0x9900eaff,0x0000eaff},null,Shader.TileMode.CLAMP));c.drawOval(130,160,270,205,p);
  Path shield=new Path();shield.moveTo(200,48);shield.cubicTo(183,64,158,69,144,72);shield.lineTo(144,112);shield.cubicTo(144,143,166,162,200,179);shield.cubicTo(234,162,256,143,256,112);shield.lineTo(256,72);shield.cubicTo(242,69,217,64,200,48);shield.close();
  color(0xff073357);p.setShader(new LinearGradient(146,68,254,158,new int[]{0xff0c2340,0xff07598c,0xff09223d},null,Shader.TileMode.CLAMP));c.drawPath(shield,p);stroke(0xff26dbff,3);c.drawPath(shield,p);
  c.save();c.scale(.88f,.88f,200,110);stroke(0xff4e8fb7,1);c.drawPath(shield,p);c.restore();pin(c,200,108,2.4f);
  stroke(0xff096e93,1);c.drawRoundRect(0.5f,0.5f,399.5f,219.5f,16,16,p);
  color(0xff7ab8d5);p.setTextSize(8);p.setTypeface(Typeface.MONOSPACE);c.drawText("GLOBAL NETWORK  /  ILLUSTRATION",12,211,p);
  c.restore();
 }
}
