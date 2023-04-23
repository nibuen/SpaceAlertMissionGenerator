package com.boarbeard.generator.beimax

import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

/**
 * minimum and maximum incoming data by phases
 */
private const val minIncomingDataTotal = 1

/**
 * minimum and maximum data transfers by phases
 */
private const val minDataTransferTotal = 1

data class DataOperationsBundle(
    // The size are the three phases, TODO bind this better to a phase configuration
    val incomingData: IntArray = IntArray(3),
    val dataTransfers: IntArray = IntArray(3),
    val success: Boolean = false
) {
    fun addIncomingData(phase: Int) {
        incomingData[phase]++
    }

    fun setDataTransfers(phase: Int, amount: Int) {
        dataTransfers[phase] = amount
    }

    fun debugPrint() {
        // debugging information
        for (phase in 0..2) {
            Timber.tag("generateData").v(
                "Phase %d : Incoming Data = %d; Data Transfers = %d",
                phase + 1,
                incomingData[phase],
                dataTransfers[phase]
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataOperationsBundle

        if (!incomingData.contentEquals(other.incomingData)) return false
        if (!dataTransfers.contentEquals(other.dataTransfers)) return false
        if (success != other.success) return false

        return true
    }

    override fun hashCode(): Int {
        var result = incomingData.contentHashCode()
        result = 31 * result + dataTransfers.contentHashCode()
        result = 31 * result + success.hashCode()
        return result
    }
}

class DataOperationGenerator(
    private val missionPreferences: MissionPreferences,
    private val generator: Random,
) {
    var dataTransferPhases = intArrayOf(0,0,0)
    /**
     * Generate data operations (either data transfer or incoming data)
     *
     * @return true, if data creation could be generated
     */
    fun generateDataOperations(): DataOperationsBundle {
        val bundle = DataOperationsBundle()

        var randomIncomingData: Int =
            generator.nextInt(missionPreferences.getMaxIncomingData() - missionPreferences.getMinIncomingData() + 1) + missionPreferences.getMinIncomingData()
        Timber.tag("generateData").v("randomIncomingData: %d", randomIncomingData)

        // start with a random in one of the first two phases
        bundle.addIncomingData(generator.nextInt(2))
        randomIncomingData--

        // split evenly until we can't
        while (randomIncomingData / 3 >= 1) {
            bundle.addIncomingData(0)
            bundle.addIncomingData(1)
            bundle.addIncomingData(2)
            randomIncomingData -= 3
        }

        // finally just fit stuff randomly
        var lastPlaced = -1
        while (randomIncomingData > 0) {
            var phase: Int
            do {
                phase = generator.nextInt(3)
            } while (lastPlaced == phase)
            lastPlaced = phase
            bundle.addIncomingData(phase)
            randomIncomingData--
        }

        // set data transfers

        var randomDataTransfer: Int =
            generator.nextInt(missionPreferences.getMaxDataTransfer() - missionPreferences.getMinDataTransfer() + 1) + missionPreferences.getMinDataTransfer()
        Timber.tag("generateData").v("randomDataTransfer: %d", randomDataTransfer)

        // start with one in the first phase
        dataTransferPhases[0]++
        randomDataTransfer--

        // randomly put the rest of the data transfers
        for (i in 0 until randomDataTransfer) {
            dataTransferPhases[generator.nextInt(3)]++
        }

        // now set the data transfers
        dataTransferPhases.forEachIndexed {phase, num ->
            bundle.setDataTransfers(phase, num)
        }

        // check minimums and maximums
        if (bundle.incomingData.sum() < minIncomingDataTotal || bundle.dataTransfers.sum() < minDataTransferTotal) return bundle

        // debugging information
        bundle.debugPrint()

        return bundle.copy(success = true)
    }

}