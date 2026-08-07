package com.vibe.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Capacitor Plugin para la eliminación segura de archivos multimedia en Android,
 * compatible con Scoped Storage (Android 10+ / API 29+) y MediaStore.
 */
@CapacitorPlugin(name = "MediaDelete")
public class MediaDeletePlugin extends Plugin {

    private ActivityResultLauncher<IntentSenderRequest> deleteLauncher;

    private int pendingBatchDeletedCount = 0;
    private int pendingBatchPendingCount = 0;
    private List<String> pendingBatchFailedFiles = new ArrayList<>();
    private List<String> pendingBatchInputs = new ArrayList<>();
    private List<String> pendingBatchPathsToScan = new ArrayList<>();

    private void clearPendingBatchState() {
        pendingBatchDeletedCount = 0;
        pendingBatchPendingCount = 0;
        pendingBatchFailedFiles.clear();
        pendingBatchInputs.clear();
        pendingBatchPathsToScan.clear();
    }

    @Override
    public void load() {
        super.load();
        // Registrar el launcher para manejar la respuesta del diálogo de confirmación del sistema
        deleteLauncher = getActivity().registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                PluginCall savedCall = getSavedCall();
                if (savedCall != null) {
                    String methodName = savedCall.getMethodName();
                    if ("deleteFiles".equals(methodName) || "deleteMultipleFiles".equals(methodName)) {
                        JSObject ret = new JSObject();
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            for (String path : pendingBatchPathsToScan) {
                                MediaScannerConnection.scanFile(getContext(), new String[]{path}, null, null);
                            }
                            int totalDeleted = pendingBatchDeletedCount + pendingBatchPendingCount;
                            ret.put("success", pendingBatchFailedFiles.isEmpty());
                            ret.put("deletedCount", totalDeleted);
                            ret.put("failedCount", pendingBatchFailedFiles.size());
                            ret.put("failedFiles", new JSArray(pendingBatchFailedFiles));
                            savedCall.resolve(ret);
                        } else {
                            int totalFailed = pendingBatchFailedFiles.size() + pendingBatchPendingCount;
                            List<String> allFailed = new ArrayList<>(pendingBatchFailedFiles);
                            allFailed.addAll(pendingBatchInputs);
                            ret.put("success", false);
                            ret.put("deletedCount", pendingBatchDeletedCount);
                            ret.put("failedCount", totalFailed);
                            ret.put("failedFiles", new JSArray(allFailed));
                            ret.put("error", "El usuario denegó el permiso para eliminar los archivos.");
                            savedCall.resolve(ret);
                        }
                        clearPendingBatchState();
                    } else {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            JSObject ret = new JSObject();
                            ret.put("success", true);
                            savedCall.resolve(ret);
                        } else {
                            savedCall.reject("El usuario denegó el permiso para eliminar el archivo.");
                        }
                    }
                    freeSavedCall();
                }
            }
        );
    }

    @PluginMethod
    public void deleteFile(PluginCall call) {
        String input = call.getString("uri");
        if (input == null || input.trim().isEmpty()) {
            input = call.getString("path");
        }
        if (input == null || input.trim().isEmpty()) {
            input = call.getString("filePath");
        }

        if (input == null || input.trim().isEmpty()) {
            call.reject("No se proporcionó ninguna URI o ruta de archivo ('uri', 'path' o 'filePath').");
            return;
        }

        try {
            Uri contentUri = null;
            String filePath = null;

            if (input.startsWith("content://")) {
                contentUri = Uri.parse(input);
            } else {
                if (input.startsWith("file://")) {
                    filePath = Uri.parse(input).getPath();
                } else {
                    filePath = input;
                }

                if (filePath != null) {
                    contentUri = getContentUriFromPath(getContext(), filePath);
                }
            }

            // 1. Si tenemos una URI de ContentResolver/MediaStore
            if (contentUri != null) {
                deleteContentUri(call, contentUri, filePath);
                return;
            }

            // 2. Si es una ruta de archivo física no indexada en MediaStore
            if (filePath != null) {
                deletePhysicalFile(call, filePath);
                return;
            }

            call.reject("No se pudo determinar el tipo de URI o ruta para: " + input);

        } catch (Exception e) {
            call.reject("Error al intentar eliminar el archivo: " + e.getLocalizedMessage());
        }
    }

    /**
     * Intenta eliminar una URI de MediaStore respetando Scoped Storage (API 29+ y API 30+).
     */
    private void deleteContentUri(PluginCall call, Uri contentUri, String filePath) {
        ContentResolver contentResolver = getContext().getContentResolver();

        // Intento directo con ContentResolver
        try {
            int rowsDeleted = contentResolver.delete(contentUri, null, null);
            if (rowsDeleted > 0) {
                if (filePath != null) {
                    MediaScannerConnection.scanFile(getContext(), new String[]{filePath}, null, null);
                }
                JSObject ret = new JSObject();
                ret.put("success", true);
                call.resolve(ret);
                return;
            }
        } catch (SecurityException securityException) {
            // Se capturará para solicitar permisos mediante IntentSender en Android 10+
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && securityException instanceof RecoverableSecurityException) {
                RecoverableSecurityException rse = (RecoverableSecurityException) securityException;
                requestUserDeletePermission(call, rse.getUserAction().getActionIntent().getIntentSender());
                return;
            }
        }

        // Android 11+ (API 30+): Usar MediaStore.createDeleteRequest para solicitar confirmación al usuario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                PendingIntent pendingIntent = MediaStore.createDeleteRequest(
                        contentResolver,
                        Collections.singletonList(contentUri)
                );
                requestUserDeletePermission(call, pendingIntent.getIntentSender());
                return;
            } catch (Exception e) {
                call.reject("Error al crear la solicitud de eliminación en Android 11+: " + e.getLocalizedMessage());
                return;
            }
        }

        // Si falló direct delete y no es API 30+ ni RecoverableSecurityException, intentar borrado físico si hay filePath
        if (filePath != null) {
            deletePhysicalFile(call, filePath);
        } else {
            call.reject("No se pudo eliminar el archivo a través de MediaStore.");
        }
    }

    /**
     * Elimina un archivo directamente desde el sistema de archivos (útil para API < 29 o archivos en almacenamiento privado/interno).
     */
    private void deletePhysicalFile(PluginCall call, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            call.reject("El archivo no existe en el disco: " + filePath);
            return;
        }

        boolean deleted = file.delete();
        if (deleted) {
            MediaScannerConnection.scanFile(getContext(), new String[]{filePath}, null, null);
            JSObject ret = new JSObject();
            ret.put("success", true);
            call.resolve(ret);
        } else {
            call.reject("No se pudo eliminar el archivo del almacenamiento de forma directa.");
        }
    }

    /**
     * Solicita permiso al usuario para eliminar el archivo multimedia lanzando un IntentSender.
     */
    private void requestUserDeletePermission(PluginCall call, IntentSender intentSender) {
        try {
            saveCall(call);
            IntentSenderRequest request = new IntentSenderRequest.Builder(intentSender).build();
            deleteLauncher.launch(request);
        } catch (Exception e) {
            freeSavedCall();
            call.reject("Error al lanzar el diálogo de confirmación de eliminación: " + e.getLocalizedMessage());
        }
    }

    /**
     * Busca la Content URI en MediaStore correspondiente a una ruta de archivo local.
     */
    private Uri getContentUriFromPath(Context context, String path) {
        // Buscar en MediaStore.Audio.Media
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{ MediaStore.Audio.Media._ID };
        String selection = MediaStore.Audio.Media.DATA + "=?";
        String[] selectionArgs = new String[]{ path };

        try (Cursor cursor = context.getContentResolver().query(audioUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                long id = cursor.getLong(idColumn);
                return ContentUris.withAppendedId(audioUri, id);
            }
        } catch (Exception ignored) {}

        // Fallback a MediaStore.Files
        Uri filesUri = MediaStore.Files.getContentUri("external");
        try (Cursor cursor = context.getContentResolver().query(filesUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                long id = cursor.getLong(idColumn);
                return ContentUris.withAppendedId(filesUri, id);
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Extrae una lista de cadenas de texto desde un JSArray (admitiendo cadenas u objetos con {uri, path, filePath}).
     */
    private List<String> extractStringList(JSArray array) {
        List<String> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            try {
                Object obj = array.get(i);
                if (obj instanceof String) {
                    String s = (String) obj;
                    if (!s.trim().isEmpty()) {
                        list.add(s);
                    }
                } else if (obj instanceof JSONObject) {
                    JSONObject jsonObj = (JSONObject) obj;
                    String val = jsonObj.optString("uri", null);
                    if (val == null) val = jsonObj.optString("path", null);
                    if (val == null) val = jsonObj.optString("filePath", null);
                    if (val != null && !val.trim().isEmpty()) {
                        list.add(val);
                    }
                }
            } catch (Exception ignored) {}
        }
        return list;
    }

    @PluginMethod
    public void deleteMultipleFiles(PluginCall call) {
        deleteFiles(call);
    }

    @PluginMethod
    public void deleteFiles(PluginCall call) {
        JSArray filesArray = call.getArray("files");
        if (filesArray == null) filesArray = call.getArray("uris");
        if (filesArray == null) filesArray = call.getArray("paths");
        if (filesArray == null) filesArray = call.getArray("filePaths");
        if (filesArray == null) filesArray = call.getArray("urls");

        List<String> inputList = extractStringList(filesArray);

        if (inputList.isEmpty()) {
            call.reject("No se proporcionó ninguna lista de URIs o rutas de archivo ('files', 'uris', 'paths' o 'filePaths').");
            return;
        }

        int deletedCount = 0;
        List<String> failedFiles = new ArrayList<>();
        List<Uri> pendingUrisForPermission = new ArrayList<>();
        List<String> pendingInputsForPermission = new ArrayList<>();
        List<String> pendingPathsToScan = new ArrayList<>();
        ContentResolver contentResolver = getContext().getContentResolver();

        for (String input : inputList) {
            try {
                Uri contentUri = null;
                String filePath = null;

                if (input.startsWith("content://")) {
                    contentUri = Uri.parse(input);
                } else {
                    if (input.startsWith("file://")) {
                        filePath = Uri.parse(input).getPath();
                    } else {
                        filePath = input;
                    }

                    if (filePath != null) {
                        contentUri = getContentUriFromPath(getContext(), filePath);
                    }
                }

                // 1. Intento directo con ContentResolver
                if (contentUri != null) {
                    try {
                        int rowsDeleted = contentResolver.delete(contentUri, null, null);
                        if (rowsDeleted > 0) {
                            if (filePath != null) {
                                MediaScannerConnection.scanFile(getContext(), new String[]{filePath}, null, null);
                            }
                            deletedCount++;
                            continue;
                        }
                    } catch (SecurityException securityException) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            pendingUrisForPermission.add(contentUri);
                            pendingInputsForPermission.add(input);
                            if (filePath != null) {
                                pendingPathsToScan.add(filePath);
                            }
                            continue;
                        }
                    }
                }

                // 2. Intento de borrado físico directo
                if (filePath != null) {
                    File file = new File(filePath);
                    if (file.exists() && file.delete()) {
                        MediaScannerConnection.scanFile(getContext(), new String[]{filePath}, null, null);
                        deletedCount++;
                        continue;
                    }
                }

                // 3. Android 11+ MediaStore pending request fallback
                if (contentUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !pendingUrisForPermission.contains(contentUri)) {
                    pendingUrisForPermission.add(contentUri);
                    pendingInputsForPermission.add(input);
                    if (filePath != null) {
                        pendingPathsToScan.add(filePath);
                    }
                    continue;
                }

                failedFiles.add(input);

            } catch (Exception e) {
                failedFiles.add(input);
            }
        }

        // Si hay archivos que requieren diálogo de confirmación del sistema en Android 11+ (API 30+)
        if (!pendingUrisForPermission.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                PendingIntent pendingIntent = MediaStore.createDeleteRequest(
                        contentResolver,
                        pendingUrisForPermission
                );
                pendingBatchDeletedCount = deletedCount;
                pendingBatchPendingCount = pendingUrisForPermission.size();
                pendingBatchFailedFiles = failedFiles;
                pendingBatchInputs = pendingInputsForPermission;
                pendingBatchPathsToScan = pendingPathsToScan;

                requestUserDeletePermission(call, pendingIntent.getIntentSender());
                return;
            } catch (Exception e) {
                failedFiles.addAll(pendingInputsForPermission);
            }
        }

        // Si no se requirió diálogo de confirmación o en Android < 11
        JSObject ret = new JSObject();
        ret.put("success", failedFiles.isEmpty());
        ret.put("deletedCount", deletedCount);
        ret.put("failedCount", failedFiles.size());
        ret.put("failedFiles", new JSArray(failedFiles));
        call.resolve(ret);
    }
}