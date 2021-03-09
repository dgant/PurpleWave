package Macro

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.CountMap

import scala.collection.mutable

/**
  * Identifies pixels that mineral harvesters cross at exactly 11+LF prior to arriving their mineral
  * This point in their mining cycle is the optimal time to issue a Gather command
  * because the mining cycle can only actually begin at periodic intervals after the command was issued;
  * Starting a cycle at 11 + LF from mineral adjacency causes the cycle to begin immediately.
  *
  * The logic and approach is borrowed from
  * </src/Workers/WorkerOrderTimer.cpp
  * which was in turn based on research done by, I think, Ankmairdor
  */
trait AccelerantPixels {
  lazy val accelerantPixels = new mutable.HashMap[UnitInfo, CountMap[Pixel]]
  lazy val accelerantPixelRadius = Math.ceil(Protoss.Probe.topSpeed).toInt

  def getAccelerantPixel(mineral: UnitInfo): Option[Pixel] = accelerantPixels.get(mineral).flatMap(_.mode)

  def onAccelerant(unit: FriendlyUnitInfo, mineral: UnitInfo): Boolean = {
    getAccelerantPixel(mineral).exists(accelerationPixel =>
      unit.pixelDistanceCenter(accelerationPixel) < accelerantPixelRadius
      && unit.pixelDistanceSquared(mineral.pixel) <= accelerationPixel.pixelDistanceSquared(mineral.pixel))
  }

  def updateAccelerantPixels(): Unit = {
    val arrivingWorkers = With.units.ours.filter(u =>
      u.unitClass.isWorker
      && u.orderTarget.exists(t =>
        t.mineralsLeft > 0
        && u.pixelDistanceEdge(t) <= 1
        && u.pixel != u.previousPixel(1)))
    arrivingWorkers.foreach(worker => {
      val framesAgo = 11 + With.latency.latencyFrames
      val accelerantPixel = worker.previousPixel(framesAgo)
      if (accelerantPixel.pixelDistance(worker.pixel) > 32) {
        val mineral = worker.orderTarget.get
        if ( ! accelerantPixels.contains(mineral)) {
          accelerantPixels(mineral) = new CountMap[Pixel]()
        }
        val count = accelerantPixels(mineral)
        count(accelerantPixel) += 1
        // Limit the map size
        if (count.keys.size > 12) {
          count --= count.keys.toVector.sortBy(count).take(count.keys.size / 2)
        }

      }
    })
  }
}
