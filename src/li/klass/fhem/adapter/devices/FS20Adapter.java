package li.klass.fhem.adapter.devices;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.ToggleButton;
import li.klass.fhem.R;
import li.klass.fhem.activities.CurrentActivityProvider;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.activities.deviceDetail.FS20DeviceDetailActivity;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.service.ExecuteOnSuccess;
import li.klass.fhem.service.FS20Service;
import li.klass.fhem.service.RoomListService;

import java.util.List;

public class FS20Adapter extends DeviceDetailAvailableAdapter<FS20Device> {

    private class UpdateCurrentActivityOnSuccess implements ExecuteOnSuccess {

        @Override
        public void onSuccess() {
            BaseActivity<?,?> currentActivity = CurrentActivityProvider.INSTANCE.getCurrentActivity();
            if (currentActivity != null) {
                currentActivity.update(false);
            }
        }
    }
    
    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        public int progress;

        private SeekBarChangeListener(int progress) {
            this.progress = progress;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            this.progress = progress;

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            FS20Device device = RoomListService.INSTANCE.deviceListForAllRooms(false).getDeviceFor((String) seekBar.getTag());
            FS20Service.INSTANCE.dim(seekBar.getContext(), device, progress, new UpdateCurrentActivityOnSuccess());
        }
    }

    @Override
    public View getDeviceView(LayoutInflater layoutInflater, FS20Device device) {
        if (device.isDimDevice()) {
            return getFS20SeekView(layoutInflater, device);
        } else {
            return getFS20ToggleView(layoutInflater, device);
        }
    }

    @Override
    protected void fillDeviceDetailView(final Context context, View view, final FS20Device device) {
        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, device.getState());

        TableRow seekBarRow = (TableRow) view.findViewById(R.id.switchSeekBarRow);
        TableRow toggleButtonRow = (TableRow) view.findViewById(R.id.switchToggleButtonRow);

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        ToggleButton switchButton = (ToggleButton) view.findViewById(R.id.switchButton);

        if (device.isDimDevice()) {
            int initialProgress = device.getFS20DimState();
            seekBar.setProgress(initialProgress);
            seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener(device.getFS20DimState()));
            seekBar.setTag(device.getName());

            toggleButtonRow.setVisibility(View.GONE);
        } else {
            switchButton.setChecked(device.isOn());
            switchButton.setTag(device.getName());

            seekBarRow.setVisibility(View.GONE);
        }

        Button switchSetOptions = (Button) view.findViewById(R.id.switchSetOptions);
        switchSetOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
                contextMenu.setTitle(context.getResources().getString(R.string.switchDevice));
                final List<String> setOptions = device.getSetOptions();

                contextMenu.setItems(setOptions.toArray(new CharSequence[setOptions.size()]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String option = setOptions.get(item);
                        FS20Service.INSTANCE.setState(context, device, option, new UpdateCurrentActivityOnSuccess());
                        dialog.dismiss();
                    }
                });
                contextMenu.show();

            }
        });
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, FS20DeviceDetailActivity.class);
        return intent;
    }

    private View getFS20ToggleView(LayoutInflater layoutInflater, FS20Device child) {
        View view = layoutInflater.inflate(R.layout.room_detail_fs20, null);

        setTextView(view, R.id.deviceName, child.getAliasOrName());

        ToggleButton switchButton = (ToggleButton) view.findViewById(R.id.switchButton);
        switchButton.setChecked(child.isOn());
        switchButton.setTag(child.getName());

        return view;
    }

    private View getFS20SeekView(LayoutInflater layoutInflater, final FS20Device child) {
        View view = layoutInflater.inflate(R.layout.room_detail_fs20_seek, null);

        setTextView(view, R.id.deviceName, child.getAliasOrName());

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        final int initialProgress = child.getFS20DimState();
        seekBar.setProgress(initialProgress);
        seekBar.setTag(child.getName());

        seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener(child.getFS20DimState()));

        return view;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_fs20;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return FS20Device.class;
    }

    @Override
    protected Class<? extends Activity> getDeviceDetailActivity() {
        return FS20DeviceDetailActivity.class;
    }
}