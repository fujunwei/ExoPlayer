/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer.demo;

import com.google.android.exoplayer.util.Util;

import java.util.Locale;

/**
 * Holds statically defined sample definitions.
 */
/* package */ class Samples {

  public static class Sample {

    public final String name;
    public final String contentId;
    public final String provider;
    public final String uri;
    public final int type;

    public Sample(String name, String uri, int type) {
      this(name, name.toLowerCase(Locale.US).replaceAll("\\s", ""), "", uri, type);
    }

    public Sample(String name, String contentId, String provider, String uri, int type) {
      this.name = name;
      this.contentId = contentId;
      this.provider = provider;
      this.uri = uri;
      this.type = type;
    }

  }

  public static final Sample[] YOUTUBE_DASH_MP4 = new Sample[] {
    new Sample("Google Glass (MP4,H264)",
        "http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?"
        + "as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&"
        + "ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7."
        + "8506521BFC350652163895D4C26DEE124209AA9E&key=ik0", Util.TYPE_DASH),
    new Sample("Google Play (MP4,H264)",
        "http://www.youtube.com/api/manifest/dash/id/3aa39fa2cc27967f/source/youtube?"
        + "as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&"
        + "ipbits=0&expire=19000000000&signature=A2716F75795F5D2AF0E88962FFCD10DB79384F29."
        + "84308FF04844498CE6FBCE4731507882B8307798&key=ik0", Util.TYPE_DASH),
  };

  public static final Sample[] YOUTUBE_DASH_WEBM = new Sample[] {
    new Sample("Google Glass (WebM,VP9)",
        "http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?"
        + "as=fmp4_audio_clear,webm2_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&"
        + "ipbits=0&expire=19000000000&signature=249B04F79E984D7F86B4D8DB48AE6FAF41C17AB3."
        + "7B9F0EC0505E1566E59B8E488E9419F253DDF413&key=ik0", Util.TYPE_DASH),
    new Sample("Google Play (WebM,VP9)",
        "http://www.youtube.com/api/manifest/dash/id/3aa39fa2cc27967f/source/youtube?"
        + "as=fmp4_audio_clear,webm2_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&"
        + "ipbits=0&expire=19000000000&signature=B1C2A74783AC1CC4865EB312D7DD2D48230CC9FD."
        + "BD153B9882175F1F94BFE5141A5482313EA38E8D&key=ik0", Util.TYPE_DASH),
  };

  public static final Sample[] SMOOTHSTREAMING = new Sample[] {
    new Sample("Super speed",
        "http://playready.directtaps.net/smoothstreaming/SSWSS720H264/SuperSpeedway_720.ism",
        Util.TYPE_SS),
    new Sample("Super speed (PlayReady)",
        "http://playready.directtaps.net/smoothstreaming/SSWSS720H264PR/SuperSpeedway_720.ism",
        Util.TYPE_SS),
  };

  private static final String WIDEVINE_GTS_MPD =
      "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd";
  public static final Sample[] WIDEVINE_GTS = new Sample[] {
    new Sample("WV: HDCP not specified", "d286538032258a1c", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
    new Sample("WV: HDCP not required", "48fcc369939ac96c", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
    new Sample("WV: HDCP required", "e06c39f1151da3df", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
    new Sample("WV: Secure video path required (MP4,H264)", "0894c7c8719b28a0", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
    new Sample("WV: Secure video path required (WebM,VP9)", "0894c7c8719b28a0", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/vp9/tears/tears.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Secure video path required (MP4,H265)", "0894c7c8719b28a0", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd",
        Util.TYPE_DASH),
    new Sample("WV: HDCP + secure video path required", "efd045b1eb61888a", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
    new Sample("WV: 30s license duration (fails at ~30s)", "f9a34cab7b05881a", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
  };

  public static final Sample[] WIDEVINE_HDCP = new Sample[] {
    new Sample("WV: HDCP: None (not required)", "HDCP_None", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
    new Sample("WV: HDCP: 1.0 required", "HDCP_V1", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
    new Sample("WV: HDCP: 2.0 required", "HDCP_V2", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
    new Sample("WV: HDCP: 2.1 required", "HDCP_V2_1", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
    new Sample("WV: HDCP: 2.2 required", "HDCP_V2_2", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
    new Sample("WV: HDCP: No digital output", "HDCP_NO_DIGTAL_OUTPUT", "widevine_test",
        WIDEVINE_GTS_MPD, Util.TYPE_DASH),
  };

  public static final Sample[] WIDEVINE_H264_MP4_CLEAR = new Sample[] {
    new Sample("WV: Clear SD & HD (MP4,H264)",
        "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Clear SD (MP4,H264)",
        "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears_sd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Clear HD (MP4,H264)",
        "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears_hd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Clear UHD (MP4,H264)",
        "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears_uhd.mpd",
        Util.TYPE_DASH),
  };

  public static final Sample[] WIDEVINE_H264_MP4_SECURE = new Sample[] {
    new Sample("WV: Secure SD & HD (MP4,H264)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Secure SD (MP4,H264)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears_sd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Secure HD (MP4,H264)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears_hd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Secure UHD (MP4,H264)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears_uhd.mpd",
        Util.TYPE_DASH),
  };

  public static final Sample[] WIDEVINE_VP9_WEBM_CLEAR = new Sample[] {
    new Sample("WV: Clear SD & HD (WebM,VP9)",
        "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Clear SD (WebM,VP9)",
        "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears_sd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Clear HD (WebM,VP9)",
        "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears_hd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Clear UHD (WebM,VP9)",
        "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears_uhd.mpd",
        Util.TYPE_DASH),
  };

  public static final Sample[] WIDEVINE_VP9_WEBM_SECURE = new Sample[] {
    new Sample("WV: Secure SD & HD (WebM,VP9)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/vp9/tears/tears.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Secure SD (WebM,VP9)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/vp9/tears/tears_sd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Secure HD (WebM,VP9)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/vp9/tears/tears_hd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Secure UHD (WebM,VP9)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/vp9/tears/tears_uhd.mpd",
        Util.TYPE_DASH),
  };

  public static final Sample[] WIDEVINE_H265_MP4_CLEAR = new Sample[] {
    new Sample("WV: Clear SD & HD (MP4,H265)",
        "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Clear SD (MP4,H265)",
        "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears_sd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Clear HD (MP4,H265)",
        "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears_hd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Clear UHD (MP4,H265)",
        "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears_uhd.mpd",
        Util.TYPE_DASH),
  };

  public static final Sample[] WIDEVINE_H265_MP4_SECURE = new Sample[] {
    new Sample("WV: Secure SD & HD (MP4,H265)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Secure SD (MP4,H265)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears_sd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Secure HD (MP4,H265)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears_hd.mpd",
        Util.TYPE_DASH),
    new Sample("WV: Secure UHD (MP4,H265)", "", "widevine_test",
        "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears_uhd.mpd",
        Util.TYPE_DASH),
  };

  public static final Sample[] HLS = new Sample[] {
    new Sample("Apple master playlist",
        "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/"
        + "bipbop_4x3_variant.m3u8", Util.TYPE_HLS),
          new Sample("Le TV",
                  "http://65.255.32.146/182/32/96/letv-uts/14/ver_00_22-1038632618-avc-420125-aac-32001-2702040-155657976-db70ee048c713186109dfca48e72ed84-1461166590594.m3u8?crypt=82aa7f2e421&b=460&nlh=4096&nlt=60&bf=73&p2p=1&video_type=mp4&termid=2&tss=ios&platid=3&splatid=301&its=0&qos=3&fcheck=0&mltag=84&proxy=1805330700,1805331980,1805330700&uid=3227962428.rp&keyitem=GOw_33YJAAbXYE-cnQwpfLlv_b2zAkYctFVqe5bsXQpaGNn3T1-vhw..&ntm=1462769400&nkey=7c9a5aacff778ab74c848e8a7299adc6&nkey2=a3e1eefab47eaf4fe752b0bfaec740a1&geo=US-0-0-100&p1=0&payff=0&ostype=android&cvid=1217581064718&g3proxy_tm=1462758450&playid=0&vid=25177029&g3proxy_ercode=0&hwtype=un&uip=192.102.204.60&vtype=13&tm=1462758464&mmsid=53688911&uuid=1758462523258904&p2=04&key=c9a085954976c79d729da5eecefc8b16&errc=0&gn=997&rtmcd=104&buss=84&cips=192.102.204.60&jsonp=vjs_146275849214674", Util.TYPE_HLS),
    new Sample("Apple master playlist advanced",
        "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/"
        + "bipbop_16x9_variant.m3u8", Util.TYPE_HLS),
    new Sample("Apple TS media playlist",
        "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/"
        + "prog_index.m3u8", Util.TYPE_HLS),
    new Sample("Apple AAC media playlist",
        "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear0/"
        + "prog_index.m3u8", Util.TYPE_HLS),
    new Sample("Apple ID3 metadata", "http://devimages.apple.com/samplecode/adDemo/ad.m3u8",
        Util.TYPE_HLS),
  };

  public static final Sample[] MISC = new Sample[] {
    new Sample("Dizzy", "http://html5demos.com/assets/dizzy.mp4", Util.TYPE_OTHER),
    new Sample("Letv", "http://153.3.167.168/192/14/78/letv-gug/17/ver_00_22-1039861798-avc-254784-aac-32493-14982-560712-7f6e54761eca430f47317d63d2f37750-1461750064985.mp4?crypt=38aa7f2e131400&b=1314&nlh=4096&nlt=60&bf=8000&p2p=1&video_type=mp4&termid=0&tss=no&platid=100&splatid=10000&its=0&qos=5&fcheck=0&mltag=1&proxy=2567153569,2567123691,467476875&uid=2053118450.rp&keyitem=GOw_33YJAAbXYE-cnQwpfLlv_b2zAkYctFVqe5bsXQpaGNn3T1-vhw..&ntm=1494118800&nkey=9a62fe32327d43861350733665c56f5f&nkey2=fd41e82ce2a45e60e5325018669dd9e9&geo=CN-10-127-2&gugtype=1&mmsid=54659091&type=m_liuchang_mp4&errc=0&gn=1114&rtmcd=104&buss=1&cips=122.96.25.242", Util.TYPE_OTHER),
          new Sample("Youku_1", "http://103.38.59.16/youku/6572A72CF903378AD898046C2/03002001005721B16ECE05003E88037E969A53-228F-72D7-A8C5-B434E9A4547F.mp4", Util.TYPE_OTHER),
          new Sample("Youku_2", "http://38.98.63.18/youku/6766AC86093E818E33AF36CE5/0300080A0056FCA666C35E2BEEFCF99A5FAF56-554A-D894-0E24-C0C3D42BFEA2.mp4", Util.TYPE_OTHER),
          new Sample("Youku_3", "http://120.52.73.4/103.38.59.54/youku/6767C944C6477C26314C3276/0300080A0156FCA666C35E2BEEFCF99A5FAF56-554A-D894-0E24-C0C3D42BFEA2.mp4", Util.TYPE_OTHER),
          new Sample("Youku_4", "http://120.52.73.6/103.38.59.53/youku/6768E60812457BCD54325CAB/0300080A0256FCA666C35E2BEEFCF99A5FAF56-554A-D894-0E24-C0C3D42BFEA2.mp4", Util.TYPE_OTHER),
          new Sample("Youku_5", "http://120.52.73.7/103.38.59.44/youku/696C3C47FC3478D9FD5A5D12/0300080A0356FCA666C35E2BEEFCF99A5FAF56-554A-D894-0E24-C0C3D42BFEA2.mp4", Util.TYPE_OTHER),
          new Sample("Youku_6", "http://38.98.63.21/youku/6568E60CEA308132CFB9D41D0/0300080A0456FCA666C35E2BEEFCF99A5FAF56-554A-D894-0E24-C0C3D42BFEA2.mp4", Util.TYPE_OTHER),
          new Sample("Youku_7", "http://120.52.73.5/103.38.59.16/youku/676C3C47DC4A7CAB71B95716/0300080A0556FCA666C35E2BEEFCF99A5FAF56-554A-D894-0E24-C0C3D42BFEA2.mp4", Util.TYPE_OTHER),
          new Sample("Youku_8", "http://120.52.73.6/103.38.59.55/youku/656B1F894D477C2631934BDD/0300080A0656FCA666C35E2BEEFCF99A5FAF56-554A-D894-0E24-C0C3D42BFEA2.mp4", Util.TYPE_OTHER),
          new Sample("Apple AAC 10s", "https://devimages.apple.com.edgekey.net/"
        + "streaming/examples/bipbop_4x3/gear0/fileSequence0.aac", Util.TYPE_OTHER),
    new Sample("Apple TS 10s", "https://devimages.apple.com.edgekey.net/streaming/examples/"
        + "bipbop_4x3/gear1/fileSequence0.ts", Util.TYPE_OTHER),
    new Sample("Android screens (Matroska)", "http://storage.googleapis.com/exoplayer-test-media-1/"
        + "mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv",
        Util.TYPE_OTHER),
    new Sample("Big Buck Bunny (MP4 Video)",
        "http://redirector.c.youtube.com/videoplayback?id=604ed5ce52eda7ee&itag=22&source=youtube&"
        + "sparams=ip,ipbits,expire,source,id&ip=0.0.0.0&ipbits=0&expire=19000000000&signature="
        + "513F28C7FDCBEC60A66C86C9A393556C99DC47FB.04C88036EEE12565A1ED864A875A58F15D8B5300"
        + "&key=ik0", Util.TYPE_OTHER),
    new Sample("Google Play (MP3 Audio)",
        "http://storage.googleapis.com/exoplayer-test-media-0/play.mp3", Util.TYPE_OTHER),
    new Sample("Google Play (Ogg/Vorbis Audio)",
        "https://storage.googleapis.com/exoplayer-test-media-1/ogg/play.ogg", Util.TYPE_OTHER),
    new Sample("Google Glass (WebM Video with Vorbis Audio)",
        "http://demos.webmproject.org/exoplayer/glass_vp9_vorbis.webm", Util.TYPE_OTHER),
    new Sample("Big Buck Bunny (FLV Video)",
        "http://vod.leasewebcdn.com/bbb.flv?ri=1024&rs=150&start=0", Util.TYPE_OTHER),
  };

  private Samples() {}

}
