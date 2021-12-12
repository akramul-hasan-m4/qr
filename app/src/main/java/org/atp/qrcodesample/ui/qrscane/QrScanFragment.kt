package org.atp.qrcodesample.ui.qrscane

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.Barcode.QR_CODE
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.snackbar.Snackbar
import org.atp.qrcodesample.databinding.FragmentHomeBinding
import java.lang.Exception

private const val TAG = "QrScanFragment"
class QrScanFragment : Fragment() {


    private var cameraSource : CameraSource? = null
    private var _binding: FragmentHomeBinding? = null

    private lateinit var cameraSource2: CameraSource
    private lateinit var detector: BarcodeDetector

    companion object{
        val permissions = arrayOf(Manifest.permission.CAMERA)
    }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        checkPermission()
        return root
    }

    private fun setupControls(){
        detector = BarcodeDetector.Builder(requireActivity()).build()
        detector.setProcessor(processor2)
        cameraSource2 = CameraSource.Builder(requireActivity(), detector).setAutoFocusEnabled(true).build()

        BarcodeDetector.Builder(requireContext()).setBarcodeFormats(QR_CODE).build().apply {
            setProcessor(processor2)
            if (!isOperational) {
                Log.d(TAG, "Native QR detector dependencies not available!")
                return
            }
            cameraSource2 = CameraSource.Builder(requireContext(), this).setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK).build()
        }


        binding.surfaceView.holder.addCallback(surfaceCallBack)

    }

    private val surfaceCallBack = object : SurfaceHolder.Callback{
        override fun surfaceCreated(p0: SurfaceHolder) {
            try {
                if (isPlayServicesAvailable(requireActivity())){
                    if (ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    cameraSource2.start(p0)
                }else{
                    Log.d(TAG, "surfaceCreated: play service not updated")
                }

            }catch (e : Exception){
                Log.d(TAG, "surfaceCreated: ${e}")
            }
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
           cameraSource2.stop()
        }
    }

    private val processor2 = object : Detector.Processor<Barcode>{
        override fun release() {

        }

        override fun receiveDetections(detectors: Detector.Detections<Barcode>) {
            val detectedItems = detectors.detectedItems
            Log.d(TAG, "receiveDetections: 1")
            if(detectedItems.isNotEmpty()){
                val qrCode : SparseArray<Barcode> = detectedItems
                val code = qrCode.valueAt(0)
                Log.d(TAG, "receiveDetections: 2 "+ code.displayValue)
                binding.textHome.text = code.displayValue
                stopCamera()
            }else{
                Log.d(TAG, "receiveDetections: 3")
                binding.textHome.text = "N/A"
            }
        }

    }

    private fun stopCamera(){
        cameraSource2.stop()
    }






//others


    private val processor = object : Detector.Processor<Barcode> {

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            detections?.apply {
                if (detectedItems.isNotEmpty()) {
                    val qr = detectedItems.valueAt(0)
                    // Parses the WiFi format for you and gives the field values directly
                    // Similarly you can do qr.sms for SMS QR code etc.
                    qr.wifi?.let {
                        Log.d(TAG, "SSID: ${it.ssid}, Password: ${it.password}")
                    }
                }
            }
        }

        override fun release() {}
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
           // setUpCameraView()
            setupControls()
        } else {
            permReqLauncher.launch(permissions)
            // Request camers permission from user
        }
    }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                setupControls()
               // setUpCameraView()
            }
        }

    private fun setUpCameraView(){
        BarcodeDetector.Builder(requireContext()).setBarcodeFormats(QR_CODE).build().apply {
            setProcessor(processor)
            if (!isOperational) {
                Log.d(TAG, "Native QR detector dependencies not available!")
                return
            }
            cameraSource = CameraSource.Builder(requireContext(), this).setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK).build()
        }
    }



    private val callback = object : SurfaceHolder.Callback {

        override fun surfaceCreated(holder: SurfaceHolder) {
            // Ideally, you should check the condition somewhere
            // before inflating the layout which contains the SurfaceView
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
               Snackbar.make(binding.root, "Permission Denied", Snackbar.LENGTH_SHORT).show()
                return
            }
            if (isPlayServicesAvailable(requireActivity()))
                cameraSource?.start(holder)
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            cameraSource?.stop()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) { }
    }


    // Helper method to check if Google Play Services are up to-date on the phone
    fun isPlayServicesAvailable(activity: Activity): Boolean {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireActivity())
        if (code != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, code, code).show()
            return false
        }
        return true
    }


    override fun onDestroyView() {
        super.onDestroyView()
        cameraSource?.release()
        _binding = null
    }
}