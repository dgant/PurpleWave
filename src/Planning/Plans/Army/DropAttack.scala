package Planning.Plans.Army

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Squads.Goals.SquadDrop
import Micro.Squads.Squad
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.{UnitCountOne, UnitCountTransportable}
import Planning.Composition.UnitMatchers.{UnitMatchDroppable, UnitMatchTransport, UnitMatcher}
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan

class DropAttack extends Plan {
  
  val transportLock = new Property(new LockUnits)
  transportLock.get.unitMatcher.set(UnitMatchTransport)
  transportLock.get.unitCounter.set(UnitCountOne)
  
  val paratrooperMatcher = new Property[UnitMatcher](UnitMatchDroppable)
  
  val paratrooperLock = new Property(new LockUnits)
  paratrooperLock.get.unitMatcher.inherit(paratrooperMatcher)
  
  val squad = new Squad(this)
  
  override def onUpdate() {
    
    val target = attackTarget
    
    transportLock.get.unitPreference.set(UnitPreferClose(target))
    transportLock.get.acquire(this)
    val transports = transportLock.get.units
    
    if (transports.isEmpty) return
    
    paratrooperLock.get.unitPreference.set(UnitPreferClose(target))
    paratrooperLock.get.unitCounter.set(new UnitCountTransportable(transports))
    val paratroopers = paratrooperLock.get.inquire(this)
    
    if (potentialFighters.isEmpty || potentialFighters.get.isEmpty) {
      return
    }
    
    //transportLock.get.acquire(this)
    paratrooperLock.get.acquire(this)
    
    squad.conscript(transportLock.get.units)
    paratrooperLock.get.units.foreach(squad.recruit)
    squad.goal = new SquadDrop(target)
    
  }
  
  protected def attackTarget: Pixel = {
    if (With.geography.enemyBases.isEmpty) {
      With.intelligence.mostBaselikeEnemyTile.pixelCenter
    }
    else {
      With.geography.enemyBases.maxBy(_.townHallTile.groundPixels(With.geography.home)).heart.pixelCenter
    }
  }
}
