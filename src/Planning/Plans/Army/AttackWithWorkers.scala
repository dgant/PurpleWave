package Planning.Plans.Army

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Intent.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class AttackWithWorkers extends Plan {
  
  val fighters = new LockUnits
  fighters.unitMatcher.set(UnitMatchWorkers)
  fighters.unitCounter.set(UnitCountEverything)
  fighters.interruptable.set(false)
  
  var haveSeenABase = false
  
  override def onUpdate() {
    fighters.acquire(this)
    
    if (With.geography.enemyBases.nonEmpty) {
      haveSeenABase = true
    }
    
    if ( ! haveSeenABase) {
      findStartLocation()
    }
    else if (With.geography.enemyBases.isEmpty && ! With.units.enemy.exists(unit => unit.unitClass.isBuilding && ! unit.flying)) {
      findBases()
    }
    else {
      tickle()
    }
  }
  
  def findStartLocation() {
    // 2-Player: We know where they are. Go SMOrc.
    // 3-player: Scout one base with one probe while keeping the others in the middle.
    // 4-player: Scout two bases with one probe while keeping the others in the middle.
    
    val possibleStarts = With.geography.startBases.filter(base => base.lastScoutedFrame <= 0 && ! base.owner.isUs)
    
    if (possibleStarts.isEmpty) {
      // Defensive handling of situation that makes no sense
      findBases()
    }
    else if (possibleStarts.size == 1) {
      fighters.units.foreach(tickle(_, possibleStarts.head))
    }
    else {
      // Scout all but the furthest base and keep the rest of the workers in the middle
      val unscoutedBases    = new mutable.ArrayBuffer[Base] ++ possibleStarts
      val unassignedScouts  = new mutable.HashSet[FriendlyUnitInfo] ++ fighters.units
      while(unassignedScouts.nonEmpty && unscoutedBases.size > 1) {
        val nextBase = unscoutedBases.minBy(_.zone.distancePixels(With.geography.ourMain.zone))
        val scout = unassignedScouts.minBy(_.framesToTravel(nextBase.heart.pixelCenter))
        unassignedScouts  -= scout
        unscoutedBases    -= nextBase
        tickle(scout, nextBase)
      }
      
      val middleZone = With.geography.zones.minBy(_.centroid.tileDistanceFast(SpecificPoints.tileMiddle))
      unassignedScouts.foreach(tickle(_, middleZone.centroid.pixelCenter))
    }
  }
  
  def findBases() {
    // Distribute scouts among unscouted bases, preferring to send more to the closest bases
    val unscoutedBases =
      With.geography.bases
        .filter( ! _.owner.isUs)
        .filter(_.isStartLocation || haveSeenABase) //Only search non-start locations until we've killed the first
        .toVector
        .sortBy(_.heart.tileDistanceFast(With.geography.home))
        .sortBy( ! _.isStartLocation)
        .sortBy(_.lastScoutedFrame)
  
    val unassignedScouts = new mutable.HashSet[FriendlyUnitInfo] ++ fighters.units
    while(unassignedScouts.nonEmpty) {
      unscoutedBases.foreach(base => {
        if (unassignedScouts.nonEmpty) {
          val scout = unassignedScouts.minBy(_.pixelDistanceFast(base.heart.pixelCenter))
          unassignedScouts.remove(scout)
          tickle(scout, base)
        }
      })
    }
  }
  
  def tickle(unit: FriendlyUnitInfo, base: Base) {
    tickle(unit, base.heart.pixelCenter)
  }
  
  def tickle() {
    val base = With.geography.enemyBases.toList.sortBy(_.workers.size).lastOption
    val target = base.map(_.heart.pixelCenter).getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
    fighters.units.foreach(tickle(_, target))
  }
  
  def tickle(unit: FriendlyUnitInfo, target: Pixel) {
    unit.intend(new Intention(this) {
      toTravel = Some(target)
      canTickle = true
    })
  }
}
