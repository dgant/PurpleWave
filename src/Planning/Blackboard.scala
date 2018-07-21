package Planning

import Lifecycle.With

class Blackboard {
  
  var maxFramesToSendAdvanceBuilder: Int = With.configuration.maxFramesToSendAdvanceBuilder
  
  protected var resets: Vector[() => Unit] = Vector.empty
  protected def add[T](property: Property[T]): Property[T] = {
    resets = resets :+ (() => property.reset())
    property
  }
  
  def reset() {
    resets.foreach(reset => reset())
  }
  
  var aggressionRatio     : Property[Double]  = add(new Property(1.0))
  var safetyRatio         : Property[Double]  = add(new Property(1.2))
  var gasLimitFloor       : Property[Int]     = add(new Property(450))
  var gasLimitCeiling     : Property[Int]     = add(new Property(100000))
  var gasTargetRatio      : Property[Double]  = add(new Property(if (With.self.isProtoss) 3.0 / 10.0 else 3.0 / 8.0))
  var allIn               : Property[Boolean] = add(new Property(false))
  var yoloEnabled         : Property[Boolean] = add(new Property(true))
  var allowIslandBases    : Property[Boolean] = add(new Property(false))
  var keepingHighTemplar  : Property[Boolean] = add(new Property(false))
  
  var lastScoutDeath: Int = -24 * 60
  var enemyUnitDied: Boolean = false
}
