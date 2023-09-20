package Planning.Plans.Army

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.UnitCounters.{CountEverything, UnitCounter}
import Utilities.UnitFilters.IsWorker

import scala.collection.mutable

class AttackWithWorkers(counter: UnitCounter = CountEverything) extends Plan {
  
  val fighters: LockUnits = new LockUnits(this, IsWorker).setCounter(counter).setInterruptible(false)
  
  var haveSeenABase = false
  
  override def onUpdate(): Unit = {
    fighters.acquire()
    
    haveSeenABase ||= With.geography.enemyBases.nonEmpty
    
    if ( ! haveSeenABase) {
      findStartLocation()
    } else if (With.geography.enemyBases.isEmpty && ! With.units.enemy.exists(unit => unit.unitClass.isBuilding && ! unit.flying)) {
      findBases()
    } else {
      tickle()
    }
  }

  lazy val waitingPoint: Tile = With.geography.allTiles.minBy(tile => With.geography.mains.filterNot(_.owner.isUs).map(_.heart.groundPixels(tile)).sum)
  def findStartLocation(): Unit = {
    // 2-Player: We know where they are. Go SMOrc.
    // 3-player: Scout one base with one probe while keeping the others in the middle.
    // 4-player: Scout two bases with one probe while keeping the others in the middle.
    
    val possibleStarts = With.geography.mains.filter(base => base.lastFrameScoutedByUs <= 0 && ! base.owner.isUs)
    
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
        val nextBase = unscoutedBases.minBy(_.zone.heart.groundPixels(With.geography.home))
        val scout = unassignedScouts.minBy(_.framesToTravelTo(nextBase.heart.center))
        unassignedScouts  -= scout
        unscoutedBases    -= nextBase
        tickle(scout, nextBase)
      }

      unassignedScouts.foreach(tickle(_, waitingPoint.center))
    }
  }
  
  def findBases(): Unit = {
    // Distribute scouts among unscouted bases, preferring to send more to the closest bases
    val unscoutedBases =
      With.geography.bases
        .filter( ! _.owner.isUs)
        .filter(_.isMain || haveSeenABase) //Only search non-start locations until we've killed the first
        .sortBy(_.heart.tileDistanceFast(With.geography.home))
        .sortBy( ! _.isMain)
        .sortBy(_.lastFrameScoutedByUs)
  
    val unassignedScouts = new mutable.HashSet[FriendlyUnitInfo] ++ fighters.units
    while(unassignedScouts.nonEmpty) {
      unscoutedBases.foreach(base => {
        if (unassignedScouts.nonEmpty) {
          val scout = unassignedScouts.minBy(_.pixelDistanceCenter(base.heart.center))
          unassignedScouts.remove(scout)
          tickle(scout, base)
        }
      })
    }
  }
  
  def tickle(unit: FriendlyUnitInfo, base: Base): Unit = {
    tickle(unit, base.heart.center)
  }
  
  def tickle(): Unit = {
    val base = With.geography.enemyBases.toList.sortBy(_.workerCount).lastOption
    val target = base.map(_.heart.center).getOrElse(With.scouting.enemyHome.center)
    fighters.units.foreach(tickle(_, target))
  }
  
  def tickle(unit: FriendlyUnitInfo, target: Pixel): Unit = {
    unit.intend(this)
      .setTravel(target)
      .setCanTickle(true)
  }
}
