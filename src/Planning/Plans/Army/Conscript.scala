package Planning.Plans.Army

import Micro.Squads.Squad
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountCombat
import Planning.Composition.UnitMatchers._
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel.EnrichedPixelCollection

class Conscript(val squad: Squad) extends Plan {
  
  var mustFight : Boolean = false
  var overkill  : Double = 2.0
  var enemies   : Seq[UnitInfo] = Seq.empty
  
  val fighters = new Property[LockUnits](new LockUnits)
  fighters.get.unitMatcher.set(UnitMatchWarriors)
  
  override def isComplete: Boolean = ! enemies.exists(_.likelyStillAlive)
  
  override def onUpdate() {
    if (isComplete) return
    
    val centroid = enemies.map(_.pixelCenter).centroid
    
    fighters.get.release()
    fighters.get.unitMatcher.set(UnitMatchCombat(enemies))
    fighters.get.unitCounter.set(new UnitCountCombat(enemies, mustFight, overkill))
    fighters.get.unitPreference.set(UnitPreferClose(centroid))
    fighters.get.acquire(this.parent.getOrElse(this))
  
    squad.enemies = enemies
    squad.conscript(fighters.get.units)
  }
}
