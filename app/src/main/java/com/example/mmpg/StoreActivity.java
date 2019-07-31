package com.example.mmpg;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class StoreActivity extends AppCompatActivity {

    ListView list;      // 리스트 뷰로 표현한다.
    CustomList adapter;     // 리스트 뷰를 커스텀 뷰로 표현한다.

    // 리스트 뷰 아이템에 넣을 TextView 와 ImageView 의 text 부분을 setText 하기 위해 배열로 선언하였다.
    String[] titles = { "연타 Lv. " , "골드 2배  Lv. ", "데미지 2배 Lv. ", "강타  Lv. " };     // 스킬 타이틀
    String[] values = { "연타 횟수 ", "골드 2배 횟수 ", "데미지 2배 횟수 ", "강타 데미지 " };     // 스킬 변동 내용
    String[] needGolds = { "강화 비용 : ", "강화 비용 : ", "강화 비용 : ", "강화 비용 : " };     // 스킬 강화 비용
    Integer[] images = { R.drawable.skill1_button, R.drawable.skill2_button, R.drawable.skill3_button,R.drawable.skill4_button };   // 스킬 이미지

    public static final String PREFS_NAME1 = "MyPrefs1";    // 효과음을 제어하기 위한 공유 프레퍼런스
    String FILENAME = "data.txt";            // 게임 플레이에 필요한 데이터를 저장한 텍스트 파일의 이름을 지정
    TextView goldInfo;      // 상점에서 유저가 물건을 사기 위해 가지고 있는 골드를 표시할 TextView

    int characterLv;        // 캐릭터 레벨을 파일에서 불러와 저장할 변수
    int monsterLv;          // 몬스터 레벨
    long gold;              // 보유 재화
    long [] needGold = {0,0,0,0};          // 강화 시 필요 비용

    int []skill_lv = {0,0,0,0};          // 스킬 레벨
    int []skill_value = {0,0,0,0};       // 스킬 레벨에 따른 값

    // data.txt 파일에 있는 데이터를 불러와서 하나의 문자열로 리턴하는 메소드
    private String readText(){
        String data = null;
        try {
            FileInputStream fis = openFileInput(FILENAME);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            data = new String(buffer);
            fis.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        String data = readText();       // 파일에서 읽어온 데이터 문자열

        // 문자열의 포함된 /를 통해 캐릭터 레벨, 몬스터 레벨, 보유 재화를 array[0] ~ array[6]에 순서대로 저장한다.
        String[] array = data.split("/");
        characterLv = Integer.valueOf(array[0]);
        monsterLv = Integer.valueOf(array[1]);
        gold = Integer.valueOf(array[2]);
        skill_lv[0] = Integer.valueOf(array[3]);
        skill_lv[1] = Integer.valueOf(array[4]);
        skill_lv[2] = Integer.valueOf(array[5]);
        skill_lv[3] = Integer.valueOf(array[6]);

        // 보유 골드를 TextView 에 표시한다.
        goldInfo = (TextView)findViewById(R.id.gold);
        goldInfo.setText("보유 골드 : " + gold);

        // 스킬 레벨에 따른 강화 비용을 설정한다. ( 강화 비용 = 레벨 * 500 )
        for(int i = 0; i < 4; i++) {
            needGold[i] = skill_lv[i] * 500;
        }

        // 각각의 스킬 레벨에 따른 스킬의 스킬 값을 설정한다.
        skill_value[0] = skill_lv[0] + 9;   // 레벨 1 기준 : 10 -> 11
        skill_value[1] = skill_lv[1] + 19;  // 레벨 1 기준 : 20 -> 21
        skill_value[2] = skill_lv[2] + 19;  // 레벨 1 기준 : 20 -> 21
        skill_value[3] = 45 + (skill_lv[3] * 5);    // 레벨 1 기준 : 50 -> 55

        // 공유 프레퍼런스를 가져와서 효과음을 제어한다.
        SharedPreferences settings = getSharedPreferences(PREFS_NAME1, 0);
        final boolean effects = settings.getBoolean("Effects", true);

        // 커스텀 리스트 뷰를 적용한다.
        adapter = new CustomList(StoreActivity.this);
        list = (ListView)findViewById(R.id.listView);
        list.setAdapter(adapter);

        // 효과음을 재생할 SoundPool 을 생성한다.
        final SoundPool sp = new SoundPool(1,         // 최대 음악파일의 개수
                AudioManager.STREAM_MUSIC, // 스트림 타입
                0);        // 음질 - 기본값:0

        // 스킬 레벨을 올리면 발생하는 효과음을 설정
        final int soundID = sp.load(this, // 현재 화면의 제어권자
                R.raw.buy_sound,    // 음악 파일
                1);        // 우선순위

        // 리스트 뷰에 아이템을 클릭하여 강화를 진행한다.
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // 돈이 부족하면 토스트 메시지와 함께 강화를 거부한다.
                if (needGold[+position] > gold)
                    Toast.makeText(StoreActivity.this, "보유한 골드가 부족합니다.", Toast.LENGTH_SHORT).show();
                // 돈이 충분하면 강화하고 리스트 뷰 갱신한다.
                else {
                    // effects가 true면 효과음을 재생한다.
                    if(effects)
                        sp.play(soundID, 1, 1, 0, 0, 1.0f);

                    gold -= needGold[+position];    // 강화를 하면 강화 비용 만큼 보유 골드를 빼서 설정
                    skill_lv[+position]++;          // 스킬 레벨을 올리고
                    needGold[+position] += 500;     // 다음 스킬 강화 시 필요한 비용을 설정한다.

                    // 선택한 스킬 1,2,3은 1씩 스킬 값을 증가시키고
                    if(position == 0 || position == 1 || position == 2)
                        skill_value[position] += 1;
                    // 스킬 4는 5씩 스킬 값을 증가시킨다.
                    else if(position == 3)
                        skill_value[position] += 5;

                    adapter.notifyDataSetChanged();     // 아이템을 클릭하여 처리한 이벤트로 바뀐 변수 값을 다시 리스트뷰의 내용을 갱신한다.
                    goldInfo.setText("보유 골드 : " + gold);    // 변경된 보유 골드를 표시한다.
                }
            }
        });
    }

    // 커스텀 리스트를 적용시키기 위한 메소드
    public class CustomList extends ArrayAdapter<String>{
        private final Activity context;

        // 커스텀 리스트 메소드의 생성자
        public CustomList(Activity activity) {
            super(activity, R.layout.item, titles);
            this.context = activity;
        }

        // 커스텀 리스트의 아이템의 각각의 TextView 와 ImageView 의 내용을 설정한다.
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate((R.layout.item), null, true);

            // 각각의 아이템 행의 TextView와 ImageView의 ID를 가져온다.
            ImageView imageView = (ImageView) rowView.findViewById(R.id.reinforce);
            TextView title = (TextView) rowView.findViewById(R.id.skill_title);
            TextView value = (TextView) rowView.findViewById(R.id.skill_value);
            TextView needGold = (TextView) rowView.findViewById(R.id.skill_needGold);

            // 해당 아이템 행의 내용을 설정한다.
            imageView.setImageResource(images[position]);
            title.setText(titles[position] + skill_lv[position]);

            // 해당 아이템 행의 스킬의 스킬 값과 다음 레벨의 스킬 값을 표시한다.
            if(position == 0 || position == 1 || position == 2)
                value.setText(values[position] + skill_value[position] + " -> " + (skill_value[position] + 1));
            else if(position == 3)
                value.setText(values[position] + skill_value[position] + " -> " + (skill_value[position] + 5));

            // 해당 아이템 행의 스킬 강화 비용을 표시한다.
            needGold.setText(needGolds[position] + (skill_lv[position] * 500));

            return rowView;
        }
    }

    // 상점 액티비티를 종료하면 변경된 내용을 data.txt 파일에 저장한다.
    @Override
    protected void onPause() {
        super.onPause();

        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write((characterLv + "/" + monsterLv + "/" + gold + "/" + skill_lv[0] + "/" + skill_lv[1] + "/" + skill_lv[2] + "/" + skill_lv[3]).getBytes());
            fos.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
