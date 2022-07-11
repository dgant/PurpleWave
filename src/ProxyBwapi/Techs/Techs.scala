package ProxyBwapi.Techs

import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import bwapi.TechType

object Techs {
  def all: Vector[Tech] = With.proxy.techs
  def get(tech: TechType): Tech = With.proxy.techsById(tech.id)
  def None: Tech = all.find(_.bwapiTech == TechType.None).get
  def Unknown: Tech = all.find(_.bwapiTech == TechType.Unknown).get
  lazy val free: Set[Tech] = Set(
    Protoss.ArchonMeld,
    Protoss.DarkArchonMeld,
    Zerg.DarkSwarm,
    Terran.DefensiveMatrix,
    Protoss.Feedback,
    Terran.Healing,
    Zerg.InfestCommandCenter,
    Terran.NuclearStrike,
    Zerg.Parasite,
    Terran.ScannerSweep)
  lazy val nonFree: Set[Tech] = all.toSet.diff(free)
}
