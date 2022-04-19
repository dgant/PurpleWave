package Information.Geography.NeoGeo

/**
 * A Continent is a fully-walkable area.
 * Every tile adjacent to a Continent is unwalkable or off the map entirely.
 */
abstract class NeoContinent {
  def walkable: Seq[Int]
}
