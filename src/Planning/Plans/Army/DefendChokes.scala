package Planning.Plans.Army

import Information.Geography.Types.Edge
import Lifecycle.With
import Mathematics.Formations.Formation
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Plan
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class DefendChokes(val maxChokes: Int = 3) extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWarriors)
  defenders.get.unitCounter.set(UnitCountEverything)
  
  override def onUpdate() {
  
    // Step 1. Organize our chokes & defenders
    
    val chokes = With.geography.ourBorder
      .toVector
      .sortBy(choke =>
        if (With.geography.enemyBases.nonEmpty)
          With.geography.enemyBases.map(_.heart.pixelCenter.pixelDistanceFast(choke.centerPixel)).min
        else
          With.intelligence.mostBaselikeEnemyTile.pixelCenter.pixelDistanceFast(choke.centerPixel))
        .take(maxChokes)
    
    if (chokes.isEmpty) return
    
    defenders.get.acquire(this)
    val unassignedUnits = new mutable.HashSet[FriendlyUnitInfo] ++ defenders.get.units
    val unitsNearestChoke = chokes
      .map(choke => (choke, unassignedUnits.toVector.sortBy(_.framesToTravelTo(choke.centerPixel))))
      .toMap
    
    val assignments = new mutable.HashMap[Edge, ArrayBuffer[FriendlyUnitInfo]]
    chokes.foreach(choke => assignments.put(choke, new ArrayBuffer[FriendlyUnitInfo]))
    
    // Step 2. Assign our defenders to chokes
    
    while (unassignedUnits.nonEmpty) {
      chokes.foreach(choke => {
        val queue = unitsNearestChoke(choke)
        queue
          .find(unassignedUnits.contains)
          .foreach(assignee => {
          unassignedUnits.remove(assignee)
            assignments(choke).append(assignee)
        })
      })
    }
    
    // Step 3. Arrange defensive formation & issue intentions
    
    chokes.foreach(choke => {
      val chokeDefenders = assignments(choke)
      val formation =
        Formation.concave(
          chokeDefenders,
          choke.sidePixels.head,
          choke.sidePixels.last,
          choke.zones
            .toList
            .sortBy(_.centroid.tileDistanceFast(With.geography.home))
            .sortBy( ! _.owner.isUs)
            .head
            .centroid
            .pixelCenter)
      
      chokeDefenders.foreach(
        defender => {
          val spot = formation(defender)
          defender.intend(new Intention(this) {
            toTravel = Some(spot)
          })
        })
    })
  }
}
