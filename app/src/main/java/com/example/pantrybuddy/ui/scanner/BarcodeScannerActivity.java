package com.example.pantrybuddy.ui.scanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.remote.openfoodfacts.OpenFoodFactsService;
import com.example.pantrybuddy.data.remote.openfoodfacts.ScannedProduct;
import com.example.pantrybuddy.databinding.ActivityBarcodeScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Full-screen camera activity scanning EAN/UPC barcodes and QR codes using CameraX and ML Kit
public class BarcodeScannerActivity extends AppCompatActivity {

    public static final String EXTRA_SCANNED_PRODUCT = "extra_scanned_product";
    public static final String EXTRA_SCANNED_BARCODE = "extra_scanned_barcode";

    private ActivityBarcodeScannerBinding binding;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private Camera camera;
    private boolean isTorchOn = false;
    private boolean isProcessingBarcode = false;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showCameraUI();
                    startCamera();
                } else {
                    showPermissionDeniedUI();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBarcodeScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39
                )
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        setupControls();
        checkAndRequestCameraPermission();
    }

    private void setupControls() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnTorchToggle.setOnClickListener(v -> toggleTorch());

        binding.btnGrantPermission.setOnClickListener(v -> checkAndRequestCameraPermission());
    }

    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            showCameraUI();
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showCameraUI() {
        binding.layoutPermissionDenied.setVisibility(View.GONE);
        binding.previewView.setVisibility(View.VISIBLE);
        binding.reticleOverlay.setVisibility(View.VISIBLE);
        binding.layoutScannerHint.setVisibility(View.VISIBLE);
    }

    private void showPermissionDeniedUI() {
        binding.layoutPermissionDenied.setVisibility(View.VISIBLE);
        binding.previewView.setVisibility(View.GONE);
        binding.reticleOverlay.setVisibility(View.GONE);
        binding.layoutScannerHint.setVisibility(View.GONE);
        binding.layoutLoading.setVisibility(View.GONE);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Failed to initialize camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::processImageProxy);

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
            );
        } catch (Exception e) {
            Toast.makeText(this, "Camera binding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(@NonNull ImageProxy imageProxy) {
        if (isProcessingBarcode) {
            imageProxy.close();
            return;
        }

        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage inputImage = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            barcodeScanner.process(inputImage)
                    .addOnSuccessListener(this::handleBarcodeScanResults)
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private void handleBarcodeScanResults(List<Barcode> barcodes) {
        if (isProcessingBarcode || barcodes == null || barcodes.isEmpty()) {
            return;
        }

        for (Barcode barcode : barcodes) {
            String rawValue = barcode.getRawValue();
            if (rawValue != null && !rawValue.trim().isEmpty()) {
                isProcessingBarcode = true;
                vibrateFeedback();
                lookupProduct(rawValue.trim());
                break;
            }
        }
    }

    private void lookupProduct(@NonNull String barcode) {
        runOnUiThread(() -> {
            binding.layoutScannerHint.setVisibility(View.GONE);
            binding.layoutLoading.setVisibility(View.VISIBLE);
            binding.tvLoadingMessage.setText(getString(R.string.scanner_loading));
        });

        OpenFoodFactsService.getInstance().fetchProduct(barcode, new OpenFoodFactsService.ProductCallback() {
            @Override
            public void onSuccess(@NonNull ScannedProduct product) {
                Intent data = new Intent();
                data.putExtra(EXTRA_SCANNED_PRODUCT, product);
                data.putExtra(EXTRA_SCANNED_BARCODE, barcode);
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onNotFound(@NonNull String notFoundBarcode) {
                showNotFoundDialog(notFoundBarcode);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                showErrorDialog(barcode, errorMessage);
            }
        });
    }

    private void showNotFoundDialog(@NonNull String barcode) {
        new AlertDialog.Builder(this)
                .setTitle("Product Not Found")
                .setMessage("Open Food Facts did not return product details for barcode: " + barcode + ". Would you like to add it manually?")
                .setPositiveButton("Add Manually", (dialog, which) -> {
                    Intent data = new Intent();
                    data.putExtra(EXTRA_SCANNED_BARCODE, barcode);
                    setResult(RESULT_OK, data);
                    finish();
                })
                .setNegativeButton("Scan Again", (dialog, which) -> resumeScanning())
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(@NonNull String barcode, @NonNull String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Network Issue")
                .setMessage("Could not fetch product details (" + errorMessage + "). Would you like to enter the item manually or scan again?")
                .setPositiveButton("Add Manually", (dialog, which) -> {
                    Intent data = new Intent();
                    data.putExtra(EXTRA_SCANNED_BARCODE, barcode);
                    setResult(RESULT_OK, data);
                    finish();
                })
                .setNegativeButton("Scan Again", (dialog, which) -> resumeScanning())
                .setCancelable(false)
                .show();
    }

    private void resumeScanning() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutScannerHint.setVisibility(View.VISIBLE);
        isProcessingBarcode = false;
    }

    private void toggleTorch() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            isTorchOn = !isTorchOn;
            camera.getCameraControl().enableTorch(isTorchOn);
            binding.btnTorchToggle.setImageResource(isTorchOn ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        } else {
            Toast.makeText(this, "Flashlight not available on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void vibrateFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    Vibrator vibrator = vibratorManager.getDefaultVibrator();
                    vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            } else {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        v.vibrate(70);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }
}
