package com.google.ar.core.examples.java.helloar;


import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.widget.TextView;



import androidx.annotation.NonNull;

import com.google.ar.core.examples.java.common.helpers.Point;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;


import java.util.ArrayList;





public class MainActivity extends Activity {
    private GLSurfaceView mView;
    private Feature_Points mRenderer;

    private DatabaseReference rootRef;
    private DatabaseReference childRef;
    private TextView tv;
    //暫存
    private ArrayList<Point> points;
    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;


    private static final String TAG ="reading save...";
    private ValueEventListener valueEventListener;


    //XYZ資料
    private  String stringArray[] = new String[3000];
    private float floatArray[] = new float[3000];
    Double X;
    Double Y;
    Double Z;

    private float dataXYZ[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new GLSurfaceView(this);
        mRenderer = new Feature_Points(this);
        mView.setRenderer(mRenderer);
        setContentView(mView);


        //new code
        //graphicView = new GraphicView(this);
        onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_point_from_db);
        rootRef= FirebaseDatabase.getInstance().getReference();
        childRef=rootRef.child("arcore-f3cb4-default-rtdb");
        tv=findViewById(R.id.textViewSeeDB);
        points=new ArrayList<Point>();
        //Authentication
        mAuth= FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull @NotNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=mAuth.getCurrentUser();
                if(user!=null){
                    Log.d(TAG, "onAuthStateChanged:signed_in");
                }
                else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        //重點:用Valuelistener監聽資料庫狀態，再用datasnapshot(資料快照)抓下資料
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {



                int i = 0;
                for(DataSnapshot ds:snapshot.getChildren()){
                    X=ds.child("x").getValue(Double.class);
                    //   tv.append("X"+X+"\n");
                    Y=ds.child("y").getValue(Double.class);
                    //   tv.append("Y"+Y+"\n");
                    Z=ds.child("z").getValue(Double.class);
                    //   tv.append("Z"+Z+"\n");
                    stringArray[i]   = String.valueOf(X) ;
                    stringArray[i+1] = String.valueOf(Y) ;
                    stringArray[i+2] = String.valueOf(Z) ;

                    floatArray[i] = Float.parseFloat(stringArray[i]);
                    floatArray[i+1] = Float.parseFloat(stringArray[i+1]);
                    floatArray[i+2] = Float.parseFloat(stringArray[i+2]);

                    i = i+3;
                }


            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        };
        rootRef.addListenerForSingleValueEvent(valueEventListener);








    }



    public boolean onTouchEvent(MotionEvent event) {
        return mRenderer.onTouchEvent(event);
    }

    public class Feature_Points implements GLSurfaceView.Renderer {
        private Context mContext;
        private FloatBuffer mVertexBuffer = null;
        public float mAngleX = 0.0f;
        public float mAngleY = 0.0f;
        public float mAngleZ = 0.0f;
        private float mPreviousX;
        private float mPreviousY;
        private final float TOUCH_SCALE_FACTOR = 0.6f;
        private final float AXIS_SCALE_FACTOR = 1.5f;
        private final int AXIS_WIDTH = 10;
        private final float POINT_WIDTH = 20f;
        private int program;
        private int color_length;
        private int vertex_length;

        //建立顏色array(r,g,b,a)
        //宣告成全域變數
        public float colors[]={
                0.0f,0.0f,1.0f,1.0f,
                0.0f,0.5f,0.0f,1.0f,
                1.0f,0.0f,1.0f,1.0f,
                0.0f,1.0f,1.0f,0.8f,
        };

        //建立位置array(x,y,z)
        //宣告成全域變數
        public float vertex_list[]={
                0.5f, 0.5f, 0.0f,
                0.3f, -0.4f, 0.2f,
                -0.2f, 0.3f, 0.1f,
                -0.5f,-0.2f,-0.3f
        };


        //XYZ資料
        private  String stringArray[] = new String[3000];
        private float floatArray[] = new float[3000];
        Double X;
        Double Y;
        Double Z;

        private float dataXYZ[];



        public Feature_Points(Context context) {
            mContext = context;
        }

        public void onDrawFrame(GL10 gl) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            gl.glTranslatef(0.0f, 0.0f, -3.0f);
            gl.glRotatef(mAngleX, 1, 0, 0);
            gl.glRotatef(mAngleY, 0, 1, 0);
            gl.glRotatef(mAngleZ, 0, 0, 1);
            gl.glScalef(AXIS_SCALE_FACTOR, AXIS_SCALE_FACTOR, AXIS_SCALE_FACTOR);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
            /*float colors[]={
                    1.0f,0.0f,0.0f,1.0f,
                    0.0f,1.0f,0.0f,1.0f,
                    0.0f,0.0f,1.0f,1.0f,
                    0.0f,1.0f,1.0f,0.8f,
            };*/
            Log.d("vertex length: ",""+vertex_length);
            //每四個r,g,b,a一組
            color_length=(colors.length/4)-1;
            Log.d("color length: ",""+color_length);

            //檢查點的座標數值是否和顏色數值相符
            if(color_length!=vertex_length){
                Log.d("OpenGL Error","座標和顏色組數不同");
            }
            //設定點大小
            gl.glPointSize(20);

            for(int index=0;index<=color_length;index++){

                //設定顏色 依序填入r,g,b,a
                gl.glColor4f(colors[4*index], colors[4*index+1], colors[4*index+2], colors[4*index+3]);
                /*
                GL10.GL_POINTS:繪製openGL類型:POINTS
                index:從array第幾項開始，利用for loop從0開始
                count:從array[index]開始數2個element進行繪製
                */
                gl.glDrawArrays(GL10.GL_POINTS,index,1);
            }
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
            gl.glEnable(GL10.GL_DEPTH_TEST);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            // Get all the buffers ready
            setAllBuffers();
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
            float aspect = (float)width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-aspect, aspect, -1.0f, 1.0f, 1.0f, 10.0f);
        }

        private void setAllBuffers(){
            /*float vertex_list[] = {
                    0.5f, 0.5f, 0.0f,
                    0.3f, 0.4f, 0.2f,
                    -0.2f, 0.3f, 0.1f,
                    -0.5f,-0.2f,-0.3f,
            };*/
            ByteBuffer vbb = ByteBuffer.allocateDirect(vertex_list.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            mVertexBuffer = vbb.asFloatBuffer();
            mVertexBuffer.put(vertex_list);
            mVertexBuffer.position(0);
            vertex_length=(vertex_list.length/3)-1;
        }

        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;
                    mAngleY = (mAngleY + (int)(dx * TOUCH_SCALE_FACTOR) + 360) % 360;
                    mAngleX = (mAngleX + (int)(dy * TOUCH_SCALE_FACTOR) + 360) % 360;
                    break;
            }
            mPreviousX = x;
            mPreviousY = y;
            return true;
        }
    }
}