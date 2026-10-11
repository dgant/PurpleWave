package Information.Geography

import Debugging.Logger
import Information.GameSense.GameSensor
import Information.Geography.Calculations.UpdateBase
import Information.Geography.Types.{Base, Geo, Metro, Zone}
import Information.Scouting.Scouting
import Lifecycle.With
import Lifecycle.Configure.Configuration
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, UnitInfo}
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.Time.{Forever, Minutes}

object SharedMainZoneTest extends App {
  // Supply observations without starting BWAPI; exercise the actual ownership updater and Geo predicate.
  private val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
  unsafeField.setAccessible(true)
  private val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
  def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
  def fields(obj: AnyRef): Iterator[java.lang.reflect.Field] =
    Iterator.iterate[Class[_]](obj.getClass)(_.getSuperclass).takeWhile(_ != null).flatMap(_.getDeclaredFields)
  def set(obj: AnyRef, name: String, value: Any): Unit = {
    val field = fields(obj).find(f => f.getName == name || f.getName.endsWith("$$" + name)).get
    field.setAccessible(true)
    field.set(obj, value.asInstanceOf[AnyRef])
  }
  def freezeLazyFields(obj: AnyRef): Unit = fields(obj).filter(_.getName.startsWith("bitmap$")).foreach(field => {
    field.setAccessible(true)
    if (field.getType == java.lang.Long.TYPE) field.setLong(obj, -1L)
    else if (field.getType == java.lang.Integer.TYPE) field.setInt(obj, -1)
    else if (field.getType == java.lang.Boolean.TYPE) field.setBoolean(obj, true)
  })
  def player(id: Int, neutral: Boolean = false, enemy: Boolean = false): PlayerInfo = {
    val raw = allocate(classOf[bwapi.Player])
    set(raw, "id", id)
    val p = allocate(classOf[PlayerInfo])
    set(p, "bwapiPlayer", raw)
    freezeLazyFields(p)
    set(p, "name", id.toString)
    set(p, "isNeutral", neutral)
    set(p, "isEnemy", enemy)
    set(p, "townHallClass", Protoss.Nexus)
    set(p, "raceCurrentCache", new Cache(() => bwapi.Race.Protoss))
    p
  }

  With.frame = Minutes(5)()
  With.mapTileWidth = 64
  With.mapTileHeight = 64
  With.mapTileArea = 4096
  With.configuration = new Configuration
  With.configuration.logstd = false
  With.configuration.debugging = false
  With.logger = new Logger
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
  With.neutral = player(0, neutral = true)
  With.self = player(1)
  With.enemies = Vector(player(2, enemy = true))
  With.sense = allocate(classOf[GameSensor])
  set(With.sense, "_enemySecretMinerals", 1000d)
  With.scouting = allocate(classOf[Scouting])
  set(With.scouting, "_firstExpansionFrameUs", Forever())
  set(With.scouting, "_firstExpansionFrameEnemy", Forever())
  With.geography = allocate(classOf[Geography])
  set(With.geography, "ourMainCache", new Cache[Base](() => null))
  set(With.geography, "ourNaturalCache", new Cache[Base](() => null))

  def zone(): Zone = {
    val z = allocate(classOf[Zone])
    freezeLazyFields(z)
    set(z, "name", "fixture zone")
    set(z, "metro", None)
    set(z, "units", new UnorderedBuffer[UnitInfo])
    z
  }
  def base(z: Zone, main: Boolean): Base = {
    val b = allocate(classOf[Base])
    freezeLazyFields(b)
    set(b, "name", if (main) "main" else "natural")
    set(b, "zone", z)
    set(b, "zones", Vector(z))
    set(b, "bases", Vector(b))
    set(b, "isMain", main)
    set(b, "heart", Tile(8, 8))
    set(b, "arrow", "")
    set(b, "adjective", "")
    b.natural = None
    b.naturalOf = None
    b.townHall = None
    b.owner = With.neutral
    b.lastFrameScoutedByUs = 1
    b.allTimeOwners = Set.empty
    val metro = allocate(classOf[Metro])
    freezeLazyFields(metro)
    set(metro, "bases", Vector(b))
    set(b, "metro", metro)
    b
  }
  def pair(shared: Boolean): (Base, Base) = {
    val main = base(zone(), main = true)
    val natural = base(if (shared) main.zone else zone(), main = false)
    main.natural = Some(natural)
    main.owner = With.enemy
    natural.naturalOf = Some(main)
    set(With.scouting, "_firstEnemyMain", Some(main))
    (main, natural)
  }
  def building(): ForeignUnitInfo = {
    val u = allocate(classOf[ForeignUnitInfo])
    set(u, "_player", With.enemy)
    set(u, "_unitClass", Protoss.Gateway)
    set(u, "_complete", true)
    set(u, "_pixel", Pixel(256, 256))
    u
  }
  private val updateOwner = UpdateBase.getClass.getDeclaredMethods.find(_.getName == "updateOwner").get
  updateOwner.setAccessible(true)
  def update(b: Base): Unit = updateOwner.invoke(UpdateBase, b)
  private var passed = 0
  def check(name: String)(test: => Unit): Unit = { test; passed += 1; println("PASS " + name) }

  check("main buildings do not imply an expansion inside the same zone") {
    val (main, natural) = pair(shared = true)
    main.zone.units.add(building())
    update(natural)
    assert(natural.owner == With.neutral)
  }
  check("a building still implies an unscouted natural in a separate zone") {
    val (_, natural) = pair(shared = false)
    natural.zone.units.add(building())
    update(natural)
    assert(natural.owner == With.enemy)
  }
  check("an observed town hall still establishes ownership inside a shared zone") {
    val (_, natural) = pair(shared = true)
    val hall = building()
    set(hall, "_unitClass", Protoss.Nexus)
    natural.townHall = Some(hall)
    update(natural)
    assert(natural.owner == With.enemy)
  }
  check("an empty separate natural stays neutral") {
    val (_, natural) = pair(shared = false)
    update(natural)
    assert(natural.owner == With.neutral)
  }
  check("recently scouted naturals retain the inference delay") {
    val (_, natural) = pair(shared = false)
    natural.zone.units.add(building())
    natural.lastFrameScoutedByUs = With.frame - 1
    update(natural)
    assert(natural.owner == With.neutral)
  }

  // Leave isBackyard inherited so these exercise its production logic without frozen lazy values.
  class GeographyView(val bases: Vector[Base], main: Boolean) extends Geo {
    val tiles: Set[Tile] = Set.empty
    val zones: Vector[Zone] = bases.map(_.zone).distinct
    def heart: Tile = Tile(8, 8)
    def owner: PlayerInfo = With.neutral
    def units: Seq[UnitInfo] = Seq.empty
    override lazy val isMain: Boolean = main
  }
  check("only the natural is a backyard when it shares the main zone") {
    val (main, natural) = pair(shared = true)
    assert(new GeographyView(Vector(natural), main = false).isBackyard)
    assert(!new GeographyView(Vector(main), main = true).isBackyard)
    assert(!new GeographyView(Vector(main, natural), main = true).isBackyard)
  }
  check("separate exposed naturals and unrelated bases are not backyards") {
    val (_, natural) = pair(shared = false)
    assert(!new GeographyView(Vector(natural), main = false).isBackyard)
    assert(!new GeographyView(Vector(base(zone(), main = false)), main = false).isBackyard)
  }
  check("the existing separate-zone backyard rule still applies") {
    val (_, natural) = pair(shared = false)
    val metro = allocate(classOf[Metro])
    freezeLazyFields(metro)
    set(metro, "exits", Vector.empty)
    set(metro, "rushDistanceMin", 100d)
    set(natural.zone, "metro", Some(metro))
    set(natural.zone, "rushDistanceMin", 200d)
    assert(new GeographyView(Vector(natural), main = false).isBackyard)
  }
  println(s"SharedMainZoneTest: $passed cases passed.")
}
