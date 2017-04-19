package com.rokolabs.rokomoji.stickers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rokolabs.rokomoji.BuildConfig;
import com.rokolabs.rokomoji.KeyboardService;
import com.rokolabs.rokomoji.packs.PackData;
import com.rokolabs.sdk.http.Response;
import com.rokolabs.sdk.stickers.RokoStickers;
import com.rokolabs.sdk.tools.ThreadUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by mist on 13.12.16.
 */

public class Stickers {
    private static final String TAG = "Stickers";
    private static final String PACK_DATA_LIST = "PACK_DATA_LIST";
    private static final String SAVE_VERSION = "SAVE_VERSION";
    public StickerpacksResponse stickerpacksResponse;
    public final List<PackData> packDataList;
    public List<PackData> packDataListDefault = new ArrayList<PackData>();

    private SharedPreferences sharedPreferences;
    private long lastDownload = 0;
    private Context lContext;

    public Stickers(Context context) {
        lContext = context;
        sharedPreferences = context.getSharedPreferences("ROKOmoji", Context.MODE_PRIVATE);
        checkVersion(false);
        String packDataListString = sharedPreferences.getString(PACK_DATA_LIST, null);
        packDataList = new ArrayList<>();
        if (packDataListString != null) {
            Type listType = new TypeToken<List<PackData>>() {
            }.getType();

            Gson gson = new Gson();
            packDataList.addAll((Collection<? extends PackData>) gson.fromJson(packDataListString, listType));
        } else {
            setDefaultStickerPack();
        }
    }

    public static Boolean itGif(String pathName) {
        if ("image/gif".equals(getMimeTypeOfFile(pathName))) {
            return true;
        }
        return false;
    }

    public static String getMimeTypeOfFile(String pathName) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, opt);
        return opt.outMimeType;
    }

    public static BitmapFactory.Options getBitmapOptions(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inJustDecodeBounds = false;
        return options;
    }

    public void loadStickers(final CallbackStickersLoaded callback) {
        long timeWaiting = 15 * 60 * 1000; //15 min
        //long timeWaiting = 10000; //10 sec
        if ((System.currentTimeMillis() - lastDownload) > timeWaiting) {
            lastDownload = System.currentTimeMillis();
            Log.i(TAG, "--- Loading stickers...");
            RokoStickers.getStickerpacks(null, "resolve=stickers", new RokoStickers.CallbackRokoStickers() {
                @Override
                public void success(Response response) {
                    stickerpacksResponse = new Gson().fromJson(response.body, StickerpacksResponse.class);
                    loadImages(callback);
                    Log.i(TAG, "--- Loading stickers complete");
                }

                @Override
                public void failure(Response response) {
                    Log.e(TAG, "loadStickers failure, code: " + response.code);
                }
            });
        } else {
            callback.pack();
        }
    }

    public void loadImages(final CallbackStickersLoaded callback) {
        if (stickerpacksResponse != null) {
            packDataList.clear();
            List<StickerpacksResponse.Stickerpacks> activePacks = new ArrayList<>();
            for (final StickerpacksResponse.Stickerpacks pack : stickerpacksResponse.data) {
                if (!"active".equals(pack.liveStatus)) {
                    continue;
                }
                activePacks.add(pack);
            }
            if(activePacks.size() == 0)
                return;

            packDataList.addAll(Arrays.asList(new PackData[activePacks.size()]));
            final CountDownLatch countDownLatch = new CountDownLatch(activePacks.size());

            int i = 0;
            for (final StickerpacksResponse.Stickerpacks pack : activePacks) {
                final int finalI = i++;
                ThreadUtils.runOnBackground(new Runnable() {
                    @Override
                    public void run() {
                        File packIconOn = downloadFile(pack.packIconFileGroup.files[0].file.url, "i" + pack.packIconFileGroup.files[0].file.objectId);
                        File packIconOff = downloadFile(pack.unselectedPackIconFileGroup.files[0].file.url, "i" + pack.unselectedPackIconFileGroup.files[0].file.objectId);

                        PackData packData = new PackData();
                        packData.objectId = pack.objectId;
                        packData.name = pack.name;
                        packData.iconOn = packIconOn;
                        packData.iconOff = packIconOff;
                        List<StickerData> stickerData = new ArrayList<StickerData>();

                        for (StickerpacksResponse.Stickerpacks.PackStickers sticker : pack.stickers) {
                            for (StickerpacksResponse.Stickerpacks.SRFiles srFiles : sticker.imageFileGroup.files) {
                                File localeFile = downloadFile(srFiles.file.url, "s" + srFiles.file.objectId);
                                if (localeFile != null) {
                                    File iconFile = createIconKey(localeFile, "si" + srFiles.file.objectId);
                                    if (iconFile != null) {
                                        StickerData sd = new StickerData();
                                        sd.objectId = sticker.objectId;
                                        sd.packId = pack.objectId;
                                        sd.packName = pack.name;
                                        sd.file = localeFile;
                                        sd.iconKey = iconFile;
                                        sd.mime = getMimeTypeOfFile(localeFile.getPath()); //"image/gif";
                                        sd.url = srFiles.file.url;
                                        sd.imageId = srFiles.file.objectId;
                                        stickerData.add(sd);
                                    }
                                }
                            }
                        }
                        synchronized (packDataList) {
                            if (stickerData.size() > 0) {
                                packData.stickers = stickerData;
                                packDataList.set(finalI, packData);
                            }

                            if (packDataList.size() > 0) {
                                String json = new Gson().toJson(packDataList);
                                sharedPreferences.edit().putString(PACK_DATA_LIST, json).apply();
                                sharedPreferences.edit().putInt(SAVE_VERSION, BuildConfig.VERSION_CODE).apply();
                            }

                            countDownLatch.countDown();
                            if (callback != null) {
                                callback.pack();
                            }
                        }
                    }
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Resize bitmap for icon key
    private File createIconKey(File imageFile, String localeFileName) {
        final File outputFile = new File(KeyboardService.imagesDir, localeFileName);
        if (outputFile.exists()) {
            return outputFile;
        }
        try {
            OutputStream dataWriter = new FileOutputStream(outputFile);
            Bitmap bm = BitmapFactory.decodeFile(imageFile.getPath());
            if (bm == null) {
                imageFile.delete();
                return null;
            }
            try {
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, lContext.getResources().getDisplayMetrics());
                int optimaDp = Math.round(px);
                //int height = Math.round(bm.getHeight() / (bm.getWidth() / optimaWidth));

                int width = 0;
                int height = 0;
                if(bm.getWidth() > bm.getHeight()){
                    double kf = (double) bm.getWidth() / (double) optimaDp;
                    width = optimaDp;
                    height = (int) Math.round((double) bm.getHeight() / kf);
                } else if (bm.getHeight() > bm.getWidth()){
                    double kf = ((double) bm.getHeight() / (double) optimaDp);
                    height=optimaDp;
                    width = (int) Math.round((double) bm.getWidth() / kf);
                } else {
                    width=optimaDp;
                    height=optimaDp;
                }

                Log.v("TAG",localeFileName+"-------------------------------------------------------");
                Log.v("TAG","w: "+bm.getWidth()+" h: "+bm.getHeight());
                Log.v("TAG","w: "+width+" h1: "+height);

                Bitmap ico = Bitmap.createScaledBitmap(bm, width, height, true);
                ico.compress(Bitmap.CompressFormat.PNG, 100, dataWriter);
                Log.i(TAG, "create icon " + outputFile.getName() + ": " + outputFile.length() + " bytes; Width: " + width + "px Height: " + height);
                return outputFile;
            } finally {
                if (dataWriter != null) {
                    dataWriter.flush();
                    dataWriter.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private File downloadFile(String url, String localeFileName) {
        final File outputFile = new File(KeyboardService.imagesDir, localeFileName);
        if (outputFile.exists()) {
            return outputFile;
        }
        try {
            InputStream resourceReader = (InputStream) new URL(url).getContent();
            final byte[] buffer = new byte[4096];
            OutputStream dataWriter = new FileOutputStream(outputFile);
            try {
                while (true) {
                    final int numRead = resourceReader.read(buffer);
                    if (numRead <= 0) {
                        break;
                    }
                    dataWriter.write(buffer, 0, numRead);
                }
                if (outputFile.length() > 0) {
                    Log.i(TAG, "load file: " + outputFile.getName() + ": " + outputFile.length() + " bytes");
                    return outputFile;
                } else {
                    outputFile.delete();
                    return null;
                }
            } finally {
                if (dataWriter != null) {
                    dataWriter.flush();
                    dataWriter.close();
                }
                if (resourceReader != null) {
                    resourceReader.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void defAppPack(){
        // in = lContext.getAssets().open("pack/pack_on.png");

        try {
            String packList[] = lContext.getAssets().list("pack_app");
            for(String img: packList){
                Log.i("###>>>",img);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setDefaultStickerPack() {
        checkVersion(true);
        InputStream in = null;
        String packList[]=new String[0];
        final String PACK_LIB="pack";
        final String PACK_APP="pack_app";
        final String PACK_ICON="pack_on.png";
        String curAssets="";

        try {
            in = lContext.getAssets().open(PACK_APP+"/"+PACK_ICON);
            curAssets=PACK_APP;
            packList = lContext.getAssets().list(curAssets);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(in==null) {
            try {
                in = lContext.getAssets().open(PACK_LIB+"/"+PACK_ICON);
                curAssets=PACK_LIB;
                packList = lContext.getAssets().list(curAssets);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (in != null) {
            long packId = 1;
            PackData packData = new PackData();
            packData.objectId = packId;
            packData.name = "ROKOmoji";
            packData.iconOn = copyImgFile(in, "i" + packId + "_on");
            //packData.iconOff = copyImgFile(inOff, "i" + packId + "_off");
            List<StickerData> stickerData = new ArrayList<StickerData>();
            long i = 0;
            for (String img: packList) {
                if(PACK_ICON.equals(img)){
                    continue;
                }
                InputStream sIs = null;
                try {
                    sIs = lContext.getAssets().open(curAssets+"/"+img);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (sIs != null) {
                    StickerData sd = new StickerData();
                    i=i+1;
                    File file = copyImgFile(sIs, "s" + img);
                    sd.objectId = i;
                    sd.imageId = i;
                    sd.packId = packId;
                    sd.packName = packData.name;
                    sd.file = file;
                    sd.iconKey = createIconKey(file, "si" + img);
                    sd.mime = getMimeTypeOfFile(file.getPath());//"image/gif"
                    sd.url = null;
                    stickerData.add(sd);
                }
            }
            packData.stickers = stickerData;
            packDataListDefault.add(packData);
        }
    }

    private File copyImgFile(InputStream resourceReader, String localeFileName) {
        try {
            final File tempFile = new File(KeyboardService.imagesDir, localeFileName);
            final byte[] buffer = new byte[4096];
            OutputStream dataWriter = null;
            try {
                dataWriter = new FileOutputStream(tempFile);
                while (true) {
                    final int numRead = resourceReader.read(buffer);
                    if (numRead <= 0) {
                        break;
                    }
                    dataWriter.write(buffer, 0, numRead);
                }

                return tempFile;

            } finally {
                if (dataWriter != null) {
                    dataWriter.flush();
                    dataWriter.close();
                }
                if (resourceReader != null) {
                    resourceReader.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkVersion(Boolean del) {
        //BuildConfig.VERSION_CODE;
        int saveVersion = sharedPreferences.getInt(SAVE_VERSION, 0);
        Log.i(TAG, "Check version: old: "+saveVersion+" current: "+BuildConfig.VERSION_CODE);
        if ((saveVersion != BuildConfig.VERSION_CODE) || del) {
            sharedPreferences.edit().clear().commit();
            clearCash(KeyboardService.imagesDir);
            clearCash(KeyboardService.tempDir);
        }
    }

    public void clearCash(File dir) {
        Log.d(TAG, "--- Clear cash ---");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    Log.v(TAG, "--- " + file.getName() + " delete");
                    file.delete();
                }
            }
        }
    }

    public interface CallbackStickersLoaded {
        void pack();
    }

}
