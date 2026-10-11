package Information.Geography

import Information.Geography.Calculations.MergeMainZones

object MergeMainZonesTest {
  def main(args: Array[String]): Unit = {
    def merge(
      chokes: Vector[(Int, Int, Double)],
      bases: Set[Int] = Set(0, 3),
      mains: Set[Int] = Set(0),
      heights: Map[Int, Int] = Map.empty): Map[Int, Int] = {
      val regions = (chokes.flatMap(c => Vector(c._1, c._2)) ++ bases ++ mains).distinct
      val result = MergeMainZones(regions, mains, bases, (r: Int) => heights.getOrElse(r, 1), chokes)
      assert(result == MergeMainZones(regions.reverse, mains, bases, (r: Int) => heights.getOrElse(r, 1), chokes.reverse),
        "Merging must be independent of region/choke iteration order")
      mains.foreach(r => assert(result(r) == r, "Never merge another main"))
      result
    }

    assert(merge(Vector((0, 1, 0d)))(1) == 0, "Mineral-blocked Andromeda back pocket")
    assert(merge(Vector((0, 1, 350d), (1, 3, 400d)))(1) == 0, "Katrina plateau passage")
    assert(merge(Vector((0, 1, 480d), (1, 3, 80d)))(1) == 0, "Polaris main forecourt")
    assert(merge(Vector((0, 1, 80d), (1, 3, 400d)))(1) == 1, "Preserve a genuine main choke")
    assert(merge(Vector((0, 1, 0d), (1, 3, 400d)))(1) == 1, "Blocked shortcut is not an enclosed pocket")
    assert(merge(Vector((0, 1, 400d)), heights = Map(1 -> 0))(1) == 1, "Preserve terrain-level boundaries")
    assert(merge(Vector((0, 1, 400d), (1, 2, 40d), (1, 3, 40d)))(1) == 1, "Preserve branching areas")
    assert(merge(Vector((0, 1, 400d), (1, 3, 80d)), bases = Set(0, 1, 3))(1) == 1, "Preserve expansions")
    assert(merge(Vector((0, 1, 400d), (1, 3, 80d)), mains = Set(0, 3))(1) == 1, "Preserve shared approaches")
    assert(merge(Vector((0, 1, 400d), (1, 2, 400d), (2, 3, 80d)))(2) == 0, "Contract a chain of subdivisions")
    assert(merge(Vector((0, 1, 400d), (1, 2, 80d), (2, 3, 80d)))(2) == 2, "Stop a chain at its real choke")
    assert(merge(Vector((1, 2, 400d)))(1) == 1, "Leave disconnected components alone")
    assert(merge(Vector.empty)(0) == 0, "Leave isolated mains alone")
    assert(merge(Vector((0, 1, 150d), (0, 1, 150d), (1, 3, 80d)))(1) == 1,
      "Parallel narrow chokes are not one broad connection")
    assert(merge(Vector((0, 1, 448d)), bases = Set(0, 1))(1) == 0, "Broad same-height backyard natural")
    assert(merge(Vector((0, 1, 80d)), bases = Set(0, 1))(1) == 1, "Preserve a natural behind a real choke")
    assert(merge(Vector((0, 1, 0d)), bases = Set(0, 1))(1) == 1, "Preserve a mineral-blocked expansion")
    assert(merge(Vector((0, 1, 448d)), bases = Set(0, 1), heights = Map(1 -> 0))(1) == 1,
      "Preserve a backyard on a different terrain level")
    assert(merge(Vector((0, 1, 448d)), bases = Set(0, 1), mains = Set(0, 1))(1) == 1,
      "Never absorb another starting location")
    assert(merge(Vector((0, 1, 400d), (1, 2, 448d)), bases = Set(0, 2))(2) == 0,
      "Backyard natural after a base-free subdivision is contracted")
    println("MergeMainZones: 20 cases passed (including reversed graph order).")
    if (args.nonEmpty) testMaps(args(0))
  }

  private def testMaps(filename: String): Unit = {
    var name = ""
    var regions = Vector.empty[Int]
    var mains = Set.empty[Int]
    var bases = Set.empty[Int]
    var heights = Map.empty[Int, Int]
    var expected = Map.empty[Int, Int]
    var chokes = Vector.empty[(Int, Int, Double)]
    var maps = 0
    def check(): Unit = if (name.nonEmpty) {
      assert(MergeMainZones(regions, mains, bases, heights.apply, chokes) == expected, name)
      assert(MergeMainZones(regions.reverse, mains, bases, heights.apply, chokes.reverse) == expected, name + " reversed")
      maps += 1
    }
    val source = scala.io.Source.fromFile(filename)
    try source.getLines().filterNot(_.startsWith("#")).foreach(line => {
      val row = line.split("\t")
      row(0) match {
        case "MAP" =>
          check()
          name = row(1)
          regions = Vector.empty
          mains = Set.empty
          bases = Set.empty
          heights = Map.empty
          expected = Map.empty
          chokes = Vector.empty
        case "REGION" =>
          val region = row(1).toInt
          regions :+= region
          heights += region -> row(2).toInt
          if (row(3).toBoolean) mains += region
          if (row(4).toBoolean) bases += region
          expected += region -> row(5).toInt
        case "CHOKE" => chokes :+= ((row(1).toInt, row(2).toInt, row(3).toDouble))
      }
    }) finally source.close()
    check()
    assert(maps == 46, "Expected the entire 45-map AIIDE pool plus Andromeda")
    println(f"MergeMainZones: $maps real map graphs passed (including reversed graph order).")
  }
}
