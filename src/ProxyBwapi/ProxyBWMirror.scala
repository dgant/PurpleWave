package ProxyBwapi

import ProxyBwapi.Techs._
import ProxyBwapi.UnitClass._
import ProxyBwapi.Upgrades._
import bwapi.{TechType, UnitType, UpgradeType}

class ProxyBWMirror {
  
  lazy val namesByUnitType      : Map[UnitType, String]     = UnitTypes     .all.map(unitType     => (unitType,           unitType.toString))           .toMap
  lazy val unitClassByTypeName  : Map[String, UnitClass]    = UnitTypes     .all.map(unitType     => (unitType.toString,  new UnitClass (unitType)))    .toMap
  lazy val techsByType          : Map[TechType, Tech]       = TechTypes     .all.map(techType     => (techType,           new Tech      (techType)))    .toMap
  lazy val upgradesByType       : Map[UpgradeType, Upgrade] = UpgradeTypes  .all.map(upgradeType  => (upgradeType,        new Upgrade   (upgradeType))) .toMap
}
