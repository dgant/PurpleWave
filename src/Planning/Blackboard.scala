package Planning

import Lifecycle.With
import Planning.Plans.Basic.NoPlan

class Blackboard {
  
  var maxFramesToSendAdvanceBuilder: Int = With.configuration.maxFramesToSendAdvanceBuilder
  
  protected var resets: Vector[() => Unit] = Vector.empty
  protected def add[T](property  : Property[T])  : Property[T] = {
    resets = resets :+ (() => property.reset())
    property
  }
  
  def reset() {
    resets.foreach(reset => reset())
  }

  var status                : Property[Seq[String]] = add(new Property(Seq.empty))
  var aggressionRatio       : Property[Double]      = add(new Property(1.0))
  var wantToAttack          : Property[Boolean]     = add(new Property(false))
  var defendingBase         : Property[Boolean]     = add(new Property(false))
  var safeToMoveOut         : Property[Boolean]     = add(new Property(false))
  var scoutPlan             : Property[Plan]        = add(new Property(NoPlan()))
  var gasWorkerFloor        : Property[Int]         = add(new Property(0))      // Require at least this many gas workers
  var gasWorkerCeiling      : Property[Int]         = add(new Property(200))    // Require no more than this many gas workers (unless saturated on minerals)
  var gasLimitFloor         : Property[Int]         = add(new Property(0))      // Max gas mining until at least this much gas
  var gasLimitCeiling       : Property[Int]         = add(new Property(100000)) // Stop gas mining after this much gas
  var gasWorkerRatio        : Property[Double]      = add(new Property(if (With.self.isProtoss) 3.0 / 10.0 else 3.0 / 8.0))
  var allIn                 : Property[Boolean]     = add(new Property(false))
  var yoloEnabled           : Property[Boolean]     = add(new Property(true))
  var allowIslandBases      : Property[Boolean]     = add(new Property(false))
  var keepingHighTemplar    : Property[Boolean]     = add(new Property(true))
  var pushKiters            : Property[Boolean]     = add(new Property(false))
  var stealGas              : Property[Boolean]     = add(new Property(false))
  var mcrs                  : Property[Boolean]     = add(new Property(With.configuration.enableMCRS))
  var preferCloseExpansion  : Property[Boolean]     = add(new Property(false))
  var lastScoutDeath        : Int                   = -24 * 60
  var enemyUnitDied         : Boolean               = false
}
