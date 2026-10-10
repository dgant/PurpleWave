package Micro.Actions.Protoss.Shuttle

import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object ShuttleEscape extends Action {
  private val margin = 64.0

  private def clearance(shuttle: FriendlyUnitInfo, threat: UnitInfo, at: Pixel): Double =
    threat.pixelDistanceEdge(shuttle, at) - threat.pixelRangeAgainst(shuttle)

  def active(shuttle: FriendlyUnitInfo): Boolean = {
    if (!Protoss.Shuttle(shuttle) || !shuttle.agent.contains(this)) return false
    if (shuttle.matchups.threats.forall(clearance(shuttle, _, shuttle.pixel) >= margin)) {
      shuttle.agent.remove(this)
      return false
    }
    true
  }

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = Protoss.Shuttle(shuttle) &&
    (active(shuttle) || (shuttle.loadedUnits.exists(Protoss.Reaver) &&
      shuttle.agent.passengersPrioritized.filter(Protoss.Reaver).forall(_.loaded) &&
      shuttle.matchups.threatsInRange.nonEmpty))

  // Explicit retreats also protect empty Shuttles from later pickup/parking orders.
  def start(shuttle: FriendlyUnitInfo): Boolean = {
    if (!Protoss.Shuttle(shuttle) ||
      shuttle.matchups.threats.forall(clearance(shuttle, _, shuttle.pixel) >= margin)) return false
    if (!active(shuttle)) shuttle.agent.put(this, shuttle.pixel)
    delegate(shuttle)
  }

  def waypoint(shuttle: FriendlyUnitInfo): Pixel = {
    val from = shuttle.pixel
    val threats = shuttle.matchups.threats.toVector
    def depth(threat: UnitInfo, at: Pixel): Double = Math.max(0, margin - clearance(shuttle, threat, at))
    val initial = threats.map(depth(_, from))
    def samples(to: Pixel): Vector[Pixel] = {
      val steps = Math.max(1, Math.ceil(from.pixelDistance(to) / 16).toInt)
      (0 to steps).map(i => Pixel(
        Math.round(from.x + (to.x - from.x) * i.toDouble / steps).toInt,
        Math.round(from.y + (to.y - from.y) * i.toDouble / steps).toInt)).toVector
    }
    def depths(points: Vector[Pixel]): Vector[Seq[Double]] = points.map(p => threats.map(depth(_, p)))
    def excess(values: Vector[Seq[Double]]): Double =
      values.map(v => v.zip(initial).map { case (now, before) => Math.max(0, now - before) }.foldLeft(0.0)(Math.max)).max

    // Keep a safe escape heading instead of turning back toward the raid every update.
    shuttle.agent.get[Pixel](this).filter(to => from.pixelDistance(to) > 128 && {
      val values = depths(samples(to))
      excess(values) == 0 && values.last.forall(_ == 0)
    }).foreach(to => return to)

    val away = threats.map(t => t.pixel.radiansTo(from))
    val radians = away ++ (0 until 64).map(_ * 2.0 * Math.PI / 64)
    val candidates = radians.map(from.radiateRadians(_, 320).clamp(shuttle.unitClass.dimensionMax / 2)).distinct
      .filter(from.pixelDistance(_) >= 80)
    candidates.minBy(to => {
      val points = samples(to)
      val values = depths(points)
      val deeper = excess(values)
      val clear = values.indexWhere(_.forall(_ == 0))
      val exitDistance = if (clear < 0) 640.0 + values.last.sum else from.pixelDistance(points(clear))
      val turn = shuttle.framesToTurnTo(to)
      // Never deepen an existing threat or enter another one when a clean escape exists.
      // Then prefer the quickest exit, irrespective of the next mission destination.
      (if (deeper == 0) 0 else 1, deeper, Math.max(0, 256 - from.pixelDistance(to)),
        exitDistance / Math.max(1, shuttle.topSpeed) + turn, values.map(_.sum).sum)
    })
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val to = waypoint(shuttle)
    shuttle.agent.put(this, to)
    shuttle.agent.decision.set(to)
    Commander.move(shuttle, allowOvershoot = false)
  }
}
