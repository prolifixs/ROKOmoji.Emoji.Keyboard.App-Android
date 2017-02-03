package com.rokolabs.keyboard.stickers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rokolabs.keyboard.KeyboardService;
import com.rokolabs.keyboard.packs.PackData;
import com.rokolabs.sdk.http.Response;
import com.rokolabs.sdk.stickers.RokoStickers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mist on 13.12.16.
 */

public class Stickers {
    private static final String TAG = "Stickers";
    private static final String PACK_DATA_LIST = "PACK_DATA_LIST";
    public StickerpacksResponse stickerpacksResponse;
    public List<PackData> packDataList = new ArrayList<PackData>();
    public List<PackData> packDataListDefault = new ArrayList<PackData>();

    private SharedPreferences sharedPreferences;
    private long lastDownload = 0;
    private Context lContext;

    public Stickers(Context context) {
        lContext = context;
        sharedPreferences = context.getSharedPreferences("ROKOmoji", Context.MODE_PRIVATE);
        String packDataListString = sharedPreferences.getString(PACK_DATA_LIST, null);
        if (packDataListString != null) {
            Type listType = new TypeToken<List<PackData>>() {
            }.getType();

            Gson gson = new Gson();
            packDataList = (List<PackData>) gson.fromJson(packDataListString, listType);
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

    public void loadStickers(final CallbackStickersLoaded callback) {
        long timeWaiting = 15 * 60 * 1000; //15 min
        //long timeWaiting = 10000; //10 sec
        if ((System.currentTimeMillis() - lastDownload) > timeWaiting) {
            lastDownload = System.currentTimeMillis();
            Log.i(TAG,"Loading stickers...");
            RokoStickers.getStickerpacks(null, "resolve=stickers", new RokoStickers.CallbackRokoStickers() {
                @Override
                public void success(Response response) {
                    stickerpacksResponse = new Gson().fromJson(response.body, StickerpacksResponse.class);
                    loadImages(callback);
                }

                @Override
                public void failure(Response response) {
                    Log.e(TAG, "loadStickers failure, code: " + response.code);
                }
            });
        }
    }

    public void loadImages(final CallbackStickersLoaded callback) {
        if (stickerpacksResponse != null) {
            try {
                packDataList.clear();
            } catch (Exception e) {
                packDataList = new ArrayList<PackData>();
            }

            for (StickerpacksResponse.Stickerpacks pack : stickerpacksResponse.data) {
                if ("active".equals(pack.liveStatus)) {
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
                                if (itGif(localeFile.getPath())) {
                                    StickerData sd = new StickerData();
                                    sd.objectId = sticker.objectId;
                                    sd.packId = pack.objectId;
                                    sd.packName = pack.name;
                                    sd.file = localeFile;
                                    sd.mime = "image/gif";
                                    sd.url = srFiles.file.url;
                                    sd.imageId = srFiles.file.objectId;
                                    stickerData.add(sd);
                                }
                            }
                        }
                    }
                    if(stickerData.size()>0) {
                        packData.stickers = stickerData;
                        packDataList.add(packData);
                    }
                }
            }
            if(packDataList.size()>0) {
                String json = new Gson().toJson(packDataList);
                sharedPreferences.edit().putString(PACK_DATA_LIST, json).apply();
            }
            if (callback != null) {
                callback.pack();
            }
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
                return outputFile;
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

    private void setDefaultStickerPack() {
        InputStream in = null;
        InputStream inOff = null;
        try {
            in = lContext.getAssets().open("pack/pack_on.png");
            inOff = lContext.getAssets().open("pack/pack_off.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (in != null) {
            long packId = 1;
            PackData packData = new PackData();
            packData.objectId = packId;
            packData.name = "ROKOmoji";
            packData.iconOn = copyImgFile(in, "i" + packId + "_on");
            packData.iconOff = copyImgFile(inOff, "i" + packId + "_off");
            List<StickerData> stickerData = new ArrayList<StickerData>();
            for (int i = 1; i <= 10; i++) {
                InputStream sIs = null;
                try {
                    sIs = lContext.getAssets().open("pack/m" + i + ".gif");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (sIs != null) {
                    StickerData sd = new StickerData();
                    sd.objectId = i;
                    sd.imageId = i;
                    sd.packId = packId;
                    sd.packName = packData.name;
                    sd.file = copyImgFile(sIs, "s" + i);
                    sd.mime = "image/gif";
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

    public interface CallbackStickersLoaded {
        void pack();
    }

}
