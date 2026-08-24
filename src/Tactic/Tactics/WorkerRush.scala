package Tactic.Tactics

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Actions.Combat.Tactics.Tickle2026
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.IsWorker
import Utilities.UnitPreferences.PreferClose

import scala.collection.mutable

class WorkerRush extends Tactic {

  override def launch(): Unit = {
    val attackers = With.blackboard.workerRushers()
    lock.release()

    if (attackers <= 0) return

    lock
      .setMatcher(IsWorker)
      .setCounter(CountUpTo(attackers))
      .setPreference(PreferClose(With.scouting.enemyHome.center))
      .setInterruptible(false)
      .acquire()

    haveSeenABase ||= With.geography.enemyBases.nonEmpty

    if ( ! haveSeenABase) {
      findStartLocation()
    } else if (With.geography.enemyBases.isEmpty && ! With.units.enemy.exists(unit => unit.unitClass.isBuilding && ! unit.flying)) {
      findBases()
    } else {
      tickle()
    }
  }

  private var haveSeenABase = false

  private lazy val waitingPoint: Tile = With.geography.allTiles.minBy(tile => With.geography.mains.filterNot(_.owner.isUs).map(_.heart.groundPixels(tile)).sum)

  private def findStartLocation(): Unit = {
    // 2-player: We know where they are. Go SMOrc.
    // 3-player: Scout one base with one worker while keeping the others in the middle.
    // 4-player: Scout two bases with one worker while keeping the others in the middle.
    val possibleStarts = With.geography.mains.filter(base => base.lastFrameScoutedByUs <= 0 && ! base.owner.isUs)

    if (possibleStarts.isEmpty) {
      findBases()
    } else if (possibleStarts.size == 1) {
      lock.units.foreach(tickle(_, possibleStarts.head))
    } else {
      // Scout all but the furthest base and keep the rest of the workers in the middle.
      val unscoutedBases   = new mutable.ArrayBuffer[Base] ++ possibleStarts
      val unassignedScouts = new mutable.HashSet[FriendlyUnitInfo] ++ lock.units
      while (unassignedScouts.nonEmpty && unscoutedBases.size > 1) {
        val nextBase = unscoutedBases.minBy(_.zone.heart.groundPixels(With.geography.home))
        val scout = unassignedScouts.minBy(_.framesToTravelTo(nextBase.heart.center))
        unassignedScouts -= scout
        unscoutedBases   -= nextBase
        tickle(scout, nextBase)
      }

      unassignedScouts.foreach(tickle(_, waitingPoint.center))
    }
  }

  private def findBases(): Unit = {
    // Distribute scouts among unscouted bases, preferring to send more to the closest bases.
    val unscoutedBases =
      With.geography.bases
        .filter( ! _.owner.isUs)
        .filter(_.isMain || haveSeenABase)
        .sortBy(_.heart.tileDistanceFast(With.geography.home))
        .sortBy( ! _.isMain)
        .sortBy(_.lastFrameScoutedByUs)

    val unassignedScouts = new mutable.HashSet[FriendlyUnitInfo] ++ lock.units
    if (unscoutedBases.isEmpty) {
      unassignedScouts.foreach(tickle(_, With.scouting.enemyHome.center))
      return
    }
    while (unassignedScouts.nonEmpty) {
      unscoutedBases.foreach(base => {
        if (unassignedScouts.nonEmpty) {
          val scout = unassignedScouts.minBy(_.pixelDistanceCenter(base.heart.center))
          unassignedScouts -= scout
          tickle(scout, base)
        }
      })
    }
  }

  private def tickle(unit: FriendlyUnitInfo, base: Base): Unit = {
    tickle(unit, base.heart.center)
  }

  private def tickle(): Unit = {
    val base = With.geography.enemyBases.toList.sortBy(_.workerCount).lastOption
    val target = base.map(_.heart.center).getOrElse(With.scouting.enemyHome.center)
    lock.units.foreach(tickle(_, target))
  }

  private def tickle(unit: FriendlyUnitInfo, target: Pixel): Unit = {
    unit.intend(this)
      .setTerminus(target)
      .setAction(Tickle2026)
  }
}
