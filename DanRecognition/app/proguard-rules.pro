# Specifies to exhaustively list classes and class members matched by the various -keep options
#-printseeds ./seeds.txt

# Specifies to list dead code of the input class files
#-printusage ./usage.txt

# Specifies to print the mapping from old names to new names for classes and class members that have been renamed
#-printmapping ./mapping.txt

# Specifies to write out the internal structure of the class files, after any processing
#-dump ./dump.txt

# Specifies the number of optimization passes to be performed
-optimizationpasses 5

# Specifies not to optimize the input class files
-dontoptimize

# Specifies not to generate mixed-case class names while obfuscating
-dontusemixedcaseclassnames

# Specifies to repackage all class files that are renamed, by moving them into the single given package
-repackageclasses ''

# Specifies that the access modifiers of classes and class members may be broadened during processing
-allowaccessmodification

# Specifies to write out some more information during processing
-verbose

# Specifies not to ignore non-public library classes
-dontskipnonpubliclibraryclasses

# Specifies not to ignore package visible library class members (fields and methods)
-dontskipnonpubliclibraryclassmembers

# Specifies the optimizations to be enabled and disabled, at a more fine-grained level
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-dontwarn

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.support.v4.view.PagerAdapter
-keep public class * extends m.framework.ui.widget.viewpager.ViewPagerAdapter
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment


-keep class * extends android.view.View {
  public <init>(android.content.Context);
  public <init>(android.content.Context, android.util.AttributeSet);
  public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class * extends android.preference.Preference {
  public <init>(android.content.Context);
  public <init>(android.content.Context, android.util.AttributeSet);
  public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclasseswithmembers class * {
    native <methods>;
}

-keep class * implements android.os.Parcelable {
     public static final android.os.Parcelable$Creator *;
}

#继承序列化的很多类都被写入了文件中，因此统一保持不混淆
-keep class * implements java.io.Serializable {
     *;
}

-keepattributes **
-libraryjars <java.home>/lib/rt.jar

-verbose 
-ignorewarnings 

-dontwarn org.apache.commons.httpclient.**
 
-dontwarn android.support.v4.** 
-dontwarn **CompatHoneycomb
-dontwarn **CompatHoneycombMR2
-dontwarn **CompatCreatorHoneycombMR2

-dontwarn gson-2.2.4.jar.**
-keep class com.google.** {*;}
-keep class org.json.** {*;}
-keep class org.apache.http.entity.mime.**{*;}
-keep class sun.misc.Unsafe {*;}
-keep public class com.android.vending.licensing.ILicensingService
-keepattributes Signature  
  
-keepattributes *Annotation* 

-keep public class * implements java.io.Serializable {*;}

-dontwarn cn.sharesdk.**
-dontwarn **.R$*
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}

-keepclassmembers class ** {
    public void onEvent*(**);
}

-keepclassmembers class ** {
     void *(android.view.View);
}

-keep class **.entity.** { *; }

-keep class * implements com.duomai.common.http.request.IRequestResult {*;}

-keep class com.duomai.common.http.image.IImageRequest{
  *;
}

-keep class com.duomai.haimibuyer.share.**{
  *;
}


-keep class com.duomai.haimibuyer.request.BaseImageLoadListener{
  *;
}

-keep class com.easemob.** {
	*;
}	


-dontwarn com.lidroid.xutils.**
-keepattributes *Annotation*
#-libraryjars libs/xutils.jar
-keep public class com.lidroid.xutils.** { *; }  
-keep public class * extends java.lang.annotation.Annotation { *; }
-keep public interface com.lidroid.xutils.view.annotation.ViewInject
-keep class * implements com.lidroid.xutils.view.annotation.ViewInject{
	*;
}

-keep class com.easemob.** {*;}
-keep class org.jivesoftware.** {*;}
-keep class org.apache.** {*;}
-dontwarn  com.easemob.**
-keep class com.easemob.chatui.utils.SmileUtils {*;}

-keep public interface com.duomai.**

-keep interface com.qiniu.android.storage.UpCompletionHandler { *; }

-dontwarn com.qiniu.android.**
-keep class com.qiniu.android.** {*;}

-keep class com.baidu.** {*;}
-keep interface com.baidu.**

-keep class com.alipay.** {*;}

-keep class com.ut.device.** {*;}

-keep class com.ta.utdid2.** {*;}

-keep class com.google.zxing.** {*;}

-keep class de.greenrobot.event.** {*;}

-keep class org.apache.http.entity.mime.** {*;}

-keep class com.tencent.** {*;}

-keep class org.litepal.** {*;}

-keep class m.framework.** {*;}

-keep class com.sina.** {*;}

-keep class cn.sharesdk.** {*;}
-keep public interface cn.sharesdk.**

-keep class com.unionpay.** {*;}

-keep class com.UCMobile.PayPlugin.** {*;}

-keep class com.lidroid.xutils.** {*;}

-keep class m.framework.ui.widget.viewpager.** {
  *;
}

-keep class android.support.v7.** {
  *;
}

-dontwarn com.igexin.**
-keep class com.igexin.**{*;}

-dontwarn com.android.volley.**
-keep class com.android.volley.**{*;}
-keep public interface com.android.volley.**

-dontwarn com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.**{*;}
-keep public interface com.nostra13.universalimageloader.**

-dontwarn jp.co.cyberagent.android.**
-keep class jp.co.cyberagent.android.**{*;}
-keep public interface jp.co.cyberagent.android.**
-keep class cn.jiajixin.nuwa.** { *; }
-keep class android.support.multidex.** { *; }

-keep class * extends org.litepal.crud.DataSupport {
    *;
}