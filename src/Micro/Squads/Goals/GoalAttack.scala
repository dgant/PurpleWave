package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Agency.Intention
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class GoalAttack extends GoalBasic {
  
  override def toString: String = "Attack " + target.zone.name
  
  var target: Pixel = With.intelligence.mostBaselikeEnemyTile.pixelCenter
  
  override def run() {
    chooseTarget()
    squad.units.foreach(attacker => {
      With.intelligence.highlightScout(attacker)
      attacker.agent.intend(squad.client, new Intention {
        toTravel = Some(target)
      })
    })

    def targetFilter(unit: UnitInfo): Boolean = (
      unit.isEnemy
      && unit.likelyStillAlive
      && unit.possiblyStillThere
      && unit.unitClass.dealsDamage)

    val occupiedBases = squad.units.flatMap(_.base).filter(_.owner.isEnemy)
    squad.enemies = With.units.enemy.view.filter(u => targetFilter(u) && u.zone == target.zone || u.canMove || u.unitClass.isSiegeTank).toSeq
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
          val output              = (1.0 + resourcesProjected) / distanceLog / defendersLog
          output
        })
        .map(base => ByOption.minBy(base.units.filter(u => u.isEnemy && u.unitClass.isBuilding))(_.pixelDistanceCenter(base.townHallArea.midPixel))
          .map(_.pixelCenter)
          .getOrElse(base.townHallArea.midPixel))
        .getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
  }
  
  override protected def offerUseful(candidates: Iterable[FriendlyUnitInfo]) {
    candidates.foreach(c => if (unitMatcher.accept(c)) addCandidate(c))
  }
  override protected def offerUseless(candidates: Iterable[FriendlyUnitInfo]) {}
}
