package ProxyBwapi

import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Techs.{Tech, TechTypes}
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.Upgrades.{Upgrade, UpgradeTypes}

import scala.collection.JavaConverters._

class ProxyBWAPI {
  val unitClasses           : Vector[UnitClass]  = UnitTypes.all.map(UnitClass)
  lazy val unitClassesById  : Vector[UnitClass]  = (0 to unitClasses.map(_.id).max).map(i => unitClasses.find(_.id == i).getOrElse(UnitClasses.UnitClasses.None)).toVector
  val techs                 : Vector[Tech]       = TechTypes.all.map(Tech)
  lazy val techsById        : Vector[Tech]       = (0 to techs.map(_.id).max).map(i => techs.find(_.id == i).getOrElse(Techs.None)).toVector
  val upgrades              : Vector[Upgrade]    = UpgradeTypes.all.map(Upgrade)
  lazy val upgradesById     : Vector[Upgrade]    = (0 to upgrades.map(_.id).max).map(i => upgrades.find(_.id == i).getOrElse(Upgrades.Upgrades.None)).toVector
  val players               : Vector[PlayerInfo] = With.game.getPlayers.asScala.map(PlayerInfo).toVector
  lazy val playersById      : Vector[PlayerInfo] = (0 to players.map(_.id).max).map(i => players.find(_.id == i).orNull).toVector
}
