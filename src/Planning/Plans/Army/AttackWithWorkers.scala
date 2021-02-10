package Planning.Plans.Army

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{CountEverything, UnitCounter}
import Planning.UnitMatchers.MatchWorkers
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class AttackWithWorkers(counter: UnitCounter = CountEverything) extends Plan {
  
  val fighters = new LockUnits
  fighters.matcher.set(MatchWorkers)
  fighters.counter.set(counter)
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

  lazy val waitingPoint: Tile = With.geography.allTiles.minBy(tile => With.geography.startBases.filterNot(_.owner.isUs).map(_.heart.groundPixels(tile)).sum)
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
        val scout = unassignedScouts.minBy(_.framesToTravelTo(nextBase.heart.pixelCenter))
        unassignedScouts  -= scout
        unscoutedBases    -= nextBase
        tickle(scout, nextBase)
      }

      unassignedScouts.foreach(tickle(_, waitingPoint.pixelCenter))
    }
  }
  
  def findBases() {
    // Distribute scouts among unscouted bases, preferring to send more to the closest bases
    val unscoutedBases =
      With.geography.bases
        .filter( ! _.owner.isUs)
        .filter(_.isStartLocation || haveSeenABase) //Only search non-start locations until we've killed the first
        .sortBy(_.heart.tileDistanceFast(With.geography.home))
        .sortBy( ! _.isStartLocation)
        .sortBy(_.lastScoutedFrame)
  
    val unassignedScouts = new mutable.HashSet[FriendlyUnitInfo] ++ fighters.units
    while(unassignedScouts.nonEmpty) {
      unscoutedBases.foreach(base => {
        if (unassignedScouts.nonEmpty) {
          val scout = unassignedScouts.minBy(_.pixelDistanceCenter(base.heart.pixelCenter))
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
    val base = With.geography.enemyBases.toList.sortBy(_.workerCount).lastOption
    val target = base.map(_.heart.pixelCenter).getOrElse(With.scouting.mostBaselikeEnemyTile.pixelCenter)
    fighters.units.foreach(tickle(_, target))
  }
  
  def tickle(unit: FriendlyUnitInfo, target: Pixel) {
    unit.agent.intend(this, new Intention {
      toTravel = Some(target)
      canTickle = true
    })
  }
}
