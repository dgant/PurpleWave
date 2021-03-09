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
  * https://github.com/bmnielsen/Stardust/blob/b8a91e52f453e6fdc60798edac569826df98148a/src/Workers/WorkerOrderTimer.cpp
  * which was in turn based on research done by, I think, Ankmairdor.
  * See also: https://tl.net/forum/bw-strategy/530231-mineral-boosting
  *
  * Via Bruce, on Discord:
  * Order timer optimization gives about 1-2% with one worker per patch and about half that with two workers per patch.
  * The timer is on a 9-frame cycle, so without manipulation that basically means a worker will wait a random number
  * of frames between 0 and 8 before mining once it reaches a patch. The manipulation is to re-issue the mine order
  * when the worker is exactly 9 frames away, which when done correctly ensures you get the 0 frame delay every time.
  * So you can say it on average saves you about 4 frames per round trip (which is typically like 150 frames for a close patch IIRC).
  * A wrinkle is that every 150 frames the game resets all of the order timers, so if that happens while you are trying
  * to manipulate it, you'll get a random wait instead.
  *
  * The same thing applies when two workers are on a patch and a worker is waiting for the other to finish.
  * Once the first worker finishes mining, the other worker will only start mining once its timer hits the right frame.
  * So if you re-issue the mine order for it exactly 9 frames before the first worker finishes mining, it will take over immediately.
  * In this case the 150-frame reset is even worse though, since this also affects how many frames it takes the first worker to finish mining,
  * so in practice you can only really manipulate this correctly half the time.
  * And as an aside, what is referred to as mineral boosting is this exact effect but on the resource delivery end.
  * It is basically manipulating the worker's pathing to ensure it returns minerals on the exact frame that causes no delay,
  * which has the side effect that the worker doesn't need to stop and preserves its speed.
  * This is much more difficult to manipulate though, since workers stop when you re-issue the return cargo command.
  * So I don't think we'll see it in bots soon, but you never know - it has a higher potential benefit compared to the optimization I described above.
  *
  * Yeah, it's basically just manipulating the return path to make it a multiple of 9 frames from stopping mining to return cargo, causing the worker to maintain speed.
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
