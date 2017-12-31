package Micro.Actions.Combat.Decisionmaking

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
    
    // Styles of fighting,
    // on approximate continuum of (GET IN) -> (GET OUT):
    //
    // * Hug (Dragoon vs. Sieged Tank)
    // * Fill (Dragoons moving through choke)
    // * Charge (Zealot vs. Sieged Tank)
    // * Chase (Corsair vs. Mutalisk)
    // * Abuse (Dragoon vs. Zealot)
    // * Breathe (Dragoon vs. Marine)
    // * Bite (Zealot surrounded by Zerglings)
    // * Purr (Goliath being repaired)
    // * Brawl (Zergling vs. Zergling; Marines vs. Carriers)
    // * Reposition (Dragoons vs. Dragoons)
    // * Flee (Zealot behind enemy lines)
    // * Retreat (Zealot running from Zealots)
    //
    // Also consider that sometimes we can just *ignore* the threat and continue to our destination
    
    lazy val mostEntangledThreat = unit.matchups.mostEntangledThreatDiffused.get
    lazy val bunkers      = Bunk.openBunkersFor(unit)
    lazy val freeToFlee   = unit.topSpeed        >= mostEntangledThreat.topSpeedChasing
    lazy val freeToChase  = unit.topSpeedChasing >= mostEntangledThreat.topSpeed
    lazy val outrange     = unit.canAttack(mostEntangledThreat) && unit.pixelRangeAgainstFromEdge(mostEntangledThreat) > mostEntangledThreat.pixelRangeAgainstFromEdge(unit)
    lazy val canBait      = mostEntangledThreat.matchups.threatsInRange.exists(ally => ally.matchups.framesOfSafetyDiffused > unit.matchups.framesOfEntanglementDiffused)
    
    if (freeToFlee) {
      if (outrange) {
        if (freeToChase) {
          // Let's chase 'em from a distance
          KiteSafely.consider(unit)
        }
        else {
          // Example: Two Marines kiting a Zealot
          // The one being targeted shoots while the other flees.
          if (unit.matchups.framesOfSafetyCurrently > unit.unitClass.framesToTurnAndShootAndTurnBackAndAccelerate) {
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
      if (outrange && ! canBait) {
        Potshot.consider(unit)
      }
      Avoid.consider(unit)
    }
  }
}
