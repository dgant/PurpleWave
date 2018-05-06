package ProxyBwapi

import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Techs._
import ProxyBwapi.UnitClasses._
import ProxyBwapi.Upgrades._
import bwapi.{TechType, UnitType, UpgradeType}

import scala.collection.JavaConverters._

class ProxyBWMirror {
  
  /*
  Store proxied versions of BWMirror enumerated types indexed both by BWMirror enum type as well as by name.
  
  The reason we need both is because BWMirror doesn't always return a singleton object when getting, say, a UnitType.
  It may return a distinct object which internally refers to the same BWAPI pointer. But due to the lack of hashcode
  function we can't index into a dictionary using it.
  
  So for mapping, say, a UnitType -> UnitClass, we'd first try indexing the UnitType, then as a fallback the name.
   */
  
  lazy val unitClassByName  : Map[String, UnitClass]    = UnitTypes.all.map(unitType => (unitType.toString,           UnitClass (unitType))).toMap
  lazy val unitClassByType  : Map[UnitType, UnitClass]  = UnitTypes.all.map(unitType => (unitType,                    unitClassByName(unitType.toString))).toMap
  lazy val techsByName      : Map[String, Tech]         = TechTypes.all.map(techType => (techType.toString,           Tech(techType))).toMap
  lazy val techsByType      : Map[TechType, Tech]       = TechTypes.all.map(techType => (techType,                    techsByName(techType.toString))).toMap
  lazy val upgradesByName   : Map[String, Upgrade]      = UpgradeTypes.all.map(upgradeType => (upgradeType.toString,  Upgrade(upgradeType))).toMap
  lazy val upgradesByType   : Map[UpgradeType, Upgrade] = UpgradeTypes.all.map(upgradeType => (upgradeType,           upgradesByName(upgradeType.toString))).toMap
  lazy val playersById      : Map[Int, PlayerInfo]      = With.game.getPlayers.asScala.map(player => (player.getID, PlayerInfo(player))).toMap
}
