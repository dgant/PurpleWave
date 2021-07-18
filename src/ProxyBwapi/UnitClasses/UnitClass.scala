package ProxyBwapi.UnitClasses

import Lifecycle.With
import Mathematics.Points.{Point, Tile, TileRectangle}
import Mathematics.Maff
import Micro.Heuristics.MicroValue
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.{Neutral, Protoss, Terran, Zerg}
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.{Race, UnitType}

import scala.collection.mutable.ListBuffer

final case class UnitClass(base: UnitType) extends UnitClassProxy(base) with UnitMatcher {

  //////////////
  // Geometry //
  //////////////

  lazy val dimensionMin: Int = Math.min(width, height)
  lazy val dimensionMax: Int = Math.max(width, height)
  lazy val area: Int = dimensionMin * dimensionMax
  lazy val occupancy: Int = if (isFlyer) 0 else (dimensionMin * dimensionMax) / (Math.max(1, tileWidth) * Math.max(1, tileHeight))
  lazy val sqrtArea: Int = Math.sqrt(area).toInt
  lazy val radialHypotenuse: Double = Math.sqrt(width.toDouble * width.toDouble + height.toDouble * height.toDouble) / 2.0
  lazy val perimeter: Int = 2 * width + 2 * height

  lazy val topLeft: Point = Point(dimensionLeft, dimensionUp)
  lazy val topRight: Point = Point(dimensionRight, dimensionUp)
  lazy val bottomLeft: Point = Point(dimensionLeft, dimensionDown)
  lazy val bottomRight: Point = Point(dimensionRight, dimensionDown)
  lazy val corners: Vector[Point] = Vector(topLeft, topRight, bottomLeft, bottomRight)

  //////////////
  // Movement //
  //////////////

  lazy val accelerationFrames: Int = if (acceleration > 1) Math.ceil(256.0 * topSpeed / acceleration).toInt else 1
  lazy val framesToTurn180: Int = framesToTurn(Math.PI)

  def framesToTurn(radians: Double): Int = Math.abs(Maff.nanToZero(Math.ceil(127.0 * Maff.normalizePiToPi(radians) / Math.PI / turnRadius))).toInt

  lazy val needsToTurnToShoot: Boolean = ! Vector(Terran.Goliath, Terran.SiegeTankUnsieged, Protoss.Dragoon).contains(this)
  lazy val framesToTurnShootTurnAccelerate: Int = stopFrames + accelerationFrames + (if (needsToTurnToShoot) 2 * framesToTurn180 else 0) + With.latency.latencyFrames
  lazy val hasMomentum: Boolean = isFlyer || floats

  ////////////
  // Combat //
  ////////////

  lazy val ranged: Boolean = canAttack && pixelRangeMax > 32 * 2
  lazy val melee: Boolean = canAttack && ! ranged

  lazy val suicides: Boolean = Array(
    Terran.SpiderMine,
    Protoss.Scarab,
    Zerg.InfestedTerran,
    Zerg.Scourge)
    .contains(this)

  lazy val floats: Boolean = Array(
    Terran.SCV,
    Terran.Vulture,
    Terran.SpiderMine,
    Protoss.Probe,
    Protoss.Archon,
    Protoss.DarkArchon,
    Zerg.Drone)
    .contains(this)

  lazy val triggersSpiderMines: Boolean = ! floats && ! isFlyer && ! isBuilding

  lazy val splashesFriendly: Boolean = Array(
    Terran.SiegeTankSieged,
    Terran.SpiderMine,
    Zerg.InfestedTerran)
    .contains(this)

  lazy val dealsRadialSplashDamage: Boolean = Array(
    Terran.SiegeTankSieged,
    Terran.Valkyrie,
    Terran.SpiderMine,
    Protoss.Archon,
    Protoss.Corsair,
    Protoss.Reaver,
    Protoss.Scarab,
    Zerg.Devourer, // Well, SORT of
    Zerg.InfestedTerran)
    .contains(this)

  // Subjective measure of splashiness
  lazy val splashFactor: Double = if (dealsRadialSplashDamage || this == Zerg.Lurker)
    2.5
  else if (this == Zerg.Mutalisk)
    1.25
  else
    1.0

  lazy val maxTotalHealth: Int = maxHitPoints + maxShields

  // Via http://www.starcraftai.com/wiki/Regeneration
  lazy val repairHpPerFrame: Double = if (isMechanical) 0.9 * maxHitPoints / Math.max(1.0, buildFrames) else 0.0

  lazy val effectiveAirDamage: Int =
    if (this == Terran.Bunker) Terran.Marine.airDamageRaw
    else if (this == Protoss.Carrier) Protoss.Interceptor.airDamageRaw
    else airDamageRaw

  lazy val effectiveGroundDamage: Int =
    if (this == Terran.Bunker) Terran.Marine.groundDamageRaw
    else if (this == Protoss.Carrier) Protoss.Interceptor.groundDamageRaw
    else if (this == Protoss.Reaver) Protoss.Scarab.groundDamageRaw
    else groundDamageRaw

  private def cooldownZeroBecomesInfinity(cooldown: Int): Int = if (cooldown == 0) Int.MaxValue else cooldown

  lazy val airDamageCooldown: Int =
    if (this == Terran.Bunker) Terran.Marine.airDamageCooldown
    else if (this == Protoss.Carrier) Protoss.Interceptor.airDamageCooldown
    else if (this == Protoss.Interceptor) 45 // Reasonable approximation; Interceptors mostly are on "cooldown" due to movement
    else cooldownZeroBecomesInfinity(airDamageCooldownRaw)

  lazy val groundDamageCooldown: Int =
    if (this == Terran.Bunker) Terran.Marine.groundDamageCooldown
    else if (this == Protoss.Carrier) Protoss.Interceptor.airDamageCooldown
    else if (this == Protoss.Interceptor) 45 // Reasonable approximation; Interceptors mostly are on "cooldown" due to movement
    else if (this == Protoss.Reaver) 60
    else cooldownZeroBecomesInfinity(groundDamageCooldownRaw)

  lazy val attacksGround: Boolean = effectiveGroundDamage > 0
  lazy val attacksAir: Boolean = effectiveAirDamage > 0
  lazy val canAttack: Boolean = attacksGround || attacksAir

  def canAttack(enemy: UnitClass): Boolean = if (enemy.isFlyer) attacksAir else attacksGround
  def canAttack(enemy: UnitInfo): Boolean = if (enemy.flying) attacksAir else attacksGround

  lazy val dealsDamage: Boolean = rawCanAttack || isSpellcaster || this == Terran.Bunker || this == Protoss.Carrier || this == Protoss.Reaver || this == Zerg.Lurker

  lazy val pixelRangeGround: Double =
    if (this == Terran.Bunker) Terran.Marine.pixelRangeGround + 32.0
    else if (this == Protoss.Carrier) 32.0 * 8.0
    else if (this == Protoss.Reaver) 32.0 * 8.0
    else groundRangeRaw
  lazy val pixelRangeAir: Double =
    if (this == Terran.Bunker) Terran.Marine.pixelRangeGround + 32.0
    else if (this == Protoss.Carrier) 32.0 * 8.0
    else airRangeRaw

  lazy val pixelRangeMax: Double = Math.max(pixelRangeGround, pixelRangeAir)

  lazy val effectiveRangePixels: Double =
    if (isDetector) sightRangePixels
    else if (this == Terran.Battlecruiser)  32.0 * 10.0
    else if (this == Terran.Medic)          32.0 * 2.0
    else if (this == Protoss.Arbiter)       32.0 * 9.0
    else if (this == Protoss.DarkArchon)    32.0 * 10.0
    else if (this == Protoss.HighTemplar)   32.0 * 9.0
    else if (this == Zerg.Defiler)          32.0 * 9.0
    else if (this == Zerg.Queen)            32.0 * 9.0
    else pixelRangeMax

  lazy val tileArea: TileRectangle = TileRectangle(Tile(0, 0), tileSize)
  lazy val orderable: Boolean = ! isSpell && ! Vector(Protoss.Interceptor, Protoss.Scarab, Terran.SpiderMine).contains(this)
  lazy val isMinerals: Boolean = isMineralField
  lazy val isGas: Boolean = Vector(Neutral.Geyser, Terran.Refinery, Protoss.Assimilator, Zerg.Extractor).contains(this)
  lazy val isResource: Boolean = isMinerals || isGas
  lazy val isTownHall: Boolean = isResourceDepot
  lazy val isStaticDefense: Boolean = (isBuilding && canAttack || this == Terran.Bunker || this == Protoss.ShieldBattery) && this != Terran.SiegeTankSieged
  lazy val isTransport: Boolean = spaceProvided > 0 && isFlyer && this != Protoss.Carrier
  lazy val unaffectedByDarkSwarm: Boolean = Vector(
    Terran.SiegeTankSieged,
    Terran.Firebat,
    Protoss.Zealot,
    Protoss.DarkTemplar,
    Protoss.Reaver,
    Protoss.Scarab,
    Protoss.Archon,
    Zerg.Zergling,
    Zerg.Lurker,
    Zerg.Ultralisk
  ).contains(this)

  ///////////
  // Macro //
  ///////////

  lazy val unitsTrained: Vector[UnitClass] = UnitClasses.all.filter(_.whatBuilds._1 == this).toVector

  lazy val trainsUnits: Boolean = UnitClasses.all.exists(unit => unit.whatBuilds._1 == this)
  lazy val trainsAirUnits: Boolean = UnitClasses.all.exists(unit => unit.whatBuilds._1 == this && unit.isFlyer)
  lazy val trainsGroundUnits: Boolean = UnitClasses.all.exists(unit => unit.whatBuilds._1 == this && !unit.isFlyer && !unit.isBuilding)

  lazy val isProtoss: Boolean = race == Race.Protoss
  lazy val isTerran: Boolean = race == Race.Terran
  lazy val isZerg: Boolean = race == Race.Zerg

  lazy val framesToFinishCompletion: Int = if (isProtoss) 75 else if (isZerg) 12 else 0

  lazy val buildTechEnabling: Option[Tech] = if (requiredTechRaw == Techs.None || requiredTechRaw == Techs.Unknown) None else Some(requiredTechRaw)
  lazy val buildUnitsEnabling: Vector[UnitClass] = _buildUnitsEnabling
  lazy val buildUnitsBorrowed: Vector[UnitClass] = _buildUnitsBorrowed
  lazy val buildUnitsSpent: Vector[UnitClass] = _buildUnitsSpent

  private def _buildUnitsEnabling: Vector[UnitClass] = {
    lazy val output = new ListBuffer[UnitClass]

    // Probes (Protoss buildings)
    addBuildUnitIf(output, isProtoss && isBuilding, Protoss.Probe)

    // Pylon (Protoss buildings except Nexus/Pylon/Assimilator)
    addBuildUnitIf(output, requiresPsi, Protoss.Pylon)

    // Obvious prerequisites
    addBuildUnitIf(output, Terran.Firebat, Terran.Academy)
    addBuildUnitIf(output, Terran.Medic, Terran.Academy)
    addBuildUnitIf(output, Terran.Ghost, Terran.Academy)
    addBuildUnitIf(output, Terran.Ghost, Terran.CovertOps)
    addBuildUnitIf(output, Terran.Goliath, Terran.Armory)
    addBuildUnitIf(output, Terran.SiegeTankUnsieged, Terran.MachineShop)
    addBuildUnitIf(output, Terran.Dropship, Terran.ControlTower)
    addBuildUnitIf(output, Terran.Valkyrie, Terran.Armory)
    addBuildUnitIf(output, Terran.Valkyrie, Terran.ControlTower)
    addBuildUnitIf(output, Terran.ScienceVessel, Terran.ScienceFacility)
    addBuildUnitIf(output, Terran.ScienceVessel, Terran.ControlTower)
    addBuildUnitIf(output, Terran.Battlecruiser, Terran.PhysicsLab)
    addBuildUnitIf(output, Terran.Battlecruiser, Terran.ControlTower)
    addBuildUnitIf(output, Terran.MissileTurret, Terran.EngineeringBay)
    addBuildUnitIf(output, Terran.Bunker, Terran.Barracks)
    addBuildUnitIf(output, Terran.Factory, Terran.Barracks)
    addBuildUnitIf(output, Terran.Academy, Terran.Barracks)
    addBuildUnitIf(output, Terran.Comsat, Terran.Academy)
    addBuildUnitIf(output, Terran.Armory, Terran.Factory)
    addBuildUnitIf(output, Terran.Starport, Terran.Factory)
    addBuildUnitIf(output, Terran.ScienceFacility, Terran.Factory)
    addBuildUnitIf(output, Terran.ScienceFacility, Terran.Starport)
    addBuildUnitIf(output, Terran.NuclearSilo, Terran.CovertOps)
    addBuildUnitIf(output, Protoss.Dragoon, Protoss.CyberneticsCore)
    addBuildUnitIf(output, Protoss.HighTemplar, Protoss.TemplarArchives)
    addBuildUnitIf(output, Protoss.DarkTemplar, Protoss.TemplarArchives)
    addBuildUnitIf(output, Protoss.Arbiter, Protoss.TemplarArchives)
    addBuildUnitIf(output, Protoss.Reaver, Protoss.RoboticsSupportBay)
    addBuildUnitIf(output, Protoss.Observer, Protoss.Observatory)
    addBuildUnitIf(output, Protoss.Carrier, Protoss.FleetBeacon)
    addBuildUnitIf(output, Protoss.Arbiter, Protoss.ArbiterTribunal)
    addBuildUnitIf(output, Protoss.PhotonCannon, Protoss.Forge)
    addBuildUnitIf(output, Protoss.ShieldBattery, Protoss.Gateway)
    addBuildUnitIf(output, Protoss.CyberneticsCore, Protoss.Gateway)
    addBuildUnitIf(output, Protoss.RoboticsFacility, Protoss.CyberneticsCore)
    addBuildUnitIf(output, Protoss.Stargate, Protoss.CyberneticsCore)
    addBuildUnitIf(output, Protoss.CitadelOfAdun, Protoss.CyberneticsCore)
    addBuildUnitIf(output, Protoss.TemplarArchives, Protoss.CitadelOfAdun)
    addBuildUnitIf(output, Protoss.RoboticsSupportBay, Protoss.RoboticsFacility)
    addBuildUnitIf(output, Protoss.Observatory, Protoss.RoboticsFacility)
    addBuildUnitIf(output, Protoss.FleetBeacon, Protoss.Stargate)
    addBuildUnitIf(output, Protoss.ArbiterTribunal, Protoss.Stargate)
    addBuildUnitIf(output, Protoss.ArbiterTribunal, Protoss.TemplarArchives)
    addBuildUnitIf(output, Zerg.Zergling, Zerg.SpawningPool)
    addBuildUnitIf(output, Zerg.Hydralisk, Zerg.HydraliskDen)
    addBuildUnitIf(output, Zerg.Mutalisk, Zerg.Spire)
    addBuildUnitIf(output, Zerg.Scourge, Zerg.Spire)
    addBuildUnitIf(output, Zerg.Queen, Zerg.QueensNest)
    addBuildUnitIf(output, Zerg.Ultralisk, Zerg.UltraliskCavern)
    addBuildUnitIf(output, Zerg.Defiler, Zerg.DefilerMound)
    addBuildUnitIf(output, Zerg.Guardian, Zerg.GreaterSpire)
    addBuildUnitIf(output, Zerg.Devourer, Zerg.GreaterSpire)
    addBuildUnitIf(output, Zerg.SporeColony, Zerg.EvolutionChamber)
    addBuildUnitIf(output, Zerg.SunkenColony, Zerg.SpawningPool)
    addBuildUnitIf(output, Zerg.HydraliskDen, Zerg.SpawningPool)
    addBuildUnitIf(output, Zerg.Lair, Zerg.SpawningPool)
    addBuildUnitIf(output, Zerg.Spire, Zerg.Lair)
    addBuildUnitIf(output, Zerg.QueensNest, Zerg.Lair)
    addBuildUnitIf(output, Zerg.Hive, Zerg.QueensNest)
    addBuildUnitIf(output, Zerg.UltraliskCavern, Zerg.Hive)
    addBuildUnitIf(output, Zerg.DefilerMound, Zerg.Hive)
    addBuildUnitIf(output, Zerg.GreaterSpire, Zerg.Hive)
    addBuildUnitIf(output, Zerg.NydusCanal, Zerg.Hive)

    output.toVector
  }

  private def _buildUnitsBorrowed: Vector[UnitClass] = {

    lazy val output = new ListBuffer[UnitClass]

    // All Terran units that train from buildings
    addBuildUnitIf(output, Terran.SCV, Terran.CommandCenter)
    addBuildUnitIf(output, Terran.Marine, Terran.Barracks)
    addBuildUnitIf(output, Terran.Firebat, Terran.Barracks)
    addBuildUnitIf(output, Terran.Medic, Terran.Barracks)
    addBuildUnitIf(output, Terran.Ghost, Terran.Barracks)
    addBuildUnitIf(output, Terran.Vulture, Terran.Factory)
    addBuildUnitIf(output, Terran.Goliath, Terran.Factory)
    addBuildUnitIf(output, Terran.SiegeTankUnsieged, Terran.Factory)
    addBuildUnitIf(output, Terran.Dropship, Terran.Starport)
    addBuildUnitIf(output, Terran.Wraith, Terran.Starport)
    addBuildUnitIf(output, Terran.Valkyrie, Terran.Starport)
    addBuildUnitIf(output, Terran.ScienceVessel, Terran.Starport)
    addBuildUnitIf(output, Terran.Battlecruiser, Terran.Starport)
    addBuildUnitIf(output, Terran.NuclearMissile, Terran.NuclearSilo)

    // SCV (for all Terran building except add-ons)
    addBuildUnitIf(output, isBuilding && race == Race.Terran && !isAddon, Terran.SCV)

    // Factory (for Machine Shop)
    // Starport (for Control Tower)
    // Science Facility (for Covert Ops/Physics Lab)
    // Command Center (for Comsat/Nuke Silo)
    addBuildUnitIf(output, isAddon, whatBuilds._1)

    // All Protoss units that train from buildings
    addBuildUnitIf(output, Protoss.Probe, Protoss.Nexus)
    addBuildUnitIf(output, Protoss.Zealot, Protoss.Gateway)
    addBuildUnitIf(output, Protoss.Dragoon, Protoss.Gateway)
    addBuildUnitIf(output, Protoss.HighTemplar, Protoss.Gateway)
    addBuildUnitIf(output, Protoss.DarkTemplar, Protoss.Gateway)
    addBuildUnitIf(output, Protoss.Shuttle, Protoss.RoboticsFacility)
    addBuildUnitIf(output, Protoss.Reaver, Protoss.RoboticsFacility)
    addBuildUnitIf(output, Protoss.Observer, Protoss.RoboticsFacility)
    addBuildUnitIf(output, Protoss.Scout, Protoss.Stargate)
    addBuildUnitIf(output, Protoss.Corsair, Protoss.Stargate)
    addBuildUnitIf(output, Protoss.Carrier, Protoss.Stargate)
    addBuildUnitIf(output, Protoss.Arbiter, Protoss.Stargate)

    // Pop quiz: What's the only Zerg unit that trains from a building?
    addBuildUnitIf(output, Zerg.InfestedTerran, Zerg.InfestedCommandCenter)
    addBuildUnitIf(output, Zerg.InfestedCommandCenter, Zerg.Queen)

    output.toVector
  }

  private def _buildUnitsSpent: Vector[UnitClass] = {

    lazy val output = new ListBuffer[UnitClass]

    // Archons
    addBuildUnitIf(output, Protoss.Archon, Protoss.HighTemplar)
    addBuildUnitIf(output, Protoss.Archon, Protoss.HighTemplar)
    addBuildUnitIf(output, Protoss.DarkArchon, Protoss.DarkTemplar)
    addBuildUnitIf(output, Protoss.DarkArchon, Protoss.DarkTemplar)

    // Larva (All Zerg non-building units except Lurker/Guardian/Devourer)
    addBuildUnitIf(output, Zerg.Drone, Zerg.Larva)
    addBuildUnitIf(output, Zerg.Overlord, Zerg.Larva)
    addBuildUnitIf(output, Zerg.Zergling, Zerg.Larva)
    addBuildUnitIf(output, Zerg.Hydralisk, Zerg.Larva)
    addBuildUnitIf(output, Zerg.Mutalisk, Zerg.Larva)
    addBuildUnitIf(output, Zerg.Ultralisk, Zerg.Larva)
    addBuildUnitIf(output, Zerg.Queen, Zerg.Larva)
    addBuildUnitIf(output, Zerg.Defiler, Zerg.Larva)

    // Drone (Most Zerg buildings)
    addBuildUnitIf(output, Zerg.Hatchery, Zerg.Drone)
    addBuildUnitIf(output, Zerg.Extractor, Zerg.Drone)
    addBuildUnitIf(output, Zerg.CreepColony, Zerg.Drone)
    addBuildUnitIf(output, Zerg.SpawningPool, Zerg.Drone)
    addBuildUnitIf(output, Zerg.EvolutionChamber, Zerg.Drone)
    addBuildUnitIf(output, Zerg.HydraliskDen, Zerg.Drone)
    addBuildUnitIf(output, Zerg.Spire, Zerg.Drone)
    addBuildUnitIf(output, Zerg.QueensNest, Zerg.Drone)
    addBuildUnitIf(output, Zerg.UltraliskCavern, Zerg.Drone)
    addBuildUnitIf(output, Zerg.DefilerMound, Zerg.Drone)
    addBuildUnitIf(output, Zerg.NydusCanal, Zerg.Drone)

    // Zerg morphs
    addBuildUnitIf(output, Zerg.LurkerEgg, Zerg.Hydralisk)
    addBuildUnitIf(output, Zerg.Lurker, Zerg.Hydralisk)
    addBuildUnitIf(output, Zerg.Cocoon, Zerg.Mutalisk)
    addBuildUnitIf(output, Zerg.Guardian, Zerg.Mutalisk)
    addBuildUnitIf(output, Zerg.Devourer, Zerg.Mutalisk)
    addBuildUnitIf(output, Zerg.Lair, Zerg.Hatchery)
    addBuildUnitIf(output, Zerg.Hive, Zerg.Lair)
    addBuildUnitIf(output, Zerg.SunkenColony, Zerg.CreepColony)
    addBuildUnitIf(output, Zerg.SporeColony, Zerg.CreepColony)

    output.toVector
  }

  private def addBuildUnitIf(classes: ListBuffer[UnitClass], predicate: Boolean, thenAddThatClass: UnitClass) {
    if (predicate) classes.append(thenAddThatClass)
  }

  private def addBuildUnitIf(classes: ListBuffer[UnitClass], ifThisClass: UnitClass, thenAddThatClass: UnitClass) {
    addBuildUnitIf(classes, this == ifThisClass, thenAddThatClass)
  }

  lazy val mineralValue: Int = if (this == Zerg.Larva) 0 else mineralPrice + buildUnitsSpent.map(_.mineralValue).sum
  lazy val gasValue: Int = if (this == Zerg.Larva) 0 else gasPrice + buildUnitsSpent.map(_.gasValue).sum
  lazy val copiesProduced: Int = if (isTwoUnitsInOneEgg) 2 else 1
  lazy val subjectiveValue: Double =
    if (isSpell) 0 else if (this == Zerg.LurkerEgg) Zerg.Lurker.subjectiveValue else if (this == Zerg.Cocoon) Zerg.Guardian.subjectiveValue else
      (
        (
          mineralValue
            + MicroValue.gasToMineralsRatio * gasValue.toInt
            + 6.25 * supplyRequired // 100 minerals buys 16 supply; 100 / 16 = 6.25
            + (if (isZerg) 25.0 / copiesProduced else 0.0) // Larva value
          )
          * (if (isWorker) 1.3 else 1.0)
          * (if (whatBuilds._1 == Terran.Factory) 1.2 else 1.0)
          * (if (this == Protoss.Carrier) 2.0 else 1.0)
          / (if (this == Protoss.Interceptor) 4.0 else 1.0)
          / copiesProduced
        )

  //////////////////////
  // Micro frame data //
  //////////////////////

  // Largely from https://docs.google.com/spreadsheets/d/1bsvPvFil-kpvEUfSG74U3E5PLSTC02JxSkiR8QdLMuw/edit#gid=0
  //
  // According to professor jaj22:
  // Well, the stop frames are the main guide (to how many frames of movement are lost by attacking)
  // But that won't strictly tell you how many frames you lose.
  //
  lazy val stopFrames: Int =
    if (this == Terran.SCV) 2
    else if (this == Terran.Marine) 8
    else if (this == Terran.Firebat) 8
    else if (this == Terran.Ghost) 3
    else if (this == Terran.Vulture) 2
    else if (this == Terran.Goliath) 1
    else if (this == Terran.SiegeTankUnsieged) 1
    else if (this == Terran.SiegeTankSieged) 1
    else if (this == Terran.Wraith) 2
    else if (this == Terran.Battlecruiser) 2
    else if (this == Terran.Valkyrie) 40
    else if (this == Protoss.Probe) 2
    else if (this == Protoss.Zealot) 7
    else if (this == Protoss.Dragoon) 2
    else if (this == Protoss.DarkTemplar) 9
    else if (this == Protoss.Archon) 15
    else if (this == Protoss.Reaver) 1
    else if (this == Protoss.Scout) 2
    else if (this == Protoss.Corsair) 8
    else if (this == Protoss.Arbiter) 4
    else if (this == Protoss.PhotonCannon) 7 // I counted this myself but not sure I got it right
    else if (this == Zerg.Drone) 2
    else if (this == Zerg.Zergling) 4
    else if (this == Zerg.Hydralisk) 3
    else if (this == Zerg.Lurker) 2
    else if (this == Zerg.Ultralisk) 14
    else if (this == Zerg.Mutalisk) 2
    else if (this == Zerg.Devourer) 9
    else 2 // Arbitrary default.

  // These numbers are taken from Dave Churchill's table,
  // but the Dragoon number at least doesn't seem to correlate to the required delay to prevent attack cancelling.
  lazy val minStop: Int =
    if (this == Protoss.Dragoon) 5
    else if (this == Zerg.Devourer) 7
    else if (this == Protoss.Carrier) 48
    else 0

  lazy val attackAnimationFrames: Int =
    if (this == Terran.SCV) 2
    else if (this == Terran.Marine) 8
    else if (this == Terran.Firebat) 8
    else if (this == Terran.Ghost) 4
    else if (this == Terran.Goliath) 1
    else if (this == Terran.SiegeTankUnsieged) 1
    else if (this == Terran.SiegeTankSieged) 1
    else if (this == Protoss.Zealot) 8
    else if (this == Protoss.Dragoon) 9
    else if (this == Protoss.DarkTemplar) 9
    else if (this == Protoss.Reaver) 1
    else if (this == Protoss.Corsair) 8
    else if (this == Protoss.Arbiter) 5
    else if (this == Zerg.Zergling) 5
    else if (this == Zerg.Hydralisk) 3
    else if (this == Zerg.Ultralisk) 15
    else 0

  //////////////////
  // Capabilities //
  //////////////////

  // These largely exist for performance reasons;
  // We want to avoid calling isTHING methods for units where it will always be true

  lazy val canFly: Boolean = isFlyer || isFlyingBuilding
  lazy val canBurrow: Boolean = Vector(Terran.SpiderMine, Zerg.Drone, Zerg.Zergling, Zerg.Hydralisk, Zerg.Lurker, Zerg.Defiler).contains(this)
  lazy val canStim: Boolean = this == Terran.Marine || this == Terran.Firebat
  lazy val canSiege: Boolean = this == Terran.SiegeTankUnsieged || this == Terran.SiegeTankSieged
  lazy val canBeIrradiated: Boolean = Players.all.exists(_.isUnknownOrTerran) && !isBuilding
  lazy val canBeIrradiateBurned: Boolean = Players.all.exists(_.isUnknownOrTerran) && !isBuilding && isOrganic && this != Zerg.LurkerEgg && this != Zerg.Egg
  lazy val canBeStormed: Boolean = Players.all.exists(_.isUnknownOrProtoss) && ! isBuilding && ! isInvincible
  lazy val canBeLockedDown: Boolean = Players.all.exists(_.isUnknownOrTerran) && !isBuilding && isMechanical
  lazy val canBeMaelstrommed: Boolean = Players.all.exists(_.isUnknownOrProtoss) && !isBuilding && isOrganic
  lazy val canBeEnsnared: Boolean = Players.all.exists(_.isUnknownOrZerg) && !isBuilding
  lazy val canBeStasised: Boolean = Players.all.exists(_.isUnknownOrProtoss) && !isBuilding
  lazy val canLoadUnits: Boolean = Vector(Terran.Bunker, Terran.Dropship, Protoss.Shuttle, Zerg.Overlord).contains(this)
  lazy val canBeTransported: Boolean = ! isBuilding && spaceRequired <= 8 // BWAPI gives 255 for unloadable units
  lazy val spells: Array[Tech] =
    if (this == Terran.Battlecruiser) Array(Terran.Yamato)
    else if (this == Terran.Ghost) Array(Terran.GhostCloak, Terran.Lockdown, Terran.NuclearStrike)
    else if (this == Terran.Medic) Array(Terran.Healing, Terran.OpticalFlare, Terran.Restoration)
    else if (this == Terran.ScienceVessel) Array(Terran.DefensiveMatrix, Terran.EMP, Terran.Irradiate)
    else if (this == Terran.Wraith) Array(Terran.WraithCloak)
    else if (this == Protoss.Arbiter) Array(Protoss.Recall, Protoss.Stasis)
    else if (this == Protoss.DarkArchon) Array(Protoss.Feedback, Protoss.Maelstrom, Protoss.MindControl)
    else if (this == Protoss.HighTemplar) Array(Protoss.Hallucination, Protoss.PsionicStorm)
    else if (this == Zerg.Defiler) Array(Zerg.Consume, Zerg.DarkSwarm, Zerg.Plague)
    else if (this == Zerg.Queen) Array(Zerg.Ensnare, Zerg.InfestCommandCenter, Zerg.Parasite, Zerg.SpawnBroodlings)
    else Array()
  lazy val castsSpells: Boolean = spells.nonEmpty
  lazy val attacksOrCastsOrDetectsOrTransports: Boolean = canAttack || castsSpells || isDetector || isTransport || this == Zerg.LurkerEgg

  //////////////////////////////
  // Performance optimization //
  //////////////////////////////

  lazy val abuseAllowed: Boolean = Vector(
    Terran.Marine,
    Terran.Vulture,
    Terran.SiegeTankUnsieged,
    Terran.Goliath,
    Terran.Wraith,
    Protoss.Dragoon,
    Zerg.Hydralisk).contains(this)

  lazy val fallbackAllowed: Boolean = Vector(
    Terran.Marine,
    Terran.Firebat,
    Terran.Vulture,
    Terran.SiegeTankUnsieged,
    Terran.Goliath,
    Protoss.Archon,
    Protoss.Dragoon,
    Protoss.Reaver,
    Protoss.Scout,
    Zerg.Hydralisk).contains(this)

  /////////////////
  // Convenience //
  /////////////////

  // UnitMatcher
  @inline def apply(unit: UnitInfo): Boolean = unit.unitClass == this

  override val toString: String = asString
    .replace("Terran_", "")
    .replace("Zerg_", "")
    .replace("Protoss_", "")
    .replace("Neutral_", "")
    .replace("Resource_", "")
    .replace("Critter_", "")
    .replace("Special_", "")
    .replace("Vulture_Spider", "Spider")
    .replaceAll("_", " ")

  override val hashCode: Int = toString.hashCode
}