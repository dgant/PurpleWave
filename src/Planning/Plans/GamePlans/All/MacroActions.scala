package Planning.Plans.GamePlans.All

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Requests._
import Placement.Access.PlaceLabels.PlaceLabel
import Placement.Access.{PlaceLabels, PlacementQuery}
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Macro.Automatic.Rounding.Rounding
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{BuildOnce, BuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Scouting.{ScoutAt, ScoutOn}
import ProxyBwapi.Buildable
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import Utilities.UnitFilters.UnitFilter

trait MacroActions {
  def status(text: String): Unit = With.blackboard.status.set(With.blackboard.status() :+ text)

  def attack(): Unit = With.blackboard.wantToAttack.set(true)
  def harass(): Unit = With.blackboard.wantToHarass.set(true)
  def allIn(): Unit = { With.blackboard.yoloing.set(true); attack() }

  def scoutOn(unitMatcher: UnitFilter, scoutCount: Int = 1, quantity: Int = 1): Unit = {
    new ScoutOn(unitMatcher, scoutCount = scoutCount, quantity = quantity).update()
  }
  def scoutAt(minimumSupply: Int, maxScouts: Int = 1): Unit = {
    new ScoutAt(minimumSupply = minimumSupply, maxScouts = maxScouts).update()
  }
  def aggression(value: Double): Unit = {
    With.blackboard.aggressionRatio.set(value)
  }
  def gasWorkerFloor(value: Int): Unit = With.blackboard.gasWorkerFloor.set(value)
  def gasWorkerCeiling(value: Int): Unit = With.blackboard.gasWorkerCeiling.set(value)
  def gasLimitFloor(value: Int): Unit = With.blackboard.gasLimitFloor.set(value)
  def gasLimitCeiling(value: Int): Unit = With.blackboard.gasLimitCeiling.set(value)

  def get(item: RequestBuildable): Unit = With.scheduler.request(NoPlan(), item)
  def get(units: UnitClass*): Unit = units.foreach(get(1, _))
  def get(unit: UnitClass, placementQuery: PlacementQuery): Unit = get(1, unit, placementQuery)
  def get(unit: UnitClass, base: Base): Unit = get(1, unit, base)
  def get(quantity: Int, unit: UnitClass): Unit = get(RequestUnit(unit, quantity))
  def get(quantity: Int, unit: UnitClass, placementQuery: PlacementQuery): Unit = get(RequestUnit(unit, quantity, placementQueryArg = Some(placementQuery)))
  def get(quantity: Int, unit: UnitClass, base: Base): Unit = get(1, unit, new PlacementQuery(unit).requireBase(base))
  def get(quantity: Int, unit: UnitClass, base: Base, labels: PlaceLabel*): Unit = get(1, unit, new PlacementQuery(unit).requireBase(base).requireLabelYes(labels: _*))
  def get(upgrade: Upgrade): Unit = get(RequestUpgrade(upgrade))
  def get(upgrade: Upgrade, level: Int): Unit = get(Get(level, upgrade))
  def get(tech: Tech): Unit = get(RequestTech(tech))
  def once(units: UnitClass*): Unit = units.foreach(once(1, _))
  def once(upgrade: Upgrade): Unit = get(upgrade)
  def once(upgrade: Upgrade, level: Int): Unit = get(upgrade, level)
  def once(tech: Tech): Unit = get(tech)
  def once(quantity: Int, unit: UnitClass): Unit = BuildOnce(NoPlan(), Get(quantity, unit))

  def buildOrder(items: RequestBuildable*): Unit = {
    new BuildOrder(items: _*).update()
  }
  def requireEssentials(): Unit = {
    new RequireEssentials().update()
  }
  def pump(unitClass: UnitClass, maximumTotal: Int = Int.MaxValue, maximumConcurrently : Int = Int.MaxValue): Unit = {
    new Pump(unitClass, maximumTotal, maximumConcurrently).update()
  }
  def pumpWorkers(oversaturate: Boolean = false, maximumTotal: Int = 75, maximumConcurrently: Int = 2): Unit = {
    new PumpWorkers(oversaturate, maximumTotal, maximumConcurrently).update()
  }
  def pumpSupply(): Unit = {
    new RequireSufficientSupply().update()
  }
  def pumpRatio(
    unitClass     : UnitClass,
    minimum       : Int,
    maximum       : Int,
    ratios        : Seq[MatchingRatio],
    round         : Rounding = Rounding.Up): Unit = {
    new PumpRatio(unitClass, minimum, maximum, ratios, round).update()
  }
  def pumpShuttleAndReavers(reavers: Int = 50, shuttleFirst: Boolean = true): Unit = {
    new PumpShuttleAndReavers(reavers, shuttleFirst).update()
  }
  def makeDarkArchons(): Unit = {
    With.blackboard.makeDarkArchons.set(true)
  }
  def pylonBlock(): Unit = {
    new PylonBlock().update()
  }
  def upgradeContinuously(upgrade: Upgrade, maxLevel: Int = 3): Unit = {
    new UpgradeContinuously(upgrade, maxLevel).update()
  }
  def extractorTrick(): Unit = {
    new ExtractorTrick().update()
  }
  def cancel(buildables: Buildable*): Unit = {
    With.blackboard.toCancel.set(With.blackboard.toCancel() ++ buildables)
  }
  def buildGasPumps(quantity: Int = Int.MaxValue): Unit = {
    new BuildGasPumps(quantity).update()
  }
  def requireBases(count: Int): Unit = {
    new RequireBases(count).update()
  }
  def requireMiningBases(count: Int): Unit = {
    new RequireMiningBases(count).update()
  }

  def buildDefensesAtBase(count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel], base: Base): Unit = {
    def query(buildingClass: UnitClass): PlacementQuery = new PlacementQuery(buildingClass)
      .requireBase(base)
      .requireLabelYes(PlaceLabels.Defensive)
      .preferLabelYes(labels: _*)
    if (defenseClass.requiresPsi) get(Protoss.Pylon, query(Protoss.Pylon))
    get(count, Protoss.PhotonCannon, query(Protoss.PhotonCannon))
  }
  def buildDefensesAt(count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel], bases: Seq[Base]): Unit = {
    if (defenseClass == Terran.MissileTurret)  get(Terran.EngineeringBay)
    if (defenseClass == Protoss.PhotonCannon)  get(Protoss.Forge)
    if (defenseClass == Zerg.SporeColony)      get(Zerg.EvolutionChamber)
    bases.foreach(buildDefensesAtBase(count, defenseClass, labels, _))

  }
  def buildDefenseAtBases(count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = {
    buildDefensesAt(count, defenseClass, labels, With.geography.ourBases)
  }
  def buildDefenseAtMain(count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = {
    buildDefensesAt(count, defenseClass, labels, Seq(With.geography.ourMain))
  }
  def buildDefenseAtNatural(count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = {
    buildDefensesAt(count, defenseClass, labels, With.geography.ourBases.filter(_.isOurNatural))
  }
  def buildDefenseAtExpansions(count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = {
    buildDefensesAt(count, defenseClass, labels, With.geography.ourBases.filterNot(_.isOurMain).filterNot(_.isOurNatural))
  }
  def buildDefenseAtOpenings(count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = {
    buildDefensesAt(count, defenseClass, labels, With.geography.ourBases.filterNot(_.natural.exists(_.isOurs)))
  }
  def buildCannonsAtBases         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Protoss.PhotonCannon,   labels)
  def buildCannonsAtMain          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Protoss.PhotonCannon,   labels)
  def buildCannonsAtNatural       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Protoss.PhotonCannon,   labels)
  def buildCannonsAtExpansions    (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Protoss.PhotonCannon,   labels)
  def buildCannonsAtOpenings      (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Protoss.PhotonCannon,   labels)
  def buildBatteriesAtBases       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Protoss.ShieldBattery,  labels)
  def buildBatteriesAtMain        (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Protoss.ShieldBattery,  labels)
  def buildBatteriesAtNatural     (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Protoss.ShieldBattery,  labels)
  def buildBatteriesAtExpansions  (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Protoss.ShieldBattery,  labels)
  def buildBatteriesAtOpenings    (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Protoss.ShieldBattery,  labels)
  def buildBunkersAtBases         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Terran.Bunker,          labels)
  def buildBunkersAtMain          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Terran.Bunker,          labels)
  def buildBunkersAtNatural       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Terran.Bunker,          labels)
  def buildBunkersAtExpansions    (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Terran.Bunker,          labels)
  def buildBunkersAtOpenings      (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Terran.Bunker,          labels)
  def buildTurretsAtBases         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Terran.MissileTurret,   labels)
  def buildTurretsAtMain          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Terran.MissileTurret,   labels)
  def buildTurretsAtNatural       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Terran.MissileTurret,   labels)
  def buildTurretsAtExpansions    (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Terran.MissileTurret,   labels)
  def buildTurretsAtOpenings      (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Terran.MissileTurret,   labels)
  def buildSunkensAtBases         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Zerg.SunkenColony,      labels)
  def buildSunkensAtMain          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Zerg.SunkenColony,      labels)
  def buildSunkensAtNatural       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Zerg.SunkenColony,      labels)
  def buildSunkensAtExpansions    (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Zerg.SunkenColony,      labels)
  def buildSunkensAtOpenings      (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Zerg.SunkenColony,      labels)
  def buildSporesAtBases          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Zerg.SporeColony,       labels)
  def buildSporesAtMain           (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Zerg.SporeColony,       labels)
  def buildSporesAtNatural        (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Zerg.SporeColony,       labels)
  def buildSporesAtExpansions     (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Zerg.SporeColony,       labels)
  def buildSporesAtOpenings       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Zerg.SporeColony,       labels)

  def roll(key: String, orobability: Double): Boolean = { With.strategy.roll(key, orobability) }
}