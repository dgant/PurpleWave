package Planning.Plans.Army

import Lifecycle.With
import Mathematics.PurpleMath
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
        .maxBy(With.geography.enemyBases)(base =>
          (base.mineralsLeft + base.gasLeft)
          / (1.0 + base.defenders.map(_.subjectiveValue).sum)
          / (1.0 + PurpleMath.nanToOne(attackerCenter.groundPixels(base.heart.pixelCenter))))
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
      && e.unitClass.helpsInCombat
      && ( ! e.unitClass.isBuilding || e.zone == target.zone))
    squad.conscript(attackers.get.units)
  }
}
