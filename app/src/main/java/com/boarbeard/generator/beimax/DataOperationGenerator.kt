package com.boarbeard.generator.beimax

import timber.log.Timber
import java.util.*

//private int constantThreatUnconfirmedExpansion = 2;
/**
 * minimum data operations (either data transfer or incoming data)
 */
private val minDataOperations = arrayOf(2..4, 2..4, 0..3)


/**
 * minimum and maximum incoming data by phases
 */
private const val minIncomingDataTotal = 1


/**
 * minimum and maximum data transfers by phases
 */
private val minDataTransfer = intArrayOf(0, 1, 1)
private val maxDataTransfer = intArrayOf(1, 2, 1)
private const val minDataTransferTotal = 3

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

    fun withinRanges(): Boolean {
        for (phase in 0..2) {
            // check ranges
            val totalDataOperationsForPhase = incomingData[phase] + dataTransfers[phase]
            if (!minDataOperations[phase].contains(totalDataOperationsForPhase)) {
                return false
            }
        }

        return true
    }

    fun debugPrint() {
        // debugging information
        for (phase in 0..2) {
            Timber.tag("generateData").v(
                "Phase ${(phase + 1)} : Incoming Data = ${incomingData[phase]}; Data Transfers = ${dataTransfers[phase]}"
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

        // generate stuff by phase
        for (phase in 0..2) {
            bundle.setDataTransfers(
                phase,
                generator.nextInt(maxDataTransfer[phase] - minDataTransfer[phase] + 1) + minDataTransfer[phase]
            )

            if (!bundle.withinRanges()) return bundle
        }

        // check minimums
        if (bundle.incomingData.sum() < minIncomingDataTotal || bundle.dataTransfers.sum() < minDataTransferTotal) return bundle

        // debugging information
        bundle.debugPrint()

        return bundle.copy(success = true)
    }

}