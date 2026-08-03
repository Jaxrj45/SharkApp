package com.example.tibuapp.ui.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


class CodigoBarrasAnalizador(
    private val onCodigoDetectado: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private var ultimoCodigo = ""
    private var ultimoTiempo = 0L

    @androidx.camera.core.ExperimentalGetImage
    override  fun analyze(imageProxy: ImageProxy){
        val mediaImage = imageProxy.image?: run{ //cerar si lo que obtengo no es una imagen osea null
            imageProxy.close()
            return
        }

        val imagen = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees //para que pueda ser reconocido si la imagen fue tomada verticalmente
        )

        scanner.process(imagen)
            .addOnSuccessListener { codigos ->
                codigos.firstOrNull() {it.valueType == Barcode.TYPE_PRODUCT ||  it.rawValue !=null }
                    ?.rawValue
                    ?.let{
                        codigo ->
                        val ahora = System.currentTimeMillis()
                        if(codigo != ultimoCodigo || ahora - ultimoTiempo > 3000){
                            ultimoCodigo=codigo
                            ultimoTiempo=ahora
                            onCodigoDetectado(codigo)
                        }
                    }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}