package cn.upus.app.upems.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.ui.activity.*;

public class WelComeActivity extends AppCompatActivity {

    //{"hasscn":"1","kindno":"20","kindna":"有屏校园配餐柜","cateno":"2"},
    //{"hasscn":"1","kindno":"22","kindna":"有屏刷卡存包柜","cateno":"2"},
    //{"hasscn":"1","kindno":"23","kindna":"有屏扫码存包柜","cateno":"2"},
    //{"hasscn":"1","kindno":"25","kindna":"有屏社区快递柜","cateno":"2"},
    //{"hasscn":"1","kindno":"26","kindna":"有屏商务快递柜","cateno":"2"},
    //{"hasscn":"1","kindno":"29","kindna":"有屏智能洗衣柜","cateno":"2"},
    //{"hasscn":"1","kindno":"32","kindna":"横屏智能寄存柜","cateno":"2"},
    //{"hasscn":"1","kindno":"33","kindna":"有屏智能钥匙柜","cateno":"2"},
    //{"hasscn":"1","kindno":"34","kindna":"有屏智能网板柜","cateno":"2"},
    //{"hasscn":"1","kindno":"35","kindna":"有屏智能信报箱","cateno":"2"}

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //startActivity(new Intent(this, Main5.class));

        String devkind = MApp.mSp.getString(UserData.DEVKIND);
        if (MApp.mSp.getBoolean(UserData.isHorizontal)) {
            switch (devkind) {
                case "20"://有屏校园配餐柜
                case "22"://有屏刷卡存包柜
                case "23"://有屏扫码存包柜
                case "25"://有屏社区快递柜
                    startActivity(new Intent(this, Main3.class));
                    break;
                case "26"://有屏商务快递柜
                    startActivity(new Intent(this, Main2.class));
                    break;
                case "29"://有屏智能洗衣柜
                    startActivity(new Intent(this, Main1.class));
                    break;
                case "32"://横屏智能寄存柜

                    break;
                case "33"://有屏智能钥匙柜
                    startActivity(new Intent(this, Main6.class));
                    break;
                case "34"://有屏智能网板柜
                    startActivity(new Intent(this, Main7.class));
                    break;
                case "35"://有屏智能信报箱
                    startActivity(new Intent(this, Main4.class));
                    break;
                default:
                    startActivity(new Intent(this, Main3.class));
                    break;
            }
        } else {
            switch (devkind) {
                case "20"://有屏校园配餐柜
                case "22"://有屏刷卡存包柜
                case "23"://有屏扫码存包柜
                case "25"://有屏社区快递柜
                    startActivity(new Intent(this, Main1.class));
                    break;
                case "26"://有屏商务快递柜
                    startActivity(new Intent(this, Main2.class));
                    break;
                case "29"://有屏智能洗衣柜
                    startActivity(new Intent(this, Main1.class));
                    break;
                case "32"://横屏智能寄存柜

                    break;
                case "33"://有屏智能钥匙柜
                    startActivity(new Intent(this, Main6.class));
                    break;
                case "34"://有屏智能网板柜
                    startActivity(new Intent(this, Main7.class));
                    break;
                case "35"://有屏智能信报箱
                    startActivity(new Intent(this, Main4.class));
                    break;
                default:
                    startActivity(new Intent(this, Main1.class));
                    break;
            }
        }
        finish();
    }

}
