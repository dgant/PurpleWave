package Macro.Scheduling

import Lifecycle.With
import Performance.Cache
import Planning.UnitMatchers.{MatchAnd, MatchComplete}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import Utilities.ByOption

import scala.collection.mutable

class Projections {

  def unit(unitClass: UnitClass): Int = unitCaches(unitClass)()
  def tech(tech: Tech): Int = techCaches(tech)()
  def upgrade(upgrade: Upgrade, level: Int = 0): Int = upgradeCaches((upgrade, level))()

  private val unitCaches = new mutable.HashMap[UnitClass, Cache[Int]] {
    override def default(key: UnitClass): Cache[Int] = {
      put(key, new Cache(() => framesToUnits(key)))
      this(key)
    }
  }
  private val techCaches = new mutable.HashMap[Tech, Cache[Int]] {
    override def default(key: Tech): Cache[Int] = {
      put(key, new Cache(() => framesToTech(key)))
      this(key)
    }
  }
  private val upgradeCaches = new mutable.HashMap[(Upgrade, Int), Cache[Int]] {
    override def default(key: (Upgrade, Int)): Cache[Int] = {
      put(key, new Cache(() => framesToUpgrade(key._1, key._2)))
      this(key)
    }
  }

  private val forever: Int = 24 * 60 * 120

  // Frames before we could possibly have this unit, not counting costs
  //
  private def framesToUnits(
    unitClass: UnitClass,
    // Performance: Avoid creating an empty array if necessary
    unitsInCycle: Array[UnitClass] = null): Int = {

    // Do we have it already?
    if (With.units.existsOurs(MatchAnd(unitClass, MatchComplete))) return 0

    // Are we building what we need already?
    val soonestUnit = ByOption.minBy(With.units.ours.view.filter(_.isPrerequisite(unitClass)))(_.remainingCompletionFrames)
    if (soonestUnit.isDefined) {
      return soonestUnit.get.remainingCompletionFrames
    }

    // Do we need to build other units to build this?
    val frameLimits =
      (unitClass.buildUnitsEnabling ++
        unitClass.buildUnitsBorrowed ++
        unitClass.buildUnitsSpent)
          .distinct
          .map(requiredClass =>
            if (unitsInCycle != null && unitsInCycle.contains(requiredClass))
              forever
            else
              framesToUnits(
                requiredClass,
                if (unitsInCycle == null) Array(requiredClass) else unitsInCycle :+ requiredClass))

    unitClass.buildFrames + ByOption.max(frameLimits).getOrElse(0)
  }

  // Frames before we could possibly have this Tech, not considering income
  //
  private def framesToTech(tech: Tech): Int = {

    // Do we have it already?
    if (With.self.hasTech(tech)) {
      return 0
    }

    // How much longer before the tech finishes?
    val techer = With.units.ours.find(unit => unit.teching && unit.techingType == tech)
    if (techer.isDefined) {
      return techer.get.remainingTechFrames
    }

    // Do we need to build the thing that techs this?
    val framesToTecher = framesToUnits(tech.whatResearches)
    tech.researchFrames + framesToTecher
  }

  // Frames before we could possibly have this Upgrade, not considering income
  //
  private def framesToUpgrade(upgrade: Upgrade, level: Int = 1): Int = {

    // Do we have it already?
    if (With.self.getUpgradeLevel(upgrade) >= level) {
      return 0
    }

    // Are we not even at the previous level?
    if (With.self.getUpgradeLevel(upgrade) < level - 1) {
      framesToUpgrade(upgrade, level - 1) + upgrade.upgradeFrames(level)
    }

    // How much longer before the upgrade finishes?
    val upgrader = With.units.ours.find(unit => unit.upgrading && unit.upgradingType == upgrade)
    if (upgrader.isDefined) {
      return upgrader.get.remainingUpgradeFrames
    }

    // Do we need to build the thing that upgrades this?
    val framesToUpgrader = framesToUnits(upgrade.whatUpgrades)
    framesToUpgrader + upgrade.upgradeFrames(level)
  }
}
