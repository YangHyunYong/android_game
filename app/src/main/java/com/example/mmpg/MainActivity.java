package com.example.mmpg;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button btn_start, btn_store, btn_setting;    // 각 액티비티로 전환하는 버튼
    String FILENAME = "data.txt";   // 캐릭터, 몬스터 레벨 + 보유 골드

    int characterLv, monsterLv;        // 캐릭터, 몬스터 레벨을 파일에서 불러와 저장할 변수
    long gold;                           // 보유 골드

    int skill1,skill2,skill3, skill4;  // 스킬 레벨

    // data.txt 파일에 있는 데이터를 불러와서 하나의 문자열로 리턴하는 메소드
    private String readText() {
        String data = null;
        try {
            FileInputStream fis = openFileInput(FILENAME);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            data = new String(buffer);
            fis.close();
        }
        catch (IOException e)
        {
            // 처음 실행 시, data.txt 가 없어서 오류가 발생하면 게임 초기 실행 시 데이터로 초기화 하여 data.txt 를 생성한다.
            try {
                // 파일을 수정하기 위해 MODE_PRIVATE 로 설정하여 파일을 저장한다.
                FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                fos.write(("1/1/0/1/1/1/1").getBytes()); // String 문자열을 byte[] 배열로 바꾸는 String 클래스에서 제공하는 메소드
                fos.close();
            }
            catch(IOException e2) {e.printStackTrace();}

            // 게임 초기 데이터로 초기화된 data.txt 를 불러온다.
            try {
                // 파일을 읽어온다.
                FileInputStream fis = openFileInput(FILENAME);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                data = new String(buffer);
                fis.close();
            }
            catch (IOException e3) {e.printStackTrace();}
        }
        // 읽어온 문자열을 반환한다.
        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String data = readText();                   // 파일에서 읽어온 데이터 문자열
        String[] array = data.split("/");    // 문자열의 포함된 /를 통해 캐릭터 레벨, 몬스터 레벨, 보유 재화를 array[0] ~ array[6]에 순서대로 저장한다.

        // 파일에서 읽어온 데이터를 불러와서 저장하고 표기한다.
        characterLv = Integer.valueOf(array[0]);
        monsterLv = Integer.valueOf(array[1]);
        gold = Integer.valueOf(array[2]);

        skill1 = Integer.valueOf(array[3]);
        skill2 = Integer.valueOf(array[4]);
        skill3 = Integer.valueOf(array[5]);
        skill4 = Integer.valueOf(array[6]);

        // 기존의 플레이 하며 변경된 데이터가 저장된 것을 불러온다.
        // 유저 입장에선 애플리케이션을 종료 후 재시작해도 이어할 수 있도록 한다.
        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write((characterLv + "/" + monsterLv + "/" + gold + "/" + skill1 + "/" + skill2 + "/" + skill3 + "/" + skill4).getBytes());
            fos.close();
        }
        catch(Exception e) { e.printStackTrace();}

        btn_start = (Button) findViewById(R.id.button1);
        btn_store = (Button) findViewById(R.id.button2);
        btn_setting = (Button) findViewById(R.id.button3);

        // 각 버튼 클릭 시 다른 액티비티로 전환
        btn_start.setOnClickListener(new View.OnClickListener() {        // 게임 시작 버튼을 누르면 게임 플레이 액티비티로 전환한다.
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, LodingActivity.class);
               startActivity(in);
            }
        });
        btn_store.setOnClickListener(new View.OnClickListener() {        // 게임에서 벌은 재화를 통해 캐릭터를 업그레이드 할 수 있는 상점 액티비티로 전환한다.
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, StoreActivity.class);
                startActivity(in);
            }
        });
        btn_setting.setOnClickListener(new View.OnClickListener() {        // 게임을 진행하면서 필요한 환경 설정을 하기 위한 환경 설정 액티비티로 전환한다.
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(in);
            }
        });
    }

    //액티비티의 메소드 onCreateOptionsMenu()를 재정의 후 메뉴 리소스 팽창
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //메뉴 항목 선택 시 ID 확인 후 실행
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.producer:     // 선택 시 개발자 정보를 보여주는 액티비티로 전환
                Intent intent1 = new Intent(MainActivity.this, ProducerActivity.class);
                startActivity(intent1);
                return true;
            case R.id.evaluate:     // 선택 시 게임을 평가하는 레이팅 바가 있는 대화상자를 보여준다.
                openOptionsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //메뉴 항목 중 게임 평가 선택 시 사용되는 메소드
    private void openOptionsDialog() {
        View linearLayout = getLayoutInflater().inflate(R.layout.ratingdialog, null);
        final AlertDialog.Builder ratingDialog = new AlertDialog.Builder(this);
        final RatingBar rating = (RatingBar) linearLayout.findViewById(R.id.ratingBar);

        ratingDialog.setView(linearLayout);
        ratingDialog.setTitle("게임을 평가해주세요!");

        // 평가하기 버튼을 누른다.
        ratingDialog.setPositiveButton("평가하기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "평가 감사합니다", Toast.LENGTH_SHORT).show();
            }
        });
        // 평가를 취소한다.
        ratingDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(MainActivity.this, "정말요?ㅠㅠ", Toast.LENGTH_SHORT).show();
            }
        });
        ratingDialog.create();     // 게임 평가 다이얼로그 생성
        ratingDialog.show();       // 게임 평가 다이얼로그 화면에 표시
    }
}

