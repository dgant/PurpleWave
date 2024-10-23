package Planning.Plans

import Gameplans.All.Modal
import Lifecycle.With
import Utilities.Property
import bwapi.Race

class SwitchEnemyRace(
  whenTerran  : Plan = NoPlan(),
  whenProtoss : Plan = NoPlan(),
  whenZerg    : Plan = NoPlan(),
  whenRandom  : Plan = NoPlan())
    extends Plan with Modal {
  
  val terran  = new Property[Plan](whenTerran)
  val protoss = new Property[Plan](whenProtoss)
  val zerg    = new Property[Plan](whenZerg)
  val random  = new Property[Plan](whenRandom)
  
  def isComplete: Boolean = false
  
  override def onUpdate(): Unit = {
    appropriatePlan.update()
  }
  
  protected def appropriatePlan: Plan = {
    val matchupPlan = With.enemy.raceCurrent match {
      case Race.Terran    => terran
      case Race.Protoss   => protoss
      case Race.Zerg      => zerg
      case _              => random
    }
    matchupPlan.get
  }
}
