package com.rokolabs.rokomoji.stickers;

import java.util.List;

/**
 * Created by mist on 13.12.16.
 */

public class StickerpacksResponse {
    public String apiStatusCode;
    public List<Stickerpacks> data;

    public static class Stickerpacks {
        public long objectId;

        public String name;
        public String liveStatus;
        public String displayType;
        public String createDate;
        public String updateDate;
        public PackIconFileGroup packIconFileGroup;
        public UnselectedPackIconFileGroup unselectedPackIconFileGroup;
        public PackStickers stickers[];

        public static class PackIconFileGroup {
            public long objectId;
            public SRFiles files[];
        }

        public static class UnselectedPackIconFileGroup {
            public long objectId;
            public SRFiles files[];
        }

        public static class PackStickers {
            public long objectId;
            public String createDate;
            public String updateDate;
            public Double scaleFactor;
            public ImageFileGroup imageFileGroup;

            class ImageFileGroup {
                public long objectId;
                public SRFiles files[];
            }
        }

        class SRFiles {
            File file;
            String alias;

            class File {
                public long objectId;
                String url;
            }
        }
    }
}
