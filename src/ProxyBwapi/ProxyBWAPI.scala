package ProxyBwapi

import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Techs._
import ProxyBwapi.UnitClasses._
import ProxyBwapi.Upgrades._

import scala.collection.JavaConverters._

class ProxyBWAPI {
  lazy val unitClasses      : Vector[UnitClass]   = UnitTypes.all.map(UnitClass)
  lazy val unitClassesById  : Vector[UnitClass]   = Range(0, unitClasses.map(_.id).max).map(i => unitClasses.find(_.id == i).getOrElse(UnitClasses.UnitClasses.None)).toVector
  lazy val techs            : Vector[Tech]        = TechTypes.all.map(Tech)
  lazy val techsById        : Vector[Tech]        = Range(0, techs.map(_.id).max).map(i => techs.find(_.id == i).getOrElse(Techs.None)).toVector
  lazy val upgrades         : Vector[Upgrade]     = UpgradeTypes.all.map(Upgrade)
  lazy val upgradesById     : Vector[Upgrade]     = Range(0, upgrades.map(_.id).max).map(i => upgrades.find(_.id == i).getOrElse(Upgrades.Upgrades.None)).toVector
  lazy val players          : Vector[PlayerInfo]  = With.game.getPlayers.asScala.map(PlayerInfo).toVector
  lazy val playersById      : Vector[PlayerInfo]  = Range(0, players.map(_.id).max).map(i => players.find(_.id == i).orNull).toVector
}
