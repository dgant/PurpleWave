
package Planning.Plans.Army

import Lifecycle.With
import Micro.Agency.Intention
import Micro.Squads.Goals.SquadPush
import Micro.Squads.Squad
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Plan
import Utilities.ByOption
import Utilities.EnrichPixel._

class Attack extends Plan {
  
  description.set("Attack")
  
  val squad = new Squad(this)
  
  val attackers = new Property[LockUnits](new LockUnits)
  attackers.get.unitMatcher.set(UnitMatchWarriors)
  attackers.get.unitCounter.set(UnitCountEverything)
  
  override def onUpdate() {
    
    attackers.get.acquire(this)
  
    val attackingUnits = attackers.get.units
    if (attackingUnits.isEmpty) return
      
    val attackerCenter = attackingUnits.map(_.pixelCenter).centroid
    val target =
      ByOption
        .maxBy(With.geography.enemyBases)(base => {
          val age                 = With.framesSince(base.lastScoutedFrame)
          val resources           = base.mineralsLeft + base.gasLeft
          val distance            = attackerCenter.pixelDistance(base.heart.pixelCenter)
          val defenders           = base.defenders.map(_.subjectiveValue).sum
          val resourcesProjected  = Math.max(resources / 4.0, resources - With.economy.incomePerFrameMinerals * 15 * age)
          val distanceLog         = 1 + Math.log(1 + distance)
          val defendersLog        = 1 + Math.log(1 + defenders)
          val output              = (1.0 + resourcesProjected) / distanceLog / defendersLog
          output
        })
        .map(base => ByOption.minBy(base.units.filter(u => u.isEnemy && u.unitClass.isBuilding))(_.pixelDistanceCenter(base.townHallArea.midPixel))
          .map(_.pixelCenter)
          .getOrElse(base.townHallArea.midPixel))
        .getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
    
    val attackIntent = new Intention { toTravel = Some(target) }
    attackers.get.units.foreach(attacker => {
      With.intelligence.highlightScout(attacker)
      attacker.agent.intend(this, attackIntent)
    })
  
    squad.goal = new SquadPush(target)
    squad.enemies = With.units.enemy.filter(e =>
      e.likelyStillAlive
      && e.possiblyStillThere
      && e.unitClass.dealsDamage
      && ( ! e.unitClass.isBuilding || e.zone == target.zone))
    squad.conscript(attackers.get.units)
  }
}
