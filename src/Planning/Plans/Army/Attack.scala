package Planning.Plans.Army

import Lifecycle.With
import Micro.Agency.Intention
import Micro.Squads.Goals.Push
import Micro.Squads.Squad
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Plan

class Attack extends Plan {
  
  description.set("Attack")
  
  val squad = new Squad(this)
  
  val attackers = new Property[LockUnits](new LockUnits)
  attackers.get.unitMatcher.set(UnitMatchWarriors)
  attackers.get.unitCounter.set(UnitCountEverything)
  
  override def onUpdate() {
    
    val target =
      if (With.geography.enemyBases.isEmpty)
        With.intelligence.mostBaselikeEnemyTile.pixelCenter
      else {
        val base =
          With.geography.enemyBases
            .minBy(base =>
              if (With.geography.ourBases.nonEmpty)
                With.geography.ourBases.map(_.zone.distancePixels(base.zone)).min
              else
                -base.mineralsLeft)
        
        // Actually hunt down the remaining buildings
        if (With.units.enemy.nonEmpty) {
          With.units.enemy.minBy(_.pixelDistanceFast(base.heart.pixelCenter)).pixelCenter
        }
        else {
          base.heart.pixelCenter
        }
      }
    
    attackers.get.unitPreference.set(UnitPreferClose(target))
    attackers.get.acquire(this)
    
    if (attackers.get.units.isEmpty) return
    
    val attackIntent = new Intention { toTravel = Some(target) }
    attackers.get.units.foreach(_.agent.intend(this, attackIntent))
  
    squad.goal = new Push(target)
    squad.enemies = With.units.enemy.filter(u => u.unitClass.helpsInCombat && ( ! u.unitClass.isBuilding || u.pixelCenter.zone == target.zone))
    squad.conscript(attackers.get.units)
  }
}
