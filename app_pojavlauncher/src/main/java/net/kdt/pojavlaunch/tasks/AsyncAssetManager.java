package net.kdt.pojavlaunch.tasks;


import static net.kdt.pojavlaunch.Architecture.archAsString;
import static net.kdt.pojavlaunch.JRE21Util.checkInternalJre21;
import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AsyncAssetManager {

    private AsyncAssetManager(){}

    /**
     * Attempt to install the java 8 runtime, if necessary
     * @param am App context
     */
    public static void unpackRuntime(AssetManager am) {
        /* Check if JRE is included */
        String rt_version = null;
        String current_rt_version = MultiRTUtils.__internal__readBinpackVersion("Internal-8");
        try {
            rt_version = Tools.read(am.open("components/jre-8/version"));
        } catch (IOException e) {
            Log.e("JREAuto", "JRE was not included on this APK.", e);
        }
        String exactJREName = MultiRTUtils.getExactJreName(8);
        if(current_rt_version == null && exactJREName != null && !exactJREName.equals("Internal-8")/*this clause is for when the internal runtime is goofed*/) return;
        if(rt_version == null) return;
        if(rt_version.equals(current_rt_version)) return;

        // Install the runtime in an async manner, hope for the best
        String finalRt_version = rt_version;
        sExecutorService.execute(() -> {

            try {
                MultiRTUtils.installRuntimeNamedBinpack(
                        am.open("components/jre-8/universal.tar.xz"),
                        am.open("components/jre-8/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                        "Internal-8", finalRt_version);
                MultiRTUtils.postPrepare("Internal-8");
            }catch (IOException e) {
                Log.e("JREAuto", "Internal-8 JRE unpack failed", e);
            }
        });
    }

    public static void unpackRuntime11(AssetManager am) {
        String rt_version = null;
        String current_rt_version = MultiRTUtils.__internal__readBinpackVersion("Internal-11");
        try {
            rt_version = Tools.read(am.open("components/jre-11/version"));
        } catch (IOException e) {
            Log.e("JREAuto", "JRE was not included on this APK.", e);
        }
        String exactJREName = MultiRTUtils.getExactJreName(11);
        if(current_rt_version == null && exactJREName != null && !exactJREName.equals("Internal-11")) return;
        if(rt_version == null) return;
        if(rt_version.equals(current_rt_version)) return;

        String finalRt_version = rt_version;
        sExecutorService.execute(() -> {

            try {
                MultiRTUtils.installRuntimeNamedBinpack(
                        am.open("components/jre-11/universal.tar.xz"),
                        am.open("components/jre-11/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                        "Internal-11", finalRt_version);
                MultiRTUtils.postPrepare("Internal-11");
            }catch (IOException e) {
                Log.e("JREAuto", "Internal-11 JRE unpack failed", e);
            }
        });
    }

    public static void unpackRuntime17(AssetManager am) {
        String rt_version = null;
        String current_rt_version = MultiRTUtils.__internal__readBinpackVersion("Internal-17");
        try {
            rt_version = Tools.read(am.open("components/jre-17/version"));
        } catch (IOException e) {
            Log.e("JREAuto", "JRE was not included on this APK.", e);
        }
        String exactJREName = MultiRTUtils.getExactJreName(17);
        if(current_rt_version == null && exactJREName != null && !exactJREName.equals("Internal-17")) return;
        if(rt_version == null) return;
        if(rt_version.equals(current_rt_version)) return;

        String finalRt_version = rt_version;
        sExecutorService.execute(() -> {

            try {
                MultiRTUtils.installRuntimeNamedBinpack(
                        am.open("components/jre-17/universal.tar.xz"),
                        am.open("components/jre-17/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                        "Internal-17", finalRt_version);
                MultiRTUtils.postPrepare("Internal-17");
            }catch (IOException e) {
                Log.e("JREAuto", "Internal-17 JRE unpack failed", e);
            }

            checkInternalJre21(am);
        });
    }

    /** Unpack single files, with no regard to version tracking */
    public static void unpackSingleFiles(Context ctx){
        ProgressLayout.setProgress(ProgressLayout.EXTRACT_SINGLE_FILES, 0);
        sExecutorService.execute(() -> {
            try {
                Tools.copyAssetFile(ctx, "options.txt", Tools.DIR_GAME_NEW, false);
                Tools.copyAssetFile(ctx, "default.json", Tools.CTRLMAP_PATH, false);

                Tools.copyAssetFile(ctx, "launcher_profiles.json", Tools.DIR_GAME_NEW, false);
                Tools.copyAssetFile(ctx,"resolv.conf",Tools.DIR_DATA, false);

                File path = new File(Tools.DIR_GAME_HOME + "/login/version");
                Tools.copyAssetFile(ctx,"login/version",path.getParent(),false);
                InputStream in = ctx.getAssets().open("login/version");
                byte[] b = new byte[in.available()];
                in.read(b);
                int newVersion = Integer.parseInt(new String(b));
                in.close();
                path.getParentFile().mkdirs();
                int oldVersion = Integer.parseInt(Tools.read(Tools.DIR_GAME_HOME + "/login/version"));
                boolean overwrite=newVersion>oldVersion;
                Tools.copyAssetFile(ctx,"login/version",path.getParent(),overwrite);
                Tools.copyAssetFile(ctx,"login/nide8auth.jar",path.getParent(),overwrite);
                Tools.copyAssetFile(ctx,"login/authlib-injector.jar",path.getParent(),overwrite);
            } catch (IOException e) {
                Log.e("AsyncAssetManager", "Failed to unpack critical components !");
            }
            ProgressLayout.clearProgress(ProgressLayout.EXTRACT_SINGLE_FILES);
        });
    }

    public static void unpackComponents(Context ctx){
        ProgressLayout.setProgress(ProgressLayout.EXTRACT_COMPONENTS, 0);
        sExecutorService.execute(() -> {
            try {
                unpackComponent(ctx, "caciocavallo", false);
                unpackComponent(ctx, "caciocavallo11", false);
                unpackComponent(ctx, "caciocavallo17", false);
                // Since the Java module system doesn't allow multiple JARs to declare the same module,
                // we repack them to a single file here
                unpackComponent(ctx, "lwjgl3", false);
                unpackComponent(ctx, "security", true);
                unpackComponent(ctx, "arc_dns_injector", true);
                unpackComponent(ctx, "forge_installer", true);
            } catch (IOException e) {
                Log.e("AsyncAssetManager", "Failed o unpack components !",e );
            }
            ProgressLayout.clearProgress(ProgressLayout.EXTRACT_COMPONENTS);
        });
    }

    private static void unpackComponent(Context ctx, String component, boolean privateDirectory) throws IOException {
        AssetManager am = ctx.getAssets();
        String rootDir = privateDirectory ? Tools.DIR_DATA : Tools.DIR_GAME_HOME;

        File versionFile = new File(rootDir + "/" + component + "/version");
        InputStream is = am.open("components/" + component + "/version");
        if(!versionFile.exists()) {
            if (versionFile.getParentFile().exists() && versionFile.getParentFile().isDirectory()) {
                FileUtils.deleteDirectory(versionFile.getParentFile());
            }
            versionFile.getParentFile().mkdir();

            Log.i("UnpackPrep", component + ": Pack was installed manually, or does not exist, unpacking new...");
            String[] fileList = am.list("components/" + component);
            for(String s : fileList) {
                Tools.copyAssetFile(ctx, "components/" + component + "/" + s, rootDir + "/" + component, true);
            }
        } else {
            FileInputStream fis = new FileInputStream(versionFile);
            String release1 = Tools.read(is);
            String release2 = Tools.read(fis);
            if (!release1.equals(release2)) {
                if (versionFile.getParentFile().exists() && versionFile.getParentFile().isDirectory()) {
                    FileUtils.deleteDirectory(versionFile.getParentFile());
                }
                versionFile.getParentFile().mkdir();

                String[] fileList = am.list("components/" + component);
                for (String fileName : fileList) {
                    Tools.copyAssetFile(ctx, "components/" + component + "/" + fileName, rootDir + "/" + component, true);
                }
            } else {
                Log.i("UnpackPrep", component + ": Pack is up-to-date with the launcher, continuing...");
            }
        }
    }
}
