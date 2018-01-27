@file:Suppress("unused", "RedundantVisibilityModifier")
package org.sertain.command

import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder
import org.sertain.RobotLifecycle

private typealias WpiLibSubsystem = edu.wpi.first.wpilibj.command.Subsystem

/**
 * @see edu.wpi.first.wpilibj.command.Subsystem
 */
public abstract class Subsystem : WpiLibSubsystem(), RobotLifecycle {
    open val defaultCommand: Command? = null

    override fun initSendable(builder: SendableBuilder?) {
        super.initSendable(builder)
        RobotLifecycle.addListener(this)
    }

    override fun initDefaultCommand() = setDefaultCommand(defaultCommand?.mirror)
}
