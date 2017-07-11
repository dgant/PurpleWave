package Macro.Scheduling

import Lifecycle.With
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.Upgrades.Upgrade

object Project {
  
  // Frames before we could possibly have this unit, not counting costs
  //
  def framesToUnits(unitClass: UnitClass, quantity: Int = 1, unitsInCycle: Array[UnitClass] = Array.empty): Int = {
    
    val unitsOfClass          = With.units.ours.filter(_.is(unitClass))
    val unitsOfClassComplete  = unitsOfClass.filter(_.complete)
    
    // Do we already have it?
    val shortfall = quantity - unitsOfClassComplete.size
    if (shortfall <= 0) {
      return 0
    }
    
    // Are we building what we need already?
    val incomplete = unitsOfClass.filterNot(_.complete).toVector.sortBy(_.framesBeforeBecomingComplete)
    if (incomplete.size >= shortfall) {
      incomplete
        .take(shortfall)
        .last
        .framesBeforeBecomingComplete
    }
    
    val frameLimits =
      (unitClass.buildUnitsEnabling ++
        unitClass.buildUnitsBorrowed ++
        unitClass.buildUnitsSpent)
          .toSet[UnitClass]
          .map(requiredClass =>
            if (unitsInCycle.contains(requiredClass))
              Int.MaxValue
            else
              framesToUnits(requiredClass, 1, unitsInCycle :+ requiredClass))
        
    if (frameLimits.isEmpty) {
      Int.MaxValue
    } else {
      frameLimits.max
    }
  }
  
  // Frames before we could possibly have this Tech, not counting costs
  //
  def framesToTech(tech: Tech): Int = {
    
    // Do we have it already?
    if (With.self.hasTech(tech)) {
      return 0
    }
    
    // Do we need to build the thing that techs this?
    val framesToTecher = framesToUnits(tech.whatResearches)
    if (framesToTecher > 0) {
      return framesToTecher + tech.researchFrames
    }
    
    // How much longer before the tech finishes?
    val techer = With.units.ours.find(unit => unit.teching && unit.techingType == tech)
    if (techer.isDefined) {
      return techer.get.remainingTechFrames
    }
  
    tech.researchFrames
  }
  
  def framesToUpgrade(upgrade: Upgrade, level: Int = 1): Int = {
    
    // Do we have it already?
    if (With.self.getUpgradeLevel(upgrade) >= level) {
      return 0
    }
    
    // Are we not even at the previous level?
    if (With.self.getUpgradeLevel(upgrade) < level - 1) {
      framesToUpgrade(upgrade, level - 1) + upgrade.upgradeTime(level)
    }
    
    // Do we need to build the thing that upgrades this?
    val framesToUpgrader = framesToUnits(upgrade.whatUpgrades)
    if (framesToUpgrader > 0) {
      return framesToUpgrader + upgrade.upgradeTime(level) //Doesn't count catching up to this level from lower levels!
    }
  
    // How much longer before the upgrade finishes?
    val upgrader = With.units.ours.find(unit => unit.upgrading && unit.upgradingType == upgrade)
    if (upgrader.isDefined) {
      return upgrader.get.remainingUpgradeFrames
    }
  
    upgrade.upgradeTime(level)
  }
}
