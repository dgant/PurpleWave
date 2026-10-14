package Tactic.Missions

import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.Points.{Pixel, Points, Tile}
import Mathematics.Shapes.Ray
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Seconds

private[Missions] class DropRoute {
  private var destination = Vector.empty[(Tile, Double)]
  private var path: Option[TilePath] = None
  private var values = Vector.empty[(Int, Boolean)]
  private var avoidingVision = true
  private var proximityBoundary = Option.empty[Double]
  private var preferenceTiles = Vector.empty[(Tile, Boolean)]
  private var concealed = false
  private var progress: Option[Tile] = None
  private var progressFrame = With.frame
  private var failureFrame: Option[Int] = None

  def stalled: Boolean = failureFrame.exists(With.framesSince(_) >= Seconds(8)())
  private def indirect(tile: Tile): Boolean = proximityBoundary.forall(tile.proximity < _)
  private def exposed(tile: Tile): Boolean = tile.visibleToEnemy && indirect(tile)
  private def value(tile: Tile): (Int, Boolean) = (With.grids.enemyRangeAir.get(tile), avoidingVision && exposed(tile))

  def next(from: Tile, to: Tile, arrivalRange: Double = 0, following: Vector[(Tile, Double)] = Vector.empty,
    preferUnseen: Boolean = true, friendlyProximity: Option[Double] = None): Option[TilePath] = {
    val stops = (to, arrivalRange) +: following
    if (destination != stops) {
      destination = stops
      path = None
      progress = None
      failureFrame = None
    }
    if (progress.forall(_.tileDistanceFast(from) >= 2)) {
      progress = Some(from)
      progressFrame = With.frame
    }
    val proximityModeChanged = proximityBoundary.isDefined != friendlyProximity.isDefined
    proximityBoundary = friendlyProximity
    if (avoidingVision != preferUnseen || proximityModeChanged ||
      preferenceTiles.exists { case (tile, applied) => indirect(tile) != applied }) {
      avoidingVision = preferUnseen
      path = None
    }
    val changed = path.exists(p => p.tiles.get.map(value).toVector != values)
    val offPath = path.exists(_.tiles.get.forall(_.tileDistanceFast(from) > 3))
    if (path.isEmpty || changed || offPath) {
      // Choose the sight-region contact using both legs, not the nearest point
      // on the inbound leg. The shared pathfinder still owns threat evasion.
      val contact = if (following.isEmpty) to else DropRoute.flybys(from, stops).head
      def find(goal: Tile, range: Double, visionCost: Double): Option[TilePath] = Some(new PathfindProfile(from, Some(goal),
        endDistanceMaximum = range,
        threatMaximum = Some(0),
        canCrossUnwalkable = Some(true), canEndUnwalkable = Some(true),
        costEnemyVision = visionCost, costRepulsion = 5, costThreat = 10,
        repulsors = Vector(PathfindRepulsor(Points.middle, 1, Math.min(With.mapPixelWidth, With.mapPixelHeight))),
        threatMaximumAlongPath = Some(0), explorationMaximum = Some(With.mapTileArea),
        useVisionAndRepulsion = indirect).find).filter(_.pathExists)
      def findRoute(visionCost: Double): Option[TilePath] = if (following.isEmpty) find(to, arrivalRange, visionCost) else {
        val preferred = if (With.grids.enemyRangeAir.get(contact) == 0 && (visionCost == 0 || !exposed(contact))) find(contact, 0, visionCost) else None
        // A geometric contact can be blocked. Any safe point in the original
        // sight region is preferable to abandoning an otherwise reachable peek.
        preferred.orElse(find(to, arrivalRange, visionCost))
      }
      def unseen(route: TilePath): Boolean = route.tiles.get.forall(t => !exposed(t))
      def length(route: TilePath): Double = route.tiles.get.sliding(2).filter(_.size == 2)
        .map(pair => Math.sqrt(pair.head.tileDistanceSquared(pair.last).toDouble)).sum
      // Preserve the perimeter preference, but only pay extra for actual concealment.
      // Fewer visible tiles provide no surprise once the enemy sees the flight anyway.
      path = findRoute(0).map(normal => {
        if (!preferUnseen || exposed(from) || (arrivalRange == 0 && exposed(to)) || unseen(normal)) normal else
          findRoute(10).filter(p => unseen(p) && length(p) <= 1.25 * length(normal)).getOrElse(normal)
      })
      concealed = preferUnseen && path.exists(unseen)
      values = path.map(_.tiles.get.map(value).toVector).getOrElse(Vector.empty)
      // Watch both the current route and its direct shortcut as the army advances.
      // Small proximity changes that cross no tiles need not rerun A* every frame.
      preferenceTiles = if (proximityBoundary.isEmpty) Vector.empty else path.map(p =>
        (p.tiles.get ++ Ray(from.center, to.center).map(_.tile)).distinct.map(t => t -> indirect(t)).toVector).getOrElse(Vector.empty)
    }
    if (path.isEmpty || With.framesSince(progressFrame) >= Seconds(8)()) {
      failureFrame = failureFrame.orElse(Some(With.frame))
      None
    } else {
      failureFrame = None
      path
    }
  }

  // Check the actual accelerated command, including its entire flight segment.
  // Long clear headings retain speed; a turn must not cut across enemy range.
  def waypoint(transport: FriendlyUnitInfo, route: TilePath): Option[Pixel] = {
    val tiles = route.tiles.get
    val index = tiles.indices.minBy(i => tiles(i).center.pixelDistanceSquared(transport.pixel))
    // Finish the escape leg before looking ahead toward the raid.
    val escapeEnd = if (transport.tile.enemyRangeAgainst(transport) == 0) tiles.size - 1 else
      (index until tiles.size).find(i => tiles(i).enemyRangeAgainst(transport) == 0).getOrElse(tiles.size - 1)
    val end = Math.min(index + 16, escapeEnd)
    val candidates = (end to Math.min(index + 1, end) by -1).iterator.map(tiles(_).center).filter(pixel => {
      val flight = Ray(transport.pixel, Commander.adjustDestination(transport, pixel)).map(_.tile.enemyRangeAgainst(transport)).toVector
      MicroPathing.isSafeMove(transport, pixel) && flight.sliding(2).forall(p => p.last <= p.head)
    }).toStream
    if (!concealed || !DropRoute.preferUnseen(transport)) candidates.headOption else
      // Concealment is optional: an accelerated turn that reveals us must not stall a safe flight.
      candidates.find(pixel => Ray(transport.pixel, Commander.adjustDestination(transport, pixel))
        .forall(p => !exposed(p.tile))).orElse(candidates.headOption)
  }
}

private[Missions] object DropRoute {
  def preferUnseen(transport: FriendlyUnitInfo): Boolean =
    !transport.visibleToOpponents && With.framesSince(transport.lastFrameVisibleToOpponents) >= Seconds(15)()

  def hasSafePathShorterThan(from: Pixel, to: Pixel, maximumDistance: Double): Boolean = {
    if (!from.valid || !to.valid || from.pixelDistance(to) >= maximumDistance) return false
    if (Ray(from, to).forall(p => With.grids.enemyRangeAir.get(p.tile) == 0)) return true
    if (With.grids.enemyRangeAir.get(to.tile) > 0) return false
    // Measure the shortest safe route, not the vision/perimeter penalties of the preferred flight.
    val shortest = new PathfindProfile(from.tile, Some(to.tile),
      threatMaximum = Some(0), canCrossUnwalkable = Some(true), canEndUnwalkable = Some(true),
      threatMaximumAlongPath = Some(0), explorationMaximum = Some(With.mapTileArea)).find
    shortest.tiles.exists(tiles => {
      val points = from +: tiles.drop(1).dropRight(1).map(_.center) :+ to
      points.sliding(2).map(pair => pair.head.pixelDistance(pair.last)).sum < maximumDistance
    })
  }

  // Tighten a polyline through sight discs, keeping the following raid as an
  // anchor. Each adjustment minimizes the two adjacent legs. Repeated forward
  // and backward passes also remove bends across several scouting stops.
  def flybys(from: Tile, stops: Vector[(Tile, Double)]): Vector[Tile] = {
    val points = stops.map { case (tile, _) => (tile.x.toDouble, tile.y.toDouble) }.toArray
    def distance(a: (Double, Double), b: (Double, Double)): Double = Math.hypot(a._1 - b._1, a._2 - b._2)
    for (_ <- 0 until 12; i <- points.indices ++ points.indices.reverse) {
      val center = (stops(i)._1.x.toDouble, stops(i)._1.y.toDouble)
      val radius = Math.max(0, stops(i)._2 - 0.75) // Allow for rounding to a tile.
      val before = if (i == 0) (from.x.toDouble, from.y.toDouble) else points(i - 1)
      val after = if (i + 1 == points.length) before else points(i + 1)
      val dx = after._1 - before._1
      val dy = after._2 - before._2
      val lengthSquared = dx * dx + dy * dy
      val fraction = if (lengthSquared == 0) 0.0 else Math.max(0, Math.min(1,
        ((center._1 - before._1) * dx + (center._2 - before._2) * dy) / lengthSquared))
      val projection = (before._1 + fraction * dx, before._2 + fraction * dy)
      points(i) = if (radius == 0) center else if (distance(projection, center) <= radius) projection else {
        val incoming = Math.atan2(before._2 - center._2, before._1 - center._1)
        val outgoing = Math.atan2(after._2 - center._2, after._1 - center._1)
        val turn = Math.atan2(Math.sin(outgoing - incoming), Math.cos(outgoing - incoming))
        var low = Math.min(incoming, incoming + turn)
        var high = Math.max(incoming, incoming + turn)
        def onCircle(angle: Double): (Double, Double) = (center._1 + radius * Math.cos(angle), center._2 + radius * Math.sin(angle))
        def length(angle: Double): Double = { val p = onCircle(angle); distance(before, p) + distance(p, after) }
        for (_ <- 0 until 24) {
          val left = (2 * low + high) / 3
          val right = (low + 2 * high) / 3
          if (length(left) < length(right)) high = right else low = left
        }
        onCircle((low + high) / 2)
      }
    }
    points.map { case (x, y) => Tile(Math.round(x).toInt, Math.round(y).toInt).clip }.toVector
  }
}
