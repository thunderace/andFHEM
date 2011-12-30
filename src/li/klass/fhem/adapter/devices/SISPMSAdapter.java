package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.SISPMSDevice;

public class SISPMSAdapter extends DeviceListOnlyAdapter<SISPMSDevice> {

    @Override
    public View getDeviceView(LayoutInflater layoutInflater, SISPMSDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_sispms, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(device.getAliasOrName());

        ToggleButton switchButton = (ToggleButton) view.findViewById(R.id.switchButton);
        switchButton.setChecked(device.isOn());
        switchButton.setTag(device);

        return view;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return SISPMSDevice.class;
    }
}
