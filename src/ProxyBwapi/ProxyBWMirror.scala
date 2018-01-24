package ProxyBwapi

import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Techs._
import ProxyBwapi.UnitClass._
import ProxyBwapi.Upgrades._
import bwapi.{TechType, UnitType, UpgradeType}

import scala.collection.JavaConverters._

class ProxyBWMirror {
  
  lazy val namesByUnitType      : Map[UnitType, String]     = UnitTypes     .all.map(unitType     => (unitType,           unitType.toString))         .toMap
  lazy val unitClassByTypeName  : Map[String, UnitClass]    = UnitTypes     .all.map(unitType     => (unitType.toString,  new UnitClass (unitType)))  .toMap
  lazy val techsByType          : Map[TechType, Tech]       = TechTypes     .all.map(techType     => (techType,           Tech        (techType)))    .toMap
  lazy val upgradesByType       : Map[UpgradeType, Upgrade] = UpgradeTypes  .all.map(upgradeType  => (upgradeType,        Upgrade     (upgradeType))) .toMap
  lazy val playersByPlayer      : Map[Int, PlayerInfo]      = With.game.getPlayers.asScala.map(player => (player.getID, PlayerInfo(player))).toMap
}
