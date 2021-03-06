@file:Suppress("unused", "RedundantVisibilityModifier")
@file:JvmName("Motors")
package org.sertain.hardware

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX
import edu.wpi.first.wpilibj.PWMSpeedController
import org.sertain.RobotLifecycle
import org.sertain.add
import org.sertain.hardware.BrakeWhenStarted.minusAssign
import org.sertain.hardware.BrakeWhenStarted.plusAssign
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

public typealias Talon = WPI_TalonSRX

private val globalFollowers = ConcurrentHashMap<Talon, MutableList<Talon>>()

public val Talon.followers get() = globalFollowers[this] ?: emptyList<Talon>()

/**
 * Joins two [Talons][Talon] together by having the second follow the first.
 *
 * @param other the Talon which should follow this one
 * @return the original Talon
 */
public operator fun Talon.plus(other: Talon) = apply {
    other.follow(this)
    globalFollowers[this] = globalFollowers[this]?.apply { add(other) } ?: mutableListOf(other)
}

/**
 * Disjoins two [Talons][Talon] by having the second unfollow the first.
 *
 * @param other the Talon which should unfollow this one
 * @return the original Talon
 */
public operator fun Talon.minus(other: Talon) = apply {
    other.set(ControlMode.PercentOutput, 0.0)
    globalFollowers[this]?.remove(other)
}

/** Sets the currently selected sensor. */
@JvmOverloads
public fun Talon.setSelectedSensor(sensor: FeedbackDevice, timeoutMillis: Int = 1000) {
    configSelectedFeedbackSensor(sensor, sensor.value, timeoutMillis)
}

/**
 * Gets the position of the specified [sensor].
 *
 * @param sensor the [FeedbackDevice] to get the position of
 * @return the position of the [sensor]
 */
@JvmOverloads
public fun Talon.getEncoderPosition(sensor: FeedbackDevice = FeedbackDevice.QuadEncoder): Int =
        getSelectedSensorPosition(sensor.value)

/**
 * Sets the position of the specified [sensor].
 *
 * @param sensor the [FeedbackDevice] to get the position of
 * @param position the position to set the [sensor] to
 */
@JvmOverloads
public fun Talon.setEncoderPosition(
        position: Int,
        sensor: FeedbackDevice = FeedbackDevice.QuadEncoder,
        timeoutMillis: Int = 0
) {
    setSelectedSensorPosition(position, sensor.value, timeoutMillis)
}

/**
 * Gets the velocity of the specified [sensor].
 *
 * @param sensor the [FeedbackDevice] to get the velocity of
 * @return the velocity of the [sensor]
 */
@JvmOverloads
public fun Talon.getEncoderVelocity(sensor: FeedbackDevice = FeedbackDevice.QuadEncoder): Int =
        getSelectedSensorVelocity(sensor.value)

/**
 * Sets the [Talon] output to [percent].
 *
 * @param percent the percent output of the motor
 */
public fun Talon.setPercent(percent: Double) = this.set(ControlMode.PercentOutput, percent)

/**
 * Sets the target [Talon] position to [position].
 *
 * @param position the target motor position
 * @param feedForward the feedForward value, if any, to pass to the control loop
 */
@JvmOverloads
public fun Talon.setPosition(position: Double, feedForward: Double? = null) {
    if (feedForward == null) {
        this.set(ControlMode.Position, position)
    } else {
        this.set(ControlMode.Position, position, DemandType.ArbitraryFeedForward, feedForward)
    }
}

/**
 * Sets the target [Talon] velocity to [velocity].
 *
 * @param velocity the target motor velocity
 * @param feedForward the feedForward value, if any, to pass to the control loop
 */
@JvmOverloads
public fun Talon.setVelocity(velocity: Double, feedForward: Double? = null) {
    if (feedForward == null) {
        this.set(ControlMode.Velocity, velocity)
    } else {
        this.set(ControlMode.Velocity, velocity, DemandType.ArbitraryFeedForward, feedForward)
    }
}

/**
 * Sets the target [Talon] current to [current].
 *
 * @param velocity the target motor current
 * @param feedForward the feedForward value, if any, to pass to the control loop
 */
@JvmOverloads
public fun Talon.setCurrent(current: Double, feedForward: Double? = null) {
    if (feedForward == null) {
        this.set(ControlMode.Current, current)
    } else {
        this.set(ControlMode.Current, current, DemandType.ArbitraryFeedForward, feedForward)
    }
}

/**
 * Sets the target [Talon] position to [position] using MotionMagic.
 *
 * @param position the target motor position
 * @param feedForward the feedForward value, if any, to pass to the control loop
 */
@JvmOverloads
public fun Talon.setMotionMagic(position: Double, feedForward: Double? = null) {
    if (feedForward == null) {
        this.set(ControlMode.MotionMagic, position)
    } else {
        this.set(ControlMode.MotionMagic, position, DemandType.ArbitraryFeedForward, feedForward)
    }
}

/**
 * Sets the [Talon]'s current mode between either Brake or Coast.
 *
 * @param enable whether brake mode should be enabled
 * @return the original Talon
 */
@JvmOverloads
public fun Talon.setBrake(enable: Boolean = true) = apply {
    setNeutralMode(if (enable) NeutralMode.Brake else NeutralMode.Coast)
}

/**
 * Sets the Talon to use Auto Brake mode.
 *
 * @return the original Talon
 * @see BrakeWhenStarted
 */
public fun Talon.autoBrake() = apply { BrakeWhenStarted += this }

/**
 * Sets the Talon to brake only when you explicitly use [setBrake].
 *
 * @return the original Talon
 * @see BrakeWhenStarted
 */
public fun Talon.manualBrake() = apply { BrakeWhenStarted -= this }

/**
 * Sets the [Talon]'s current kP value.
 *
 * @param value the kP value
 * @return the original Talon
 */
@JvmOverloads
public fun Talon.setP(value: Double, timeoutMillis: Int = 1000) = apply {
    config_kP(0, value, timeoutMillis)
}

/**
 * Sets the [Talon]'s current kI value.
 *
 * @param value the kI value
 * @return the original Talon
 */
@JvmOverloads
public fun Talon.setI(value: Double, timeoutMillis: Int = 1000) = apply {
    config_kI(0, value, timeoutMillis)
}

/**
 * Sets the [Talon]'s current kD value.
 *
 * @param value the kD value
 * @return the original Talon
 */
@JvmOverloads
public fun Talon.setD(value: Double, timeoutMillis: Int = 1000) = apply {
    config_kD(0, value, timeoutMillis)
}

/**
 * Sets the [Talon]'s current kF value.
 *
 * @param value the kF value
 * @return the original Talon
 */
@JvmOverloads
public fun Talon.setF(value: Double, timeoutMillis: Int = 1000) = apply {
    config_kF(0, value, timeoutMillis)
}

/**
 * Sets the [Talon]'s current kP, kI, kD and kF values.
 *
 * @param kP the kP value
 * @param kI the kI value
 * @param kD the kD value
 * @param kF the kF value
 * @return the original Talon
 */
@JvmOverloads
public fun Talon.setPIDF(
        kP: Double = 0.0,
        kI: Double = 0.0,
        kD: Double = 0.0,
        kF: Double = 0.0,
        timeoutMillis: Int = 1000
) = apply {
    setP(kP, timeoutMillis)
    setI(kI, timeoutMillis)
    setD(kD, timeoutMillis)
    setF(kF, timeoutMillis)
}

/**
 * Sets whether the Talon should be inverted.
 *
 * @param inverted whether the Talon should be inverted
 * @return the original Talon
 */
@JvmOverloads
public fun Talon.invert(inverted: Boolean = true) = apply { this.inverted = inverted }

/**
 * Stops the Talon until [WPI_TalonSRX.set] is called again.
 *
 * @return the original Talon
 */
public fun Talon.stop() = apply { stopMotor() }

/**
 * Sets whether the controller should be inverted or not.
 *
 * @param inverted whether the controller should be inverted
 * @return the original controller
 */
@JvmOverloads
public fun PWMSpeedController.invert(inverted: Boolean = true) = apply { setInverted(inverted) }

/**
 * Puts all specified talons in brake mode when the robot is enabled in either teleop or autonomous
 * mode, and will disable brake mode 5 seconds after the robot is disabled in order to allow the
 * robot to be pushed around on the field.
 *
 * @see autoBrake
 */
private object BrakeWhenStarted : RobotLifecycle {
    private val talons = mutableSetOf<Talon>()
    private var updateTask: TimerTask? = null

    init {
        add(this)
    }

    operator fun BrakeWhenStarted.plusAssign(talon: Talon) {
        synchronized(talons) { talons += talon }
    }

    operator fun BrakeWhenStarted.minusAssign(talon: Talon) {
        synchronized(talons) { talons -= talon }
    }

    override fun onStart() {
        updateTask?.cancel()
        set(true)
    }

    override fun onStop() {
        updateTask = Timer().schedule(TimeUnit.SECONDS.toMillis(5)) { set(false) }
    }

    private fun set(brake: Boolean) {
        synchronized(talons) { for (talon in talons) talon.setBrake(brake) }
    }
}
