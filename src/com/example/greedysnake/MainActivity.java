package com.example.greedysnake;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener {

	private SoundPool soundPool;
	private int soundBg,soundOver;
	private MediaPlayer mediaPlayer;
	private ImageView imageView;
	private Paint paint;
	private Canvas canvas;
	private Bitmap bitmap;
	private Knot knot;//�ڵ����
	private Food food;//ʳ�����
	private ArrayList<Knot> snake;
	private static int width = 10; //һ�����ӳ���
	private Button Up,Left,Right,Down;//�������Ұ���
	private Button Speedup,Speeddown,Pause;//���٣����٣���ͣ
	private int count = 0;//�Ե���ʳ�����
	private int baseScore = 200;//��һ��ʳ��Ļ�����
	private TextView gameScore,gameRate;//��Ϸ��������ǰ��ʾ��Ϸ�ٶ�
	private float rate = 500;//Ĭ����Ϸ�ٶ�
	private boolean stop = true;//�����Զ�����
	private boolean stop1 = true;//���ƻ������
	private boolean stop2 = true;//���Ʊ������ֲ���
	private Handler handler = new  Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what  == 100){
				Log.i("aaa", "�Զ���������");
				if(stop1){
					moveSnake(snake.get(snake.size()-1).d);
					Draw();
				}
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		imageView = (ImageView) findViewById(R.id.imageView);
		Up = (Button) findViewById(R.id.Up);
		Left = (Button) findViewById(R.id.Left);
		Right = (Button) findViewById(R.id.Right);
		Down = (Button) findViewById(R.id.Down);
		gameScore = (TextView) findViewById(R.id.gameScore);
		gameRate = (TextView) findViewById(R.id.gameRate);
		Pause = (Button) findViewById(R.id.Pause);
		Speeddown = (Button) findViewById(R.id.Speeddown);
		Speedup = (Button) findViewById(R.id.Speedup);

		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		soundOver = soundPool.load(this, R.raw.gameover, 1);

		mediaPlayer = MediaPlayer.create(this, R.raw.onepiece);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		paint = new Paint();
		paint.setStrokeWidth(1);
		paint.setColor(Color.BLACK);
		bitmap = Bitmap.createBitmap(300, 300, Config.ARGB_8888);
		canvas = new Canvas( bitmap);
		canvas.drawColor(Color.WHITE);		

		init();//��ʼ���ߣ�ʳ��
		new Thread(new myRunnable()).start();//��ʱ�̸߳���UI
		new Thread(new musicRunnable()).start();
		Up.setOnClickListener(this);
		Left.setOnClickListener(this);
		Right.setOnClickListener(this);
		Down.setOnClickListener(this);
		Pause.setOnClickListener(this);
		Speeddown.setOnClickListener(this);
		Speedup.setOnClickListener(this);
		//�ڵ㷽��1��  2��  -1��  -2��

	}

	//��ʼ���ߺ�ʳ��
	public void init(){
		//��ʼ����
		snake = new ArrayList<MainActivity.Knot>();
		for (int i = 0; i < 6; i++) {                 //��ʼ����Ϊ6
			snake.add(new Knot(i, 0, -1)) ;
		}
		//��ʼ��ʳ��
		food = new Food(0, 0);

		randomFood();

		Draw();
	}

	//����ڵ�
	class Knot{
		int x;    int y;    int d;//�ڵ㷽��1��  2��  -1��  -2��
		public Knot(int x, int y, int d) {
			this.x = x;
			this.y = y;
			this.d = d;
		}
	}

	//ʳ����
	class Food{
		int x;    int y;
		public Food(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	//�����񣬻��ߣ���ʳ��
	public void Draw(){
		//�������
		bitmap.eraseColor(Color.WHITE);
		imageView.setImageBitmap(bitmap);
		//������
		//paint.setStyle(Paint.Style.STROKE);
		/*    paint.setColor(Color.BLACK);
				for (int i = 1; i < 30; i++) {
					canvas.drawLine(0, i*width, 300, i*width, paint);
					canvas.drawLine(i*width, 0, i*width, 300, paint);
				}
		 */	
		//����
		//paint.setStyle(Paint.Style.FILL);//���û���Ϊʵ��
		for (int  i = 0; i < snake.size() ;i++ )
		{
			if(i==snake.size()-1){
				paint.setColor(Color.RED);
			}else{
				paint.setColor(Color.BLACK);
			}
			canvas.drawRect(snake.get(i).x*width, snake.get(i).y*width, 
					(snake.get(i).x+1)*width, (snake.get(i).y+1)*width, paint);
		}
		//��ʳ��
		paint.setColor(Color.GREEN);
		canvas.drawRect(food.x*width, food.y*width , (food.x+1)*width, (food.y + 1)*width, paint);

		imageView.setImageBitmap(bitmap);

		if (!Test())
		{	   
			myDialog();
		}

	}
	//������Ӧʱ��
	public void onClick(View view){
		int key = view.getId();//�ڵ㷽��1��  2��  -1��  -2��
		int leibie = 0;//0��ʾ�������1��ʾ���Ƽ�
		switch (key) {
		case R.id.Left:  if (snake.get(snake.size()-2).d !=-1 ) {
			snake.get(snake.size()-1).d = 1;
		}
		break;
		case R.id.Up:  if (snake.get(snake.size()-2).d !=-2 ) {
			snake.get(snake.size()-1).d = 2;
		}
		break;
		case R.id.Right:  if (snake.get(snake.size()-2).d != 1 ) {
			snake.get(snake.size()-1).d = -1;
		}
		break;
		case R.id.Down:  if (snake.get(snake.size()-2).d !=2 ) {
			snake.get(snake.size()-1).d = -2;
		}
		break;
		case R.id.Pause:   if(stop == true){
			stop1 = false;
			stop = false;
		}else{
			stop = true;
			stop1 = true;
			new Thread(new myRunnable()).start();
		}
		if(Pause.getText().equals("��ͣ")){
			Pause.setText("����");
		}else{
			Pause.setText("��ͣ");
		}
		leibie = 1;
		break;
		case R.id.Speeddown:   rate += 100;
		gameRate.setText(1000/rate+"fps");
		leibie = 1;
		break;
		case R.id.Speedup:      if(rate >100){rate -=100;
		gameRate.setText(1000/rate+"fps");
		}else{
			Toast.makeText(this, "�Ѵ�����ٶ�", Toast.LENGTH_SHORT).show();
		}

		leibie = 1;
		break;
		}

		if ( snake.get(snake.size()-2).d != snake.get(snake.size()-1).d && leibie == 0)
		{
			moveSnake(snake.get(snake.size()-1).d);
			Draw();
		}


	}
	//�ƶ���
	public void moveSnake(int direction){
		if (!eatFood()) {
			for (int i = 1; i < snake.size() ;i++ )
			{
				snake.get(i-1).x = snake.get(i).x;
				snake.get(i-1).y = snake.get(i).y;
				snake.get(i-1).d = snake.get(i).d;
			}
		}

		switch (direction)
		{
		case 1: snake.get(snake.size()-1).x--; break;
		case 2: snake.get(snake.size()-1).y--; break;
		case -1: snake.get(snake.size()-1).x++; break;
		case -2: snake.get(snake.size()-1).y++; break;
		}

	}
	//���ʳ��
	public void randomFood(){
		//Math.randon ����0~1����
		food.x = (int) Math.ceil(Math.random()*28 + 1);
		food.y = (int) Math.ceil(Math.random()*28 + 1);
		for (int i = 0;i < snake.size() ;i++ )
		{
			if (food.x==snake.get(i).x&&food.y==snake.get(i).y)
			{
				randomFood();
			}
		}  
	}
	//5.��ʳ��
	public boolean eatFood(){
		if (food.x==snake.get(snake.size()-1).x&&food.y==snake.get(snake.size()-1).y)
		{
			count += 1;
			gameScore.setText("��Ϸ������" + count*baseScore);
			snake.add(new Knot(food.x, food.y, snake.get(snake.size()-1).d)  );
			randomFood();
			Draw();
			return true;
		}
		return false;
	}

	//7.��ײ���
	public  boolean Test(){
		if (snake.get(snake.size()-1).x<0||snake.get(snake.size()-1).x>=30||snake.get(snake.size()-1).y<0||snake.get(snake.size()-1).y>=30)
		{
			stop = false;//ֹͣ�Զ��˶�
			stop1 = false;
			stop2 = false;
			mediaPlayer.pause();
			soundPool.play(soundOver, 1.0f, 1.0f, 0, 0, 1.0f);
			return false;
		}
		if (eatFood())
		{
			return true;
		}else{
			for (int i = 0;i<snake.size()-2 ;i++ )
			{
				if (snake.get(snake.size()-1).x==snake.get(i).x&&snake.get(snake.size()-1).y==snake.get(i).y)
				{
					stop = false;//ֹͣ�Զ��˶�
					stop1 = false;
					stop2 = false;
					mediaPlayer.pause();
					soundPool.play(soundOver, 1.0f, 1.0f, 0, 0, 1.0f);
					return false;
				}
			}
		}
		return true;
	}
	//���Զ�����
	private class myRunnable implements Runnable{

		@Override
		public void run() {
			while(stop){
				try {
					Thread.sleep((long) rate);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.sendEmptyMessage(100);
			}
		}
	}
	//musicRunnable
	private class musicRunnable implements Runnable{

		@Override
		public void run() {
			while(stop2){
				try {
					mediaPlayer.prepare();
				} catch (Exception e) {
					e.printStackTrace();
				}
				mediaPlayer.start();
			}

		}

	}
	//�����Ի���
	protected void myDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("��Ҫ����һ����");
		builder.setTitle("������Ϸ�ܷ�Ϊ��"+count*baseScore);
		builder.setPositiveButton("�õ�", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				bitmap.eraseColor(Color.WHITE);
				canvas.setBitmap(bitmap);
				init();//��ʼ��
				rate = 500;
				stop = true;
				stop1 = true;
				stop2 = true;
				mediaPlayer.start();
				new Thread(new myRunnable()).start();
				new Thread(new musicRunnable()).start();
			}
		});
		builder.setNegativeButton("�����˳�", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
				finish();
			}
		});
		builder.create().show();
	}


}
