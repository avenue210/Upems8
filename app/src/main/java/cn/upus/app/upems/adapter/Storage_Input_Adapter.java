package cn.upus.app.upems.adapter;

import android.support.annotation.Nullable;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.upus.app.upems.R;
import cn.upus.app.upems.bean.StorageEntity;

/**
 * 配送 寄 手动输入 适配器
 */
public class Storage_Input_Adapter extends BaseQuickAdapter<StorageEntity, BaseViewHolder> {

    private Deposit mDeposit;
    private Delete mDelete;
    private OpenLock mOpenLock;

    public void getOpenLock(OpenLock openLock) {
        this.mOpenLock = openLock;
    }

    public void getDelete(Delete delete) {
        this.mDelete = delete;
    }

    public void getDeposit(Deposit deposit) {
        this.mDeposit = deposit;
    }

    public interface Delete {
        void callBack(StorageEntity item);
    }

    public interface Deposit {
        void callBack(StorageEntity item);
    }

    public interface OpenLock {
        void callBack(StorageEntity item);
    }

    public Storage_Input_Adapter(int layoutResId, @Nullable List<StorageEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, StorageEntity item) {
        helper.setText(R.id.tv_shelfno, item.getPosno() + " (" + item.getKindna() + ")");
        helper.setText(R.id.tv_barno, item.getBarno());
        helper.setText(R.id.tv_tel, item.getTel());

        Button bt_deposit = helper.getView(R.id.bt_deposit);
        Button bt_open_lock = helper.getView(R.id.bt_open_lock);
        Button bt_delete = helper.getView(R.id.bt_delete);

        bt_deposit.setOnClickListener(v -> {
            if (null != mDeposit) {
                mDeposit.callBack(item);
            }
        });
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

    }
}
