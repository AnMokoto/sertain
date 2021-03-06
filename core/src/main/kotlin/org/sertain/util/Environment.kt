@file:Suppress("unused", "RedundantVisibilityModifier")
@file:JvmName("Environment")
package org.sertain.util

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser as NativeSendableChooser

/**
 * Gets the current alliance station from the FMS.
 * @see AllianceStation
 */
public val station: AllianceStation?
    get() = AllianceStation(when (DriverStation.getInstance().alliance) {
        DriverStation.Alliance.Blue -> Alliance.BLUE
        DriverStation.Alliance.Red -> Alliance.RED
        else -> Alliance.INVALID
    }, DriverStation.getInstance().location)

/** The team's alliance color. */
public enum class Alliance {
    RED,
    BLUE,
    INVALID
}

/**
 * The team's exact alliance station position which consists of the alliance color and station
 * number.
 *
 * @property alliance the [Alliance] of this station.
 * @property station the station number for this color, between 1 and 3.
 */
public data class AllianceStation(public val alliance: Alliance, public val station: Int)

@Suppress("FunctionName")
public fun <T> SendableChooser(
        default: Pair<String, T>,
        vararg others: Pair<String, T>
) = NativeSendableChooser<T>().apply {
    addDefault(default.first, default.second)
    for ((name, value) in others) addObject(name, value)
}
