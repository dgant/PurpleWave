package Macro

import java.io.PrintWriter
import java.nio.file.Paths

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{CountMap, Seconds}
import mjson.Json

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
trait Zippers {
  private val workerSpawnAgeThreshold = Seconds(7)()
  lazy val zipperRadius = Math.ceil(Protoss.Probe.topSpeed).toInt
  lazy val zippersSteady = new mutable.HashMap[UnitInfo, CountMap[Pixel]]
  lazy val zippersSpawn = new mutable.HashMap[UnitInfo, Pixel]

  def getAccelerantPixelSpawn(mineral: UnitInfo): Option[Pixel] = zippersSpawn.get(mineral)
  def getAccelerantPixelSteady(mineral: UnitInfo): Option[Pixel] = zippersSteady.get(mineral).flatMap(_.mode)

  private def unitNewlySpawned(unit: FriendlyUnitInfo): Boolean = With.framesSince(unit.frameDiscovered) < workerSpawnAgeThreshold
  def onAccelerant(unit: FriendlyUnitInfo, mineral: UnitInfo): Boolean = {
    (if (unitNewlySpawned(unit)) getAccelerantPixelSpawn(unit) else getAccelerantPixelSteady(unit))
      .exists(accelerationPixel =>
        unit.pixelDistanceCenter(accelerationPixel) < zipperRadius
        && unit.pixelDistanceSquared(mineral.pixel) <= accelerationPixel.pixelDistanceSquared(mineral.pixel))
  }


  def updateAccelerantPixels(): Unit = {
    if (With.frame == 0) { onStart() }
    val arrivingWorkers = With.units.ours.filter(u =>
      u.unitClass.isWorker
      && u.orderTarget.exists(t =>
        t.mineralsLeft > 0
        && u.pixelDistanceEdge(t) <= 1
        && u.pixel != u.previousPixel(1)))
    arrivingWorkers.foreach(worker => {
      val framesAgo = 11 + With.latency.latencyFrames
      val zipper = worker.previousPixel(framesAgo)
      if (zipper.pixelDistance(worker.pixel) > 32) {
        val mineral = worker.orderTarget.get
        add(mineral, zipper, 1)
        if (With.framesSince(worker.frameDiscovered) < workerSpawnAgeThreshold) {
          zippersSpawn(mineral) = zippersSpawn.getOrElse(mineral, zipper)
        }
      }
    })
  }

  private def add(mineral: UnitInfo, pixel: Pixel, value: Int): Unit = {
    zippersSteady(mineral) = zippersSteady.getOrElse(mineral, new CountMap[Pixel]())
    val count = zippersSteady(mineral)
    count(pixel) += value
    if (count.keys.size > 12) {
      count --= count.keys.toVector.sortBy(count).take(count.keys.size / 2)
    }
  }

  ///////////////////
  // Serialization //
  ///////////////////

  class MapInfo(
      var hash: String = "",
      val zippersSpawn  : mutable.Buffer[(Tile, Pixel)] = new mutable.ArrayBuffer[(Tile, Pixel)](),
      val zippersSteady : mutable.Buffer[(Tile, Pixel)] = new mutable.ArrayBuffer[(Tile, Pixel)]()) {
    def this(json: Json) {
      this()
      hash = json.at("mapHash").asString
      var i = 0
      val zippersSpawnJson = json.at("zippersSpawn")
      val zippersSteadyJson = json.at("zippersSteady")
      while (i < zippersSpawnJson.asJsonList.size) {
        val zipperJson = zippersSpawnJson.at(i)
        zippersSpawn.append((
          Tile(zipperJson.at("mtx").asInteger, zipperJson.at("mty").asInteger),
          Pixel(zipperJson.at("gpx").asInteger, zipperJson.at("gpy").asInteger)))
        i += 1
      }
      i = 0
      while (i < zippersSteadyJson.asJsonList.size) {
        val zipperJson = zippersSteadyJson.at(i)
        zippersSteady.append((
          Tile(zipperJson.at("mtx").asInteger, zipperJson.at("mty").asInteger),
          Pixel(zipperJson.at("gpx").asInteger, zipperJson.at("gpy").asInteger)))
        i += 1
      }
    }
    def asJson: Json = {
      val output = Json.`object`().set("mapHash", hash).set("zippersSpawn", Json.array()).set("zippersSteady", Json.array())
      for (((t, p), i) <- zippersSpawn.zipWithIndex) {
        output.at("zippersSpawn").add(i).set(i, Json.`object`()).at(i).set("mtx", t.x).set("mty", t.y).set("gpx", p.x).set("gpy", p.y)
      }
      for (((t, p), i) <- zippersSteady.zipWithIndex) {
        output.at("zippersSteady").add(i).set(i, Json.`object`()).at(i).set("mtx", t.x).set("mty", t.y).set("gpx", p.x).set("gpy", p.y)
      }
      output
    }
  }


  lazy val filename: String = f"accelerants-${With.mapCleanName}.json"

  def read(): Unit = {
    Seq(With.bwapiData.ai, With.bwapiData.read, With.bwapiData.write).foreach(directory => {
      try {
        val file = Paths.get(directory, filename).toFile
        if (file.exists()) {
          With.logger.debug(s"Found accelerant logs in ${file.getAbsoluteFile}")
          val jsonText = scala.io.Source.fromFile(file).mkString
          val json = Json.read(jsonText)
          val mapInfo = new MapInfo(json)
          if (mapInfo.hash == With.game.mapHash) {
            mapInfo.zippersSpawn.foreach(p => With.units.neutral.find(_.tileTopLeft == p._1).filterNot(zippersSpawn.contains).foreach(add(_, p._2, 10)))
            mapInfo.zippersSteady.foreach(p => With.units.neutral.find(_.tileTopLeft == p._1).filterNot(zippersSteady.contains).foreach(add(_, p._2, 10)))
          }
        }
      } catch { case exception: Exception => With.logger.onException(exception) }
    })
  }

  def write(): Unit = {
    val info = new MapInfo(
      With.game.mapHash,
      zippersSpawn.map(p => (p._1.tileTopLeft, p._2)).toBuffer,
      zippersSteady.filterNot(_._2.mode.isEmpty).map(p => (p._1.tileTopLeft, p._2.mode.get)).toBuffer)
    val text = info.asJson.toString()
    try {
      val file = Paths.get(With.bwapiData.write, filename).toFile
      val writer = new PrintWriter(file)
      writer.print(text)
      writer.close()
    } catch { case exception: Exception => With.logger.onException(exception) }
  }

  def onStart(): Unit = {
    read()
    val cachedSpawn = With.units.neutral.view.count(zippersSpawn.contains)
    val cachedSteady = With.units.neutral.view.count(zippersSteady.contains)
    if (cachedSpawn > 0) { With.logger.debug(f"$cachedSteady minerals have cached spawn zippers") }
    if (cachedSteady > 0) { With.logger.debug(f"$cachedSteady minerals have cached steady zippers") }
  }

  def onEnd(): Unit = {
    // Read again in case we're running games in parallel so we don't lose data
    read()
    write()
  }
}
