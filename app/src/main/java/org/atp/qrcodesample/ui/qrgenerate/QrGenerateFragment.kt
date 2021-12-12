package org.atp.qrcodesample.ui.qrgenerate

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.atp.qrcodesample.databinding.FragmentDashboardBinding

class QrGenerateFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.textDashboard.setOnClickListener {
            binding.ivQrView.setImageBitmap(getQrCodeBitmap())
        }

        return root
    }

    private fun getQrCodeBitmap(): Bitmap {
        val size = 512 //pixels
        val qrCodeContent = "WIFI:S:ZEROPOINT;T:WPA;P:123456;;"
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
            }
    }

    //more template https://github.com/zxing/zxing/wiki/Barcode-Contents


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}