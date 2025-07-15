package Macro.Actions

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Actions.Rounding.Rounding
import Macro.Facts.MacroCounting
import Macro.Requests._
import Mathematics.Maff
import Placement.Access.PlaceLabels.{DefendAir, DefendEntrance, DefendGround, MacroHatch, PlaceLabel}
import Placement.Access.{PlaceLabels, PlacementQuery}
import Placement.FoundationSources
import ProxyBwapi.Buildable
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import Utilities.?
import Utilities.UnitFilters.UnitFilter

trait MacroActions extends MacroCounting {

  def status(text: Any): Unit = With.blackboard.status.set(With.blackboard.status() :+ ?(text == null, "", text.toString))
  def status(predicate: Boolean, text: => Any): Unit = if (predicate) status(text)

  def recordRequestedBases(): Unit = {
    val max = Maff.max(With.scheduler.requests.view.map(_.request).filter(_.unit.exists(_.isTier1TownHall)).map(_.quantity)).getOrElse(0)
    status(max > 0, f"${max}base")
  }

  def attack(value: Boolean = true) : Unit = if (value) With.blackboard.wantToAttack.set(true)
  def harass(value: Boolean = true) : Unit = if (value) With.blackboard.wantToHarass.set(true)
  def allIn (value: Boolean = true) : Unit = if (value) { With.blackboard.yoloing.set(true); attack() }

  def aggression(value: Double): Unit = {
    With.blackboard.aggressionRatio.set(value)
  }

    def scout(scoutCount: Int = 1): Unit = {
    With.blackboard.maximumScouts.set(Math.max(With.blackboard.maximumScouts(), scoutCount))
  }
  def scoutOn(unitMatcher: UnitFilter, quantity: Int = 1, scouts: Int = 1): Unit = if (With.units.ours.count(unitMatcher) >= quantity)  scout(scouts)
  def scoutAt(minimumSupply: Int,                         scouts: Int = 1): Unit = if (supplyUsed200 >= minimumSupply)                  scout(scouts)

  def gasWorkerFloor  (value: Int): Unit = With.blackboard.gasWorkerFloor   .set(value)
  def gasWorkerCeiling(value: Int): Unit = With.blackboard.gasWorkerCeiling .set(value)
  def gasLimitFloor   (value: Int): Unit = With.blackboard.gasLimitFloor    .set(value)
  def gasLimitCeiling (value: Int): Unit = With.blackboard.gasLimitCeiling  .set(value)

  def get(item: RequestBuildable): Unit = With.scheduler.request(this, item)
  def get(units: UnitClass*)                                              : Unit = units.foreach(get(1, _))
  def get(unit: UnitClass, placementQuery: PlacementQuery)                : Unit = get(1, unit, placementQuery)
  def get(unit: UnitClass, base: Base)                                    : Unit = get(1, unit, base)
  def get(quantity: Int, unit: UnitClass)                                 : Unit = get(RequestUnit(unit, quantity))
  def get(quantity: Int, unit: UnitClass, placementQuery: PlacementQuery) : Unit = get(RequestUnit(unit, quantity, placementQueryArg = Some(placementQuery)))
  def get(quantity: Int, unit: UnitClass, labels: PlaceLabel*)            : Unit = get(1, unit, new PlacementQuery(unit).requireLabelYes(labels: _*))
  def get(quantity: Int, unit: UnitClass, base: Base)                     : Unit = get(1, unit, new PlacementQuery(unit).requireBase(base))
  def get(quantity: Int, unit: UnitClass, base: Base, labels: PlaceLabel*): Unit = get(1, unit, new PlacementQuery(unit).requireBase(base).requireLabelYes(labels: _*))
  def get(upgrade: Upgrade)                                               : Unit = get(RequestUpgrade(upgrade))
  def get(upgrade: Upgrade, level: Int)                                   : Unit = get(Get(level, upgrade))
  def get(tech: Tech)                                                     : Unit = get(RequestTech(tech))

  def once(upgrade: Upgrade)              : Unit = get(upgrade)
  def once(upgrade: Upgrade, level: Int)  : Unit = get(upgrade, level)
  def once(tech: Tech)                    : Unit = get(tech)
  def once(quantity: Int, unit: UnitClass): Unit = BuildOnce(this, Get(quantity, unit))
  def once(units: UnitClass*)             : Unit = units.foreach(once(1, _))

  def autosupply(): Unit = {
    With.scheduler.request(this, RequestAutosupply)
  }
  def pump(unitClass: UnitClass, maximumTotal: Int = 400, maximumConcurrently : Int = 400): Unit = {
    Pump(unitClass, maximumTotal, maximumConcurrently)
  }
  def pumpWorkers(oversaturate: Boolean = false, maximumTotal: Int = 75, maximumConcurrently: Int = 2): Unit = {
    PumpWorkers(oversaturate, maximumTotal, maximumConcurrently)
  }
  def pumpRatio(unitClass: UnitClass, minimum: Int, maximum: Int, ratios: Seq[MatchingRatio], round: Rounding = Rounding.Up): Unit = {
    PumpRatio(unitClass, minimum, maximum, ratios, round)
  }
  def pumpShuttleAndReavers(reavers: Int = 50, shuttleFirst: Boolean = true): Unit = {
    PumpShuttleAndReavers(reavers, shuttleFirst)
  }
  def makeDarkArchons(): Unit = {
    With.blackboard.makeDarkArchons.set(true)
  }
  def makeArchons(energy: Int = 300): Unit = {
    With.blackboard.maximumArchonEnergy.set(energy)
  }
  def roll(key: String, probability: Double): Boolean = {
    With.strategy.roll(key, probability)
  }
  def upgradeContinuously(upgrade: Upgrade, maxLevel: Int = 3): Boolean = {
    UpgradeContinuously(upgrade, maxLevel)
  }
  def cancel(buildables: Buildable*): Unit = {
    With.blackboard.toCancel.set(With.blackboard.toCancel() ++ buildables)
  }
  def pumpGasPumps(quantity: Int = 400): Unit = {
    PumpGasPumps(quantity)
  }

  // For expansion logic, avoid relying on info from Geography, which can lag and cause float or potentially double-expanding
  private def completeBases       : Int = ourBaseTownHalls.count(b => b.complete)
  private def completeMiningBases : Int = ourBaseTownHalls.count(b => b.complete && b.base.exists(isMiningBase))

  def expandNTimes        (times: Int)    : Unit = if (times > 0) get(times, With.self.townHallClass, new PlacementQuery(With.self.townHallClass).requireBase(With.geography.bases.filterNot(_.townHall.exists(_.openForBusiness)): _*))
  def expandOnce()                        : Unit = expandNTimes(1)
  def requireBases        (count: Int)    : Unit = expandNTimes(count - completeBases)
  def requireMiningBases  (count: Int)    : Unit = expandNTimes(count - completeMiningBases)
  def approachBases       (count: Int)    : Unit = if (completeBases        < count) expandOnce()
  def approachMiningBases (count: Int)    : Unit = if (completeMiningBases  < count) expandOnce()
  def maintainMiningBases (max: Int = 10) : Unit = approachMiningBases(Math.min(max, With.geography.maxMiningBasesOurs))

  def buildDefensesAtBase(count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel], base: Base): Unit = {
    if (count > 0) {
      def query(unitClass: UnitClass) = new PlacementQuery(unitClass)
        .requireBase(base)
        .requireLabelYes(PlaceLabels.Defensive)
        .preferLabelYes(labels: _*)
      BuildDefense(count, defenseClass, query)
    }
  }

  def buildDefensesAt(count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel], bases: Seq[Base]): Unit = {
    bases.foreach(buildDefensesAtBase(count, defenseClass, labels, _))
  }

  def buildDefenseAtBases         (count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = buildDefensesAt(count, defenseClass, labels, With.geography.ourBases)
  def buildDefenseAtMain          (count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = buildDefensesAt(count, defenseClass, labels, Seq(With.geography.ourMain))
  def buildDefenseAtNatural       (count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = buildDefensesAt(count, defenseClass, labels, Seq(With.geography.ourNatural))
  def buildDefenseAtFoyer         (count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = buildDefensesAt(count, defenseClass, labels, Seq(With.geography.ourFoyer))
  def buildDefenseAtExpansions    (count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = buildDefensesAt(count, defenseClass, labels, With.geography.ourBases.filterNot(_.isOurMain).filterNot(_.isOurNatural))
  def buildDefenseAtOpenings      (count: Int, defenseClass: UnitClass, labels: Seq[PlaceLabel]): Unit = buildDefensesAt(count, defenseClass, labels, With.geography.ourMetros.flatMap(m => m.exits.flatMap(_.zones.flatMap(_.bases)).filter(m.bases.contains)))

  def buildCannonsAtBases         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Protoss.PhotonCannon,   labels)
  def buildCannonsAtMain          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Protoss.PhotonCannon,   labels)
  def buildCannonsAtNatural       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Protoss.PhotonCannon,   labels)
  def buildCannonsAtFoyer         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtFoyer       (count, Protoss.PhotonCannon,   labels)
  def buildCannonsAtExpansions    (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Protoss.PhotonCannon,   labels)
  def buildCannonsAtOpenings      (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Protoss.PhotonCannon,   labels)
  def buildBatteriesAtBases       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Protoss.ShieldBattery,  labels)
  def buildBatteriesAtMain        (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Protoss.ShieldBattery,  labels)
  def buildBatteriesAtNatural     (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Protoss.ShieldBattery,  labels)
  def buildBatteriesAtFoyer       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtFoyer       (count, Protoss.ShieldBattery,  labels)
  def buildBatteriesAtExpansions  (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Protoss.ShieldBattery,  labels)
  def buildBatteriesAtOpenings    (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Protoss.ShieldBattery,  labels)
  def buildBunkersAtBases         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Terran.Bunker,          labels)
  def buildBunkersAtMain          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Terran.Bunker,          labels)
  def buildBunkersAtNatural       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Terran.Bunker,          labels)
  def buildBunkersAtFoyer         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtFoyer       (count, Terran.Bunker,          labels)
  def buildBunkersAtExpansions    (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Terran.Bunker,          labels)
  def buildBunkersAtOpenings      (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Terran.Bunker,          labels)
  def buildTurretsAtBases         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Terran.MissileTurret,   ?(labels.contains(DefendEntrance) || labels.contains(DefendGround), labels, labels :+ DefendAir))
  def buildTurretsAtMain          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Terran.MissileTurret,   ?(labels.contains(DefendEntrance) || labels.contains(DefendGround), labels, labels :+ DefendAir))
  def buildTurretsAtNatural       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Terran.MissileTurret,   ?(labels.contains(DefendEntrance) || labels.contains(DefendGround), labels, labels :+ DefendAir))
  def buildTurretsAtFoyer         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtFoyer       (count, Terran.MissileTurret,   ?(labels.contains(DefendEntrance) || labels.contains(DefendGround), labels, labels :+ DefendAir))
  def buildTurretsAtExpansions    (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Terran.MissileTurret,   ?(labels.contains(DefendEntrance) || labels.contains(DefendGround), labels, labels :+ DefendAir))
  def buildTurretsAtOpenings      (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Terran.MissileTurret,   ?(labels.contains(DefendEntrance) || labels.contains(DefendGround), labels, labels :+ DefendAir))
  def buildSunkensAtBases         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Zerg.SunkenColony,      labels :+ DefendGround)
  def buildSunkensAtMain          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Zerg.SunkenColony,      labels :+ DefendGround)
  def buildSunkensAtNatural       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Zerg.SunkenColony,      labels :+ DefendGround)
  def buildSunkensAtFoyer         (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtFoyer       (count, Zerg.SunkenColony,      labels :+ DefendGround)
  def buildSunkensAtExpansions    (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Zerg.SunkenColony,      labels :+ DefendGround)
  def buildSunkensAtOpenings      (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Zerg.SunkenColony,      labels :+ DefendGround)
  def buildSporesAtBases          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtBases       (count, Zerg.SporeColony,       labels :+ DefendAir)
  def buildSporesAtMain           (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtMain        (count, Zerg.SporeColony,       labels :+ DefendAir)
  def buildSporesAtNatural        (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtNatural     (count, Zerg.SporeColony,       labels :+ DefendAir)
  def buildSporesAtFoyer          (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtFoyer       (count, Zerg.SporeColony,       labels :+ DefendAir)
  def buildSporesAtExpansions     (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtExpansions  (count, Zerg.SporeColony,       labels :+ DefendAir)
  def buildSporesAtOpenings       (count: Int, labels: PlaceLabel*): Unit = buildDefenseAtOpenings    (count, Zerg.SporeColony,       labels :+ DefendAir)

  def fillMacroHatches(totalHatcheryCount: Int, preferredBases: Base*): Unit = {
    get(
      totalHatcheryCount,
      Zerg.Hatchery,
      new PlacementQuery(Zerg.Hatchery)
        .foundationSource(FoundationSources.Default)
        .requireLabelYes()
        .preferLabelYes(MacroHatch)
        .preferBase(
          Maff.orElse(
            preferredBases,
            With.geography.ourMiningBases,
            With.geography.ourBases,
            With.geography.ourMetros.flatMap(_.bases),
            With.geography.ourBasesAndSettlements).toSeq: _*))
  }

  def sneakyCitadel(): Unit = {
    SneakyCitadel()
  }
}