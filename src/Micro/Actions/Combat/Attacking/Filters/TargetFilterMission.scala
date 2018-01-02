package Micro.Actions.Combat.Attacking.Filters

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterMission extends TargetFilter {
  
  // Target units according to our goals.
  // Ignore them if they're distractions.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val cleanup      = With.intelligence.firstEnemyMain.isDefined && With.geography.enemyBases.isEmpty
    lazy val pillaging    = actor.agent.canPillage || cleanup
    lazy val destination  = actor.agent.destination.zone
    lazy val arrived      = actor.zone == destination || target.zone == destination
    lazy val engaged      = actor.squadmates.exists(squadmate => squadmate.zone == actor.zone && squadmate.matchups.threatsInRange.nonEmpty)
    lazy val inRange      = actor.inRangeToAttackFast(target)
    
    val output = pillaging || arrived || inRange || engaged
    
    output
  }
  
}
