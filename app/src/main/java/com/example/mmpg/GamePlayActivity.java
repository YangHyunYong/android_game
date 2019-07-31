package com.example.mmpg;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.BatteryManager;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class GamePlayActivity extends AppCompatActivity {

    public static final String PREFS_NAME1 = "MyPrefs1";

    LinearLayout linearLayout;

    Button btn_s1, btn_s2, btn_s3, btn_s4, btn_reinforce;  // 스킬 버튼
    TextView text_battery;                   // 배터리 퍼센트 표시,
    TextView goldInfo, cLv, mLv, mHP, skill_gauge, needGoldInfo;     // 보유 골드, 캐릭터 레벨, 몬스터 레벨, 캐릭터 데미지, 몬스터 체력, 스킬 게이지
    ImageView image_battery;    // 배터리 이미지 표시
    ImageView player, monster, effect;   // 몬스터와 캐릭터 이미지

    String FILENAME = "data.txt";   // 데이터 파일

    public static final String PREFS_NAME2 = "MyPrefs2";    // 공유 프레퍼런스

    AnimationDrawable animationDrawable, effectDrawable;    // 플레이어 프레임 애니메이션, 보스 프레임 애니메이션

    int characterLv, monsterLv;        // 캐릭터 레벨, 몬스터 레벨
    long monsterHP;         // 몬스터 레벨로 결정되는 몬스터 체력
    long temp_monsterHP = monsterHP;     // 본래 몬스터 체력
    long gold;               // 보유 재화
    int attack_count;       // 공격 횟수
    long needGold;

    int skill1, skill2, skill3, skill4;      // 스킬 레벨
    int skill1_Value, skill2_Value, skill3_Value, skill4_Value;      // 스킬 레벨에 따른 값

    private ProgressBar mProgress, mProgress2;
    private int gauge = 0;

    int attack_count_a, attack_count_g;    // 데미지 2배 지속을 위한 변수
    boolean double_attack, double_gold; // 공격 횟수 두배 , 골드 두배

    int boss_count = 0;

    private int soundID, soundID1, soundID2, soundID3, soundID4;
    private SoundPool sp;

    SharedPreferences settings2;

    // 배터리 정보 관련 함수
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        registerReceiver(receiver, filter);
    }

    public void onBackPressed() {
        AlertDialog.Builder alert_ex = new AlertDialog.Builder(this);
        alert_ex.setMessage("정말로 종료하시겠습니까?");
        alert_ex.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert_ex.setPositiveButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GamePlayActivity.this.finish();
            }
        });
        alert_ex.show();
    }

    // 게임 액티비티를 나가면, 지금까지 획득한 재화와 몬스터 레벨을 저장한다.
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);

        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write((characterLv + "/" + monsterLv + "/" + gold + "/" + skill1 + "/" + skill2 + "/" + skill3 + "/" + skill4).getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 방송수신자를 이용하여 배터리 정보를 표기한다.
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            text_battery.setText(action);


            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int maxvalue = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int value = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int level = value * 100 / maxvalue;
                text_battery.setText(level + "%");

                if (level < 100 && level > 70)
                    image_battery.setImageResource(R.drawable.battery100);
                else if (level <= 70 && level > 30)
                    image_battery.setImageResource(R.drawable.battery70);
                else if (level <= 30)
                    image_battery.setImageResource(R.drawable.battery30);
            } else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
                text_battery.setText("배터리 부족");
            }
        }
    };

    // data.txt 파일에 있는 데이터를 불러와서 하나의 문자열로 리턴하는 메소드
    private String readText() {
        String data = null;
        try {
            FileInputStream fis = openFileInput(FILENAME);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            data = new String(buffer);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        LodingActivity loadingActivity = (LodingActivity) LodingActivity._LoadingActivity;
        loadingActivity.finish();

        linearLayout = (LinearLayout)findViewById(R.id.layout);

        goldInfo = (TextView) findViewById(R.id.gold);
        cLv = (TextView) findViewById(R.id.characterLv);
        mLv = (TextView) findViewById(R.id.monsterLv);
        mHP = (TextView) findViewById(R.id.hp);
        skill_gauge = (TextView) findViewById(R.id.gauge);
        needGoldInfo = (TextView) findViewById(R.id.needGold);

        text_battery = (TextView) findViewById(R.id.text_battery);
        image_battery = (ImageView) findViewById(R.id.image_battery);

        monster = (ImageView) findViewById(R.id.monster);
        player = (ImageView) findViewById(R.id.player);

        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress2 = (ProgressBar) findViewById(R.id.progress_bar2);

        String data = readText();       // 파일에서 읽어온 데이터 문자열
        String[] array = data.split("/");   // 문자열의 포함된 /를 통해 캐릭터 레벨, 몬스터 레벨, 보유 재화를 array[0] ~ array[2]에 순서대로 저장한다.

        // 파일에서 읽어온 데이터를 불러와서 저장하고 표기한다.
        characterLv = Integer.valueOf(array[0]);
        cLv.setText("캐릭터 Lv. " + characterLv);

        monsterLv = Integer.valueOf(array[1]);
        mLv.setText("몬스터 Lv. " + monsterLv);

        gold = Integer.valueOf(array[2]);
        goldInfo.setText("골드 : " + gold);

        skill1 = Integer.valueOf(array[3]);
        skill2 = Integer.valueOf(array[4]);
        skill3 = Integer.valueOf(array[5]);
        skill4 = Integer.valueOf(array[6]);

        skill1_Value = skill1 + 9;
        skill2_Value = skill2 + 19;
        skill3_Value = skill3 + 19;
        skill4_Value = 45 + (skill4 * 5);

        needGold = characterLv * 100;
        needGoldInfo.setText("강화 비용 : " + needGold);

        // 배터리 정보를 표기 설정을 on / off 제어하기 위해 공유 프레퍼런스를 사용하여 저장된 설정 값을 불러온다.
        SharedPreferences settings = getSharedPreferences(PREFS_NAME2, 0);
        final boolean battery = settings.getBoolean("Battery", true);

        // 스킬 버튼
        btn_s1 = (Button) findViewById(R.id.btn_skill1);
        btn_s2 = (Button) findViewById(R.id.btn_skill2);
        btn_s3 = (Button) findViewById(R.id.btn_skill3);
        btn_s4 = (Button) findViewById(R.id.btn_skill4);
        btn_reinforce = (Button) findViewById(R.id.reinforce);

        btn_s1.setText("연타\rLv. " + skill1);
        btn_s2.setText("골드\rLv. " + skill2);
        btn_s3.setText("데미지\rLv." + skill3);
        btn_s4.setText("강타\rLv. " + skill4);

        //몬스터 이미지가 앞으로 오게한다.
        player.bringToFront();

        //몬스터 hp 설정
        monsterHP = monsterLv * 100;
        temp_monsterHP = monsterHP;
        mHP.setText(monsterHP + " / " + temp_monsterHP);
        mProgress2.setMax((int) temp_monsterHP);
        mProgress2.setProgress((int) monsterHP);

        // 불러온 bool 값이 true 이면 표기, false 면 표기하지 않게한다.
        if (battery) {
            image_battery.setVisibility(View.VISIBLE);
            text_battery.setVisibility(View.VISIBLE);
        } else {
            image_battery.setVisibility(View.INVISIBLE);
            text_battery.setVisibility(View.INVISIBLE);
        }

        settings2 = getSharedPreferences(PREFS_NAME1, 0);
        final boolean effects = settings2.getBoolean("Effects", true);

        sp = new SoundPool(3,         // 최대 음악파일의 개수
                AudioManager.STREAM_MUSIC, // 스트림 타입
                0);        // 음질 - 기본값:0

        // 각각의 재생하고자하는 음악을 미리 준비한다
        soundID = sp.load(this, // 현재 화면의 제어권자
                R.raw.reinforce,    // 음악 파일
                1);        // 우선순위

        soundID1 = sp.load(this, // 현재 화면의 제어권자
                R.raw.skill1_sound,    // 음악 파일
                1);        // 우선순위

        soundID2 = sp.load(this, // 현재 화면의 제어권자
                R.raw.skill4_sound,    // 음악 파일
                1);        // 우선순위

        soundID3 = sp.load(GamePlayActivity.this, R.raw.attack, 1);

        soundID4 = sp.load(this, // 현재 화면의 제어권자
                R.raw.skill2_sound,    // 음악 파일
                1);        // 우선순위
        //버튼 클릭 시 특정 스킬 사용
        // 버튼 1 -> 10번 연속 공격

        // 터치를 하여 몬스터를 공격할 때 발생되는 이벤트를 처리한다 -> 골드 증가 / 액션 그래픽 발생 / 몬스터 체력 감소 / 데미지 표시 등
        linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(effects)
                    sp.play(soundID3, 1, 1, 0, 0,1.0f );

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Random ran = new Random();

                    // 캐릭터의 액션 동작을 보여준다.
                    player = findViewById(R.id.player);
                    player.setBackgroundResource(R.drawable.animation);
                    animationDrawable = (AnimationDrawable) player.getBackground();
                    animationDrawable.start();
                    player.bringToFront();

                    if (gauge < 300) {
                        gauge += 50;
                        skill_gauge.setText(gauge + " / 300");
                        mProgress.setProgress(gauge);
                    }

                    // boss 히트 이펙트
                    if (boss_count % 3 == 0) { // 첫번째 보스 일 때
                        monster = findViewById(R.id.monster);
                        monster.setBackgroundResource(R.drawable.boss);
                        effect = findViewById(R.id.attack_effect);
                        effect.setBackgroundResource(R.drawable.bossanimation);
                        effectDrawable = (AnimationDrawable) effect.getBackground();
                        effectDrawable.start();
                        effectDrawable.setVisible(true, true);
                    } else if (boss_count % 3 == 1) { // 두번째 보스 밑에는 위와 같음.
                        monster = findViewById(R.id.monster);
                        monster.setBackgroundResource(R.drawable.boos2sizefix);
                        effect = findViewById(R.id.attack_effect);
                        effect.setBackgroundResource(R.drawable.boss2animation);
                        effectDrawable = (AnimationDrawable) effect.getBackground();
                        effectDrawable.start();
                        effectDrawable.setVisible(true, true);
                    } else if (boss_count % 3 == 2) {
                        monster = findViewById(R.id.monster);
                        monster.setBackgroundResource(R.drawable.boss3);
                        effect = findViewById(R.id.attack_effect);
                        effect.setBackgroundResource(R.drawable.boss3attackanim);
                        effectDrawable = (AnimationDrawable) effect.getBackground();
                        effectDrawable.start();
                        effectDrawable.setVisible(true, true);
                    }

                    // 몬스터 타격 시, 캐릭터 데미지 표시와 몬스터 체력 감소
                    monsterHP -= (characterLv * 2) + ran.nextInt(6);
                    mProgress2.setProgress((int) monsterHP);

                    if (monsterHP > 0) {
                        mHP.setText(monsterHP + " / " + temp_monsterHP);
                    } else if (monsterHP <= 0) {
                        monster.setBackgroundResource(R.drawable.boos2sizefix); // 보스가 죽으면 다음 보스 이미지 설정
                        boss_count++; // 두번째 보스 시작
                        monsterLv++;
                        monsterHP = monsterLv * 100;
                        temp_monsterHP = monsterHP;
                        mProgress2.setMax((int) temp_monsterHP);
                        mProgress2.setProgress((int) monsterHP);
                        mHP.setText(monsterHP + " / " + temp_monsterHP);
                        mLv.setText("몬스터 Lv : " + monsterLv);
                    }

                    // 몬스터 타격 시, 공식대로 골드를 증가
                    gold += monsterLv;
                    goldInfo.setText("골드 : " + gold);

                    if (double_gold == true) {  // 재화 2배 스킬 사용
                        // 몬스터 타격 시, 공식대로 골드를 증가
                        if (attack_count_g == skill2_Value) { // 공격 5번 일 때
                            double_gold = false; // 더블 골드는 끝난다 ( 5회이기 때문 )
                            attack_count_g = 0; // 카운트는 다시 0으로
                        } else {
                            gold += monsterLv * 2; // 몬스터 LV * 2  재화 벌이
                            attack_count_g++; // 어택 카운트 증가
                            Toast.makeText(getApplicationContext(), "공격횟수 " + attack_count_g, Toast.LENGTH_SHORT).show(); // 공격 횟수 표시
                            goldInfo.setText("골드 : " + gold); // 골드 따로 표시 ( 안에서 돌기 때문 )
                        }
                    }

                    if (double_attack == true) {  // 데미지 2배 스킬 사용
                        // 몬스터 타격 시, 공식대로 데미지 증가
                        if (attack_count_a == skill3_Value) {
                            double_attack = false;
                            attack_count_a = 0;
                        } else {
                            attack_count_a++;
                            Toast.makeText(getApplicationContext(), "공격횟수 " + attack_count_a, Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) { // 클릭 후 땠을 때
                    animationDrawable.setVisible(true, true);  // 영상이 반복적으로 되게 해준다.
                    effectDrawable.setVisible(true, true);
                }

                return false;
            }
        });

        btn_s1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (gauge >= 100) {

                    if (effects)
                        sp.play(soundID1, // 준비한 soundID
                                1,         // 왼쪽 볼륨 float 0.0(작은소리)~1.0(큰소리)
                                1,         // 오른쪽볼륨 float
                                0,         // 우선순위 int
                                0,     // 반복회수 int -1 : 무한반복, 0 : 반복안함
                                1.0f);    // 재생속도 float 0.5(절반 속도) ~ 2.0(2 배속)

                    gauge -= 100;
                    skill_gauge.setText(gauge + " / 300");
                    mProgress.setProgress(gauge);

                    while(attack_count != 10)
                    {
                        Random ran = new Random();
                        monsterHP -= Long.valueOf(String.valueOf(characterLv * 2 + ran.nextInt(6)));

                        if(monsterHP > 0) {
                            multi_attack(boss_count);
                            mHP.setText(String.valueOf(monsterHP  + " / " + temp_monsterHP));
                            mProgress2.setProgress((int)monsterHP);
                        }
                        else if(monsterHP < 0) {
                            multi_change(boss_count);
                            monsterLv++;
                            monsterHP = monsterLv * 100;
                            temp_monsterHP = monsterHP;
                            mProgress2.setMax((int) temp_monsterHP);
                            mProgress2.setProgress((int) monsterHP);
                            mHP.setText(monsterHP + " / " + temp_monsterHP);
                            mLv.setText("몬스터 Lv : " + monsterLv);
                            boss_count++;
                            break;
                        }
                        // 몬스터 처치 시, 공식대로 골드를 증가
                        gold += monsterLv;
                        goldInfo.setText("골드 : " + gold);
                        attack_count++;
                    }
                    attack_count=0;
                }
                else
                    Toast.makeText(GamePlayActivity.this, "스킬 게이지가 부족합니다", Toast.LENGTH_SHORT).show();
            }
        });
        btn_s2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (gauge >= 200) {

                    if(effects)
                        sp.play(soundID4, 1, 1, 0, 0,1.0f );

                    gauge -= 200;
                    skill_gauge.setText(gauge + " / 300");
                    mProgress.setProgress(gauge);

                    double_gold = true;  // 카운트 시작
                } else
                    Toast.makeText(GamePlayActivity.this, "스킬 게이지가 부족합니다", Toast.LENGTH_SHORT).show();
            }
        });
        btn_s3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (gauge >= 200) {

                    if(effects)
                        sp.play(soundID4, 1, 1, 0, 0,1.0f );

                    gauge -= 200;
                    skill_gauge.setText(gauge + " / 300");
                    mProgress.setProgress(gauge);

                    double_attack = true;  // 카운트 시작
                } else
                    Toast.makeText(GamePlayActivity.this, "스킬 게이지가 부족합니다", Toast.LENGTH_SHORT).show();
            }
        });

        btn_s4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (gauge >= 300) {

                    if (effects)
                        sp.play(soundID2, // 준비한 soundID
                                1,         // 왼쪽 볼륨 float 0.0(작은소리)~1.0(큰소리)
                                1,         // 오른쪽볼륨 float
                                0,         // 우선순위 int
                                0,     // 반복회수 int -1 : 무한반복, 0 : 반복안함
                                1.0f);    // 재생속도 float 0.5(절반 속도) ~ 2.0(2 배속)

                    gauge -= 300;
                    skill_gauge.setText(gauge + " / 300");
                    mProgress.setProgress(gauge);

                    Random ran = new Random();
                    monsterHP = monsterHP - (characterLv * 50 + ran.nextInt(6) * 10);
                    player.bringToFront();
                    if(monsterHP > 0) {
                        boss_attack(boss_count);
                        mHP.setText(monsterHP + " / " + temp_monsterHP);
                        mProgress2.setProgress((int) monsterHP);
                    }

                    else if (monsterHP < 0) {
                        last_attack(boss_count);
                        monsterLv++;
                        monsterHP = monsterLv * 100;
                        temp_monsterHP = monsterHP;
                        mProgress2.setMax((int) temp_monsterHP);
                        mProgress2.setProgress((int) monsterHP);
                        mHP.setText(monsterHP + " / " + temp_monsterHP);
                        mLv.setText("몬스터 Lv : " + monsterLv);
                        boss_count++;
                    }
                    // 몬스터 타격 시, 공식대로 골드를 증가
                    gold += monsterLv * 100;
                    goldInfo.setText("골드 : " + gold);
                } else
                    Toast.makeText(GamePlayActivity.this, "스킬 게이지가 부족합니다", Toast.LENGTH_SHORT).show();
            }
        });

        btn_reinforce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (needGold > gold)
                    Toast.makeText(GamePlayActivity.this, "보유한 골드가 부족합니다.", Toast.LENGTH_SHORT).show();
                else {
                    // 효과음 프레퍼런스가 true면 효과음 재생
                    if (effects)
                        sp.play(soundID, // 준비한 soundID
                                1,         // 왼쪽 볼륨 float 0.0(작은소리)~1.0(큰소리)
                                1,         // 오른쪽볼륨 float
                                0,         // 우선순위 int
                                0,     // 반복회수 int -1 : 무한반복, 0 : 반복안함
                                1.0f);    // 재생속도 float 0.5(절반 속도) ~ 2.0(2 배속)

                    gold -= needGold;
                    characterLv++;
                    needGold += 100;

                    goldInfo.setText("골드 : " + gold);
                    cLv.setText("캐릭터 Lv. " + characterLv);
                    needGoldInfo.setText("강화 비용 : " + needGold);
                }
            }
        });
    }

    // 자동으로 골드가 증가하는 것을 다루는 핸들러 객체
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateThread();
        }
    };

    // 자동으로 돈을 증가시키는 기능으로, 초당 올라가는 gold값을 표시한다.
    private void updateThread() {
        gold++;
        goldInfo.setText("골드 : " + gold);
    }

    // 액티비티가 실행이 되면 스레드를 통해 gold 값을 1초마다 증가시킨다.
    @Override
    public void onStart() {
        super.onStart();
        Thread myThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        handler.sendMessage(handler.obtainMessage());
                        Thread.sleep(1000);
                    } catch (Throwable t) {
                    }
                }
            }
        });
        myThread.start();
    }

    public void boss_attack(int count){
        if(count%3 == 0) {
            player = findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.smiteplayeranim);
            animationDrawable = (AnimationDrawable) player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true, true);

            monster = findViewById(R.id.attack_effect);
            monster.setBackgroundResource(R.drawable.smitebossanim);
            effectDrawable = (AnimationDrawable) monster.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true, true);
        }else if(count%3==1){

            player = findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.smiteplayeranim);
            animationDrawable = (AnimationDrawable) player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true, true);

            monster = findViewById(R.id.attack_effect);
            monster.setBackgroundResource(R.drawable.smiteboss2anim);
            effectDrawable = (AnimationDrawable) monster.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true, true);
        }
        else if(count%3==2){
            player = findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.smiteplayeranim);
            animationDrawable = (AnimationDrawable) player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true, true);
            monster = findViewById(R.id.attack_effect);
            monster.setBackgroundResource(R.drawable.boss3smite);
            effectDrawable = (AnimationDrawable) monster.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true, true);
        }
    }

    public void last_attack(int count){
        if(count%3 == 0) {
            player = findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.smiteplayeranim);
            animationDrawable = (AnimationDrawable) player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true, true);

            monster = findViewById(R.id.attack_effect);
            monster.setBackgroundResource(R.drawable.returnboss1);
            effectDrawable = (AnimationDrawable) monster.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true, true);
        }else if(count%3==1){

            player = findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.smiteplayeranim);
            animationDrawable = (AnimationDrawable) player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true, true);

            monster = findViewById(R.id.attack_effect);
            monster.setBackgroundResource(R.drawable.returnboss2);
            effectDrawable = (AnimationDrawable) monster.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true, true);
        }
        else if(count%3==2){
            player = findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.smiteplayeranim);
            animationDrawable = (AnimationDrawable) player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true, true);
            monster = findViewById(R.id.attack_effect);
            monster.setBackgroundResource(R.drawable.returnboss3);
            effectDrawable = (AnimationDrawable) monster.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true, true);
        }
    }

    public void multi_attack(int count){
        if(boss_count%3==0){ // 첫번 째 보스일 때
            player=findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.animation);
            animationDrawable=(AnimationDrawable)player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true,true);
            monster=findViewById(R.id.monster);
            monster.setBackgroundResource(R.drawable.boss);
            effect=findViewById(R.id.attack_effect);
            effect.setBackgroundResource(R.drawable.doubleattackanimation);
            effectDrawable=(AnimationDrawable)effect.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true,true);

        }
        else if(boss_count%3==1){ // 두번 째 보스일 때
            player=findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.animation);
            animationDrawable=(AnimationDrawable)player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true,true);
            monster=findViewById(R.id.monster);
            monster.setBackgroundResource(R.drawable.boos2sizefix);
            effect=findViewById(R.id.attack_effect);
            effect.setBackgroundResource(R.drawable.boss2doubleattackanim);
            effectDrawable=(AnimationDrawable)effect.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true,true);
        }
        else if(boss_count%3==2){
            player=findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.animation);
            animationDrawable=(AnimationDrawable)player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true,true);
            monster=findViewById(R.id.monster);
            monster.setBackgroundResource(R.drawable.boss3);
            effect=findViewById(R.id.attack_effect);
            effect.setBackgroundResource(R.drawable.boss3doubleattack_anim);
            effectDrawable=(AnimationDrawable)effect.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true,true);
        }
    }

    public void multi_change(int count){
        if(boss_count%3==0){ // 첫번 째 보스일 때
            player=findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.animation);
            animationDrawable=(AnimationDrawable)player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true,true);
            monster=findViewById(R.id.monster);
            monster.setBackgroundResource(R.drawable.returnboss1);
            effect=findViewById(R.id.attack_effect);
            effect.setBackgroundResource(R.drawable.returnattakboss1);
            effectDrawable=(AnimationDrawable)effect.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true,true);

        }
        else if(boss_count%3==1){ // 두번 째 보스일 때
            player=findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.animation);
            animationDrawable=(AnimationDrawable)player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true,true);
            monster=findViewById(R.id.monster);
            monster.setBackgroundResource(R.drawable.returnboss2);
            effect=findViewById(R.id.attack_effect);
            effect.setBackgroundResource(R.drawable.returnattackboss2);
            effectDrawable=(AnimationDrawable)effect.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true,true);
        }
        else if(boss_count%3==2){
            player=findViewById(R.id.player);
            player.setBackgroundResource(R.drawable.animation);
            animationDrawable=(AnimationDrawable)player.getBackground();
            animationDrawable.start();
            animationDrawable.setVisible(true,true);
            monster=findViewById(R.id.monster);
            monster.setBackgroundResource(R.drawable.returnboss3);
            effect=findViewById(R.id.attack_effect);
            effect.setBackgroundResource(R.drawable.returnattackboss3);
            effectDrawable=(AnimationDrawable)effect.getBackground();
            effectDrawable.start();
            effectDrawable.setVisible(true,true);
        }
    }
}
