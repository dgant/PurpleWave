package Micro.Formation

import Mathematics.Points.Pixel
import Micro.Formation.WallGapPositions._

object WallGapPositionsTest extends App {
  def body(kind: bwapi.UnitType): Body = Body(kind.dimensionLeft, kind.dimensionUp, kind.dimensionRight + 1, kind.dimensionDown + 1)
  val zealot = body(bwapi.UnitType.Protoss_Zealot)
  val probe = body(bwapi.UnitType.Protoss_Probe)
  val ling = body(bwapi.UnitType.Zerg_Zergling)
  var passed = 0
  def check(name: String)(test: => Unit): Unit = { test; passed += 1; println("PASS " + name) }
  def defender(id: Int, worker: Boolean = false): Defender = Defender(id, if (worker) probe else zealot, worker, Pixel(180, 300))
  val buildings = Vector(Box(0, 100, 100, 220), Box(140, 100, 220, 220), Box(280, 100, 360, 220), Box(392, 100, 480, 220))
  def geometry(bs: Vector[Box] = buildings, inside: Pixel = Pixel(240, 500), door: Option[Pixel] = Some(Pixel(120, 160)), terrain: (Int, Int) => Boolean = (_, _) => true): Geometry =
    new Geometry(bs.map(_ -> true), inside, door, terrain, ling)
  def disjoint(result: Map[Int, Pixel], units: Vector[Defender]): Unit = {
    val bodies = units.filter(u => result.contains(u.id)).map(u => u.body.at(result(u.id)))
    assert(bodies.indices.forall(i => bodies.indices.forall(j => i == j || ! bodies(i).intersects(bodies(j)))))
  }

  check("walking path before wider gaps; remaining gaps widest first") {
    val g = geometry()
    assert(g.gaps.map(_.width) == Vector(40, 60, 32), g.gaps)
    val result = g.assign(Vector(defender(1), defender(2), defender(3)))
    assert(g.assign(Vector(defender(1))).values.forall(p => p.x > 100 && p.x < 140))
    assert(g.assign(Vector(defender(1), defender(2))).values.exists(p => p.x > 220 && p.x < 280))
    assert(result.values.exists(p => p.x > 360 && p.x < 392))
  }
  check("fill every gap once before adding a second layer") {
    val g = geometry()
    val units = (1 to 6).map(defender(_)).toVector
    val result = g.assign(units)
    assert(result.size == 6)
    Vector((100, 140), (360, 392)).foreach { case (left, right) =>
      val ys = result.values.filter(p => p.x > left && p.x < right).map(_.y).toVector.sorted
      assert(ys.size == 2 && ys.last - ys.head >= zealot.up + zealot.down, result)
    }
    val wide = result.values.filter(p => p.x > 220 && p.x < 280).toVector.sortBy(_.x)
    assert(wide.size == 2 && wide.head.y == wide.last.y && wide.last.x - wide.head.x >= 23, result)
    disjoint(result, units)
  }
  check("Zealots always take the outside and Probes the inside, independent of input order") {
    val g = geometry()
    val units = Vector(defender(1, true), defender(2, true), defender(3, true), defender(4), defender(5), defender(6))
    val result = g.assign(units)
    (1 to 3).foreach(i => assert(result(i).y > result(i + 3).y))
    assert(result == g.assign(units.reverse))
    disjoint(result, units)
  }
  check("the first rank seals the Forge-Gateway throat before the cannon aisles") {
    val front = Vector(Box(1036, 3016, 1109, 3061), Box(1168, 2992, 1265, 3065))
    val cannons = Vector(Box(1036, 3088, 1077, 3121), Box(1164, 3088, 1205, 3121), Box(1228, 3088, 1269, 3121))
    val g = new Geometry(front.map(_ -> true) ++ cannons.map(_ -> false), Pixel(1152, 3312), Some(Pixel(1135, 3023)), (_, _) => true, ling)
    val units = Vector(defender(1), defender(2)).map(_.copy(pixel = Pixel(1216, 3072)))
    val result = g.assign(units)
    assert(result.size == 2)
    result.values.map(zealot.at).foreach(b => assert(b.left >= 1109 && b.right <= 1168 && b.top >= 3016 && b.bottom <= 3061, result))
    assert(! crosses(front ++ result.values.map(zealot.at), Box(1109, 2990, 1168, 3080), _.y == 2990, _.y == 3079))
    disjoint(result, units)
  }
  check("seven Zealots and four Probes form compact ranks in the Eclipse entrance") {
    val front = Vector(Box(1036, 3016, 1109, 3061), Box(1168, 2992, 1265, 3065))
    val cannons = Vector(Box(1036, 3088, 1077, 3121), Box(1164, 3088, 1205, 3121), Box(1228, 3088, 1269, 3121))
    val g = new Geometry(front.map(_ -> true) ++ cannons.map(_ -> false), Pixel(1152, 3312), Some(Pixel(1135, 3023)), (_, _) => true, ling)
    val units = ((1 to 7).map(defender(_)) ++ (8 to 11).map(defender(_, true))).toVector
    val result = g.assign(units)
    assert(result.size == units.size, result)
    val outer = units.filterNot(_.probe).map(u => u.body.at(result(u.id)))
    val inner = units.filter(_.probe).map(u => u.body.at(result(u.id)))
    assert(outer.map(_.top).distinct.sorted.sliding(2).forall(rank => rank.size < 2 || rank(1) - rank(0) <= zealot.up + zealot.down + 4), result)
    assert(inner.map(_.top).min >= outer.map(_.bottom).max, result)
    disjoint(result, units)
  }
  check("two Zealots seal the horizontal Eclipse throat") {
    val front = Vector(Box(3376, 1072, 3473, 1145), Box(3404, 968, 3477, 1013))
    val g = geometry(front, Pixel(3808, 1040), Some(Pixel(3471, 1039)))
    val units = Vector(defender(1), defender(2)).map(_.copy(pixel = Pixel(3380, 1150)))
    val result = g.assign(units)
    assert(result.size == 2)
    assert(! crosses(front ++ result.values.map(zealot.at), Box(3380, 1013, 3500, 1072), _.x == 3380, _.x == 3499), result)
  }
  check("front defenders are recessed by their full body and melee reach") {
    val g = geometry()
    val result = g.assign(Vector(defender(1)))
    val b = zealot.at(result(1))
    assert(b.top >= 116 && b.bottom <= 220, b)
    assert(g.fits(zealot, result(1)))
  }
  check("reverse wall orientation reverses layer order") {
    val g = geometry(inside = Pixel(240, -300))
    val units = (1 to 6).map(defender(_)).toVector
    val result = g.assign(units)
    Vector((100, 140), (360, 392)).foreach { case (left, right) =>
      val ys = result.values.filter(p => p.x > left && p.x < right).map(_.y).toVector.sorted
      assert(ys.size == 2 && ys.last - ys.head >= zealot.up + zealot.down, result)
    }
    assert(result.values.forall(p => zealot.at(p).bottom <= 204))
    disjoint(result, units)
  }
  check("horizontal wall uses unit height when determining fit") {
    val rotated = buildings.map(b => Box(b.top, b.left, b.bottom, b.right))
    val g = geometry(rotated, Pixel(500, 240), Some(Pixel(160, 120)))
    val units = (1 to 6).map(defender(_)).toVector
    val result = g.assign(units)
    assert(result.size == 6)
    assert(result.values.forall(p => g.fits(zealot, p)))
    disjoint(result, units)
  }
  check("terrain can bound the intentional walking path") {
    val g = geometry(Vector(Box(100, 100, 200, 220)), Pixel(150, 500), Some(Pixel(82, 160)), (x, _) => x * 8 >= 64)
    assert(g.gaps.exists(_.width == 36), g.gaps)
    val result = g.assign(Vector(defender(1)))
    assert(result(1).x >= 75 && result(1).x <= 88)
  }
  check("gaps too narrow for Zerglings need no defenders") {
    val g = geometry(Vector(Box(0, 100, 100, 220), Box(110, 100, 210, 220)))
    assert(g.gaps.isEmpty)
    assert(g.assign(Vector(defender(1))).isEmpty)
  }
  check("a gap narrower than the defender is plugged from its inner mouth") {
    val g = geometry(Vector(Box(0, 100, 100, 220), Box(118, 100, 218, 220)), Pixel(109, 500), None)
    val result = g.assign(Vector(defender(1)))
    assert(result.size == 1)
    assert(zealot.at(result(1)).top == 221)
  }
  check("third buildings and nonwalkable pockets cannot receive assignments") {
    val g = geometry(Vector(Box(0, 100, 100, 220), Box(140, 100, 240, 220), Box(100, 140, 140, 180)))
    assert(g.assign(Vector(defender(1))).isEmpty)
    assert(geometry(terrain = (_, _) => false).assign(Vector(defender(1))).isEmpty)
  }
  check("empty squad and exhausted gap capacity terminate safely") {
    val g = geometry()
    assert(g.assign(Vector.empty).isEmpty)
    val units = (1 to 100).map(defender(_)).toVector
    val result = g.assign(units)
    assert(result.size < 100)
    disjoint(result, units)
  }
  check("offset building corners are plugged without collisions") {
    val g = geometry(Vector(Box(0, 100, 100, 200), Box(124, 224, 224, 324)), Pixel(-100, 500), None)
    assert(g.gaps.size == 1, g.gaps)
    val result = g.assign(Vector(defender(1)))
    assert(result.size == 1)
    val expanded = ling.forbidden(zealot.at(result(1)))
    assert(expanded.touches(ling.forbidden(g.gaps.head.first)), result)
    assert(expanded.touches(ling.forbidden(g.gaps.head.second)), result)
  }
  check("reserved walking path has priority without a Wall object") {
    val g = new Geometry(buildings.map(_ -> true), Pixel(240, 500), None, (_, _) => true, ling,
      p => p.x >= 100 && p.x < 140)
    assert(g.gaps.head.width == 40)
  }
  check("a pylon on the front line splits the entrance into two gaps") {
    val g = geometry(Vector(Box(0, 100, 100, 220), Box(140, 140, 180, 180), Box(220, 100, 320, 220)), Pixel(160, 500), None)
    assert(g.gaps.map(_.width) == Vector(40, 40), g.gaps)
    val result = g.assign(Vector(defender(1), defender(2)))
    assert(result.values.exists(p => p.x > 100 && p.x < 140), result)
    assert(result.values.exists(p => p.x > 180 && p.x < 220), result)
  }
  check("spaces behind the wall between cannon rows are not wall entrances") {
    val front = Vector(Box(0, 100, 100, 220), Box(140, 100, 240, 220))
    val rear = Box(264, 244, 344, 324)
    val g = new Geometry(front.map(_ -> true) :+ (rear -> false), Pixel(100, 500), Some(Pixel(120, 160)), (_, _) => true, ling)
    assert(g.gaps.size == 1, g.gaps)
  }
  check("reinforcements take the outside while existing defenders move deeper") {
    val g = geometry(Vector(Box(0, 100, 100, 220), Box(140, 100, 240, 220)), Pixel(120, 500), None)
    val first = g.assign(Vector(defender(1)))(1)
    val result = g.assign(Vector(defender(1).copy(pixel = first), defender(2).copy(pixel = Pixel(120, 60))))
    assert(result(1).y > result(2).y, result)
    assert(g.assign(Vector(defender(1).copy(pixel = result(1)), defender(2).copy(pixel = result(2)))) == result)
    val fromInside = geometry(Vector(Box(0, 100, 100, 220), Box(140, 100, 240, 220)), Pixel(120, 500), None)
      .assign(Vector(defender(1).copy(pixel = first), defender(2).copy(pixel = Pixel(120, 300))))
    assert(fromInside(2).y > fromInside(1).y, fromInside)
  }
  check("a front defender leads to a farther gap instead of blocking newcomers") {
    val g = geometry(door = Some(Pixel(376, 160)))
    val first = g.assign(Vector(defender(1)))(1)
    val result = g.assign(Vector(defender(1).copy(pixel = first), defender(2).copy(pixel = Pixel(500, 300))))
    assert(result(1).x < first.x, result)
    assert(result(2).x == first.x, result)
  }
  check("collision-free pockets behind an impassable neck are not assigned") {
    val front = Vector(Box(0, 100, 100, 220), Box(140, 100, 240, 220))
    val neck = Vector(Box(100, 180, 111, 196), Box(129, 180, 140, 196))
    val g = new Geometry(front.map(_ -> true) ++ neck.map(_ -> false), Pixel(120, 500), None,
      (x, _) => x * 8 >= 96 && x * 8 < 144, ling)
    val result = g.assign(Vector(defender(1)))
    assert(result.nonEmpty)
    assert(zealot.at(result(1)).top > 196, result)
  }
  check("incidental cliff nooks are not gaps between the front wall buildings") {
    val g = geometry(Vector(Box(148, 152, 221, 197), Box(280, 128, 377, 201)), Pixel(250, 500), Some(Pixel(250, 170)),
      (x, y) => x * 8 >= (if (y * 8 < 120) 96 else 80))
    assert(g.gaps.size == 1 && g.gaps.head.doorway, g.gaps)
  }
  check("reinforcements approaching around a building join the inner end") {
    val g = geometry(Vector(Box(0, 100, 100, 220), Box(140, 100, 240, 220)), Pixel(120, 500), None)
    val units = (1 to 7).map(defender(_)).toVector
    val first = g.assign(units)
    val settled = units.map(u => u.copy(pixel = first(u.id)))
    val result = g.assign(settled :+ defender(8).copy(pixel = Pixel(300, 221)))
    assert(result(8).y == result.values.map(_.y).max, result)
  }
  check("units keep their gap while ranks adapt to their physical order") {
    val g = geometry()
    val units = (1 to 4).map(defender(_)).toVector
    val first = g.assign(units)
    val reversed = units.zip(first.values.toVector.reverse).map { case (u, p) => u.copy(pixel = p) }
    val after = g.assign(reversed)
    def gapOf(p: Pixel): Gap = g.gaps.minBy(gap => Math.abs(gap.across(p)))
    first.foreach { case (id, p) => assert(gapOf(after(id)) == gapOf(p)) }
  }
  check("movement leaves clearance at building and terrain edges") {
    val g = geometry(Vector(Box(100, 100, 220, 220), Box(260, 100, 380, 220)), Pixel(240, 500), None,
      (x, _) => x >= 4)
    assert(! g.fits(zealot, Pixel(100 - zealot.right, 160)))
    assert(g.fits(zealot, Pixel(99 - zealot.right, 160)))
    assert(! g.fits(zealot, Pixel(32 + zealot.left, 160)))
    assert(g.fits(zealot, Pixel(33 + zealot.left, 160)))
  }
  check("approach waypoints route around collision edges before reaching the exact slot") {
    val g = geometry(Vector(Box(100, 0, 220, 100), Box(100, 140, 220, 240)), Pixel(500, 120), None)
    var unit = defender(1).copy(pixel = Pixel(80, 155))
    val target = g.assign(Vector(unit))(1)
    assert(g.waypoint(unit.copy(pixel = Pixel(-1000, -1000)), target) == target)
    assert(g.waypoint(unit, target) != target)
    for (_ <- 0 until 64 if unit.pixel != target) {
      val approach = g.waypoint(unit, target)
      assert(g.fits(zealot, approach))
      assert(unit.pixel.pixelDistance(approach) <= 64)
      val steps = Math.ceil(unit.pixel.pixelDistance(approach)).toInt
      for (i <- 0 to steps) assert(g.fits(zealot, Pixel(
        Math.round(unit.pixel.x + (approach.x - unit.pixel.x).toDouble * i / Math.max(1, steps)).toInt,
        Math.round(unit.pixel.y + (approach.y - unit.pixel.y).toDouble * i / Math.max(1, steps)).toInt)))
      unit = unit.copy(pixel = approach)
    }
    assert(unit.pixel == target, (unit.pixel, target))
    assert(g.waypoint(unit, target) == target)
  }
  // Independent pixel flood fill verifies that positions actually stop a Zergling,
  // including the one-pixel routes that coarse tile walkability cannot represent.
  def crosses(obstacles: Vector[Box], region: Box, outside: Pixel => Boolean, inside: Pixel => Boolean): Boolean = {
    val queue = scala.collection.mutable.Queue.empty[Pixel]
    val seen = scala.collection.mutable.Set.empty[Pixel]
    def open(p: Pixel): Boolean = ! obstacles.exists(_.intersects(ling.at(p)))
    for (x <- region.left until region.right; y <- region.top until region.bottom) {
      val p = Pixel(x, y)
      if (outside(p) && open(p)) { queue.enqueue(p); seen += p }
    }
    while (queue.nonEmpty) {
      val p = queue.dequeue()
      if (inside(p)) return true
      Vector(p.add(-1, 0), p.add(1, 0), p.add(0, -1), p.add(0, 1)).foreach { next =>
        if (region.contains(next) && ! seen(next) && open(next)) { seen += next; queue.enqueue(next) }
      }
    }
    false
  }
  check("pixel pathfinding confirms a full-tile path is sealed") {
    val bs = Vector(Box(0, 100, 100, 220), Box(140, 100, 240, 220))
    val g = geometry(bs, Pixel(120, 500), None)
    val region = Box(80, 80, 160, 250)
    val outside: Pixel => Boolean = _.y == 80
    val inside: Pixel => Boolean = _.y == 249
    assert(crosses(bs, region, outside, inside))
    val result = g.assign(Vector(defender(1)))
    assert(! crosses(bs ++ result.values.map(zealot.at), region, outside, inside))
  }
  check("pixel pathfinding confirms wide gaps are sealed by side-by-side reinforcements") {
    val bs = Vector(Box(0, 100, 100, 220), Box(160, 100, 260, 220))
    val g = geometry(bs, Pixel(130, 500), None)
    val region = Box(80, 80, 180, 250)
    val outside: Pixel => Boolean = _.y == 80
    val inside: Pixel => Boolean = _.y == 249
    val one = g.assign(Vector(defender(1)))
    assert(crosses(bs ++ one.values.map(zealot.at), region, outside, inside))
    val two = g.assign(Vector(defender(1), defender(2)))
    assert(! crosses(bs ++ two.values.map(zealot.at), region, outside, inside))
  }
  check("pixel pathfinding confirms diagonal corner and inner-mouth seals") {
    val bs = Vector(Box(0, 100, 100, 200), Box(124, 224, 224, 324))
    val g = geometry(bs, Pixel(-100, 500), None)
    val region = Box(40, 140, 184, 284)
    val gap = g.gaps.head
    val outside: Pixel => Boolean = p => gap.depth(p) <= -60
    val inside: Pixel => Boolean = p => gap.depth(p) >= 60
    assert(crosses(bs, region, outside, inside))
    val result = g.assign(Vector(defender(1)))
    assert(! crosses(bs ++ result.values.map(zealot.at), region, outside, inside))
    val narrow = Vector(Box(0, 100, 100, 220), Box(118, 100, 218, 220))
    val plug = geometry(narrow, Pixel(109, 500), None).assign(Vector(defender(1)))
    assert(! crosses(narrow ++ plug.values.map(zealot.at), Box(80, 80, 140, 270), _.y == 80, _.y == 269))
  }
  check("defenders already in their slots are not swapped") {
    val g = geometry()
    val units = (1 to 6).map(defender(_)).toVector
    val result = g.assign(units)
    assert(g.assign(units.map(u => u.copy(pixel = result(u.id)))) == result)
  }
  println(s"$passed wall gap tests passed")
}
