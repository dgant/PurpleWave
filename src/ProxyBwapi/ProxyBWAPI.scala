package ProxyBwapi

import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Techs._
import ProxyBwapi.UnitClasses._
import ProxyBwapi.Upgrades._
import bwapi.{TechType, UnitType, UpgradeType}

import scala.collection.JavaConverters._

class ProxyBWAPI {
  lazy val unitClassByType  : Map[UnitType, UnitClass]  = UnitTypes.all.map(unitType => (unitType,                    UnitClass (unitType))).toMap
  lazy val techsByType      : Map[TechType, Tech]       = TechTypes.all.map(techType => (techType,                    Tech(techType))).toMap
  lazy val upgradesByType   : Map[UpgradeType, Upgrade] = UpgradeTypes.all.map(upgradeType => (upgradeType,           Upgrade(upgradeType))).toMap
  lazy val playersById      : Map[Int, PlayerInfo]      = With.game.getPlayers.asScala.map(player => (player.getID, PlayerInfo(player))).toMap
}
