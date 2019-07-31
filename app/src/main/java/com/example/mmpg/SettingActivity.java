package com.example.mmpg;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;

public class SettingActivity extends AppCompatActivity implements  View.OnClickListener{

    public static final String PREFS_NAME1 = "MyPrefs1";
    public static final String PREFS_NAME2 = "MyPrefs2";

    String FILENAME = "data.txt";   // 캐릭터, 몬스터 레벨 + 보유 골드 + 스킬 레벨

    boolean effects, battery_s;     // 공유 프레퍼런스의 boolean 값을 저장할 변수

    // 각각의 환경 설정 ON / OFF 버튼을 ImageButton 으로 표현하였다.
    ImageButton bgmON, bgmOFF, effectON, effectOFF, batteryON, batteryOFF, reset;

    // 게임 데이터 초기화 버튼 클릭 시, 유저에게 다시 한번 의도를 묻는 대화상자이다.
    private void openOptionsDialog() {
        View linearLayout = getLayoutInflater().inflate(R.layout.resetdialog, null);
        final AlertDialog.Builder resetDialog = new AlertDialog.Builder(this);

        // 대화 상자의 제목을 설정하고 현재 레이아웃에 표현한다.
        resetDialog.setView(linearLayout);
        resetDialog.setTitle("주의");

        // 초기화 버튼을 클릭 시 데이터를 초기화 한다.
        resetDialog.setPositiveButton("초기화", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(SettingActivity.this, "초기화 되었습니다.", Toast.LENGTH_SHORT).show();

                // 처음 초기 데이터를 data.txt 파일에 쓴다. ( 1,1,0,1,1,1,1 )
                try {
                    // 파일을 수정하기 위해 MODE_PRIVATE 로 설정하여 파일을 저장한다.
                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    fos.write(("1/1/0/1/1/1/1").getBytes()); // String 문자열을 byte[] 배열로 바꾸는 String 클래스에서 제공하는 메소드
                    fos.close();
                }
                catch(IOException e) {e.printStackTrace();}
            }
        });
        // 취소 버튼 클릭 시, 초기화 취소
        resetDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        resetDialog.create();     // 게임 초기화 재확인 다이얼로그 생성
        resetDialog.show();       // 게임 초기화 재확인 다이얼로그 화면에 표시
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 각 해당 ON/OFF 버튼에 이벤트 리스너를 추가한다.
        bgmON = (ImageButton)findViewById(R.id.bgmon);
        bgmOFF = (ImageButton)findViewById(R.id.bgmoff);

        bgmON.setOnClickListener(this);
        bgmOFF.setOnClickListener(this);

        effectON = (ImageButton)findViewById(R.id.effecton);
        effectOFF = (ImageButton)findViewById(R.id.effectoff);

        effectON.setOnClickListener(this);
        effectOFF.setOnClickListener(this);

        batteryON = (ImageButton)findViewById(R.id.batteryon);
        batteryOFF = (ImageButton)findViewById(R.id.batteryoff);

        batteryON.setOnClickListener(this);
        batteryOFF.setOnClickListener(this);

        reset = (ImageButton)findViewById(R.id.reset);
        reset.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.bgmon:
                Toast.makeText(this, "BGM 을 재생합니다.", Toast.LENGTH_SHORT).show();
                startService(new Intent(this, BGMService.class));       // 서비스를 시작하여 BGM 을 재생한다.
                break;

            case R.id.bgmoff:
                Toast.makeText(this, "BGM 을 정지합니다.", Toast.LENGTH_SHORT).show();
                stopService(new Intent(this, BGMService.class));        // 서비스를 종료하여 BGM 을 중지한다.
                break;

            case R.id.effecton:
                Toast.makeText(this, "효과음을 재생합니다.", Toast.LENGTH_SHORT).show();
                effects = true;     // boolean 값을 true로 설정한다.
                SharedPreferences Effects = getSharedPreferences(PREFS_NAME1, 0);
                SharedPreferences.Editor editor = Effects.edit();
                editor.putBoolean("Effects", effects);      // true 값을 공유 프레퍼런스 값을 저장한다.
                editor.commit();    // 변경된 공유 프레퍼런스 설정 완료
                break;

            case R.id.effectoff:
                Toast.makeText(this, "효과음을 정지합니다.", Toast.LENGTH_SHORT).show();
                effects = false;
                Effects = getSharedPreferences(PREFS_NAME1, 0);
                editor = Effects.edit();
                editor.putBoolean("Effects", effects);      // false 값을 공유 프레퍼런스 값을 저장한다.
                editor.commit();    // 변경된 공유 프레퍼런스 설정 완료
                break;

            case R.id.batteryon:
                Toast.makeText(this, "배터리 정보를 표시합니다.", Toast.LENGTH_SHORT).show();
                battery_s = true;
                SharedPreferences Battery = getSharedPreferences(PREFS_NAME2, 0);
                editor = Battery.edit();
                editor.putBoolean("Battery", battery_s);    // true 값을 공유 프레퍼런스 값을 저장한다.
                editor.commit();     // 변경된 공유 프레퍼런스 설정 완료
                break;

            case R.id.batteryoff:
                Toast.makeText(this, "배터리 정보를 표시하지 않습니다.", Toast.LENGTH_SHORT).show();
                battery_s = false;
                Battery = getSharedPreferences(PREFS_NAME2, 0);
                editor = Battery.edit();
                editor.putBoolean("Battery", battery_s);    // false 값을 공유 프레퍼런스 값을 저장한다.
                editor.commit();    // 변경된 공유 프레퍼런스 설정 완료
                break;

            case R.id.reset:
                openOptionsDialog();    // 초기화 버튼을 클릭하며 대화 상자를 표시한다.
                break;
        }
    }
}
