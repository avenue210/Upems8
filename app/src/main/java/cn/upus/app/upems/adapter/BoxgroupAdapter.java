package cn.upus.app.upems.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SpanUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;
import java.util.Objects;

import cn.upus.app.upems.R;
import cn.upus.app.upems.bean.BoxgroupEntity;
import cn.upus.app.upems.ui.view.CustomerKeyboard;

/**
 * 配餐柜 分组适配
 */
public class BoxgroupAdapter extends BaseQuickAdapter<BoxgroupEntity.DetailBean, BaseViewHolder> {

    private Activity context;
    private InputComplete mInputComplete;

    public void setInputComplete(Activity context, InputComplete inputComplete) {
        this.context = context;
        this.mInputComplete = inputComplete;
    }

    public interface InputComplete {
        void callBack(BoxgroupEntity.DetailBean detailBean, int number);
    }

    public BoxgroupAdapter(int layoutResId, @Nullable List<BoxgroupEntity.DetailBean> data) {
        super(layoutResId, data);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void convert(BaseViewHolder helper, BoxgroupEntity.DetailBean item) {
        helper.setText(R.id.tv_kindna, item.getKindna());
        helper.setText(R.id.tv_size, String.valueOf(item.getSize()));

        TextView tv_numble = helper.getView(R.id.tv_numble);
        Button bt_box_delete = helper.getView(R.id.bt_box_delete);
        Button bt_box_add = helper.getView(R.id.bt_box_add);
        tv_numble.setText("0");

        if (null != mInputComplete) {
            tv_numble.addTextChangedListener(new MTextWatcher(item, mInputComplete));
        }

        bt_box_add.setOnClickListener(v -> {
            int numble = Integer.parseInt(tv_numble.getText().toString());
            if (numble < item.getSize()) {
                tv_numble.setText((numble + 1) + "");
                item.setAddSize(Integer.parseInt(tv_numble.getText().toString()));
            }
        });

        bt_box_delete.setOnClickListener(v -> {
            int numble = Integer.parseInt(tv_numble.getText().toString());
            if (numble > 0) {
                tv_numble.setText((numble - 1) + "");
                item.setAddSize(Integer.parseInt(tv_numble.getText().toString()));
            }
        });

        tv_numble.setOnClickListener(v -> {
            StringBuilder stringBuilder = new StringBuilder();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle(context.getResources().getString(R.string.tips_input_numbel));
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_box_numbel_input, null);
            builder.setView(view);

            CustomerKeyboard customerKeyboard = view.findViewById(R.id.custom_key_board);
            TextView tv = view.findViewById(R.id.tv);

            customerKeyboard.setOnCustomerKeyboardClickListener(new CustomerKeyboard.CustomerKeyboardClickListener() {
                @Override
                public void click(String number) {
                    stringBuilder.append(number);
                    tv.setText(stringBuilder.toString());
                }

                @Override
                public void delete() {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                        tv.setText(stringBuilder.toString());
                    } else {
                        tv.setText("0");
                    }
                }
            });

            builder.setPositiveButton(context.getResources().getString(R.string.ok_sure), (dialog, which) -> {
                if (!TextUtils.isEmpty(stringBuilder.toString())) {
                    if (Integer.valueOf(stringBuilder.toString()) > item.getSize()) {
                        ToastUtils.setBgColor(Color.RED);
                        ToastUtils.setMsgColor(Color.WHITE);
                        ToastUtils.showShort(new SpanUtils()
                                .appendImage(R.drawable.ic_tip, SpanUtils.ALIGN_CENTER)
                                .append("  ")
                                .append(context.getResources().getString(R.string.The_number_of_input_is_larger_than_that_of_the_remaining_goods))
                                .setForegroundColor(Color.WHITE)
                                .setFontSize(16, true)
                                .create()
                        );
                    } else {
                        tv_numble.setText(stringBuilder.toString());
                        item.setAddSize(Integer.parseInt(tv_numble.getText().toString()));
                    }
                }
            });
            builder.setNegativeButton(context.getResources().getString(R.string.ok_cancel), (dialog, which) -> {

            });
            BarUtils.setNavBarVisibility(context, false);
            BarUtils.setStatusBarVisibility(context, false);

            AlertDialog alertDialog = builder.create();
            Objects.requireNonNull(alertDialog.getWindow()).setGravity(Gravity.CENTER);
            alertDialog.setCanceledOnTouchOutside(false);
            builder.show();
        });
    }


    class MTextWatcher implements TextWatcher {

        private BoxgroupEntity.DetailBean item;
        private InputComplete mInputComplete;

        public MTextWatcher(BoxgroupEntity.DetailBean item, InputComplete mInputComplete) {
            this.item = item;
            this.mInputComplete = mInputComplete;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            //用户输入完毕后，处理输入数据，回调给主界面处理
            if (s != null) {
                mInputComplete.callBack(item, Integer.parseInt(s.toString()));
            }
        }
    }
}
