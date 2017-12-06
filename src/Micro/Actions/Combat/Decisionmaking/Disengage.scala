package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.{Avoid, KiteSafely}
import Micro.Actions.Combat.Tactics.{Bunk, Potshot}
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Disengage extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFlee              &&
    unit.canMove                    &&
    unit.matchups.threats.nonEmpty  &&
    ! Yolo.active
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    val mostEntangledThreat = unit.matchups.mostEntangledThreatDiffused.get
    
    lazy val bunkers      = Bunk.openBunkersFor(unit)
    lazy val freeToFlee   = unit.topSpeed        > mostEntangledThreat.topSpeedChasing
    lazy val freeToChase  = unit.topSpeedChasing > mostEntangledThreat.topSpeed
    lazy val outrange     = unit.canAttack(mostEntangledThreat) && unit.pixelRangeAgainstFromEdge(mostEntangledThreat) > mostEntangledThreat.pixelRangeAgainstFromEdge(unit)
  
    // Bunker? Yes, please.
    // TODO: Should probably make sure there's room! Otherwise we just spamclick it
    if (bunkers.nonEmpty) {
      val bunker = bunkers.minBy(_.pixelDistanceFast(unit))
      With.commander.rightClick(unit, bunker)
      return
    }
    
    if (freeToFlee) {
      if (outrange) {
        
        if (freeToChase) {
          // Let's chase 'em from a distance
          KiteSafely.consider(unit)
        }
        else {
          // Example: Two Marines kiting a Zealot
          // The one being targeted shoots while the other flees.
          if (unit.matchups.framesOfSafetyCurrently > unit.framesToTurnAndShootAndTurnBackAndAccelerate) {
            KiteSafely.consider(unit)
          }
          else {
            Avoid.consider(unit)
          }
        }
      }
      else {
        Avoid.consider(unit)
      }
    }
    else {
      // We can at least trade with them while running away.
      // Do we want to? There's some complicated tradeoffs to make here.
      // For now, assume yes.
      if (outrange) {
        Potshot.consider(unit)
      }
      Avoid.consider(unit)
    }
  }
}
