package Planning.Plans.Compound

import Lifecycle.With
import Planning.Plans.Basic.NoPlan
import Planning.{Plan, Property}
import bwapi.Race

class SwitchEnemyRace(
  whenTerran  : Plan = NoPlan(),
  whenProtoss : Plan = NoPlan(),
  whenZerg    : Plan = NoPlan(),
  whenRandom  : Plan = NoPlan())
    extends Plan {
  
  val terran  = new Property[Plan](whenTerran)
  val protoss = new Property[Plan](whenProtoss)
  val zerg    = new Property[Plan](whenZerg)
  val random  = new Property[Plan](whenRandom)
  
  description.set("Given enemy race")
  
  override def getChildren: Iterable[Plan] = Vector(terran.get, protoss.get, zerg.get, random.get)
  
  override def isComplete: Boolean = appropriatePlan.isComplete
  
  override def onUpdate() {
    delegate(appropriatePlan)
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
