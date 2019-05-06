package cn.upus.app.upems.adapter;

import android.support.annotation.Nullable;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.upus.app.upems.R;
import cn.upus.app.upems.bean.PartParameterEntity;

/**
 * 刷卡取件 列表 22
 */
public class PayByCardTakeListAdapter extends BaseQuickAdapter<PartParameterEntity, BaseViewHolder> {

    private OpenLock mOpenLock;

    public void getOpenLock(OpenLock openLock) {
        this.mOpenLock = openLock;
    }

    public interface OpenLock {
        void callBack(PartParameterEntity item);
    }

    public PayByCardTakeListAdapter(int layoutResId, @Nullable List<PartParameterEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PartParameterEntity item) {
        helper.setText(R.id.tv_shelfno, item.getShelfno());
        helper.setText(R.id.tv_barno, item.getExpno());
        Button bt_open_lock = helper.getView(R.id.bt_open_lock);
        bt_open_lock.setOnClickListener(v -> {
            if (null != mOpenLock) {
                mOpenLock.callBack(item);
            }
        });
    }
}
