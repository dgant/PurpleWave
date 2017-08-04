package Planning.Plans.Army.Recruitment

import Micro.Squads.Squad
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountCombat
import Planning.Composition.UnitMatchers._
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel.EnrichedPixelCollection

class Recruit extends Plan {
  
  var mustFight   : Boolean = false
  var overkill    : Double = 2.0
  var enemies     : Seq[UnitInfo] = Seq.empty
  
  val fighters = new Property[LockUnits](new LockUnits)
  fighters.get.unitMatcher.set(UnitMatchWarriors)
  
  val squad = new Squad(this)
  
  override def isComplete: Boolean = ! enemies.exists(_.alive)
  
  override def onUpdate() {
    if (isComplete) return
    
    squad.enemies = enemies
    val centroid = enemies.map(_.pixelCenter).centroid
      
    fighters.get.unitMatcher.set(UnitMatchCombat(enemies))
    fighters.get.unitCounter.set(new UnitCountCombat(enemies, mustFight, overkill))
    fighters.get.unitPreference.set(UnitPreferClose(centroid))
    fighters.get.acquire(this)
    
    squad.recruits = fighters.get.units.toSeq
    squad.commission()
  }
}
