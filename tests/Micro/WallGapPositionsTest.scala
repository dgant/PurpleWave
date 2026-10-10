package Micro.Formation

import Mathematics.Points.Pixel
import Micro.Formation.WallGapPositions._

object WallGapPositionsTest extends App {
  val zealot = Body(11, 19, 12, 20)
  val probe = Body(11, 11, 12, 12)
  val ling = Body(8, 4, 9, 5)
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
    assert(result(1).x > 100 && result(1).x < 140)
    assert(result(2).x > 220 && result(2).x < 280)
    assert(result(3).x > 360 && result(3).x < 392)
  }
  check("fill every gap once before adding a second layer") {
    val g = geometry()
    val units = (1 to 6).map(defender(_)).toVector
    val result = g.assign(units)
    assert(result.size == 6)
    Vector(1, 3).foreach(i => assert(result(i + 3).y >= result(i).y + 39, result))
    assert(result(5).y == result(2).y && Math.abs(result(5).x - result(2).x) >= 23, result)
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
    Vector(1, 3).foreach(i => assert(result(i + 3).y < result(i).y))
    assert(zealot.at(result(1)).bottom <= 204)
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
  check("terrain forms the other side of a wall gap") {
    val g = geometry(Vector(Box(100, 100, 200, 220)), Pixel(150, 500), None, (x, _) => x * 8 >= 64)
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
    assert(zealot.at(result(1)).top == 220)
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
