package Micro.Formation

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

/** Assigns melee defenders to the actual collision gaps in an occupied natural wall. */
class WallGapPositions(base: Base) {
  import WallGapPositions._

  private var previousBuildings = Vector.empty[(Box, Boolean)]
  private var previousDoor: Option[Pixel] = None
  private var geometry: Option[Geometry] = None

  def apply(units: Iterable[FriendlyUnitInfo]): Map[FriendlyUnitInfo, Pixel] = {
    if (! base.isNatural || ! With.enemies.exists(_.isZerg)) return Map.empty
    val defenders = units.filter(u => u.alive && u.complete && ! u.loaded && u.isAny(Protoss.Zealot, Protoss.Probe)).toVector
    if (defenders.isEmpty) return Map.empty
    val foundations = With.placement.get(base).flatMap(f => With.placement.get(f.tile)).distinct.filter(_.point.requirement.labels.contains(PlaceLabels.Wall))
    val wall = With.units.ours.filter(u => u.alive && ! u.flying && u.unitClass.isBuilding && foundations.exists(f =>
      f.tile == u.tileTopLeft && f.point.requirement.dimensions == ((u.unitClass.tileWidth, u.unitClass.tileHeight)))).toVector
    if (! wall.exists(u => u.unitClass.tileWidth == 4 && u.unitClass.tileHeight == 3)
      || ! wall.exists(u => u.unitClass.tileWidth == 3 && u.unitClass.tileHeight == 2)) return Map.empty

    val wallBoxes = wall.filterNot(_.unitClass.isStaticDefense).map(u => body(u.unitClass).at(u.pixel))
    val vicinity = Box(wallBoxes.map(_.left).min, wallBoxes.map(_.top).min,
      wallBoxes.map(_.right).max, wallBoxes.map(_.bottom).max).expand(128)
    val buildings = With.units.all.filter(u => u.alive && ! u.flying && (u.unitClass.isBuilding || u.unitClass.isMinerals))
      .map(u => body(u.unitClass).at(u.pixel)).filter(_.intersects(vicinity)).toVector.distinct
      .sortBy(b => (b.left, b.top, b.right, b.bottom)).map(b => (b, wallBoxes.contains(b)))
    val door = With.placement.wall.filter(w => w.buildings.exists(b => wall.exists(_.tileTopLeft == b._1))).flatMap(_.gap).map(_.center)
    if (geometry.isEmpty || buildings != previousBuildings || door != previousDoor) {
      previousBuildings = buildings
      previousDoor = door
      geometry = Some(new Geometry(buildings, base.townHallArea.center, door,
        (x, y) => x >= 0 && y >= 0 && x < With.mapTileWidth * 4 && y < With.mapTileHeight * 4 && With.game.isWalkable(x, y),
        body(Zerg.Zergling), p => {
          val requirement = With.placement.at(p.tile).requirement
          requirement.walkableAfter && ! requirement.buildableAfter
        }))
    }
    val byId = defenders.map(u => u.id -> u).toMap
    geometry.get.assign(defenders.map(u => Defender(u.id, body(u.unitClass), Protoss.Probe(u), u.pixel)))
      .map { case (id, pixel) => byId(id) -> pixel }
  }
}

object WallGapPositions {
  private[Formation] case class Box(left: Int, top: Int, right: Int, bottom: Int) {
    def center: Pixel = Pixel((left + right) / 2, (top + bottom) / 2)
    def expand(n: Int): Box = Box(left - n, top - n, right + n, bottom + n)
    def intersects(b: Box): Boolean = left < b.right && b.left < right && top < b.bottom && b.top < bottom
    def touches(b: Box): Boolean = left <= b.right && b.left <= right && top <= b.bottom && b.top <= bottom
    def contains(p: Pixel): Boolean = p.x >= left && p.x < right && p.y >= top && p.y < bottom
  }
  private[Formation] case class Body(left: Int, up: Int, right: Int, down: Int) {
    def at(p: Pixel): Box = Box(p.x - left, p.y - up, p.x + right, p.y + down)
    // Integer centers at which this body would collide with the obstacle.
    def forbidden(b: Box): Box = Box(b.left - right + 1, b.top - down + 1, b.right + left, b.bottom + up)
    def extent(x: Double, y: Double): Double = Math.abs(x) * (if (x < 0) left else right) + Math.abs(y) * (if (y < 0) up else down)
  }
  private def body(kind: UnitClass): Body = Body(kind.dimensionLeft, kind.dimensionUp, kind.dimensionRightExclusive, kind.dimensionDownExclusive)
  private[Formation] case class Defender(id: Int, body: Body, probe: Boolean, pixel: Pixel)
  private[Formation] case class Gap(first: Box, second: Box, a: Pixel, b: Pixel, nx: Double, ny: Double, outer: Double, inner: Double, doorway: Boolean) {
    val center: Pixel = a.midpoint(b)
    private val length = Math.hypot(b.x - a.x, b.y - a.y)
    val width: Int = Math.ceil(length).toInt
    val tx: Double = (b.x - a.x) / length
    val ty: Double = (b.y - a.y) / length
    def depth(p: Pixel): Double = (p.x - center.x) * nx + (p.y - center.y) * ny
    def across(p: Pixel): Double = (p.x - center.x) * tx + (p.y - center.y) * ty
    def at(across: Double, depth: Double): Pixel = Pixel(
      Math.round(center.x + across * tx + depth * nx).toInt,
      Math.round(center.y + across * ty + depth * ny).toInt)
  }

  /** Pure geometry apart from the supplied, immutable terrain walkability snapshot. */
  private[Formation] class Geometry(buildings: Vector[(Box, Boolean)], inside: Pixel, door: Option[Pixel], walkable: (Int, Int) => Boolean, ling: Body, hallway: Pixel => Boolean = _ => false) {
    private val boxes = buildings.map(_._1)
    private val walls = buildings.filter(_._2).map(_._1)
    private val bounds = Box(walls.map(_.left).min, walls.map(_.top).min, walls.map(_.right).max, walls.map(_.bottom).max).expand(96)

    // Prefix sums make footprint/terrain collision checks constant-time. The extra
    // margin includes reinforcement positions and Zergling contact samples.
    private val minX = Math.floorDiv(bounds.left - 160, 8)
    private val minY = Math.floorDiv(bounds.top - 160, 8)
    private val width = Math.floorDiv(bounds.right + 160, 8) - minX + 1
    private val height = Math.floorDiv(bounds.bottom + 160, 8) - minY + 1
    private val terrain = Array.tabulate(width * height)(i => walkable(minX + i % width, minY + i / width))
    private val stride = width + 1
    private val blocked = Array.fill(stride * (height + 1))(0)
    for (y <- 0 until height; x <- 0 until width) {
      val i = x + 1 + (y + 1) * stride
      blocked(i) = (if (terrain(x + y * width)) 0 else 1) + blocked(i - 1) + blocked(i - stride) - blocked(i - stride - 1)
    }
    private def terrainOpen(x: Int, y: Int): Boolean = terrain(x - minX + (y - minY) * width)

    def fits(body: Body, p: Pixel): Boolean = {
      val b = body.at(p)
      if (boxes.exists(_.intersects(b))) return false
      val x0 = Math.floorDiv(b.left, 8) - minX
      val y0 = Math.floorDiv(b.top, 8) - minY
      val x1 = Math.floorDiv(b.right - 1, 8) - minX + 1
      val y1 = Math.floorDiv(b.bottom - 1, 8) - minY + 1
      x0 >= 0 && y0 >= 0 && x1 <= width && y1 <= height &&
        blocked(x1 + y1 * stride) - blocked(x0 + y1 * stride) - blocked(x1 + y0 * stride) + blocked(x0 + y0 * stride) == 0
    }

    // Merge walk cells into rectangles so a straight cliff is one side of one gap.
    private val terrainBoxes: Vector[Box] = {
      val result = mutable.ArrayBuffer.empty[Box]
      var active = Map.empty[(Int, Int), Int]
      for (y <- Math.floorDiv(bounds.top, 8) to Math.floorDiv(bounds.bottom, 8)) {
        val next = mutable.Map.empty[(Int, Int), Int]
        var x = Math.floorDiv(bounds.left, 8)
        val end = Math.floorDiv(bounds.right, 8)
        while (x <= end) {
          if (terrainOpen(x, y)) x += 1
          else {
            val start = x
            while (x <= end && ! terrainOpen(x, y)) x += 1
            val key = (start, x)
            val index = active.getOrElse(key, result.size)
            if (index == result.size) result += Box(8 * start, 8 * y, 8 * x, 8 * (y + 1))
            else result(index) = result(index).copy(bottom = 8 * (y + 1))
            next(key) = index
          }
        }
        active = next.toMap
      }
      result.toVector
    }

    private def between(a: Box, b: Box): Option[Gap] = {
      def closest(lowA: Int, highA: Int, lowB: Int, highB: Int): (Int, Int) = {
        if (highA <= lowB) (highA, lowB)
        else if (highB <= lowA) (lowA, highB)
        else { val middle = (Math.max(lowA, lowB) + Math.min(highA, highB)) / 2; (middle, middle) }
      }
      val (ax, bx) = closest(a.left, a.right, b.left, b.right)
      val (ay, by) = closest(a.top, a.bottom, b.top, b.bottom)
      val width = Math.hypot(bx - ax, by - ay)
      if (width == 0 || width > 96 || ling.forbidden(a).touches(ling.forbidden(b))) return None
      val center = Pixel((ax + bx) / 2, (ay + by) / 2)
      val delta = inside.subtract(center)
      val sign = if ((ay - by) * delta.x + (bx - ax) * delta.y >= 0) 1 else -1
      val nx = sign * (ay - by) / width
      val ny = sign * (bx - ax) / width
      // Ignore alleys along the back of the wall, such as those between cannon rows.
      if (nx * delta.x + ny * delta.y < 0.45 * Math.hypot(delta.x, delta.y)) return None
      val diagonal = ax != bx && ay != by
      val depths = if (diagonal) Vector(0.0) else if (ax != bx)
        Vector(ny * (Math.max(a.top, b.top) - center.y), ny * (Math.min(a.bottom, b.bottom) - center.y))
      else Vector(nx * (Math.max(a.left, b.left) - center.x), nx * (Math.min(a.right, b.right) - center.x))
      val reservedDoor = door.isEmpty && (Math.floorDiv(Math.min(ax, bx), 32) to Math.floorDiv(Math.max(ax, bx), 32)).exists(x =>
        (Math.floorDiv(Math.min(ay, by), 32) to Math.floorDiv(Math.max(ay, by), 32)).exists(y => {
          val p = Tile(x, y).center
          hallway(p) && ! boxes.exists(_.contains(p)) && p.pixelDistanceSquared(center) <= width * width / 4 + 32 * 32
        }))
      val g = Gap(a, b, Pixel(ax, ay), Pixel(bx, by), nx, ny, depths.min, depths.max,
        door.exists(p => Math.abs((p.x - center.x) * nx + (p.y - center.y) * ny) <= Math.max(32, depths.max - depths.min) &&
          Math.abs((p.x - center.x) * (bx - ax) / width + (p.y - center.y) * (by - ay) / width) < width / 2) || reservedDoor)
      // Reject closed pockets and channels obstructed by a third building.
      if (! (g.outer.toInt - 8 to g.inner.toInt + 8 by 4).forall(d => fits(ling, g.at(0, d)))) None else Some(g)
    }

    private val rawGaps = walls.flatMap(a => (boxes ++ terrainBoxes).filterNot(_ == a).flatMap(b => between(a, b)))
    val gaps: Vector[Gap] = {
      val distinct = mutable.ArrayBuffer.empty[Gap]
      // A jagged cliff can produce several rectangles bordering the same passage.
      rawGaps.sortBy(g => (g.width, g.center.x, g.center.y)).foreach { g =>
        val duplicate = distinct.indexWhere(other =>
          (other.first == g.first || other.first == g.second || other.second == g.first || other.second == g.second) &&
          other.nx * g.nx + other.ny * g.ny > 0.9 &&
          Math.abs(other.across(g.center)) < Math.min(other.width, g.width) / 2.0 &&
          Math.abs(other.depth(g.center)) <= 128)
        if (duplicate < 0) distinct += g
        else if (g.doorway) distinct(duplicate) = distinct(duplicate).copy(doorway = true)
      }
      distinct.toVector.sortBy(g => (if (g.doorway) 0 else 1, -g.width, g.center.x, g.center.y))
    }

    private case class Slot(pixel: Pixel, box: Box, blocked: Box, exposure: Int, depth: Double, across: Double)
    private val slots = mutable.Map.empty[(Gap, Body), Vector[Slot]]
    private def candidates(g: Gap, body: Body): Vector[Slot] = slots.getOrElseUpdate((g, body), {
      val diagonal = g.tx != 0 && g.ty != 0
      // Cardinal corridors can shelter the whole front plus melee reach. At an offset
      // corner there may be no such recess; search the inner mouth for the best seal.
      val first = if (diagonal) 0 else Math.ceil(g.outer + body.extent(-g.nx, -g.ny) + 16).toInt
      val last = Math.ceil(g.inner + 128).toInt
      val step = if (diagonal) 2 else 4
      val cross = ((-g.width / 2 to g.width / 2 by step).map(_.toDouble) ++ Vector(0.0,
        -g.width / 2.0 + body.extent(-g.tx, -g.ty), g.width / 2.0 - body.extent(g.tx, g.ty))).distinct
      // Include exact obstacle contacts as well as the search lattice: a legal
      // inner-mouth seal can have only one pixel of clearance.
      val depths = ((first to last by step).map(_.toDouble) ++ Vector(
        Math.ceil(g.inner + body.extent(-g.nx, -g.ny)), Math.floor(g.inner - body.extent(g.nx, g.ny))))
        .filter(d => d >= first && d <= last).distinct
      depths.flatMap(d => cross.map(c => g.at(c, d))).distinct.filter(p => fits(body, p)).map { p =>
        val b = body.at(p)
        // Count possible Zergling contact positions around the perimeter. Fewer
        // contacts means less exposed surface, including at staggered corners.
        val contacts =
          (b.left to b.right by 8).flatMap(x => Vector(Pixel(x, b.top - ling.down), Pixel(x, b.bottom + ling.up))) ++
          (b.top to b.bottom by 8).flatMap(y => Vector(Pixel(b.left - ling.right, y), Pixel(b.right + ling.left, y)))
        Slot(p, b, ling.forbidden(b), contacts.count(fits(ling, _)), g.depth(p), Math.abs(g.across(p)))
      }.toVector.sortBy(s => (s.exposure, s.depth, s.across, s.pixel.x, s.pixel.y))
    })

    private def connectedTo(start: Box, blockers: Vector[Box]): Vector[Box] = {
      val reached = mutable.ArrayBuffer(start)
      val remaining = mutable.ArrayBuffer(blockers: _*)
      var next = 0
      while (next < reached.size) {
        val neighbors = remaining.filter(_.touches(reached(next))).toVector
        reached ++= neighbors
        remaining --= neighbors
        next += 1
      }
      reached.toVector
    }

    private var previousComposition = Vector.empty[(Boolean, Body)]
    private var plan = Vector.empty[(Boolean, Body, Pixel)]

    def assign(units: Vector[Defender]): Map[Int, Pixel] = {
      val composition = units.map(u => (u.probe, u.body)).sortBy { case (probe, b) => (probe, b.left, b.up, b.right, b.down) }
      if (composition != previousComposition) {
        previousComposition = composition
        plan = allocate(composition)
      }
      val remaining = mutable.ArrayBuffer(units: _*)
      plan.map { case (probe, body, pixel) =>
        val unit = remaining.filter(u => u.probe == probe && u.body == body).minBy(u => (u.pixel.pixelDistanceSquared(pixel), u.id))
        remaining -= unit
        unit.id -> pixel
      }.toMap
    }

    private def allocate(composition: Vector[(Boolean, Body)]): Vector[(Boolean, Body, Pixel)] = {
      val remaining = mutable.ArrayBuffer(composition: _*)
      val occupied = mutable.ArrayBuffer.empty[Box]
      val output = mutable.ArrayBuffer.empty[(Boolean, Body, Pixel)]
      val placed = mutable.Map.empty[Gap, Vector[(Boolean, Body, Slot)]]
      var progressed = true
      while (remaining.nonEmpty && progressed) {
        progressed = false
        gaps.foreach { g =>
          val previous = placed.getOrElse(g, Vector.empty)
          val blockers = previous.map(_._3.blocked)
          val fromA = connectedTo(ling.forbidden(g.first), blockers)
          val fromB = connectedTo(ling.forbidden(g.second), blockers)
          val sealedAlready = fromA.exists(_.touches(ling.forbidden(g.second)))
          val zealotBack = previous.filterNot(_._1).map { case (_, body, s) => s.depth + body.extent(g.nx, g.ny) }.reduceOption(_ max _)
          // One unit per gap per pass. Zealots consume the outer slots before Probes.
          val kinds = remaining.distinct
          val choice = kinds.iterator.flatMap { case (probe, body) =>
            def legal(s: Slot): Boolean =
              (! probe || zealotBack.forall(_ <= s.depth - body.extent(-g.nx, -g.ny))) &&
                ! occupied.exists(_.intersects(s.box))
            val options = candidates(g, body)
            // Close remaining lanes before reinforcing an already sealed passage.
            // This also puts a too-large defender at the inner mouth of a tiny gap.
            val slot = if (sealedAlready) options.find(legal) else (2 to 0 by -1).iterator.flatMap { sides =>
              options.find(s => legal(s) &&
                (if (fromA.exists(_.touches(s.blocked))) 1 else 0) + (if (fromB.exists(_.touches(s.blocked))) 1 else 0) == sides)
            }.take(1).toVector.headOption
            slot.map(s => (probe, body, s))
          }.take(1).toVector.headOption
          choice.foreach { case (probe, body, s) =>
            output += ((probe, body, s.pixel))
            occupied += s.box
            placed(g) = previous :+ ((probe, body, s))
            remaining -= ((probe, body))
            progressed = true
          }
        }
      }
      output.toVector
    }
  }
}
