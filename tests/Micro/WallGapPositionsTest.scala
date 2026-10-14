package Micro.Formation

import Mathematics.Points.Pixel
import Micro.Formation.WallGaps._

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

  check("minimum defenders counts Zealots needed to seal narrow, centered and wide gaps") {
    for ((width, expected) <- Vector((10, 0), (18, 1), (40, 1), (50, 1), (60, 2), (90, 6))) {
      val bs = Vector(Box(0, 100, 100, 220), Box(100 + width, 100, 200 + width, 220))
      val g = geometry(bs, Pixel(100 + width / 2, 500), None)
      assert(g.minimumDefenders.contains(expected), (width, g.minimumDefenders))
      val units = (1 to expected).map(defender(_)).toVector
      val result = g.assign(units)
      val region = Box(80, 80, 120 + width, 270)
      assert(! crosses(bs ++ result.values.map(zealot.at), region, _.y == 80, _.y == 269), (width, result))
      if (expected > 0) {
        val fewer = geometry(bs, Pixel(100 + width / 2, 500), None).assign(units.dropRight(1))
        assert(crosses(bs ++ fewer.values.map(zealot.at), region, _.y == 80, _.y == 269), (width, fewer))
      }
    }
  }
  check("minimum defenders seals all gaps with the actual allocation order") {
    val g = geometry()
    val count = g.minimumDefenders.get
    assert(count > g.gaps.size, count)
    val units = (1 to count).map(defender(_)).toVector
    val result = g.assign(units)
    val fewer = g.assign(units.dropRight(1))
    val region = Box(80, 80, 412, 270)
    assert(! crosses(buildings ++ result.values.map(zealot.at), region, _.y == 80, _.y == 269), result)
    assert(crosses(buildings ++ fewer.values.map(zealot.at), region, _.y == 80, _.y == 269), fewer)
  }
  check("querying minimum defenders does not alter existing assignments") {
    val g = geometry()
    val units = (1 to 7).map(defender(_)).toVector
    val before = g.assign(units)
    assert(g.minimumDefenders.nonEmpty)
    assert(g.assign(units) == before)
  }
  check("minimum defenders reports an entrance that Zealots cannot occupy") {
    val g = geometry(Vector(Box(0, 100, 100, 220), Box(118, 100, 218, 220)), Pixel(109, 500), None,
      (x, _) => x * 8 >= 96 && x * 8 < 120)
    assert(g.gaps.nonEmpty)
    assert(g.minimumDefenders.isEmpty)
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
  check("a lone defender centers a two-Zealot opening and recenters after a casualty") {
    for (width <- Vector(50, 60); reverse <- Vector(false, true)) {
      val bs = Vector(Box(0, 100, 100, 220), Box(100 + width, 100, 200 + width, 220))
      val g = geometry(bs, Pixel(100 + width / 2, if (reverse) -300 else 500), None)
      val unit = defender(1)
      val one = g.assign(Vector(unit))
      val box = zealot.at(one(1))
      assert(Math.abs((box.left - 100) - (100 + width - box.right)) <= 1, one)
      assert(g.fits(zealot, one(1)))
      val units = Vector(unit.copy(pixel = one(1)), defender(2))
      val two = g.assign(units)
      if (width == 60) assert(two(1).y == two(2).y && two(1).x != two(2).x, two)
      disjoint(two, units)
      assert(! crosses(bs ++ two.values.map(zealot.at), Box(80, 80, 120 + width, 250), _.y == 80, _.y == 249), two)
      val survivor = unit.copy(pixel = two(1))
      assert(g.assign(Vector(survivor)) == one)
      assert(g.assign(Vector(unit.copy(pixel = one(1)))) == one)
      if (width == 50) assert(! crosses(bs ++ one.values.map(zealot.at), Box(80, 80, 170, 250), _.y == 80, _.y == 249), one)
    }
  }
  check("single defenders center horizontally using Zealot capacity even for a Probe") {
    for (worker <- Vector(false, true); reverse <- Vector(false, true)) {
      val g = geometry(Vector(Box(100, 0, 220, 100), Box(100, 140, 220, 240)),
        Pixel(if (reverse) -300 else 500, 120), None)
      val unit = defender(1, worker)
      val result = g.assign(Vector(unit))
      val box = unit.body.at(result(1))
      assert(Math.abs((box.top - 100) - (140 - box.bottom)) <= 1, result)
      assert(g.fits(unit.body, result(1)))
    }
  }
  check("centering depends on each gap's occupancy rather than squad size") {
    val g = geometry()
    val units = Vector(defender(1), defender(2), defender(3))
    val result = g.assign(units)
    val wide = result.values.find(p => p.x > 220 && p.x < 280).get
    assert(Math.abs(wide.x - 250) <= 1, result)
    assert(g.assign(units.reverse) == result)
    disjoint(result, units)
  }
  check("a lone backup centers while paired frontline defenders keep their slots") {
    val g = geometry(Vector(Box(0, 100, 100, 220), Box(160, 100, 260, 220)), Pixel(130, 500), None)
    val units = (1 to 4).map(defender(_)).toVector
    val pair = g.assign(units.take(2)).values.toSet
    val three = g.assign(units.take(3))
    assert(pair.subsetOf(three.values.toSet), three)
    val backup = three.values.maxBy(_.y)
    assert(Math.abs(backup.x - 130) <= 1, three)
    val four = g.assign(units)
    assert(pair.subsetOf(four.values.toSet), four)
    assert(four.values.count(_.y == four.values.map(_.y).max) == 2, four)
    disjoint(three, units.take(3))
    disjoint(four, units)
    val mixed = g.assign(Vector(defender(1), defender(2, true)))
    val mixedBoxes = Vector(zealot.at(mixed(1)), probe.at(mixed(2)))
    val front = Vector(Box(0, 100, 100, 220), Box(160, 100, 260, 220))
    assert(! crosses(front ++ mixedBoxes, Box(80, 80, 180, 250), _.y == 80, _.y == 249), mixed)
    assert(zealot.at(mixed(1)).bottom <= probe.at(mixed(2)).top, mixed)
  }
  check("offset corners center lone defenders without reopening an existing seal") {
    for ((x, y) <- Vector((124, 248), (148, 264))) {
      val front = Vector(Box(0, 100, 100, 200), Box(x, y, x + 100, y + 100))
      val g = geometry(front, Pixel(-100, 500), None)
      assert(g.gaps.size == 1, g.gaps)
      val gap = g.gaps.head
      val one = g.assign(Vector(defender(1)))
      val box = zealot.at(one(1))
      if (x == 148) assert(Math.abs(gap.across(box.center)) <= 1, one)
      else assert(! crosses(front :+ box, Box(40, 140, 184, 308), p => gap.depth(p) <= -60, p => gap.depth(p) >= 60), one)
      assert(g.fits(zealot, one(1)))
      val two = g.assign(Vector(defender(1), defender(2)))
      disjoint(two, Vector(defender(1), defender(2)))
    }
  }
  check("centering never puts a lone defender inside a blocker") {
    val front = Vector(Box(0, 100, 100, 220), Box(190, 100, 290, 220))
    val blocker = Box(140, 100, 150, 220)
    val g = new Geometry(front.map(_ -> true) :+ (blocker -> false), Pixel(145, 500), None, (_, _) => true, ling)
    val result = g.assign(Vector(defender(1)))
    assert(result.size == 1 && g.fits(zealot, result(1)), result)
    assert(! zealot.at(result(1)).intersects(blocker), result)
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
    val g = geometry(terrain = (x, y) => x * 8 >= 96 && x * 8 < 144 && y * 8 >= 96 && y * 8 < 256)
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
  check("Circuit Breaker's three-building wall includes its narrow Pylon-Forge gap") {
    val front = Vector(Box(528, 2896, 625, 2969), Box(528, 3028, 561, 3061), Box(524, 3080, 597, 3125))
    val g = geometry(front, Pixel(288, 2992), Some(Pixel(527, 2991)))
    assert(g.gaps.map(_.width) == Vector(59, 19), g.gaps)
    val result = g.assign(Vector(defender(1), defender(2)))
    assert(result.size == 2, result)
    assert(result.values.exists(p => p.y > 2969 && p.y < 3028), result)
    assert(result.values.exists(p => p.y > 3061 && p.y < 3080), result)
    assert(! crosses(front ++ result.values.map(zealot.at), Box(480, 3061, 600, 3080), _.x == 599, _.x == 480), result)
  }
  check("a narrow gap gets one backup before reserves fill its side pocket") {
    val front = Vector(Box(528, 2896, 625, 2969), Box(528, 3028, 561, 3061), Box(524, 3080, 597, 3125))
    val cannons = Vector(Box(460, 3024, 501, 3057), Box(460, 3088, 501, 3121))
    val g = new Geometry(front.map(_ -> true) ++ cannons.map(_ -> false), Pixel(288, 2992), Some(Pixel(527, 2991)), (_, _) => true, ling)
    val plug = g.assign((1 to 3).map(defender(_)).toVector).values.maxBy(_.y)
    val backed = g.assign((1 to 5).map(defender(_)).toVector)
    assert(backed.values.exists(p => p.y == plug.y && p.x < plug.x && p.x >= plug.x - 24), backed)
    val units = (1 to 7).map(defender(_)).toVector
    val result = g.assign(units)
    // Keep the narrow-gap backups fixed; the lone wide-gap backup moves aside for its partner.
    assert(backed.values.filter(_.y > 3061).toSet.subsetOf(result.values.toSet), (backed, result))
    assert(result.values.exists(p => Math.abs(p.x - plug.x) <= 4 && p.y <= plug.y - 20 && p.y > 3028), result)
    assert(! result.values.exists(p => p.y == plug.y && p.x < plug.x - 24), result)
    assert(units.forall(u => g.fits(u.body, result(u.id))), result)
    disjoint(result, units)
    val settled = units.map(u => u.copy(pixel = result(u.id)))
    assert(g.assign(settled) == result)
    val casualties = Set(result.find(_._2 == plug).get._1, result.find(_._2 == Pixel(540, 2975)).get._1)
    val backup = result.find { case (_, p) => p.y == plug.y && p.x < plug.x }.get._1
    val flank = result.find { case (_, p) => Math.abs(p.x - plug.x) <= 4 && p.y <= plug.y - 20 && p.y > 3028 }.get._1
    val after = g.assign(settled.filterNot(u => casualties(u.id)))
    assert(after(backup) == result(backup), (result, after))
    assert(after(flank) == plug, (result, after))
  }
  check("reserves spread beside the front instead of queuing beyond a cannon") {
    val front = Vector(Box(3376, 3056, 3473, 3129), Box(3404, 2888, 3477, 2933), Box(3408, 2996, 3441, 3029))
    val cannons = Vector(Box(3500, 2896, 3541, 2929), Box(3500, 3024, 3541, 3057), Box(3500, 3088, 3541, 3121))
    val g = new Geometry(front.map(_ -> true) ++ cannons.map(_ -> false), Pixel(3808, 2992), Some(Pixel(3407, 2959)), (_, _) => true, ling)
    val first = g.assign((1 to 3).map(defender(_)).toVector).values.toVector
    val units = ((1 to 7).map(defender(_)) ++ (8 to 11).map(defender(_, true))).toVector
    val result = g.assign(units)
    assert(result.size == units.size, result)
    assert(first.forall(result.values.toSet), result)
    assert(result.values.forall(p => first.exists(_.pixelDistance(p) <= 96)), result)
    assert(units.forall(u => g.fits(u.body, result(u.id))), result)
    disjoint(result, units)
    val settled = units.map(u => u.copy(pixel = result(u.id)))
    assert(g.assign(settled) == result)
    val arriving = new Geometry(front.map(_ -> true) ++ cannons.map(_ -> false), Pixel(3808, 2992), Some(Pixel(3407, 2959)), (_, _) => true, ling)
    assert(arriving.assign(units) == result)
    val upper = result.find(_._2 == Pixel(3480, 3012)).get._1
    val lower = result.find(_._2 == Pixel(3480, 3036)).get._1
    val crowded = settled.map(u => if (u.id == upper) u.copy(pixel = Pixel(3479, 3013))
      else if (u.id == lower) u.copy(pixel = Pixel(3502, 3009)) else u)
    val cleared = arriving.assign(crowded)
    assert(cleared(upper) == result(lower) && cleared(lower) == result(upper), cleared)
    val almostSettled = settled.map(u => if (u.id == upper) u.copy(pixel = result(upper).add(-1, 1))
      else if (u.id == lower) u.copy(pixel = result(lower).add(3, 0)) else u)
    assert(g.assign(almostSettled) == result)
    val plugs = result.filter { case (_, p) => first.contains(p) }
    val casualty = plugs.maxBy(_._2.y)._1
    val survivors = settled.filterNot(_.id == casualty)
    val after = g.assign(survivors)
    assert(plugs.filterNot(_._1 == casualty).forall { case (id, p) => after(id) == p }, (result, after))
    val replacement = after.find(_._2 == result(casualty)).get._1
    assert(result(replacement).pixelDistance(result(casualty)) <= 40, (result, after))
    g.assign(settled)
    val casualties = Set(casualty, plugs.minBy(_._2.y)._1)
    val backup = result.find(_._2 == Pixel(3452, 3036)).get._1
    val flank = result.find(_._2 == Pixel(3456, 3016)).get._1
    val reduced = g.assign(settled.filterNot(u => casualties(u.id)))
    assert(reduced(backup) == result(casualty) && reduced(flank) == result(backup), reduced)
    assert(settled.filter(_.probe).forall(u => reduced(u.id) == result(u.id)), reduced)
    val onlyProbes = g.assign(settled.filter(_.probe))
    assert(onlyProbes.values.exists(_.x < 3452), onlyProbes)
  }
  check("a nearby Zealot replaces a dead front defender") {
    val g = geometry(Vector(Box(0, 100, 100, 220), Box(118, 100, 218, 220)), Pixel(109, 500), None)
    val units = ((1 to 5).map(defender(_)) ++ (6 to 7).map(defender(_, true))).toVector
    val front = g.assign(Vector(defender(1)))(1)
    val before = g.assign(units)
    val victim = before.find(_._2 == front).get._1
    val survivors = units.filterNot(_.id == victim).map(u => u.copy(pixel = before(u.id)))
    val after = g.assign(survivors)
    val replacement = after.find(_._2 == front).get._1
    assert(before(replacement).pixelDistance(front) <= 32, (before, after))
    assert(! survivors.find(_.id == replacement).get.probe)
    assert(after.size == survivors.size)
    disjoint(after, survivors)
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
