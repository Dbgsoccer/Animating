package com.example.chrisbennett.animating;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.util.Random;

import static android.R.attr.button;
import static android.R.attr.visibility;
import static android.R.attr.visible;
import static android.R.attr.x;
import static java.lang.Math.sin;


public class CustomView extends SurfaceView implements SurfaceHolder.Callback {

    protected Context context;
    private Bitmap balloon;
    private Bitmap bwBalloon;
    DrawingThread thread;
    Paint text;
    int x,y;
    int score;
    int speed = score;
    int corgiHeight = 100;
    public int screenWidth,screenHeight;
    Button b = (Button) findViewById(R.id.newGameBtn);

    //int amplitude = 10;





    public int getScreenHeight() {
        screenHeight = getDisplay().getHeight();
        return screenHeight;
    }
    public int getScreenWidth(){
        screenWidth = getDisplay().getWidth();
        return screenWidth;
    }
    public CustomView(Context ctx, AttributeSet attrs) {
        super(ctx,attrs);
        context = ctx;

        balloon = BitmapFactory.decodeResource(context.getResources(),R.drawable.supercorgi);
        bwBalloon=balloon.copy(Bitmap.Config.ARGB_8888, true);
        bwBalloon = resizeBitmap(bwBalloon,150,corgiHeight);
        /*
        for(int i=0;i<bwBalloon.getWidth();i++) {
           for(int j=0;j<bwBalloon.getHeight();j++) {
                int g = Color.red(bwBalloon.getPixel(i,j));
                bwBalloon.setPixel(i,j,Color.rgb(g,g,g));
            }
        }
        */
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        text=new Paint();
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(Color.WHITE);
        text.setTextSize(24);
        x=0;
        y=0;
        score = 0;


    }


    public Bitmap resizeBitmap(Bitmap b, int newWidth, int newHeight) {
        int w = b.getWidth();
        int h = b.getHeight();
        float scaleWidth = ((float) newWidth) / w;
        float scaleHeight = ((float) newHeight) / h;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                b, 0, 0, w, h, matrix, false);
        b.recycle();
        return resizedBitmap;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
        boolean waitingForDeath = true;
        while(waitingForDeath) {
            try {
                thread.join();
                waitingForDeath = false;
            }
            catch (Exception e) {
                Log.v("Thread Exception", "Waiting on drawing thread to die: " + e.getMessage());
            }
        }

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread= new DrawingThread(holder, context, this);
        thread.setRunning(true);
        thread.start();

    }


    public void customDraw(Canvas canvas) {
        //Button b = (Button) findViewById(R.id.newGameBtn);
        screenHeight = getScreenHeight();
        screenWidth = getScreenWidth();
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bwBalloon,x,y,null);
        canvas.drawText("Score: " + score,screenWidth/2,50,text);
        x+=score;
        //bellow is a potential way to make corgi move in not a straight line
        //x =  amplitude * (int)sin(y);
        //Log.v("drawing", "y: " + y);

        if(x>screenWidth){

            b.setVisibility(VISIBLE);
            //need to be able to switch layout view here
            //gameOver();
        }
    }
    public void newGame(){
        x=0;
        y=0;
        score=0;
        Button b = (Button) findViewById(R.id.newGameBtn);
        b.setVisibility(INVISIBLE);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v("touch event",event.getX() + "," + event.getY());

        double distance = Math.sqrt((x-event.getX()) * (x-event.getX()) + (y-event.getY()) * (y-event.getY()));
        if(distance < 150) {
            score++;
           //this is how we avoid hardcoding where corgi shows up
            int min = corgiHeight;
            int max = screenHeight-corgiHeight*4;

            //not sure why I need to multiply corgiHeight by 4 in prder to keep it
            //above the bottom of the screen
            Random r = new Random();
            int i1 = r.nextInt(max - min + 1) + min;
            y = i1;
            x=0;
        }
        return true;
    }


    class DrawingThread extends Thread {
        private boolean running;
        private Canvas canvas;
        private SurfaceHolder holder;
        private Context context;
        private CustomView view;

        private int FRAME_RATE = 30;
        private double delay = 1.0 / FRAME_RATE * 1000;
        private long time;

        public DrawingThread(SurfaceHolder holder, Context c, CustomView v) {
            this.holder=holder;
            context = c;
            view = v;
            time = System.currentTimeMillis();
        }
//


        void setRunning(boolean r) {
            running = r;
        }

        @Override
        public void run() {
            super.run();
            while(running){
                if(System.currentTimeMillis() - time > delay) {
                    time = System.currentTimeMillis();
                    canvas = holder.lockCanvas();
                    if(canvas!=null){
                        view.customDraw(canvas);
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }



    }
}
//I need to make a hidden button that appears when the corgi hits a thing
// When it pops up, hae an on click event that set score to 0, set corgi back to starting position, and makes button invisible
