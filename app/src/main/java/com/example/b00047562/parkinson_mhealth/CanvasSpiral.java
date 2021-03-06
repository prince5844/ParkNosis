package com.example.b00047562.parkinson_mhealth;

/**
 * Created by Os on 2/22/2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


import java.text.DecimalFormat;
import java.util.ArrayList;

public class CanvasSpiral extends View {

    private static final String TAG = "Showing size";
    String TAG2="Listing x&y";
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    //  private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    private int density;
    private double screenInches;
    private double diagonal;
    int maxX,maxY,width,height;
    float ratio;
    float wd,hd;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    Context context;
    private Paint mPaint,mPaint2,mPaint3;
    float []OriginalSpiralPoints = new float[168];
    float []pointsArray = new float[2160];
    ArrayList<Float> bigFlo;

    public static ArrayList<SpiralData> getSpiralData() {
        return spiralData;
    }

    //User inputed spiral
    public static ArrayList<SpiralData> spiralData;

    public CanvasSpiral(Context context, AttributeSet attrs) {
        super(context,attrs);
        this.context = context;
        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(10f);
        mPaint.setAlpha(0x80);

        mPaint2= new Paint();
        mPaint2.setAntiAlias(true);
        mPaint2.setColor(Color.GREEN);
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setStrokeJoin(Paint.Join.ROUND);
        mPaint2.setStrokeWidth(10f);
        mPaint3= new Paint();
        mPaint3.setAntiAlias(true);
        mPaint3.setColor(Color.parseColor("#f06292"));
        mPaint3.setStyle(Paint.Style.STROKE);
        mPaint3.setStrokeJoin(Paint.Join.ROUND);
        mPaint3.setStrokeWidth(10f);

        /*
        Display mdisp =((Activity) context).getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        maxX = mdispSize.x;
        maxY = mdispSize.y;
        */

        calculateMetrics();
        setupDrawing();
        bigFlo= new ArrayList<>();
    }


    private void setupDrawing(){
        //get drawing area setup for interaction
        drawPath = new Path();
        drawPaint = new Paint();

        //  drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(10);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setColor(Color.GREEN);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        //start new list
        spiralData = new ArrayList<>();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

    }


    public float[] getOriginalSpiralPoints() {
        return OriginalSpiralPoints;
    }

    private void calculateMetrics()
    {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        width=dm.widthPixels;
        height=dm.heightPixels;
        density=dm.densityDpi;

        maxX = width;
        maxY = height;

        wd=(float)(width)/(float)density;
        hd=(float)(height)/(float)density;
        screenInches = Math.sqrt(Math.pow(wd,2)+Math.pow(hd,2));

        diagonal = Math.sqrt(Math.pow((float)width,2)+Math.pow((float)height,2));
        ratio = (wd > hd? hd : wd);
        //ratio = (width > height? (float)height/width : (float)width/height);
        height = height + 80;

//        Log.d("debug", "Screen pixels - width: " +width+" height: "+height );
//        Log.d("debug", "Screen density: "+density+" wd: " +wd+" hd: "+hd );
//        Log.d("debug", "maxX: " +maxX+" maxY: "+maxY );
//        Log.d("debug", "Screen in square inches : " + screenInches);
//        Log.d("debug", "Diagonal: "+diagonal );
//        Log.d("debug", "Ratio: "+ratio );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float angle,x ,y ;
        int j=0, z=0;

        for (z=0; z<1080; z++,j=j+2) {
            angle = 0.50502f * z;

            x = width/2   -10+ (5 + angle) * (float) Math.cos(angle);
            y = height/2 -250+ (5 + angle) * (float) Math.sin(angle);

            pointsArray[j]=x;
            pointsArray[j+1]=y;
        }
        int k=0;
        for (int m=0; m<2160; m++,k=k+2)
        {

            OriginalSpiralPoints[k]=pointsArray[m];
            OriginalSpiralPoints[k+1]=pointsArray[m+1];
            m+=25;

        }

        for (int i=0;i<166;i=i+2)
        {
            canvas.drawLine(OriginalSpiralPoints[i], OriginalSpiralPoints[i + 1], OriginalSpiralPoints[i + 2], OriginalSpiralPoints[i + 3], mPaint);

            // Log.d(TAG2, "onDraw: "+(i/2)+ " x&y: " + OriginalSpiralPoints[i] + " " + OriginalSpiralPoints[i + 1]);
        }

        canvas.drawPoints(OriginalSpiralPoints,mPaint3);

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);

    }

    public float FindAccuracy(){
        float AccuracyPercent=0;
        //try {
        DecimalFormat df = new DecimalFormat("#.###");
        for (int i = 0; i < bigFlo.size()/2-2; i+=2) {

            for (int j = 0; j < OriginalSpiralPoints.length/2 - 2; j += 2) {
                if (Float.parseFloat(df.format(bigFlo.get(j))) == Float.parseFloat(df.format(OriginalSpiralPoints[j]))
                        && Float.parseFloat(df.format(bigFlo.get(j+1))) == Float.parseFloat(df.format( OriginalSpiralPoints[j + 1])))
                    AccuracyPercent++;
//                    Log.d("BigFlo", bigFlo.get(j)+"");
//                    Log.d("OS", OriginalSpiralPoints[j]+"");
//                    Log.d("Acc", AccuracyPercent+"");

            }
        }
        AccuracyPercent /= (float)spiralData.size();
        // }catch (Exception c){
        // Log.d("AccErr", c.getMessage());
        ;

        // }

        return AccuracyPercent*100f;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//detect user touch
        long curTime = System.currentTimeMillis();
        float touchX = event.getX();
        float touchY = event.getY();
        bigFlo.add(event.getX());
        bigFlo.add(event.getY());


        //store spiral draw points
        SpiralData data = new SpiralData(curTime,touchX,touchY);
        spiralData.add(data);



        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
//                Spiral.alert.setTextColor(Color.parseColor("#00e676"));
//                Spiral.alert.setText("OK!");
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                //Toast.makeText(context, String.valueOf(FindAccuracy()),Toast.LENGTH_SHORT).show();
//                Spiral.alert.setTextColor(Color.RED);
//                Spiral.alert.setText("KEEP TOUCHING!");
                drawPath.reset();
                Spiral.redrawOpen.setEnabled(true); // to prevent crashing or redrawing when no data is available - this will updated soon
                Spiral.btnSubmit.setEnabled(true);

                //   Log.d(TAG, "onTouchEvent: " + spiralData.size());
                break;
            default:
                return false;
        }
        invalidate();

        return true;
    }
    public void cleardisp()
    {
        spiralData.clear();
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        invalidate();
    }

//
//    public void setDensity(int density) {
//        this.density = density;
//    }
//    public int getDensity()
//    {
//        return density;
//    }
}
