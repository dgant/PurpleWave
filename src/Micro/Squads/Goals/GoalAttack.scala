package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Agency.Intention
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class GoalAttack extends GoalBasic {
  
  override def toString: String = "Attack " + target.zone.name
  
  var target: Pixel = With.intelligence.mostBaselikeEnemyTile.pixelCenter
  var berzerk: Boolean = false
  
  override def run() {
    chooseTarget()
    squad.units.foreach(attacker => {
      With.intelligence.highlightScout(attacker)
      attacker.agent.intend(squad.client, new Intention {
        toTravel = Some(target)
        canBerzerk = berzerk
      })
    })
    
    val allEnemies = With.units.enemy.filter(e => e.likelyStillAlive && e.possiblyStillThere)
    val defenders = allEnemies.filter(e => e.unitClass.dealsDamage && ( ! e.unitClass.isBuilding || e.zone == target.zone))
    squad.enemies = defenders
        
  }
  
  protected def chooseTarget(): Unit = {
    val attackerCenter = PurpleMath.centroid(squad.units.map(_.pixelCenter))
    target =
      ByOption
        .maxBy(With.geography.enemyBases)(base => {
          val age                 = With.framesSince(base.lastScoutedFrame)
          val resources           = base.mineralsLeft + base.gasLeft
          val distance            = attackerCenter.pixelDistance(base.heart.pixelCenter)
          val defenders           = base.defenders.map(_.subjectiveValue).sum
          val resourcesProjected  = Math.max(resources / 4.0, resources - With.economy.incomePerFrameMinerals * 20 * age)
          val distanceLog         = 1 + Math.log(1 + distance)
          val defendersLog        = 1 + Math.log(1 + defenders)
          val output              = (1.0 + resourcesProjected) / distanceLog // / defendersLog
          output
        })
        .map(base => ByOption.minBy(base.units.filter(u => u.isEnemy && u.unitClass.isBuilding))(_.pixelDistanceCenter(base.townHallArea.midPixel))
          .map(_.pixelCenter)
          .getOrElse(base.townHallArea.midPixel))
        .getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
  }
  
  override protected def offerUseful(candidates: Iterable[FriendlyUnitInfo]) {
    candidates.foreach(addCandidate)
  }
  override protected def offerUseless(candidates: Iterable[FriendlyUnitInfo]) {}
}
