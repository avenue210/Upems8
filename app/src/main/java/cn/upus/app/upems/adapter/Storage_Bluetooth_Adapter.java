package cn.upus.app.upems.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.upus.app.upems.R;
import cn.upus.app.upems.bean.StorageEntity;

/**
 * 配送 寄 扫描录入 适配器
 */
public class Storage_Bluetooth_Adapter extends BaseQuickAdapter<StorageEntity, BaseViewHolder> {

    private Delete mDelete;
    private OpenLock mOpenLock;

    private String shelfno = null;

    public void setShelfno(String shelfno) {
        this.shelfno = shelfno;
    }

    public void getOpenLock(OpenLock openLock) {
        this.mOpenLock = openLock;
    }

    public void getDelete(Delete delete) {
        this.mDelete = delete;
    }

    public interface Delete {
        void callBack(StorageEntity item);
    }

    public interface OpenLock {
        void callBack(StorageEntity item);
    }

    public Storage_Bluetooth_Adapter(int layoutResId, @Nullable List<StorageEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, StorageEntity item) {
        helper.setText(R.id.tv_shelfno, item.getPosno() + " (" + item.getKindna() + ")");
        helper.setText(R.id.tv_barno, item.getBarno());
        helper.setText(R.id.tv_tel, item.getTel());

        Button bt_open_lock = helper.getView(R.id.bt_open_lock);
        Button bt_delete = helper.getView(R.id.bt_delete);

        bt_open_lock.setOnClickListener(v -> {
            if (null != mOpenLock) {
                mOpenLock.callBack(item);
            }
        });
        bt_delete.setOnClickListener(v -> {
            if (null != mDelete) {
                mDelete.callBack(item);
            }
        });

        if (!TextUtils.isEmpty(shelfno)) {
            CardView cardView = helper.getView(R.id.card_view);
            if (shelfno.equals(item.getShelfno())) {
                cardView.setCardBackgroundColor(0x30ff0000);
            } else {
                cardView.setCardBackgroundColor(0xffffff);
            }
        }
    }
}
