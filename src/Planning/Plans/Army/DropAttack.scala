package Planning.Plans.Army

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Squads.Goals.GoalDrop
import Micro.Squads.Squad
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{UnitCountOne, UnitCountTransportable}
import Planning.UnitMatchers.{UnitMatchDroppable, UnitMatchTransport, UnitMatcher}
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}

class DropAttack extends Plan {
  
  val transportLock = new Property(new LockUnits)
  transportLock.get.unitMatcher.set(UnitMatchTransport)
  transportLock.get.unitCounter.set(UnitCountOne)
  transportLock.get.interruptable.set(false)
  
  val paratrooperMatcher = new Property[UnitMatcher](UnitMatchDroppable)
  
  val paratrooperLock = new Property(new LockUnits)
  paratrooperLock.get.unitMatcher.inherit(paratrooperMatcher)
  paratrooperLock.get.interruptable.set(false)
  
  val squad = new Squad(this)
  
  override def onUpdate() {
    
    val target = attackTarget
    
    transportLock.get.unitPreference.set(UnitPreferClose(target))
    transportLock.get.acquire(this)
    val transports = transportLock.get.units
    
    if (transports.isEmpty) return
    
    paratrooperLock.get.unitPreference.set(UnitPreferClose(target))
    paratrooperLock.get.unitCounter.set(new UnitCountTransportable(transports))
    paratrooperLock.get.acquire(this)
    val paratroopers = paratrooperLock.get.units
    
    if (paratroopers.isEmpty) return
    
    transports.foreach(squad.recruit)
    paratroopers.foreach(squad.recruit)
    squad.setGoal(new GoalDrop(target))
  }
  
  protected def attackTarget: Pixel = {
    if (With.geography.enemyBases.isEmpty) {
      With.intelligence.mostBaselikeEnemyTile.pixelCenter
    }
    else {
      With.geography.enemyBases.maxBy(evaluateBase).heart.pixelCenter
    }
  }
  
  protected def evaluateBase(base: Base): Double = {
    val value           = base.workers.size.toDouble
    val distanceNormal  = 32.0 * 50.0
    val distanceGround  = distanceNormal + Math.min(With.geography.home.pixelCenter.groundPixels(base.heart.pixelCenter), 32.0 * 256.0 * 1.41)
    val distanceAir     = distanceNormal + With.geography.home.pixelCenter.pixelDistance(base.heart.pixelCenter)
    val safety          = (1.0 +  base.defenders.size)
    val output          = value * distanceGround / safety / distanceAir
    output
  }
}
