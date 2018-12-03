package Macro.Scheduling

import Lifecycle.With
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import Utilities.ByOption

object Project {
  
  val forever = 24 * 60 * 120
  
  // Frames before we could possibly have this unit, not counting costs
  //
  def framesToUnits(
    unitClass: UnitClass,
    quantity: Int = 1,
    // Performance: Avoid creating an empty array if necessary
    unitsInCycle: Array[UnitClass] = null): Int = {

    val unitsOfClass          = With.units.ours.view.filter(_.isPrerequisite(unitClass))
    val unitsOfClassComplete  = unitsOfClass.count(_.complete)
    
    // Do we already have it?
    val shortfall = quantity - unitsOfClassComplete
    if (shortfall <= 0) {
      return 0
    }
    
    // Are we building what we need already?
    val incompleteCount = unitsOfClass.count(!_.complete)
    if (incompleteCount >= shortfall) {
      return unitsOfClass
        .toArray
        .filter(!_.complete)
        .sortBy(_.remainingCompletionFrames)
        .take(shortfall)
        .last
        .remainingCompletionFrames
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
                1,
                if (unitsInCycle == null) Array(requiredClass) else unitsInCycle :+ requiredClass))
    
    unitClass.buildFrames + ByOption.max(frameLimits).getOrElse(0)
  }
  
  // Frames before we could possibly have this Tech, not considering income
  //
  def framesToTech(tech: Tech): Int = {
    
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
  def framesToUpgrade(upgrade: Upgrade, level: Int = 1): Int = {
    
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
