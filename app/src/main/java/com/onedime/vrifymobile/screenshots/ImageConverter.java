package com.onedime.vrifymobile.screenshots;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.view.Display;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ImageConverter implements ImageReader.OnImageAvailableListener
{
    private int width;
    private int height;
    private ImageReader reader;
    private ScreenshotService screenshotService;
    private Bitmap lastScreenshot;
    private List<byte[]> recentScreenshots = new ArrayList<>();

    public ImageConverter(ScreenshotService service)
    {
        this.screenshotService = service;
        Display display = this.screenshotService.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);

        width = point.x;
        height = point.y;
        while((width * height) > (2 << 19))
        {
            width = width >> 1;
            height = height >> 1;
        }

        this.reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        this.reader.setOnImageAvailableListener(this, this.screenshotService.getHandler());
    }

    private byte[][] convertToArray(List<byte[]> bitmapList)
    {
        byte[][] images = new byte[bitmapList.size()][];
        for(int index = 0; index < bitmapList.size(); index++)
        {
            images[index] = bitmapList.get(index);
        }

        return images;
    }
    @Override
    public void onImageAvailable(ImageReader imageReader)
    {
        Image image = this.reader.acquireLatestImage();
        if(image != null)
        {
            Image.Plane[] planes = image.getPlanes();
            for(int index = 0; index < planes.length; index++)
            {
                Image.Plane plane = planes[index];ByteBuffer buffer = plane.getBuffer();
                int pixelStride = plane.getPixelStride();
                int rowStride = plane.getRowStride();
                int padding = rowStride - pixelStride * width;
                int bitmapWidth = width + padding / pixelStride;

                if(lastScreenshot == null ||
                        lastScreenshot.getWidth() != bitmapWidth ||
                        lastScreenshot.getHeight() != height)
                {
                    if(lastScreenshot != null)
                    {
                        lastScreenshot.recycle();
                    }

                    lastScreenshot = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888);
                }

                lastScreenshot.copyPixelsFromBuffer(buffer);
                image.close();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Bitmap cropped = Bitmap.createBitmap(lastScreenshot, 0, 0, width, height);
                cropped.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] imageData = outputStream.toByteArray();
                this.screenshotService.handleImage(imageData);
            }
            this.getSurface().release();
            this.close();
        }
    }

    Surface getSurface()
    {
        return this.reader.getSurface();
    }

    int getWidth()
    {
        return width;
    }

    int getHeight()
    {
        return height;
    }

    void close()
    {
        this.reader.close();
    }
}
