package com.example.rtse.domain.util

import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import kotlin.math.cos

object FFTUtils {

    private var fft : FastFourierTransformer = FastFourierTransformer(DftNormalization.STANDARD)
    private lateinit var fftOptions: FFTOptions

    fun setFFTOptions(options: FFTOptions){
        fftOptions = options
    }

    // Array<out Complex>? means that we can read it's elements as Complex type like:
    // val element1: Complex = complexArray[0]
    fun getFFT(buffer: DoubleArray): Array<out Complex>? {
        val windowedData = fftOptions.windowFunction.getWindowFunction(buffer)
        return fft.transform(windowedData, TransformType.FORWARD)
    }

    fun getIFFT(complexCoefficients: Array<out Complex>): DoubleArray {
        val inverseTransformedData = fft.transform(complexCoefficients, TransformType.INVERSE)
        return inverseTransformedData.map { it.real }.toDoubleArray()
    }

}

data class FFTOptions(
    val frameSize : Int = 512,
    val stride : Int = frameSize/2,
    val windowFunction: WindowFunction = WindowFunction.Hanning,
)
sealed class WindowFunction(val getWindowFunction: (DoubleArray) -> DoubleArray) {

    object Hanning : WindowFunction(getWindowFunction = { inputData: DoubleArray ->
        val N = inputData.size
        val windowedData = DoubleArray(N)

        // Apply Hanning window function
        for (i in 0 until N) {
            val windowValue = 0.5 * (1 - cos(2 * Math.PI * i / (N - 1)))
            windowedData[i] = inputData[i] * windowValue
        }

        windowedData
    })

    object Hamming : WindowFunction(getWindowFunction = { inputData: DoubleArray ->
        val N = inputData.size
        val windowedData = DoubleArray(N)

        // Apply Hamming window function
        for (i in 0 until N) {
            val windowValue = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (N - 1))
            windowedData[i] = inputData[i] * windowValue
        }

        windowedData

    })
}



