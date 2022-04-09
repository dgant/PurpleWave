package Macro.Allocation

import Lifecycle.With
import Mathematics.Points.Tile
import Performance.Tasks.TimedTask
import Planning.Plans.Basic.NoPlan
import Planning.Prioritized
import Planning.ResourceLocks.LockTiles
import Utilities.Time.Forever

import scala.collection.mutable.ArrayBuffer

final class Groundskeeper extends TimedTask {
  class TileReservation(val tile: Tile, var owner: Prioritized, var update: Int) {
    def renewed: Boolean = update >= With.groundskeeper.updates
    def recent: Boolean = update >= With.groundskeeper.updates - 1
  }

  lazy val tileReservations: Array[TileReservation] =
    (0 until With.mapTileWidth).flatMap(x =>
      (0 until With.mapTileHeight).map(y =>
        new TileReservation(Tile(x, y), NoPlan(), -Forever()))).toArray

  lazy val reservations = new ArrayBuffer[(LockTiles, Seq[TileReservation])]()

  var updates: Int = 0

  override protected def onRun(budgetMs: Long): Unit = {
    updates += 1
    var inactive: Option[LockTiles] = None
    do {
      inactive = reservations.find(_._2.forall( ! _.recent)).map(_._1)
      inactive.foreach(release)
    } while (inactive.isDefined)
  }

  def isFree(tile: Tile): Boolean = tile.valid && ! tileReservations(tile.i).renewed

  def reserved: Seq[Tile] = reservations.view.flatMap(_._2.view).map(_.tile)

  def satisfy(lock: LockTiles): Boolean = {
    release(lock)
    lazy val lockReservations = lock.tiles.view.map(_.i).map(tileReservations)
    lock.satisfied = lock.tiles.forall(_.valid) && lockReservations.forall(res => res.owner == lock.owner || ! res.renewed)
    if (lock.satisfied) {
      lockReservations.foreach(_.owner = lock.owner)
      lockReservations.foreach(_.update = updates)
      reservations += ((lock, lockReservations))
    }
    lock.satisfied
  }

  def release(lock: LockTiles): Unit = {
    val current = reservations.view.filter(_._1.owner == lock.owner).toVector
    current.view.flatMap(_._2).filter(_.owner == lock.owner).foreach(_.update = -Forever())
    reservations --= current
    lock.satisfied = false
  }
}
