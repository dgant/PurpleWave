package Planning.Plans.Army

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Squads.Goals.SquadDrop
import Micro.Squads.Squad
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.{UnitCountOne, UnitCountTransportable}
import Planning.Composition.UnitMatchers.{UnitMatchDroppable, UnitMatchTransport}
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan

class DropAttack extends Plan {
  
  val transportLock = new Property(new LockUnits)
  transportLock.get.unitMatcher.set(UnitMatchTransport)
  transportLock.get.unitCounter.set(UnitCountOne)
  
  val fighterLock = new Property(new LockUnits)
  fighterLock.get.unitMatcher.set(UnitMatchDroppable)
  
  val squad = new Squad(this)
  
  override def onUpdate() {
    
    val target = attackTarget
    
    transportLock.get.unitPreference.set(UnitPreferClose(target))
    transportLock.get.acquire(this)
    
    val potentialTranports = Some(transportLock.get.units) //transportLock.get.inquire(this
    
    if (potentialTranports.isEmpty || potentialTranports.get.isEmpty) {
      return
    }
    
    fighterLock.get.unitPreference.set(UnitPreferClose(target))
    fighterLock.get.unitCounter.set(new UnitCountTransportable(potentialTranports.get))
    val potentialFighters = fighterLock.get.inquire(this)
    
    if (potentialFighters.isEmpty || potentialFighters.get.isEmpty) {
      return
    }
    
    //transportLock.get.acquire(this)
    fighterLock.get.acquire(this)
    
    squad.conscript(transportLock.get.units)
    fighterLock.get.units.foreach(squad.recruit)
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
