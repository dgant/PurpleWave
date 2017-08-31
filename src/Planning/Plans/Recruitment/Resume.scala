package Planning.Plans.Recruitment

import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class Resume(unit: FriendlyUnitInfo) {
  
  lazy val antiAir        : Boolean = unit.unitClass.attacksGround
  lazy val antiGround     : Boolean = unit.unitClass.attacksGround
  lazy val airToAir       : Boolean = unit.flying && antiAir
  lazy val airToGround    : Boolean = unit.flying && antiGround
  lazy val detects        : Boolean = unit.unitClass.isDetector
  lazy val heals          : Boolean = unit.is(Terran.Medic)
  lazy val repairs        : Boolean = unit.is(Terran.SCV)
  lazy val builds         : Boolean = unit.unitClass.isWorker
  lazy val splashesAir    : Boolean = ResumeFeatures.allSplash.contains(unit.unitClass) || ResumeFeatures.airSplash.contains(unit.unitClass)
  lazy val splashesGround : Boolean = ResumeFeatures.allSplash.contains(unit.unitClass) || ResumeFeatures.groundSplash.contains(unit.unitClass)
  lazy val spots          : Boolean = unit.flying
  lazy val transports     : Boolean = unit.is(Terran.Dropship) || unit.is(Protoss.Shuttle) || (unit.is(Protoss.Arbiter) && unit.energy > 125 && With.self.hasTech(Protoss.Recall)) || (unit.is(Zerg.Overlord) && With.self.hasUpgrade(Zerg.OverlordDrops))
  lazy val sieges         : Boolean = ResumeFeatures.siege.contains(unit.unitClass)
  
}