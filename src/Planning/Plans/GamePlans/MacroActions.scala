package Planning.Plans.GamePlans

import Lifecycle.With
import Macro.Requests.{RequestBuildable, Get}
import Planning.Plan
import Planning.Plans.Macro.Automatic.Rounding.Rounding
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{BuildOrder, RequireEssentials}
import Planning.Plans.Macro.{CancelIncomplete, CancelOrders}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Scouting.{ScoutAt, ScoutOn}
import Utilities.UnitFilters.UnitFilter
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade

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

  def get(unit: UnitClass): Unit = get(Get(unit))
  def get(quantity: Int, unit: UnitClass): Unit = get(Get(quantity, unit))
  def get(upgrade: Upgrade): Unit = get(Get(upgrade))
  def get(upgrade: Upgrade, level: Int): Unit = get(Get(level, upgrade))
  def get(tech: Tech): Unit = get(Get(tech))
  def once(unit: UnitClass): Unit = buildOrder(Get(1, unit))
  def once(upgrade: Upgrade): Unit = get(upgrade)
  def once(upgrade: Upgrade, level: Int): Unit = get(upgrade, level)
  def once(tech: Tech): Unit = get(tech)
  def once(quantity: Int, unit: UnitClass): Unit = buildOrder(Get(quantity, unit))
  def get(item: RequestBuildable): Unit = {
    With.scheduler.request(_requesterPlan, item)
  }
  private def _requesterPlan = new Plan()

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
  def cancelIncomplete(matchers: UnitFilter*): Unit = {
    new CancelIncomplete(matchers: _*).update()
  }
  def cancelOrders(matchers: UnitFilter*): Unit = {
    new CancelOrders(matchers: _*).update()
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

  def roll(key: String, orobability: Double): Boolean = { With.strategy.roll(key, orobability) }
}