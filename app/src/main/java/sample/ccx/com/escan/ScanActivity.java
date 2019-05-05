package sample.ccx.com.escan;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import sample.ccx.com.escan.fragment.ECameraScanFragment;
import sample.ccx.com.escan.fragment.ECreateEncodeQRFragment;
import sample.ccx.com.escan.fragment.ESelectPhotoImageFragment;

public class ScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        String type = getIntent().getStringExtra("fragmentType");
        Fragment fragment = null;
        switch (type) {
            case "create":
                fragment = new ECreateEncodeQRFragment();
                break;
            case "camera":
                getSupportActionBar().hide();
                fragment = new ECameraScanFragment();
                break;
            case "select":
                fragment = new ESelectPhotoImageFragment();
            default:
                finish();
                break;
        }

        getSupportFragmentManager().beginTransaction().add(R.id.zxing_fl, fragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
