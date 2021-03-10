package ProxyBwapi.Techs

import Lifecycle.With
import bwapi.TechType

object Techs {
  def all: Vector[Tech] = With.proxy.techs
  def get(tech: TechType): Tech = With.proxy.techsById(tech.id)
  def None: Tech = all.find(_.bwapiTech == TechType.None).get
  def Unknown: Tech = all.find(_.bwapiTech == TechType.Unknown).get
}
