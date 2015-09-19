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
	private Knot knot;//节点对象
	private Food food;//食物对象
	private ArrayList<Knot> snake;
	private static int width = 10; //一个格子长度
	private Button Up,Left,Right,Down;//上下左右按键
	private Button Speedup,Speeddown,Pause;//加速，减速，暂停
	private int count = 0;//吃到的食物个数
	private int baseScore = 200;//吃一个食物的基本分
	private TextView gameScore,gameRate;//游戏分数，当前显示游戏速度
	private float rate = 500;//默认游戏速度
	private boolean stop = true;//控制自动运行
	private boolean stop1 = true;//控制画面绘制
	private boolean stop2 = true;//控制背景音乐播放
	private Handler handler = new  Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what  == 100){
				Log.i("aaa", "自动跑起来了");
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

		init();//初始化蛇，食物
		new Thread(new myRunnable()).start();//定时线程更新UI
		new Thread(new musicRunnable()).start();
		Up.setOnClickListener(this);
		Left.setOnClickListener(this);
		Right.setOnClickListener(this);
		Down.setOnClickListener(this);
		Pause.setOnClickListener(this);
		Speeddown.setOnClickListener(this);
		Speedup.setOnClickListener(this);
		//节点方向：1左  2上  -1右  -2下

	}

	//初始化蛇和食物
	public void init(){
		//初始化蛇
		snake = new ArrayList<MainActivity.Knot>();
		for (int i = 0; i < 6; i++) {                 //初始长度为6
			snake.add(new Knot(i, 0, -1)) ;
		}
		//初始化食物
		food = new Food(0, 0);

		randomFood();

		Draw();
	}

	//蛇类节点
	class Knot{
		int x;    int y;    int d;//节点方向：1左  2上  -1右  -2下
		public Knot(int x, int y, int d) {
			this.x = x;
			this.y = y;
			this.d = d;
		}
	}

	//食物类
	class Food{
		int x;    int y;
		public Food(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	//画网格，画蛇，画食物
	public void Draw(){
		//清除画布
		bitmap.eraseColor(Color.WHITE);
		imageView.setImageBitmap(bitmap);
		//画网格
		//paint.setStyle(Paint.Style.STROKE);
		/*    paint.setColor(Color.BLACK);
				for (int i = 1; i < 30; i++) {
					canvas.drawLine(0, i*width, 300, i*width, paint);
					canvas.drawLine(i*width, 0, i*width, 300, paint);
				}
		 */	
		//画蛇
		//paint.setStyle(Paint.Style.FILL);//设置画笔为实心
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
		//画食物
		paint.setColor(Color.GREEN);
		canvas.drawRect(food.x*width, food.y*width , (food.x+1)*width, (food.y + 1)*width, paint);

		imageView.setImageBitmap(bitmap);

		if (!Test())
		{	   
			myDialog();
		}

	}
	//键盘响应时间
	public void onClick(View view){
		int key = view.getId();//节点方向：1左  2上  -1右  -2下
		int leibie = 0;//0表示方向键，1表示控制键
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
		if(Pause.getText().equals("暂停")){
			Pause.setText("继续");
		}else{
			Pause.setText("暂停");
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
			Toast.makeText(this, "已达最大速度", Toast.LENGTH_SHORT).show();
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
	//移动蛇
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
	//随机食物
	public void randomFood(){
		//Math.randon 返回0~1的数
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
	//5.吃食物
	public boolean eatFood(){
		if (food.x==snake.get(snake.size()-1).x&&food.y==snake.get(snake.size()-1).y)
		{
			count += 1;
			gameScore.setText("游戏分数：" + count*baseScore);
			snake.add(new Knot(food.x, food.y, snake.get(snake.size()-1).d)  );
			randomFood();
			Draw();
			return true;
		}
		return false;
	}

	//7.碰撞检测
	public  boolean Test(){
		if (snake.get(snake.size()-1).x<0||snake.get(snake.size()-1).x>=30||snake.get(snake.size()-1).y<0||snake.get(snake.size()-1).y>=30)
		{
			stop = false;//停止自动运动
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
					stop = false;//停止自动运动
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
	//蛇自动运行
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
	//死亡对话框
	protected void myDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("还要再玩一次吗？");
		builder.setTitle("您的游戏总分为："+count*baseScore);
		builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				bitmap.eraseColor(Color.WHITE);
				canvas.setBitmap(bitmap);
				init();//初始化
				rate = 500;
				stop = true;
				stop1 = true;
				stop2 = true;
				mediaPlayer.start();
				new Thread(new myRunnable()).start();
				new Thread(new musicRunnable()).start();
			}
		});
		builder.setNegativeButton("无情退出", new DialogInterface.OnClickListener() {

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
