package com.onedime.vrifymobile.screenshots;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ScreenshotActivity extends AppCompatActivity
{
    private static final int REQUEST_SCREENSHOT = 59706;
    private MediaProjectionManager projectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(),
                REQUEST_SCREENSHOT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCREENSHOT)
        {
            if (resultCode == RESULT_OK)
            {
                Intent screenshotIntent = new Intent(this, ScreenshotService.class);
                screenshotIntent.putExtra(ScreenshotService.EXTRA_RESUT_CODE, resultCode);
                screenshotIntent.putExtra(ScreenshotService.EXTRA_RESULT_INTENT, data);
                startService(screenshotIntent);
            }
        }

        finish();
    }
}